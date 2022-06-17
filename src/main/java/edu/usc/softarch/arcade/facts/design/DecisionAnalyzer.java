package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.facts.issues.IssueRecord;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class DecisionAnalyzer {
	//region ATTRIBUTES
	private final Collection<IssueRecord> issues;
	//endregion

	//region CONSTRUCTORS
	public DecisionAnalyzer(Collection<IssueRecord> issues) {
		this.issues = issues;	}
	//endregion

	//region PROCESSING
	public Collection<Decision> getDecisionList(String version,
			Collection<Change> changes) {
		Collection<Decision> result = new ArrayList<>();

		for (IssueRecord issue : this.issues) {
			if (issue.getFixVersions().contains(version)) {
				Decision decision = getDecision(version, issue, changes);
				if (!decision.isEmpty()) result.add(decision);
			}
		}

		return result;
	}

	private Decision getDecision(String version, IssueRecord issue,
			Collection<Change> changes) {
		Collection<String> addedElements = new HashSet<>();
		Collection<String> removedElements = new HashSet<>();

		for (Change change : changes) {
			EnhancedSet<String> addedFiles = new EnhancedHashSet<>(
				issue.getFileChanges().stream()
					.map(Map.Entry::getValue).collect(Collectors.toList()));
			EnhancedSet<String> addedChanges =
				new EnhancedHashSet<>(change.getAddedElements());
			addedElements.addAll(addedFiles.intersection(addedChanges));

			EnhancedSet<String> removedFiles = new EnhancedHashSet<>(
				issue.getFileChanges().stream()
					.map(Map.Entry::getKey).collect(Collectors.toList()));
			EnhancedSet<String> removedChanges =
				new EnhancedHashSet<>(change.getRemovedElements());
			removedElements.addAll(removedFiles.intersection(removedChanges));
		}

		return new Decision(issue.description, issue.id, version,
			addedElements, removedElements);
	}
	//endregion
}
