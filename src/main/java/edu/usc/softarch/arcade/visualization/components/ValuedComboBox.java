package edu.usc.softarch.arcade.visualization.components;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

public class ValuedComboBox<T> extends JPanel {
	//region ATTRIBUTES
	public final JComboBox<T> comboBox;
	public final JTextField textField;
	//endregion

	//region CONSTRUCTORS
	public ValuedComboBox(ActionListener listener, String label,
			String comboAction, String textAction, T[] values) {
		// Create the label
		JLabel valuedComboBoxLabel = new JLabel(label);
		valuedComboBoxLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create combo box
		this.comboBox = new JComboBox<>(values);
		this.comboBox.setActionCommand(comboAction);
		this.comboBox.addActionListener(listener);
		this.comboBox.setPreferredSize(new Dimension(150, 25));
		this.comboBox.setBorder(BorderFactory.createRaisedBevelBorder());

		// Create text field
		this.textField = new JTextField(20);
		this.textField.setActionCommand(textAction);
		this.textField.addActionListener(listener);
		this.textField.setPreferredSize(new Dimension(50, 30));
		this.textField.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Set up panel
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Global constraints
		c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1; c.weighty = 1.0;
		// Label constraints
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		this.add(valuedComboBoxLabel, c);
		// Combo box constraints
		c.gridx = 0; c.gridy = 1; c.weightx = 0.0;
		this.add(this.comboBox, c);
		// Text field constraints
		c.gridx = 1; c.gridy = 1; c.weightx = 1.0;
		this.add(this.textField, c);
	}
	//endregion
}
