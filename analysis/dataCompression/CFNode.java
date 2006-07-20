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

package analysis.dataCompression;

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
	/* Class Variables */
	private ArrayList<ClusterFeature> cfs;
	public ClusterFeature parentCF = null;
	public CFNode parentNode = null;
	
	public CFNode prevLeaf = null;
	public CFNode nextLeaf = null;
	
	public DistanceMetric dMetric;
	/**
	 * Constructor
	 * @param p - parent cluster feature
	 */
	public CFNode(ClusterFeature p, DistanceMetric d) {
		cfs = new ArrayList<ClusterFeature>();
		parentCF = p;
		if (parentCF != null) 
			parentNode = p.curNode;
		dMetric = d;
	}
	public boolean sameContents(CFNode nodeToCompare) {
		boolean same = true;
		int i = 0;
		for (i = 0; i < cfs.size(); i++) {
	//		System.out.ln("nodeToCompare");
	//		nodeToCompare.getCFs().get(i).printCF("");
	//		System.out.println("node");
	//		cfs.get(i).printCF("");
			if(!nodeToCompare.getCFs().get(i).isEqual((cfs.get(i)))) {
				same = false;
				break;
			}
		}
		if(i > nodeToCompare.getCFs().size())
			same = false;
		return same;
	}
	/**
	 * Adds a cluster feature
	 * @param cf - cf to add
	 */
	public void addCF(ClusterFeature cf){
		cfs.add(cf);
		cf.curNode = this;
	}
	
	/**
	 * Removes a cluster feature
	 * @param cf - cf to remove
	 * @return true if successful, false otherwise.
	 */
	public boolean removeCF(ClusterFeature cf) {
		for (int i = 0; i < cfs.size(); i++) {
			if (cfs.get(i).isEqual(cf)) {
				cfs.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns the two closest cluster features (cf1 and cf2) in this node.  
	 * The returned array is of the form {cf1,cf2}.
	 * @return the array above
	 */
	public ClusterFeature[] getTwoClosest() {
		if (cfs.size() < 2) {
			return null;
		}
		float minDistance = Float.MAX_VALUE;
		float thisDistance;
		ClusterFeature entryA = null, entryB = null;
		BinnedPeakList listI, listJ;
		for (int i = 0; i < cfs.size(); i++) {
			listI = cfs.get(i).getSums();
			for (int j = i; j < cfs.size(); j++) {
				listJ = cfs.get(j).getSums();
				thisDistance = listI.getDistance(listJ,
						dMetric);
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
	
	/**
	 * returns the two farthest cluster features (cf1 and cf2) in this node.  
	 * The returned array is of the form {cf1,cf2}.
	 * @return the array above
	 */
	public ClusterFeature[] getTwoFarthest() {
		if (cfs.size() < 2) {
			return null;
		}
		float maxDistance = Float.MIN_VALUE;
		float thisDistance;
		ClusterFeature entryA = null, entryB = null;
		BinnedPeakList listI, listJ;
		for (int i = 0; i < cfs.size(); i++) {
			listI = cfs.get(i).getSums();
			for (int j = i; j < cfs.size(); j++) {
				listJ = cfs.get(j).getSums();
				thisDistance = listI.getDistance(listJ,
						dMetric);
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
	
	/**
	 * Gets the closest CF in the node to the given entry.
	 * @param entry - binnedPeakList to compare
	 * @return - closest CF
	 */
	public ClusterFeature getClosest(BinnedPeakList entry) {
		float minDistance = Float.MAX_VALUE;
		float thisDistance;
		ClusterFeature minCF = null;
		BinnedPeakList list; 
		for (int i = 0; i < cfs.size(); i++) {
			if (cfs.get(i).getCount() != 0) {
				list = cfs.get(i).getSums();
				thisDistance = list.getDistance(entry,dMetric);
				if (thisDistance < minDistance) {
					minDistance = thisDistance;
					minCF = cfs.get(i);
				}
			}
		}
		return minCF;
	}
	
	
	/**
	 * Updates the parent for the node and its CFs.
	 * @param parent - new parent CF
	 */
	public void updateParent(ClusterFeature parent) {
			
		parentCF = parent;
		if (parent == null)
			parentNode = null;
		else
			parentNode = parent.curNode;
		if (!isLeaf()) {
			for (int i = 0; i < cfs.size(); i++) {
				cfs.get(i).updatePointers(cfs.get(i).child, this);
				cfs.get(i).child.parentCF = cfs.get(i);
				cfs.get(i).child.parentNode = this;
			}
		}
	}
	
	/**
	 * clears the node.
	 * @return the parentCF
	 */
	public ClusterFeature clearNode() {
		ClusterFeature returnThis = parentCF;
		parentNode = null;
		parentCF = null;
		nextLeaf = null;
		prevLeaf = null;
		
		cfs.clear();
		return returnThis;
	}
	
	/**
	 * Prints the node
	 * @param delimiter - delimiter for the given level in the tree
	 */
	public void printNode(String delimiter) {
	//	System.out.println(delimiter + "node: " + this);
		//System.out.println(delimiter + "  parent node: " + parentNode);
		//System.out.println(delimiter + "  parent CF: " + parentCF);
		//System.out.println(delimiter+"memory: " + getMemory());
		for (int i = 0; i < cfs.size(); i++) {
			cfs.get(i).printCF(delimiter);
		}
	}
	
	/**
	 * Determines if the given node is a leaf or not.
	 * @return - true if it's a leaf, false otherwise.
	 */
	public boolean isLeaf() {
		if (cfs.size() == 0) 
			return true;
		if (cfs.get(0).child == null)
			return true;
		return false;
	}
	
	/**
	 * get the number of CFs
	 * @return - size
	 */
	public int getSize() {
		return cfs.size();
	}
	
	/**
	 * Gets the cluster feature array
	 * @return - cf array
	 */
	public ArrayList<ClusterFeature> getCFs() {
		return cfs;
	}	
	
	public long getMemory(){
		long returnThis = 50*cfs.size(); //Conservative estimate.
		for (int i = 0; i < cfs.size(); i++)
			returnThis+=cfs.get(i).getMemory();
		return returnThis;
	}
	/*public static void main(String args) {
		CFNode node = new CFNode();
		node.cfs.add(new ClusterFeature())
		
	}*/
}
