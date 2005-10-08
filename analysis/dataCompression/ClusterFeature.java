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
 * The Original Code is EDAM Enchilada's ClusterFeature class.
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
import java.util.Iterator;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.DummyNormalizer;

/**
 * 
 * @author ritza
 *
 * ClusterFeature contains the information for each cluster feature
 * in the CFTree.  It includes the number of particles in the cf, the sums of
 * the particles, the sum of the squares of the particles, and a child node
 * (if there is one).
 * 
 * TODO: All binnedPeakLists are automatically DummyNormalizer; is this right?
 * 
 */

public class ClusterFeature {
	private int count;
	private BinnedPeakList sums;
	private float squareSums;
	public CFNode child = null;
	public CFNode curNode;
	private ArrayList<Integer> atomIDs;
	
	/**
	 * Constructor
	 * @param cur - current node
	 */
	public ClusterFeature(CFNode cur) {
		count = 0;
		sums = new BinnedPeakList(new DummyNormalizer());
		squareSums = 0;
		curNode = cur;
		atomIDs = new ArrayList<Integer>();
	}
	
	/**
	 * Constructor
	 * @param cur - current node
	 * @param c - count
	 * @param s1 - sums peaklist
	 * @param s2 - sum of sqaures
	 * @param ids - atomids
	 */
	public ClusterFeature(CFNode cur, int c, BinnedPeakList s1, float s2, ArrayList<Integer> ids) {
		curNode = cur;
		count = c;
		sums = s1;
		squareSums = s2;
		atomIDs = ids;
	}
	
	/**
	 * Updates the cf by adding a peaklist to it.
	 * @param list - binnedPeakList
	 * @param atomID - atomID
	 */
	public void updateCF(BinnedPeakList list, int atomID) {
		count++;
		sums.addAnotherParticle(list);
		BinnedPeak peak;
		Iterator<BinnedPeak> iterator = list.iterator();
		while (iterator.hasNext()) {
			peak = iterator.next();
			squareSums += peak.value*peak.value;
		}
		atomIDs.add(new Integer(atomID));
	}
	
	/**
	 * Updates the CF by adding the cfs in its child.
	 * @return true if successful, false if there's no child.
	 */
	public boolean updateCF() {
		if (child == null || child.getCFs().size() == 0) 
			return false;
		atomIDs.clear();
		ArrayList<ClusterFeature> cfs = child.getCFs();
		count = 0;
		sums = new BinnedPeakList(new DummyNormalizer());
		squareSums = 0;
		for (int i = 0; i < cfs.size(); i++) {
			count += cfs.get(i).count;
			sums.addAnotherParticle(cfs.get(i).getSums());
			squareSums += cfs.get(i).squareSums;
			atomIDs.addAll(cfs.get(i).getAtomIDs());
		}
		return true;
	}
	
	/**
	 * Updates the child and the currentNode.
	 * @param newChild - new child
	 * @param newCurNode - new current node.
	 */
	public void updatePointers(CFNode newChild, CFNode newCurNode) {
		child = newChild;
		curNode = newCurNode;
	}

	/**
	 * Tests for equality with another cluster feature. Note: i don't check 
	 * for same parent/child yet. Note: I don't check for atomIDs, since they 
	 * might not be around in the end.
	 * @param cf - cf to compare
	 * @return true if they are the same, false otherwise.
	 */
	public boolean isEqual(ClusterFeature cf) {
		if (cf.getCount() != count || cf.getSums().length() != sums.length())
			return false;
		
		if (squareSums != cf.squareSums)
			return false;
		
		Iterator<BinnedPeak> sumsA = sums.iterator();
		Iterator<BinnedPeak> sumsB = cf.getSums().iterator();
		BinnedPeak peakA;
		BinnedPeak peakB;
		while (sumsA.hasNext()) {
			peakA = sumsA.next();
			peakB = sumsB.next();
			if (peakA.value != peakB.value || peakA.key != peakB.key)
				return false;
		}
		return true;
	}
	
	/**
	 * prints the cluster feature
	 * @param delimiter - delimiter for the level
	 */
	public void printCF(String delimiter) {
		//System.out.print(delimiter + "CF: " + this);
		System.out.println(delimiter + "Count: " + count);
	}
	
	
	/**
	 * sets the count
	 * @param c - new count
	 */
	public void setCount(int c) {
		count = c;
	}
		
	/**
	 * gets the count
	 * @return - count
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * gets the sums
	 * @return - sums
	 */
	public BinnedPeakList getSums() {
		return sums;
	}
	
	/**
	 * sets the sum of squares
	 * @param s - new sum of squares
	 */
	public void setSumOfSquares(float s) {
		squareSums = s;
	}
	
	/**
	 * gets the sum of squares
	 * @return - sum of squares
	 */
	public float getSumOfSquares() {
		return squareSums;
	}
	
	/**
	 * Gets the centroid for the CF.
	 * @return - cf's centroid
	 */
	public BinnedPeakList getCentroid() {
		BinnedPeakList list = new BinnedPeakList(new DummyNormalizer());
		Iterator<BinnedPeak> iterator = sums.iterator();
		BinnedPeak next;
		while (iterator.hasNext()) {
			next = iterator.next();
			list.addNoChecks(next.key, next.value / count);
		}
		list.normalize(DistanceMetric.CITY_BLOCK);
		return list;
	}
	
	public ArrayList<Integer> getAtomIDs() {
		return atomIDs;
	}

}
