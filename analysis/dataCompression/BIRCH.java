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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import collection.Collection;
import ATOFMS.ParticleInfo;
import analysis.*;
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
	private int branchingFactor; // Number of cluster features each node is allowed to have.
	private int collectionID;
	private CFTree curTree;
	// start and end measure the time it takes
	// to build the tree and rebuild the tree
	// realStart and realEnd measure the total time
	private long start, end, realStart, realEnd;
	private long buildTotal = 0; 
	private long rebuildTotal = 0;
	private int rebuildCount;
	
	/**
	 * MEM_THRESHOLD is a new way of calculating memory predictably.  This threshold
	 * should ultimately be the max threshold of the system that Enchilada is being
	 * run on, but for now I manually set it to test smaller datasets.  I think, for example.
	 * that running Tester works well if MEM_THRESHOLD = 1000.
	 * 
	 * This should be an advanced option that the user can change in the GUI.
	 * 
	 * memory is in bytes - janara
	 */
	
	//1000 for test data, 150118 for i, 54857600
	private final float MEM_THRESHOLD = 150118;
	
	/*
	 * Constructor.  Calls the CompressData Class's constructor.
	 */
	public BIRCH(Collection c, InfoWarehouse database, String name, String comment, DistanceMetric d) 
	{
		super(c,database,name,comment,d);
		// set parameters.
		branchingFactor = 4; // should be an advanced option in the GUI
		collectionID = oldCollection.getCollectionID();
		assignCursorType();
		rebuildCount = 0; 
	}
	
	/**
	 * Builds the tree in memory. Inserts the particles one at a time, 
	 * increasing the threshold if we run out of memory.
	 * @param threshold - initial threshold for tree.
	 */
	public void buildTree(float threshold) {
		System.out.println("\n**********BUILDING THE PRELIMINARY TREE**********");
		start = new Date().getTime();
		realStart = new Date().getTime();
		curTree = new CFTree(threshold, branchingFactor, distanceMetric); 
		// Insert particles one by one.
		ParticleInfo particle;
		CFNode changedNode, lastSplitNode;

		while(curs.next()) {
			particle = curs.getCurrent();
			System.out.println("inserting particle " + particle.getID());
			particle.getBinnedList().normalize(distanceMetric);
			assert!particle.getBinnedList().containsZeros() : "zero present";
			
			// insert the entry
			changedNode = curTree.insertEntry(particle.getBinnedList(), particle.getID());	
			// if it's possible to split the node, do so.
			lastSplitNode = curTree.splitNodeIfPossible(changedNode);
			// If there has been a split above the leaves, refine the split
			if (!lastSplitNode.isLeaf() || 
					!changedNode.equals(lastSplitNode)) {
				curTree.refineMerge(lastSplitNode);
			}	
			
		//	curTree.printTree();
			// if we have run out of memory, rebuild the tree.
			if (curTree.getMemory()> MEM_THRESHOLD) {
				end = new Date().getTime();
				buildTotal = buildTotal + (end-start);
				start = new Date().getTime();
				int[] counts = {0,0,0,0,0};
				int leafNum = curTree.countNodesRecurse(curTree.root,counts)[1];
				
				/**
				 * If there is only one leaf node, then that means that there is only
				 * enough memory to clump all particles into one collection, therefore
				 * making BIRCH pointless.
				 */
				if (curTree.threshold >= 2 && leafNum == 1) {
					System.err.println("Out of memory at max threshold with 1 " +
							"leaf in tree.\nAll points will be in the same leaf," +
							" so the clustering is pointless.\nExiting.");
					System.exit(0);
				}
				rebuildCount++;
				System.out.println("OUT OF MEMORY @ "+ curTree.getMemory() + "\n");
				curTree.countNodes();
				
				System.out.println("\nFINAL TREE: ");
				curTree.printTree();
				System.out.println("*****************REBUILDING TREE*****************\n");
			//	System.out.println("scanning distances...");
			//	curTree.scanDistances();
				// rebuild tree.
				rebuildTree();
				end = new Date().getTime();
				rebuildTotal = rebuildTotal + (end-start);
				start = new Date().getTime();
				curTree.countNodes();
			}
		}	
		end = new Date().getTime();
		realEnd = new Date().getTime();
		buildTotal = buildTotal + (end-start);
		System.out.println("\nFINAL TREE:");
		curTree.printTree();
		System.out.println("scanning distances...");
		curTree.scanAllNodes();
		System.out.println("interval: " + (realEnd-realStart));
		System.out.println("buildTotal : " + buildTotal);
		System.out.println("rebuildtotal : " + rebuildTotal);
		System.out.println("rebuildCount : " + rebuildCount);
	}
	
	/**
	 * Rebuilds the tree if we run out of memory.  Calls rebuildTreeRecurse,
	 * then removes all the empty nodes in the new tree.  Sets the current
	 * tree to the new one at the end of the method.
	 */
	public void rebuildTree() {
		// predict the next best threshold.
		float newThreshold = curTree.nextThreshold();
		System.out.println("new Threshold: " + newThreshold);
		
		CFTree newTree = new CFTree(newThreshold, branchingFactor, distanceMetric);
		newTree = rebuildTreeRecurse(newTree, newTree.root, curTree.root, null);
		
		newTree.assignLeaves();
		
		// remove all the nodes with count = 0;
		CFNode curLeaf = newTree.getFirstLeaf();
		
		while (curLeaf != null) {
			// TODO: make this more elegant
			if (curLeaf.getSize() == 0 || 
					(curLeaf.getSize() == 1 && curLeaf.getCFs().get(0).getCount() == 0)) {
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
		newTree.assignLeaves();
	
		//if it's a leaf, we want to insert each cluster feature
		//into the new tree, merging as many cluster features
		//as possible
		if (oldCurNode.isLeaf()) {
			if (lastLeaf == null)
				lastLeaf = newTree.getFirstLeaf();
			else if (lastLeaf.nextLeaf == null)
				lastLeaf.nextLeaf = newCurNode;
			
			// reinsert leaf;
			boolean reinserted;
			for (int i = 0; i < oldCurNode.getSize(); i++) {
				System.out.println("\nAdding CF:");
				ClusterFeature thisCF = oldCurNode.getCFs().get(i);
				thisCF.printCF("");
				//try to reinsert the cf
				reinserted = newTree.reinsertEntry(thisCF);
				
				//if reinserting it would have resulted in too many cfs for that node
				if (!reinserted) {
					//make a new cluster feature and add it to newCurNode
					ClusterFeature newLeaf = new ClusterFeature(
							newCurNode, thisCF.getCount(), thisCF.getSums(), 
							thisCF.getSumOfSquares(), thisCF.getAtomIDs());				
					newCurNode.addCF(newLeaf);
					//update everything
					newTree.updateNonSplitPath(newLeaf.curNode);
					for (int j = 0; j < newLeaf.curNode.getCFs().size(); j++){
						newLeaf.curNode.getCFs().get(j).updateCF();
					}
				}
			//	newTree.printTree();
			//	System.out.println("scanning distances");
			//	newTree.scanAllNodes();
			}
		}
		//if it's not a leaf, we just want to build the correct path
		else {
			for (int i = 0; i < oldCurNode.getSize(); i++) {
				System.out.println("adding an empty cluster feature to build the path for");
				oldCurNode.getCFs().get(i).printCF("");
				
				//keep making empty cfs till there are the same number in newCurNode as in oldCurNode
				while (newCurNode.getSize() <= i) {
					ClusterFeature newCF = new ClusterFeature(newCurNode, newCurNode.dMetric);
					newCurNode.addCF(newCF);
				}
				//if the child of that cf is null, make a new child and update the pointers
				if (newCurNode.getCFs().get(i).child == null) {
					CFNode newChild = new CFNode(newCurNode.getCFs().get(i), distanceMetric);
					newCurNode.getCFs().get(i).updatePointers(
							newChild, newCurNode);
				}
			//	newTree.printTree();
				//rebuild the tree using the child as the new node
				rebuildTreeRecurse(newTree, newCurNode.getCFs().get(i).child, 
						oldCurNode.getCFs().get(i).child, lastLeaf);
			}
		}
		oldCurNode = null;
		return newTree;
	}
	//THIS IS GOING TO BE SUPER SLOW--FIX THIS
/*	public void getError() {
		Collection collection = db.getCollection(collectionID);
		curs = new NonZeroCursor(db.getBinnedCursor(collection));
		ParticleInfo particle;
		float totalError = 0;
		int numParticles = 0;
		while(curs.next()) {
			particle = curs.getCurrent();
			System.out.println("inserting particle " + particle.getID());
			particle.getBinnedList().normalize(distanceMetric);
			totalError = totalError + curTree.getError(particle);
			numParticles ++;
		}
		totalError = totalError/numParticles;
	}*/
	/**
	 * @Override
	 * 
	 * sets the cursor type to memory binned cursor, since the whole point
	 * of this algorithm is that it's in memory.
	 */
	public boolean assignCursorType() {
		Collection collection = db.getCollection(collectionID);
		curs = new NonZeroCursor(db.getBinnedCursor(collection));
		return true;
	}

	@Override
	public void compress() {	
		buildTree(0.0f);
		System.out.println();
		curTree.countNodes();
		putCollectionInDB();
	}

	@Override
	/**
	 * This method is for ATOFMS particles only.  The SQL stmts. are MUCH
	 * easier to write, and we're not looking to generalize this method until
	 * we've restructured the db to accomodate generalizations. - AR
	 */
	protected void putCollectionInDB() {	
		// Create new collection and dataset:
		ArrayList<ArrayList<Integer>> centroidList = new ArrayList<ArrayList<Integer>>();
		String compressedParams = getDatasetParams(oldDatatype);
		int[] IDs = db.createEmptyCollectionAndDataset(newDatatype,0,name,comment,""); 
		int newCollectionID = IDs[0];
		int newDatasetID = IDs[1];
		
		// insert each CF as a new atom.
		int atomID;
		Collection collection  = new Collection(newDatatype, newCollectionID, db);
		
		CFNode leaf = curTree.getFirstLeaf();
		ArrayList<ClusterFeature> curCF;
		ArrayList<Integer> curIDs;
		ArrayList<String> sparseArray = new ArrayList<String>();
		ArrayList<Integer> cfAtomIDs;
		// Enter the CFS of each leaf
		while (leaf != null) {
			curCF = leaf.getCFs();
			for (int i = 0; i < curCF.size(); i++) {
				cfAtomIDs = curCF.get(i).getAtomIDs();
				centroidList.add(cfAtomIDs);
				
				atomID = db.getNextID();
				
				// create denseAtomInfo string.
				String denseStr = "";
				curIDs = curCF.get(i).getAtomIDs();
				ArrayList<String> denseNames = db.getColNames(newDatatype, DynamicTable.AtomInfoDense);
				
				// start at 1 to skip AtomID column.
				for (int j = 1; j < denseNames.size()-1; j++) {
					denseStr += db.aggregateColumn(DynamicTable.AtomInfoDense,denseNames.get(j),curIDs,oldDatatype);
					denseStr += ", ";
				}
				denseStr+=curCF.get(i).getCount();
				
				// create sparseAtomInfo string arraylist.
				sparseArray = new ArrayList<String>();
				Iterator iter = curCF.get(i).getSums().iterator();
				while (iter.hasNext()) {
					BinnedPeak p=(BinnedPeak) iter.next();
					sparseArray.add(p.key + "," + 
							p.value + "," + p.value + 
							"," + 0);
				}				
				
				//insert particle
				db.insertParticle(denseStr,sparseArray,collection,newDatasetID,atomID);
			}
			leaf=leaf.nextLeaf;
		}
		
		ArrayList<String> list = new ArrayList<String>();
		db.updateInternalAtomOrder(collection);

		for (int i = 0; i<centroidList.size(); i++) {
			
			Integer j = new Integer(i);
			System.out.println("Set: " + i);
			for (int k = 0; k<centroidList.get(i).size(); k++) {
				try {
					Statement stmt = db.getCon().createStatement();
					
					String query = "SELECT *\n" +
					" FROM ATOFMSAtomInfoDense" +
					" WHERE AtomID = " + centroidList.get(i).get(k);
					ResultSet rs = stmt.executeQuery(query);
					rs.next();
					System.out.println("AtomID: " + centroidList.get(i).get(k) + " file: " + rs.getString(6));
					stmt.close();
				} catch (SQLException e) {
					System.err.println("Exception creating the dataset entries:");
					e.printStackTrace();
				}
				
			}
		}
		System.out.println("Done inserting BIRCH into DB.");
	}
}
