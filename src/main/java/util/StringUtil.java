package util;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public static String extractVersionPretty(final String name) {
		final Pattern p = Pattern
				.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)*(-(RC|ALPHA|BETA|M|Rc|Alpha|Beta|rc|alpha|beta|deb|b|a|final|Final|FINAL)([0-9])*)*");
		final Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}

	public static String extractPkgPrefix(final String name) {
		final Pattern p = Pattern.compile("src\\/(.*)\\/org");
		final Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}

	public static String extractOrgSuffix(final String name) {
		final Pattern p = Pattern.compile("org\\/(.*)");
		final Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}

	public static boolean containStr(final String str, final String[] strs) {
		if (str == null) {
			return false;
		}
		for (String tmp : strs) {
			if (str.equals(tmp)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check whether the filename is valid should be java file should be in the
	 * valid packages, e.g. src/java/org
	 * 
	 * @param filepath
	 * @return
	 */
	public static boolean isValidFilename(final String filepath,
			final String[] prefixs) {
		if (containStr(extractPkgPrefix(filepath), prefixs)
				&& filepath.endsWith(".java")) {
			return true;
		}
		return false;
	}

	public static void printStringSet(Set<String> strs) {
		for (String str : strs) {
			System.out.println(str);
		}
	}

	/**
	 * convert src/java/org/apache/hadoop/dfs/FSDataset.java to
	 * org.apache.hadoop.dfs.FSDataset
	 * 
	 * @param dir
	 * @return
	 */
	public static String dir2pkg(String dir) {
		String orgSuffix = extractOrgSuffix(dir);
		try {
		String tmp = orgSuffix.substring(0, orgSuffix.lastIndexOf(".java"));
		return tmp.replace('/', '.');
		} catch (Exception e){
			return null;
		}
	}

	/**
	 * if the version is 1.1.1.1, then change it to 1.1.1, only keep 3 numbers
	 * 
	 * @param version
	 * @return
	 */
	public static String formatIssueVersion(final String version) {
		 Pattern p = Pattern
				.compile("((\\d)+\\.){3}(\\d)+");
		 Matcher m = p.matcher(version);
		if (m.find()) {
			String tmp = m.group(0);
			p = Pattern.compile("((\\d)+\\.){2}(\\d)+");
			m = p.matcher(tmp);
			if(m.find()){
				return m.group(0);
			}
		}
		return version;
	}
}
