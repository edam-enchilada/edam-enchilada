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
 * The Original Code is EDAM Enchilada's Clusters class.
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
 */
package analysis.clustering;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
//import java.util.Arrays;

import database.CollectionCursor;
import database.InfoWarehouse;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.DistanceMetric;
import analysis.MedianFinder;
import analysis.ParticleInfo;

/**
 * @author andersbe
 * This abstract class implements methods specific to Cluster 
 * algorithms.
 */
public abstract class Cluster extends CollectionDivider {
	protected ArrayList<Double> totalDistancePerPass;
	protected int numPasses,collectionID;
	protected String parameterString;
	//protected final int NUM_REFINEMENTS = 3;
	
	/**
	 * Use the City Block or Manhattan distance metric both in
	 * normalization and in measuring distance.
	 */
	//public static final int CITY_BLOCK = 0;
	
	/**
	 * Use the Euclidean Squared distance metric both in 
	 * normalization and in measuring distance.
	 */
	//public static final int EUCLIDEAN_SQUARED = 1;
	
	//public static final int DOT_PRODUCT = 2;
	
	DistanceMetric distanceMetric = DistanceMetric.CITY_BLOCK;
	private static final int MAX_LOCATION = 2500;
	private static int DOUBLE_MAX = MAX_LOCATION * 2;
	private float[] longerLists = new float[MAX_LOCATION * 2];
	
	protected int zeroPeakListParticleCount = 0;
	protected int clusterCentroidIters = 0;
	protected int sampleIters = 0;
	
	/**
	 * Builds the cluster name a bit, then sends information off
	 * to the CollectionDivider constructor
	 * @param cID		The id of the collection to cluster
	 * @param database	An active InfoWarehouse
	 * @param name		A name to append to ",CLUST"
	 * @param comment	A comment for the cluster.
	 */
	public Cluster(int cID, InfoWarehouse database, String name, 
			String comment) {
		super(cID,database,name.concat(",CLUST"),comment);
	}
	
	/**
	 * See CollectionDivider for a description of how to 
	 * implement this method.
	 * @param method
	 * @return
	 */
	public abstract boolean setDistanceMetric(DistanceMetric method);
	
	/**
	 * Prints out each peak in a peaklist.  Used for reporting results.
	 * @param inputList		The list to print out
	 * @param out			A printwriter to print to
	 */
	protected void writeBinnedPeakListToFile(
			BinnedPeakList inputList,
			PrintWriter out)
	{
		out.println("Location:\tArea:");
		inputList.resetPosition();
		BinnedPeak tempPeak;
		for (int i = 0; i < inputList.length(); i++)
		{
			tempPeak = inputList.getNextLocationAndArea();
			out.println(tempPeak.location + "\t" + tempPeak.area);
		}
	}
	
	/**
	 * A method to produce a normalized BinnedPeakList from a
	 * non-normalized one.  Depending on which distance metric is
	 * used, this method will adapt to produce a distance of one 
	 * from <0,0,0,....,0> to the vector represented by the list
	 * @param 	list A list to normalize
	 * @return 	a new BinnedPeaklist that represents list 
	 * 			normalized.
	 */
	protected BinnedPeakList normalize(BinnedPeakList list)
	{
		float magnitude = list.getMagnitude(distanceMetric);
		
		
		BinnedPeakList returnList = new BinnedPeakList();
		BinnedPeak temp;
		list.resetPosition();
		for (int i = 0; i < list.length(); i++)
		{
			temp = list.getNextLocationAndArea();
			if ((float)(temp.area / magnitude) != 0.0f)
				returnList.addNoChecks(temp.location, 
						temp.area / magnitude);
		}
		return returnList;
	}
	
	// replaced by BinnedPeakList.getDistance
	/*
	 * Returns the distance between the vectors represented by
	 * two peaklists.  Uses whichever distance metric has been 
	 * set.
	 * @param list1 The first atom's peaklist.
	 * @param list2 The second atom's peaklist.
	 * @return the distance between the atoms.
	 */
	/*protected double getDistance(BinnedPeakList list1, 
			BinnedPeakList list2)
	{
		//TODO: Make this more graceful
		
		//This seems to take a 2 seconds longer?
		//Arrays.fill(longerLists, 0.0f);
		
	    // longerLists keeps track of which peak locations have nonzero areas
		for (int i = 0; i < DOUBLE_MAX; i++)
		{
			longerLists[i] = 0;
		}
		double distance = 0;
		BinnedPeakList longer;
		BinnedPeakList shorter;
		list1.resetPosition();
		list2.resetPosition();
		if (list1.length() < list2.length())
		{
			shorter = list1;
			longer = list2;
		}
		else
		{
			longer = list1;
			shorter = list2;
		}
		
		BinnedPeak temp;
		
		for (int i = 0; i < longer.length(); i++)
		{
			temp = longer.getNextLocationAndArea();
			longerLists[temp.location + MAX_LOCATION] = temp.area;
			//Do we need this?: - nope
			//bCheckedLocs[temp.location + MAX_LOCATION] = true;

			// Assume optimistically that each location is unmatched in the
			// shorter peak list.
			if (distanceMetric == DistanceMetric.CITY_BLOCK)
			    distance += temp.area;
			else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				distance += temp.area*temp.area;
			else if (distanceMetric == DistanceMetric.DOT_PRODUCT)
			    ; // If no match in shorter list, contributes nothing
			else {
			    assert false :
			        "Invalid distance metric: " + distanceMetric;
				distance = -1.0;
			}
		}	
		
		shorter.resetPosition();
		double eucTemp = 0;
		for (int i =  0; i < shorter.length(); i++)
		{
			temp = shorter.getNextLocationAndArea();
			if (longerLists[temp.location+MAX_LOCATION] != 0)
			{
				if (distanceMetric == DistanceMetric.CITY_BLOCK)
				{
					distance -= longerLists[temp.location+MAX_LOCATION];
				}
				else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				{
					distance -= longerLists[temp.location+MAX_LOCATION]*
						longerLists[temp.location+MAX_LOCATION];
				}
				else if (distanceMetric == DistanceMetric.DOT_PRODUCT)
				    ; // Again, nothing to subtract off here
				else {
				    assert false :
				        "Invalid distance metric: " + distanceMetric;
					distance = -1.0;
				}
				
				if (distanceMetric == DistanceMetric.CITY_BLOCK)
					distance += Math.abs(temp.area-longerLists[temp.location+MAX_LOCATION]);
				else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				{
					eucTemp = temp.area-longerLists[temp.location+MAX_LOCATION];
					distance += eucTemp*eucTemp;
				}
				else if (distanceMetric == DistanceMetric.DOT_PRODUCT) {
				    distance +=
				        temp.area*longerLists[temp.location+MAX_LOCATION];
				}
				else {
				    assert false :
				        "Invalid distance metric: " + distanceMetric;
					distance = -1.0;
				}
				
			}
			else
			{
				if (distanceMetric == DistanceMetric.CITY_BLOCK)
					distance += temp.area;
				else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
					distance += temp.area*temp.area;
				else if (distanceMetric == DistanceMetric.DOT_PRODUCT)
				    ; // Nothing to add here if new match
				else {
				    assert false :
				        "Invalid distance metric: " + distanceMetric;
					distance = -1.0;
				}
			}
			
		}
		
		// Dot product distance actually ranges from 0 to 1 (since data is
		// normalized). A value of 1 indicates two points are the same, 0
		// indicates completely different. In order to make rest of code work
		// (small distance is considered good), negate distance and 1 to it.
		// This places distance between 0 and 1 like other measures and doesn't
		// affect anything else. (Admittedly, this is a hack, but dot product
		// distance is ultimately the same thing as Euclidean squared anyway).
		if (distanceMetric == DistanceMetric.DOT_PRODUCT)
		    distance = 1-distance;

		assert distance < 2.01 :
		    "Distance should be <= 2.0, actually is " + distance;
		if (distance > 2) {
			//System.out.println("Rounding off " + distance +
			//		"to 2.0");
			distance = 2.0;
		}
		return distance;
	}*/
	
	/**
	 * Returns the distance between the vectors represented by
	 * two peaklists.  Uses whichever distance metric has been 
	 * set.
	 * @param list1 The first atom's peaklist.
	 * @param list2 The second atom's peaklist.
	 * @return the distance between the atoms.
	 */
	protected double ogetDistance(BinnedPeakList list1, 
			BinnedPeakList list2)
	{
		ArrayList<Integer> checkedLocations = 
			new ArrayList<Integer>();
		double distance = 0;
		BinnedPeakList longer;
		BinnedPeakList shorter;
		list1.resetPosition();
		list2.resetPosition();
		if (list1.length() < list2.length())
		{
			shorter = list1;
			longer = list2;
		}
		else
		{
			longer = list1;
			shorter = list2;
		}
		BinnedPeak temp;
		double shorterTemp;
		for (int i = 0; i < longer.length(); i++)
		{
			temp = longer.getNextLocationAndArea();
			checkedLocations.add(new Integer(temp.location));
			if (distanceMetric == DistanceMetric.CITY_BLOCK)
			{
				distance += Math.abs(temp.area - 
						shorter.getAreaAt(temp.location));
			}
			else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
			{
				shorterTemp = shorter.getAreaAt(temp.location);
				distance += (temp.area - shorterTemp)
				* (temp.area - shorterTemp);
			}
			else
			{
				distance = -1.0f;
			}
		}
		boolean alreadyChecked = false;
		for (int i = 0; i < shorter.length(); i++)
		{
			alreadyChecked = false;
			temp = shorter.getNextLocationAndArea();
			double longerTemp;
			for (Integer loc : checkedLocations)
			{
				if (temp.location == loc.intValue())
				{
					alreadyChecked = true;
				}
			}
			if (alreadyChecked)
				;
			else
			{
				if (distanceMetric == DistanceMetric.CITY_BLOCK)
				{
					distance += Math.abs(temp.area - 
							longer.getAreaAt(temp.location));
				}
				else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				{
					longerTemp = longer.getAreaAt(temp.location);
					distance += (temp.area - longerTemp) *
					(temp.area - longerTemp);
				}
				else
				{
					distance = -1.0f;
				}
			}
		}
		
		if (distance > 2) {
			System.out.println("Rounding off " + distance +
					"to 2.0");
			distance = 2.0f;
		}
		
		return distance;
	}

	protected void printDistanceToNearestCentroid(
			ArrayList<Centroid> centroidList,
			CollectionCursor curs)
	{
		int particleCount = 0;
		ParticleInfo thisParticleInfo = null;
		BinnedPeakList thisBinnedPeakList = null;
		double nearestDistance = 3.0;
		double totalDistance = 0.0;
		double distance = 3.0;
		int chosenCluster = -1;
		curs.reset();
		while(curs.next())
		{ // while there are particles remaining
			particleCount++;
			thisParticleInfo = curs.getCurrent();
			thisBinnedPeakList = thisParticleInfo.getBinnedList();
			thisBinnedPeakList = normalize(thisBinnedPeakList);
			// no centroid will be found further than the max distance (2.0)
			// since that centroid would not be considered
			nearestDistance = 3.0f;
			for (int centroidIndex = 0; 
			centroidIndex < centroidList.size(); 
			centroidIndex++)
			{// for each centroid
				distance = centroidList.get(centroidIndex).peaks.
					getDistance(thisBinnedPeakList, distanceMetric);
				if (distance < nearestDistance)
				{
					nearestDistance = distance;
					chosenCluster = centroidIndex;
				}
			}// end for each centroid
			Centroid temp = centroidList.get(chosenCluster);
			totalDistance += nearestDistance;
			
		}// end while there are particles remaining
		curs.reset();
		
		System.out.println("Stable Centroid average distance: " + 
				totalDistance/particleCount);
		
	}
	
	protected int assignAtomsToNearestCentroid(
			ArrayList<Centroid> centroidList,
			CollectionCursor curs)
	{
		
		int particleCount = 0;
		ParticleInfo thisParticleInfo = null;
		BinnedPeakList thisBinnedPeakList = null;
		double nearestDistance = 3.0;
		double totalDistance = 0.0;
		double distance = 3.0;
		int chosenCluster = -1;
		putInSubCollectionBatchInit();
		while(curs.next())
		{ // while there are particles remaining
			particleCount++;
			thisParticleInfo = curs.getCurrent();
			thisBinnedPeakList = thisParticleInfo.getBinnedList();
			thisBinnedPeakList = normalize(thisBinnedPeakList);
			// no centroid will be found further than the max distance (2.0)
			// since that centroid would not be considered
			nearestDistance = 3.0f;
			for (int centroidIndex = 0; 
			centroidIndex < centroidList.size(); 
			centroidIndex++)
			{// for each centroid
				distance = centroidList.get(centroidIndex).peaks.
					getDistance(thisBinnedPeakList, distanceMetric);
				if (distance < nearestDistance)
				{
					nearestDistance = distance;
					chosenCluster = centroidIndex;
				}
			}// end for each centroid
			Centroid temp = centroidList.get(chosenCluster);
			totalDistance += nearestDistance;
			if (temp.numMembers == 0)
			{
				temp.subCollectionNum = createSubCollection();
				if (temp.subCollectionNum == -1)
					System.err.println(
							"Problem creating sub collection");
			}
			putInSubCollectionBatch(thisParticleInfo.getParticleInfo().
					getAtomID(), 
					temp.subCollectionNum);
			temp.numMembers++;
			
		}// end while there are particles remaining
		putInSubCollectionBatchExecute();
		curs.reset();
		totalDistancePerPass.add(new Double(totalDistance));
		printDescriptionToDB(particleCount, centroidList);
		return newHostID;
	}
	protected int assignAtomsToNearestCentroid(
			ArrayList<Centroid> centroidList,
			CollectionCursor curs,
			float vigilance)
	{
		Centroid outliers = new Centroid(null, 0);
		int particleCount = 0;
		ParticleInfo thisParticleInfo = null;
		BinnedPeakList thisBinnedPeakList = null;
		double nearestDistance = 3.0;
		double totalDistance = 0.0;
		double distance = 3.0;
		int chosenCluster = -1;
		putInSubCollectionBatchInit();		
		//for (Centroid c : centroidList)
		//{
		//	c.peaks = normalize(c.peaks);
		//}
		while(curs.next())
		{ // while there are particles remaining
			particleCount++;
			thisParticleInfo = curs.getCurrent();
			thisBinnedPeakList = thisParticleInfo.getBinnedList();
			thisBinnedPeakList = normalize(thisBinnedPeakList);
			// no centroid will be found further than the 
			// vigilance since that centroid would not be 
			// considered
			nearestDistance = 3.0;
			for (int centroidIndex = 0; 
			centroidIndex < centroidList.size(); 
			centroidIndex++)
			{// for each centroid
				distance = centroidList.get(centroidIndex).peaks.
					getDistance(thisBinnedPeakList, distanceMetric);
				if (distance < nearestDistance)
				{
					nearestDistance = distance;
					chosenCluster = centroidIndex;
				}
			}// end for each centroid
			if (nearestDistance > vigilance)
			{
				if (outliers.numMembers == 0)
					outliers.subCollectionNum = 
						createSubCollection("Outliers", "Outliers");
				putInSubCollectionBatch(
						thisParticleInfo.getParticleInfo().
						getAtomID(),
						outliers.subCollectionNum);
				outliers.numMembers++;
				System.out.println("Outlier #" + 
						outliers.numMembers);
			}
			else
			{
				Centroid temp = centroidList.get(chosenCluster);
				totalDistance += nearestDistance;
				
				if (temp.numMembers == 0)
				{
					temp.subCollectionNum = createSubCollection();
				}
				putInSubCollectionBatch(
						thisParticleInfo.getParticleInfo().
						getAtomID(), 
						temp.subCollectionNum);
				
				temp.numMembers++;
			}
			
		}// end while there are particles remaining
		putInSubCollectionBatchExecute();
		curs.reset();
		totalDistancePerPass.add(new Double(totalDistance));
		//curs.close();
		//for (Centroid c : centroidList)
		//{
		//	c.peaks = normalize(c.peaks);
		//}
		printDescriptionToDB(particleCount, centroidList);

		return newHostID;
	}
	
	private void printDescriptionToDB(
			int particleCount,
			ArrayList<Centroid> centroidList)
	{
	
		// sort centroidList.
		ArrayList<Centroid> orderedList = new ArrayList<Centroid>();
		int numCentroids = centroidList.size();
		while (orderedList.size() != numCentroids) {
			int smallestIndex = 0;
			for (int i = 1; i < centroidList.size(); i++) 
				if (centroidList.get(i).subCollectionNum < 
						centroidList.get(smallestIndex).subCollectionNum) 
					smallestIndex = i;
			orderedList.add(centroidList.get(smallestIndex));
			centroidList.remove(smallestIndex);
		}
		centroidList = orderedList;
		
		PrintWriter out = null;
		StringWriter sendToDB = new StringWriter();
		out = new PrintWriter(sendToDB
				/*new FileWriter(collectionID + ","
				 + parameterString +
				 ".txt")*/);
		
		out.println("Clustering Parameters: ");
		out.println(parameterString + "\n\n");
		
		out.println("Number of ignored particles with zero peaks = " + 
		        zeroPeakListParticleCount);
		if (distanceMetric == DistanceMetric.DOT_PRODUCT)
		    out.println("Distance shown here is actually 1 - dot product.");
		out.println("Total clustering passes during sampling = " + sampleIters);
		out.println("Total number of centroid clustering passes = " +
		        clusterCentroidIters);
		out.println("Total number of passes = " + totalDistancePerPass.size());
		out.println("Average distance of all points from their centers " +
		"at each iteration:");
		
		for (int distanceIndex = 0; distanceIndex < totalDistancePerPass.size(); 
		distanceIndex++)
		{
			out.println(
					totalDistancePerPass.get(
							distanceIndex).doubleValue()/particleCount);
		}
		
		out.println("average distance of all points from their centers " +
		"on final assignment:");
		out.println(totalDistancePerPass.get(
					totalDistancePerPass.size()-1).doubleValue()/particleCount);
		
		out.println();
		out.println("Peaks in centroids:");
		for (int centroidIndex = 0; centroidIndex < centroidList.size();
			centroidIndex++)
		{
			out.println("Centroid " + 
					centroidList.get(centroidIndex).subCollectionNum +
					": Number of particles = " +
					centroidList.get(centroidIndex).numMembers);
		}
		out.println();
		for (int centroidIndex = 0; 
		centroidIndex < centroidList.size();
		centroidIndex++)
		{
			out.println("Centroid " + 
					centroidList.get(centroidIndex).subCollectionNum +
			":");
			out.println("Number of particles in cluster: " + 
					centroidList.get(centroidIndex).numMembers);
			writeBinnedPeakListToFile(
					centroidList.get(centroidIndex).peaks,out);
			
		}
		//out.close();
		System.out.println(sendToDB.toString());
		db.setCollectionDescription(newHostID,sendToDB.toString());
	}
	// For testing
	/*public static void main (String[] args)
	{
		BinnedPeakList list1 = new BinnedPeakList();
		BinnedPeakList list2 = new BinnedPeakList();
		list1.add(30,1);
		list2.add(10,0);
		list2.add(30,0);
		list1.add(10,0);
		//list2.add(15, 23);
		//BinnedPeak temp = list1.getNextLocationAndHeight();
		//System.out.println(temp.location);
		//System.out.println(temp.height + "\n");
		//list1.resetPosition();
		int dMetric = CITY_BLOCK;
		list1 = normalize(list1, dMetric);
		temp = list1.getNextLocationAndHeight();
		System.out.println(temp.location);
		System.out.println(temp.height);
		list1.resetPosition();
		//list2 = normalize(list2, dMetric);
		System.out.println(getDistance(list1,list2,dMetric));
	}*/
}
