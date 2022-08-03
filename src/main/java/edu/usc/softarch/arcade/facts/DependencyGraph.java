package edu.usc.softarch.arcade.facts;

import edu.usc.softarch.arcade.facts.dependencies.ODEMReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class DependencyGraph extends LinkedHashSet<Map.Entry<String, String>> {
	//region CONSTRUCTORS
	public DependencyGraph() { super(); }
	//endregion

	//region SERIALIZATION
	public static DependencyGraph readOdem(String path)
			throws ParserConfigurationException, SAXException, IOException {
		DependencyGraph result = new DependencyGraph();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		String screwYourDtd =
			"http://apache.org/xml/features/nonvalidating/load-external-dtd";
		factory.setFeature(screwYourDtd, false);
		SAXParser parser = factory.newSAXParser();

		ODEMReader handler = new ODEMReader();
		parser.parse(path, handler);
		result.addAll(handler.dependencies);

		return result;
	}

	public static DependencyGraph readRsf(String path) throws IOException {
		DependencyGraph result = new DependencyGraph();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;

				String[] entry = line.split(" ");

				result.add(new AbstractMap.SimpleEntry<>(entry[1], entry[2]));
			}
		}

		return result;
	}
	//endregion

	//region ACCESSORS
	public boolean add(String from, String to) {
		return this.add(new AbstractMap.SimpleEntry<>(from, to)); }
	//endregion

	//region SERIALIZATION
	public void writeToRsf(String rsfPath) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(new PrintStream(rsfPath))) {
			for (Map.Entry<String, String> edge : this)
				writer.println("depends " + edge.getKey() + " " + edge.getValue());
		}
	}
	//endregion
}
