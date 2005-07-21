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
	
	public void rebuildSimpleTree() {
		curTree.nextSimpleThreshold();
		CFNode curLeaf = curTree.getFirstLeaf(curTree.root);
		while (curLeaf != null) {
				ArrayList<ClusterFeature> cfs = curLeaf.getCFs();
				for (int i = 0; i < cfs.size(); i++) {
					ClusterFeature list = cfs.get(i);
					for (int j = 0; j < i; j++) {
						if (list.getSums().getDistance(cfs.get(j).getSums(), 
								DistanceMetric.CITY_BLOCK) < curTree.threshold) {
							ClusterFeature merged = curTree.mergeEntries(cfs.get(i),cfs.get(j));
							break;
						}
					}
			}
			curLeaf = curLeaf.nextLeaf;
		}
		curLeaf = curTree.getFirstLeaf(curTree.root);
		while (curLeaf != null) {
			curTree.updateNonSplitPath(curLeaf);
			curLeaf = curLeaf.nextLeaf;
		}
	}
	
	// TODO: get a null pointer exception here.
	public void rebuildTreeFromPaper() {
		CFTree oldTree = curTree;
		CFTree newTree = new CFTree(curTree.nextSimpleThreshold(), branchingFactor,leafFactor);
		CFNode oldNode, newNode = null;
		oldNode = oldTree.getFirstLeaf(oldTree.root);
		newNode = new CFNode(oldNode.parentCF);
		while (oldNode != null) {
			// put entries in leaf:
			ArrayList<ClusterFeature> oldCFs = oldNode.getCFs();
			ClusterFeature closestCF;
			for (int i = 0; i < oldCFs.size(); i++) {
				closestCF = newTree.findClosestLeafEntry(oldCFs.get(i).getSums(), newNode);
				newNode.addCF(oldCFs.get(i));
				if (closestCF != null &&
						closestCF.getSums().getDistance(oldCFs.get(i).getSums(), 
								DistanceMetric.CITY_BLOCK) > newTree.threshold) {
					ClusterFeature merge = newTree.mergeEntries(closestCF,oldCFs.get(i));
					newTree.updateNonSplitPath(merge.curNode);
				}
				
			}
		}
		//newTree.printTree();
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
		BIRCH birch = new BIRCH(2, db, "BIRCH", "comment");
		birch.setCursorType(0);
		birch.setDistanceMetric(DistanceMetric.CITY_BLOCK);	
		birch.buildTree(0.75f);
		System.out.println("-------------------------------");
		birch.curTree.printTree();
		System.out.println("-------------------------------");
		int[] info = birch.curTree.countNodes();
		System.out.println();
		System.out.println("threshold: " + birch.curTree.threshold);
		System.out.println("# of nodes: " + info[0]);
		System.out.println("# of leaves: " + info[1]);
		System.out.println("# of subclusters: " + info[2]);
		System.out.println("# of grouped subclusters: " + info[3]);
		System.out.println("# of particles represented: " + info[4]);

		/**
		birch.rebuildSimpleTree();
		//System.out.println("-------------------------------");
		//birch.curTree.printTree();
		System.out.println("-------------------------------");
		info = birch.curTree.countNodes();
		System.out.println();
		System.out.println("threshold: " + birch.curTree.threshold);
		System.out.println("# of nodes: " + info[0]);
		System.out.println("# of leaves: " + info[1]);
		System.out.println("# of subclusters: " + info[2]);
		System.out.println("# of grouped subclusters: " + info[3]);
		System.out.println("# of particles represented: " + info[4]);
	
		birch.rebuildSimpleTree();
		//System.out.println("-------------------------------");
		//birch.curTree.printTree();
		System.out.println("-------------------------------");
		info = birch.curTree.countNodes();
		System.out.println();
		System.out.println("threshold: " + birch.curTree.threshold);
		System.out.println("# of nodes: " + info[0]);
		System.out.println("# of leaves: " + info[1]);
		System.out.println("# of subclusters: " + info[2]);
		System.out.println("# of grouped subclusters: " + info[3]);
		System.out.println("# of particles represented: " + info[4]);
		
		birch.rebuildSimpleTree();
		//System.out.println("-------------------------------");
		//birch.curTree.printTree();
		System.out.println("-------------------------------");
		info = birch.curTree.countNodes();
		System.out.println();
		System.out.println("threshold: " + birch.curTree.threshold);
		System.out.println("# of nodes: " + info[0]);
		System.out.println("# of leaves: " + info[1]);
		System.out.println("# of subclusters: " + info[2]);
		System.out.println("# of grouped subclusters: " + info[3]);
		System.out.println("# of particles represented: " + info[4]);
*/
	}	
}
