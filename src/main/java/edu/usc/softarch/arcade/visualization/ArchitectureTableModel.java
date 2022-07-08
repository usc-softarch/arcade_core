package edu.usc.softarch.arcade.visualization;

import javax.swing.table.AbstractTableModel;
import java.util.Map;

public abstract class ArchitectureTableModel extends AbstractTableModel {
	public abstract Map.Entry<Integer, Double> getHighestValue(int row);
}
