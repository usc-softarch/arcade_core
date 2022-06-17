package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.facts.issues.IssueRecord;
import edu.usc.softarch.util.Tree;

import java.util.Collection;

public class DecisionAnalyzer {
	//region ATTRIBUTES
	private final Collection<IssueRecord> issues;
	private final Tree<String> versions;
	//endregion

	//region CONSTRUCTORS
	public DecisionAnalyzer(Collection<IssueRecord> issues,
			Tree<String> versions) {
		this.issues = issues;
		this.versions = versions;
	}
	//endregion

	//region PROCESSING
	
	//endregion
}
