package edu.usc.softarch.arcade.topics;

import cc.mallet.classify.tui.Text2Vectors;
import cc.mallet.classify.tui.Vectors2Vectors;
import cc.mallet.topics.tui.TopicTrainer;
import edu.usc.softarch.arcade.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

/**
 * Utility to automate execution of MALLET.
 *
 * @see DocTopics
 */
public class MalletRunner {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		if (args.length < 6)
			run(args[0], args[1], args[2], args[3]);
		else if (args.length < 8)
			run(args[0], args[1], args[2], args[3],
				Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]));
		else
			run(args[0], args[1], args[2], args[3],
				Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]),
				Integer.parseInt(args[6]), Integer.parseInt(args[7]));
	}

	/**
	 * Executes MALLET to obtain a vectors and topicmodel files, which are used
	 * in building {@link DocTopics} instances.
	 *
	 * Will create a temporary copy of the source directory containing only the
	 * relevant files, in order to avoid tainting the vectors. The temporary copy
	 * is deleted at the end of the execution.
	 *
	 * Defaults number of topics to 50 and number of iterations to 250.
	 *
	 * @param sourceDir Path to the directory containing the source code of the
	 *                  subject system.
	 * @param language Source language of the subject system.
	 * @param artifactsPath Path to the directory into which to place the outputs.
	 * @param stopWordPath Path to a stopwords file to be used in extracting the
	 *                     token vectors.
	 * @throws IOException If there is a problem accessing any of the required
	 * 										 files.
	 * @see #copySource()
	 * @see #runTopicModeling()
	 * @see #runPrune()
	 * @see #runInferencer()
	 * @see #cleanUp()
	 */
	public static void run(String sourceDir, String language,
			String artifactsPath, String stopWordPath) throws IOException {
		run(sourceDir, language, artifactsPath, stopWordPath,
			false, false);
	}

	/**
	 * Executes MALLET to obtain a vectors and topicmodel files, which are used
	 * in building {@link DocTopics} instances.
	 *
	 * Defaults number of topics to 50 and number of iterations to 250.
	 *
	 * @param sourceDir Path to the directory containing the source code of the
	 *                  subject system.
	 * @param artifactsPath Path to the directory into which to place the outputs.
	 * @param stopWordPath Path to a stopwords file to be used in extracting the
	 *                     token vectors.
	 * @param copyReady Whether to create a pruned copy of the source directory
	 *                  or to use a previously created one.
	 * @param keepCopy Whether to keep the pruned copy of the source directory
	 *                 for future executions.
	 * @throws IOException If there is a problem accessing any of the required
	 * 										 files.
	 * @see #copySource()
	 * @see #runTopicModeling()
	 * @see #runPrune()
	 * @see #runInferencer()
	 * @see #cleanUp()
	 */
	public static void run(String sourceDir, String language,
			String artifactsPath, String stopWordPath,
			boolean copyReady, boolean keepCopy) throws IOException {
		run(sourceDir, language, artifactsPath, stopWordPath,
			copyReady, keepCopy, 50, 250);
	}

	/**
	 * Executes MALLET to obtain a vectors and topicmodel files, which are used
	 * in building {@link DocTopics} instances.
	 *
	 * @param sourceDir Path to the directory containing the source code of the
	 *                  subject system.
	 * @param artifactsPath Path to the directory into which to place the outputs.
	 * @param stopWordPath Path to a stopwords file to be used in extracting the
	 *                     token vectors.
	 * @param copyReady Whether to create a pruned copy of the source directory
	 *                  or to use a previously created one.
	 * @param keepCopy Whether to keep the pruned copy of the source directory
	 *                 for future executions.
	 * @param numTopics Number of topics to use in the inferencing process.
	 * @param numIterations Number of iterations to use in the inferencing
	 *                      process.
	 * @throws IOException If there is a problem accessing any of the required
	 * 										 files.
	 * @see #copySource()
	 * @see #runTopicModeling()
	 * @see #runPrune()
	 * @see #runInferencer()
	 * @see #cleanUp()
	 */
	public static void run(String sourceDir, String language,
			String artifactsPath, String stopWordPath, boolean copyReady,
			boolean keepCopy, int numTopics, int numIterations) throws IOException {
		MalletRunner runner = new MalletRunner(sourceDir, language,
			artifactsPath, stopWordPath, numTopics, numIterations);
		if (!copyReady)
			runner.copySource();
		runner.runTopicModeling();
		runner.runPrune();
		runner.runInferencer();
		if (!keepCopy)
			runner.cleanUp();
	}
	//endregion

	//region ATTRIBUTES
	private static final char fs = File.separatorChar;
	/**
	 * Directory containing the source files of the subject system.
	 */
	private final File sourceDir;
	/**
	 * Directory containing the pruned source directory of the subject system,
	 * after removing files which are not in the requested source language.
	 */
	private final File targetDir;
	/**
	 * Source language of the subject system.
	 */
	private final String language;
	/**
	 * Path to the directory in which to place the output vectors and topicmodel
	 * files.
	 */
	private final String artifactsPath;
	/**
	 * Path to a stopwords file to be used in extracting the token vectors.
	 */
	private final String stopWordPath;
	private final int numTopics;
	private final int numIterations;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Sets up an instance of MalletRunner for execution. Defaults number of
	 * topics to 50 and number of iterations to 250.
	 *
	 * @param sourceDir Path to the directory containing the source code of the
	 *                  subject system.
	 * @param language Source language of the subject system.
	 * @param artifactsPath Path to the directory into which to place the outputs.
	 * @param stopWordPath Path to a stopwords file to be used in extracting the
	 *                     token vectors.
	 */
	public MalletRunner(String sourceDir, String language,
			String artifactsPath, String stopWordPath) {
		this(sourceDir, language, artifactsPath, stopWordPath,
			50, 250);
	}

	/**
	 * Sets up an instance of MalletRunner for execution.
	 *
	 * @param sourceDir Path to the directory containing the source code of the
	 *                  subject system.
	 * @param language Source language of the subject system.
	 * @param artifactsPath Path to the directory into which to place the outputs.
	 * @param stopWordPath Path to a stopwords file to be used in extracting the
	 *                     token vectors.
	 * @param numTopics Number of topics to use in the inference process.
	 * @param numIterations Number of iterations to use in the inference process.
	 */
	public MalletRunner(String sourceDir, String language,
			String artifactsPath, String stopWordPath, int numTopics,
			int numIterations) {
		this.sourceDir = new File(sourceDir
			.replaceFirst("^~",System.getProperty("user.home")));
		this.targetDir = new File(sourceDir
			.replaceFirst("^~",System.getProperty("user.home")) + "_temp");
		this.language = language.toLowerCase();
		this.artifactsPath = artifactsPath;
		(new File(this.artifactsPath)).mkdirs();
		if (this.language.equalsIgnoreCase("java"))
			this.stopWordPath =
				stopWordPath + File.separator + "javakeywordsexpanded";
		else if (this.language.equalsIgnoreCase("c"))
			this.stopWordPath = stopWordPath + File.separator + "ckeywordsexpanded";
		else if (this.language.equalsIgnoreCase("python"))
			this.stopWordPath = stopWordPath + File.separator + "pythonkeywordsexpanded";
		else
			throw new RuntimeException("Unrecognized language " + this.language);
		this.numTopics = numTopics;
		this.numIterations = numIterations;
	}
	//endregion

	//region PROCESSING
	/**
	 * Creates a temporary copy of the subject system's source directory,
	 * containing only files related to the selected source language. This avoids
	 * tainting the topicmodel with unrelated token vectors.
	 */
	public void copySource() throws IOException {
		sanityCheck();

		for (File file : FileUtil.getFileListing(this.sourceDir)) {
			if (file.isDirectory()) continue;
			Pattern p;
			switch (this.language) {
				case "java":
					if (!file.getName().endsWith(".java"))
						continue;
					copyFile(file);
					break;
				case "c":
					p = Pattern.compile(
						"\\.(c|cpp|cxx|cc|include|s|h|hh|hpp|hxx|ih|m|mm|icc|ia|tbl|p)$");
					if (p.matcher(file.getName()).find())
						copyFile(file);
					break;
				case "python":
					p = Pattern.compile("\\.(py|py3|pyc|pyo|pyw|pyx|pyd|pxd|pyi"
						+ "|pyz|pywz|rpy|pyde|pyp|pyt|xpy|ipynb)$");
					if (p.matcher(file.getName()).find())
						copyFile(file);
					break;
				default:
					throw new UnsupportedOperationException("Language " + this.language
						+ " is not supported. Supported languages are java, c and python.");
			}
		}
	}

	/**
	 * Makes sure that running this with inverted arguments won't result in
	 * deleting the subject system's source directory.
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

	/**
	 * Runs MALLET import-dir to create a vectors file, which is a serialized
	 * representation of an {@link cc.mallet.types.InstanceList}.
	 */
	public void runTopicModeling() throws IOException {
		Text2Vectors.main(new String[]{
			"--input", targetDir.getAbsolutePath(),
			"--remove-stopwords", "TRUE",
			"--keep-sequence", "TRUE",
			"--output", artifactsPath + fs + "vectors-raw",
			"--stoplist-file", stopWordPath
		});
	}

	/**
	 * Runs MALLET prune to remove tokens from the vectors file if they appear
	 * in more than 90% of the system files.
	 */
	public void runPrune() throws IOException {
		Vectors2Vectors.main(new String[]{
			"--input", artifactsPath + fs + "vectors-raw",
			"--output", artifactsPath + fs + "vectors",
			"--min-idf", "1.6"
		});
	}

	/**
	 * Runs MALLET train-topics to create a topicmodel file, which is a serialized
	 * representation of a {@link cc.mallet.topics.TopicInferencer}.
	 */
	public void runInferencer() throws IOException {
		TopicTrainer.main(new String[]{
			"--input", artifactsPath + fs + "vectors",
			"--inferencer-filename", artifactsPath + fs + "topicmodel",
			"--num-top-words", "0",
			"--num-topics", String.valueOf(this.numTopics),
			"--num-threads", "5",
			"--num-iterations", String.valueOf(this.numIterations)
		});
	}

	/**
	 * Deletes the temporary copy of the source directory.
	 */
	public void cleanUp() {
		FileUtil.deleteNonEmptyDirectory(this.targetDir);	}
	//endregion
}
