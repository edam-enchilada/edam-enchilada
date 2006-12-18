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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;

/**
 * 
 * @author ritza
 * @author christej
 *
 * ClusterFeature contains the information for each cluster feature
 * in the CFTree.  It includes the number of particles in the cf, the sums of
 * the particles, the sum of the squares of the particles, and a child node
 * (if there is one).
 * 
 *
 * 
 */
public class ClusterFeature {
	private int count; //num particles represented
	private float magnitude; // magnitude before being normalized
	private BinnedPeakList sums;
	private float squareSums;
	public CFNode child = null;
	public CFNode curNode; //node it belongs to
	private ArrayList<Integer> atomIDs;
	private DistanceMetric dMetric;
	
	private long memory=0;
	
	/**
	 * Constructor
	 * @param cur - current node
	 */
	public ClusterFeature(CFNode cur, DistanceMetric d) {
		count = 0;
		dMetric = d;
		sums = new BinnedPeakList(new Normalizer());
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
	public ClusterFeature(CFNode cur, int c, BinnedPeakList s1, float s2,
			ArrayList<Integer> ids) {
		curNode = cur;
		dMetric = cur.dMetric;
		count = c;
		magnitude = s1.getMagnitude(dMetric);
		sums = s1;
		squareSums = s2;
		atomIDs = ids;
		memory=8*sums.length()+4*atomIDs.size();
	}

	/**
	 * Constructor
	 * @param cur - current node
	 * @param c - count
	 * @param s1 - sums peaklist
	 * @param s2 - sum of sqaures
	 * @param ids - atomids
	 * @param mag - magnitude
	 */
	public ClusterFeature(CFNode cur, int c, BinnedPeakList s1, float s2,
			ArrayList<Integer> ids, float mag) {
		curNode = cur;
		dMetric = cur.dMetric;
		count = c;
		magnitude = mag;
		sums = s1;
		squareSums = s2;
		atomIDs = ids;
		memory=8*sums.length()+4*atomIDs.size();
	}

	/**
	 * Updates the cf by adding a peaklist to it.
	 * @param list - binnedPeakList
	 * @param atomID - atomID
	 */
	public void updateCF(BinnedPeakList list, int atomID, boolean normalized) {
		assert(normalized) : "BIRCH only tested for normalized data";
		
		int oldPeakListMem = 8*sums.length();
		atomIDs.add(new Integer(atomID));
		sums.multiply(magnitude);
		sums.addAnotherParticle(list);
		count++;
		magnitude = sums.posNegNormalize(dMetric);
		// calculate the square sums.
		Entry<Integer, Float> peak;
		Iterator<Map.Entry<Integer, Float>> iterator = list.getPeaks().entrySet().iterator();
		while (iterator.hasNext()) {
			peak = iterator.next();
			squareSums += peak.getValue()*peak.getValue();
		}
		
		memory+= (8*sums.length()-oldPeakListMem)+4;
	}
	
	/**
	 * Updates the CF by adding the cfs in its child.
	 * @return true if successful, false if there's no child.
	 */
	public boolean updateCF() {
		if (child == null || child.getCFs().size() == 0) {
			float testMag;
			// Yes, single equals below: I'm calculating the magnitude
			// only once to save time (dmusican)
			assert ((testMag=sums.getMagnitude(dMetric)) > .9999 &&
					 testMag < 1.0001) :
					 "Cluster feature not normalized like it should be:" +
					 testMag;
			return false;
		}
		atomIDs = new ArrayList<Integer>();
		ArrayList<ClusterFeature> cfs = child.getCFs();
		count = 0;
		squareSums = 0;
		HashMap<Integer, Float> tempSums = new HashMap<Integer, Float>(1000);
		for (int i = 0; i < cfs.size(); i++) {
			tempSums = cfs.get(i).getSums().addWeightedToHash(tempSums, cfs.get(i).magnitude);
			count += cfs.get(i).count;
			squareSums += cfs.get(i).squareSums;
			atomIDs.addAll(cfs.get(i).getAtomIDs());
		}
		// sped up by dmusican
		sums = new BinnedPeakList(new Normalizer(),tempSums);
		/*Set<Entry<Integer, Float>> keys = tempSums.entrySet();
		for(Entry<Integer, Float> x: keys) {
			sums.addNoChecks(x.getKey(), x.getValue());
		}*/

		magnitude = sums.posNegNormalize(dMetric);
		memory= 8*sums.length()+4*atomIDs.size();
		return true;
	}
	
	/**
	 * Updates the child and the currentNode.
	 * @param newChild - new child
	 * @param newCurNode - new current node.
	 */
	public void updatePointers(CFNode newChild, CFNode newCurNode) {
		if(newChild!=null)
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
		
		Iterator<Map.Entry<Integer, Float>> sumsA = cf.getSums().getPeaks().entrySet().iterator();
		Iterator<Map.Entry<Integer, Float>> sumsB = sums.getPeaks().entrySet().iterator();
		Entry<Integer, Float> peakA;
		Entry<Integer, Float> peakB;
		while (sumsA.hasNext()) {
			peakA = sumsA.next();
			peakB = sumsB.next();
			if (peakA.getKey().intValue() != peakB.getKey().intValue()) {
				System.out.print(peakA.getKey()+", =?= "+peakB.getKey());
					System.out.println("FALSE");
					return false;
			}
			if (peakA.getValue().floatValue() != peakB.getValue().floatValue() ){
				System.out.println(peakA.getValue() + " " + peakB.getValue());
				return false;
			}

		}
		return true;
	}
	
	/**
	 * prints the cluster feature
	 * @param delimiter - delimiter for the level
	 */
	public void printCF(String delimiter) {
		//System.out.print(delimiter + "CF: " + this);
		//System.out.print(delimiter+ "CF : ");
		//System.out.println(delimiter+"CF magnitude: "+sums.getMagnitude(dMetric));
		//System.out.println(delimiter+"CF Count: " + count);
		Object[] atoms = (atomIDs.toArray());
		Arrays.sort(atoms);
		System.out.print(delimiter + "CF::: " + count +" (");
		for (int i = 0; i < atomIDs.size(); i++) {
		//	System.out.print(atomIDs.get(i) + " ");
			System.out.print(atoms[i] + " ");
		}
		System.out.println(")");
		System.out.printf("Original magnitude = %10.3f\n",magnitude);
		//sums.printPeakList();
		//System.out.println(delimiter+"CF SS: " + squareSums);
		//System.out.println(delimiter+"CF Magnitude: " + sums.getMagnitude(dMetric));
		//System.out.println(delimiter+"CF length: " + sums.length());
		//System.out.println(delimiter+"AtomList length: " + atomIDs.size());
		//System.out.println(delimiter+"memory: " + memory);
		//System.out.println(delimiter+"child node: " + child);
	}
	
	public CFNode getChild(){
		return child;
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
	public void setSums(BinnedPeakList s) {
		sums = s;
		Iterator<Map.Entry<Integer, Float>> iterator = s.getPeaks().entrySet().iterator();
		Entry<Integer, Float> peak;
		squareSums = 0;
		while (iterator.hasNext()) {
			peak = iterator.next();
			squareSums += peak.getValue()*peak.getValue();
		}
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
	
	public ArrayList<Integer> getAtomIDs() {
		return atomIDs;
	}
	
	public long getMemory(){
		return memory;
	}
	
	public void absorbCF(ClusterFeature absorbed) {
		sums.multiply(magnitude);
		absorbed.sums.multiply(absorbed.magnitude);
		sums.addAnotherParticle(absorbed.getSums());
		magnitude = sums.posNegNormalize(dMetric);
		squareSums+=absorbed.getSumOfSquares();
		count+=absorbed.getCount();
		atomIDs.addAll(absorbed.getAtomIDs());
	}
	
	public void makeSumsSparse(){
		BinnedPeakList newSums = new BinnedPeakList(new Normalizer());
		Iterator<BinnedPeak> iter = sums.iterator();
		BinnedPeak p;
		while (iter.hasNext()) {
			p = iter.next();
			if (p.value!=0) {
				newSums.add(p);
			}
		}
		sums = newSums;
	}

	public void containsZeros() {
		Iterator<BinnedPeak> iter = sums.iterator();
		BinnedPeak p;
		while (iter.hasNext()) {
			p = iter.next();
			assert (p.value!=0) : "p.value!=0";
		}
	}
	
	/**
	 * Accessor method for magnitude
	 * @return magnitude
	 */
	public float getMagnitude() {
		return magnitude;
	}

	/**
	 * Mutator method for magnitude
	 * @param magnitude - the original magnitude of this cf before normalizing
	 * @author dmusican
	 */
	public void setMagnitude(float magnitude) {
		this.magnitude = magnitude;
	}
}
