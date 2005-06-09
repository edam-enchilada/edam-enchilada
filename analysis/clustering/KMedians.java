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
 * The Original Code is EDAM Enchilada's KMedians class.
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


/*
 * Created on Aug 19, 2004
 *
 */
package analysis.clustering;

import java.util.*;

import analysis.*;
import database.CollectionCursor;
import database.InfoWarehouse;

/**
 * @author andersbe
 */
public class KMedians extends ClusterK {
	
	
	/**
	 * @param cID
	 * @param database
	 */
	public KMedians(int cID, InfoWarehouse database, int k,
			String name, String comment, boolean refine) 
	{
		super(cID, database, k, 
				name.concat("KMedians"), comment, refine);
	}

	public int cluster() {
		return divide();
	}

	public Centroid averageCluster(
			Centroid origCentroids,
			ArrayList<Integer> particlesInCentroid,
			CollectionCursor curs)
	{
		ArrayList<BinnedPeakList> medianThis = 
			new ArrayList<BinnedPeakList>(particlesInCentroid.size());
		int atomID = 0;
//		 Loop through the particles in the centroid and add the areas together.
		for (int i = 0; i < particlesInCentroid.size(); i++) 
		{
			// Using the atomID, find the atom's peak list.
			atomID = particlesInCentroid.get(i).intValue();
			medianThis.add(normalize(
					curs.getPeakListfromAtomID(atomID)));
		}
		Centroid returnThis = null;
		MedianFinder mf = null;
		//try
		//{
		if (medianThis.size() == 0)
		{
			System.out.println("Centroid contains no particles");
			returnThis = new Centroid(
					new BinnedPeakList(),
					0,
					origCentroids.subCollectionNum);
		}
		else
		{
			mf = new MedianFinder(medianThis);
			//Simply use getMedian for this to work the old way
			// TODO:
			returnThis = new Centroid(
					//mf.getMedian(),
					mf.getMedianSumToOne(), 
					0,
					origCentroids.subCollectionNum);
			//returnThis.peaks = normalize(returnThis.peaks);
		}
		return returnThis;
	}
	
	/*public Centroid averageCluster(
			Centroid origCentroids,
			ArrayList<Integer> particlesInCentroid,
			CollectionCursor curs) {		
		ArrayList<ArrayList<Float>> nonZeros = new ArrayList<ArrayList<Float>>();
		ArrayList<Integer> zeroCounter = new ArrayList<Integer>();
		BinnedPeakList newCentroidPeaks = new BinnedPeakList();
		
		int atomID;
		float curArea;
		float median;
		int incrementZero;
		int particlesLeftInCentroid = particlesInCentroid.size();
		Integer initializedZero = new Integer(0);
		BinnedPeakList curPeakList= new BinnedPeakList();
		BinnedPeak addedPeak;
		int currentIndex = -1;
		int[] locs = findFirstAndLastLocations(particlesInCentroid);
		
		// While there is still something in the particlesInCentroid array, keep adding
		// to nonZeros array and zeroCounter array by incrementing the location.
		for (int currentLocation = locs[0]; currentLocation < locs[1]; currentLocation++) {
			//System.out.println(particlesLeftInCentroid);
			currentIndex++;
			nonZeros.add(new ArrayList<Float>());
			zeroCounter.add(initializedZero);
			// Loop through every particle assigned to the centroid. 
			for (int curPart = 0; curPart < particlesInCentroid.size(); curPart++) {
				atomID = particlesInCentroid.get(curPart).intValue();
				curPeakList = curs.getPeakListfromAtomID(atomID);
				curArea = curPeakList.getAreaAt(currentLocation);
				// If the area at currentLocation is zero, increment zeroCounter.
				if (curArea == 0.0f) {
					incrementZero = zeroCounter.get(currentIndex).intValue() + 1;
					zeroCounter.set(currentIndex,new Integer(incrementZero));
				}
				// Else, add the area to nonZeros at the currentLocation.
				else {
					nonZeros.get(currentIndex).add(new Float(curArea));
				}
				// If this is the last location in the particle, remove it.
				if (curPeakList.getLastLocation() == currentLocation) 
					particlesLeftInCentroid--;
				
				// If more than half of the particles are zeros, then break out of loop.
				if (zeroCounter.get(currentIndex).intValue() 
						> particlesInCentroid.size()/2)
					break;
			}
			// If there are more areas than zeros, then find the median and
			// add it to the new centroid peak list.
			if (zeroCounter.get(currentIndex).intValue() 
					< nonZeros.get(currentIndex).size()) {
					median = getMedian(nonZeros.get(currentIndex), 
							zeroCounter.get(currentIndex).intValue());
					newCentroidPeaks.add(currentLocation, median);
			}
		}
		newCentroidPeaks = normalize(newCentroidPeaks);
		Centroid newCent = new Centroid(newCentroidPeaks, 0);
		return newCent;
	}*/
	
	/*public int[] findFirstAndLastLocations(ArrayList<Integer> particles) {
		int atomID;
		int curLoc;
		int minLocation = 30000;
		int maxLocation = -30000;
		for (int i = 0; i < particles.size(); i++) {
			atomID = particles.get(i).intValue();
			curLoc = curs.getPeakListfromAtomID(atomID).getFirstLocation();	
			if (curLoc < minLocation) 
				minLocation = curLoc;
			if (curLoc > minLocation) 
				maxLocation = curLoc;
		}
		int[] locs = {minLocation, maxLocation};
		return locs;
	}
	
	public float getMedian(ArrayList<Float> array, int zeros) {
		Float median;
		Collections.sort(array);
		// add the correct number of zeros:
		for (int j = 0; j < zeros; j++) 
			array.add(0,new Float(0));
		int size = array.size();
		//find the median:
		if (size == 1)
			median = array.get(0);
		else if (size == 2) 
			median = array.get(1);
		else if (even((double)size)) 
			median = array.get(size/2);
		else 
			median = array.get(size/2+1);
		
		return median.floatValue();
	}
	
	public boolean even(double a) {
		while (a > 1.0)
			a = a/2.0;
		if (a == 0.0) 
			return true;
		else 
			return false;
	}*/
}