package edu.usc.softarch.arcade.visualization.clustering;

import edu.usc.softarch.arcade.visualization.ArchitectureTableModel;
import edu.usc.softarch.arcade.visualization.components.DefaultButton;
import edu.usc.softarch.arcade.visualization.components.LabeledComboBox;
import edu.usc.softarch.arcade.visualization.components.LabeledTextField;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Map;

public abstract class ArchitectureViewer extends JPanel
		implements ActionListener, ListSelectionListener {
	//region ATTRIBUTES
	// Real Attributes
	protected final String projectName;
	protected final ArchitectureTableModel tableModel;

	// Swing Components
	private final LabeledTextField indexTextField;
	private final LabeledComboBox<Integer> featureComboBox;
	private final JTable architectureTable;
	private final JLabel topFeatureLabel;
	private final JLabel topFeatureProportionLabel;
	private final TableRowSorter<TableModel> architectureFilter;

	// Actions
	private static final String indexTextFieldAction = "indexTextField";
	private static final String featureComboAction = "featureComboBox";
	private static final String resetButtonAction = "resetButtonAction";
	//endregion

	//region CONSTRUCTORS
	protected ArchitectureViewer(String projectName) {
		// Real constructor
		this.projectName = projectName;

		// Set layout
		this.setLayout(new GridBagLayout());
		this.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0.0;

		// Architecture index filter
		this.indexTextField = new LabeledTextField(
			this, getIndexName(), indexTextFieldAction);
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		this.add(this.indexTextField, c);

		// Feature filter elements
		Integer[] featureList = getFeatureIndex();
		this.featureComboBox = new LabeledComboBox<>(this,
			getFeatureName(), featureComboAction, featureList);
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		this.add(this.featureComboBox, c);

		// Reset button
		DefaultButton resetButton =
			new DefaultButton(this, "Reset", resetButtonAction);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		this.add(resetButton, c);

		// Top TopicItem finder
		JLabel featureRankLabel = new JLabel(getFeatureRankText());
		featureRankLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.topFeatureLabel = new JLabel();
		this.topFeatureLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 20));
		this.topFeatureProportionLabel = new JLabel();
		this.topFeatureProportionLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel topTopicPanel = new JPanel();
		topTopicPanel.setLayout(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		topTopicPanel.add(featureRankLabel, c);
		c.gridx = 1; c.gridy = 0; c.weightx = 0.0;
		topTopicPanel.add(this.topFeatureLabel);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		topTopicPanel.add(this.topFeatureProportionLabel);
		c.gridx = 0; c.gridy = 1; c.weightx = 0.0;
		this.add(topTopicPanel, c);

		// Architecture table elements
		this.tableModel = buildArchitectureTable();
		this.architectureTable = new JTable(this.tableModel);
		this.architectureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.architectureTable.getColumnModel()
			.getColumn(0).setPreferredWidth(400);
		for (int i = 1; i < this.architectureTable.getColumnCount(); i++)
			this.architectureTable.getColumnModel().getColumn(i).setPreferredWidth(60);
		this.architectureTable.getSelectionModel().addListSelectionListener(this);

		this.architectureFilter = new TableRowSorter<>(this.architectureTable.getModel());
		Comparator<Double> proportionComparator = Double::compareTo;
		for (int i = 1; i < this.architectureTable.getColumnCount(); i++)
			this.architectureFilter.setComparator(i, proportionComparator);
		this.architectureTable.setRowSorter(architectureFilter);

		JScrollPane architectureScrollPane = new JScrollPane(this.architectureTable);
		architectureScrollPane.setHorizontalScrollBarPolicy(
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		architectureScrollPane.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		this.add(architectureScrollPane, c);
	}
	//endregion

	//region PROCESSING
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case indexTextFieldAction:
				this.architectureFilter.setRowFilter(
					RowFilter.regexFilter(this.indexTextField.textField.getText()));
				break;
			case featureComboAction:
				for (int i = 1; i < this.architectureTable.getColumnCount(); i++) {
					if (!featureComboBox.comboBox.getSelectedItem().equals(i)) {
						architectureTable.getColumn(String.valueOf(i)).setMinWidth(0);
						architectureTable.getColumn(String.valueOf(i)).setMaxWidth(0);
						architectureTable.getColumn(String.valueOf(i)).setWidth(0);
					} else {
						architectureTable.getColumn(String.valueOf(i)).setMaxWidth(60);
						architectureTable.getColumn(String.valueOf(i)).setMinWidth(60);
						architectureTable.getColumn(String.valueOf(i)).setWidth(60);
					}
				}
				break;
			case resetButtonAction:
				this.indexTextField.textField.setText("");
				this.architectureFilter.setRowFilter(
					RowFilter.regexFilter(this.indexTextField.textField.getText()));
				for (int i = 1; i < 101; i++) {
					architectureTable.getColumn(String.valueOf(i)).setMaxWidth(60);
					architectureTable.getColumn(String.valueOf(i)).setMinWidth(60);
					architectureTable.getColumn(String.valueOf(i)).setWidth(60);
				}
				architectureTable.repaint();
				this.topFeatureLabel.setText("");
				this.topFeatureProportionLabel.setText("");
				break;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int row = this.architectureTable
			.convertRowIndexToModel(this.architectureTable.getSelectedRow());
		Map.Entry<Integer, Double> highestValue =
			this.tableModel.getHighestValue(row);

		this.topFeatureLabel.setText(String.valueOf(highestValue.getKey()));
		this.topFeatureProportionLabel.setText(
			String.valueOf(highestValue.getValue()));
	}

	protected abstract ArchitectureTableModel buildArchitectureTable();
	protected abstract Integer[] getFeatureIndex();
	protected abstract String getIndexName();
	protected abstract String getFeatureName();
	protected abstract String getFeatureRankText();
	//endregion
}
