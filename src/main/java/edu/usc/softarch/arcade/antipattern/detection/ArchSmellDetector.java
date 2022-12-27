package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.IOException;

import edu.usc.softarch.arcade.antipattern.detection.concern.ConcernOverload;
import edu.usc.softarch.arcade.antipattern.detection.concern.ScatteredParasiticFunctionality;
import edu.usc.softarch.arcade.antipattern.detection.dependency.DependencyCycle;
import edu.usc.softarch.arcade.antipattern.detection.dependency.LinkOverload;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.topics.DocTopics;

public class ArchSmellDetector {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		String depsRsfFilename = args[0];
		String clustersRsfFilename = args[1];
		String detectedSmellsFilename = args[2];
		String docTopicsPath = "";
		if (args.length > 3)
			docTopicsPath = args[3];

		if (!docTopicsPath.isEmpty()) {
			DocTopics.deserialize(docTopicsPath);
			ArchSmellDetector asd = new ArchSmellDetector(depsRsfFilename,
				clustersRsfFilename, detectedSmellsFilename, docTopicsPath);
			asd.run(true, true, true);
		} else {
			ArchSmellDetector asd = new ArchSmellDetector(depsRsfFilename,
				clustersRsfFilename, detectedSmellsFilename);
			asd.run(true, false, true);
		}
	}

	public SmellCollection run(boolean runStructural, boolean runConcern,
			boolean runSerialize) throws IOException {
		// Make sure at least one type of smell detection algorithms was selected
		if (!runConcern && !runStructural)
			throw new IllegalArgumentException("At least one type of smell "
				+ "detection must be selected.");

		// Initialize variables
		SmellCollection detectedSmells = new SmellCollection();

		// Execute detection algorithms
		if (runConcern) {
			detectedSmells.addAll(ConcernOverload.detect(this.arch));
			detectedSmells.addAll(ScatteredParasiticFunctionality.detect(this.arch,
				scatteredConcernThreshold, parasiticConcernThreshold));
		}
		if (runStructural) {
			detectedSmells.addAll(
				DependencyCycle.detect(this.arch, this.depsRsfFilename));
			detectedSmells.addAll(
				LinkOverload.detect(this.arch, this.depsRsfFilename));
		}

		// Serialize results
		if (runSerialize) {
			new File(detectedSmellsFilename).getParentFile().mkdirs();
			detectedSmells.serialize(detectedSmellsFilename);
		}

		// Return results
		return detectedSmells;
	}
	//endregion

	//region ATTRIBUTES
	private final String depsRsfFilename;
	private final String detectedSmellsFilename;
	private final double scatteredConcernThreshold;
	private final double parasiticConcernThreshold;
	private final ReadOnlyArchitecture arch;
	//endregion ATTRIBUTES

	//region CONSTRUCTORS
	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename) throws IOException {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, 
			"", .20,
			.20);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename,String docTopicsPathTemporary) throws IOException {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename,
			docTopicsPathTemporary, .20, .20);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename,String docTopicsPathTemporary,
			double scatteredConcernThreshold, double parasiticConcernThreshold)
			throws IOException {
		this.depsRsfFilename = depsRsfFilename;
		this.detectedSmellsFilename = detectedSmellsFilename;
		this.scatteredConcernThreshold = scatteredConcernThreshold;
		this.parasiticConcernThreshold = parasiticConcernThreshold;
		this.arch = ReadOnlyArchitecture.readFromRsf(clustersRsfFilename);
		if (!docTopicsPathTemporary.isEmpty())
			this.arch.loadDocTopics(docTopicsPathTemporary);
	}
	//endregion CONSTRUCTORS
}
