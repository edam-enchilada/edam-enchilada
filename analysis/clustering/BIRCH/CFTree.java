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
	// Q: should dist < threshold or should dist <= threshold?
	public float threshold;
	private int branchFactor;
	public CFNode root;
	private int numDataPoints;
	
	private ClusterFeature recentlySplitA;
	private ClusterFeature recentlySplitB;
	
	// Constructor
	public CFTree(float t, int b) {
		threshold = t;
		branchFactor = b;
		root = new CFNode(null);
		numDataPoints = 0;
		recentlySplitA = null;
		recentlySplitB = null;
	}
	
	// insert a particle. returns the changed CFNode.
	public CFNode insertEntry(BinnedPeakList entry, int atomID) {
 		numDataPoints++;
		System.out.println("inserting particle # " + numDataPoints);
		// If this is the first entry, make it the root.
		if (root.getSize() == 0) {
			ClusterFeature firstCF = new ClusterFeature(root);
			firstCF.updateCF(entry, atomID);
			root.addCF(firstCF);
			return root;
		}
		ClusterFeature closestLeaf = findClosestLeafEntry(entry,root);
		CFNode closestNode = closestLeaf.curNode;
		if (closestLeaf.getCentroid().getDistance(entry, DistanceMetric.CITY_BLOCK) <= threshold) 
			closestLeaf.updateCF(entry, atomID);
		else {
			ClusterFeature newEntry;
			if (closestNode.parentNode == null)
				newEntry = new ClusterFeature(null);
			else
				newEntry = new ClusterFeature(closestNode.parentNode);
			newEntry.updateCF(entry, atomID);
			newEntry.updatePointers(null, closestNode);
			closestNode.addCF(newEntry);
		}
		return closestNode;
		
	}
		
	// find closest leaf entry (recursive)
	// returns closest CF.
	public ClusterFeature findClosestLeafEntry(BinnedPeakList entry, CFNode curNode) {
		if (curNode == null)
			return null;
		// get the closest cf in the current node;
		ClusterFeature minFeature = curNode.getClosest(entry);
		if (minFeature.curNode.isLeaf()) {
			return minFeature;
		}
		return findClosestLeafEntry(entry, minFeature.child);
	}
	
	// splits a given leaf node (recursive) returns the node where split stops
	// right now, does what the algorithm suggests: take two farthest 
	// points as seeds and separates entries.
	public CFNode splitNodeIfPossible(CFNode node) {
		assert (node.isLeaf()) : "Split node is not a leaf";
		if (node.getSize() >= branchFactor) { 
			return splitNodeRecurse(node);
		}
		updateNonSplitPath(node);
		return node;	
	}
	
	public CFNode splitNodeRecurse(CFNode node) {	
		CFNode nodeA = new CFNode(node.parentCF);
		CFNode nodeB = new CFNode(node.parentCF);
		if (node.isLeaf()) {
			nodeA.updateLeafPointers(node.parentCF, node.prevLeaf, node.nextLeaf);
			nodeB.updateLeafPointers(node.parentCF, node.prevLeaf, node.nextLeaf);
		}
		ClusterFeature[] farthestTwo = node.getTwoFarthest();
		ClusterFeature entryA = farthestTwo[0];
		ClusterFeature entryB = farthestTwo[1];
		ArrayList<ClusterFeature> cfs = node.getCFs();
		BinnedPeakList listI, listJ;
		
		// use entryA and entryB as two seeds; separate entries.
		nodeA.addCF(entryA);
		nodeB.addCF(entryB);
		float distA, distB;
		for (int i = 0; i < cfs.size(); i++) {
			if (cfs.get(i) != entryA && cfs.get(i) != entryB) {
				listI = cfs.get(i).getCentroid();
				distA = listI.getDistance(entryA.getCentroid(),DistanceMetric.CITY_BLOCK);
				distB = listI.getDistance(entryB.getCentroid(),DistanceMetric.CITY_BLOCK);
				if (distA < distB) {
					nodeA.addCF(cfs.get(i));
				}
				else if (distA > distB)
					nodeB.addCF(cfs.get(i));
				else {
					double rand = Math.random();
					if (rand <= 0.5)
						nodeA.addCF(cfs.get(i));
					else nodeB.addCF(cfs.get(i));
				}
			}
		}
		
		for (int i = 0; i < nodeA.getSize(); i++) 
			nodeA.getCFs().get(i).updatePointers(nodeA.getCFs().get(i).child, nodeA);
		
		for (int i = 0; i < nodeB.getSize(); i++) 
			nodeB.getCFs().get(i).updatePointers(nodeB.getCFs().get(i).child, nodeB);
		
		assert ((nodeA.isLeaf() && nodeB.isLeaf()) || 
				(!nodeA.isLeaf() && !nodeB.isLeaf())) : 
					"one node's a leaf, the other isn't";
		
		ClusterFeature parent = node.parentCF;
		ClusterFeature parentA = null,parentB = null;
		CFNode parentNode = null;
		
		// If split node is root, increase tree height by 1.
		if (parent == null) {
			root = new CFNode(null);
			parentNode = root;
		}
		else {
			parentNode = parent.curNode;
			parentNode.removeCF(parent);
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
		
		if (nodeA.isLeaf() && nodeB.isLeaf()) {
			nodeA.updateLeafPointers(parentA, nodeA.prevLeaf, nodeB);
			nodeB.updateLeafPointers(parentB, nodeA, nodeB.nextLeaf);
			if (nodeB.nextLeaf != null)
				nodeB.nextLeaf.prevLeaf = nodeB;
			if (nodeA.prevLeaf != null)
				nodeA.prevLeaf.nextLeaf = nodeA;
		}
		else {
			nodeA.updateNonLeafPointers(parentA);
			nodeB.updateNonLeafPointers(parentB);
		}	
		
		assert (parentA.curNode.equals(parentB.curNode)) : "parents aren't in same node.";
		
		if (nodeA.isLeaf()) {
			assert (nodeA.parentCF != null) : "leaf's parentCF is null!";
		}
		
		// If parentCF node needs to be split, recurse.
		if (parentNode.getSize() >= branchFactor) {
			return splitNodeRecurse(parentNode);
		}
		// If parentCF node doesn't need to be split,update rest of path and
		// we're done.
		updateNonSplitPath(parentNode);
		recentlySplitA = parentA;
		recentlySplitB = parentB;
		return parentNode;
		
	}
	
	// updates the path starting from the node's PARENT and up.
	public void updateNonSplitPath(CFNode node) {
		if (node != null && node.parentCF != null) {
			node.parentCF.updateCF();
			updateNonSplitPath(node.parentCF.curNode);
		}
	}
	
	// gets the leftmost leaf in the tree.
	public CFNode getFirstLeaf(CFNode node) {
		if (node.isLeaf()) {
			if (node.prevLeaf == null)
				return node;
			else return getFirstLeaf(node.prevLeaf);
		}
		else return getFirstLeaf(node.getCFs().get(0).child);
	}
	
	// removes node.  TODO:  is there any way to deal with this better?
	// also removes all children under that node.
	public void removeNode(CFNode node) {
		ClusterFeature parentCF = node.clearNode();
		if (parentCF != null) {
			parentCF.child = null;
			updateNonSplitPath(parentCF.curNode);
		}
	}
	
	// refines the merge
	public void refineMerge(CFNode node) {
		// find closest two entries in the given node.
		ClusterFeature[] closestTwo = node.getTwoClosest();
		if ((closestTwo[0].isEqual(recentlySplitA) || closestTwo[1].isEqual(recentlySplitA)) &&
				(closestTwo[0].isEqual(recentlySplitB) || closestTwo[1].isEqual(recentlySplitB)))
			return;
		ClusterFeature merge = mergeEntries(closestTwo[0], closestTwo[1]);
		// use the splitNodeRecurse method if merged child needs to split.
		if (!merge.curNode.isLeaf()) 
			if (merge.child.getSize() >= branchFactor) {
				splitNodeRecurse(merge.child);
			}
		updateNonSplitPath(merge.child);
	}
	
	// merge two entries and their children
	// returns merged cluster feature
	public ClusterFeature mergeEntries(ClusterFeature entry, ClusterFeature entryToMerge) {
		assert (entry.curNode.equals(entryToMerge.curNode)) : "entries aren't in same node!";		
		CFNode curNode = entry.curNode;
		entry.getSums().addAnotherParticle(entryToMerge.getSums());
		entry.setSumOfSquares(entry.getSumOfSquares() + entryToMerge.getSumOfSquares());
		entry.setCount(entry.getCount() + entryToMerge.getCount());
		if (!curNode.isLeaf()) {
			for (int i = 0; i < entryToMerge.child.getSize(); i++) 
				entry.child.addCF(entryToMerge.child.getCFs().get(i));
		}
		// is it ok to have objects that point to nothing?  garbage collect?
		entry.curNode.removeCF(entryToMerge);
		entry.updateCF();
		return entry;
	}
	
	// estimates the next threshold and resets it
	// resets it according to dMin;
	public float nextSimpleThreshold() {
		float dMin = 0;
		CFNode leaf = getFirstLeaf(root);
		int maxCount = 0, count;
		CFNode maxLeaf = null;
		while (leaf != null) {
			count = 0;
			for (int i = 0; i < leaf.getSize(); i++) 
				count += leaf.getCFs().get(i).getCount();
			if (count > maxCount && leaf.getSize() > 1) {
				maxCount = count;
				maxLeaf = leaf;
			}
			leaf = leaf.nextLeaf;
		}
		if (maxLeaf == null)
			dMin = threshold + 0.1f;
		else {
			ClusterFeature[] m = maxLeaf.getTwoClosest();
			dMin = m[0].getCentroid().getDistance(m[1].getCentroid(), DistanceMetric.CITY_BLOCK);
		}
		assert (dMin > threshold) : "min distance bewteen two entries is smaller than T!";
		
		return dMin;
	}
	
	// estimates the next threshold and resets it
	// according to paper, not high priority right now.
	public float nextPaperThreshold() {
		return 0;
	}
	
	public void insertEmptyPath(float tHold, ArrayList<Integer> indices) {
		CFNode curNode = root;
		for (int i = 0; i < indices.size(); i++) {
			// if index is too big, add a new node.  Otherwise, it's already there.
			if (curNode.getCFs().size() <= indices.get(i)) {
				new CFNode(curNode.parentCF);
			}
		}
	}
	
	// scan outliers to see if they can fit into tree.
	public boolean scanOutliers() {
		return true;
	}
	
	public void printTree() {
		System.out.println();
		System.out.println("Printing CF Tree:");
		printTreeRecurse(root, "");
	}
	
	public void printTreeRecurse(CFNode node, String delimiter) {
		System.out.println(delimiter + "NEW NODE");
		node.printNode(delimiter);
		if (!node.isLeaf()) 
			for (int i = 0; i < node.getSize(); i++) 
				printTreeRecurse(node.getCFs().get(i).child, delimiter + "  ");
		else System.out.println(delimiter + "cfs: null");
	}
	
	public void printLeaves(CFNode node) {
		if (node == null)
			return;
		assert (node.isLeaf()) : " node is not leaf!";
		System.out.println("PREV: " + node.prevLeaf + " CUR: " + node + " NEXT: " + node.nextLeaf);	
		if (node.nextLeaf != null) 	
			printLeaves(node.nextLeaf);
	}
	
	public void countNodes() {
		int[] counts = {0,0,0,0,0};
		counts = countNodesRecurse(root, counts);
		
		System.out.println();
		System.out.println("threshold: " + threshold);
		System.out.println("# of nodes: " + counts[0]);
		System.out.println("# of leaves: " + counts[1]);
		System.out.println("# of subclusters: " + counts[2]);
		System.out.println("# of grouped subclusters: " + counts[3]);
		System.out.println("# of particles represented: " + counts[4]);

	}
	
	//{nodeCount, leafCount, subclusterCount, groupedsubclustercount, particleCount}
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
}
