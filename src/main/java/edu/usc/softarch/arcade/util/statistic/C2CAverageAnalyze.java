package edu.usc.softarch.arcade.util.statistic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class C2CAverageAnalyze {

	public static void main(String[] args) {
		String sourceFile = args[0];
		
		try (BufferedReader br = new BufferedReader(new FileReader(sourceFile)))
		{
			String sCurrentLine;
			float MajortotalST = 0;
			float MajortotalTS = 0;
			int Majorcount = 0;

			while ((sCurrentLine = br.readLine()) != null) {
						
				if(sCurrentLine.contains("source")){
					//get metric from source to target
					float metricST = Float.parseFloat(sCurrentLine.split(":")[1].replaceAll(" ", ""));
					System.out.println(sCurrentLine);
					//get metric from target to source
					sCurrentLine = br.readLine();
					float metricTS = Float.parseFloat(sCurrentLine.split(":")[1].replaceAll(" ", ""));
					System.out.println(sCurrentLine);
					//
					MajortotalST += metricST;
					MajortotalTS += metricTS;
					Majorcount += 1;
					
					//move to next version
					while (!(sCurrentLine = br.readLine()).equals("")) {}
					sCurrentLine = br.readLine();
				}
			}
			
		System.out.println("Avg source to target = " + MajortotalST/Majorcount);
		System.out.println("Avg target to source = " + MajortotalTS/Majorcount);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}