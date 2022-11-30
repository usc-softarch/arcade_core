package edu.usc.softarch.util;

import java.util.Collection;
import java.util.Set;

public interface EnhancedSet<E> extends Set<E> {
	Set<E> union(Collection<E> c);
	Set<E> intersection(Collection<E> c);
	Object[] intersectionArray(Collection<E> c);
	int intersectionSize(Collection<E> c);
	Set<E> difference(Collection<E> c);
	Set<E> symmetricDifference(Collection<E> c);
	Object[] symmetricDifferenceArray(Collection<E> c);
	int symmetricDifferenceSize(Collection<E> c);
}
