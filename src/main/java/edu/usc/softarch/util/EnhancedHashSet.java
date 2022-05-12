package edu.usc.softarch.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EnhancedHashSet<E>
		extends HashSet<E> implements EnhancedSet<E> {
	public EnhancedHashSet() { super(); }
	public EnhancedHashSet(Collection<? extends E> c) { super(c);	}
	public EnhancedHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor); }
	public EnhancedHashSet(int initialCapacity) { super(initialCapacity); }

	@Override
	public Set<E> union(Collection<E> c) {
		Set<E> unionSet = new EnhancedHashSet<>(this);
		unionSet.addAll(c);
		return unionSet;
	}

	@Override
	public Set<E> intersection(Collection<E> c) {
		Set<E> intersectionSet = new EnhancedHashSet<>(this);
		intersectionSet.retainAll(c);
		return intersectionSet;
	}

	@Override
	public Set<E> difference(Collection<E> c) {
		Set<E> differenceSet = new EnhancedHashSet<>(this);
		differenceSet.removeAll(c);
		return differenceSet;
	}

	@Override
	public Set<E> symmetricDifference(Collection<E> c) {
		Set<E> symmetricDiff = new EnhancedHashSet<>(this);
		symmetricDiff.addAll(c);
		symmetricDiff.removeAll(this.intersection(c));
		return symmetricDiff;
	}

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (E e : this)
			toReturn.append(e.toString()).append(System.lineSeparator());
		return toReturn.toString();
	}
}
