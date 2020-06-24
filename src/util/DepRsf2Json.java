package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.usc.softarch.arcade.util.FileUtil;

public class DepRsf2Json {
	// static Logger logger = org.apache.logging.log4j.LogManager.getLogger(DepRsf2Json.class);
	// Define XML TAGs
	private static String NAME 				= "name";
	private static String COMPONENT 		= "component";
	private static String IMPORTS 			= "imports";
	
	public static String PKGNAME;
	
	public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
		
		// create json array to keep information
		JSONArray arrays = new JSONArray();
		
		// Hashmap to keep the import
		HashMap<String, List<String>> call_deps = new HashMap<>();
		
		// Hashset to keep only important entities
		HashSet<String> all_entities = new HashSet();
		
		String output_file		= "E:\\android\\clustered\\edeg7_7.0.1_acdc_clustered.json"; //args[0]; //
		
		// read two files: architecture rsf + dependencies rsf
		String arch_rsf_file		= "E:\\android\\clustered\\edeg7_7.0.1_acdc_clustered.rsf"; //args[0]; //
		final File arch_rsf = FileUtil.checkDir(arch_rsf_file, false, false);
		String deps_file			= "E:\\android\\deps\\edeg7-7.0.1_deps.rsf"; //args[1]; //
		final File deps = FileUtil.checkDir(deps_file, false, false);

		// Create important Hashset
		try {
			final BufferedReader in = new BufferedReader(new FileReader(arch_rsf));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				
				JSONObject details_json = new JSONObject();

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
				// int tokenLimit = 3;

				final String arcType = s.findInLine(expr);
				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				
				if (!checkIgnore(endNode))
					all_entities.add(endNode);
				
				if (s.findInLine(expr) != null) {
					// logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}
				s.close();
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		//read deps files to create call dependencies maps		
		try {
			final BufferedReader in = new BufferedReader(new FileReader(deps_file));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
				// int tokenLimit = 3;

				final String arcType = s.findInLine(expr);
				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				
				List<String> tmp = call_deps.get(startNode);
				if (tmp == null)
					tmp = new ArrayList<String>();

				// only add if the start node is in the entities set
				if (all_entities.contains(endNode))
					tmp.add(endNode);

				call_deps.put(startNode, tmp);
				
				if (s.findInLine(expr) != null) {
					// logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}
				s.close();
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// for each object in architecture
		// create json object
		try {
			final BufferedReader in = new BufferedReader(new FileReader(arch_rsf));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				
				JSONObject details_json = new JSONObject();

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
				// int tokenLimit = 3;

				final String arcType = s.findInLine(expr);
				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
//				final List<String> fact = Lists.newArrayList(arcType, startNode, endNode);
				
				if (!checkIgnore(endNode)){
					List<String> imports = call_deps.get(endNode);
					if (imports == null)
						imports = new ArrayList<>();
					
					details_json.put(NAME, endNode);
					details_json.put(COMPONENT, startNode);
					details_json.put(IMPORTS, imports);
					arrays.add(details_json);
				}
//	
				if (s.findInLine(expr) != null) {
					// logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}

				s.close();
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		JSONUtil.writeJSONArray2File(arrays, output_file);

	}
	
	static private boolean checkIgnore(String inputName){
		if (inputName.startsWith("java."))
			return true;
		return false;
	}
	
}
