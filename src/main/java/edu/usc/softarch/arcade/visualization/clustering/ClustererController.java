package edu.usc.softarch.arcade.visualization.clustering;

import edu.usc.softarch.arcade.clustering.Clusterer;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.simmeasures.SimData;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.arcade.visualization.components.DefaultButton;
import edu.usc.softarch.arcade.visualization.topics.DocTopicsViewer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Dimension;
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
	private final JTextPane archSizeTextPane;

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
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0;

		// Choose other system button
		this.chooseOtherSystemButton = new DefaultButton(this,
			"Back to selection", chooseOtherSystemAction);
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		this.add(this.chooseOtherSystemButton, c);

		// Undo step button
		this.undoStepButton =
			new DefaultButton(this, "Undo", undoStepAction);
		this.undoStepButton.setEnabled(false); //TODO functionality not ready
		c.gridx = 2; c.gridy = 0; c.weightx = 1.0;
		this.add(this.undoStepButton, c);

		// Architecture size label
		this.archSizeTextPane = new JTextPane();
		this.archSizeTextPane.setEditable(false);
		this.archSizeTextPane.setBackground(null);
		this.archSizeTextPane.setBorder(null);
		StyledDocument archSizeDoc = this.archSizeTextPane.getStyledDocument();
		SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
		StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
		archSizeDoc.setParagraphAttributes(
			0, archSizeDoc.getLength(), centerAttribute, false);
		c.gridx = 1; c.gridy = 1; c.weightx = 1.0;
		this.add(this.archSizeTextPane, c);

		// Next step button
		this.doStepButton =
			new DefaultButton(this, "Next", doStepAction);
		c.gridx = 2; c.gridy = 1; c.weightx = 1.0;
		this.add(this.doStepButton, c);

		// Reload system button
		this.reloadSystemButton =
			new DefaultButton(this, "Reload", reloadSystemAction);
		c.gridx = 0; c.gridy = 2; c.weightx = 1.0;
		this.add(this.reloadSystemButton, c);

		// Ready to stop label
		this.isReadyToStopLabel = new JLabel("Text");
		this.isReadyToStopLabel.setHorizontalAlignment(SwingConstants.CENTER);
		c.gridx = 1; c.gridy = 2; c.weightx = 1.0;
		this.add(this.isReadyToStopLabel, c);

		// Save results button
		this.saveResultsButton =
			new DefaultButton(this, "Save", saveResultsAction);
		c.gridx = 2; c.gridy = 2; c.weightx = 1.0;
		this.add(this.saveResultsButton, c);

		// Cluster 1 label
		this.cluster1NameLabel = new JLabel("Text");
		this.cluster1NameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.cluster1NameLabel.setPreferredSize(new Dimension(200,25));
		c.gridx = 0; c.gridy = 3; c.weightx = 0.0;
		this.add(this.cluster1NameLabel, c);

		// Similarity value label
		this.simValueLabel = new JLabel("Text");
		this.simValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.simValueLabel.setPreferredSize(new Dimension(200,25));
		c.gridx = 1; c.gridy = 3; c.weightx = 0.0;
		this.add(this.simValueLabel, c);

		// Cluster 2 label
		this.cluster2NameLabel = new JLabel("Text");
		this.cluster2NameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.cluster2NameLabel.setPreferredSize(new Dimension(200,25));
		c.gridx = 2; c.gridy = 3; c.weightx = 0.0;
		this.add(this.cluster2NameLabel, c);

		// Architecture viewer
		this.viewer = loadViewer();
		c.gridx = 0; c.gridy = 4; c.weightx = 1.0; c.weighty = 1.0;
		c.gridwidth = 3; c.fill = GridBagConstraints.BOTH;
		this.add(this.viewer, c);

		refreshLabels();
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
		if (e.getActionCommand() == null) return;

		switch (e.getActionCommand()) {
			case doStepAction:
				try {
					this.clusterer.doClusteringStep();
				} catch (UnmatchingDocTopicItemsException ex) {
					throw new RuntimeException(ex);
				} catch (DistributionSizeMismatchException ex) {
					throw new RuntimeException(ex);
				}
				this.refreshLabels();
				this.loadViewer();
				break;
		}
	}

	private void refreshLabels() {
		boolean notReadyToStop = this.clusterer.stopCrit
			.notReadyToStop(this.clusterer.getArchitecture());

		if (notReadyToStop)
			this.isReadyToStopLabel.setText("Clustering not finished running.");
		else
			this.isReadyToStopLabel.setText("Clustering is done!");

		SimData data = this.clusterer.identifyMostSimClusters();

		this.cluster1NameLabel.setText(data.c1.name);
		this.cluster2NameLabel.setText(data.c2.name);
		this.simValueLabel.setText(data.cellValue.toString());

		int archSize = this.clusterer.getArchitecture().size();
		this.archSizeTextPane.setText("Architecture size: " + archSize);
	}
	//endregion
}
