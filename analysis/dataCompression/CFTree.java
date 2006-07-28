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

package analysis.dataCompression;
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
	private DistanceMetric dMetric;
	
	private ClusterFeature recentlySplitA = null;
	private ClusterFeature recentlySplitB = null;
	
	private ArrayList<BinnedPeakList> testDisList; //used for test purposes only
	private final double problem = 0.00000000001;
	private long memory;
	/**
	 * Constructor.  
	 * @param t - threshold for the tree
	 * @param b - branching factor for the tree
	 */
	public CFTree(float t, int b, DistanceMetric d) {
		threshold = t;
		branchFactor = b;
		dMetric = d;
		root = new CFNode(null,dMetric); // initialize the root
	}
	
	/**
	 * Inserts a particle into the tree.
	 * @param entry - binned peak list of next particle
	 * @param atomID - particle's corresponding atomID (testing purposes)
	 * @return node that particle is inserted into.
	 */
	public CFNode insertEntry(BinnedPeakList entry, int atomID) {
		numDataPoints++;
		//show progress every 100 particles
		if (numDataPoints % 100 == 0)
			System.out.println("inserting particle # " + numDataPoints);
		// If this is the first entry, make it the root.
		if (root.getSize() == 0) {
			ClusterFeature firstCF = new ClusterFeature(root, dMetric);
			firstCF.updateCF(entry, atomID);
			root.addCF(firstCF);
			memory=root.getMemory();
			return root;
		}
		ClusterFeature closestLeaf = findClosestLeafEntry(entry,root);
		CFNode closestNode = closestLeaf.curNode;
		
		memory-=closestNode.getMemory();
		// if distance is below the threshold, CF absorbs the new entry.
		if (threshold == 0 && closestLeaf.getSums().getDistance(
					entry, dMetric) <= threshold + problem) {
			closestLeaf.updateCF(entry, atomID);
		}
		else if (threshold!=0 && closestLeaf.getSums().getDistance(
					entry, dMetric) <= threshold) {
			closestLeaf.updateCF(entry, atomID);
		}
		//	else, add it to the closestNode as a new CF.
		else {
			ClusterFeature newEntry;
			if (closestNode.parentNode == null)
				newEntry = new ClusterFeature(null, dMetric);
			else
				newEntry = new ClusterFeature(closestNode.parentNode, dMetric);
			newEntry.updateCF(entry, atomID);
			newEntry.updatePointers(null, closestNode);
			closestNode.addCF(newEntry);
		}
		memory+=closestNode.getMemory();
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
		if (numDataPoints%100 == 0)
			System.out.println("reinserting cluster feature # " + numDataPoints);
		
		// If this is the first entry, make it the root.
		if (root.getSize() == 1 && root.getCFs().get(0).getCount() == 0) {
			firstLeaf.addCF(cf);
			cf.updatePointers(null, firstLeaf);
			updateNonSplitPath(firstLeaf);
			for (int j = 0; j < firstLeaf.getCFs().size(); j++){
				firstLeaf.getCFs().get(j).updateCF();
			}
			memory=firstLeaf.getMemory();
			return true;
		}
		ClusterFeature closestLeaf = 
			findClosestLeafEntry(cf.getSums(),root);
		if (closestLeaf==null) {
			System.err.println("CLOSEST LEAF = NULL in CFTree");
			System.err.println("Try increasing the memory threshold. Exiting.");
			System.exit(0);
		}
			
		ClusterFeature mergedCF;
		CFNode closestNode = closestLeaf.curNode;
		// If distance is within the threshold, the closestEntry absorbs CF.
//		System.out.println("threshold = " + threshold);
		float distance = closestLeaf.getSums().getDistance(cf.getSums(), dMetric);
//		System.out.println("distance = " + distance);
		if (distance <= threshold) {
//			System.out.println("absorbing the cf");
			closestLeaf.absorbCF(cf);
		}
		// If adding the entry as a new CF results in a split, return false.
		else if (closestNode.getSize() >= branchFactor-1) {
//			System.out.println("closestNode.getSize() >= branchFactor-1");
			return false;
		}
		// Add it as a new CF to the closest Node if it doesn't split it.
		else {
//			System.out.println("adding as a new clusterFeature to closestNode");
			memory-=closestNode.getMemory();
			closestNode.addCF(cf);
			cf.updatePointers(null, closestNode);
			memory+=closestNode.getMemory();
		}
		int size = closestNode.getCFs().size();
		updateNonSplitPath(closestNode);
		
		for (int i = 0; i < closestLeaf.curNode.getCFs().size(); i++){
			closestLeaf.curNode.getCFs().get(i).updateCF();
		}
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
		if(minFeature==null) {
			return null;
		}
		// get the closest cf in the current node;
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
		//System.out.println("Splitting node: "+ node);
		//System.out.flush();
		assert (node.isLeaf()) : "Split node is not a leaf";
		if (node.getSize() >= branchFactor) { 
			node = splitNodeRecurse(node);
		}
		assignLeaves();
		return node;	
	}
	
	/**
	 * Recursive method for splitNodeIfPossible.
	 * @param node - node to split.
	 * @return last split node.
	 */
	/**
	 * Recursive method for splitNodeIfPossible.
	 * @param node - node to split.
	 * @return last split node.
	 */
	private CFNode splitNodeRecurse(CFNode node) {	
		memory-=node.getMemory();
		CFNode nodeA = new CFNode(null, dMetric);
		CFNode nodeB = new CFNode(null, dMetric);
	
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
				listI = cfs.get(i).getSums();
				distA = listI.getDistance(entryA.getSums(), dMetric);
				distB = listI.getDistance(entryB.getSums(), dMetric);
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
			root = new CFNode(null, dMetric);
			parentNode = root;
		}
		else {
			memory-=origParent.curNode.getMemory();
			parentNode = origParent.curNode;
			parentNode.removeCF(origParent);
		}	
		//update all the parents etc.  
		parentA = new ClusterFeature(parentNode, dMetric);
		parentA.updatePointers(nodeA, parentNode);
		parentA.updateCF();
		parentNode.addCF(parentA);
		nodeA.updateParent(parentA);
		
		parentB = new ClusterFeature(parentNode, dMetric);
		parentB.updatePointers(nodeB, parentNode);
		parentB.updateCF();
		parentNode.addCF(parentB);
		nodeB.updateParent(parentB);
		memory+=parentNode.getMemory();
		
		removeNode(node);
		
		assert (parentA.curNode.equals(parentB.curNode)) : 
			"parents aren't in same node.";
		if (nodeA.isLeaf()) {
			assert (nodeA.parentCF != null) : "leaf's parentCF is null!";
		}

		memory+=nodeA.getMemory();
		memory+=nodeB.getMemory();
		
		// If parentCF node needs to be split, recurse.
		if (parentNode.getSize() >= branchFactor) {
			return splitNodeRecurse(parentNode);
		}
		// If parentCF node doesn't need to be split, we're done.
		recentlySplitA = parentA;
		recentlySplitB = parentB;
		
		return parentNode;
	}
	
	/**
	 * TODO: This is where the biggest performance hit comes.
	 * Updates the path recursively starting from the node's PARENT and up.  
	 * It is important to note that the first node passed won't be updated.
	 * @param node - node to update.
	 */
	public void updateNonSplitPath(CFNode node) {
		if (node != null && node.parentCF != null) {
			memory-=node.parentCF.getMemory();
			node.parentCF.updateCF();
			memory+=node.parentCF.getMemory();
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
			CFNode curNode = parentCF.curNode;
			curNode.removeCF(parentCF);
			parentCF.child = null;
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
						closestTwo[1].isEqual(recentlySplitB))){
			return;
		}
		ClusterFeature merge = mergeEntries(closestTwo[0], closestTwo[1]);
		
		// use the splitNodeRecurse method if merged child needs to split.
		if (!merge.curNode.isLeaf()) 
			if (merge.child.isLeaf())
				splitNodeIfPossible(merge.child);
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
		
		memory-=curNode.getMemory();
		
		BinnedPeakList b1 = entry.getSums();
		b1.multiply(entry.getCount());
		
		BinnedPeakList b2 = entryToMerge.getSums();
		b2.multiply(entryToMerge.getCount());
		
		b1.addAnotherParticle(b2);
		b1.normalize(dMetric);
		
		ArrayList<Integer> newAtomIds = entry.getAtomIDs();
		newAtomIds.addAll(entryToMerge.getAtomIDs());
		
		ClusterFeature returnThis = new ClusterFeature(
				curNode,
				entry.getCount()+entryToMerge.getCount(),
				b1,
				entry.getSumOfSquares() + entryToMerge.getSumOfSquares(),
				newAtomIds);
		
		returnThis.child = entry.child;
		if (returnThis.child != null)
			returnThis.child.parentCF = returnThis;
			
		if (!curNode.isLeaf()) {
			for (int i = 0; i < entryToMerge.child.getSize(); i++) {
				returnThis.child.addCF(entryToMerge.child.getCFs().get(i));
			}
		}
		
		curNode.removeCF(entry);
		curNode.removeCF(entryToMerge);
		
		curNode.addCF(returnThis);
		returnThis.updateCF();
		
		memory+=curNode.getMemory();
		return returnThis;
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
		if (closest != null && closest[0].getSums().
				getDistance(closest[1].getSums(), 
				dMetric) <=  threshold) {
			returnThis = true;
			mergedCF = mergeEntries(closest[0], closest[1]);
		}
		else
			merge = false;
		}
		return returnThis;
	}
	
	/**
	 * Estimates the next threshold for the new tree once this tree has used
	 * up all the available memory.  Calculates the minimum distance between
	 * two CFs for the most crowded leaf.
	 * @return new threshold.
	 */
	/*public float nextThreshold() {
		float dMin = nextThresholdRecurse(root);
		assert (dMin > threshold) : 
			"min distance bewteen two entries is smaller than T!";
		//assert (dMin <= 2.1) : "min distance is greater than 2: " + dMin;
		//if (dMin > 2.0) {
		//	System.out.println("rounding " + dMin + " to 2.0");
		//	dMin = 2.0f;
		//}
		return dMin;
	}*/
	
	public float nextThreshold() {
		float dMin = 2;
		CFNode node = firstLeaf;
		while(node!=null) {
			if(node.getSize()>1) {
				ClusterFeature[] m = node.getTwoClosest();
				if( m[0].getSums().getDistance(m[1].getSums(), dMetric)<dMin)
				{
					dMin = m[0].getSums().getDistance(m[1].getSums(), dMetric);
				}
			}
			node = node.nextLeaf;
		}

		if (node == null && dMin == 2)
			return threshold + 0.1f;
		return dMin;
	}
	/*
	private float nextThresholdRecurse(CFNode node) {
		//System.out.println("\texploring node " + node);
		float dMin = 0;
		if (node.isLeaf()) {
			// if there's only one CF in the leaf, find another leaf.
			if (node.getSize() <= 1) {
				//countNodes();
				node = firstLeaf;
				while (node.getSize() <= 1) {
					node = node.nextLeaf;
					if (node == null)
						return threshold + 0.1f;
				}
			}
		
			ClusterFeature[] m = node.getTwoClosest();

			dMin = m[0].getSums().getDistance(m[1].getSums(), 
					dMetric);
			
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
	*/
	
	
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
	
	public void scanAllNodes() {
		
		scanAllNodesRecurse(root);
	}
	public void scanAllNodesRecurse(CFNode node) {
		//start at the bottom left leaf cluster feature
		//compare the cluster feature to the summary cluster
		//feature one level up, repeat for all cluster features
		//in the tree
		for (int i = 0; i < node.getCFs().size(); i++) {
			
			ClusterFeature tempCF = node.getCFs().get(i);
			ClusterFeature closestCF = null;
			float parentDistance;
			float otherDistance = 3;
			
			if(node!=root && node.getSize() != 0 && tempCF.getCount() != 0) {
	//		for (int i = 0; i < node.getCFs().size(); i++) {
	//			tempCF = node.getCFs().get(i);
				//get the distance to the parent cluster feature
			//	parentDistance = tempCF.getSums().getDistance(node.parentCF.getSums(), dMetric);
				//get the smallest distance to the other cluster features in the parent node
				for (int j = 0; j < node.parentNode.getCFs().size(); j++) {
					if (otherDistance>tempCF.getSums().getDistance(node.parentNode.getCFs().get(j).getSums(), dMetric)) {
						otherDistance = tempCF.getSums().getDistance(node.parentNode.getCFs().get(j).getSums(), dMetric);
						closestCF = node.parentNode.getCFs().get(j);
					}
				}
			//	closestCF.printCF("");
				if(closestCF!=node.parentCF) {
			//		System.out.println("****very bad");
			//		System.out.println("tempCF");
			//		tempCF.printCF("");
			//		System.out.println("closestCF");
			//		closestCF.printCF("");
				}
			}
			//now move on to the child
			if (tempCF.child!=null)
				scanAllNodesRecurse(tempCF.child);
		}
		
	}
	/**
	 * scanDistances() and getDistancesRecurse() 
	 * find the closest leaf clusterFeature to each
	 * leaf clusterFeature
	 * it's a test method
	 */
	public void scanDistances() {	
		getDistancesRecurse(firstLeaf);
		float distance;
	}
	public void getDistancesRecurse(CFNode node) {
		for (int i = 0; i < node.getSize(); i++) {
			//find largest distance inside the node
			float largestDisInNode = 0;
			float tempDist;
			for (int j = 0; j < node.getSize(); j++) {
				if(j!=i)
				{
					tempDist = node.getCFs().get(i).getSums().getDistance(node.getCFs().get(j).getSums(), dMetric);
					if(tempDist>largestDisInNode)
						largestDisInNode = tempDist;
				}
			}
			node.getCFs().get(i).printCF("");
	//		System.out.println("largest " + largestDisInNode);
			
			//find smallest distance outside the node
			CFNode leaf = firstLeaf;
			ClusterFeature closestCF = null;
			float smallestDisOutNode = 3;
			while(leaf.nextLeaf!=null){
				if(leaf.nextLeaf!=node){
					leaf = leaf.nextLeaf;
					for (int j = 0; j < leaf.getSize(); j++) {
						tempDist = node.getCFs().get(i).getSums().getDistance(leaf.getCFs().get(j).getSums(), dMetric);
						if(tempDist<smallestDisOutNode) {
							smallestDisOutNode = tempDist;
							closestCF = leaf.getCFs().get(j);
						}
					}
				}
			}
	//		System.out.println("smallest " + smallestDisOutNode);
	//		System.out.println("closestCF ");
	//		closestCF.printCF("");
			//compare the distances
			if(smallestDisOutNode<largestDisInNode)
				System.out.println("****not good");
		}
		if(node.nextLeaf!=null)
			getDistancesRecurse(node.nextLeaf);
	}

/*	public float getError(ParticleInfo particle){
		float error = 0;
		//find the clusterFeature with that AtomID
		int id = particle.getID();
		ClusterFeature cf = findCFWithID(id);
		//get the distance from the clusterFeature to the particle
		return error;
	}*/

	/**
	 * prints the tree.
	 */
	public void printTree() {
		System.out.println("\nPrinting CF Tree:");
		printTreeRecurse(root, "");
		System.out.println();
		System.out.println("printTree done");
		System.out.flush();
	}
	
	/**
	 * Recursive method for printing the tree.
	 * @param node - current node in tree traversal
	 * @param delimiter - current delimiter for level.
	 */
	public void printTreeRecurse(CFNode node, String delimiter) {
		if (!node.isLeaf()) {	
			System.out.println(delimiter + "NEW NODE");
			for (int i = 0; i < node.getSize(); i++) {
				node.getCFs().get(i).printCF(delimiter);
				node.getCFs().get(i).getSums().getMagnitude(dMetric);
				printTreeRecurse(node.getCFs().get(i).child, delimiter + "    ");
			}
		}
		else {
			System.out.println(delimiter + "NEW LEAF");
			for (int i = 0; i < node.getSize(); i++) {
				node.getCFs().get(i).printCF(delimiter);
				node.getCFs().get(i).getSums().getMagnitude(dMetric);
			}
		}
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
		System.out.println("TREE INFORMATION:\n");
		System.out.println("threshold: " + threshold);
		System.out.println("# of nodes: " + counts[0]);
		System.out.println("# of leaves: " + counts[1]);
		System.out.println("# of subclusters: " + counts[2]);
		System.out.println("# of grouped subclusters: " + counts[3]);
		System.out.println("# of particles represented: " + counts[4]);
		System.out.println("memory used: " + memory);
//		 make sure memory is getting added correctly:
		System.out.println("Tested mem: " + tester(root,0,""));
		System.out.println();
		System.out.println("****************************");
		System.out.flush();
	}
	
	public int tester(CFNode node, int mem, String tab)
	{
		mem+=node.getMemory();
		//node.printNode(tab);
		for (int i = 0; i < node.getCFs().size(); i++) {
			if(node.getCFs().get(i).child != null){
					mem+=tester(node.getCFs().get(i).child,0,tab+=" ");
			}
		}
		return mem;
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
	public int[] countNodesRecurse(CFNode node, int[] counts) {
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
	
	/**
	 * The tree needs to merge if the number of leaves is greater than
	 * the number of nodes that can be held in memory.  
	 * @return
	 */
	public long getMemory() {
		return memory;
	}
	
	/**
	 * Test code - shouldn't be used in actual implementation.
	 * @param current
	 * @param firstPass
	 */
	public void findTreeMemory(CFNode current, boolean firstPass) {
		if (firstPass)
			memory=0;
		memory+=current.getMemory();
		for (int i = 0; i < current.getCFs().size(); i++)
			if(current.getCFs().get(i).child != null)
				findTreeMemory(current.getCFs().get(i).child,false);
	}
}
