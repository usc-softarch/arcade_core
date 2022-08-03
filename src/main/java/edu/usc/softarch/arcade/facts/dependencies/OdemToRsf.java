package edu.usc.softarch.arcade.facts.dependencies;

import edu.usc.softarch.arcade.facts.DependencyGraph;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class OdemToRsf {
	//region PUBLIC INTERFACE
	public static void main(String[] args)
			throws ParserConfigurationException, SAXException, IOException {
		String inputPath = args[0];
		String outputPath = args[1];

		DependencyGraph result = DependencyGraph.readOdem(inputPath);
		result.writeToRsf(outputPath);
	}
	//endregion
}
