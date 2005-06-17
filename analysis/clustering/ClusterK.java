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
 * The Original Code is EDAM Enchilada's ClusterK class.
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

import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.DistanceMetric;
import analysis.MedianFinder;
import analysis.ParticleInfo;
import analysis.SubSampleCursor;
import database.CollectionCursor;
import database.InfoWarehouse;
import database.NonZeroCursor;
import externalswing.SwingWorker;
//import database.SQLServerDatabase;

/**
 * An intermediate class to implement if you are using an 
 * algorithm which produces a user specified number of clusters.  
 * Appends this number to the name of the spectrum and sets a 
 * variable(k) to this value.
 * 
 * 
 */
public abstract class ClusterK extends Cluster {
	
	/* Declared Class Variables */
	private boolean refineCentroids; // true to refine centroids, false otherwise.
	protected int k; // number of centroids desired.
	private int numParticles; // number of particles in the collection.
	private Random random;

	private static float error = 0.01f;
	private static int numSamples = 50;
	protected NonZeroCursor curs;
	private int returnThis;
	private JFrame parentContainer;
	private int curInt;
	private double difference;
	
	private JDialog errorUpdate;
	private JLabel errorLabel;
	private JFrame container;
	

	
	/**
	 * Constructor; calls the constructor of the Cluster class.
	 * @param cID - collection ID
	 * @param database - database interface
	 * @param k - number of centroids desired
	 * @param name - collection name
	 * @param comment - comment to insert
	 * @param refineCentroids - true to refine centroids, false otherwise.
	 * 
	 */
	public ClusterK(int cID, InfoWarehouse database, int k, 
			String name, String comment, boolean refineCentroids) 
	{
		super(cID, database,name.concat(",K=" + k),comment);
		this.k = k;
		this.refineCentroids = refineCentroids;
		collectionID = cID;
		parameterString = name.concat(",K=" + k);
		totalDistancePerPass = new ArrayList<Double>();
		random = new Random(43291);
		
	}
	
	/**
	 * Divide refines the centroids if needed and calls the clustering method.
	 * In the end, it finalizes the clusters by calling a method to report 
	 * the centroids.
	 * TODO:  The max number of subsamples clustered when we reifine centroids is 
	 * 50.  We need a way to either validate this or a way to change it from the
	 * application.  
	 * 
	 * (non-Javadoc)
	 * @see analysis.CollectionDivider#divide()
	 */
	public int divide() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				ArrayList<Centroid> centroidList = 
					new ArrayList<Centroid>();
				numParticles = db.getCollectionSize(collectionID);
				// If refineCentroids is true, randomize the db and cluster subsamples.
				if (refineCentroids) {
					int sampleSize;
					if (numSamples*4 > numParticles) 
						sampleSize = numParticles/(numSamples*2);
					else 
						sampleSize = numParticles/numSamples - 1;
					db.seedRandom(90125);
					CollectionCursor randCurs = 
						db.getRandomizedCursor(collectionID);
					NonZeroCursor partCurs = null;
					System.out.println("clustering subSamples:");
					System.out.println("number of samples: " + numSamples);
					System.out.println("sample size: " + sampleSize);
					ArrayList<ArrayList<Centroid>> allCentroidLists =
					    new ArrayList<ArrayList<Centroid>>(numSamples);
					sampleIters = 0;
					for(int i = 0; i < numSamples; i++)
					{
						curInt = i+1;
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									updateErrorDialog("Clustering subsample #" + (curInt));
								}
							});
						} catch (Exception e) {	e.printStackTrace(); }
						partCurs = new NonZeroCursor(new SubSampleCursor(
								randCurs, 
								i*sampleSize, 
								sampleSize));
						allCentroidLists.add(processPart(new ArrayList<Centroid>(),
						        				partCurs));
						centroidList.addAll(allCentroidLists.get(i));
						sampleIters += totalDistancePerPass.size();
					}
					
					// Of the various centroids that are found, try clustering all
					// the centroids together using each set of centroids as a starting
					// point. For each of the centroids that result, choose those
					// that result in the least error.
					System.out.println("clustering Centroids:");
					double bestDistance = Double.POSITIVE_INFINITY;
					ArrayList<Centroid> bestStartingCentroids = null;
					int bestIndex = -1;
					clusterCentroidIters = 0;
					for (int i=0; i < numSamples; i++) {
						curInt = i+1;
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									updateErrorDialog("Clustering centroid #" + (curInt));
								}
							});
						} catch (Exception e) {	e.printStackTrace(); }
						ArrayList<Centroid> centroids = 
							processPart(allCentroidLists.get(i),
									new NonZeroCursor(
											new CentroidListCursor(centroidList)));
						double distance = (totalDistancePerPass.
					              get(totalDistancePerPass.size()-1).doubleValue());
						clusterCentroidIters += totalDistancePerPass.size();
					    if (distance < bestDistance) {
					        bestDistance = distance;
					        bestStartingCentroids = centroids;
					        bestIndex = i;
					    }
					}
					System.out.println("Centroid clustering iterations: " +
					        clusterCentroidIters);
					centroidList = processPart(bestStartingCentroids,
							new NonZeroCursor(new CentroidListCursor(centroidList)));
					partCurs.close();
					randCurs.close();
				} 
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							updateErrorDialog("Clustering particles...");
						}
					});
				} catch (Exception e) {	e.printStackTrace(); }
				centroidList = processPart(centroidList, curs);
				
				System.out.println("returning");
				
				returnThis = 
					assignAtomsToNearestCentroid(centroidList, curs);
				curs.close();
				
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							errorUpdate.setVisible(false);
							errorUpdate = null;
						}
					});
				} catch (Exception e) {	e.printStackTrace(); }
				
				return returnThis;
			}
		};
		worker.start();
		
		errorUpdate = new JDialog((JFrame)container,"Clustering",true);
		errorLabel = new JLabel("Clusters stabilize when change in error = 0");
		errorLabel.setSize(100,250);
		errorUpdate.add(errorLabel);
		errorUpdate.pack();
		errorUpdate.validate();
		errorUpdate.setVisible(true);
		
		return returnThis;
	}
	
	public void updateErrorDialog(String str) {
		if (errorUpdate != null) {
		errorLabel.setText(str);
		errorUpdate.validate();
		}
	}
	
	/**
	 * Sets the cursor type; clustering can be done using either by 
	 * disk or by memory.
	 * 
	 * (non-Javadoc)
	 * @see analysis.CollectionDivider#setCursorType(int)
	 */
	public boolean setCursorType(int type) 
	{
		switch (type) {
		case CollectionDivider.DISK_BASED :
			curs = new NonZeroCursor(db.getBinnedCursor(collectionID));
		return true;
		case CollectionDivider.STORE_ON_FIRST_PASS : 
		    curs = new NonZeroCursor(db.getMemoryBinnedCursor(collectionID));
		return true;
		default :
			return false;
		}
	}
	
	/**
	 * Sets the distance metric.  If using K-Means, the distance metric will always
	 * be Euclidean Squared, since it is guaranteed to decrease.  If using K-Medians, the
	 * distance metric will always be City Block, since it is guaranteed to decrease.
	 * 
	 * (non-Javadoc)
	 * @see analysis.clustering.Cluster#setDistancMetric(int)
	 */
	public boolean setDistanceMetric(DistanceMetric method) {
		distanceMetric = method;
		if (method == DistanceMetric.CITY_BLOCK)
			return true;
		else if (method == DistanceMetric.EUCLIDEAN_SQUARED)
			return true;
		else if (method == DistanceMetric.DOT_PRODUCT)
			return true;
		else
		{
			throw new IllegalArgumentException("Illegal distance metric.");
		}
	}

	private static class OutlierData implements Comparable<OutlierData> {
		private BinnedPeakList peakList;
		private double distance;
		public OutlierData(BinnedPeakList pl, double d) {
			peakList = pl;
			distance = d;
		}
		public int compareTo(OutlierData o) {
			if (distance > o.distance)
				return 1;
			else if (distance < o.distance)
				return -1;
			else
				return 0;
		}
	}

	/**
	 * ProcessPart is the method that does the actual clustering.  For K-Means and
	 * K-Medians, this is the exact same method.
	 * 
	 * @param centroidList - list of centroids - enter a null list on first pass.
	 * @param curs - cursor to loop through the particles in the db.
	 * @return the new list of centroids.
	 */
	private ArrayList<Centroid> processPart(
			ArrayList<Centroid> centroidList,
			NonZeroCursor curs) {
		
		boolean isStable = false;
		
		// Create an arrayList of particle peaklists for each centroid. 
		int arrayStartingSize = numParticles/k;
		ArrayList<ArrayList<Integer>> particlesInCentroids = 
			new ArrayList<ArrayList<Integer>>(k);
		for (int i = 0; i < k; i++) 
			particlesInCentroids.add(new ArrayList<Integer>(arrayStartingSize));
		
		
		// If there are the same number of particles as centroids, 
		// assign each particle to a centroid and you're done.
		if (numParticles == k) {
			centroidList.clear();
			for (int j = 0; j < k; j++) {
				curs.next();
				centroidList.add(
						new Centroid(normalize(
								curs.getCurrent().getBinnedList()),
								1));
			}
			return centroidList;
		}
		
		// If there are fewer centroids than particles, display an error. 
		if (k > numParticles) {
			System.err.println("Not enough particles to cluster " +
					"with this many centroids. (You requested more" +
			"centroids than there are particles");
			return centroidList;
		}

		// If the centroid list contains some centroids, but not the right
		// amount, display an error.
		if (centroidList.size() > 0 && centroidList.size() != k) {
			System.err.println("There are some initial centroids, but" +
					"not the right amount of them.");
			return centroidList;
		}
		
		// If the centroidList has no centroids in it, choose k random
		// centroids. Only choose random peaks where at least one particle
		// has a peak at that location.
		if (centroidList.size() == 0) {
		    
		    // Take the first point as the first centroid. For each succeeding
		    // point, take the one that is furthest away from the closest
		    // of the points chosen so far.
		    curs.reset();
		    boolean status = curs.next();
		    assert status : "Cursor is empty.";
		    Centroid newCent = 
		    	new Centroid(normalize(curs.getCurrent().getBinnedList()),0);
		    // TODO:  this should be sumtoone?? for K-Medians!!!
		    centroidList.add(newCent);
		    for (int i=1; i < k; i++) {
		        curs.reset();
				BinnedPeakList furthestPeakList = null;
				double furthestGlobalDistance = Double.MIN_VALUE;
		        while (curs.next()) {
					ParticleInfo thisParticleInfo = curs.getCurrent();
					BinnedPeakList thisBinnedPeakList =
						curs.getPeakListfromAtomID(thisParticleInfo.getID());
					thisBinnedPeakList = normalize(thisBinnedPeakList);
					double nearestDistance = Double.MAX_VALUE;
					for (int curCent = 0; 
					     curCent < centroidList.size(); 
					     curCent++)
						
					{// for each centroid
						double distance =
							centroidList.get(curCent).peaks.
								getDistance(thisBinnedPeakList, distanceMetric);
						//If nearestDistance hasn't been set or is larger 
						//than found distance, set the nearestCentroid index.
						if (distance < nearestDistance)
							nearestDistance = distance;
					}// end for each centroid
					if (nearestDistance > furthestGlobalDistance) {
					    furthestGlobalDistance = nearestDistance;
					    furthestPeakList = thisBinnedPeakList;
					}
		        } // while curs.next()
		        newCent = new Centroid(furthestPeakList,0);
		        centroidList.add(newCent);
		    } // for i:1 to k    
		} // if centroid list size is 0
		
		// Since TreeSet does not allow dups, insert distinct but
		// small initial values.
		TreeSet<OutlierData> outliers = new TreeSet<OutlierData>();
		
		//clear totalDistancePerPass array.
		totalDistancePerPass.clear(); 
		double accumDistance = 0.0;
		curs.reset();
		while (!isStable) {
			for (ArrayList<Integer> array : particlesInCentroids){
				array.clear();
			}

			outliers.clear();
			for (int i=0; i < k; i++)
				outliers.add(new OutlierData(null,i*Double.MIN_VALUE));
			double smallestOutlierDistance = Double.MIN_VALUE;
			while(curs.next())
			{ // while there are particles remaining
				ParticleInfo thisParticleInfo = curs.getCurrent();
				BinnedPeakList thisBinnedPeakList =
					curs.getPeakListfromAtomID(thisParticleInfo.getID());
				thisBinnedPeakList = normalize(thisBinnedPeakList);
				double nearestDistance = Double.MAX_VALUE;
				int nearestCentroid = -1;
				for (int curCent = 0; curCent < k; curCent++)
				{// for each centroid
					double distance = centroidList.get(curCent).peaks.getDistance(thisBinnedPeakList,distanceMetric);
					//If nearestDistance hasn't been set or is larger 
					//than found distance, set the nearestCentroid index.
					if (distance < nearestDistance){
						nearestCentroid = curCent;
						nearestDistance = distance;
					}
				}// end for each centroid
				
				// TreeSets do not allow duplicates. Therefore, we make small
				// distinctions in the distances for the outliers that we add (if necessary).
				// Making the distance smaller here (not bigger) is crucial. This ensures that
				// if a whole series of atoms have the same distance, they do not keep bouncing
				// each other out.
				if (nearestDistance > smallestOutlierDistance) {
					OutlierData outlier = new OutlierData(thisBinnedPeakList,nearestDistance);
					while (outlier.distance > smallestOutlierDistance && outliers.contains(outlier))
						outlier.distance -= 1e-5;

					//	If distance is still an outlier, add it to the outlier array.
					if (nearestDistance > smallestOutlierDistance) {
						outliers.add(outlier);			
						if (outliers.size() > k)
							outliers.remove(outliers.first());						
						smallestOutlierDistance = outliers.first().distance;
					}
				}					
				
				// Put atomID assigned to curCent in particlesInCentroids array, and increment
				// appropriately.  
				particlesInCentroids.get(nearestCentroid).add(new Integer(
						thisParticleInfo.getID()));
				centroidList.get(nearestCentroid).numMembers++;
				accumDistance += nearestDistance;	
			
			}// end while there are particles remaining
			zeroPeakListParticleCount = curs.getZeroCount();
			totalDistancePerPass.add(new Double(accumDistance));

			// reset centroid list.  The averageCluster method is overwritten
			// in K-Means and K-Medians.
			for (int i = 0; i < k; i++) {
				Centroid newCent = averageCluster(centroidList.get(i),
						particlesInCentroids.get(i), curs);
				centroidList.set(i, newCent);
			}
			//accumDistance:
			accumDistance = 0.0;
			// cursor:
			curs.reset();

			if (outliers.last().distance < 1E-4) {
				System.out.println("Particles are perfectly clustered!");
				return centroidList;
			}

			// If there is one (or more) empty centroids, replace them 
			ArrayList<Integer> emptyCentIndex = new ArrayList<Integer>();
			isStable = stableCentroids(totalDistancePerPass);
			for (int i = 0; i < k; i++) {
				if (particlesInCentroids.get(i).size() == 0) {
					OutlierData outlier = outliers.last();
					centroidList.set(i,new Centroid(outlier.peakList,0));
					outliers.remove(outlier);
					isStable = false;
				}
			}
		} // end while loop
			
		// Remove the last pass in the total distance array,
		// since these are duplicates.
		//totalDistancePerPass.remove(totalDistancePerPass.size()-1);
		//totalDistancePerPass.remove(totalDistancePerPass.size()-1);
		System.out.println("Zero count = " + curs.getZeroCount());
		return centroidList;
	}
		
	/**
	 * Determines whether the centroids are stable or not.  It does this by
	 * determining how much the centroids moved on the last pass and comparing
	 * it to a pre-determined error.
	 * TODO:  Error here is arbitrary. We need to either determine a best 
	 * error or be able to change the error from the application.	  
	 * 
	 * @param totDist - total distance array
	 * @param error - pre-determined error
	 * @return - true if stable, false otherwise.
	 */
	public boolean stableCentroids(ArrayList<Double> totDist) {
		if (totDist.size() == 1)
			return false;
		int lastIndex = totDist.size() - 1;
		difference = 
			totDist.get(lastIndex-1).doubleValue() - 
			totDist.get(lastIndex).doubleValue();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					updateErrorDialog("Change in error = " + (difference));
				}
			});
		} catch (Exception e) {	e.printStackTrace(); }
		//difference = Math.abs(difference);
		System.out.println("Error: " + totDist.get(lastIndex).doubleValue());
		System.out.println("Change in error: " + difference);
		System.out.flush();
		assert (difference >= -0.00001f) : "increased error!";
		if (difference > error) 
			return false;
		return true;
	}
	
	/**
	 * The following four get and set methods are used in the Advanced dialog box
	 * for the user to input specifications.
	 */
	
	public static float getError() {
		return error;
	}
	
	public static int getNumSamples() {
		return numSamples;
	}
	
	public static void setError(float err) {
		error = err;
	}
	
	public static void setNumSamples(int num) {
		numSamples = num;
	}
	
	/**
	 * Abstract method averageCluster that is overwritten in the children classes.
	 * 
	 * @param thisCentroid - centroid to average
	 * @param particlesInCentroid - list of atomIDs for this centroid
	 * @param curs - the cursor; used to get binned peak lists, not for looping 
	 * through particles.
	 * @return - a new centroid.
	 */
	public abstract Centroid averageCluster(
			Centroid thisCentroid,
			ArrayList<Integer> particlesInCentroid,
			CollectionCursor curs);
	
}
