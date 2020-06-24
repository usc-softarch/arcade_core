package edu.usc.softarch.arcade.callgraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import soot.SootClass;
import soot.SootMethod;

/**
 * @author joshua
 *
 */
public class MyClass implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5575671464833110817L;
	public String packageName;
	public String className;
	HashSet<MyMethod> methods;
	
	public void addMethod(MyMethod m) {
		methods.add(m);
	}
	
	public String methodsToString(int tabCount) {
		String methodsStr = "";
		int methodCount = 0;
		Iterator<MyMethod> iter = methods.iterator();
		while (iter.hasNext()) {
			MyMethod m = (MyMethod)iter.next();
			for (int i=0;i<tabCount;i++) {
				methodsStr +='\t';
			}
			methodsStr += methodCount + ": " + m.toString() + "\n"; 
			methodCount++;
		}
		return methodsStr;
	}
	
	public MyClass(MyClass declaringClass) {
		this.className = new String(declaringClass.className);
		this.packageName = new String(declaringClass.packageName);
		this.methods = new HashSet<MyMethod>(declaringClass.methods);
	}
	
	public MyClass(SootClass declaringClass) {
		this.className = new String(declaringClass.getShortName());
		this.packageName = new String(declaringClass.getPackageName());
		this.methods = new HashSet<MyMethod>();
		/*for (SootMethod sm : declaringClass.getMethods()) {
			methods.add(new MyMethod(sm));
		}*/
	}

	public boolean equals(Object o) {
		MyClass c = (MyClass) o;
		if (
				this.className.equals(c.className) &&
				this.packageName.equals(c.packageName)
				//(this.methods == null ? true : this.methods.equals(c.methods) )
			) {
				return true;
		}
		else {
			return false;
		}
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.className == null ? 0 : this.className.hashCode());
		hash = 37 * hash + (this.packageName == null ? 0 : this.packageName.hashCode());
		//hash = 37 * hash + (this.methods == null ? 0 : this.methods.hashCode());
		return hash;
	}
	
	public String toString() {
		return this.packageName + "." + this.className;
	}

	public HashSet<MyMethod> getMethods() {
		return new HashSet<MyMethod>(methods);
	}
	
	
}
