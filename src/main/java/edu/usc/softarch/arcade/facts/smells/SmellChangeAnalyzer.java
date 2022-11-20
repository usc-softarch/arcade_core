package edu.usc.softarch.arcade.facts.smells;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.facts.ChangeAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SmellChangeAnalyzer extends ChangeAnalyzer<Smell> {
	//region ATTRIBUTES
	private final SmellCollection arch1smells;
	private final SmellCollection arch2smells;
	//endregion

	//region CONSTRUCTORS
	public SmellChangeAnalyzer(String path1, String path2,
			String smells1, String smells2) throws IOException {
		super(path1, path2);
		this.arch1smells = SmellCollection.deserialize(smells1);
		this.arch2smells = SmellCollection.deserialize(smells2);
	}

	public SmellChangeAnalyzer(File file1, File file2,
			File smells1, File smells2) throws IOException {
		super(file1, file2);
		this.arch1smells = SmellCollection.deserialize(smells1);
		this.arch2smells = SmellCollection.deserialize(smells2);
	}

	public SmellChangeAnalyzer(
			ReadOnlyArchitecture arch1, ReadOnlyArchitecture arch2,
			SmellCollection smells1, SmellCollection smells2) {
		super(arch1, arch2);
		this.arch1smells = smells1;
		this.arch2smells = smells2;
	}
	//endregion

	//region PROCESSING
	@Override
	protected Set<Smell> getAddedElements(String source, String target) {
		Collection<Smell> sourceSmells = arch1smells.getClusterSmells(source);
		Collection<Smell> targetSmells = arch2smells.getClusterSmells(target);

		return getRelevantElements(targetSmells, sourceSmells);
	}

	@Override
	protected Set<Smell> getRemovedElements(String source, String target) {
		Collection<Smell> sourceSmells = arch1smells.getClusterSmells(source);
		Collection<Smell> targetSmells = arch2smells.getClusterSmells(target);

		return getRelevantElements(sourceSmells, targetSmells);
	}

	/**
	 * Gets smells that have a match in the other cluster. If looking for added
	 * smells, one looks for a match of each target smell in the source smells.
	 * If looking for removed smells, vice-versa.
	 */
	private Set<Smell> getRelevantElements(
			Collection<Smell> toMatch, Collection<Smell> against) {
		Set<Smell> result = new HashSet<>();

		for (Smell smell : toMatch)
			if (findMatchingSmell(smell, against))
				result.add(smell);

		return result;
	}

	private boolean findMatchingSmell(Smell toMatch, Collection<Smell> against) {
		switch (toMatch.getSmellType()) {
			/* There can only be one Link Overload smell instance per cluster, so
			 * if the matched cluster also has Link Overload, the smells match. */
			case buo:
				return against.stream()
					.noneMatch(s -> s.getSmellType().equals(Smell.SmellType.buo));

			/* A cluster can have multiple Concern Overload smells, but each has a
			 * unique topic number. */
			case bco:
				return against.stream()
					.filter(s -> s.getSmellType().equals(Smell.SmellType.bco))
					.noneMatch(s -> s.getTopicNum() == toMatch.getTopicNum());

			/* A cluster can be involved in multiple dependency cycles, but this is
			 * rare. For now, we will assume that if both are involved in a BDC, it
			 * is the same smell. If the matched cluster has two BDCs, an exception
			 * will be thrown, and if that ever happens, we can treat it. */
			case bdc:
				Collection<Smell> bdcInstances = against.stream()
					.filter(s -> s.getSmellType().equals(Smell.SmellType.bdc))
					.collect(Collectors.toList());
				if (bdcInstances.size() > 1)
					throw new RuntimeException("Matched cluster is involved in multiple "
						+ "dependency cycles. No instances of this have yet been found "
						+ "by the USC SoftArch team, and it is not covered by ARCADE. "
						+ "Please submit an issue at "
						+ "https://github.com/usc-softarch/arcade_core");
				return bdcInstances.isEmpty();

			/* This is dealt the same way as Concern Overload. */
			case spf:
				return against.stream()
					.filter(s -> s.getSmellType().equals(Smell.SmellType.spf))
					.noneMatch(s -> s.getTopicNum() == toMatch.getTopicNum());

			default:
				throw new IllegalArgumentException(
					"Unknown smell type " + toMatch.getSmellType());
		}
	}
	//endregion
}
