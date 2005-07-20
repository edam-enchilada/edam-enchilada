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
 * ClusterFeature contains the information for each cluster feature
 * in the CFTree.  It includes the number of particles in the cf, the sums of
 * the particles, the sum of the squares of the particles, and a child node
 * (if there is one).
 * 
 */

public class ClusterFeature {
	private ArrayList<Integer> atomIDs;
	private int count;
	private BinnedPeakList sums;
	private BinnedPeakList squareSums;
	public CFNode child = null;
	public CFNode curNode;
	
	// Constructor
	public ClusterFeature(CFNode cur) {
		count = 0;
		sums = new BinnedPeakList();
		squareSums = new BinnedPeakList();
		curNode = cur;
		atomIDs = new ArrayList<Integer>();
	}
	
	// Constructor
	public ClusterFeature(CFNode cur, int c, BinnedPeakList s1, BinnedPeakList s2, ArrayList<Integer> ids) {
		curNode = cur;
		count = c;
		sums = s1;
		squareSums = s2;
		atomIDs = ids;
	}
	
	// Updates the cf by adding a peaklist to it
	public void updateCF(BinnedPeakList list, int atomID) {
		list = normalize(list, DistanceMetric.CITY_BLOCK);
		count++;
		sums.addAnotherParticle(list);
		BinnedPeakList squares = new BinnedPeakList();
		BinnedPeak peak;
		Iterator<BinnedPeak> iterator = list.iterator();
		while (iterator.hasNext()) {
			peak = iterator.next();
			squareSums.add(peak.location, peak.area*peak.area);
		}
		sums = normalize(sums, DistanceMetric.CITY_BLOCK);
		squareSums = normalize(squareSums, DistanceMetric.CITY_BLOCK);
		atomIDs.add(new Integer(atomID));
	}
	
	// updates the cf by adding the cfs in its child.
	// returns false if there's no child
	public boolean updateCF() {
		if (child == null)
			return false;
		count = 0;
		sums = new BinnedPeakList();
		squareSums = new BinnedPeakList();
		ArrayList<ClusterFeature> cfs = child.getCFs();
		atomIDs.clear();
		for (int i = 0; i < cfs.size(); i++) {
			count += cfs.get(i).count;
			sums.addAnotherParticle(cfs.get(i).getSums());
			squareSums.addAnotherParticle(cfs.get(i).getSumOfSquares());
			atomIDs.addAll(cfs.get(i).atomIDs);
		}
		// TODO: only normalize sums??
		sums = normalize(sums, DistanceMetric.CITY_BLOCK);
		squareSums = normalize(squareSums, DistanceMetric.CITY_BLOCK);
		return true;
	}
	
	// updates the child and the current node in one shot.
	public void updatePointers(CFNode newChild, CFNode newCurNode) {
		child = newChild;
		curNode = newCurNode;
	}

	// tests for equality with another cluster feature.
	// Note: i don't check for same parent/child yet.
	// Note: I don't check for atomIDs, since they might not be around in the end.
	public boolean isEqual(ClusterFeature cf) {
		if (cf.getCount() != count || cf.getSums().length() != sums.length())
			return false;
		
		Iterator<BinnedPeak> sumsA = sums.iterator();
		Iterator<BinnedPeak> sumsB = cf.getSums().iterator();
		Iterator<BinnedPeak> sumsOfSquaresA = squareSums.iterator();
		Iterator<BinnedPeak> sumsOfSquaresB = cf.getSumOfSquares().iterator();
		BinnedPeak peakA;
		BinnedPeak peakB;
		while (sumsA.hasNext()) {
			peakA = sumsA.next();
			peakB = sumsB.next();
			if (peakA.area != peakB.area || peakA.location != peakB.location)
				return false;
			peakA = sumsOfSquaresA.next();
			peakB = sumsOfSquaresB.next();
			if (peakA.area != peakB.area || peakA.location != peakB.location)
				return false;
		}
		return true;
	}
	
	// prints the cluster feature
	public void printCF(String delimiter) {
		System.out.print(delimiter + "Count: " + count);
		System.out.print("  AtomIDs: ");
		for (int i = 0; i < atomIDs.size(); i++)
			System.out.print(atomIDs.get(i) + ", ");
		System.out.println();
	}
		
	// gets the count
	public int getCount() {
		return count;
	}
	
	// gets the sums
	public BinnedPeakList getSums() {
		return sums;
	}
	
	// gets the sums of the squares
	public BinnedPeakList getSumOfSquares() {
		return squareSums;
	}
	
	public ArrayList<Integer> getAtomIDs() {
		return atomIDs;
	}
	
	/**
	 * 
	 * TAKEN FROM CLUSTER!! TODO TODO TODO
	 * A method to produce a normalized BinnedPeakList from a
	 * non-normalized one.  Depending on which distance metric is
	 * used, this method will adapt to produce a distance of one 
	 * from <0,0,0,....,0> to the vector represented by the list
	 * @param 	list A list to normalize
	 * @return 	a new BinnedPeaklist that represents list 
	 * 			normalized.
	 */
	protected BinnedPeakList normalize(BinnedPeakList list, DistanceMetric distanceMetric)
	{
		float magnitude = list.getMagnitude(distanceMetric);
		BinnedPeakList returnList = new BinnedPeakList();
		BinnedPeak temp;
		Iterator<BinnedPeak> iter = list.iterator();
		while (iter.hasNext()) {
			temp = iter.next();
			if ((float)(temp.area / magnitude) != 0.0f)
				returnList.addNoChecks(temp.location, 
						temp.area / magnitude);
		}
		return returnList;
	}

}
