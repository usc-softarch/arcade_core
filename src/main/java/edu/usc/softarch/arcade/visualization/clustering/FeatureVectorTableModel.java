package edu.usc.softarch.arcade.visualization.clustering;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.visualization.ArchitectureTableModel;
import org.apache.commons.lang.NotImplementedException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class FeatureVectorTableModel extends ArchitectureTableModel {
	//region ATTRIBUTES
	private final Architecture arch;
	//endregion

	//region CONSTRUCTORS
	public FeatureVectorTableModel(Architecture arch) {
		this.arch = arch; }
	//endregion

	//region PROCESSING
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Cluster";
		else
			return String.valueOf(column);
	}

	@Override
	public int getRowCount() {
		return this.arch.size();
	}

	@Override
	public int getColumnCount() {
		return this.arch.firstEntry().getValue().getNumEntities() + 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Cluster row = (new ArrayList<>(this.arch.values())).get(rowIndex);

		if (columnIndex == 0)
			return row.name;

		return row.getFeatureMap()[columnIndex - 1];
	}

	@Override
	public Map.Entry<Integer, Double> getHighestValue(int row) {
		Cluster cluster = (new ArrayList<>(this.arch.values())).get(row);

		int featureIndex = -1;
		double proportion = 0.0;
		double[] featureMap = cluster.getFeatureMap();

		for (int i = 0; i < featureMap.length; i++) {
			if (featureMap[i] > proportion) {
				featureIndex = i;
				proportion = featureMap[i];
			}
		}

		return new AbstractMap.SimpleEntry<>(featureIndex + 1, proportion);
	}

	@Override
	public void refresh() {
		throw new NotImplementedException(); }
	//endregion
}
