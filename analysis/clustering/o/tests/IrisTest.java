package analysis.clustering.o.tests;

import junit.framework.TestCase;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.clustering.o.*;

public class IrisTest extends TestCase implements Partition {
	Partition root;
	DataWithSummary irises;

	protected void setUp() throws Exception {
		super.setUp();
		irisize();
		root = new UndeterminedPartition(this);
	}

	public void testGetLeftChild() {
	}

	public void testGetRightChild() {
	}

	public void testSplit() {
		assertNotNull(irises);
		root.split(irises);
		
		assertNotNull(root);
		assertNotNull(root.getLeftChild());
		assertNotNull(root.getRightChild());
		System.out.println("Root: <" + root.toString() + ">");
		System.out.println("Left child: <" + root.getLeftChild().toString()
				+ ">");
		System.out.println("Right child: <" + root.getRightChild().toString()
				+ ">");
	}

	
	
	
	
	public CollectionDivider getCollectionSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public Partition getLeftChild() {
		// TODO Auto-generated method stub
		return null;
	}

	public Partition getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Partition getRightChild() {
		// TODO Auto-generated method stub
		return null;
	}

	public String rulesDown() {
		// TODO Auto-generated method stub
		return null;
	}

	public String rulesUp() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCollectionSource(CollectionDivider collectionSource) {
		// TODO Auto-generated method stub
		
	}

	public void setParent(Partition parent) {
		// TODO Auto-generated method stub
		
	}

	public int split(DataWithSummary atoms) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean transmogrifyChild(Partition oldChild, Partition newChild) {
		if (root == oldChild) {
			root = newChild;
			return true;
		}
		return false;
	}

	private Float[][] irisData = {
			{5.1f,3.5f,1.4f,0.2f},
			{4.9f,3.0f,1.4f,0.2f},
			{4.7f,3.2f,1.3f,0.2f},
			{4.6f,3.1f,1.5f,0.2f},
			{5.0f,3.6f,1.4f,0.2f},
			{5.4f,3.9f,1.7f,0.4f},
			{4.6f,3.4f,1.4f,0.3f},
			{5.0f,3.4f,1.5f,0.2f},
			{4.4f,2.9f,1.4f,0.2f},
			{4.9f,3.1f,1.5f,0.1f},
			{5.4f,3.7f,1.5f,0.2f},
			{4.8f,3.4f,1.6f,0.2f},
			{4.8f,3.0f,1.4f,0.1f},
			{4.3f,3.0f,1.1f,0.1f},
			{5.8f,4.0f,1.2f,0.2f},
			{5.7f,4.4f,1.5f,0.4f},
			{5.4f,3.9f,1.3f,0.4f},
			{5.1f,3.5f,1.4f,0.3f},
			{5.7f,3.8f,1.7f,0.3f},
			{5.1f,3.8f,1.5f,0.3f},
			{5.4f,3.4f,1.7f,0.2f},
			{5.1f,3.7f,1.5f,0.4f},
			{4.6f,3.6f,1.0f,0.2f},
			{5.1f,3.3f,1.7f,0.5f},
			{4.8f,3.4f,1.9f,0.2f},
			{5.0f,3.0f,1.6f,0.2f},
			{5.0f,3.4f,1.6f,0.4f},
			{5.2f,3.5f,1.5f,0.2f},
			{5.2f,3.4f,1.4f,0.2f},
			{4.7f,3.2f,1.6f,0.2f},
			{4.8f,3.1f,1.6f,0.2f},
			{5.4f,3.4f,1.5f,0.4f},
			{5.2f,4.1f,1.5f,0.1f},
			{5.5f,4.2f,1.4f,0.2f},
			{4.9f,3.1f,1.5f,0.1f},
			{5.0f,3.2f,1.2f,0.2f},
			{5.5f,3.5f,1.3f,0.2f},
			{4.9f,3.1f,1.5f,0.1f},
			{4.4f,3.0f,1.3f,0.2f},
			{5.1f,3.4f,1.5f,0.2f},
			{5.0f,3.5f,1.3f,0.3f},
			{4.5f,2.3f,1.3f,0.3f},
			{4.4f,3.2f,1.3f,0.2f},
			{5.0f,3.5f,1.6f,0.6f},
			{5.1f,3.8f,1.9f,0.4f},
			{4.8f,3.0f,1.4f,0.3f},
			{5.1f,3.8f,1.6f,0.2f},
			{4.6f,3.2f,1.4f,0.2f},
			{5.3f,3.7f,1.5f,0.2f},
			{5.0f,3.3f,1.4f,0.2f},
			{7.0f,3.2f,4.7f,1.4f},
			{6.4f,3.2f,4.5f,1.5f},
			{6.9f,3.1f,4.9f,1.5f},
			{5.5f,2.3f,4.0f,1.3f},
			{6.5f,2.8f,4.6f,1.5f},
			{5.7f,2.8f,4.5f,1.3f},
			{6.3f,3.3f,4.7f,1.6f},
			{4.9f,2.4f,3.3f,1.0f},
			{6.6f,2.9f,4.6f,1.3f},
			{5.2f,2.7f,3.9f,1.4f},
			{5.0f,2.0f,3.5f,1.0f},
			{5.9f,3.0f,4.2f,1.5f},
			{6.0f,2.2f,4.0f,1.0f},
			{6.1f,2.9f,4.7f,1.4f},
			{5.6f,2.9f,3.6f,1.3f},
			{6.7f,3.1f,4.4f,1.4f},
			{5.6f,3.0f,4.5f,1.5f},
			{5.8f,2.7f,4.1f,1.0f},
			{6.2f,2.2f,4.5f,1.5f},
			{5.6f,2.5f,3.9f,1.1f},
			{5.9f,3.2f,4.8f,1.8f},
			{6.1f,2.8f,4.0f,1.3f},
			{6.3f,2.5f,4.9f,1.5f},
			{6.1f,2.8f,4.7f,1.2f},
			{6.4f,2.9f,4.3f,1.3f},
			{6.6f,3.0f,4.4f,1.4f},
			{6.8f,2.8f,4.8f,1.4f},
			{6.7f,3.0f,5.0f,1.7f},
			{6.0f,2.9f,4.5f,1.5f},
			{5.7f,2.6f,3.5f,1.0f},
			{5.5f,2.4f,3.8f,1.1f},
			{5.5f,2.4f,3.7f,1.0f},
			{5.8f,2.7f,3.9f,1.2f},
			{6.0f,2.7f,5.1f,1.6f},
			{5.4f,3.0f,4.5f,1.5f},
			{6.0f,3.4f,4.5f,1.6f},
			{6.7f,3.1f,4.7f,1.5f},
			{6.3f,2.3f,4.4f,1.3f},
			{5.6f,3.0f,4.1f,1.3f},
			{5.5f,2.5f,4.0f,1.3f},
			{5.5f,2.6f,4.4f,1.2f},
			{6.1f,3.0f,4.6f,1.4f},
			{5.8f,2.6f,4.0f,1.2f},
			{5.0f,2.3f,3.3f,1.0f},
			{5.6f,2.7f,4.2f,1.3f},
			{5.7f,3.0f,4.2f,1.2f},
			{5.7f,2.9f,4.2f,1.3f},
			{6.2f,2.9f,4.3f,1.3f},
			{5.1f,2.5f,3.0f,1.1f},
			{5.7f,2.8f,4.1f,1.3f},
			{6.3f,3.3f,6.0f,2.5f},
			{5.8f,2.7f,5.1f,1.9f},
			{7.1f,3.0f,5.9f,2.1f},
			{6.3f,2.9f,5.6f,1.8f},
			{6.5f,3.0f,5.8f,2.2f},
			{7.6f,3.0f,6.6f,2.1f},
			{4.9f,2.5f,4.5f,1.7f},
			{7.3f,2.9f,6.3f,1.8f},
			{6.7f,2.5f,5.8f,1.8f},
			{7.2f,3.6f,6.1f,2.5f},
			{6.5f,3.2f,5.1f,2.0f},
			{6.4f,2.7f,5.3f,1.9f},
			{6.8f,3.0f,5.5f,2.1f},
			{5.7f,2.5f,5.0f,2.0f},
			{5.8f,2.8f,5.1f,2.4f},
			{6.4f,3.2f,5.3f,2.3f},
			{6.5f,3.0f,5.5f,1.8f},
			{7.7f,3.8f,6.7f,2.2f},
			{7.7f,2.6f,6.9f,2.3f},
			{6.0f,2.2f,5.0f,1.5f},
			{6.9f,3.2f,5.7f,2.3f},
			{5.6f,2.8f,4.9f,2.0f},
			{7.7f,2.8f,6.7f,2.0f},
			{6.3f,2.7f,4.9f,1.8f},
			{6.7f,3.3f,5.7f,2.1f},
			{7.2f,3.2f,6.0f,1.8f},
			{6.2f,2.8f,4.8f,1.8f},
			{6.1f,3.0f,4.9f,1.8f},
			{6.4f,2.8f,5.6f,2.1f},
			{7.2f,3.0f,5.8f,1.6f},
			{7.4f,2.8f,6.1f,1.9f},
			{7.9f,3.8f,6.4f,2.0f},
			{6.4f,2.8f,5.6f,2.2f},
			{6.3f,2.8f,5.1f,1.5f},
			{6.1f,2.6f,5.6f,1.4f},
			{7.7f,3.0f,6.1f,2.3f},
			{6.3f,3.4f,5.6f,2.4f},
			{6.4f,3.1f,5.5f,1.8f},
			{6.0f,3.0f,4.8f,1.8f},
			{6.9f,3.1f,5.4f,2.1f},
			{6.7f,3.1f,5.6f,2.4f},
			{6.9f,3.1f,5.1f,2.3f},
			{5.8f,2.7f,5.1f,1.9f},
			{6.8f,3.2f,5.9f,2.3f},
			{6.7f,3.3f,5.7f,2.5f},
			{6.7f,3.0f,5.2f,2.3f},
			{6.3f,2.5f,5.0f,1.9f},
			{6.5f,3.0f,5.2f,2.0f},
			{6.2f,3.4f,5.4f,2.3f},
			{5.9f,3.0f,5.1f,1.8f},
				};
	
	private void irisize() {
		// slurp the raw array into a list of BPLs.
		BinnedPeakList temp;
		irises = new DataWithSummary();
		for (int iris = 0; iris < irisData.length; iris++) {
			temp = new BinnedPeakList();
			for (int dim = 0; dim < 4; dim++) {
				temp.add(dim, irisData[iris][dim]);
			}
			irises.add(temp);
		}

	}
}