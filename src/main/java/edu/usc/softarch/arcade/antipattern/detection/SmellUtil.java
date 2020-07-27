package edu.usc.softarch.arcade.antipattern.detection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileUtil;

public class SmellUtil {
	public static String getSmellAbbreviation(Smell smell) {
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
	
	public static Set<Smell> deserializeDetectedSmells(
			String detectedSmellsGtFilename) {
		XStream xstream = new XStream();
		String xml = null;
		try {
			xml = FileUtil.readFile(detectedSmellsGtFilename,StandardCharsets.UTF_8);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Set<Smell> detectedGtSmells = (Set<Smell>)xstream.fromXML(xml);
		return detectedGtSmells;
	}
}
