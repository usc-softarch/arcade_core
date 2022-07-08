package edu.usc.softarch.arcade.visualization.clustering;

import edu.usc.softarch.arcade.clustering.Clusterer;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.visualization.components.DefaultButton;
import edu.usc.softarch.arcade.visualization.topics.DocTopicsViewer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClustererController extends JPanel
		implements ActionListener {
	//region ATTRIBUTES
	// Real Attributes
	private final Clusterer clusterer;

	// Swing Components
	private final ArchitectureViewer viewer;
	private final DefaultButton chooseOtherSystemButton;
	private final DefaultButton reloadSystemButton;
	private final DefaultButton undoStepButton;
	private final DefaultButton doStepButton;
	private final DefaultButton saveResultsButton;
	private final JLabel isReadyToStopLabel;
	private final JLabel cluster1NameLabel;
	private final JLabel cluster2NameLabel;
	private final JLabel simValueLabel;

	// Actions
	private static final String chooseOtherSystemAction = "goBackToSelection";
	private static final String reloadSystemAction = "reloadSystem";
	private static final String undoStepAction = "undoStep";
	private static final String doStepAction = "doStep";
	private static final String saveResultsAction = "saveResults";
	//endregion

	//region CONSTRUCTORS
	public ClustererController(Clusterer clusterer) {
		// Real constructor
		this.clusterer = clusterer;

		// Set layout
		this.setLayout(new GridBagLayout());
		this.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weighty = 0.0;

		// Choose other system button
		this.chooseOtherSystemButton = new DefaultButton(this,
			"Back to selection", chooseOtherSystemAction);
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		this.add(this.chooseOtherSystemButton, c);

		// Undo step button
		this.undoStepButton =
			new DefaultButton(this, "Undo", undoStepAction);
		this.undoStepButton.setEnabled(false); //TODO functionality not ready
		c.gridx = 4; c.gridy = 0; c.weightx = 0.0;
		this.add(this.undoStepButton, c);

		// Next step button
		this.doStepButton =
			new DefaultButton(this, "Next", doStepAction);
		c.gridx = 4; c.gridy = 1; c.weightx = 0.0;
		this.add(this.doStepButton, c);

		// Reload system button
		this.reloadSystemButton =
			new DefaultButton(this, "Reload", reloadSystemAction);
		c.gridx = 0; c.gridy = 2; c.weightx = 0.0;
		this.add(this.reloadSystemButton, c);

		// Ready to stop label
		this.isReadyToStopLabel = new JLabel("");
		c.gridx = 2; c.gridy = 2; c.weightx = 0.0;
		this.add(this.isReadyToStopLabel, c);

		// Save results button
		this.saveResultsButton =
			new DefaultButton(this, "Save", saveResultsAction);
		c.gridx = 4; c.gridy = 2; c.weightx = 0.0;
		this.add(this.saveResultsButton, c);

		// Cluster 1 label
		this.cluster1NameLabel = new JLabel("");
		c.gridx = 0; c.gridy = 3; c.weightx = 0.0;
		this.add(this.cluster1NameLabel, c);

		// Similarity value label
		this.simValueLabel = new JLabel("");
		c.gridx = 2; c.gridy = 3; c.weightx = 0.0;
		this.add(this.simValueLabel, c);

		// Cluster 2 label
		this.cluster2NameLabel = new JLabel("");
		c.gridx = 4; c.gridy = 3; c.weightx = 0.0;
		this.add(this.cluster2NameLabel, c);

		// Architecture viewer
		this.viewer = loadViewer();
		c.gridx = 0; c.gridy = 4; c.weightx = 1.0; c.weighty = 1.0;
		c.gridwidth = 5; c.fill = GridBagConstraints.BOTH;
		this.add(this.viewer, c);
	}

	private ArchitectureViewer loadViewer() {
		if (this.clusterer.algorithm.equals(ClusteringAlgorithmType.ARC))
			return new DocTopicsViewer(this.clusterer.getArchitecture().projectName);
		return new FeatureVectorViewer(this.clusterer.getArchitecture());
	}
	//endregion

	//region PROCESSING
	@Override
	public void actionPerformed(ActionEvent e) {

	}
	//endregion
}
