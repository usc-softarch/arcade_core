package edu.usc.softarch.arcade.weka;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.datatypes.Proj;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * @author joshua
 *
 */
public class AddTargetClassToArff {
	
	public static void main(String[] args) {
		String filename = "";
			
		try {
			if (Config.proj.equals(Proj.LlamaChat)) { 
				filename = "/Users/joshuaga/Documents/workspace/MyExtractors/datasets/LlamaChat/" + Config.llamaChatStr + "_withFieldAccessInfo.arff";
				addTargetAttributes(filename,(new ClassValueMap()).LlamaChatMap);
			}
			else if (Config.proj.equals(Proj.FreeCS)) {
				filename = "/Users/joshuaga/Documents/workspace/MyExtractors/datasets/freecs/" + Config.freecsStr + "_withFieldAccessInfo.arff";
				addTargetAttributes(filename,(new ClassValueMap()).freecsMap);
			}
			else {
				System.out.println("Cannot identify current project..exiting");
				System.exit(1);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void addTargetAttributes(String filename, HashMap<String,String> map) throws IOException {
		FileReader fr = new FileReader(filename);
		Instances instances = new Instances(fr);
		FastVector attVals = new FastVector();
		attVals.addElement("p");
		attVals.addElement("d");
		attVals.addElement("c");
		instances.insertAttributeAt(new Attribute("class", attVals), instances.numAttributes());
		
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry pairs = (Map.Entry) iter.next();
			Instance instance = findMatchingInstance(instances,(String)pairs.getKey());
			if (instance == null) {
				System.out.println("Cannot find instance: " + pairs.getKey());
				System.exit(1);
			}
			instance.setValue(instances.attribute("class"), (String)pairs.getValue());
		}
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		try {
			String[] splitFileName = filename.split("\\.");
			String fullFileName = splitFileName[0]  + "_withTargetClasses.arff";
			saver.setFile(new File(fullFileName));
			saver.setDestination(new File(fullFileName)); // **not**
																				// necessary
			// in 3.5.4 and
			// later
			saver.writeBatch();
			System.out.println("Wrote file: " + fullFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Instance findMatchingInstance(Instances data,
			String className) {
		Enumeration instances = data.enumerateInstances();
		boolean DEBUG_findMatchningInstance = false;
		Attribute name = data.attribute("name");
		while (instances.hasMoreElements()) {
			Instance instance = (Instance)instances.nextElement();
			String instanceName = instance.stringValue(name);
			if (DEBUG_findMatchningInstance)
				System.out.println("Comparing " + "'" + instanceName + "' to '" + className + "'");
			if (instanceName.equals(className)) {
				return instance;
			}
		}
		return null;
	}
}
