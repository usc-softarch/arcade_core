package edu.usc.softarch.arcade.metrics;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.metrics.decay.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.util.FileUtil;

public class DecayMetricAnalyzer {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		String clusterPath = FileUtil.tildeExpandPath(args[0]);
		String depsPath = FileUtil.tildeExpandPath(args[1]);

		System.out.println(Arrays.toString(run(clusterPath, depsPath)));
	}

	public static double[] run(String clusterPath, String depsPath)
			throws IOException {
		if ((new File(clusterPath)).isFile())
			return runSingle(clusterPath, depsPath);
		else
			return runBatch(clusterPath, depsPath);
	}

	private static double[] runSingle(String clusterPath, String depsPath)
			throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> graph =
			ReadOnlyArchitecture.readFromRsf(clusterPath)
				.buildGraph(depsPath);
		double[] result = new double[7];

		result[0] = RatioCohesiveInteractions.detectRci(graph);
		result[1] = TwoWayPairRatio.computeTwoWayPairRatio(graph);
		result[2] = ArchitecturalStability.computeStability(graph);
		result[3] = TurboMQ.computeTurboMq(clusterPath, depsPath);
		result[4] = IntraConnectivity.computeIntraConnectivity(clusterPath, depsPath);
		result[5] = InterConnectivity.computeInterConnectivity(clusterPath, depsPath);
		result[6] = BasicMQ.computeBasicMq(clusterPath, depsPath);

		return result;
	}

	private static double[] runBatch(String clusterPath, String depsPath)
		throws IOException {
		List<File> clusterFiles = FileUtil.getFileListing(
			new File(FileUtil.tildeExpandPath(clusterPath)));
		clusterFiles = clusterFiles.stream()
			.filter(f -> f.getName().contains(".rsf")).collect(Collectors.toList());
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);

		List<File> depsFiles = FileUtil.getFileListing(
			new File(FileUtil.tildeExpandPath(depsPath)));
		depsFiles = depsFiles.stream()
			.filter(f -> f.getName().contains(".rsf")).collect(Collectors.toList());
		depsFiles = FileUtil.sortFileListByVersion(depsFiles);

		double[] decayValues = new double[(clusterFiles.size()) * 7];

		for (int i = 1; i < clusterFiles.size() + 1; i++) {
			double[] singleResults = runSingle(
				clusterFiles.get(i - 1).getAbsolutePath(),
				depsFiles.get(i - 1).getAbsolutePath());

			decayValues[(i - 1) * 7] = singleResults[0];
			decayValues[((i - 1) * 7) + 1] = singleResults[1];
			decayValues[((i - 1) * 7) + 2] = singleResults[2];
			decayValues[((i - 1) * 7) + 3] = singleResults[3];
			decayValues[((i - 1) * 7) + 4] = singleResults[4];
			decayValues[((i - 1) * 7) + 5] = singleResults[5];
			decayValues[((i - 1) * 7) + 6] = singleResults[6];
		}

		getDescriptiveStatistics(decayValues, clusterFiles.size());

		return decayValues;
	}

	private static void getDescriptiveStatistics(double[] decayValues, int size) {
		double[] rciValues = new double[size];
		double[] twoWayPairRatios = new double[size];
		double[] stability = new double[size];
		double[] turbomq = new double[size];
		double[] intra = new double[size];
		double[] inter = new double[size];
		double[] basicmq = new double[size];

		for (int i = 0; i < size - 1; i++) {
			rciValues[i] = decayValues[(i * 7)];
			twoWayPairRatios[i] = decayValues[(i * 7) + 1];
			stability[i] = decayValues[(i * 7) + 2];
			turbomq[i] = decayValues[(i * 7) + 3];
			intra[i] = decayValues[(i * 7) + 4];
			inter[i] = decayValues[(i * 7) + 5];
			basicmq[i] = decayValues[(i * 7) + 6];
		}

		DescriptiveStatistics rciStats = new DescriptiveStatistics(rciValues);
		DescriptiveStatistics twoWayPairStats =
			new DescriptiveStatistics(twoWayPairRatios);
		DescriptiveStatistics stabilityStats = new DescriptiveStatistics(stability);
		DescriptiveStatistics turbomqStats = new DescriptiveStatistics(turbomq);
		DescriptiveStatistics intraStats = new DescriptiveStatistics(intra);
		DescriptiveStatistics interStats = new DescriptiveStatistics(inter);
		DescriptiveStatistics basicmqStats = new DescriptiveStatistics(basicmq);

		System.out.println(rciStats);
		System.out.println(twoWayPairStats);
		System.out.println(stabilityStats);
		System.out.println(turbomqStats);
		System.out.println(intraStats);
		System.out.println(interStats);
		System.out.println(basicmqStats);
	}
	//endregion
}
