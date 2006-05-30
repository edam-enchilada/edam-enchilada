/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's BIRCH class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package analysis.dataCompression;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Date;

import collection.Collection;
import ATOFMS.ParticleInfo;
import analysis.*;
import analysis.clustering.*;
import database.*;

/**
 * BIRCH is a scalable clustering algorithm that fits all the particles into
 * the largest height-balanced tree that fits in memory, using summary 
 * information about particles to create cluster feature.  These cluster
 * features are then clustered to produced more refined clusters.
 * 
 * @author ritza
 *
 */
public class BIRCH extends CompressData{
	/* Class Variables */
	private int branchingFactor;
	private int collectionID;
	private CFTree curTree;
	private MemoryUsage mem;
	private long start, end;
	private final float MEM_THRESHOLD = 1100;

	
	/*
	 * Constructor.  Calls the CompressData Class's constructor.
	 */
	public BIRCH(Collection c, InfoWarehouse database, String name, String comment, boolean n, DistanceMetric d) 
	{
		super(c,database,name,comment, n, d);
		// set parameters.
		branchingFactor = 4;
		collectionID = oldCollection.getCollectionID();
		setCursorType(0);
	}
	
	/**
	 * Builds the tree in memory. Inserts the particles one at a time, 
	 * increasing the threshold if we run out of memory.
	 * @param threshold - initial threshold for tree.
	 */
	public void buildTree(float threshold) {
		curTree = new CFTree(threshold, branchingFactor, distanceMetric); 
		ParticleInfo particle;
		CFNode changedNode, lastSplitNode;
		// Insert particles one by one.
		start = new Date().getTime();
		while(curs.next()) {
			particle = curs.getCurrent();
			particle.getBinnedList().normalize(distanceMetric);
			
			changedNode = curTree.insertEntry(particle.getBinnedList(), particle.getID());
			
			lastSplitNode = curTree.splitNodeIfPossible(changedNode);
	
			// If there has been a split above the leaves, refine the
			// split
			if (!lastSplitNode.isLeaf() || 
					!changedNode.equals(lastSplitNode)) {
				curTree.refineMerge(lastSplitNode);
			}	
			
			if (curTree.getMemory()> MEM_THRESHOLD) {
				int[] counts = {0,0,0,0,0};
				int leafNum = curTree.countNodesRecurse(curTree.root,counts)[1];
				if (curTree.threshold >= 2 && leafNum == 1) {
					System.err.println("Out of memory at max threshold with 1 " +
							"leaf in tree.\nAll points will be in the same leaf," +
							" so the clustering is pointless.\nExiting.");
					System.exit(0);
				}
			
				curTree.countNodes();
				System.out.println("out of memory @ "+
						curTree.getMemory()+": rebuilding tree");
				rebuildTree();
				curTree.countNodes();

			}
			System.gc();
		}	
		end = new Date().getTime();
		System.out.println("interval: " + (end-start));
	}
	
	/**
	 * Rebuilds the tree if we run out of memory.  Calls rebuildTreeRecurse,
	 * then removes all the empty nodes in the new tree.  Sets the current
	 * tree to the new one at the end of the method.
	 */
	public void rebuildTree() {
		float newThreshold = curTree.nextThreshold();
		System.out.println("new Threshold: " + newThreshold);
		CFTree newTree = new CFTree(newThreshold, branchingFactor, distanceMetric);
		newTree = rebuildTreeRecurse(newTree, newTree.root, curTree.root, null);
		// remove all the nodes with count = 0;
		newTree.assignLeaves();
		CFNode curLeaf = newTree.getFirstLeaf();
		while (curLeaf != null) {
			// TODO: make this more elegant
			if (curLeaf.getSize() == 0 || 
					(curLeaf.getSize() == 1 && 
							curLeaf.getCFs().get(0).getCount() == 0)) {
				CFNode emptyNode = curLeaf;
				ClusterFeature emptyCF;
				while (emptyNode.parentCF != null && emptyNode.parentCF.getCount() == 0) {
					emptyNode.parentNode.removeCF(emptyNode.parentCF);
					emptyNode = emptyNode.parentNode;
				}
			}
			curLeaf = curLeaf.nextLeaf;
		}
		newTree.numDataPoints = curTree.numDataPoints;
		newTree.assignLeaves();
		
		// reassign memory allocation to each node.
		newTree.findTreeMemory(newTree.root, true);
		curTree = newTree;
	}
	
	/**
	 * Recursive method for rebuilding the tree.  Returns the new tree.
	 * @param newTree - new tree that's being built
	 * @param newCurNode - current node in the new tree
	 * @param oldCurNode - corresponding node in the old tree
	 * @param lastLeaf - most recent leaf
	 * @return the final new tree.
	 */
	public CFTree rebuildTreeRecurse(CFTree newTree, CFNode newCurNode, 
			CFNode oldCurNode, CFNode lastLeaf) {
		//newTree.printTree();
		newTree.assignLeaves();
		// if this is the first entry, build the path.
		if (oldCurNode.isLeaf()) {
			if (lastLeaf == null)
				lastLeaf = newTree.getFirstLeaf();
			else if (lastLeaf.nextLeaf == null)
				lastLeaf.nextLeaf = newCurNode;
			// reinsert leaf;
			boolean reinserted;
			for (int i = 0; i < oldCurNode.getSize(); i++) {
				ClusterFeature thisCF = oldCurNode.getCFs().get(i);
				reinserted = newTree.reinsertEntry(thisCF);
				if (!reinserted) {
					ClusterFeature newLeaf = new ClusterFeature(
							newCurNode, thisCF.getCount(), thisCF.getSums(), 
							thisCF.getSumOfSquares(), thisCF.getAtomIDs());				
					newCurNode.addCF(newLeaf);
					newTree.updateNonSplitPath(newLeaf.curNode);
				}
			}
		}
		else {
			for (int i = 0; i < oldCurNode.getSize(); i++) {
				while (newCurNode.getSize() <= i) {
					ClusterFeature newCF = new ClusterFeature(newCurNode, newCurNode.dMetric);
					newCurNode.addCF(newCF);
				}
				if (newCurNode.getCFs().get(i).child == null) {
					CFNode newChild = new CFNode(newCurNode.getCFs().get(i), distanceMetric);
					newCurNode.getCFs().get(i).updatePointers(
							newChild, newCurNode);
				}
				rebuildTreeRecurse(newTree, newCurNode.getCFs().get(i).child, 
						oldCurNode.getCFs().get(i).child, lastLeaf);
			}
		}
		oldCurNode = null;
		return newTree;
	}
	
	/**
	 * @Override
	 * 
	 * sets the cursor type to memory binned cursor, since the whole point
	 * of this algorithm is that it's in memory.
	 */
	public boolean setCursorType(int type) {
		Collection collection = db.getCollection(collectionID);
		curs = new NonZeroCursor(db.getBinnedCursor(collection));
		return true;
	}

	@Override
	public void compress() {	
		buildTree(0.0f);
		System.out.println();
		System.out.println("FINAL TREE:");
		curTree.countNodes();
	}

	@Override
	/**
	 * This method is for ATOFMS particles only.  The SQL stmts. are MUCH
	 * easier to write, and we're not looking to generalize this method until
	 * we've restructured the db to accomodate generalizations. - AR
	 */
	protected void putCollectionInDB() {	
		// Create new collection and dataset:
		String compressedParams = getDatasetParams(oldDatatype);
		int[] IDs = db.createEmptyCollectionAndDataset(newDatatype,0,name,comment,compressedParams); 
		int newCollectionID = IDs[0];
		int newDatasetID = IDs[1];
		
		// insert each CF as a new atom.
		int atomID;
		Collection collection;
		CFNode leaf = curTree.getFirstLeaf();
		ArrayList<ClusterFeature> curCF;
		ArrayList<Integer> curIDs;
		while (leaf != null) {
			curCF = leaf.getCFs();
			for (int i = 0; i < curCF.size(); i++) {
				atomID = db.getNextID();
				collection = new Collection(newDatatype, newCollectionID, db);
				// create denseAtomInfo string.
				String denseStr = "";
				curIDs = curCF.get(i).getAtomIDs();
				ArrayList<String> denseNames = db.getColNames(newDatatype, DynamicTable.AtomInfoDense);
				// start at 1 to skip AtomID column.
				//for (int j = 1; j < denseNames.size(); j++) {
					//denseStr += db.aggregateColumn(DynamicTable.AtomInfoDense,denseNames.get(j),curIDs,oldDatatype);
					denseStr += ", ";
				//}
				denseStr.substring(0,denseStr.length()-3);
				// create sparseAtomInfo string arraylist.
				ArrayList<String> sparseArray = new ArrayList<String>();
				
				//insert particle
				db.insertParticle(denseStr,sparseArray,collection,newDatasetID,atomID);
			}
		}
	}
}
