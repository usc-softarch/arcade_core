package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.clustering.data.Architecture;
import edu.usc.softarch.arcade.clustering.data.Cluster;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.util.CLI;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Pkg {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = CLI.parseArguments(args);
		Architecture result = run(parsedArgs.get("projectname"),
			parsedArgs.get("projectversion"), parsedArgs.get("projectpath"),
			parsedArgs.get("depspath"), parsedArgs.get("language"));
		result.writeToRsf(false);
	}

	public static Architecture run(String projectName, String projectVersion,
			String projectPath, String depsPath, String language)
			throws IOException {
		// Load the deps.rsf and initialize Architecture
		Architecture arch = new Architecture(projectName, projectVersion,
			projectPath, SimMeasure.SimMeasureType.PKG, depsPath, language);

		// Go over all loaded entities and insert them into appropriate clusters
		Map<String, Cluster> pkgClusters = new HashMap<>();
		for (Map.Entry<String, Cluster> entry : arch.entrySet()) {
			String entityName = entry.getKey();
			String packageName = identifyPackage(entityName);
			pkgClusters.putIfAbsent(packageName, new Cluster(packageName));
			pkgClusters.get(packageName).addEntity(entityName);
		}

		// Clear the Architecture and put the new Clusters in
		arch.clear();
		arch.putAll(pkgClusters);

		return arch;
	}
	//endregion

	//region PROCESSING
	//TODO Fix this so it can take Java class formats too
	private static String identifyPackage(String entity) {
		return identifyPackage(entity, File.separator);
	}

	private static String identifyPackage(String entity, String separator) {
		int index = entity.lastIndexOf(separator);
		if (index != -1)
			return entity.substring(0, entity.lastIndexOf(separator));
		else
			return "root";
	}
	//endregion
}
