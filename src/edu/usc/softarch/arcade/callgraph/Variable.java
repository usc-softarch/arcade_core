package edu.usc.softarch.arcade.callgraph;

/**
 * @author joshua
 *
 */
public class Variable {
	public String name;
	public String type;
	
	public Variable(Variable retVal) {
		//this.name = new String(retVal.name);
		this.type = new String(retVal.type);
	}
	
	public boolean equals(Object o) {
		Variable v = (Variable) o;
		if (
				//this.name == v.name &&
				this.type == v.type
			)
				return true;
		else 
			return false;
	}
	
	public int hashCode() {
		int hash = 7;
		//hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		hash = 37 * hash + (this.type == null ? 0 : this.type.hashCode());
		return hash;
	}
	
	public String toString() {
		//return "(" + this.name + "," + this.type + ")";
		return this.type;
	}
	
}
