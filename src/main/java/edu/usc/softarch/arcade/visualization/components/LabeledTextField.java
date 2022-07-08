package edu.usc.softarch.arcade.visualization.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

public class LabeledTextField extends JPanel {
	//region ATTRIBUTES
	public final JTextField textField;
	//endregion

	//region CONSTRUCTORS
	public LabeledTextField(ActionListener listener, String label,
			String action) {
		this(listener, label, action, false);
	}

	public LabeledTextField(ActionListener listener, String label,
			String action, boolean isTopLabeled) {
		this.textField = new JTextField(20);
		this.textField.setActionCommand(action);
		this.textField.addActionListener(listener);
		this.textField.setPreferredSize(new Dimension(50, 30));
		//this.textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.textField.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		JLabel fieldLabel = new JLabel(label);
		fieldLabel.setLabelFor(this.textField);
		fieldLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Global constraints
		c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1; c.weighty = 1.0;
		// Label constraints
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		this.add(fieldLabel, c);
		// TextField constraints
		if (isTopLabeled) {
			c.gridx = 0; c.gridy = 1; c.weightx = 1.0;
		} else {
			c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		}
		this.add(this.textField, c);
	}
	//endregion
}
