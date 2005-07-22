package analysis.clustering.BIRCH;

import java.util.ArrayList;

import collection.Collection;
import ATOFMS.ParticleInfo;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.clustering.Cluster;
import database.InfoWarehouse;
import database.NonZeroCursor;
import database.SQLServerDatabase;

public class BIRCH extends Cluster{
	private int branchingFactor;
	private int maxNodes;
	private InfoWarehouse db;
	private int collectionID;
	private CFTree curTree;
	
	public BIRCH(int cID, InfoWarehouse database, String name, String comment) 
	{
		super(cID, database,name,comment);
		// set parameters
		branchingFactor = 3;
		maxNodes = 15;
		collectionID = cID;
		db = database;
	}
	
	public void buildTree(float threshold) {
		curTree = new CFTree(threshold, branchingFactor); 		
		ParticleInfo particle;
		CFNode changedNode, lastSplitNode;
		while(curs.next()) {
			particle = curs.getCurrent();
			particle.getBinnedList().normalize(distanceMetric);
			changedNode = curTree.insertEntry(particle.getBinnedList(), particle.getID());
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
		float newThreshold = curTree.nextSimpleThreshold();
		CFTree newTree = new CFTree(newThreshold, branchingFactor);
		CFNode nextLeaf = curTree.getFirstLeaf(curTree.root);
		CFNode newParent = null;
		while (nextLeaf != null) {
			if (!nextLeaf.parentNode.equals(newParent)) {
				newParent = nextLeaf.parentNode;
			ArrayList<Integer> path = getNextPath(nextLeaf);
			newTree.insertEmptyPath(curTree.threshold, path);
			}
			nextLeaf = nextLeaf.nextLeaf;
		}
		curTree = newTree;
	}
	
	public ArrayList<Integer> getNextPath(CFNode curNode) {
		curTree.printTree();
		System.out.println("curNode: " + curNode);
		ArrayList<Integer> indices = new ArrayList<Integer>();
		indices.add(new Integer(0));
		while (curNode.parentNode != null) {
			int index = curNode.parentNode.getCFs().indexOf(curNode.parentCF);
			System.out.println("parentNode: " + curNode.parentNode);
			System.out.println("parentNodeSize: " + curNode.parentNode.getSize());
			for (int i = 0; i < curNode.parentNode.getCFs().size(); i++)
				System.out.println(curNode.parentNode.getCFs().get(i));
			System.out.println();
			System.out.println("looking for cf: " + curNode.parentCF);
			indices.add(0, new Integer(index));
			System.out.println(index);
			curNode = curNode.parentNode;
		}
		return indices;
	}
	
	public void clusterLeaves() {
		
	}
	
	public void refineClusters() {
		
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
		BIRCH birch = new BIRCH(3, db, "BIRCH", "comment");
		birch.setCursorType(0);
		birch.setDistanceMetric(DistanceMetric.CITY_BLOCK);	
		birch.buildTree(1.75f);
		System.out.println("-------------------------------");
		birch.curTree.printTree();
		System.out.println("-------------------------------");
		birch.curTree.countNodes();
	
		System.out.println("nextThreshold: " + birch.curTree.nextSimpleThreshold());
		
		birch.rebuildTree();
		System.out.println("-------------------------------");
		birch.curTree.printTree();
		System.out.println("-------------------------------");
		birch.curTree.countNodes();
	
	}	
}
