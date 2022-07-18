package edu.usc.softarch.arcade.visualization.clustering;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.visualization.ArchitectureTableModel;

public class FeatureVectorViewer extends ArchitectureViewer {
	//region ATTRIBUTES
	private final Architecture arch;
	//endregion

	//region CONSTRUCTORS
	public FeatureVectorViewer(Architecture arch) {
		super(arch.projectName, arch.projectVersion);
		this.arch = arch;
	}
	//endregion

	//region PROCESSING
	@Override
	protected ArchitectureTableModel buildArchitectureTable() {
		return new FeatureVectorTableModel(this.arch); }

	@Override
	protected Integer[] getFeatureIndex() {
		Integer[] featureNumberList = new Integer[
			this.arch.values().stream().findFirst().get().getNumEntities()];
		for (int i = 0; i < featureNumberList.length; i++)
			featureNumberList[i] = i + 1;

		return featureNumberList;
	}

	@Override
	protected String getIndexName() {
		return "Cluster:"; }

	@Override
	protected String getFeatureName() {
		return "Feature:";}

	@Override
	protected String getFeatureRankText() {
		return "Highest Feature in selected cluster:"; }
	//endregion
}
