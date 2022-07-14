package edu.usc.softarch.arcade.facts.dependencies;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

public class OdemToRsf {
	//region PUBLIC INTERFACE
	public static void main(String[] args)
			throws ParserConfigurationException, SAXException, IOException {
		File inputFile = new File(args[0]);
		String outputPath = args[1];

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		String screwYourDtd =
			"http://apache.org/xml/features/nonvalidating/load-external-dtd";
		factory.setFeature(screwYourDtd, false);
		SAXParser parser = factory.newSAXParser();
		ODEMReader handler = new ODEMReader();
		parser.parse(inputFile, handler);
		Collection<Map.Entry<String, String>> result = handler.dependencies;

		try (PrintWriter writer = new PrintWriter(new PrintStream(outputPath))) {
			for (Map.Entry<String, String> edge : result)
				writer.println("depends " + edge.getKey() + " " + edge.getValue());
		}
	}
	//endregion
}
