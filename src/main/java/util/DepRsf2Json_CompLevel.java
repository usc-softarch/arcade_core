//package util;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.Set;
//
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//
//import edu.usc.softarch.arcade.util.FileUtil;
//
//public class DepRsf2Json_CompLevel {
//	public static void main(String[] args) throws IOException {
//		// create json array to keep information
//		JSONArray arrays = new JSONArray();
//
//		// Hashmap to keep the import
//		Map<String, List<String>> call_deps = new HashMap<>();
//		Map<String, String> file_2_component = new HashMap<>();
//
//		Map<String, HashSet<String>> component_2_other_components = new HashMap<>();
//
//		// Hashset to keep only important entities
//		Set<String> all_entities = new HashSet<>();
//		Set<String> all_components = new HashSet<>();
//
//		String output_file		= "E:\\android\\clustered\\z_g530_5.1_acdc_clustered.json";
//
//		// read two files: architecture rsf + dependencies rsf
//		String arch_rsf_file		= "E:\\android\\clustered\\z_g530_5.1_acdc_clustered.rsf";
//		final File arch_rsf = FileUtil.checkDir(arch_rsf_file, false, false);
//		String deps_file			= "E:\\android\\deps\\z_g530_5.1_deps.rsf";
//
//		// Create important Hashset of files Hashset of Component and mapping from files to their components
//		try (BufferedReader in = new BufferedReader(new FileReader(arch_rsf))) {
//			String line;
//			while ((line = in.readLine()) != null) {
//				if (line.trim().isEmpty()) {
//					continue;
//				}
//
//				final Scanner s = new Scanner(line);
//				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
//
//				final String startNode = s.findInLine(expr);
//				final String endNode = s.findInLine(expr);
//
//				if (!endNode.startsWith("java.")){
//					all_entities.add(endNode);
//					all_components.add(startNode);
//					file_2_component.put(endNode, startNode);
//				}
//
//				if (s.findInLine(expr) != null) {
//					System.exit(1);
//				}
//				s.close();
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//			System.exit(-1);
//		}
//
//
//		//read deps files to create call dependencies maps between files
//		try (BufferedReader in = new BufferedReader(new FileReader(deps_file))) {
//			String line;
//			while ((line = in.readLine()) != null) {
//				if (line.trim().isEmpty())
//					continue;
//
//				final Scanner s = new Scanner(line);
//				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
//
//				final String startNode = s.findInLine(expr);
//				final String endNode = s.findInLine(expr);
//
//				List<String> tmp = call_deps.get(startNode);
//				if (tmp == null)
//					tmp = new ArrayList<>();
//
//				// only add if the start node is in the entities set
//				if (all_entities.contains(endNode))
//					tmp.add(endNode);
//
//				call_deps.put(startNode, tmp);
//
//				if (s.findInLine(expr) != null)
//					System.exit(1);
//				s.close();
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//			System.exit(-1);
//		}
//
//		// for each object in architecture
//		// create json object
//		try (BufferedReader in = new BufferedReader(new FileReader(arch_rsf))) {
//			String line;
//			while ((line = in.readLine()) != null) {
//				if (line.trim().isEmpty())
//					continue;
//
//				final Scanner s = new Scanner(line);
//				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
//
//				final String startNode = s.findInLine(expr);
//				final String endNode = s.findInLine(expr);
//
//				if (!endNode.startsWith("java.")){
//					List<String> imports = call_deps.get(endNode);
//					if (imports != null)
//					{
//						//update component 2 components mapping
//						HashSet<String> _2components = component_2_other_components.get(startNode);
//						if (_2components == null)
//							_2components = new HashSet<>();
//
//						for (String file: imports){
//							String componentName = file_2_component.get(file);
//							System.out.println(componentName);
//							if (componentName  != null)
//								_2components.add(componentName);
//						}
//
//						component_2_other_components.put(startNode, _2components);
//					}
//				}
//
//				if (s.findInLine(expr) != null)
//					System.exit(1);
//
//				s.close();
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//			System.exit(-1);
//		}
//
//		// write to json the mapping from component 2 components
//
//		for (String k: component_2_other_components.keySet()){
//			JSONObject details_json = new JSONObject();
//			details_json.put("name", convertName(k));
//			List<String> converted = new ArrayList<>();
//				for (String s : component_2_other_components.get(k)) {
//					converted.add(convertName(s));
//				}
//			details_json.put("imports", converted);
//			arrays.add(details_json);
//		}
//
//		JSONUtil.writeJSONArray2File(arrays, output_file);
//	}
//
//	private static String convertName(String inputComponent){
//		if (inputComponent.endsWith(".ss")) {
//			String tmp = inputComponent.replace(".ss", "");
//			String tmp_2 = tmp.replace("\\.", "_");
//			return tmp +"." +tmp_2;
//		}
//		return inputComponent;
//	}
//}