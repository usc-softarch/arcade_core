package ArchSmellAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.usc.softarch.arcade.util.FileListing;

public class CountInterfaceBasedSmells {
	public static void main(String[] args){
		
		String inputDirFileName = "E:\\interface_1";
		List<File> fileList;
		Integer total = 0;
		Set<String> types = new HashSet<String>();
		
		//Get the list of refactoring types
		try {
			fileList = FileListing.getFileListing(new File(inputDirFileName));
			int i = 0;
			Set<File> orderedSerFiles = new TreeSet<File>();
			
			for (File file : fileList) {
					orderedSerFiles.add(file);
			}
			
			for (File file : orderedSerFiles){
				String fileName = file.getPath();
			//	System.out.println(fileName);
				BufferedReader br = null;			 
				try {
		 
					String sCurrentLine;		 
					br = new BufferedReader(new FileReader(fileName));	
					sCurrentLine = br.readLine(); // summary
					sCurrentLine = br.readLine(); // column names
					while (!(sCurrentLine = br.readLine()).isEmpty()) {
					    String[] counter = sCurrentLine.split(",");
					    //System.out.println(sCurrentLine);
					    for (int j = 1; j < 7; j++){
					    	total += Integer.valueOf(counter[j]);
					    }
					    System.out.println(total);
					}		 
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.print("source,target");
		System.out.print("source_major,source_minor,source_path,target_major,target_minor,target_path");
		for (String type: types){
			System.out.print(","+type);
		}
		System.out.println();
		
		//print number of refactoring
		try {
			fileList = FileListing.getFileListing(new File(inputDirFileName));
			int i = 0;
			Set<File> orderedSerFiles = new TreeSet<File>();
			
			for (File file : fileList) {
					orderedSerFiles.add(file);
			}
			
			for (File file : orderedSerFiles){
				String fileName = file.getPath();
			//	System.out.println(fileName);
				BufferedReader br = null;		
				Map<String, Integer> counter = new HashMap<String, Integer>();
				for (String type: types){
					counter.put(type, 0);
				}
				try {
		 
					String sCurrentLine;		 
					br = new BufferedReader(new FileReader(fileName));		 
					while ((sCurrentLine = br.readLine()) != null) {
						String ref = sCurrentLine.split("\\(")[0];
						int temp = counter.get(ref);
						temp +=1;
						counter.put(ref, temp);
					}
				// print out the result
					String[] temp = fileName.split("[\\\\|\\\\.j-]");
				//  System.out.print(temp[3]+","+temp[4]);
					System.out.print(temp[4]+","+temp[5]+","+temp[6]+","+temp[8]+","+temp[9]+","+temp[10]);
					for (String type: types){
						int tmpInt = counter.get(type);
						System.out.print(","+tmpInt);
					}
					System.out.println();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
}
