package edu.usc.softarch.arcade.callgraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Type;

/**
 * @author joshua
 */
public class MyMethod implements Serializable {
	private static final long serialVersionUID = -268381565397216273L;
	public String name;
	Set<String> params;
	public String retVal;
	public MyClass declaringClass;
	public boolean isPublic;
	public String type;
	
	public MyMethod(MyMethod method) {
		this.name = method.name;
		this.retVal = method.retVal;
		this.declaringClass = method.declaringClass;
		this.params = new HashSet<>(method.params);
		this.isPublic = method.isPublic;
	}
	
	public MyMethod(SootMethod method) {
		this.name = method.getName();
		this.retVal = method.getReturnType().toString();
		this.params = new HashSet<>();
		List<Type> parameterTypes = method.getParameterTypes();
		for (Type t : parameterTypes) {
			this.params.add(t.toString());
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
	}

	public Set<String> getParams() {
		return new HashSet<>(params);
	}
}
