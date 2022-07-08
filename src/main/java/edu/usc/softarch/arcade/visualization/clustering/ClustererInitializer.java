package edu.usc.softarch.arcade.visualization.clustering;

import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.visualization.components.DefaultButton;
import edu.usc.softarch.arcade.visualization.components.FileSelector;
import edu.usc.softarch.arcade.visualization.components.LabeledComboBox;
import edu.usc.softarch.arcade.visualization.components.LabeledTextField;
import edu.usc.softarch.arcade.visualization.components.TwoItemButtonGroup;
import edu.usc.softarch.arcade.visualization.components.ValuedComboBox;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClustererInitializer extends JPanel implements ActionListener {
	//region ATTRIBUTES
	// Swing Components
	public final LabeledComboBox<ClusteringAlgorithmType> algorithmComboBox;
	public final LabeledComboBox<SimMeasure.SimMeasureType> simMeasureComboBox;
	public final LabeledTextField projectNameTextField;
	public final LabeledTextField packagePrefixTextField;
	public final TwoItemButtonGroup languageButtonGroup;
	public final
		ValuedComboBox<StoppingCriterion.Criterion> stoppingCriterionValuedComboBox;
	public final ValuedComboBox<SerializationCriterion.Criterion>
		serializationCriterionValuedComboBox;
	public final FileSelector vectorsFileSelector;
	public final FileSelector artifactsPathSelector;
	public final FileSelector outputPathSelector;

	// Actions
	private static final String algorithmComboBoxAction = "algorithm";
	private static final String languageButtonGroup1Action = "C";
	private static final String languageButtonGroup2Action = "Java";
	private static final String vectorsSelectorAction = "vectorsPath";
	private static final String outputSelectorAction = "outputPath";
	private static final String artifactsSelectorAction = "artifactsPath";
	public static final String loadClustererAction = "loadClusterer";
	//endregion

	//region CONSTRUCTORS
	public ClustererInitializer(ActionListener listener) {
		// Set layout
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		// Algorithm selector
		this.algorithmComboBox = new LabeledComboBox<>(this, "Algorithm:",
			algorithmComboBoxAction, ClusteringAlgorithmType.values());
		this.algorithmComboBox.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 0; c.gridy = 0; c.weighty = 0.15; c.gridheight = 1;
		this.add(this.algorithmComboBox, c);

		// Project name field
		this.projectNameTextField = new LabeledTextField(this,
			"Project Name:", null);
		this.projectNameTextField.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 1; c.gridy = 0; c.weighty = 0.15; c.gridheight = 1;
		this.add(this.projectNameTextField, c);

		// Sim measure combo box
		this.simMeasureComboBox = new LabeledComboBox<>(this,
			"Similarity Measure:", null,
			SimMeasure.SimMeasureType.values());
		this.simMeasureComboBox.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.simMeasureComboBox.comboBox.setEnabled(false);
		c.gridx = 0; c.gridy = 1; c.weighty = 0.15; c.gridheight = 1;
		this.add(this.simMeasureComboBox, c);

		// Vectors file selector
		this.vectorsFileSelector = new FileSelector(this,
			"Vectors File:", vectorsSelectorAction, false);
		this.vectorsFileSelector.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 1; c.gridy = 1; c.weighty = 0.15; c.gridheight = 1;
		this.add(this.vectorsFileSelector, c);

		// Language button group
		this.languageButtonGroup =
			new TwoItemButtonGroup(this, "Project Language:",
				languageButtonGroup1Action, languageButtonGroup2Action);
		this.languageButtonGroup.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 0; c.gridy = 2; c.weighty = 0.14; c.gridheight = 1;
		this.add(this.languageButtonGroup, c);

		// Artifacts directory selector
		this.artifactsPathSelector = new FileSelector(this,
			"Artifacts Directory:", artifactsSelectorAction, true);
		this.artifactsPathSelector.button.setEnabled(false);
		this.artifactsPathSelector.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 1; c.gridy = 2; c.weighty = 0.14; c.gridheight = 1;
		this.add(this.artifactsPathSelector, c);

		// Stopping criterion panel
		this.stoppingCriterionValuedComboBox = new ValuedComboBox<>(
			this, "Stopping Criterion:", null,
			null, StoppingCriterion.Criterion.values());
		this.stoppingCriterionValuedComboBox.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 0; c.gridy = 3; c.weighty = 0.28; c.gridheight = 2;
		this.add(this.stoppingCriterionValuedComboBox, c);

		// Output directory selector
		this.outputPathSelector = new FileSelector(this,
			"Output Directory:", outputSelectorAction, true);
		this.outputPathSelector.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 1; c.gridy = 3; c.weighty = 0.14; c.gridheight = 1;
		this.add(this.outputPathSelector, c);

		// Package prefix text field
		this.packagePrefixTextField = new LabeledTextField(this,
			"Package Prefix:", null, true);
		this.packagePrefixTextField.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.packagePrefixTextField.textField.setEnabled(false);
		c.gridx = 1; c.gridy = 4; c.weighty = 0.28; c.gridheight = 2;
		this.add(this.packagePrefixTextField, c);

		// Serialization criterion panel
		this.serializationCriterionValuedComboBox = new ValuedComboBox<>(
			this, "Serialization Criterion:", null,
			null, SerializationCriterion.Criterion.values());
		this.serializationCriterionValuedComboBox.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 0, 5));
		c.gridx = 0; c.gridy = 4; c.weighty = 0.28; c.gridheight = 2;
		this.add(this.serializationCriterionValuedComboBox, c);

		// Load button
		DefaultButton loadButton = new DefaultButton(
			listener, "Load Clusterer", loadClustererAction);
		loadButton.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createRaisedSoftBevelBorder()));
		c.gridx = 1; c.gridy = 6; c.weighty = 0.14; c.gridheight = 1;
		this.add(loadButton, c);
	}
	//endregion

	//region PROCESSING
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == null) return;

		switch (e.getActionCommand()) {
			case algorithmComboBoxAction:
				processAlgorithmComboBoxAction();
				break;
			case languageButtonGroup1Action:
				this.packagePrefixTextField.textField.setEnabled(false);
				break;
			case languageButtonGroup2Action:
				this.packagePrefixTextField.textField.setEnabled(true);
				break;
			case vectorsSelectorAction:
				vectorsFileSelector.chooseFile();
				break;
			case outputSelectorAction:
				outputPathSelector.chooseFile();
				break;
			case artifactsSelectorAction:
				artifactsPathSelector.chooseFile();
				break;
		}
	}

	private void processAlgorithmComboBoxAction() {
		this.simMeasureComboBox.comboBox.removeAllItems();
		switch ((ClusteringAlgorithmType)
				this.algorithmComboBox.comboBox.getSelectedItem()) {
			case ARC:
				this.simMeasureComboBox.comboBox.addItem(SimMeasure.SimMeasureType.JS);
				this.simMeasureComboBox.comboBox.addItem(SimMeasure.SimMeasureType.WJS);
				this.artifactsPathSelector.button.setEnabled(true);
				break;
			case WCA:
				this.simMeasureComboBox.comboBox.addItem(SimMeasure.SimMeasureType.UEM);
				this.simMeasureComboBox.comboBox
					.addItem(SimMeasure.SimMeasureType.UEMNM);
				this.artifactsPathSelector.button.setEnabled(false);
				break;
			case LIMBO:
				this.simMeasureComboBox.comboBox.addItem(SimMeasure.SimMeasureType.IL);
				this.artifactsPathSelector.button.setEnabled(false);
				break;
		}
		this.simMeasureComboBox.comboBox.setEnabled(true);
	}

	public void loadConfiguration(String[] args) {
		this.algorithmComboBox.comboBox
			.setSelectedItem(ClusteringAlgorithmType.valueOf(args[0]));

		if (args[1].equalsIgnoreCase("c"))
			this.languageButtonGroup.button1.setSelected(true);
		if (args[1].equalsIgnoreCase("java"))
			this.languageButtonGroup.button2.setSelected(true);

		this.vectorsFileSelector.textField.setText(args[2]);

		this.stoppingCriterionValuedComboBox.comboBox
			.setSelectedItem(StoppingCriterion.Criterion.valueOf(args[3]));

		this.stoppingCriterionValuedComboBox.textField.setText(args[4]);

		this.simMeasureComboBox.comboBox
			.setSelectedItem(SimMeasure.SimMeasureType.valueOf(args[5]));

		this.serializationCriterionValuedComboBox.comboBox
			.setSelectedItem(SerializationCriterion.Criterion.valueOf(args[6]));

		this.serializationCriterionValuedComboBox.textField.setText(args[7]);

		this.projectNameTextField.textField.setText(args[8]);

		this.outputPathSelector.textField.setText(args[9]);

		this.packagePrefixTextField.textField.setText(args[10]);

		this.artifactsPathSelector.textField.setText(args[11]);
	}
	//endregion
}
