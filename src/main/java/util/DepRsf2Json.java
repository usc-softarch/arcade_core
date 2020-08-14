package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.usc.softarch.arcade.util.FileUtil;

public class DepRsf2Json {
	public static void main(String[] args) throws IOException {
		
		// create json array to keep information
		JSONArray arrays = new JSONArray();
		
		// Hashmap to keep the import
		Map<String, List<String>> call_deps = new HashMap<>();
		
		// Hashset to keep only important entities
		Set<String> all_entities = new HashSet<>();
		
		String output_file = "E:\\android\\clustered\\edeg7_7.0.1_acdc_clustered.json";
		
		// read two files: architecture rsf + dependencies rsf
		String arch_rsf_file = "E:\\android\\clustered\\edeg7_7.0.1_acdc_clustered.rsf";
		final File arch_rsf = FileUtil.checkDir(arch_rsf_file, false, false);
		String deps_file = "E:\\android\\deps\\edeg7-7.0.1_deps.rsf";

		// Create important Hashset
		try (BufferedReader in = new BufferedReader(new FileReader(arch_rsf))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				final String endNode = s.findInLine(expr);
				
				if (!endNode.startsWith("java."))
					all_entities.add(endNode);
				
				if (s.findInLine(expr) != null) {
					System.exit(1);
				}
				s.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		//read deps files to create call dependencies maps		
		try (BufferedReader in = new BufferedReader(new FileReader(deps_file))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				
				List<String> tmp = call_deps.get(startNode);
				if (tmp == null)
					tmp = new ArrayList<>();

				// only add if the start node is in the entities set
				if (all_entities.contains(endNode))
					tmp.add(endNode);

				call_deps.put(startNode, tmp);
				
				if (s.findInLine(expr) != null) {
					System.exit(1);
				}
				s.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// for each object in architecture
		// create json object
		try (BufferedReader in = new BufferedReader(new FileReader(arch_rsf))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				
				JSONObject details_json = new JSONObject();

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				
				if (!endNode.startsWith("java.")){
					List<String> imports = call_deps.get(endNode);
					if (imports == null)
						imports = new ArrayList<>();
					
					details_json.put("name", endNode);
					details_json.put("component", startNode);
					details_json.put("imports", imports);
					arrays.add(details_json);
				}

				if (s.findInLine(expr) != null) {
					System.exit(1);
				}

				s.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		JSONUtil.writeJSONArray2File(arrays, output_file);
	}
}
