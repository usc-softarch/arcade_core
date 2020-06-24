package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import edu.usc.softarch.arcade.classgraphs.SootClassEdge;
import edu.usc.softarch.arcade.clustering.ClusteringEngine;
import edu.usc.softarch.arcade.clustering.Feature;
import edu.usc.softarch.arcade.clustering.FeatureVector;
import edu.usc.softarch.arcade.clustering.SimCalcUtil;

import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class ClusteringEngineTest extends TestCase {
	
	ClusteringEngine ce = new ClusteringEngine();
	FeatureVector fv1 =new FeatureVector();
	FeatureVector fv2 =new FeatureVector();
	FeatureVector fv3 =new FeatureVector();
	FeatureVector fv4 =new FeatureVector();	

	protected void setUp() throws Exception {
		SootClassEdge e = null;
		for (int i=0;i<2;i++) {
			for (int j=0;j<4;j++) {
				StringBuffer s1 = new StringBuffer("A");
				s1.setCharAt(0,(char)(s1.charAt(0)+i));
				
				StringBuffer s2 = new StringBuffer("A");
				s2.setCharAt(0,(char)(s2.charAt(0)+j));
				System.out.println(s1 + "" + s2);
				e = new SootClassEdge(s1.toString(),s2.toString());
				Feature f = null;
				if (e.srcStr.equals("A") && e.tgtStr.equals("A")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("B") && e.tgtStr.equals("D")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("B") && e.tgtStr.equals("A")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("A") && e.tgtStr.equals("B")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("B") && e.tgtStr.equals("C")) {
					f = new Feature(e,1);
				}
				else {
					f = new Feature(e,0);
				}
				//System.out.println("f: " + f);
				if (e.srcStr.equals("A")) {
					fv1.add(f);
				}
				else if (e.srcStr.equals("B")){
					fv2.add(f);
				}
				/*System.out.println(fv1.toBinaryForm());
				System.out.println(fv2.toBinaryForm());*/
			}
		}
		System.out.println("Features vectors 1 and 2 respectively: ");
		System.out.println("fv1:" + fv1.toBinaryForm());
		System.out.println("fv2:" +fv2.toBinaryForm());
		
		for (int i=0;i<2;i++) {
			for (int j=0;j<4;j++) {
				StringBuffer s1 = new StringBuffer("A");
				s1.setCharAt(0,(char)(s1.charAt(0)+i));
				
				StringBuffer s2 = new StringBuffer("A");
				s2.setCharAt(0,(char)(s2.charAt(0)+j));
				System.out.println(s1 + "" + s2);
				e = new SootClassEdge(s1.toString(),s2.toString());
				Feature f = null;
				if (e.tgtStr.equals("A")) {
					f = new Feature(e,1);
				}
				else if (e.tgtStr.equals("D")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("A") && e.tgtStr.equals("A")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("B") && e.tgtStr.equals("B")) {
					f = new Feature(e,1);
				}
				else if (e.srcStr.equals("A") && e.tgtStr.equals("C")) {
					f = new Feature(e,1);
				}
				else {
					f = new Feature(e,0);
				}
				//System.out.println("f: " + f);
				if (e.srcStr.equals("A")) {
					fv3.add(f);
				}
				else if (e.srcStr.equals("B")){
					fv4.add(f);
				}
				/*System.out.println(fv1.toBinaryForm());
				System.out.println(fv2.toBinaryForm());*/
			}
		}
		System.out.println("Features vectors 3 and 4 respectively: ");
		System.out.println("fv3:" + fv3.toBinaryForm());
		System.out.println("fv4:" +fv4.toBinaryForm());
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

/*	public void testClusteringEngine() {
		fail("Not yet implemented");
	}*/

	public void testGetJaccardSim() {
		assertEquals(0.25f,SimCalcUtil.getJaccardSim(fv1, fv2), .005f);
		assertEquals(0.25f,SimCalcUtil.getJaccardSim(fv2, fv1), .005f);
		assertEquals(0.5f,SimCalcUtil.getJaccardSim(fv3, fv4), .005f);
		assertEquals(0.5f,SimCalcUtil.getJaccardSim(fv4, fv3), .005f);
	}

	public void testGetNumSharedFeatures() {
		assertEquals(1,SimCalcUtil.getNumSharedFeatures(fv1,fv2));
		assertEquals(1,SimCalcUtil.getNumSharedFeatures(fv2,fv1));
		assertEquals(2,SimCalcUtil.getNumSharedFeatures(fv3,fv4));
		assertEquals(2,SimCalcUtil.getNumSharedFeatures(fv4,fv3));
	}
	
	public void testSharedFeaturesMap() {
		HashSet<FeatureVector> set1 = new HashSet<FeatureVector>(2);
		set1.add(fv1);
		set1.add(fv2);
		SimCalcUtil.sharedFeaturesMap.put(set1, SimCalcUtil.getNumSharedFeatures(fv1, fv2));
		
		HashSet<FeatureVector> set2 = new HashSet<FeatureVector>(2);
		set2.add(fv2);
		set2.add(fv1);
		
		HashSet<FeatureVector> set3 = new HashSet<FeatureVector>(2);
		set3.add(fv3);
		set3.add(fv4);
		
		//System.out.println("SharedFeaturesMap contains key "  + set1 + "  " + ce.SharedFeaturesMap.containsKey(set1));
		//System.out.println("SharedFeaturesMap contains key "  + set2 + "  " + ce.SharedFeaturesMap.containsKey(set2));
		//System.out.println("SharedFeaturesMap contains key "  + set3 + "  " + ce.SharedFeaturesMap.containsKey(set3));
		
		assertTrue(SimCalcUtil.sharedFeaturesMap.containsKey(set1));
		assertTrue(SimCalcUtil.sharedFeaturesMap.containsKey(set2));
		assertFalse(SimCalcUtil.sharedFeaturesMap.containsKey(set3));
		
		System.out.println("SimCalcUtil.SharedFeaturesMap.get(set1): " + SimCalcUtil.sharedFeaturesMap.get(set1));
		System.out.println("SimCalcUtil.SharedFeaturesMap.get(set2): " + SimCalcUtil.sharedFeaturesMap.get(set2));
		System.out.println("SimCalcUtil.SharedFeaturesMap.get(set3): " + SimCalcUtil.sharedFeaturesMap.get(set3));
		
		SimCalcUtil.sharedFeaturesMap.put(set3, SimCalcUtil.getNumSharedFeatures(fv3, fv4));
		
		System.out.println("SimCalcUtil.SharedFeaturesMap.get(set3): " + SimCalcUtil.sharedFeaturesMap.get(set3));
		
		assertEquals(1,SimCalcUtil.sharedFeaturesMap.get(set1).intValue());
		assertEquals(1,SimCalcUtil.sharedFeaturesMap.get(set2).intValue());
		assertEquals(2,SimCalcUtil.sharedFeaturesMap.get(set3).intValue());
		
	}

	public void testGetNum10Features() {
		assertEquals(1,SimCalcUtil.getNum10Features(fv1, fv2));
		assertEquals(2,SimCalcUtil.getNum10Features(fv2, fv1));
	}
	
	public void testOneZeroFeaturesMap() {
		
		ArrayList<FeatureVector> pair1 = new ArrayList<FeatureVector>(2);
		pair1.add(fv1);
		pair1.add(fv2);
		SimCalcUtil.oneZeroFeaturesMap.put(pair1, SimCalcUtil.getNum10Features(fv1, fv2));
		System.out.println("SimCalcUtil.getNum10Features(fv1, fv2): " + SimCalcUtil.getNum10Features(fv1, fv2));
		
		ArrayList<FeatureVector> pair2 = new ArrayList<FeatureVector>(2);
		pair2.add(fv2);
		pair2.add(fv1);
		SimCalcUtil.oneZeroFeaturesMap.put(pair2, SimCalcUtil.getNum10Features(fv2, fv1));
		System.out.println("SimCalcUtil.getNum10Features(fv2, fv1): " + SimCalcUtil.getNum10Features(fv2, fv1));
		
		assertEquals(SimCalcUtil.getNum10Features(fv1, fv2), SimCalcUtil.oneZeroFeaturesMap.get(pair1).intValue()  );
		assertEquals(SimCalcUtil.getNum10Features(fv2, fv1), SimCalcUtil.oneZeroFeaturesMap.get(pair2).intValue() );
	}

	public void testGetNum01Features() {
		assertEquals(2,SimCalcUtil.getNum01Features(fv1, fv2));
		assertEquals(1,SimCalcUtil.getNum01Features(fv2, fv1));
	}

	/*public void testMain() {
		fail("Not yet implemented");
	}*/

}
