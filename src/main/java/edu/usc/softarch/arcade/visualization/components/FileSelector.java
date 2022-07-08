package edu.usc.softarch.arcade.visualization.components;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.File;

public class FileSelector extends JPanel {
	//region ATTRIBUTES
	private File selectedFile;

	public final JTextField textField;
	public final JFileChooser fileChooser;
	public final DefaultButton button;
	//endregion

	//region CONSTRUCTORS
	public FileSelector(ActionListener listener,
			String label, String chooserAction, boolean isDir) {
		// Create the label
		JLabel selectorLabel = new JLabel(label);
		selectorLabel.setPreferredSize(new Dimension(130, 40));
		selectorLabel.setBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create text field
		this.textField = new JTextField(20);
		this.textField.setEnabled(false);
		this.textField.setPreferredSize(new Dimension(50, 30));
		this.textField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0,0,0,5),
			BorderFactory.createRaisedSoftBevelBorder()));

		// Create selector button
		this.button = new DefaultButton(listener, "Select", chooserAction);
		button.setBorder(BorderFactory.createRaisedBevelBorder());

		// Create file chooser
		this.fileChooser = new JFileChooser();
		if (isDir)
			this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
			this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Set up panel
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Global constraints
		c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth = 1; c.weighty = 1.0;
		// Label constraints
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		this.add(selectorLabel, c);
		// Text field constraints
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		this.add(this.textField, c);
		// Button constraints
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		this.add(button, c);
	}
	//endregion

	//region ACCESSORS
	public File getSelectedFile() { return this.selectedFile; }
	//endregion

	//region PROCESSING
	public void chooseFile() {
		int returnVal = this.fileChooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.selectedFile = this.fileChooser.getSelectedFile();
			this.textField.setText(this.selectedFile.getAbsolutePath());
		}
	}
	//endregion
}
