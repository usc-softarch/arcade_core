package edu.usc.softarch.util.matrix;

@FunctionalInterface
public interface CellValueValidator<T extends Comparable<T>, G extends Comparable<G>> {
	public boolean validateCellValue(Cell<T, G> cell);
}
