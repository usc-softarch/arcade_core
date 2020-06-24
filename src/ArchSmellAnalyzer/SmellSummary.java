package ArchSmellAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.usc.softarch.arcade.util.FileListing;

public class SmellSummary {

	static String summaryDirectory = "E:\\output\\";
	//static String inputDirectory = "F:\\Google Drive\\ARC_Results\\jackrabbit_ARC_java_2015-08-12_07_37_56_978\\ser";
	static String inputDirectory = "E:\\cxf_data\\arc\\ser"; 
	public static void main(String[] args) {

		List<File> summaryFiles = getFiles(summaryDirectory);
		List<File> inputFiles = getFiles(inputDirectory);
		String csvSplit = ",";
		String filenameSplit = "_";
		String line = "";
		String[] inputNameArray = null;
		BufferedReader br = null;
		String view = null; // with '.csv'
		String system = null;
		for (File input : inputFiles) {
			inputNameArray = input.getName().split(filenameSplit);
			system = inputNameArray[0];
			view = inputNameArray[1];
			System.out.println("processing: " + system + view);
			// Read Excel document first
			FileInputStream excelInput = null;
			try {
				excelInput = new FileInputStream(new File(summaryDirectory
						+ system + "_Smell_Summary.xlsx"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// convert it into a POI object
			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(excelInput);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Read excel sheet that needs to be updated
			XSSFSheet worksheet = null;
			if (view.contains("arc")) {
				worksheet = workbook.getSheetAt(0);
			} else if (view.contains("acdc")) {
				worksheet = workbook.getSheetAt(1);
			} else if (view.contains("pkg")) {
				worksheet = workbook.getSheetAt(2);
			} else {
				System.out.println("view is undefined: " + view);
				break;
			}
			// declare a Cell object
			Cell cell = null;

			try {
				br = new BufferedReader(new FileReader(input));
				int excelRowNum = 3;
				line = br.readLine(); // ignore the first line (header)
				while ((line = br.readLine()) != null) {
					// use comma as separator
					System.out.println(line);
					String[] csvRow = line.split(csvSplit);
					for (int csvColumn = 0; csvColumn < 5; csvColumn++) {
						if (worksheet.getRow(excelRowNum) == null){
							worksheet.createRow(excelRowNum);
						}
						if(worksheet.getRow(excelRowNum).getCell(2+csvColumn)==null){
							cell = worksheet.getRow(excelRowNum).createCell(2+csvColumn);
						}else{
							cell = worksheet.getRow(excelRowNum).getCell(
									(2 + csvColumn));
						}
						System.out.println("cell["+excelRowNum+"]["+(2+csvColumn)+"]="+csvRow[csvColumn]);
						cell.setCellValue(csvRow[csvColumn]);
					}
					excelRowNum++;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			// important to close InputStream
			try {
				excelInput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Open FileOutputStream to write updates
			FileOutputStream excelOutput = null;
			try {
				excelOutput = new FileOutputStream(new File(summaryDirectory
						+ system + "_Smell_Summary.xlsx"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// write changes
			try {
				workbook.write(excelOutput);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// close the stream
			try {
				excelOutput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static List<File> getFiles(String inputDirectory) {

		List<File> fileList = null;
		try {
			fileList = FileListing.getFileListing(new File(inputDirectory));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileList;
	}
}
