package edu.usc.softarch.arcade.antipattern.detection.interfacebased.TYPES;

public class Class {
	public Component parent;
	public String	 name;
	public Class(String n, Component p){
		parent = p;
		name   = n;
	}
}
