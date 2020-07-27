package edu.usc.softarch.arcade.util.convert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import com.google.common.base.Joiner;

public class ExcelToRsfConverter {
	public static void main(String[] args) {
		String excelFilename = args[0];
		String rsfFilename = args[1];

		try {
			InputStream inp = new FileInputStream(excelFilename);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(0);
			Set<List<String>> facts = new HashSet<List<String>>();
			PrintStream origOut = System.out;
			System.setOut(new PrintStream(new OutputStream(){
				public void write(int b) {
				}
			}));
			for (Row row : sheet) {
				if (row.getRowNum() == 0) { // Skip header row and column
					continue;
				}
				List<String> fact = new ArrayList<String>();
				fact.add("contain");
				for (Cell cell : row) {
					
					buildFact(row, fact, cell);
					facts.add(fact);
				}
			}
			System.setOut(origOut);
			
			System.out.println("As RSF facts...");
			System.out.println(Joiner.on("\n").join(facts));
			
			FileWriter fw = new FileWriter(rsfFilename);
			BufferedWriter out = new BufferedWriter(fw);
			for (List<String> fact : facts) {
				out.write(Joiner.on(" ").join(fact) + "\n");
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void buildFact(Row row, List<String> fact, Cell cell) {
		CellReference cellRef = new CellReference(row.getRowNum(),
				cell.getColumnIndex());
		System.out.print(cellRef.formatAsString());
		System.out.print(" - ");

		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			String cellValue = cell.getRichStringCellValue()
					.getString().trim().replaceAll("\\s", "_");
			fact.add(cellValue);
			System.out.println(cellValue);
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				System.out.println(cell.getDateCellValue());
			} else {
				System.out.println(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			System.out.println(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			System.out.println(cell.getCellFormula());
			break;
		default:
			System.out.println();
		}
	}
}
