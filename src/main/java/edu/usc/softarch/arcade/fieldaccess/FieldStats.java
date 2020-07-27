package edu.usc.softarch.arcade.fieldaccess;

/**
 * @author joshua
 *
 */
public class FieldStats {
	public int fieldReadCount;
	public int fieldWriteCount;
	public int fieldInstanceInvokeCount;
	
	FieldStats() {
		super();
		fieldReadCount=0;
		fieldWriteCount=0;
		fieldInstanceInvokeCount=0;
	}
}