package analysis.clustering.BIRCH;

import collection.Collection;
import ATOFMS.ParticleInfo;
import analysis.DistanceMetric;
import analysis.clustering.Cluster;
import database.InfoWarehouse;
import database.NonZeroCursor;
import database.SQLServerDatabase;

public class BIRCH extends Cluster{
	private int branchingFactor;
	private int leafFactor;
	//private int pageSize;
	//private int availableMemory;
	private int maxNodes;
	private InfoWarehouse db;
	private int collectionID;
	private CFTree curTree;
	
	public BIRCH(int cID, InfoWarehouse database, String name, String comment) 
	{
		super(cID, database,name,comment);
		// set parameters
		branchingFactor = 5;
		leafFactor = 5;
		maxNodes = 15;
		collectionID = cID;
		db = database;
	}
	
	public void buildTree(float threshold) {
		curTree = new CFTree(threshold, branchingFactor,leafFactor); 		
		ParticleInfo particle;
		CFNode changedNode, lastSplitNode;
		while(curs.next()) {
			particle = curs.getCurrent();
			changedNode = curTree.insertEntry(normalize(particle.getBinnedList()), particle.getID());
			lastSplitNode = curTree.splitNodeIfPossible(changedNode);
			if (!lastSplitNode.isLeaf() || !changedNode.equals(lastSplitNode))
				curTree.refineMerge(lastSplitNode);
			//if (curTree.countNodes() >= maxNodes) {
			//	rebuildTree();
			//}
		}
		
		curs.reset();
	}
	
	public void rebuildTree() {
		
	}
	
	public void clusterLeaves() {
		
	}
	
	public void refineClusters() {
		
	}

	/**
	 * 
	 * From clusterK; this could be put in Cluster class.
	 * Sets the distance metric.  If using K-Means, the distance metric will always
	 * be Euclidean Squared, since it is guaranteed to decrease.  If using K-Medians, the
	 * distance metric will always be City Block, since it is guaranteed to decrease.
	 * 
	 * (non-Javadoc)
	 * @see analysis.clustering.Cluster#setDistancMetric(int)
	 */
	public boolean setDistanceMetric(DistanceMetric method) {
		distanceMetric = method;
		if (method == DistanceMetric.CITY_BLOCK)
			return true;
		else if (method == DistanceMetric.EUCLIDEAN_SQUARED)
			return true;
		else if (method == DistanceMetric.DOT_PRODUCT)
			return true;
		else
		{
			throw new IllegalArgumentException("Illegal distance metric.");
		}
	}

	@Override
	// should all be in memory; that's the point of scalable clustering.
	public boolean setCursorType(int type) {
		Collection collection = db.getCollection(collectionID);
		curs = new NonZeroCursor(db.getMemoryBinnedCursor(collection));
		return true;
	}

	@Override
	public int divide() {
		System.out.println("Accidentally dividing!");
		return 0;
	}
	
	public static void main(String[] args) {
		InfoWarehouse db = new SQLServerDatabase("SpASMSdb");
		db.openConnection();
		BIRCH birch = new BIRCH(12, db, 
				"BIRCH", "comment");
		birch.setCursorType(0);
		birch.setDistanceMetric(DistanceMetric.CITY_BLOCK);	
		birch.buildTree(1.0f);
		
		System.out.println("printing leaves");
		birch.curTree.printLeaves(birch.curTree.getFirstLeaf(birch.curTree.root));
		System.out.println("-------------------------------");
		birch.curTree.printTree();
		System.out.println("-------------------------------");
		int[] info = birch.curTree.countNodes();
		System.out.println();
		System.out.println("# of nodes: " + info[0]);
		System.out.println("# of leaves: " + info[1]);
		System.out.println("# of subclusters: " + info[2]);
		System.out.println("# of grouped subclusters: " + info[3]);
		System.out.println("# of particles represented: " + info[4]);
	}
	
	
	
}
