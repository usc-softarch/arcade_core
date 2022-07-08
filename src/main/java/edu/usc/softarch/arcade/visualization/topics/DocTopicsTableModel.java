package edu.usc.softarch.arcade.visualization.topics;

import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.visualization.ArchitectureTableModel;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DocTopicsTableModel extends ArchitectureTableModel {
	//region ATTRIBUTES
	private final String projectName;
	private final List<DocTopicItem> docTopicsCopy;
	//endregion

	//region CONSTRUCTORS
	public DocTopicsTableModel(String projectName) {
		this.projectName = projectName;
		this.docTopicsCopy =
			new ArrayList<>(DocTopics.getSingleton(this.projectName).getCopy());
	}
	//endregion

	//region PROCESSING
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Document";
		else
			return String.valueOf(column);
	}

	@Override
	public int getRowCount() {
		return DocTopics.getSingleton(this.projectName).getCopy().size(); }

	@Override
	public int getColumnCount() {
		return 101; }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return this.docTopicsCopy.get(rowIndex).getSource();

		return this.docTopicsCopy.get(rowIndex)
			.getTopic(columnIndex - 1).getProportion();
	}

	@Override
	public Map.Entry<Integer, Double> getHighestValue(int row) {
		List<DocTopicItem> docTopicsCopy =
			new ArrayList<>(DocTopics.getSingleton(this.projectName).getCopy());

		DocTopicItem toCheck = docTopicsCopy.get(row);
		Collection<TopicItem> topics = toCheck.getTopics();

		int topicNum = -1;
		double proportion = 0.0;

		for (TopicItem topic : topics) {
			double tempProportion = topic.getProportion();
			if (tempProportion > proportion) {
				topicNum = topic.topicNum;
				proportion = tempProportion;
			}
		}

		return new AbstractMap.SimpleEntry<>(topicNum + 1, proportion);
	}
	//endregion
}
