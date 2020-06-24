package IssueMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.StringUtil;

public class MergeInterfaceBasedSmell2Issues {
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
	
		String mainFolder = "E:\\lucene_data\\";
		String issue_json = mainFolder + "lucene_acdc_dep_con.json";
		String smell_json = mainFolder + "lucene_acdc_interface_smell.csv.json";
		String merged_output_file = mainFolder + "lucene_acdc_all.json";
		
//		String mainFolder = "F:\\camel_data\\";
//		String issue_json = mainFolder + "camel_arc_dep_con.json";
//		String smell_json = mainFolder + "camel_arc_interface_smell.csv.json";
//		String merged_output_file = mainFolder + "camel_arc_all.json";
//		
//		String mainFolder = "F:\\cxf_data\\";
//		String issue_json = mainFolder + "cxf_pkg_dep_con.json";
//		String smell_json = mainFolder + "cxf_pkg_interface_smell.csv.json";
//		String merged_output_file = mainFolder + "cxf_pkg_all.json";
//				
		
//		String mainFolder = "E:\\nutch_data\\";
//		String issue_json = mainFolder + "nutch_acdc_dep_con.json";
//		String smell_json = mainFolder + "nutch_acdc_interface_smell.csv.json";
//		String merged_output_file = mainFolder + "nutch_acdc_all.json";
		
//		String mainFolder = "E:\\openjpa_data\\";
//		String issue_json = mainFolder + "openjpa_pkg_dep_con.json";
//		String smell_json = mainFolder + "openjpa_pkg_interface_smell.csv.json";
//		String merged_output_file = mainFolder + "openjpa_pkg_all.json";
						
//		String issue_json = mainFolder + "continuum_data\\continuum_pkg_dep_issue.json";
//		String smell_json = mainFolder + "continuum_data\\continuum_pkg_interface_smell.csv.json";
//		String merged_output_file = mainFolder + "continuum_data\\continuum_pkg_all.json";
		
//		String issue_json = mainFolder + "wicket_acdc_dep_con.json";
//		String smell_json = mainFolder + "wicket_acdc_interface_smell.csv.json";
//		String merged_output_file = mainFolder + "wicket_acdc_all.json";
				
//		String issue_json = "F:\\ASE_2016_data\\Struts2\\dep_con_smells\\struts2_acdc.json";
//		String smell_json = "F:\\ASE_2016_data\\Struts2\\struts2_acdc_interface_smell.csv.json";
//		String merged_output_file = "F:\\ASE_2016_data\\Struts2\\Struts2_acdc_all.json";
		
		
		JSONParser parser = new JSONParser();
		JSONArray issues = (JSONArray) parser.parse(new FileReader(issue_json));
		JSONObject smell = (JSONObject) parser.parse(new FileReader(smell_json));
		
		for (int i = 0; i < issues.size(); i ++){
			JSONObject issue = (JSONObject) issues.get(i);
			String afffect_version = (String) issue.get("affect");
			
			JSONArray commits = (JSONArray) issue.get("commits");
			for (int j = 0; j < commits.size(); j++){
				JSONObject commit = (JSONObject) commits.get(j);
				JSONArray files = (JSONArray) commit.get("files");
				for (int k = 0; k < files.size(); k ++){
					JSONObject file = (JSONObject) files.get(k);
					String filename = (String) file.get("filename");
					if (filename.contains(".java") && filename.contains("org"))
					{
//						System.out.println(filename);
						String orgFormat = StringUtil.dir2pkg(filename);
						if (orgFormat != null) {
							// System.out.println(orgFormat);
							// try to map smell
							JSONObject smell_of_a_version = (JSONObject) smell.get(afffect_version);
							if (smell_of_a_version != null){
								JSONObject smell_of_a_file = (JSONObject) smell_of_a_version.get(orgFormat);
								if (smell_of_a_file != null)
								{
									Set<String> keySet = smell_of_a_file.keySet();
									JSONObject smells = (JSONObject) file.get("smells");
									if (smells == null)
										smells = new JSONObject();
									for (String key : keySet){
										smells.put(key, smell_of_a_file.get(key));
									}
									file.put("smells", smells);
									System.out.println(orgFormat);
								}
							}
						}
					}
				}
			}
		}
		
		
		File json_file = new File(merged_output_file);
		// if file doesnt exists, then create it
		if (!json_file.exists()) {
			json_file.createNewFile();
		}
		FileWriter fw = new FileWriter(json_file.getAbsolutePath());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(issues.toJSONString());
		bw.close();

		System.out.println("Done");
		
	}
}
