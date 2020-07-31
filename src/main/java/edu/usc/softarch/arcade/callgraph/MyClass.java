package edu.usc.softarch.arcade.callgraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.SootClass;

/**
 * @author joshua
 */
public class MyClass implements Serializable {
	private static final long serialVersionUID = 5575671464833110817L;
	public String packageName;
	public String className;
	Set<MyMethod> methods;
	
	public void addMethod(MyMethod m) {
		methods.add(m);
	}
	
	public String methodsToString(int tabCount) {
		String methodsStr = "";
		int methodCount = 0;
		Iterator<MyMethod> iter = methods.iterator();
		while (iter.hasNext()) {
			MyMethod m = iter.next();
			for (int i=0;i<tabCount;i++) {
				methodsStr +='\t';
			}
			methodsStr += methodCount + ": " + m.toString() + "\n"; 
			methodCount++;
		}
		return methodsStr;
	}
	
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

	public Set<MyMethod> getMethods() {
		return new HashSet<>(methods);
	}
}
