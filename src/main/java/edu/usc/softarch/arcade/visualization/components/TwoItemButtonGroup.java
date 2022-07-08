package edu.usc.softarch.arcade.visualization.components;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

public class TwoItemButtonGroup extends JPanel {
	//region ATTRIBUTES
	public final JRadioButton button1;
	public final JRadioButton button2;
	public final ButtonGroup buttonGroup;
	//endregion

	//region CONSTRUCTORS
	public TwoItemButtonGroup(ActionListener listener, String label,
			String action1, String action2) {
		// Create the label
		JLabel buttonGroupLabel = new JLabel(label);
		buttonGroupLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create first radio button
		this.button1 = new JRadioButton(action1);
		this.button1.setActionCommand(action1);
		this.button1.addActionListener(listener);

		// Create second radio button
		this.button2 = new JRadioButton(action2);
		this.button2.setActionCommand(action2);
		this.button2.addActionListener(listener);

		// Create button group
		this.buttonGroup = new ButtonGroup();
		buttonGroup.add(this.button1);
		buttonGroup.add(this.button2);

		// Set up panel
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Global constraints
		c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1; c.weighty = 1.0;
		// Label constraints
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		this.add(buttonGroupLabel, c);
		// Button1 constraints
		c.gridx = 1; c.gridy = 0; c.weightx = 0.5;
		this.add(this.button1, c);
		// Button2 constraints
		c.gridx = 2; c.gridy = 0; c.weightx = 0.5;
		this.add(this.button2, c);
	}
	//endregion
}
