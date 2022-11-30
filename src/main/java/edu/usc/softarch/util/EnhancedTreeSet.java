package edu.usc.softarch.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This implementation of {@link EnhancedSet} is highly optimized for the
 * actual enhanced operations. It is slower at effecting regular {@link Set}
 * operations due to using {@link TreeSet} as its underlying structure, but
 * is ideal in applications with a high frequency of use of the enhanced
 * operations.
 */
public class EnhancedTreeSet<E extends Comparable<E>>
		extends TreeSet<E> implements EnhancedSet<E> {
	public EnhancedTreeSet() { super(); }
	public EnhancedTreeSet(Collection<? extends E> c) { super(c);	}
	public EnhancedTreeSet(E[] a) { super(List.of(a)); }

	@Override
	public Set<E> union(Collection<E> c) {
		if (c.isEmpty()) return this;
		if (this.isEmpty()) return new EnhancedTreeSet<>(c);

		Object[] tElements = this.toArray();
		Object[] cElements = c.toArray();
		Object[] result = new Object[tElements.length + cElements.length];

		int resultIndex, tIndex, cIndex;
		resultIndex = tIndex = cIndex = 0;

		// While both sets still have elements
		while (tIndex < tElements.length && cIndex < cElements.length) {
			int relation = ((E) tElements[tIndex]).compareTo((E) cElements[cIndex]);
			// Elements are the same, move all indices
			if (relation == 0) {
				result[resultIndex++] = tElements[tIndex++];
				cIndex++;
			}
			// c element is the smaller one, move cIndex
			else if (relation > 0)
				result[resultIndex++] = cElements[cIndex++];
			// t element is the smaller one, move tIndex
			else
				result[resultIndex++] = tElements[tIndex++];
		}

		// Parse the rest of cElements
		while (cIndex < cElements.length)
			result[resultIndex++] = cElements[cIndex++];
		// Parse the rest of tElements
		while (tIndex < tElements.length)
			result[resultIndex++] = tElements[tIndex++];

		Set<E> resultSet = new EnhancedTreeSet<>();
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null) break;
			resultSet.add((E) result[i]);
		}
		return resultSet;
	}

	@Override
	public Set<E> intersection(Collection<E> c) {
		Object[] result = this.intersectionArray(c);

		Set<E> resultSet = new EnhancedTreeSet<>();
		for (Object o : result) resultSet.add((E) o);
		return resultSet;
	}

	@Override
	public Object[] intersectionArray(Collection<E> c) {
		if (c.isEmpty() || this.isEmpty()) return new Object[0];

		Object[] tElements = this.toArray();
		Object[] cElements = c.toArray();
		Object[] result = new Object[Math.max(tElements.length, cElements.length)];

		int resultIndex, tIndex, cIndex;
		resultIndex = tIndex = cIndex = 0;

		// While both sets still have elements
		while (tIndex < tElements.length && cIndex < cElements.length) {
			int relation = ((E) tElements[tIndex]).compareTo((E) cElements[cIndex]);
			// Elements are the same, insert
			if (relation == 0) {
				result[resultIndex++] = tElements[tIndex++];
				cIndex++;
			}
			// c element is the smaller one, move cIndex and discard
			else if (relation > 0) cIndex++;
				// t element is the smaller one, move tIndex and discard
			else tIndex++;
		}

		// Remaining elements are unique to one set, no need to parse
		int finalSize = 0;
		for (Object o : result) {
			if (o == null) break;
			finalSize++;
		}

		return Arrays.copyOf(result, finalSize);
	}

	@Override
	public int intersectionSize(Collection<E> c) {
		return this.intersectionArray(c).length; }

	@Override
	public Set<E> difference(Collection<E> c) {
		if (c.isEmpty()) return this;
		if (this.isEmpty()) return new EnhancedTreeSet<>();

		Object[] tElements = this.toArray();
		Object[] cElements = c.toArray();
		Object[] result = new Object[tElements.length];

		int resultIndex, tIndex, cIndex;
		resultIndex = tIndex = cIndex = 0;

		// While both sets still have elements
		while (tIndex < tElements.length && cIndex < cElements.length) {
			int relation = ((E) tElements[tIndex]).compareTo((E) cElements[cIndex]);
			// Elements are the same, move input indices and discard
			if (relation == 0) {
				tIndex++;
				cIndex++;
			}
			// c element is the smaller one, move cIndex and discard
			else if (relation > 0)
				cIndex++;
			// t element is the smaller one, move tIndex and insert
			else
				result[resultIndex++] = tElements[tIndex++];
		}

		// Parse the rest of tElements, cElements don't matter
		while (tIndex < tElements.length)
			result[resultIndex++] = tElements[tIndex++];

		Set<E> resultSet = new EnhancedTreeSet<>();
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null) break;
			resultSet.add((E) result[i]);
		}
		return resultSet;
	}

	@Override
	public Set<E> symmetricDifference(Collection<E> c) {
		Object[] result = this.symmetricDifferenceArray(c);

		Set<E> resultSet = new EnhancedTreeSet<>();
		for (Object o : result) resultSet.add((E) o);
		return resultSet;
	}

	@Override
	public Object[] symmetricDifferenceArray(Collection<E> c) {
		if (c.isEmpty()) return this.toArray();
		if (this.isEmpty()) return c.toArray();

		Object[] tElements = this.toArray();
		Object[] cElements = c.toArray();
		Object[] result = new Object[tElements.length + cElements.length];

		int resultIndex, tIndex, cIndex;
		resultIndex = tIndex = cIndex = 0;

		// While both sets still have elements
		while (tIndex < tElements.length && cIndex < cElements.length) {
			int relation = ((E) tElements[tIndex]).compareTo((E) cElements[cIndex]);
			// Elements are the same, move indices and discard
			if (relation == 0) {
				tIndex++;
				cIndex++;
			}
			// c element is the smaller one, move cIndex and insert
			else if (relation > 0)
				result[resultIndex++] = cElements[cIndex++];
				// t element is the smaller one, move tIndex and insert
			else
				result[resultIndex++] = tElements[tIndex++];
		}

		// Parse the rest of cElements
		while (cIndex < cElements.length)
			result[resultIndex++] = cElements[cIndex++];
		// Parse the rest of tElements
		while (tIndex < tElements.length)
			result[resultIndex++] = tElements[tIndex++];

		int finalSize = 0;
		for (Object o : result) {
			if (o == null) break;
			finalSize++;
		}

		return Arrays.copyOf(result, finalSize);
	}

	@Override
	public int symmetricDifferenceSize(Collection<E> c) {
		return this.symmetricDifferenceArray(c).length; }

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (E e : this)
			toReturn.append(e.toString()).append(System.lineSeparator());
		return toReturn.toString();
	}
}
