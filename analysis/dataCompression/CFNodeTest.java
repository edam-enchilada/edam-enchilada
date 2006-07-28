package analysis.dataCompression;

import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;
import junit.framework.TestCase;
import java.util.ArrayList;

public class CFNodeTest extends TestCase {
	private DistanceMetric dMetric;
	private BinnedPeakList bp1, bp2, bp3;
	
	private ClusterFeature parentCF, childCF, orphanCF, farOffCF;
	
	private CFNode parentNode,  childNode, orphanNode, farOffNode;

	protected void setUp() throws Exception {
		super.setUp();
		dMetric = DistanceMetric.EUCLIDEAN_SQUARED;
		
		parentNode = new CFNode(null, dMetric);
		parentCF = new ClusterFeature(parentNode, dMetric);
		parentNode.addCF(parentCF);
		childNode = new CFNode(parentCF, dMetric);
		parentCF.updatePointers(childNode, parentNode);
		childCF = new ClusterFeature(childNode, dMetric);
		childNode.addCF(childCF);
		
		bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-210, (float) 1);
		bp1.add(-160, (float) 1);
		bp1.add(-100, (float) 1);
		bp1.add(-30, (float) 1);
		bp1.add(20, (float) 2);
		bp1.add(90, (float) 2);
		bp1.add(120, (float) 2);
		childCF.updateCF(bp1, 1);
		
		orphanNode = new CFNode(null, dMetric);
		orphanCF = new ClusterFeature(orphanNode, dMetric);
		orphanNode.addCF(orphanCF);
		
		bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-210, (float) 1);
		bp2.add(-160, (float) 1);
		bp2.add(-100, (float) 1);
		bp2.add(-30, (float) 1);
		orphanCF.updateCF(bp2, 2);
		
		farOffNode = new CFNode(null, dMetric);
		farOffCF = new ClusterFeature(farOffNode, dMetric);
		
		bp3 = new BinnedPeakList(new Normalizer());
		bp3.add(500, 1);
		bp3.add(501, 1);
		bp3.add(502, 1);
		farOffCF.updateCF(bp3, 3);
		
	}
	public void testSameContents() {
		assert !childNode.sameContents(orphanNode);
		assert !orphanNode.sameContents(childNode);
		orphanCF.getSums().printPeakList();
		orphanCF.setSums(bp1);
		orphanCF.getSums().printPeakList();
		childCF.getSums().printPeakList();
		assert childNode.sameContents(orphanNode);
		assert orphanNode.sameContents(childNode);

	}

	public void testAddCF() {
		ArrayList<ClusterFeature> array = childNode.getCFs();
		assert(array.size() == 1);
		assert(array.get(0) == childCF);
		childNode.addCF(childCF);
		assert(array.size() == 2);
		childNode.addCF(orphanCF);
		assert(array.size() == 3);
		assert(array.get(2) == orphanCF);
		//parents do not update automatically, i'm not sure if this ought to be changed or not
		assert(parentCF.getCount() == 0);
	}

	public void testRemoveCF() {
		ArrayList<ClusterFeature> array = childNode.getCFs();
		assert(array.size() == 1);
		childNode.removeCF(childCF);
		assert(array.size() == 0);
	}

	public void testGetTwoClosest() {
		assert(orphanNode.getTwoClosest() == null);
		childNode.addCF(orphanCF);
		childNode.addCF(farOffCF);
		ClusterFeature[] array = childNode.getTwoClosest();
		assert(array[0] == orphanCF || array[0] == childCF);
		assert(array[1] == orphanCF || array[1] == childCF);
	}

	public void testGetTwoFarthest() {
		assert(orphanNode.getTwoFarthest() == null);
		childNode.addCF(orphanCF);
		childNode.addCF(farOffCF);
		ClusterFeature[] array = childNode.getTwoFarthest();
		assert(array[0] == childCF || array[0] == farOffCF);
		assert(array[1] == childCF || array[1] == farOffCF);
	}

	public void testGetClosest() {
		assert(childNode.getClosest(childCF.getSums()) == childCF);
		assert(childNode.getClosest(farOffCF.getSums()) == childCF);
		assert(childNode.getClosest(orphanCF.getSums()) == childCF);
		
		childNode.addCF(orphanCF);
		assert(childNode.getClosest(childCF.getSums()) == childCF);
		assert(childNode.getClosest(farOffCF.getSums()) == childCF);
		assert(childNode.getClosest(orphanCF.getSums()) == orphanCF);
		
		childNode.addCF(farOffCF);
		assert(childNode.getClosest(childCF.getSums()) == childCF);
		assert(childNode.getClosest(farOffCF.getSums()) == farOffCF);
		assert(childNode.getClosest(orphanCF.getSums()) == orphanCF);
		
	}

	public void testUpdateParent() {
		childNode.updateParent(orphanCF);
		assert(childCF.curNode == childNode);
		assert(childCF.curNode.parentCF == orphanCF);
		assert(childCF.curNode.parentNode == orphanNode);
		assert(childNode.parentCF == orphanCF);
		assert(childNode.parentNode == orphanNode);
		assert(orphanCF.child == childNode);
		assert(orphanCF.curNode == orphanNode);
	}

	public void testIsLeaf() {
		assert(childNode.isLeaf());
		assert(orphanNode.isLeaf());
		assert(farOffNode.isLeaf());
		assert(!parentNode.isLeaf());
	}

}
