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
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
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
		try (PrintWriter writer = new PrintWriter(outputPath)) {
			Integer total = 0;
			writer.println("version, bdc, buo, bco, spf, all");
			for (File file : inputFiles) {
				SmellCollection smells = new SmellCollection(file.getAbsolutePath());

				int buoCount = 0;
				int bdcCount = 0;
				int spfCount = 0;
				int bcoCount = 0;
				for (Smell smell : smells) {
					switch (smell.getSmellType()) {
						case buo:	buoCount++;	break;
						case bdc: bdcCount++; break;
						case spf: spfCount++; break;
						case bco: bcoCount++; break;
					}
					System.out.println(smell.getSmellType() + " "	+ smell);
				}
				int sumCount = buoCount + bdcCount + spfCount + bcoCount;
				total += sumCount;
				writer.println(FileUtil.extractVersionPretty(file.getName()) + ","
					+ bdcCount + "," + buoCount + "," + bcoCount + ","
					+ spfCount + "," + sumCount);
			}
			writer.println("total:" + total);
		}
	}
	
	static Set<File> getOrderedFiles(String inputDirectory) {
		Set<File> orderedSerFiles = new LinkedHashSet<>();
		List<File> fileList = null;
		try {
			fileList = FileListing.getFileListing(new File(inputDirectory));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (File file : fileList)
			if (file.getName().endsWith(".ser"))
				orderedSerFiles.add(file);
		return orderedSerFiles;
	}
	
	/**
	 * @param inputFiles
	 * @param outputPath: full path without .xls
	 */
	static void analyzeClassSmellnum(Set<File> inputFiles, String outputPath)
			throws IOException {
		try(HSSFWorkbook workbook = new HSSFWorkbook()) {
			for (File file : inputFiles) {
				SmellCollection smells = new SmellCollection(file.getAbsolutePath());
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
				
				Map<String, SmellCount> entityMap = new HashMap<>();

				for (Smell smell : smells) {
					Set<ConcernCluster > clusters = smell.getClusters();
					for(ConcernCluster cluster : clusters) {
						Set<String> entities = cluster.getEntities();
						for(String entity : entities) {
							if(entityMap.containsKey(entity)) {
								switch (smell.getSmellType()) {
									case buo:	entityMap.get(entity).buo++; break;
									case bdc: entityMap.get(entity).bdc++; break;
									case spf: entityMap.get(entity).spf++; break;
									case bco: entityMap.get(entity).bco++; break;
								}
							} else {
								SmellCount smellCount = new SmellCount();
								switch (smell.getSmellType()) {
									case buo:	smellCount.buo++;	break;
									case bdc: smellCount.bdc++; break;
									case spf: smellCount.spf++; break;
									case bco: smellCount.bco++; break;
								}
								entityMap.put(entity, smellCount);
							}
							entityMap.get(entity).all++;
						}
					}
				}
				
				Iterator<Entry<String, SmellCount>> it = entityMap.entrySet().iterator();
				int rownum = 1;
				while (it.hasNext()) {
					Entry<String, SmellCount> pair = it.next();
					SmellCount smellCount = pair.getValue();
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue(pair.getKey());
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
			try (FileOutputStream os = new FileOutputStream(outputPath+".xls")) {
				workbook.write(os);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param inputFiles
	 * @param outputPath: full path WITHOUT .xls
	 */
	static void analyzeCompSmellnum(Set<File> inputFiles, String outputPath)
			throws IOException {
		try (HSSFWorkbook workbook = new HSSFWorkbook()) {
			for (File file : inputFiles) {
				SmellCollection smells = new SmellCollection(file.getAbsolutePath());
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
					Set<ConcernCluster> clusters = smell.getClusters();
					for (ConcernCluster cluster: clusters) {
						if (clusterMap.containsKey(cluster.getName())) {
							switch (smell.getSmellType()) {
								case buo:	clusterMap.get(cluster.getName()).buo++;	break;
								case bdc: clusterMap.get(cluster.getName()).bdc++; break;
								case spf: clusterMap.get(cluster.getName()).spf++; break;
								case bco: clusterMap.get(cluster.getName()).bco++; break;
							}
						} else {
							SmellCount smellCount = new SmellCount();
							switch (smell.getSmellType()) {
								case buo:	smellCount.buo++;	break;
								case bdc: smellCount.bdc++; break;
								case spf: smellCount.spf++; break;
								case bco: smellCount.bco++; break;
							}
							clusterMap.put(cluster.getName(), smellCount);
						}
						clusterMap.get(cluster.getName()).all++;
					}
				}

				Iterator<Entry<String, SmellCount>> it = clusterMap.entrySet().iterator();
				int rownum = 1;
				while (it.hasNext()) {
					Entry<String, SmellCount> pair = it.next();
					SmellCount smellCount = pair.getValue();
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue(pair.getKey());
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
			try (FileOutputStream os = new FileOutputStream(outputPath+".xls")) {
				workbook.write(os);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}