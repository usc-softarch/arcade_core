package edu.usc.softarch.arcade.classgraphs;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.PackManager;
import soot.Transform;

/**
 * @author joshua
 */
public class ModifiedCallGraphExample {
	static Set<String> traversedMethodSet = new HashSet<>();
	static final boolean DEBUG = false;
	static ClassGraph clg = new ClassGraph();

	public static void main(String[] args) {
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		argsList.addAll(Arrays.asList(new String[] { "-p", "cg",
				"verbose:true", "-w", "-main-class",
				"MTSGenerator.Generator_app",// main-class
				"MTSGenerator.Generator_app",// argument classes
				"DataTypes.Event",
		}));

		String stdOutFilename = "output.txt";
		String stdErrFilename = "error.txt";

		redirectSystemOut(stdOutFilename);
		redirectSystemErr(stdErrFilename);

		PackManager.v().getPack("wjtp").add(
				new Transform("wjtp.myTrans", new ClassGraphTransformer()));

		args = argsList.toArray(new String[0]);

		soot.Main.main(args);
	}

	private static void redirectSystemErr(String stdErrFilename) {
		try {
			System.setErr(new PrintStream(new FileOutputStream(stdErrFilename)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	private static void redirectSystemOut(String stdOutFilename) {
		try {
			System.setOut(new PrintStream(new FileOutputStream(stdOutFilename)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
