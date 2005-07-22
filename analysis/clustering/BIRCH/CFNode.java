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
 * The Original Code is EDAM Enchilada's CFNode class.
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
 * CFNode is a node for the CFTree.  It contains an arraylist of cluster
 * features that belong to that node.
 * 
 * @author ritza
 *
 */
public class CFNode {
	private ArrayList<ClusterFeature> cfs;
	public ClusterFeature parentCF = null;
	public CFNode parentNode = null;
	
	// TODO: don't think we need prevLeaf
	public CFNode prevLeaf = null;
	public CFNode nextLeaf = null;
	
	// Constructor
	public CFNode(ClusterFeature p) {
		cfs = new ArrayList<ClusterFeature>();
		parentCF = p;
		if (parentCF != null)
			parentNode = p.curNode;
	}
	
	// Add a cluster feature
	public void addCF(ClusterFeature cf){
		cfs.add(cf);
		cf.curNode = this;
	}
	
	// Remove a cluster feature
	public boolean removeCF(ClusterFeature cf) {
		for (int i = 0; i < cfs.size(); i++) {
			if (cfs.get(i).isEqual(cf)) {
				cfs.remove(i);
				return true;
			}
		}
		return false;
	}
	
	// returns the two closest cluster features.
	public ClusterFeature[] getTwoClosest() {
		if (cfs.size() < 2) {
			return null;
		}
		float minDistance = Float.MAX_VALUE;
		float thisDistance;
		ClusterFeature entryA = null, entryB = null;
		BinnedPeakList listI, listJ;
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
		ClusterFeature[] closestTwo = new ClusterFeature[2];
		closestTwo[0] = entryA;
		closestTwo[1] = entryB;
		return closestTwo;
	}
	
	//returns the two farthest cluster features.
	public ClusterFeature[] getTwoFarthest() {
		if (cfs.size() < 2) {
			return null;
		}
		float maxDistance = Float.MIN_VALUE;
		float thisDistance;
		ClusterFeature entryA = null, entryB = null;
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
		ClusterFeature[] farthestTwo = new ClusterFeature[2];
		farthestTwo[0] = entryA;
		farthestTwo[1] = entryB;
		return farthestTwo;
	}
	
	public ClusterFeature getClosest(BinnedPeakList entry) {
		float minDistance = Float.MAX_VALUE;
		float thisDistance;
		ClusterFeature minCF = null;
		for (int i = 0; i < cfs.size(); i++) {
			BinnedPeakList list = cfs.get(i).getCentroid();
			thisDistance = list.getDistance(entry,DistanceMetric.CITY_BLOCK);
			if (thisDistance < minDistance) {
				minDistance = thisDistance;
				minCF = cfs.get(i);
			}
		}
		return minCF;
	}
	
	public void updateLeafPointers(ClusterFeature parent, CFNode prev, CFNode next) {
		parentCF = parent;
		if (parent == null)
			parentNode = null;
		else
			parentNode = parent.curNode;
		prevLeaf = prev;
		nextLeaf = next;
	}
	
	public void updateNonLeafPointers(ClusterFeature parent) {
		parentCF = parent;
		if (parent == null)
			parentNode = null;
		else
			parentNode = parent.curNode;
		parentNode = parent.curNode;
	}
	
	// {Node, CLusterFeature, Child}
	public ClusterFeature clearNode() {
		ClusterFeature returnThis = parentCF;
		parentNode = null;
		parentCF = null;
		nextLeaf = null;
		prevLeaf = null;
		cfs.clear();
		return returnThis;
	}
	
	// print the node
	public void printNode(String delimiter) {
		System.out.println(delimiter + "parent: " + parentNode);
		System.out.println(delimiter + "node: " + this);
		//System.out.println(delimiter + "# cfs: " + getSize());
		//System.out.println(delimiter + "prev leaf: " + prevLeaf);
		//System.out.println(delimiter + "next leaf: " + nextLeaf);
		//System.out.println(delimiter + "Node's cfs:");
		for (int i = 0; i < cfs.size(); i++) {
			cfs.get(i).printCF(delimiter);
		}
	}
	
	public boolean isLeaf() {
		if (cfs.size() == 0) 
			return true;
		if (cfs.get(0).child == null)
			return true;
		return false;
	}
	
	// get the number of cluster features
	public int getSize() {
		return cfs.size();
	}
	
	// get the cluster feature arraylist
	public ArrayList<ClusterFeature> getCFs() {
		return cfs;
	}	
}
