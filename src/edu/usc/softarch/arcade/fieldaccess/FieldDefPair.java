package edu.usc.softarch.arcade.fieldaccess;

import soot.SootField;
import soot.Unit;

/**
 * @author joshua
 *
 */
public class FieldDefPair {
	public SootField f;
	public Unit u;
	
	public FieldDefPair(SootField f, Unit u) {
		this.f = f;
		this.u = u;
	}
}