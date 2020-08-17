package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.callgraph.MyClass;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.ExtractionContext;

/**
 * @author joshua
 */
public class Cluster extends FeatureVector {
	private static final Logger logger = LogManager.getLogger(Cluster.class);
	private static final long serialVersionUID = -5521307722955232634L;
	public Set<FeatureVector> items = new HashSet<>();
	public List<Cluster> leafClusters = new ArrayList<>();
	public Set<MyClass> classes = new HashSet<>();
	
	private static UnbiasedEllenbergComparator DISTANCE_ORDER = null;
	private static  ConcernComparator CONCERN_ORDER;
	public PriorityQueue<Cluster> clusterSimQueue = null;
	
	public Cluster left = null;
	public Cluster right = null;
	
	public double simLeftRight = 0;
	public String type;
	
	
	public void instantiateClasses() {
		classes = new HashSet<>();
	}
	
	public void add(MyClass c) {
		classes.add(c);
	}
	
	public Cluster() {
		super();
		preparePriorityQueue();
	}

	public void preparePriorityQueue() {
		if (!Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.ARC)) {
			DISTANCE_ORDER = new UnbiasedEllenbergComparator();
			DISTANCE_ORDER.setRefCluster(this);
			clusterSimQueue = new PriorityQueue<Cluster>(1500, DISTANCE_ORDER);
		}
		else {
			CONCERN_ORDER = new ConcernComparator();
			CONCERN_ORDER.setRefCluster(this);
			clusterSimQueue = new PriorityQueue<Cluster>(1500, CONCERN_ORDER);
		}
	}
	
	public Cluster(Cluster left, Cluster right) {
		this.left = left;
		this.right = right;
		if (this.left != null && this.right != null) {
			this.simLeftRight = SimCalcUtil.getUnbiasedEllenbergMeasure(
					this.left, this.right);
		}
		setFeatureVectorAndName(left);
		logger.debug("Set feature vector of left in Cluster constructor: " + this.toBinaryForm());
		if (Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.WCA) || Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.ARC) )
			addClusterUsingWCAMerge(right);
		logger.debug("Added feature vector of right in Cluster constructor: " + this.toBinaryForm());
		if (this.equals(this.left))
			logger.debug("this is equal to left");
		else
			logger.debug("this is not equal to left");

		logger.debug(this.name + ":");
		logger.debug(this.toBinaryForm());
		logger.debug(this.name + "'s left: " + this.left);
		logger.debug(this.left.toBinaryForm());
		
		preparePriorityQueue();
	}

	public Cluster(FeatureVector fv) {
		setFeatureVectorAndName(fv);
		preparePriorityQueue();
		if (this.left != null && this.right != null) {
			this.simLeftRight = SimCalcUtil.getUnbiasedEllenbergMeasure(
					this.left, this.right);
		}
	}

	private void setFeatureVectorAndName(FeatureVector fv) {
		FeatureVector copyFV = new FeatureVector(fv.name);
		for (Feature f : fv) {
			copyFV.add(new Feature(f.edge,f.value));
		}
		
		items.add(copyFV);
		
		this.clear();
		this.addAll(copyFV);
		logger.debug("In " + ExtractionContext.getCurrentClassAndMethodName() + ": "
				+ itemsToString());
		this.name = itemsToString();
	}
	
	public String itemsToStringOnLine() {
		String str = "";
		for (FeatureVector fv : items) {
			str += fv.name + "\n";
		}
		
		return str;
	}
	
	public String itemsToString() {
		String str = "(";
		for (FeatureVector fv : items) {
			str += fv.name + ",";
		}
		str = str.substring(0,str.length()-1);
		str += ")";
		
		return str;
	}

	public void addClusterUsingPlainCAMerge(FeatureVector inFV) {
		FeatureVector copyFV = new FeatureVector(inFV.name);
		for (Feature f : inFV) {
			copyFV.add(new Feature(f.edge,f.value));
		}
		
		items.add(copyFV);
		plainCAMerge(copyFV);
		this.name = itemsToString();

	}
	
	public void addClusterUsingWCAMerge(FeatureVector inFV) {
		FeatureVector copyFV = new FeatureVector(inFV.name);
		for (Feature f : inFV) {
			copyFV.add(new Feature(f.edge,f.value));
		}
		
		items.add(copyFV);
		
		for (int i=0;i<this.size();i++) {
			double featureSum = 0;
			featureSum = this.get(i).value + copyFV.get(i).value;
			this.get(i).value = featureSum / items.size();
		}
		this.name = itemsToString();

	}

	private void plainCAMerge(FeatureVector inFV) {
		for (Feature f1 : this) {
			for (Feature f2 : inFV) {
				logger.debug("f1.edge.tgtStr: " + f1.edge.getTgtStr());
				logger.debug("f2.edge.tgtStr: " + f2.edge.getTgtStr());
				logger.debug("f1.value : " + f1.value);
				logger.debug("f2.value : " + f2.value);
				if (f1.edge.getTgtStr().equals(f2.edge.getTgtStr()) && (f1.value == 1 || f2.value == 1)) {
					if (f1.value == 1)
						f1.edge.setSrcStr(f1.edge.getSrcStr());
					else if (f2.value == 1)
						f1.edge.setSrcStr(f2.edge.getSrcStr());
					this.changeFeatureValue(f1.edge.getTgtStr(), 1);
				} else if (f1.edge.getTgtStr().equals(f2.edge.getTgtStr()) && !(f1.value == 1 || f2.value ==1)) {
					this.changeFeatureValue(f1.edge.getTgtStr(), 0);
				}
			}
		}
	}

	public String toString() {
		if (items == null) {
			return "empty cluster";
		}
		if (items.isEmpty()) {
			return "empty cluster";
		} else {
			String str = "(";
			for (FeatureVector fv : items) {
				str += fv.name + ",";
			}
			str = str.substring(0,str.length()-1);
			str += ")";
			return str;
		}

	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Cluster))
			return false;

		Cluster c = (Cluster) o;
		return this.name.equals(c.name);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		return hash;
	}
	
	public boolean equals(Cluster c) {
		for (Feature f1 : c) {
			for (Feature f2 :this ) {
				if (f1.edge.getTgtStr().equals(f2.edge.getTgtStr()) && (f1.value != f2.value)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void addClustersToPriorityQueue(List<Cluster> clusters) {
		
		if (TopicUtil.docTopics == null && Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.ARC))
			TopicUtil.docTopics = TopicUtil.getDocTopicsFromFile();
		for (Cluster c : clusters) {
			if (this.docTopicItem == null && Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.ARC))
				TopicUtil.setDocTopicForCluster(TopicUtil.docTopics, c);
			if ( !c.name.equals(this.name) ) {
				clusterSimQueue.add(c);
			}
		}
	}
	
	public Cluster getMostSimilarCluster() {
		return clusterSimQueue.peek();
	}

	public void clearPriorityQueue() {
		clusterSimQueue.clear();
	}

	public Set<MyClass> getClasses() {
		return new HashSet<>(classes);
	}
}
