package edu.usc.softarch.util.matrix;

@FunctionalInterface
public interface CellValueCalculator<T, G> {
	public G computeCellValue(T row, T col, int... values);
}
