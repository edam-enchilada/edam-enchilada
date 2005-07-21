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
import java.util.Iterator;

import analysis.BinnedPeak;
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
	private int leafFactor;
	private int branchFactor;
	public CFNode root;
	private int numDataPoints;
	
	// Constructor
	public CFTree(float t, int b, int l) {
		threshold = t;
		leafFactor = l;
		branchFactor = b;
		root = new CFNode(null);
		numDataPoints = 0;
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
		BinnedPeakList closestList = closestLeaf.getCentroid();
		CFNode closestNode = closestLeaf.curNode;
		if (closestList.getDistance(entry, DistanceMetric.CITY_BLOCK) < threshold) 
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
		float minDistance = Float.MAX_VALUE;
		float thisDistance;
		ClusterFeature minFeature = null;
		ArrayList<ClusterFeature> cfs = curNode.getCFs();
		for (int i = 0; i < cfs.size(); i++) {
			BinnedPeakList list = cfs.get(i).getCentroid();
			thisDistance = cfs.get(i).getSums().getDistance(entry,DistanceMetric.CITY_BLOCK);
			if (thisDistance < minDistance) {
				minDistance = thisDistance;
				minFeature = cfs.get(i);
			}
		}
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
		if (node.getSize() >= leafFactor) { 
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
		float maxDistance = Float.MIN_VALUE;
		float thisDistance;
		ClusterFeature entryA = null, entryB = null;
		ArrayList<ClusterFeature> cfs = node.getCFs();
		BinnedPeakList listI, listJ;
		for (int i = 0; i < cfs.size(); i++) {
			listI = cfs.get(i).getCentroid();
			for (int j = i; j < cfs.size(); j++) {
				listJ = cfs.get(j).getCentroid();
				thisDistance = listI.getDistance(listJ,DistanceMetric.CITY_BLOCK);
				if (thisDistance > maxDistance) {
					maxDistance = thisDistance;
					entryA = cfs.get(i);
					entryB = cfs.get(j);
				}
			}
		}
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
		parentNode.addCF(parentA);
		parentNode.addCF(parentB);
		removeNode(node);	
		
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
		return parentNode;
		
	}
	
	// updates the path starting from the node's PARENT and up.
	public void updateNonSplitPath(CFNode node) {
		if (node.parentCF != null) {
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
	public void removeNode(CFNode node) {
		node.parentCF = null;
		node.parentNode = null;
		node.nextLeaf = null;
		node.prevLeaf = null;
		node = null;
	}
	
	// refines the merge
	public void refineMerge(CFNode node) {
		// find closest two entries in the given node.
		float minDistance = Float.MAX_VALUE;
		float thisDistance;
		ClusterFeature entryA = null, entryB = null;
		ArrayList<ClusterFeature> cfs = node.getCFs();
		BinnedPeakList listI,listJ;
		for (int i = 0; i < cfs.size(); i++) {
			listI = cfs.get(i).getCentroid();
			for (int j = i; j < cfs.size(); j++) {
				listJ = cfs.get(j).getCentroid();
				thisDistance = listI.getDistance(listJ,DistanceMetric.CITY_BLOCK);
				if (i != j && thisDistance < minDistance) {
					minDistance = thisDistance;
					entryA = cfs.get(i);
					entryB = cfs.get(j);
				}
			}
		}
		ClusterFeature merge = mergeEntries(entryA, entryB);
		// use the splitNodeRecurse method if merged child needs to split.
		if (!merge.curNode.isLeaf()) {
			if (merge.child.getSize() >= branchFactor) { 
				splitNodeRecurse(merge.child);
			}
		}
		updateNonSplitPath(merge.child);
	}
	
	// merge two entries and their children
	// returns merged cluster feature
	// TODO: Deal with normalization factor here.
	public ClusterFeature mergeEntries(ClusterFeature entry, ClusterFeature entryToMerge) {
		assert (entry.curNode.equals(entryToMerge.curNode)) : "entries aren't in same node!";		
		CFNode curNode = entry.curNode;
		entry.getSums().addAnotherParticle(entryToMerge.getSums());
		entry.getSumOfSquares().addAnotherParticle(entryToMerge.getSumOfSquares());
		if (!curNode.isLeaf()) {
			for (int i = 0; i < entryToMerge.child.getSize(); i++) 
				entry.child.addCF(entryToMerge.child.getCFs().get(i));
		}
		entry.count += entryToMerge.count;
		// is it ok to have objects that point to nothing?  garbage collect?
		entry.curNode.removeCF(entryToMerge);
		
		entry.updateCF();
		return entry;
	}
	
	// estimates the next threshold and resets it
	// resets it according to dMin;
	public float nextSimpleThreshold() {
		CFNode curNode = root;
		while (!curNode.isLeaf()) {
			int maxCount = 0;
			int index = -1;
			for (int i = 0; i < curNode.getSize(); i++)
				if (curNode.getCFs().get(i).getCount() > maxCount) {
					maxCount = curNode.getCFs().get(i).getCount();
					index = i;
				}
			curNode = curNode.getCFs().get(index).child;
		}
		ArrayList<ClusterFeature> cfs = curNode.getCFs();
		float dMin = Float.MAX_VALUE;
		for (int i = 0; i < cfs.size(); i++) {
			BinnedPeakList sumsI = cfs.get(i).getCentroid();
			for (int j = i; j < cfs.size(); j++) {
				BinnedPeakList sumsJ = cfs.get(j).getCentroid();
				float dist = sumsI.getDistance(sumsJ, DistanceMetric.CITY_BLOCK);
				if (i != j && dist < dMin)
					dMin = dist;
			}
		}
		threshold = dMin;
		return dMin;
	}
	
	// estimates the next threshold and resets it
	public float nextPaperThreshold() {
		CFNode curLeaf = getFirstLeaf(root);
		// find the largest volume of the leaves.
		BinnedPeakList centroid;
		float maxVolume = Float.MIN_VALUE;
		CFNode maxLeaf = null;
		while (curLeaf != null) {
			// get leaf's centroid.
			centroid = new BinnedPeakList();
			for (int i = 0; i < curLeaf.getSize(); i++)
				centroid.addAnotherParticle(curLeaf.getCFs().get(i).getSums());
			centroid.divideAreasBy(curLeaf.getSize());
			// TODO: normalize here?
			centroid.normalize(DistanceMetric.CITY_BLOCK);
			
			// get leaf's radius
			float radius = 0, dist;
			for (int i = 0; i < curLeaf.getSize(); i++) {
				BinnedPeakList list = curLeaf.getCFs().get(i).getCentroid();
				dist = list.getDistance(centroid, DistanceMetric.CITY_BLOCK);
				radius += (dist*dist);
			}
			radius = (float) Math.sqrt(radius / curLeaf.getSize());
			
			// get leaf's volume.
			float volume = 1;
			for (int i = 0; i < curLeaf.getSize(); i++)
				volume = volume * radius;
			if (volume > maxVolume) {
				maxVolume = volume;
				maxLeaf = curLeaf;
			}
			curLeaf = curLeaf.nextLeaf;
		}
		
		//calculate nextT
		float nextT = maxVolume * (numDataPoints+1) / numDataPoints;
		
		// calculate expansion factor;
		float expansionFactor;
		float averageRootRadius = getAverageRootRadius();
		float newAverageRootRadius = averageRootRadius * (numDataPoints+1) / numDataPoints;
		if (newAverageRootRadius/averageRootRadius > 1)
			expansionFactor = newAverageRootRadius/averageRootRadius;
		else 
			expansionFactor = 1;
		
		// calculate dMin
		CFNode curNode = root;
		while (!curNode.isLeaf()) {
			int maxCount = 0;
			int index = -1;
			for (int i = 0; i < curNode.getSize(); i++)
				if (curNode.getCFs().get(i).getCount() > maxCount) {
					maxCount = curNode.getCFs().get(i).getCount();
					index = i;
				}
			curNode = curNode.getCFs().get(index).child;
		}
		ArrayList<ClusterFeature> cfs = curNode.getCFs();
		float dMin = Float.MAX_VALUE;
		for (int i = 0; i < cfs.size(); i++) {
			BinnedPeakList sumsI = cfs.get(i).getCentroid();
			for (int j = i; j < cfs.size(); j++) {
				BinnedPeakList sumsJ = cfs.get(j).getCentroid();
				float dist = sumsI.getDistance(sumsJ, DistanceMetric.CITY_BLOCK);
				if (i != j && dist < dMin)
					dMin = dist;
			}
		}
		System.out.println("old threshold: " + threshold);
		System.out.println("largestLeafVolume: " + maxVolume);
		System.out.println("nextT: " + nextT);
		System.out.println("expansionFactor: " + expansionFactor);
		System.out.println("dMin: " + dMin);
		
		
		// Finally, calculate the true nextT;
		float returnedT = -1;
		if (dMin > (expansionFactor * nextT))
			returnedT = dMin;
		else 
			returnedT = expansionFactor * nextT;
		if (returnedT < threshold)
			returnedT = threshold + .1f;
		threshold = returnedT;
		System.out.println("NEW THRESHOLD: " + returnedT);
		System.out.println();
		return returnedT;
	}
	
	public float getAverageRootRadius() {
		assert (root != null) : "root is null";
		BinnedPeakList centroid = new BinnedPeakList();
		for (int i = 0; i < root.getSize(); i++)
			centroid.addAnotherParticle(root.getCFs().get(i).getSums());
		centroid.divideAreasBy(root.getSize());
		// TODO: normalize here?
		centroid.normalize(DistanceMetric.CITY_BLOCK);

		float radius = 0, dist;
		for (int i = 0; i < root.getSize(); i++) {
			BinnedPeakList list = root.getCFs().get(i).getCentroid();
			dist = list.getDistance(centroid, DistanceMetric.CITY_BLOCK);
			radius += (dist*dist);
		}
		radius = (float) Math.sqrt(radius / root.getSize());
		
		return radius;
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
		assert (node != null && node.isLeaf()) : " node is not leaf!";
		if (node == null)
			return;
		System.out.println("PREV: " + node.prevLeaf + " CUR: " + node + " NEXT: " + node.nextLeaf);
		if (node.nextLeaf != null) 	
			printLeaves(node.nextLeaf);
	}
	
	public int[] countNodes() {
		int[] counts = {0,0,0,0,0};
		return countNodesRecurse(root, counts);
	}
	
	//{nodeCount, leafCount, subclusterCount, groupedsubclustercount, particleCount}
	public int[] countNodesRecurse(CFNode node, int[] counts) {
		if (node == null) 
			return counts;
		else if (node.isLeaf()) {
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
