package edu.usc.softarch.arcade.visualization;

import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DocTopicsViewer extends JPanel implements ActionListener, ListSelectionListener {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		String projectVersion = args[0];
		String docTopicsPath = args[1];

		DocTopics.deserialize(docTopicsPath);

		DocTopicsViewer viewerPanel = new DocTopicsViewer(projectVersion);
		JFrame window = new JFrame("DocTopics Viewer: " + projectVersion);
		window.setContentPane(viewerPanel);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.pack();
		// Puts the window in the center of the screen
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	//endregion

	//region ATTRIBUTES
	// Real Attributes
	private final String projectName;

	// Swing Components
	private final JTextField sourceTextField;
	private final JComboBox<Integer> topicNumberComboBox;
	private final JTable docTopicsTable;
	private final JLabel topTopicNumberLabel;
	private final JLabel topTopicProportionLabel;
	private final TableRowSorter<TableModel> docTopicsFilter;

	// Actions
	private static final String sourceTextFieldAction = "sourceTextField";
	private static final String topicNumberComboAction = "topicNumberComboBox";
	private static final String resetButtonAction = "resetButtonAction";
	//endregion

	//region CONSTRUCTORS
	public DocTopicsViewer(String projectName) {
		// Real constructor
		this.projectName = projectName;

		// Set layout
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0.0;

		// DTI source filter elements
		this.sourceTextField = new JTextField(20);
		this.sourceTextField.setActionCommand(sourceTextFieldAction);
		this.sourceTextField.addActionListener(this);
		this.sourceTextField.setPreferredSize(new Dimension(50, 30));
		this.sourceTextField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel sourceLabel = new JLabel("Document name:");
		sourceLabel.setLabelFor(this.sourceTextField);
		sourceLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		sourcePanel.add(sourceLabel, c);
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		sourcePanel.add(this.sourceTextField, c);
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		this.add(sourcePanel, c);

		// DTI topic number filter elements
		Integer[] topicNumberList = new Integer[100];
		for (int i = 1; i < 101; i++)
			topicNumberList[i - 1] = i;
		this.topicNumberComboBox = new JComboBox<>(topicNumberList);
		this.topicNumberComboBox.setActionCommand(topicNumberComboAction);
		this.topicNumberComboBox.addActionListener(this);
		this.topicNumberComboBox.setPreferredSize(new Dimension(50, 40));
		this.topicNumberComboBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel topicNumberLabel = new JLabel(("Topic number:"));
		topicNumberLabel.setLabelFor(topicNumberComboBox);
		topicNumberLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel topicNumberPanel = new JPanel();
		topicNumberPanel.setLayout(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		topicNumberPanel.add(topicNumberLabel, c);
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		topicNumberPanel.add(this.topicNumberComboBox, c);
		topicNumberPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
		this.add(topicNumberPanel, c);

		// Reset button
		JButton resetButton = new JButton("reset");
		resetButton.setVerticalTextPosition(SwingConstants.CENTER);
		resetButton.setHorizontalTextPosition(SwingConstants.LEADING);
		resetButton.setActionCommand(resetButtonAction);
		resetButton.addActionListener(this);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		this.add(resetButton, c);

		// Top TopicItem finder
		JLabel topTopicLabel = new JLabel("Highest Topic Item in selected document:");
		topTopicLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.topTopicNumberLabel = new JLabel();
		this.topTopicNumberLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));
		this.topTopicProportionLabel = new JLabel();
		this.topTopicProportionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel topTopicPanel = new JPanel();
		topTopicPanel.setLayout(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		topTopicPanel.add(topTopicLabel, c);
		c.gridx = 1; c.gridy = 0; c.weightx = 0.0;
		topTopicPanel.add(this.topTopicNumberLabel);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		topTopicPanel.add(this.topTopicProportionLabel);
		c.gridx = 0; c.gridy = 1; c.weightx = 0.0;
		this.add(topTopicPanel, c);

		// DocTopics table elements
		Object[] columnNames = new Object[101];
		columnNames[0] = "Source";
		for (int i = 1; i < 101; i++)
			columnNames[i] = i;
		this.docTopicsTable = new JTable(buildDocTopicsTable(), columnNames);
		this.docTopicsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.docTopicsTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		for (int i = 1; i < 101; i++)
			this.docTopicsTable.getColumnModel().getColumn(i).setPreferredWidth(60);
		this.docTopicsTable.getSelectionModel().addListSelectionListener(this);

		this.docTopicsFilter = new TableRowSorter<>(this.docTopicsTable.getModel());
		Comparator<Double> proportionComparator = Double::compareTo;
		for (int i = 1; i < 101; i++)
			this.docTopicsFilter.setComparator(i, proportionComparator);
		this.docTopicsTable.setRowSorter(docTopicsFilter);

		JScrollPane docTopicsScrollPane = new JScrollPane(docTopicsTable);
		docTopicsScrollPane.setHorizontalScrollBarPolicy(
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		docTopicsScrollPane.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		this.add(docTopicsScrollPane, c);
	}
	//endregion

	//region PROCESSING
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case sourceTextFieldAction:
				this.docTopicsFilter.setRowFilter(
					RowFilter.regexFilter(this.sourceTextField.getText()));
				break;
			case topicNumberComboAction:
				for (int i = 1; i < 101; i++) {
					if (!topicNumberComboBox.getSelectedItem().equals(i)) {
						docTopicsTable.getColumn(String.valueOf(i)).setMinWidth(0);
						docTopicsTable.getColumn(String.valueOf(i)).setMaxWidth(0);
						docTopicsTable.getColumn(String.valueOf(i)).setWidth(0);
					}
				}
				break;
			case resetButtonAction:
				this.sourceTextField.setText("");
				this.docTopicsFilter.setRowFilter(
					RowFilter.regexFilter(this.sourceTextField.getText()));
				for (int i = 1; i < 101; i++) {
					docTopicsTable.getColumn(String.valueOf(i)).setMaxWidth(60);
					docTopicsTable.getColumn(String.valueOf(i)).setMinWidth(60);
					docTopicsTable.getColumn(String.valueOf(i)).setWidth(60);
				}
				docTopicsTable.repaint();
				this.topTopicNumberLabel.setText("");
				this.topTopicProportionLabel.setText("");
				break;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int row = this.docTopicsTable.getSelectedRow();
		double[] proportions = new double[100];

		for (int i = 1; i < 101; i++)
			proportions[i - 1] = (Double) this.docTopicsTable.getValueAt(row, i);

		double maxProportion = 0.0;
		int topicNum = 0;

		for (int i = 1; i < 101; i++) {
			if (proportions[i - 1] > maxProportion) {
				maxProportion = proportions[i - 1];
				topicNum = i;
			}
		}

		this.topTopicNumberLabel.setText(String.valueOf(topicNum));
		this.topTopicProportionLabel.setText(String.valueOf(maxProportion));
	}

	private Object[][] buildDocTopicsTable() {
		List<DocTopicItem> docTopicItems =
			new ArrayList<>(DocTopics.getSingleton(this.projectName).getCopy());
		Object[][] result = new Object[docTopicItems.size()][101];

		for (int i = 0; i < docTopicItems.size(); i++) {
			result[i][0] = docTopicItems.get(i).getSource();
			for (TopicItem topic : docTopicItems.get(i).getTopics())
				result[i][topic.topicNum + 1] = topic.getProportion();
		}

		return result;
	}
	//endregion
}
