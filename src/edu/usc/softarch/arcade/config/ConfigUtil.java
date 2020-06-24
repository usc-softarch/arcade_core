package edu.usc.softarch.arcade.config;

import edu.usc.softarch.arcade.clustering.Entity;
import edu.usc.softarch.arcade.clustering.FeatureVector;

/**
 * @author joshua
 *
 */
public class ConfigUtil {
	public static String anonymousInnerClassRegExpr = "^.*\\$\\d+$";
	public static String doubleInnerClassRegExpr = "^.*\\$.*\\$.*$";
	
	public static String stripParensEnclosedClassNameWithPackageName(
			FeatureVector leaf) {
		return leaf.name.substring(leaf.name.lastIndexOf('.')+1,leaf.name.length()-1);
	}
	public static String stripParensEnclosedClassNameWithPackageName(
			Entity leaf) {
		return leaf.name.substring(leaf.name.lastIndexOf('.')+1,leaf.name.length());
	}
}
