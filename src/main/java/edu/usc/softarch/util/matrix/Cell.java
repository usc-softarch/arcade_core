package edu.usc.softarch.util.matrix;

import java.util.Objects;

public class Cell<T extends Comparable<T>, G extends Comparable<G>> implements Comparable<Cell<T, G>> {
	public final T v1;
	public final T v2;
	public final G cellValue;

	public Cell(T v1, T v2, G cellValue) {
		this.v1 = v1;
		this.v2 = v2;
		this.cellValue = cellValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cell<T, G> otherCell = (Cell<T, G>) o;
		return Objects.equals(this.v1, otherCell.v1)
			&& Objects.equals(this.v2, otherCell.v2)
			&& Objects.equals(this.cellValue, otherCell.cellValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(v1, v2, cellValue); }

	@Override
	public int compareTo(Cell<T, G> o) {
		if (!this.cellValue.equals(o.cellValue))
			return this.cellValue.compareTo(o.cellValue);
		if (!this.v1.equals(o.v1))
			return this.v1.compareTo(o.v1);
		return this.v2.compareTo(o.v2);
	}
}
