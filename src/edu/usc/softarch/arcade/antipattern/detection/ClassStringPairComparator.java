package edu.usc.softarch.arcade.antipattern.detection;

import java.util.Comparator;

import org.apache.commons.lang3.tuple.Pair;

public class ClassStringPairComparator implements Comparator<Pair<Class,String>> {

	@Override
	public int compare(Pair<Class, String> o1, Pair<Class, String> o2) {
		/*int leftCompare = o1.getLeft().getName().compareTo(o2.getLeft().getName());
		if (leftCompare != 0 ) {
			return leftCompare;
		}
		else {
			return o1.getRight().compareTo(o2.getRight());
		}*/
		return o1.getLeft().getName().compareTo(o2.getLeft().getName());
		
	}
	
}