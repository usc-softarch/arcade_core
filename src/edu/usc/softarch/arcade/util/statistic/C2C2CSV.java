package edu.usc.softarch.arcade.util.statistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class C2C2CSV {

	public static void main(String[] args) throws IOException {
		String sourceFile = args[0];
		Writer writer = null;

		System.out.println("### Input file = " + sourceFile);

		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(sourceFile + ".csv"), "utf-8"));
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourceFile));

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains("source")) {
					// get metric from source to target
					String fromVersion = sCurrentLine.split("from")[1]
							.split("to")[0].trim();
					String toVersion = sCurrentLine.split("from")[1]
							.split("to")[1].split(":")[0].trim();
					float metricST = Float
							.parseFloat(sCurrentLine.split(":")[1].trim());
					// get metric from target to source
					sCurrentLine = br.readLine();
					float metricTS = Float
							.parseFloat(sCurrentLine.split(":")[1].trim());
					System.out.println(fromVersion + "," + toVersion + ","
							+ metricST + "," + metricTS);
					writer.write(fromVersion + "," + toVersion + "," + metricST
							+ "," + metricTS + "\n");
					// move to next version
					while (!(sCurrentLine = br.readLine()).equals("")) {
					}
					sCurrentLine = br.readLine();
				} else
					continue;
			}
			br.close();
		} catch (IOException ex) {
			System.out.println("Unable to read input file: "
					+ ex.getLocalizedMessage());
		}
		try {
			writer.close();
		} catch (Exception ex) {
			System.out.println("Unable to close writer:"
					+ ex.getLocalizedMessage());
		}
	}
}
