package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.matrix.Cell;
import edu.usc.softarch.util.matrix.CellValueCalculator;
import edu.usc.softarch.util.matrix.CellValueValidator;
import edu.usc.softarch.util.matrix.FastMatrix;

import java.util.Map;
import java.util.Set;

public class RenameFixer {
	static class RFCalculator implements CellValueCalculator<String, Short> {
		@Override
		public Short computeCellValue(String row, String col, int... values) {
			String smaller;
			String bigger;
			if (row.length() > col.length()) {
				smaller = col;
				bigger = row;
			} else {
				smaller = row;
				bigger = col;
			}

			for (int i = 0; i < smaller.length(); i++)
				if (smaller.charAt(smaller.length() - i - 1) != bigger.charAt(bigger.length() - i - 1))
					return (short) i;
			return (short) smaller.length();
		}
	}

	static class RFValidator implements CellValueValidator<String, Short> {
		@Override
		public boolean validateCellValue(Cell<String, Short> cell) {
			int smallerLength = Math.min(cell.v1.length(), cell.v2.length());
			double minMatchLength = smallerLength * 0.8;
			return cell.cellValue > minMatchLength;
		}
	}

	public static void fix(ReadOnlyArchitecture ra1, ReadOnlyArchitecture ra2) {
		// Remove the matched entites from the analysis
		EnhancedSet<String> sourceEntities = ra1.getEntities();
		EnhancedSet<String> targetEntities = ra2.getEntities();
		Set<String> commonEntities = sourceEntities.intersection(targetEntities);
		Set<String> unmatched1 = sourceEntities.difference(commonEntities);
		Set<String> unmatched2 = targetEntities.difference(commonEntities);
		Map<String, ReadOnlyCluster> ra1EntityLocations =
			ra1.getEntityLocationMap();
		Map<String, ReadOnlyCluster> ra2EntityLocations =
			ra2.getEntityLocationMap();

		// Create the cell value calculator and initialize matrix
		FastMatrix<String, Short> matrix = new FastMatrix<>(
			new RFCalculator(), new RFValidator());
		if (unmatched1.isEmpty() || unmatched2.isEmpty())
			return;

		for (String row : unmatched1)
			matrix.addRow(row);
		for (String col : unmatched2)
			matrix.addColumn(col);

		Cell<String, Short> cell;
		while ((cell = matrix.pollMinCell()) != null) {
			// Update the matrix
			matrix.removeRow(cell.v1);
			matrix.removeColumn(cell.v2);

			// Rename the entities
			String match = cell.v1.substring(
				cell.v1.length() - cell.cellValue);
			ReadOnlyCluster c1 = ra1EntityLocations.get(cell.v1);
			ReadOnlyCluster c2 = ra2EntityLocations.get(cell.v2);
			c1.removeEntities(cell.v1);
			c2.removeEntities(cell.v2);
			c1.addEntity(match);
			c2.addEntity(match);
		}
	}
}
