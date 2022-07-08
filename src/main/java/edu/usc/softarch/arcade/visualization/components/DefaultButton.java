package edu.usc.softarch.arcade.visualization.components;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;

public class DefaultButton extends JButton {
	//region CONSTRUCTORS
	public DefaultButton(ActionListener listener, String label, String action) {
		super(label);
		this.setVerticalTextPosition(SwingConstants.CENTER);
		this.setHorizontalTextPosition(SwingConstants.LEADING);
		this.setActionCommand(action);
		this.addActionListener(listener);
	}
	//endregion
}
