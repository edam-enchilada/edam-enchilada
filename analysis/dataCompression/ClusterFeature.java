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
import analysis.Normalizer;

/**
 * 
 * @author ritza
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
	private int count;
	private BinnedPeakList sums;
	private float squareSums;
	public CFNode child = null;
	public CFNode curNode;
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
	public ClusterFeature(CFNode cur, int c, BinnedPeakList s1, float s2, ArrayList<Integer> ids) {
		curNode = cur;
		dMetric = cur.dMetric;
		count = c;
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
	public void updateCF(BinnedPeakList list, int atomID) {
		//System.out.println("SS: " + squareSums);
		//System.out.println("mag: "+sums.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED));
		int oldPeakListMem = 8*sums.length();
		
		atomIDs.add(new Integer(atomID));
		sums.multiply(count);
		sums.addAnotherParticle(list);
		count++;
		sums.normalize(dMetric);
		makeSumsSparse();
		// calculate the square sums.
		BinnedPeak peak;
		Iterator<BinnedPeak> iterator = list.iterator();
		while (iterator.hasNext()) {
			peak = iterator.next();
			squareSums += peak.value*peak.value;
		}
		
		memory+= (8*sums.length()-oldPeakListMem)+4;
	}
	
	/**
	 * Updates the CF by adding the cfs in its child.
	 * @return true if successful, false if there's no child.
	 */
	public boolean updateCF() {
		if (child == null || child.getCFs().size() == 0) 
			return false;
		atomIDs = new ArrayList<Integer>();
		ArrayList<ClusterFeature> cfs = child.getCFs();
		count = 0;
		sums = new BinnedPeakList(new Normalizer());
		squareSums = 0;
		BinnedPeakList temp;
		for (int i = 0; i < cfs.size(); i++) {
			temp = new BinnedPeakList();
			temp.copyBinnedPeakList(cfs.get(i).getSums());
			temp.multiply(cfs.get(i).count);
			sums.addAnotherParticle(temp);
			count += cfs.get(i).count;
			squareSums += cfs.get(i).squareSums;
			atomIDs.addAll(cfs.get(i).getAtomIDs());
		}
		sums.normalize(dMetric);
		makeSumsSparse();
		memory= 8*sums.length()+4*atomIDs.size();
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
			if (peakA.value != peakB.value || peakA.key != peakB.key) {
				System.out.print(peakA.key+","+peakA.value+" =?= "+peakB.key+","+peakB.value);
					System.out.println("FALSE");
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
		System.out.print(delimiter + "CF::: " + count +" (");
		for (int i = 0; i < atomIDs.size(); i++)
			System.out.print(atomIDs.get(i) + " ");
		System.out.println(")");
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
		sums.multiply(count);
		absorbed.sums.multiply(absorbed.count);
		sums.addAnotherParticle(absorbed.getSums());
		sums.normalize(dMetric);
		makeSumsSparse();
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
}
