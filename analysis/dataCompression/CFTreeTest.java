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
		bp1.add(-200, (float) 1);
		bp1.add(-150, (float) 1);
		cf1 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf1.updateCF(bp1, 1, false);
		
		bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-100, (float) 1);
		bp2.add(-50, (float) 1);
		cf2 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf2.updateCF(bp2, 2, false);
		
		bp3 = new BinnedPeakList(new Normalizer());
		bp3.add(-200, (float) 1);
		bp3.add(-150, (float) 1);
		cf3 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf3.updateCF(bp3, 3, false);
		
		bp4 = new BinnedPeakList(new Normalizer());
		bp4.add(150, (float) 1);
		bp4.add(200, (float) 1);
		cf4 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf4.updateCF(bp4, 4, false);
		
		bp5 = new BinnedPeakList(new Normalizer());
		bp5.add(-100, (float) 1);
		cf5 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		cf5.updateCF(bp5, 5, false);
		
		node1 = new CFNode(null, dMetric);
		node1.addCF(cf1);
		node1.addCF(cf2);
		node2 = new CFNode(null, dMetric);
		node2.addCF(cf1);
		node2.addCF(cf4);
		tree1 = new CFTree(0, 2, dMetric);
		
	}
	public void testInsertEntry() {
		tree1.insertEntry(bp1, 1);
		assertEquals(tree1.getSize(),1);
		assertTrue(tree1.root.isLeaf());
		assertTrue(tree1.root.sameContents(node1));
		
		tree1.insertEntry(bp2, 2);
		assertEquals(tree1.getSize(), 2);
		assertTrue(tree1.root.isLeaf());
		assertTrue(tree1.root.sameContents(node1));
		
		tree1.insertEntry(bp3, 3);
		assertEquals(tree1.getSize(), 3);
		assertTrue(tree1.root.isLeaf());
		assertEquals(tree1.root.getCFs().size(), 2);
	}

	public void testSplitNodeIfPossible() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		tree1.splitNodeIfPossible(tree1.root);
		assertEquals(tree1.getSize(), 2);
		assertTrue(tree1.root.isLeaf());
		assertEquals(tree1.root.getCFs().size(), 2);
		
		tree1.insertEntry(bp4, 4);

		assertEquals(tree1.getSize(), 3);
		assertTrue(tree1.root.isLeaf());
		
		tree1.splitNodeIfPossible(tree1.root);
		assertEquals(tree1.root.getCFs().size(), 2);
		assertTrue(tree1.root.getCFs().get(0).child.sameContents(node2));
	}

	public void testFindClosestLeafEntry() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		tree1.splitNodeIfPossible(tree1.root);
		tree1.insertEntry(bp4, 4);
		tree1.splitNodeIfPossible(tree1.root);
		
		Pair<ClusterFeature, Float> pair = tree1.findClosestLeafEntry(bp5, tree1.root);
		assertEquals(pair.first, tree1.root.getCFs().get(1).child.getCFs().get(0));
		assertEquals(pair.second, tree1.root.getCFs().get(1).child.getCFs().get(0).getSums().getDistance(bp5, dMetric));
	}
	public void testRemoveNode() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		tree1.splitNodeIfPossible(tree1.root);
		tree1.insertEntry(bp4, 4);
		tree1.splitNodeIfPossible(tree1.root);

		tree1.removeNode(tree1.root.getCFs().get(1).child);
		assertEquals(tree1.root.getCFs().size(), 1);
		assertTrue(tree1.root.getCFs().get(0).child.sameContents(node2));
	}
	public void testAssignLeaves() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		tree1.splitNodeIfPossible(tree1.root);
		tree1.insertEntry(bp4, 4);
		tree1.splitNodeIfPossible(tree1.root);
		assertTrue(tree1.getFirstLeaf().sameContents(node2));
	}
	public void testReinsertEntry() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		tree1.splitNodeIfPossible(tree1.root);
		tree1.insertEntry(bp5, 5);
		tree1.splitNodeIfPossible(tree1.root);
		
		tree2 = new CFTree((float)0.6, 2, dMetric);
		
		assertTrue(tree2.reinsertEntry(tree1.root.getCFs().get(0).child.getCFs().get(0)));
		assertTrue(tree2.reinsertEntry(tree1.root.getCFs().get(1).child.getCFs().get(0)));
		assertTrue(tree2.reinsertEntry(tree1.root.getCFs().get(1).child.getCFs().get(1)));
		assertFalse(tree2.reinsertEntry(cf4));
		
	}
	public void testMergeEntries() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		assertEquals(tree1.root.getSize(), 2);
		
		tree1.mergeEntries(tree1.root.getCFs().get(0), tree1.root.getCFs().get(1));
		assertEquals(tree1.root.getSize(), 1);
	}

	public void testCheckForMerge() {
		tree1.insertEntry(bp1, 1);
		tree1.insertEntry(bp2, 2);
		tree1.splitNodeIfPossible(tree1.root);
		tree1.insertEntry(bp4, 4);
		tree1.splitNodeIfPossible(tree1.root);
		
		CFNode node = tree1.root.getCFs().get(0).child;
		node.addCF(cf3);
		assertEquals(node.getCFs().size(),3);
		tree1.checkForMerge(node);
		assertEquals(node.getCFs().size(),2);
	}



}
