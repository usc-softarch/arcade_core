package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.facts.Change;
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
	public Collection<CodeElementDecision> getDecisionList(String version,
			Collection<Change<String>> changes) {
		Collection<CodeElementDecision> result = new ArrayList<>();

		for (IssueRecord issue : this.issues) {
			// get relevant merges and their version tags if not exists
			if (issue.getFixVersions().contains(version)) {
				CodeElementDecision decision = getDecision(version, issue, changes);
				if (!decision.isEmpty()) result.add(decision);
			}
		}

			return result;
		}

		private CodeElementDecision getDecision(String version, IssueRecord issue,
				Collection<Change<String>> changes) {
			Collection<String> addedElements = new HashSet<>();
			Collection<String> removedElements = new HashSet<>();

			for (Change<String> change : changes) {
				EnhancedSet<String> addedFiles = new EnhancedHashSet<>(
						issue.getFileChanges().stream().map(Map.Entry::getValue)
								.map(x -> x.replace("\\", "/"))
								.collect(Collectors.toList()));
				EnhancedSet<String> addedChanges =
						new EnhancedHashSet<>(change.getAddedElements().stream()
					.map(x -> x.replace("\\", "/"))
					.collect(Collectors.toList()));
			addedElements.addAll(addedFiles.intersection(addedChanges));

			EnhancedSet<String> removedFiles = new EnhancedHashSet<>(
				issue.getFileChanges().stream().map(Map.Entry::getKey)
					.map(x -> x.replace("\\", "/"))
					.collect(Collectors.toList()));
			EnhancedSet<String> removedChanges =
				new EnhancedHashSet<>(change.getRemovedElements().stream()
					.map(x -> x.replace("\\", "/"))
					.collect(Collectors.toList()));
			removedElements.addAll(removedFiles.intersection(removedChanges));
		}

		return new CodeElementDecision(issue.description, issue.id, version,
			addedElements, removedElements);
	}
	//endregion
}
