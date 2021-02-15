package edu.usc.softarch.arcade.antipattern.detection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.antipattern.BcoSmell;
import edu.usc.softarch.arcade.antipattern.BdcSmell;
import edu.usc.softarch.arcade.antipattern.BuoSmell;
import edu.usc.softarch.arcade.antipattern.SpfSmell;
import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileUtil;

public class SmellUtil {
	public static String getSmellAbbreviation(Smell smell) {
		if(smell.getSmellType() != null)
			return smell.getSmellType().toString();

		//TODO remove everything below this
		if (smell instanceof BcoSmell) {
			return "bco";
		}
		else if (smell instanceof SpfSmell) {
			return "spf";
		}
		else if (smell instanceof BdcSmell) {
			return "bdc";
		}
		else if (smell instanceof BuoSmell) {
			return "buo";
		}
		else {
			return "invalid smell type";
		}
	}
	
	public static Class[] getSmellClasses() {
		Class[] smellClasses = {BcoSmell.class,BdcSmell.class,BuoSmell.class,SpfSmell.class};
		return smellClasses;
	}
	
	public static Set<ConcernCluster> getSmellClusters(final Smell smell){
		return smell.clusters;
	}
	
	/**
	 * Reads a set of smells from a .ser file.
	 */
	public static Set<Smell> deserializeDetectedSmells(
			String detectedSmellsGtFilename) throws IOException {
		XStream xstream = new XStream();
		String xml = null;
		xml = FileUtil.readFile(detectedSmellsGtFilename, StandardCharsets.UTF_8);
		Object detectedGtSmells = xstream.fromXML(xml);
		if (!(detectedGtSmells instanceof Set<?>))
			throw new IllegalArgumentException(
				"Error parsing XML file: not a valid input");
		return (Set<Smell>) detectedGtSmells;
	}
}