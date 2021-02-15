package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class SmellAnalyzer {

	public static void main(String[] args) throws IOException {
		String inputDirectory = "E:\\android\\ser";
		String outputDirectory = "E:\\android\\excel\\acdc_comp";
		
		Set<File> orderedSerFiles = getOrderedFiles(inputDirectory+"/");
		analyzeCompSmellnum(orderedSerFiles, outputDirectory);
	}

	/**
	 * for the different versions of one project, 
	 * count the number of smells for each version
	 * @param inputFiles
	 * @param outputPath: full path including file format
	 */
	static void analyzeVersionSmellnum(Set<File> inputFiles, String outputPath)
			throws IOException {
		PrintWriter writer = null;
		Integer total = 0;
		try {
			writer = new PrintWriter(outputPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		writer.println("version, bdc, buo, bco, spf, all");
		for (File file : inputFiles) {
			Set<Smell> smells = SmellUtil.deserializeDetectedSmells(file
					.getAbsolutePath());

			int buoCount = 0;
			int bdcCount = 0;
			int spfCount = 0;
			int bcoCount = 0;
			for (Smell smell : smells) {
				if (SmellUtil.getSmellAbbreviation(smell).equals("buo")) {
					buoCount++;
				} else if (SmellUtil.getSmellAbbreviation(smell).equals("bdc")) {
					bdcCount++;
				} else if (SmellUtil.getSmellAbbreviation(smell).equals("spf")) {
					spfCount++;
				} else if (SmellUtil.getSmellAbbreviation(smell).equals("bco")) {
					bcoCount++;
				}
				System.out.println(SmellUtil.getSmellAbbreviation(smell) + " "
						+ smell);
			}
			int sumCount = buoCount + bdcCount + spfCount + bcoCount;
			total += sumCount;
			writer.println(FileUtil.extractVersionPretty(file.getName()) + ","
					+ bdcCount + "," + buoCount + "," + bcoCount + ","
					+ spfCount + "," + sumCount);
		}
		writer.println("total:" + total);
		writer.close();
	}
	
	static Set<File> getOrderedFiles(String inputDirectory) {
		Set<File> orderedSerFiles = new LinkedHashSet<File>();
		List<File> fileList = null;
		try {
			fileList = FileListing.getFileListing(new File(inputDirectory));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (File file : fileList) {
			if (file.getName().endsWith(".ser")) {
				orderedSerFiles.add(file);
			}
		}
		return orderedSerFiles;
	}
	
	/**
	 * @param inputFiles
	 * @param outputPath: full path without .xls
	 */
	static void analyzeClassSmellnum(Set<File> inputFiles, String outputPath)
			throws IOException {
		 HSSFWorkbook workbook = new HSSFWorkbook();  
		 
		for (File file : inputFiles) {
			
			Set<Smell> smells = SmellUtil.deserializeDetectedSmells(file
					.getAbsolutePath());
			HSSFSheet sheet = workbook.createSheet(FileUtil.extractFilenamePrefix(file));  
			sheet.setColumnWidth(0, 15000);
			//set headers
			HSSFRow row = sheet.createRow(0);  
			HSSFCell cell = row.createCell(0);  
			cell.setCellValue("class");
			cell = row.createCell(1);
			cell.setCellValue("buo");
			cell = row.createCell(2);
			cell.setCellValue("bdc");
			cell = row.createCell(3);
			cell.setCellValue("spf");
			cell = row.createCell(4);
			cell.setCellValue("bco");
			cell = row.createCell(5);
			cell.setCellValue("all");
			
			Map<String, SmellCount> entityMap = new HashMap<String, SmellCount>();

			for (Smell smell : smells) {
				Set<ConcernCluster> clusters = SmellUtil.getSmellClusters(smell);
				for(ConcernCluster cluster: clusters){
					Set<String> entities = cluster.getEntities();
					for(String entity: entities){
						if(entityMap.containsKey(entity)){
							if (SmellUtil.getSmellAbbreviation(smell).equals("buo")) {
								entityMap.get(entity).buo++;
							} else if (SmellUtil.getSmellAbbreviation(smell).equals("bdc")) {
								entityMap.get(entity).bdc++;
							} else if (SmellUtil.getSmellAbbreviation(smell).equals("spf")) {
								entityMap.get(entity).spf++;
							} else if (SmellUtil.getSmellAbbreviation(smell).equals("bco")) {
								entityMap.get(entity).bco++;
							}
						}else{
							SmellCount smellCount = new SmellCount();
							if (SmellUtil.getSmellAbbreviation(smell).equals("buo")) {
								smellCount.buo++;
							} else if (SmellUtil.getSmellAbbreviation(smell).equals("bdc")) {
								smellCount.bdc++;
							} else if (SmellUtil.getSmellAbbreviation(smell).equals("spf")) {
								smellCount.spf++;
							} else if (SmellUtil.getSmellAbbreviation(smell).equals("bco")) {
								smellCount.bco++;
							}
							entityMap.put(entity, smellCount);
						}
						entityMap.get(entity).all++;
					}
				}
			}
			
			Iterator it = entityMap.entrySet().iterator();
			int rownum = 1;
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        SmellCount smellCount = (SmellCount) pair.getValue();
		        row = sheet.createRow(rownum++);
		        cell = row.createCell(0);
		        cell.setCellValue(pair.getKey().toString());
		        cell = row.createCell(1);
		        cell.setCellValue(smellCount.buo);
		        cell = row.createCell(2);
		        cell.setCellValue(smellCount.bdc);
		        cell = row.createCell(3);
		        cell.setCellValue(smellCount.spf);
		        cell = row.createCell(4);
		        cell.setCellValue(smellCount.bco);
		        cell = row.createCell(5);
		        cell.setCellValue(smellCount.all);
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		}
		 FileOutputStream os;
		try {
			os = new FileOutputStream(outputPath+".xls");
			workbook.write(os);  
		    os.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}
	/**
	 * @param inputFiles
	 * @param outputPath: full path WITHOUT .xls
	 */
	static void analyzeCompSmellnum(Set<File> inputFiles, String outputPath)
			throws IOException {
		 HSSFWorkbook workbook = new HSSFWorkbook();  
		 
		for (File file : inputFiles) {
			
			Set<Smell> smells = SmellUtil.deserializeDetectedSmells(file
					.getAbsolutePath());
			HSSFSheet sheet = workbook.createSheet(FileUtil.extractFilenamePrefix(file));  
			sheet.setColumnWidth(0, 15000);
			//set headers
			HSSFRow row = sheet.createRow(0);  
			HSSFCell cell = row.createCell(0);  
			cell.setCellValue("components");
			cell = row.createCell(1);
			cell.setCellValue("buo");
			cell = row.createCell(2);
			cell.setCellValue("bdc");
			cell = row.createCell(3);
			cell.setCellValue("spf");
			cell = row.createCell(4);
			cell.setCellValue("bco");
			cell = row.createCell(5);
			cell.setCellValue("all");
			
			Map<String, SmellCount> clusterMap = new HashMap<>();

			for (Smell smell : smells) {
				Set<ConcernCluster> clusters = SmellUtil.getSmellClusters(smell);
				for(ConcernCluster cluster: clusters){
					if(clusterMap.containsKey(cluster.getName())){
						if (SmellUtil.getSmellAbbreviation(smell).equals("buo")) {
							clusterMap.get(cluster.getName()).buo++;
						} else if (SmellUtil.getSmellAbbreviation(smell).equals("bdc")) {
							clusterMap.get(cluster.getName()).bdc++;
						} else if (SmellUtil.getSmellAbbreviation(smell).equals("spf")) {
							clusterMap.get(cluster.getName()).spf++;
						} else if (SmellUtil.getSmellAbbreviation(smell).equals("bco")) {
							clusterMap.get(cluster.getName()).bco++;
						}
					}else{
						SmellCount smellCount = new SmellCount();
						if (SmellUtil.getSmellAbbreviation(smell).equals("buo")) {
							smellCount.buo++;
						} else if (SmellUtil.getSmellAbbreviation(smell).equals("bdc")) {
							smellCount.bdc++;
						} else if (SmellUtil.getSmellAbbreviation(smell).equals("spf")) {
							smellCount.spf++;
						} else if (SmellUtil.getSmellAbbreviation(smell).equals("bco")) {
							smellCount.bco++;
						}
						clusterMap.put(cluster.getName(), smellCount);
					}
					clusterMap.get(cluster.getName()).all++;
					}
				}

			Iterator it = clusterMap.entrySet().iterator();
			int rownum = 1;
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        SmellCount smellCount = (SmellCount) pair.getValue();
		        row = sheet.createRow(rownum++);
		        cell = row.createCell(0);
		        cell.setCellValue(pair.getKey().toString());
		        cell = row.createCell(1);
		        cell.setCellValue(smellCount.buo);
		        cell = row.createCell(2);
		        cell.setCellValue(smellCount.bdc);
		        cell = row.createCell(3);
		        cell.setCellValue(smellCount.spf);
		        cell = row.createCell(4);
		        cell.setCellValue(smellCount.bco);
		        cell = row.createCell(5);
		        cell.setCellValue(smellCount.all);
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		}
		 FileOutputStream os;
		try {
			os = new FileOutputStream(outputPath+".xls");
			workbook.write(os);  
		    os.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}
}
