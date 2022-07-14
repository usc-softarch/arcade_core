package edu.usc.softarch.arcade.facts.dependencies;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ODEMReader extends DefaultHandler {
	//region ATTRIBUTES
	private String currentClass;
	public final Collection<Map.Entry<String, String>> dependencies;
	//endregion

	//region CONSTRUCTORS
	public ODEMReader() {
		this.dependencies = new ArrayList<>(); }
	//endregion

	//region PROCESSING

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		switch (qName) {
			case "type":
				this.currentClass = attributes.getValue("name");
				break;
			case "depends-on":
				dependencies.add(new AbstractMap.SimpleEntry<>(
					this.currentClass, attributes.getValue("name")));
				break;
		}
	}
	//endregion
}
