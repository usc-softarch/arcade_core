package edu.usc.softarch.arcade.util.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DependencyGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numEntities = 100;
		int[][] depMatrix = new int[numEntities][numEntities];
		
		Random random = new Random(55);

		for (int i=0;i<numEntities;i++) {
			for (int j=0;j<numEntities;j++) {
				depMatrix[i][j] = random.nextInt(2);
			}
		}
		
		for (int i=0;i<numEntities;i++) {
			for (int j = 0; j < numEntities; j++) {
				System.out.print(depMatrix[i][j] + " ");
			}
			System.out.println();
		}
		
		
		for (int i=0;i<numEntities;i++) {
			for (int j=0;j<numEntities;j++) {
				if (depMatrix[i][j] == 1)  {
					System.out.println("depends e" + i + " e" + j);
				}
			}
		}
		
		
		
		try {
			String dirStr = "data" + File.separator + "generated" + File.separator;
			File dir = new File(dirStr);
			dir.mkdirs();
			FileWriter fstream = new FileWriter(dirStr + "generated_deps_" + numEntities + ".rsf");
			BufferedWriter out = new BufferedWriter(fstream);
			
			for (int i=0;i<numEntities;i++) {
				for (int j=0;j<numEntities;j++) {
					if (depMatrix[i][j] == 1)  {
						out.write("depends e" + i + ".c e" + j + ".c\n");
					}
				}
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
