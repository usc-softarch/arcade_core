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
	private final JTextPane cluster1NameTextPane;
	private final JTextPane cluster2NameTextPane;
	private final JLabel simValueLabel;
	private final JLabel highestDTIMatchLabel;
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
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		this.add(this.archSizeTextPane, c);

		// Undo step button
		this.undoStepButton =
			new DefaultButton(this, "Undo", undoStepAction);
		this.undoStepButton.setEnabled(false); //TODO functionality not ready
		c.gridx = 2; c.gridy = 0; c.weightx = 1.0;
		this.add(this.undoStepButton, c);

		// DTI match evaluation label
		this.highestDTIMatchLabel = new JLabel("Text");
		this.highestDTIMatchLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.highestDTIMatchLabel.setPreferredSize(new Dimension(200,25));
		c.gridx = 1; c.gridy = 1; c.weightx = 0.0;
		this.add(this.highestDTIMatchLabel, c);

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
		this.cluster1NameTextPane = new JTextPane();
		this.cluster1NameTextPane.setEditable(false);
		this.cluster1NameTextPane.setBackground(null);
		this.cluster1NameTextPane.setBorder(null);
		StyledDocument cluster1Doc = this.cluster1NameTextPane.getStyledDocument();
		SimpleAttributeSet cluster1centerAttribute = new SimpleAttributeSet();
		StyleConstants.setAlignment(cluster1centerAttribute, StyleConstants.ALIGN_CENTER);
		cluster1Doc.setParagraphAttributes(
			0, cluster1Doc.getLength(), cluster1centerAttribute, false);
		this.cluster1NameTextPane.setPreferredSize(new Dimension(200,25));
		c.gridx = 0; c.gridy = 3; c.weightx = 0.0;
		this.add(this.cluster1NameTextPane, c);

		// Similarity value label
		this.simValueLabel = new JLabel("Text");
		this.simValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.simValueLabel.setPreferredSize(new Dimension(200,25));
		c.gridx = 1; c.gridy = 3; c.weightx = 0.0;
		this.add(this.simValueLabel, c);

		// Cluster 2 label
		this.cluster2NameTextPane = new JTextPane();
		this.cluster2NameTextPane.setEditable(false);
		this.cluster2NameTextPane.setBackground(null);
		this.cluster2NameTextPane.setBorder(null);
		StyledDocument cluster2Doc = this.cluster2NameTextPane.getStyledDocument();
		SimpleAttributeSet cluster2centerAttribute = new SimpleAttributeSet();
		StyleConstants.setAlignment(cluster2centerAttribute, StyleConstants.ALIGN_CENTER);
		cluster2Doc.setParagraphAttributes(
			0, cluster2Doc.getLength(), cluster2centerAttribute, false);
		this.cluster2NameTextPane.setPreferredSize(new Dimension(200,25));
		c.gridx = 2; c.gridy = 3; c.weightx = 0.0;
		this.add(this.cluster2NameTextPane, c);

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
				this.viewer.tableModel.refresh();
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

		this.cluster1NameTextPane.setText(data.c1.name);
		this.cluster2NameTextPane.setText(data.c2.name);
		this.simValueLabel.setText(data.cellValue.toString());

		int archSize = this.clusterer.getArchitecture().size();
		this.archSizeTextPane.setText("Architecture size: " + archSize);

		if (data.c1.getDocTopicItem().getTopTopicItem().topicNum ==
				data.c2.getDocTopicItem().getTopTopicItem().topicNum)
			this.highestDTIMatchLabel.setText("Top topic items match.");
		else
			this.highestDTIMatchLabel.setText("Top topic items are different.");
	}
	//endregion
}
