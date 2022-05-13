package edu.usc.softarch.arcade.util.ldasupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.topics.CamelCaseSeparatorPipe;
import edu.usc.softarch.arcade.topics.StemmerPipe;
import edu.usc.softarch.arcade.util.FileUtil;

public class PipeExtractor {
	public static void main(String[] args) throws IOException {
		List<Pipe> pipeList = new ArrayList<>();
		String selectedLanguage = args[2];
		char fs = File.separatorChar;

		// Pipes: alphanumeric only, camel case separation, lowercase, tokenize,
		// remove stopwords english, remove stopwords java, stem, map to
		// features
		pipeList.add(new CharSequenceReplace(Pattern.compile("[^A-Za-z]"), " "));
		pipeList.add(new CamelCaseSeparatorPipe());
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern
				.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(
			"src" + fs + "main" + fs + "resources" + fs + "stoplists" + fs + "en.txt"), "UTF-8", false, false, false));
		
		if (selectedLanguage.equalsIgnoreCase("c")) {
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"src" + fs + "main" + fs + "resources" + fs + "res" + fs + "ckeywords"), "UTF-8", false, false, false));
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"src" + fs + "main" + fs + "resources" + fs + "res" + fs + "cppkeywords"), "UTF-8", false, false, false));
		}
		else {
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"src" + fs + "main" + fs + "resources" + fs + "res" + fs +  "javakeywords"), "UTF-8", false, false, false));
		}
		pipeList.add(new StemmerPipe());
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		
		String testDir = args[0];
		testDir = testDir.replaceFirst("^~",System.getProperty("user.home"));
		for (File file : FileUtil.getFileListing(new File(testDir))) {
			if (file.isFile() && file.getName().endsWith(".java")) {
				String shortClassName = file.getName().replace(".java", "");
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				String fullClassName = "";
				while ((line = reader.readLine()) != null) {
					String packageName = FileUtil.findPackageName(line);
					if (packageName != null) {
						fullClassName = packageName + "." + shortClassName;
					}
				}
				reader.close();
				String data = FileUtil.readFile(file.getAbsolutePath(),
						Charset.defaultCharset());
				Instance instance = new Instance(data, "X", fullClassName,
						file.getAbsolutePath());
				instances.addThruPipe(instance);
			}
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// if we found a c or c++ file
			if ( p.matcher(file.getName()).find() ) {
				String depsStyleFilename = file.getAbsolutePath().replace(testDir, "");
				String data = FileUtil.readFile(file.getAbsolutePath(),
						Charset.defaultCharset());
				Instance instance = new Instance(data, "X", depsStyleFilename,
						file.getAbsolutePath());
				instances.addThruPipe(instance);
			}
		}
		
		//save for next time
		String outputDir = args[1].replaceFirst("^~",System.getProperty("user.home"));
		instances.save(new File (outputDir, "output.pipe"));
	}
}