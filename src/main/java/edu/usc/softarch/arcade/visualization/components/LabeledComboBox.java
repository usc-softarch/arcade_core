package edu.usc.softarch.arcade.visualization.components;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

public class LabeledComboBox<T> extends JPanel {
	//region ATTRIBUTES
	public final JComboBox<T> comboBox;
	//endregion

	//region CONSTRUCTORS
	public LabeledComboBox(ActionListener listener, String label,
			String action, T[] values) {
		this.comboBox = new JComboBox<>(values);
		this.comboBox.setSelectedIndex(-1);
		this.comboBox.setActionCommand(action);
		this.comboBox.addActionListener(listener);
		this.comboBox.setPreferredSize(new Dimension(50, 25));
		this.comboBox.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel comboBoxLabel = new JLabel(label);
		comboBoxLabel.setLabelFor(this.comboBox);
		comboBoxLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Global constraints
		c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1; c.weighty = 1.0;
		// Label constraints
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		this.add(comboBoxLabel, c);
		// ComboBox constraints
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		this.add(this.comboBox, c);
	}
	//endregion
}
