package edu.usc.softarch.arcade.topics;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.topics.pipes.CamelCaseSeparatorPipe;
import edu.usc.softarch.arcade.topics.pipes.StemmerPipe;
import edu.usc.softarch.arcade.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility to automate execution of Mallet.
 */
public class MalletRunner {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		if (args.length < 6)
			run(args[0], args[1], args[2], args[3], args[4]);
		else
			run(args[0], args[1], args[2], args[3], args[4], Boolean.parseBoolean(args[5]));
	}

	public static void run(String sourceDir, String language, String malletPath,
			String artifactsPath, String stopWordDir) throws IOException {
		run(sourceDir, language, malletPath, artifactsPath, stopWordDir, false);
	}

	public static void run(String sourceDir, String language,
			String malletPath, String artifactsPath, String stopWordDir,
			boolean isBatch) throws IOException {
		MalletRunner runner = new MalletRunner(sourceDir, language,
			malletPath, artifactsPath, stopWordDir);
		runner.copySource();
		if (isBatch)
			runner.runPipeExtractorBatch();
		else
			runner.runPipeExtractor();
		runner.runTopicModeling();
		runner.runInferencer();
		runner.cleanUp();
	}
	//endregion

	//region ATTRIBUTES
	private static final char fs = File.separatorChar;
	private final File sourceDir;
	private final File targetDir;
	private final String language;
	private final String malletPath;
	private final String artifactsPath;
	private final String stopWordDir;
	//endregion

	//region CONSTRUCTORS
	public MalletRunner(String sourceDir, String language, String malletPath,
		String artifactsPath, String stopWordDir) {
		this.sourceDir = new File(sourceDir
			.replaceFirst("^~",System.getProperty("user.home")));
		this.targetDir = new File(sourceDir
			.replaceFirst("^~",System.getProperty("user.home")) + "_temp");
		this.language = language.toLowerCase();
		this.malletPath = malletPath;
		this.artifactsPath = artifactsPath;
		this.stopWordDir = stopWordDir;
	}
	//endregion

	//region PROCESSING
	/**
	 * Extracts all source artifacts from a repository, so that NLP is not
	 * tainted.
	 */
	public void copySource() throws IOException {
		sanityCheck();

		for (File file : FileUtil.getFileListing(this.sourceDir)) {
			if (file.isDirectory()) continue;
			if (this.language.equals("java")) {
				if (!file.getName().endsWith(".java")) continue;
				copyFile(file);
			} else if (this.language.equals("c")) {
				Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
				if (p.matcher(file.getName()).find()) {
					copyFile(file);
				}
			} else {
				throw new UnsupportedOperationException("Language " + this.language
					+ " is not supported. Supported languages are java and c.");
			}
		}
	}

	/**
	 * Makes sure that running this with inverted arguments won't result in
	 * deleting the user's entire repository.
	 */
	private void sanityCheck() throws IllegalArgumentException {
		if (this.targetDir.equals(this.sourceDir))
			throw new IllegalArgumentException("Target dir may not equal "
				+ "source dir.");
		if (this.targetDir.exists())
			throw new IllegalArgumentException("Target dir already exists.");
	}

	/**
	 * Copies a file from the source directory to the target directory specified
	 * in the constructor. Copies the directory structure as well.
	 */
	private void copyFile(File sourceFile) throws IOException {
		String sourcePath = sourceFile.getAbsolutePath();
		String targetPath = this.sourceDir.getAbsolutePath();
		String pathDiff = sourcePath.replace(targetPath, "");
		targetPath = this.targetDir.getAbsolutePath() + pathDiff;
		Path original = sourceFile.toPath();
		Path copy = Paths.get(targetPath);
		Files.createDirectories(copy.getParent());
		Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
	}

	public void runPipeExtractorBatch() throws IOException {
		for (File versionDir : this.targetDir.listFiles()) {
			runPipeExtractor(versionDir, versionDir.getName() + "_output.pipe");
		}
	}

	/**
	 * Extracts tokens from the source code for use in recovery.
	 */
	public void runPipeExtractor() throws IOException {
		runPipeExtractor(this.targetDir, "output.pipe");
	}

	private void runPipeExtractor(File source, String output) throws IOException {
		Collection<Pipe> pipeList = loadPipes();

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		for (File file : FileUtil.getFileListing(source)) {
			if (file.isDirectory()) continue;

			if (this.language.equals("java"))
				instances.addThruPipe(loadJavaInstance(file));
			else if (this.language.equals("c")) {
				instances.addThruPipe(loadCInstance(file));
			} else {
				throw new UnsupportedOperationException("Language " + this.language
					+ " is not supported. Supported languages are java and c.");
			}
		}

		FileUtil.checkDir(artifactsPath, true, false);
		instances.save(new File(artifactsPath, output));
	}

	/**
	 * Applies the following pipes: alphanumeric only, camel case separation,
	 * lowercase, tokenize, remove stopwords english, remove stopwords java,
	 * stem, map to features.
	 */
	private Collection<Pipe> loadPipes() {
		Collection<Pipe> pipeList = new ArrayList<>();

		pipeList.add(new CharSequenceReplace(Pattern.compile("[^A-Za-z]"),
			" "));
		pipeList.add(new CamelCaseSeparatorPipe());
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern
			.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));

		if (this.language.equals("c")) {
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
				stopWordDir + fs + "ckeywordsexpanded"),
				"UTF-8", false, false, false));
		} else if (this.language.equals("java")) {
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
				stopWordDir + fs +  "javakeywordsexpanded"),
				"UTF-8", false, false, false));
		} else {
			throw new UnsupportedOperationException("Language " + this.language
				+ " is not supported. Supported languages are java and c.");
		}

		pipeList.add(new StemmerPipe());
		pipeList.add(new TokenSequence2FeatureSequence());

		return pipeList;
	}

	/**
	 * Loads a Mallet Instance for a Java file.
	 */
	private Instance loadJavaInstance(File file) throws IOException {
		String shortClassName = file.getName()
			.replace(".java", "");
		String fullClassName = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String packageName = FileUtil.findPackageName(line);
				if (packageName != null) {
					fullClassName = packageName + "." + shortClassName;
					break;
				}
			}
			// No package declaration, because system was made by a mongrel...
			// ... or something in here failed, but that's less likely unless Java
			// changes their syntax.
			if (fullClassName.isEmpty())
				fullClassName = shortClassName;
		}

		if (fullClassName.isEmpty())
			throw new IOException("Could not identify Instance name.");

		String data = FileUtil.readFile(file.getAbsolutePath());
		return new Instance(data, "X", fullClassName, file.getAbsolutePath());
	}

	/**
	 * Loads a Mallet Instance for a C/CPP file.
	 */
	private Instance loadCInstance(File file) throws IOException {
		String depsStyleFilename = file.getAbsolutePath().replace(
			this.targetDir.getAbsolutePath() + fs, "");
		String data = FileUtil.readFile(file.getAbsolutePath());

		if (depsStyleFilename.isEmpty())
			throw new IOException("Could not identify Instance name.");

		return new Instance(data, "X", depsStyleFilename,
			file.getAbsolutePath());
	}

	/**
	 * Runs Mallet import-dir.
	 */
	public void runTopicModeling() throws IOException {
		List<String> command = new ArrayList<>();

		command.add(this.malletPath);
		command.add("import-dir");
		command.add("--input");
		command.add(targetDir.getAbsolutePath());
		command.add("--remove-stopwords");
		command.add("TRUE");
		command.add("--keep-sequence");
		command.add("TRUE");
		command.add("--output");
		command.add(artifactsPath + fs + "topicmodel.data");
		command.add("--stoplist-file");
		if (this.language.equals("java"))
			command.add(stopWordDir + fs + "javakeywordsexpanded");
		else if (this.language.equals("c"))
			command.add(stopWordDir + fs + "ckeywordsexpanded");
		else
			throw new UnsupportedOperationException("Language " + this.language
				+ " is not supported. Supported languages are java and c.");

		executeProcess(command);
	}

	/**
	 * Runs Mallet train-topics.
	 */
	public void runInferencer() throws IOException {
		List<String> command = new ArrayList<>();

		command.add(this.malletPath);
		command.add("train-topics");
		command.add("--input");
		command.add(artifactsPath + fs + "topicmodel.data");
		command.add("--inferencer-filename");
		command.add(artifactsPath + fs + "infer.mallet");
		command.add("--num-top-words");
		command.add("0");
		command.add("--num-topics");
		command.add("20");
		command.add("--num-threads");
		command.add("5");
		command.add("--num-iterations");
		command.add("250");

		executeProcess(command);
	}

	private void executeProcess(List<String> command) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.inheritIO();

		try {
			Process p = pb.start();
			p.waitFor();
		} catch(IOException e) {
			throw new IOException("Failed to start Process.", e);
		} catch (InterruptedException e) {
			throw new IOException("Failed to wait for Process.", e);
		}
	}

	public void cleanUp() {
		FileUtil.deleteNonEmptyDirectory(this.targetDir);	}
	//endregion
}
