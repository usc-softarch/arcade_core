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
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -268381565397216273L;

	private String name;
	private Set<String> params;
	private String retVal;
	private MyClass declaringClass;
	private boolean publicScope;
	private String type;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public MyMethod(MyMethod method) {
		this.name = method.name;
		this.retVal = method.retVal;
		this.declaringClass = method.declaringClass;
		this.params = new HashSet<>(method.params);
		this.publicScope = method.publicScope;
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
		this.publicScope = method.isPublic();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getName() { return this.name; }
	public Set<String> getParams() { return new HashSet<>(params); }
	public String getRetVal() { return this.retVal; }
	public MyClass getDeclaringClass() { return new MyClass(declaringClass); }
	public boolean isPublic() { return this.publicScope; }
	public String getType() { return this.type; }

	public void setName(String name) { this.name = name; }
	public boolean addParam(String param) { return this.params.add(param); }
	public boolean removeParam(String param) { return this.params.remove(param); }
	public void setRetVal(String retVal) { this.retVal = retVal; }
	public void setDeclaringClass(MyClass declaringClass) {
		this.declaringClass = declaringClass; }
	public void setPublic(boolean publicScope) { this.publicScope = publicScope; }
	public void setType(String type) { this.type = type; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region MISC --------------------------------------------------------------
	@Override
	public boolean equals (Object o) {
		if(!(o instanceof MyMethod))
			return false;
		
		MyMethod method = (MyMethod) o;
		
		return this.name.equals(method.name) &&
			this.retVal.equals(method.retVal) &&
			this.declaringClass.equals(method.declaringClass) &&
			this.params.equals(method.params) &&
			this.publicScope == method.publicScope;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		hash = 37 * hash + (this.retVal == null ? 0 : this.retVal.hashCode());
		hash = 37 * hash + (this.declaringClass == null ? 0 : this.declaringClass.hashCode());
		hash = 37 * hash + (this.params == null ? 0 : this.params.hashCode());
		hash = 37 * hash + (this.publicScope ? 1 : 0);
		return hash;
	}
	
	@Override
	public String toString() {
		String toReturn = "(" + (this.publicScope ? "public" : "private") + ",";
		toReturn += this.declaringClass.toString() + "." + this.name + ")";
		return toReturn;
	}
	// #endregion MISC -----------------------------------------------------------
}
