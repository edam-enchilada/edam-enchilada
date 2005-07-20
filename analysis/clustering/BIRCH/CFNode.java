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
	
	// print the node
	public void printNode(String delimiter) {
		//System.out.println(delimiter + "parent: " + parentNode);
		//System.out.println(delimiter + "node: " + this);
		System.out.println(delimiter + "# cfs: " + getSize());
		//System.out.println(delimiter + "prev leaf: " + prevLeaf);
		//System.out.println(delimiter + "next leaf: " + nextLeaf);
		System.out.println(delimiter + "Node's cfs:");
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
	
	public static void main(String[] args) {
		CFNode node = new CFNode(null);
		ClusterFeature cf1 = new ClusterFeature(node);
		ClusterFeature cf2 = new ClusterFeature(node);
		BinnedPeakList list1 = new BinnedPeakList();
		list1.add(0,1);
		list1.add(1,4);
		list1.add(2,0);
		list1.add(3,10);
		BinnedPeakList list2 = new BinnedPeakList();
		list2.add(0,0);
		list2.add(1,3);
		list2.add(2,5);
		list2.add(3,0);
		BinnedPeakList list3 = new BinnedPeakList();
		list3.add(0,1);
		list3.add(1,2);
		list3.add(2,3);
		list3.add(3,4);
		cf1.updateCF(list1,1);
		cf1.updateCF(list2,2);
		cf2.updateCF(list3,3);
		node.addCF(cf1);
		node.addCF(cf2);
		
		CFNode leafNode = new CFNode(cf1);
		ClusterFeature cf3 = new ClusterFeature(leafNode);
		BinnedPeakList list4 = new BinnedPeakList();
		list4.add(0,5);
		list4.add(1,0);
		list4.add(2,1);
		list4.add(3,1);
		cf3.updateCF(list4,4);
		leafNode.addCF(cf3);
		cf1.child = leafNode;
		
		System.out.println("Printing parentCF node:");
		node.printNode("");
		System.out.println();
		System.out.println("Printing leaf node:");
		leafNode.printNode("");
		
		ClusterFeature cf4 = new ClusterFeature(node);
		BinnedPeakList list5 = new BinnedPeakList();
		list5.add(0,1);
		list5.add(1,2);
		list5.add(2,3);
		list5.add(3,4);
		cf4.updateCF(list5,5);

		System.out.println();
		System.out.println("Removed node? " + node.removeCF(cf4));
		System.out.println("New Parent Node:");
		node.printNode("");
	}
	
}
