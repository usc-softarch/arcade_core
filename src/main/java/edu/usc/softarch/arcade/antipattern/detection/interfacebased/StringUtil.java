package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	public static String extractOrgSuffix(String name) {
		Pattern p = Pattern.compile("org\\/(.*)");
		Matcher m = p.matcher(name);
		if (m.find())
			return m.group(0);

		return null;
	}
	
	/**
	 * convert	src/java/org/apache/hadoop/dfs/FSDataset.java
	 * to 			org.apache.hadoop.dfs.FSDataset
	 * @param dir
	 * @return
	 */
	public static String dir2pkg(String dir) {
		String orgSuffix = extractOrgSuffix(dir);
		String tmp = orgSuffix.substring(0, orgSuffix.lastIndexOf(".java"));
		return tmp.replace('/', '.');
	}
	
	public static String cutInnterClass(String dir) {
		if (dir.contains("$"))
			return dir.split("$")[0];
		else
			return dir;
	}
}