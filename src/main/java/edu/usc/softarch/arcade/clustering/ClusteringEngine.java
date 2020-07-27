package edu.usc.softarch.arcade.clustering;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import mojo.MoJoCalculator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Language;
import edu.usc.softarch.arcade.util.DebugUtil;
import edu.usc.softarch.arcade.util.StopWatch;

/**
 * @author joshua
 * 
 */
public class ClusteringEngine {

	private FeatureVectorMap fvMap = new FeatureVectorMap();

	private static ArrayList<Cluster> clusters;

	private static Logger logger = Logger.getLogger(ClusteringEngine.class);

	

	public ClusteringEngine() {

	}

	public ClusteringEngine(ClassGraph clg) throws TransformerException,
			ParserConfigurationException, SAXException, IOException {
	}

	public void run() throws Exception {
		
		FastFeatureVectors fastFeatureVectors = null;

		ArrayList<FastCluster> fastClusters = null;
		
		File fastFeatureVectorsFile = new File(
				Config.getFastFeatureVectorsFilename());

		ObjectInputStream objInStream = new ObjectInputStream(
				new FileInputStream(fastFeatureVectorsFile));

		// Deserialize the object
		try {
			fastFeatureVectors = (FastFeatureVectors) objInStream.readObject();
			logger.debug("feature set size: "+ fastFeatureVectors.getNamesInFeatureSet().size());
			logger.debug("Names in Feature Set:");
			logger.debug(fastFeatureVectors.getNamesInFeatureSet());
			objInStream.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Read in serialized feature vectors...");

		if (Config.isExcelFileWritingEnabled) {
			writeXLSFromOriginalDeps();
		}

		// logger.debug(vecMap);

		StopWatch stopwatch = new StopWatch();

		stopwatch.start();
		// computeHierarchicalClustersUsingBasicMethod();
		if (Config.getCurrentClusteringAlgorithm()
				.equals(ClusteringAlgorithmType.WCA)) {
			WcaRunner.setFastFeatureVectors(fastFeatureVectors);
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.preselected)) {
				StoppingCriterion stopCriterion = new PreSelectedStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(stopCriterion);
			}
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.clustergain)) {
				StoppingCriterion singleClusterStopCriterion = new SingleClusterStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(singleClusterStopCriterion);
				StoppingCriterion clusterGainStopCriterion = new ClusterGainStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(clusterGainStopCriterion);
			}
		}
		
		for (int numTopics : Config.getNumTopicsList()) {
			Config.setNumTopics(numTopics);
			if (Config.getCurrentClusteringAlgorithm().equals(
					ClusteringAlgorithmType.ARC)) {
				throw new Exception("Pooyan-> there is a null instead of outputDir/base"); 
				
				/*
				ConcernClusteringRunner runner = new ConcernClusteringRunner(
						fastFeatureVectors, Config.tmeMethod, Config.srcDir,
						null, numTopics, Config.getTopicsFilename(),
						Config.getDocTopicsFilename(),
						Config.getTopWordsFilename());
				runner.computeClustersWithConcernsAndFastClusters(new PreSelectedStoppingCriterion());
				if (Config.stoppingCriterion
						.equals(Config.StoppingCriterionConfig.clustergain)) {
					runner.computeClustersWithConcernsAndFastClusters(new ClusterGainStoppingCriterion());
				}
				*/
			}
		}
		
		
		if (Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.LIMBO)) {
			LimboRunner.setFastFeatureVectors(fastFeatureVectors);
			LimboRunner.computeClusters(new PreSelectedStoppingCriterion());
			if (Config.stoppingCriterion.equals(Config.StoppingCriterionConfig.clustergain)) {
				LimboRunner.computeClusters(new ClusterGainStoppingCriterion());
			}
		}
		stopwatch.stop();

		String timeInSecsToComputeClusters = "Time in seconds to compute clusters: "
				+ stopwatch.getElapsedTimeSecs();
		String timeInMilliSecondsToComputeClusters = "Time in milliseconds to compute clusters: "
				+ stopwatch.getElapsedTime();
		logger.debug(timeInSecsToComputeClusters);
		System.out.println(timeInSecsToComputeClusters);
		logger.debug(timeInMilliSecondsToComputeClusters);
		System.out.println(timeInMilliSecondsToComputeClusters);
		logger.debug("Final clusters: " + fastClusters);

	}

	

	private void clusterPostProcessing() throws FileNotFoundException,
			UnsupportedEncodingException, IOException,
			ParserConfigurationException, TransformerException {
		ClusterUtil.generateLeafClusters(clusters);
		int itemsInClusters = 0;
		for (Cluster c : clusters) {
			itemsInClusters += c.leafClusters.size();
		}

		StringGraph clusterGraph = ClusterUtil.generateClusterGraph(clusters);
		logger.debug("Resulting ClusterGraph...");
		logger.debug(clusterGraph);

		HashMap<String, Integer> clusterNameToNodeNumberMap = ClusterUtil
				.createClusterNameToNodeNumberMap(clusters);
		TreeMap<Integer, String> nodeNumberToClusterNameMap = ClusterUtil
				.createNodeNumberToClusterNameMap(clusters,
						clusterNameToNodeNumberMap);

		ClusterUtil.writeClusterRSFFile(clusterNameToNodeNumberMap, clusters);

		if (Config.runMojo) {
			runMojoAgainstTargetFile(clusterNameToNodeNumberMap,
					Config.getMojoTargetFile());
		}

		clusterGraph.writeNumberedNodeDotFileWithTextMappingFile(
				Config.getClusterGraphDotFilename(),
				clusterNameToNodeNumberMap, nodeNumberToClusterNameMap);
		clusterGraph.writeXMLClusterGraph(Config.getClusterGraphXMLFilename());

		if (Config.isExcelFileWritingEnabled) {
			createXLSOfFeatureVectorsFromSplitClusters(clusters);
		}

		logger.debug("Serializing clusters...");
		serializeCAClusters();
		logger.debug("Finished serializing clusters...");
	}

	private void runMojoAgainstTargetFile(
			HashMap<String, Integer> clusterNameToNodeNumberMap,
			String targetFilename) throws IOException {
		ByteArrayOutputStream sourceStream = ClusterUtil
				.writeRSFToByteArrayOutputStream(clusterNameToNodeNumberMap,
						clusters);

		logger.debug("Printing source stream...");
		logger.debug(DebugUtil
				.convertByteArrayOutputStreamToString(sourceStream));

		MoJoCalculator mojoCalc = new MoJoCalculator(null, targetFilename, null);
		mojoCalc.sourceStream = sourceStream;
		long mojoValue = mojoCalc.mojoUsingSourceStream();
		logger.debug("MoJo of this clustering compared to " + targetFilename
				+ ": " + mojoValue);
	}

	

	

	

	

	

	/*
	 * private void printMostSimilarClustersForEachFastCluster() {
	 * logger.debug("Printing most similar clusters for each cluster..."); for
	 * (FastCluster c : fastClusters) { logger.debug("Most similar cluster to "
	 * + c + " is " + c.getMostSimilarCluster());
	 * TopicUtil.printTwoDocTopics(c.docTopicItem,
	 * c.getMostSimilarCluster().docTopicItem); } }
	 */

	private void writeXLSFromOriginalDeps() throws IOException {
		Collection<FeatureVector> fvColl = fvMap.featureVectorNameToFeatureVectorMap
				.values();
		Vector<FeatureVector> fvVec = new Vector<FeatureVector>(fvColl);

		WritableWorkbook workbook = Workbook.createWorkbook(new File(Config
				.getXLSDepsFilename()));
		WritableSheet sheet = workbook.createSheet("First Sheet", 0);

		FeatureVector first = (FeatureVector) fvVec.firstElement();
		int labelIndex = 0;
		for (Feature f : first) {
			Label label = new Label(labelIndex + 1, 0, f.edge.tgtStr);
			try {
				sheet.addCell(label);
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			labelIndex++;
		}

		for (int row = 0; row < fvVec.size(); row++) {
			FeatureVector fv = (FeatureVector) fvVec.elementAt(row);
			Label label = new Label(0, row + 1, fv.name);
			logger.debug(fv.name);
			try {
				sheet.addCell(label);
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int featureIndex = 0; featureIndex < fv.size(); featureIndex++) {
				Feature currentFeature = (Feature) fv.get(featureIndex);
				Feature f2 = (Feature) first.get(featureIndex);

				if (!currentFeature.edge.tgtStr.equals(f2.edge.tgtStr)) {
					logger.debug("While creating xls file for original fvMap, feature indices do not match...exiting");
					System.exit(1);
				}

				Number number = new Number(featureIndex + 1, row + 1,
						currentFeature.value);
				try {
					sheet.addCell(number);
				} catch (RowsExceededException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createXLSOfFeatureVectorsFromSplitClusters(
			ArrayList<Cluster> splitClusters) throws IOException {
		WritableWorkbook workbook = Workbook.createWorkbook(new File(Config
				.getXLSSimMeasureFilename()));
		WritableSheet sheet = workbook.createSheet("First Sheet", 0);

		logger.debug("Displaying returned clusters...");
		Cluster first = (Cluster) splitClusters.get(0);
		int labelIndex = 0;
		for (Feature f : first) {
			Label label = new Label(labelIndex + 1, 0, f.edge.tgtStr);
			try {
				sheet.addCell(label);
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			labelIndex++;
		}

		for (int i = 0; i < splitClusters.size(); i++) {
			Cluster c = (Cluster) splitClusters.get(i);
			Label label = new Label(0, i + 1, c.name);
			logger.debug(c.name);
			try {
				sheet.addCell(label);
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int featureIndex = 0; featureIndex < c.size(); featureIndex++) {
				Feature currentFeature = (Feature) c.get(featureIndex);
				Feature f2 = (Feature) first.get(featureIndex);
				if (!currentFeature.edge.tgtStr.equals(f2.edge.tgtStr)) {
					logger.debug("While creating xls file for similarity measures, feature indices do not match...exiting");
					System.exit(1);
				}

				Number number = new Number(featureIndex + 1, i + 1,
						currentFeature.value);
				try {
					sheet.addCell(number);
				} catch (RowsExceededException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	

	

	private void printStructuralDataForClustersBeingMerged(Cluster newCluster) {
		logger.debug("\t\t" + "Results of merge:");
		logger.debug("\t\t" + newCluster.name + ": ");
		logger.debug("\t\t" + newCluster.toBinaryForm());
	}

	private void serializeCAClusters() {
		String filename = Config.getSerializedClustersFilename();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeInt(clusters.size());
			for (Cluster c : clusters) {
				out.writeObject(c);
			}
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public class SharedFeature {
		Feature f1;
		Feature f2;

		SharedFeature(Feature f1, Feature f2) {
			this.f1 = f1;
			this.f2 = f2;
		}

		public String toString() {
			return this.f1 + "," + this.f2;
		}
	}

	public Vector<SharedFeature> getSharedFeatures(FeatureVector fv1,
			FeatureVector fv2) {
		boolean local_debug = false;
		int count = 0;
		Vector<SharedFeature> sharedFeatures = new Vector<SharedFeature>();
		for (int i = 0; i < fv1.size(); i++) {
			Feature f = fv1.get(i);
			for (int j = 0; j < fv2.size(); j++) {
				Feature f2 = fv2.get(j);
				if (local_debug && logger.isDebugEnabled()) {
					logger.debug("f.edge.tgtStr: " + f.edge.tgtStr);
					logger.debug("f2.edge.tgtStr: " + f2.edge.tgtStr);
					logger.debug("f.value: " + f.value);
					logger.debug("f2.value: " + f2.value);
					logger.debug("\n");
				}
				if (f.edge.tgtStr.equals(f2.edge.tgtStr) && f.value > 0
						&& f2.value > 0) {
					SharedFeature sf = new SharedFeature(f, f2);
					sharedFeatures.add(sf);
					count++;
					if (local_debug && logger.isDebugEnabled())
						logger.debug("Increased 11 count to: " + count);
				}
			}

		}
		return sharedFeatures;
	}

}
