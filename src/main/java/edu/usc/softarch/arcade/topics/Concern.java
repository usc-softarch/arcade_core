package edu.usc.softarch.arcade.topics;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Concern extends ArrayList<String> {
	//region CONSTRUCTORS
	public Concern(Map<Integer, List<String>> wordBags,
			Map<Integer, TopicItem> topics) {
		super();
		for (TopicItem topicItem : topics.values()) {
			int index = topicItem.topicNum;
			int proportion = (int) (topicItem.proportion * 100);
			this.addAll(wordBags.get(index).subList(0, proportion));
		}
	}

	public Concern(Concern concern) {
		super(concern);	}

	public Concern(List<String> concern) {
		super(concern); }
	//endregion

	//region SERIALIZATION
	public void serialize(JsonGenerator generator) throws IOException {
		generator.writeArray(this.toArray(new String[0]), 0, this.size()); }
	//endregion
}
