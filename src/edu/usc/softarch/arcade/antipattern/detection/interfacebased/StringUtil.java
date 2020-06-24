package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	public static String extractVersionPretty(final String name) {
		final Pattern p = Pattern.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)*(-(RC|ALPHA|BETA|M|Rc|Alpha|Beta|rc|alpha|beta|deb|b|a|final|Final|FINAL)([0-9])*)*");
		final Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}

	public static String extractPkgPrefix(final String name){
		final Pattern p = Pattern.compile("src\\/(.*)\\/org");
		final Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}
	
	public static String extractOrgSuffix(final String name){
		final Pattern p = Pattern.compile("org\\/(.*)");
		final Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}
	
	public static boolean containStr(final String str, final String[] strs){
		if(str == null){
			return false;
		}
		for(String tmp: strs){
			if(str.equals(tmp)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * convert	src/java/org/apache/hadoop/dfs/FSDataset.java
	 * to 			org.apache.hadoop.dfs.FSDataset
	 * @param dir
	 * @return
	 */
	public static String dir2pkg(String dir){
		String orgSuffix = extractOrgSuffix(dir);
		String tmp = orgSuffix.substring(0, orgSuffix.lastIndexOf(".java"));
		return tmp.replace('/', '.');
	}
	
	public static String cutInnterClass(String dir){
		if (dir.contains("$"))
		{
			return dir.split("$")[0];
		} else
			return dir;
	}
	
}