package edu.usc.softarch.arcade.callgraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import soot.SootClass;

/**
 * @author joshua
 */
public class MyClass implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 5575671464833110817L;

	private String packageName;
	private String className;
	private Set<MyMethod> methods;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public MyClass(MyClass declaringClass) {
		this.className = declaringClass.className;
		this.packageName = declaringClass.packageName;
		this.methods = new HashSet<>(declaringClass.methods);
	}
	
	public MyClass(SootClass declaringClass) {
		this.className = declaringClass.getShortName();
		this.packageName = declaringClass.getPackageName();
		this.methods = new HashSet<>();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getPackageName() { return this.packageName; }
	public String getClassName() { return this.className; }
	public Set<MyMethod> getMethods() {
		return new HashSet<>(methods); }

	public void addMethod(MyMethod m) {
		methods.add(m);	}
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	public String methodsToString(int tabCount) {
		String methodsStr = "";
		int methodCount = 0;

		for (MyMethod m : methods) {
			methodsStr += "\t".repeat(tabCount);
			methodsStr += methodCount + ": " + m.toString() + "\n"; 
			methodCount++;
		}

		return methodsStr;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MyClass))
			return false;

		MyClass c = (MyClass) o;
		return (this.className.equals(c.className)
			&& this.packageName.equals(c.packageName));
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.className == null ? 0 : this.className.hashCode());
		hash = 37 * hash + (this.packageName == null ? 0 : this.packageName.hashCode());
		return hash;
	}
	
	@Override
	public String toString() {
		return this.packageName + "." + this.className;
	}
	// #endregion MISC -----------------------------------------------------------
}