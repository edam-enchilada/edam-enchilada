package analysis.dataCompression;

import junit.framework.TestCase;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;

public class CFTreeTest extends TestCase {
	CFTree tree1, tree2;
	CFNode node1, node2, node3, node4, node5;
	ClusterFeature cf1, cf2, cf3, cf4, cf5;
	BinnedPeakList bp1, bp2, bp3, bp4, bp5;
	DistanceMetric dMetric;
	
	protected void setUp() throws Exception {
		super.setUp();
		dMetric = DistanceMetric.EUCLIDEAN_SQUARED;
		bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-210, (float) 1);
		bp1.add(-160, (float) 1);
		bp1.add(-100, (float) 1);
		bp1.add(-30, (float) 1);
		bp1.add(20, (float) 2);
		bp1.add(90, (float) 2);
		bp1.add(120, (float) 2);
		cf1 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf1.updateCF(bp1, 1, false);
		
		bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-210, (float) 1);
		bp2.add(-160, (float) 1);
		bp2.add(-100, (float) 1);
		bp2.add(-30, (float) 1);
		bp2.add(20, (float) 2);
		bp2.add(90, (float) 2);
		bp2.add(120, (float) 2);
		cf2 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf2.updateCF(bp2, 1, false);
		
		bp3 = new BinnedPeakList(new Normalizer());
		bp3.add(-210, (float) 1);
		bp3.add(-160, (float) 1);
		bp3.add(-100, (float) 1);
		bp3.add(-30, (float) 1);
		bp3.add(20, (float) 2);
		bp3.add(90, (float) 2);
		bp3.add(120, (float) 2);
		cf3 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf3.updateCF(bp3, 1, false);
		
		bp4 = new BinnedPeakList(new Normalizer());
		bp4.add(-210, (float) 1);
		bp4.add(-160, (float) 1);
		bp4.add(-100, (float) 1);
		bp4.add(-30, (float) 1);
		bp4.add(20, (float) 2);
		bp4.add(90, (float) 2);
		bp4.add(120, (float) 2);
		cf4 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf4.updateCF(bp4, 1, false);
	}
	public void testInsertEntry() {
	}

	public void testReinsertEntry() {
	}

	public void testFindClosestLeafEntry() {
	}

	public void testSplitNodeIfPossible() {
	}

	public void testUpdateNonSplitPath() {
	}

	public void testGetFirstLeaf() {
	}

	public void testRemoveNode() {
	}

	public void testRefineMerge() {
	}

	public void testMergeEntries() {
	}

	public void testCheckForMerge() {
	}

	public void testNextThreshold() {
	}

	public void testAssignLeaves() {
	}

	public void testGetNodeNumber() {
	}

	public void testFindTreeMemory() {
	}

}
