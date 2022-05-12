package edu.usc.softarch.arcade.clustering.criteria;

public class StepCountSerializationCriterion extends SerializationCriterion {
	private int stepCount;
	private final int stepThreshold;

	public StepCountSerializationCriterion(int stepThreshold) {
		this.stepThreshold = stepThreshold;
		this.stepCount = 0;
	}

	@Override
	public boolean shouldSerialize() {
		if (++this.stepCount == this.stepThreshold) {
			this.stepCount = 0;
			return true;
		}

		return false;
	}
}
