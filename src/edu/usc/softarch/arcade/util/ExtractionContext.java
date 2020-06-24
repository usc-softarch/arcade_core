package edu.usc.softarch.arcade.util;

/**
 * @author joshua
 *
 */
public class ExtractionContext {
	public static String getCurrentClassAndMethodName() {
		return Thread.currentThread().getStackTrace()[2].getClassName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName();
		
	}
}
