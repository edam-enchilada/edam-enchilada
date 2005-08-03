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
 * The Original Code is EDAM Enchilada's CFTree class.
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

package analysis.clustering.BIRCH;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;

/**
 * 
 * @author ritza
 *
 * CFTree is a representation of all the data for clustering compressed in
 * such a way that all of it is stored in a tree structure in memory.  
 */
public class CFTree {
	/* Class Variables */
	// TODO: should dist < threshold or should dist <= threshold?
	public float threshold;
	private int branchFactor;
	public CFNode root;
	public int numDataPoints = 0;
	private CFNode prev = null;
	private CFNode firstLeaf = null;
	
	private ClusterFeature recentlySplitA = null;
	private ClusterFeature recentlySplitB = null;
	
	/**
	 * Constructor.  
	 * @param t - threshold for the tree
	 * @param b - branching factor for the tree
	 */
	public CFTree(float t, int b) {
		threshold = t;
		branchFactor = b;
		root = new CFNode(null); // initialize the root
	}
	
	/**
	 * Inserts a particle into the tree.
	 * @param entry - binned peak list of next particle
	 * @param atomID - particle's corresponding atomID (testing purposes)
	 * @return node that particle is inserted into.
	 */
	public CFNode insertEntry(BinnedPeakList entry) {
 		numDataPoints++;
		System.out.println("inserting particle # " + numDataPoints);
		// If this is the first entry, make it the root.
		if (root.getSize() == 0) {
			ClusterFeature firstCF = new ClusterFeature(root);
			firstCF.updateCF(entry);
			root.addCF(firstCF);
			return root;
		}
		ClusterFeature closestLeaf = findClosestLeafEntry(entry,root);
		CFNode closestNode = closestLeaf.curNode;
		// if distance is below the threshold, CF absorbs the new entry.
		if (closestLeaf.getCentroid().getDistance(
				entry, DistanceMetric.CITY_BLOCK) <= threshold) 
			closestLeaf.updateCF(entry);
		// else, add it to the closestNode as a new CF.
		else {
			ClusterFeature newEntry;
			if (closestNode.parentNode == null)
				newEntry = new ClusterFeature(null);
			else
				newEntry = new ClusterFeature(closestNode.parentNode);
			newEntry.updateCF(entry);
			newEntry.updatePointers(null, closestNode);
			closestNode.addCF(newEntry);
		}
		updateNonSplitPath(closestNode);
		return closestNode;
		
	}
	
	/**
	 * Method for rebuilding the tree; doesn't add if it results in a split.
	 * @param cf - cluster feature to reinsert.
	 * @return - true if CF has been reinserted, false if it hasn't.
	 */
	public boolean reinsertEntry(ClusterFeature cf) {
		numDataPoints++;
		System.out.println("reinserting cluster # " + numDataPoints);
		// If this is the first entry, make it the root.

		if (root.getSize() == 1 && root.getCFs().get(0).getCount() == 0){
			getFirstLeaf().addCF(cf);
			cf.updatePointers(null, firstLeaf);
			updateNonSplitPath(firstLeaf);
			return true;
		}
		ClusterFeature closestLeaf = 
			findClosestLeafEntry(cf.getCentroid(),root);
		ClusterFeature mergedCF;
		CFNode closestNode = closestLeaf.curNode;
		// If distance is within the threshold, the closestEntry absorbs CF.
		if (closestLeaf.getCentroid().getDistance(cf.getCentroid(), 
				DistanceMetric.CITY_BLOCK) <= threshold) {
			closestLeaf.getSums().addAnotherParticle(cf.getSums());
			closestLeaf.setSumOfSquares(closestLeaf.getSumOfSquares() + 
					cf.getSumOfSquares());
			closestLeaf.setCount(closestLeaf.getCount() + cf.getCount());
		}
		// If adding the entry as a new CF results in a split, return false.
		else if (closestNode.getSize() >= branchFactor) {
			return false;
		}
		// Add it as a new CF to the closest Node if it doesn't split it.
		else {
			closestNode.addCF(cf);
			cf.updatePointers(null, closestNode);		
		}
		updateNonSplitPath(closestLeaf.curNode);
		return true;
	}
		
	/**
	 * Returns the closest leaf entry to the given entry. Recursive.
	 * @param entry - entry to compare to
	 * @param curNode - node to look in
	 * @return the CF in curNode that's the closest to entry.
	 */
	public ClusterFeature findClosestLeafEntry(BinnedPeakList entry, 
			CFNode curNode) {
		if (curNode == null)
			return null;
		// get the closest cf in the current node;
		ClusterFeature minFeature = curNode.getClosest(entry);
		if (minFeature.curNode.isLeaf()) {
			return minFeature;
		}
		return findClosestLeafEntry(entry, minFeature.child);
	}
	
	/**
	 * Splits a given leaf node if it can be split.  It currently does what 
	 * the algorithm suggests: take two farthest points as seeds and 
	 * separates entries.
	 * @param node - node to be split 
	 * @return last split node.
	 */
	public CFNode splitNodeIfPossible(CFNode node) {
		assert (node.isLeaf()) : "Split node is not a leaf";
		if (node.getSize() >= branchFactor) { 
			return splitNodeRecurse(node);
		}
		updateNonSplitPath(node);
		assignLeaves();
		return node;	
	}
	
	/**
	 * Recursive method for splitNodeIfPossible.
	 * @param node - node to split.
	 * @return last split node.
	 */
	private CFNode splitNodeRecurse(CFNode node) {	
		CFNode nodeA = new CFNode(node.parentCF);
		CFNode nodeB = new CFNode(node.parentCF);
	
		ClusterFeature[] farthestTwo = node.getTwoFarthest();
		assert (farthestTwo != null) : "trying to split a node with 1 CF";
		ClusterFeature entryA = farthestTwo[0];
		ClusterFeature entryB = farthestTwo[1];
		
		assert (entryA != null) : "EntryA is null";
		assert (entryB != null) : "EntryB is null";
		
		assert (entryA.getSums().testForMax(entryA.getCount())) :"TEST FOR MAX FAILS!";
		assert (entryB.getSums().testForMax(entryB.getCount())) :"TEST FOR MAX FAILS!";
				
		ArrayList<ClusterFeature> cfs = node.getCFs();
		BinnedPeakList listI, listJ;
		
		// use entryA and entryB as two seeds; separate entries.
		nodeA.addCF(entryA);
		nodeB.addCF(entryB);
		float distA, distB;
		for (int i = 0; i < cfs.size(); i++) {
			if (cfs.get(i) != entryA && cfs.get(i) != entryB) {
				listI = cfs.get(i).getCentroid();
				distA = listI.getDistance(entryA.getCentroid(),
						DistanceMetric.CITY_BLOCK);
				distB = listI.getDistance(entryB.getCentroid(),
						DistanceMetric.CITY_BLOCK);
				if (distA <= distB) {
					nodeA.addCF(cfs.get(i));
				}
				else
					nodeB.addCF(cfs.get(i));
			}
		}
		for (int i = 0; i < nodeA.getSize(); i++) {
			assert(nodeA.getCFs().get(i).getSums().testForMax(nodeA.getCFs().get(i).getCount())) : 
				"TEST FOR MAX FAILS IN NODE";
		}
		for (int i = 0; i < nodeB.getSize(); i++) {
			assert(nodeB.getCFs().get(i).getSums().testForMax(nodeB.getCFs().get(i).getCount())) : 
					"TEST FOR MAX FAILS IN NODE";
		}
		
		assert ((nodeA.isLeaf() && nodeB.isLeaf()) || 
				(!nodeA.isLeaf() && !nodeB.isLeaf())) : 
					"one node's a leaf, the other isn't";
		//give the two nodes different parent CFs, and add these parent CFs
		// to the parent node.
		ClusterFeature origParent = node.parentCF;
		ClusterFeature parentA = null, parentB = null;
		
		CFNode parentNode = null;
		// If split node is root, increase tree height by 1.
		if (origParent == null) {
			root = new CFNode(null);
			parentNode = root;
		}
		else {
			parentNode = origParent.curNode;
			parentNode.removeCF(origParent);
		}	
		parentA = new ClusterFeature(parentNode);
		parentA.updatePointers(nodeA, parentNode);
		parentA.updateCF();
		parentB = new ClusterFeature(parentNode);
		parentB.updatePointers(nodeB, parentNode);
		parentB.updateCF();
		parentNode.addCF(parentA);
		parentNode.addCF(parentB);
		
		removeNode(node);
		
		nodeA.updateParent(parentA);
		nodeB.updateParent(parentB);	
		
		assert (parentA.curNode.equals(parentB.curNode)) : 
			"parents aren't in same node.";
		if (nodeA.isLeaf()) {
			assert (nodeA.parentCF != null) : "leaf's parentCF is null!";
		}

		// If parentCF node needs to be split, recurse.
		if (parentNode.getSize() >= branchFactor) {
			return splitNodeRecurse(parentNode);
		}
		// If parentCF node doesn't need to be split, we're done.
		recentlySplitA = parentA;
		recentlySplitB = parentB;
	
		assignLeaves();			
		updateNonSplitPath(nodeA);	
		return parentNode;
		
	}
	
	/**
	 * TODO: THis is where the biggest performance hit comes.
	 * Updates the path recursively starting from the node's PARENT and up.  
	 * It is important to note that the first node passed won't be updated.
	 * @param node - node to update.
	 */
	public void updateNonSplitPath(CFNode node) {
		if (node != null && node.parentCF != null) {
			node.parentCF.updateCF();
			updateNonSplitPath(node.parentCF.curNode);
		}
	}
	
	/**
	 * Recusively sets the first leaf variable for the tree.
	 * @param node - current node in tree traversal.
	 */
	private void setFirstLeaf(CFNode node) {
		if (node.isLeaf()) {
			if (node.prevLeaf == null)
				firstLeaf = node;
			else setFirstLeaf(node.prevLeaf);
		}
		else setFirstLeaf(node.getCFs().get(0).child);
		assert (firstLeaf != null) : "first leaf is incorrectly set";
	}
	 
	/**
	 * returns the first leaf in the tree.
	 * @return first leaf in the tree
	 */
	public CFNode getFirstLeaf() {
		assert (firstLeaf != null) : "first leaf hasn't been set yet.";
		return firstLeaf;
	}
	
	/**
	 * Removes node and all children under that node from the tree.
	 * TODO:  is there any way to deal with this better?
	 * @param node - node to remove.
	 */
	public void removeNode(CFNode node) {
		ClusterFeature parentCF = node.clearNode();
		if (parentCF != null) {
			parentCF.child = null;
			updateNonSplitPath(parentCF.curNode);
		}
	}
	
	/**
	 * Refines the split, if it has occurred.  The variables recentlySplitA and 
	 * recentlySplitB are defined in SplitNodeRecurse as the two nodes that 
	 * were last split.  Don't merge these two together again, since that 
	 * would defeat the purpose of splitting.
	 * @param node - node to merge.
	 */
	public void refineMerge(CFNode node) {		
		// find closest two entries in the given node.
		ClusterFeature[] closestTwo = node.getTwoClosest();
		if ((closestTwo[0].isEqual(recentlySplitA) || 
				closestTwo[1].isEqual(recentlySplitA)) &&
				(closestTwo[0].isEqual(recentlySplitB) || 
						closestTwo[1].isEqual(recentlySplitB)))
			return;
		ClusterFeature merge = mergeEntries(closestTwo[0], closestTwo[1]);
		// use the splitNodeRecurse method if merged child needs to split.
		if (!merge.curNode.isLeaf()) 
			if (merge.child.getSize() >= branchFactor) {
				splitNodeRecurse(merge.child);
			}
		updateNonSplitPath(merge.child);
	}
	
	/**
	 * Merges two entries and their children.  
	 * @param entry - entry 1 
	 * @param entryToMerge - entry 2
	 * @return - merged entry (entry 2 will be merged into entry 1).
	 */
	public ClusterFeature mergeEntries(ClusterFeature entry, 
			ClusterFeature entryToMerge) {
		assert (entry.curNode.equals(entryToMerge.curNode)) : 
			"entries aren't in same node!";		
		CFNode curNode = entry.curNode;
		entry.getSums().addAnotherParticle(entryToMerge.getSums());
		entry.setSumOfSquares(entry.getSumOfSquares() + 
				entryToMerge.getSumOfSquares());
		entry.setCount(entry.getCount() + entryToMerge.getCount());
		if (!curNode.isLeaf()) {
			for (int i = 0; i < entryToMerge.child.getSize(); i++) 
				entry.child.addCF(entryToMerge.child.getCFs().get(i));
		}
		entry.curNode.removeCF(entryToMerge);
		entry.updateCF();
		return entry;
	}
	
	/**
	 * Checks to see if any CFs in the given leaf node can be merged.  This
	 * can happen when, after splitting and merge refinement, two CFs in one
	 * leaf are below the threshold in distance.  Not in the paper's algorithm.
	 * @param node - node to check for merge; has to be a leaf.
	 * @return true if merged, false if not
	 */
	public boolean checkForMerge(CFNode node) {
		assert (node.isLeaf()) : "node to check for merging isn't a leaf";
		boolean merge = true;
		boolean returnThis = false;
		ClusterFeature[] closest;
		ClusterFeature mergedCF;
		while (merge) {
		closest = node.getTwoClosest();
		if (closest != null && closest[0].getCentroid().
				getDistance(closest[1].getCentroid(), 
				DistanceMetric.CITY_BLOCK) <=  threshold) {
			returnThis = true;
			mergedCF = mergeEntries(closest[0], closest[1]);
		}
		else
			merge = false;
		}
		
		//updateNonSplitPath(node);
		return returnThis;
	}
	
	/**
	 * Estimates the next threshold for the new tree once this tree has used
	 * up all the available memory.  Calculates the minimum distance between
	 * two CFs for the most crowded leaf.
	 * @return new threshold.
	 */
	public float nextThreshold() {
		float dMin = nextThresholdRecurse(root);
		assert (dMin > threshold) : 
			"min distance bewteen two entries is smaller than T!";
		assert (dMin <= 2.1) : "min distance is greater than 2: " + dMin;
		if (dMin > 2.0) {
			System.out.println("rounding " + dMin + " to 2.0");
			dMin = 2.0f;
		}
		return dMin;
	}
	
	private float nextThresholdRecurse(CFNode node) {
		float dMin = 0;
		if (node.isLeaf()) {
			// if there's only onde CF in the leaf, find another leaf.
			if (node.getSize() <= 1) {
				//printTree();
				//countNodes();
				node = firstLeaf;
				while (node.getSize() <= 1) {
					node = node.nextLeaf;
					if (node == null)
						return threshold + 0.1f;
				}
			}
			ClusterFeature[] m = node.getTwoClosest();
			dMin = m[0].getCentroid().getDistance(m[1].getCentroid(), 
					DistanceMetric.CITY_BLOCK);
		}
		else {
			int maxCount = Integer.MIN_VALUE;
			int maxIndex = -1;
			for (int i = 0; i < node.getSize(); i++) 
				if (node.getCFs().get(i).getCount() > maxCount) {
					maxCount = node.getCFs().get(i).getCount();
					maxIndex = i;
				}
			return nextThresholdRecurse(node.getCFs().get(maxIndex).child);
		}
		return dMin;
	}
	
	
	/**
	 * Assign the prevLeaf and nextLeaf pointers to the leaves in the tree.
	 * Since this isn't needed in building the tree, it can be done at the 
	 * end.
	 */
	public void assignLeaves() {
		prev = null;
		assignLeavesRecurse(root);
		prev = null;

	}
	
	/**
	 * Recursive call for assigning the leaves.
	 * @param curNode - current noce in tree traversal.
	 */
	private void assignLeavesRecurse(CFNode curNode) {
		if (curNode.isLeaf()) {
			checkForMerge(curNode);
			curNode.prevLeaf = prev;
			curNode.nextLeaf = null;
			if (prev != null)
				prev.nextLeaf = curNode;
			prev = curNode;
		}
		else {
			curNode.prevLeaf = null;
			curNode.nextLeaf = null;
			for (int i = 0; i < curNode.getSize(); i++) 
				assignLeavesRecurse(curNode.getCFs().get(i).child);
		}
		setFirstLeaf(root);
	}
	
	// scan outliers to see if they can fit into tree.
	public boolean scanOutliers() {
		return true;
	}
	
	/**
	 * prints the tree.
	 */
	public void printTree() {
		System.out.println("Printing CF Tree:");
		printTreeRecurse(root, "");
		System.out.println();
		System.out.flush();
	}
	
	/**
	 * Recursive method for printing the tree.
	 * @param node - current node in tree traversal
	 * @param delimiter - current delimiter for level.
	 */
	private void printTreeRecurse(CFNode node, String delimiter) {
		System.out.println(delimiter + "NEW NODE");
		node.printNode(delimiter);
		if (!node.isLeaf()) 
			for (int i = 0; i < node.getSize(); i++) 
				printTreeRecurse(node.getCFs().get(i).child, delimiter + "  ");
	}
	
	/**
	 * Prints the leaf pointers.
	 * @param node - current leaf.
	 */
	public void printLeaves(CFNode node) {
		if (node == null)
			return;
		assert (node.isLeaf()) : " node is not leaf!";
		System.out.println("PREV: " + node.prevLeaf + " CUR: " + node + 
				" NEXT: " + node.nextLeaf);	
		if (node.nextLeaf != null) 	
			printLeaves(node.nextLeaf);
	}
	
	/**
	 * Gets the number of nodes in the tree.
	 * @param node - current node in tree traversal
	 * @param count - number of nodes
	 * @return count
	 */
	public int getNodeNumber(CFNode node, int count) {
		count++;
		if (!node.isLeaf()) {
			for (int i = 0; i < node.getSize(); i++) {
				count += getNodeNumber(node.getCFs().get(i).child, 0);
			}
		}
		return count;
	}
	
	/**
	 * Produces relevant summary information about a tree.
	 */
	public void countNodes() {
		int[] counts = {0,0,0,0,0};
		counts = countNodesRecurse(root, counts);
		
		
		System.out.println("****************************");
		System.out.println();		
		System.out.println("threshold: " + threshold);
		System.out.println("# of nodes: " + counts[0]);
		System.out.println("# of leaves: " + counts[1]);
		System.out.println("# of subclusters: " + counts[2]);
		System.out.println("# of grouped subclusters: " + counts[3]);
		System.out.println("# of particles represented: " + counts[4]);
		System.out.println();
		MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		System.out.println("initial memory: " + mem.getInit());
		System.out.println("memory used: " + mem.getUsed());
		System.out.println("memory committed: " + mem.getCommitted());
		System.out.println("max. memory: " + mem.getMax());
		System.out.println();
		System.out.println("****************************");
	}
	
	/**
	 * recursive call for summary info.  Returned array looks like
	 * 		{nodeCount, 
	 * 		leafCount, 
	 * 		subclusterCount, 
	 * 		groupedsubclustercount, 
	 * 		particleCount}
	 * @param node - current node in tree traversal
	 * @param counts - above array (initialized to {0,0,0,0,0}
	 * @return counts 
	 */
	private int[] countNodesRecurse(CFNode node, int[] counts) {
		if (node == null) 
			return counts;
		if (node.isLeaf()) {
			counts[0]++;
			counts[1]++;
			for (int i = 0; i < node.getSize(); i++) {
				counts[2]++;
				if (node.getCFs().get(i).getCount() > 1)
					counts[3]++;
				counts[4] += node.getCFs().get(i).getCount();
			}
		}
		else counts[0]++;
		for (int i = 0; i < node.getSize(); i++)
			countNodesRecurse(node.getCFs().get(i).child, counts);
		return counts;
	}
}
