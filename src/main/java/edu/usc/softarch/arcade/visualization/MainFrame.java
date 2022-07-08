package edu.usc.softarch.arcade.visualization;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import edu.usc.softarch.arcade.clustering.Clusterer;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.arcade.visualization.clustering.ClustererController;
import edu.usc.softarch.arcade.visualization.clustering.ClustererInitializer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class MainFrame extends JFrame implements ActionListener {
	//region PUBLIC INTERFACE
	public static void main(String[] args) {
		LafManager.install(new DarculaTheme());
		MainFrame window = new MainFrame();
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// Puts the window in the center of the screen
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	//endregion

	//region ATTRIBUTES
	// Swing attributes
	private final ClustererInitializer initializerPanel;
	private ClustererController controllerPanel;

	// Action attributes
	private static final String saveMenuItemAction = "saveConfig";
	private static final String loadMenuItemAction = "loadConfig";
	//endregion

	//region CONSTRUCTORS
	public MainFrame() {
		this.initializerPanel = new ClustererInitializer(this);
		this.setContentPane(this.initializerPanel);

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);

		JMenuItem saveMenuItem = new JMenuItem("Save Configuration");
		saveMenuItem.setActionCommand(saveMenuItemAction);
		saveMenuItem.addActionListener(this);
		menu.add(saveMenuItem);
		JMenuItem loadMenuItem = new JMenuItem("Load Configuration");
		loadMenuItem.setActionCommand(loadMenuItemAction);
		loadMenuItem.addActionListener(this);
		menu.add(loadMenuItem);

		this.setJMenuBar(menuBar);
		this.pack();
	}
	//endregion

	//region PROCESSING
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case ClustererInitializer.loadClustererAction:
				try {
					loadClusterer();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				} catch (UnmatchingDocTopicItemsException ex) {
					throw new RuntimeException(ex);
				} catch (DistributionSizeMismatchException ex) {
					throw new RuntimeException(ex);
				}
				this.setContentPane(this.controllerPanel);
				this.pack();
				break;
			case saveMenuItemAction:
				try {
					this.saveConfiguration();
				} catch (FileNotFoundException ex) {
					throw new RuntimeException(ex);
				}
				break;
			case loadMenuItemAction:
				try {
					this.loadConfiguration();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				break;
		}
	}

	private void loadClusterer() throws IOException,
			UnmatchingDocTopicItemsException, DistributionSizeMismatchException {
		Clusterer.ClusteringAlgoArguments arguments =
			new Clusterer.ClusteringAlgoArguments(loadArguments());
		Clusterer clusterer = new Clusterer(arguments.serialCrit, arguments.arch,
			arguments.algorithm, arguments.simMeasure);
		this.controllerPanel = new ClustererController(clusterer);
	}

	private String[] loadArguments() {
		String[] args = new String[12];

		try {
			args[0] = this.initializerPanel.algorithmComboBox
				.comboBox.getSelectedItem().toString();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[1] = this.initializerPanel.languageButtonGroup
				.buttonGroup.getSelection().getActionCommand();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[2] = this.initializerPanel.vectorsFileSelector
				.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[3] = this.initializerPanel.stoppingCriterionValuedComboBox
				.comboBox.getSelectedItem().toString();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[4] = this.initializerPanel
				.stoppingCriterionValuedComboBox.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[5] = this.initializerPanel.simMeasureComboBox
				.comboBox.getSelectedItem().toString();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[6] = this.initializerPanel.serializationCriterionValuedComboBox
				.comboBox.getSelectedItem().toString();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[7] = this.initializerPanel
				.serializationCriterionValuedComboBox.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[8] =
				this.initializerPanel.projectNameTextField.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[9] = this.initializerPanel.outputPathSelector.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[10] =
				this.initializerPanel.packagePrefixTextField.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		try {
			args[11] =
				this.initializerPanel.artifactsPathSelector.textField.getText();
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
			//TODO dialogbox error
		}

		//TODO loading bar

		//TODO Set up for WCA and Limbo
		return args;
	}

	private void saveConfiguration() throws FileNotFoundException {
		String[] args = loadArguments();
		JFileChooser saveDialog = new JFileChooser();

		int returnVal = saveDialog.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = saveDialog.getSelectedFile();
			try (PrintWriter writer = new PrintWriter(file)) {
				writer.write(String.join(",", args));
			}
		}
	}

	private void loadConfiguration() throws IOException {
		JFileChooser loadDialog = new JFileChooser();

		int returnVal = loadDialog.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = loadDialog.getSelectedFile();
			String content = Files.readString(file.toPath());
			String[] args = content.split(",");
			this.initializerPanel.loadConfiguration(args);
		}
	}
	//endregion
}
