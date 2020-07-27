package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.usc.softarch.arcade.facts.ConcernCluster;

public class SmellEquivalenceTester {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		SpfSmell s1 = new SpfSmell(1);
		SpfSmell s2 = new SpfSmell(1);
		assertEquals(s1,s2);
		assertEquals(s2,s1);
		
		Set<Smell> detectedGtSmells = SmellUtil.deserializeDetectedSmells("/home/joshua/Documents/Software Engineering Research/projects/recovery/smell_detection/hadoop/ground_truth/hadoop_gt_smells.ser");
		Set<Smell> copiedGtSmells = new HashSet<Smell>(detectedGtSmells);
		
		assertTrue(detectedGtSmells.equals(copiedGtSmells));
		
		List<Smell> gtSmellList = new ArrayList<Smell>();
		
		for (Smell gtSmell1 : detectedGtSmells) {
			for (Smell gtSmell2 : copiedGtSmells) {
				if (gtSmell1.equals(gtSmell2)) {
					gtSmellList.add(gtSmell1);
				}
			}
		}
		
		assertTrue(detectedGtSmells.size() == gtSmellList.size());
		
		List<Smell> dupes = new ArrayList<Smell>();
		System.out.println("");
		System.out.println("Listing detected gt smells: ");
		for (Smell smell : detectedGtSmells) {
			System.out.println(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
			if (smell instanceof BuoSmell) {
				//System.out.println(smell + " is an instance of buo");
				/*for (ConcernCluster cluster : smell.clusters) {
					if (cluster.getName().equals("1__Other_utilities")) {
						dupes.add(smell);
					}
				}*/
			}
		}
		System.out.println();
		
		/*Set<ConcernCluster> dupeClusters1 = dupes.get(0).clusters;
		Set<ConcernCluster> dupeClusters2 = dupes.get(1).clusters;
		for (ConcernCluster cluster1 : dupeClusters1) {
			for (ConcernCluster cluster2 : dupeClusters2) {
				assertTrue(cluster1.equals(cluster2));
			}
		}*/
		//assertFalse(dupeClusters1.equals(dupeClusters2));
		//assertFalse(dupes.get(0).equals(dupes.get(1)));
	}

}
