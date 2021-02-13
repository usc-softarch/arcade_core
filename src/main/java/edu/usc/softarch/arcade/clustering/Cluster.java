package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import edu.usc.softarch.arcade.callgraph.MyClass;
import edu.usc.softarch.arcade.config.Config;

/**
 * @author joshua
 */
public class Cluster extends FeatureVector {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -5521307722955232634L;
	
	private Set<FeatureVector> items;
	private List<Cluster> leafClusters;
	private Set<MyClass> classes;
	private Queue<Cluster> clusterSimQueue;
	private Cluster left;
	private Cluster right;
	private Double simLeftRight;
	private String type;
	
	private SimMeasureComparator _ORDER;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public Cluster() {
		super();

		if (!Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.ARC))
			_ORDER = new UnbiasedEllenbergComparator();
		else
			_ORDER = new ConcernComparator();

		_ORDER.setRefCluster(this);

		this.items = new HashSet<>();
		this.leafClusters = new ArrayList<>();
		this.classes = new HashSet<>();
		this.simLeftRight = 0.0;
		this.clusterSimQueue = new PriorityQueue<>(1500, _ORDER);
	}

	/**
	 * Clone constructor.
	 */
	public Cluster(Cluster c) {
		this();
		if (c == null) return; // Recursion end

		this.items = new HashSet<>(c.getItems());
		this.leafClusters = new ArrayList<>(c.getLeafClusters());
		this.classes = new HashSet<>(c.getClasses());
		this.clusterSimQueue = new PriorityQueue<>(c.getClusterSimQueue());
		this.left = new Cluster(c.getLeft());
		this.right = new Cluster(c.getRight());
		this.simLeftRight = c.getSimLeftRight();
		this.type = c.getType();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	public Set<FeatureVector> getItems() { return new HashSet<>(items); }
	public List<Cluster> getLeafClusters() {
		return new ArrayList<>(leafClusters); }
	public Set<MyClass> getClasses() { return new HashSet<>(classes); }
	public PriorityQueue<Cluster> getClusterSimQueue() { 
		return new PriorityQueue<>(clusterSimQueue); }
	public Cluster getMostSimilarCluster() { return clusterSimQueue.peek(); }
	public Cluster getLeft() { return this.left; }
	public Cluster getRight() { return this.right; }
	public Double getSimLeftRight() { return this.simLeftRight; }
	public String getType() { return this.type; }

	public boolean addItem(FeatureVector fv) { return this.items.add(fv); }
	public boolean removeItem(FeatureVector fv) { return this.items.remove(fv); }
	public void setLeafClusters(List<Cluster> leafClusters) {
		this.leafClusters = leafClusters; }
	public boolean addLeafCluster(Cluster leafCluster) {
		return this.leafClusters.add(leafCluster); }
	public boolean removeLeafCluster(Cluster leafCluster) {
		return this.leafClusters.remove(leafCluster); }
	public boolean addClass(MyClass c) { return this.classes.add(c); }
	public boolean removeClass(MyClass c) { return this.classes.remove(c); }
	public void resetClasses() { classes = new HashSet<>(); }
	public void setLeft(Cluster left) { this.left = left; }
	public void setRight(Cluster right) { this.right = right; }
	public void setSimLeftRight(Double simLeftRight) {
		this.simLeftRight = simLeftRight; }
	public void setType(String type) { this.type = type; }
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	@Override
	public String toString() {
		if (items == null || items.isEmpty())
			return "empty cluster";
		
		String str = "(";
		for (FeatureVector fv : items)
			str += fv.getName() + ",";
		
		str = str.substring(0, str.length()-1);
		str += ")";
		return str;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Cluster))
			return false;

		Cluster c = (Cluster) o;
		return this.getName().equals(c.getName());
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.getName() == null ? 0 : this.getName().hashCode());
		return hash;
	}
	
	/**
	 * @deprecated
	 */
	public boolean equals(Cluster c) { //TODO replace with the other one
		for (Feature f1 : c)
			for (Feature f2 :this )
				if (f1.getEdge().getTgtStr().equals(f2.getEdge().getTgtStr()) && (!f1.getValue().equals(f2.getValue())))
					return false;

		return true;
	}
	// #endregion MISC -----------------------------------------------------------
}