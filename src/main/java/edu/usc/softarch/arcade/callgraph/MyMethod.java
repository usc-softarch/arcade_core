package edu.usc.softarch.arcade.callgraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;

import soot.SootMethod;
import soot.Type;

/**
 * @author joshua
 *
 */
public class MyMethod implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -268381565397216273L;
	public String name;
	HashSet<String> params;
	public String retVal;
	public MyClass declaringClass;
	public boolean isPublic;
	public String type;
	
	public MyMethod(MyMethod method) {
		this.name = new String(method.name);
		this.retVal = new String(method.retVal);
		this.declaringClass = method.declaringClass;
		this.params = new HashSet<String>(method.params);
		this.isPublic = method.isPublic;
	}
	
	public MyMethod(SootMethod method) {
		this.name = new String(method.getName());
		this.retVal = new String(method.getReturnType().toString());
		this.params = new HashSet<String>();
		List<Type> parameterTypes = (List<Type>)method.getParameterTypes();
		for (Type t : parameterTypes) {
			this.params.add(new String(t.toString()));
		}
		this.declaringClass = new MyClass(method.getDeclaringClass()); 
		this.isPublic = method.isPublic();
	}

	public boolean equals (Object o) {
		MyMethod method = (MyMethod) o;
		if (
				this.name.equals(method.name) &&
				this.retVal.equals(method.retVal) &&
				this.declaringClass.equals(method.declaringClass) &&
				this.params.equals(method.params) &&
				this.isPublic == method.isPublic
			)
			return true;
		else 
			return false;
			
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		hash = 37 * hash + (this.retVal == null ? 0 : this.retVal.hashCode());
		hash = 37 * hash + (this.declaringClass == null ? 0 : this.declaringClass.hashCode());
		hash = 37 * hash + (this.params == null ? 0 : this.params.hashCode());
		hash = 37 * hash + (this.isPublic ? 1 : 0);
		return hash;
	}
	
	public String toString() {
		return "(" + (this.isPublic ? "public" : "private") + "," + this.declaringClass.toString() + "." + this.name + ")";
		//return this.name;
	}

	public HashSet<String> getParams() {
		return new HashSet<String>(params);
	}
}
