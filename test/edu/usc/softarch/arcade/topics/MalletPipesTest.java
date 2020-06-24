package edu.usc.softarch.arcade.topics;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.util.FileUtil;

public class MalletPipesTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String testFilename = "testdata/JobControl.java";

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceReplace(Pattern.compile("[^A-Za-z]")," "));
		pipeList.add(new CamelCaseSeparatorPipe());
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern
				.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"stoplists/en.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"res/javakeywords"), "UTF-8", false, false, false));
		pipeList.add(new StemmerPipe());
		pipeList.add(new TokenSequence2FeatureSequence());
		
		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		
		try {
			String testFileAsStr = FileUtil.readFile(testFilename, Charset.defaultCharset());
			Instance carrier = new Instance(testFileAsStr,"X",testFilename,testFilename);
			instances.addThruPipe(carrier);
			System.out.println(instances.get(0).getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
