package edu.usc.softarch.util;

import java.util.Collection;
import java.util.Set;

public interface EnhancedSet<E> extends Set<E> {
	public Set<E> union(Collection<E> c);
	public Set<E> intersection(Collection<E> c);
	public Set<E> difference(Collection<E> c);
	public Set<E> symmetricDifference(Collection<E> c);
}
