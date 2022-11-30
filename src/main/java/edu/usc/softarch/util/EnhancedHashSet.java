package edu.usc.softarch.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This implementation of {@link EnhancedSet} is primarily for use in
 * applications that have a low frequency of use of the enhanced operations,
 * and a high frequency of regular operations. It has little emphasis on
 * optimization but benefits from the underlying {@link HashSet} structure.
 */
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
	public Object[] intersectionArray(Collection<E> c) {
		return this.intersection(c).toArray(); }

	@Override
	public int intersectionSize(Collection<E> c) {
		return this.intersection(c).size(); }

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
	public Object[] symmetricDifferenceArray(Collection<E> c) {
		return this.symmetricDifference(c).toArray(); }

	@Override
	public int symmetricDifferenceSize(Collection<E> c) {
		return this.symmetricDifference(c).size(); }

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (E e : this)
			toReturn.append(e.toString()).append(System.lineSeparator());
		return toReturn.toString();
	}
}
