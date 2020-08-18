package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import edu.usc.softarch.extractors.cda.odem.Container;
import edu.usc.softarch.extractors.cda.odem.Namespace;
import edu.usc.softarch.extractors.cda.odem.ODEM;
import edu.usc.softarch.extractors.cda.odem.Type;

public class ODEMReader {
	private static List<Type> allTypes = new ArrayList<>();
	private static Logger logger = Logger.getLogger(ODEMReader.class);
	
	public static void setTypesFromODEMFile(String odemFile) {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(ODEM.class);
			Unmarshaller u = context.createUnmarshaller();
			ODEM odem = (ODEM) u.unmarshal(new File(odemFile));
			for (Container container : odem.getContext().getContainer()) {
				for (Namespace n : container.getNamespace()) {
					List<Type> types = n.getType();
					allTypes.addAll(types);
				}
			}
			int typeCount = 0;
			for (Type t : allTypes) {
				logger.debug(typeCount + ": " + t.getName());
				typeCount++;
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static List<Type> getAllTypes() {
		return allTypes;
	}
}