package logical_coupling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class cleanUpCodeMaat {
	
	private static String[] PATTERNS = {
			"src/org/apache/",
			"java/org/apache/",
			"java/main/org/apache/",
			"java/test/org/apache/",
			"test/org/apache/"
	};

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String inputDirFilename = args[0];
		File folder = new File(inputDirFilename);
		File[] files = folder.listFiles();
				
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith("csv")){
			String sourceFile = file.getName();
			single(sourceFile, inputDirFilename);
			}
		}
		
	
	}

	private static void single(String sourceFile, String folder) {
		
		String targetFile = folder + File.separator + sourceFile.split("\\.")[0] + "_clean.csv";
		String output;
		try (BufferedReader br = new BufferedReader(new FileReader(folder + File.separator + sourceFile)))
		{
			String sCurrentLine;
			// skip the first line
			sCurrentLine = br.readLine();
			output = sCurrentLine ;
			while ((sCurrentLine = br.readLine()) != null) {
					String[] temp = sCurrentLine.split(",");
					if (temp[0].endsWith(".java")||temp[1].endsWith(".java")){
					String class1 = convertFile(temp[0]);
					String class2 = convertFile(temp[1]);
					if (class1 != null & class2 != null){
						output += "\n" + class1 + "," + class2 + "," + temp[2] + "," + temp[3];
					}
				}
			}
			
			PrintWriter out = new PrintWriter(targetFile);
			out.println(output);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String convertFile(String javaName){
		String output = javaName;
		System.out.println(javaName);
		
		for (String pattern : PATTERNS) {
			if (javaName.contains(pattern)) {
				output = "org.apache." + output.split(pattern)[1].replace("/", ".").replace(".java", "");
				return output;
			}
		}
		
		return null;
	}
	
}
