/*
 * Created on Apr 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//package analysis.clustering;

/**
 * @author andersbe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//public class Agglomerative {

//}

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
 * The Original Code is EDAM Enchilada's Art2A class.
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
 * Ben Anderson
 */
package analysis.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import analysis.*;

import database.InfoWarehouse;
import database.NonZeroCursor;
import database.SQLServerDatabase;
import analysis.BinnedPeakList;

/**
 * @author andersbe
 *
 */
public class Agglomerative extends Cluster 
{
	ArrayList<ArrayList<PeaksAndID>> clusters = new ArrayList<ArrayList<PeaksAndID>>();
	PriorityQueue<ClusterAndNearest> distances = new PriorityQueue<ClusterAndNearest>();
	
	private static class ClusterAndNearest 
		implements Comparable<ClusterAndNearest>
	{
		int clusterIndex;
		int nearestIndex;
		float nearestDistance;
		public ClusterAndNearest(int clusterIndex, 
				int nearestIndex, float nearestDistance){
			this.clusterIndex = clusterIndex;
			this.nearestIndex = nearestIndex;
			this.nearestDistance = nearestDistance;
		}
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(analysis.clustering.Agglomerative.ClusterAndNearest)
		 */
		public int compareTo(ClusterAndNearest arg0) {
			// TODO Auto-generated method stub
			if (nearestDistance == arg0.nearestDistance)
				return 0;
			else if (nearestDistance < arg0.nearestDistance)
				return -1;
			else 
				return 1;
		}

	}
	
	private static class PeaksAndID 
	{
		BinnedPeakList peaks;
		int atomID;
		PeaksAndID(BinnedPeakList peaks, int atomID)
		{ 
			this.atomID = atomID; this.peaks = peaks;
		}
	}
	private int maxK; 
	private int size;
	protected NonZeroCursor curs;
	// stableIterations contains the number of iterations that ART-2a
	// terminates after if there has not been an improvement in totalDistance
	private final int stableIterations = 10;
	
	/**
	 * @param cID
	 * @param database
	 */
	public Agglomerative(int cID, InfoWarehouse database, int k,
			DistanceMetric dMetric, String comment) {
		super(cID, database, "Agglomerative,MaxK=" + k + ",DMetric=" + 
				dMetric, comment);
		parameterString = "Agglomerative,MaxK=" + k + ",DMetric=" + dMetric;
		this.maxK = k;
		distanceMetric = dMetric;
		collectionID = cID;
		totalDistancePerPass = new ArrayList<Float>();
		/*for (int i = 0; i < numPasses; i++)
			totalDistancePerPass.add(new Float(0.0));*/
		size = db.getCollectionSize(collectionID);
	}
	
	
	private ClusterAndNearest findClosestCluster(int toThisCluster)
	{
		float minDist = Float.MAX_VALUE;
		int index = -1;
		float tempDist;
		for (int i = 0; i < clusters.size(); i++)
		{
			if (toThisCluster != i)
			{
				tempDist = getMinDist(clusters.get(i), 
						clusters.get(toThisCluster));
				if (tempDist < minDist)
				{
					minDist = tempDist;
					index = i;
				}
			}
		}
		assert(toThisCluster != index) : "found the same cluster";
		return new ClusterAndNearest(toThisCluster, index, minDist);
	}
	
	/**
	 * A lot wrapped into one loop for efficiency.  First, removes the cluster
	 * that was closest to the original.  Second, it computes the correct new
	 * minimum distances and clusters for the clusters who used to be closest to
	 * the now removed clusters.  Finally, it decrements indices above those 
	 * that were removed.
	 * 
	 * @param nearestIndex
	 * @param clusterIndex
	 * @param newCluster
	 */
	private void removeOldAndComputeNewDistances(int nearestIndex, 
			int clusterIndex, int newCluster)
	{
		// Make sure we didn't say the cluster nearest the other was itself
		assert(nearestIndex != clusterIndex) : 
			"nearest = cluster: " + nearestIndex;
		Iterator<ClusterAndNearest> iter = distances.iterator();
		ClusterAndNearest temp;
		
		// This will hold the modified entries to reinsert into the queue.
		ArrayList<ClusterAndNearest> toAdd = new ArrayList<ClusterAndNearest>();
		
		// Update each element of the queue.
		while (iter.hasNext())
		{
			temp = iter.next();
			
			// Remove the entry for the old cluster
			if (temp.clusterIndex == nearestIndex)
			{
				iter.remove();
			}
			else
			{
				assert(temp.nearestIndex != temp.clusterIndex);

				// keep track of the predecrmented values.
				int cIOrig = temp.clusterIndex;
				int cNOrig = temp.nearestIndex;

				// make sure we actually got rid of the entry for the main 
				// cluster
				assert(cIOrig != nearestIndex);
				assert(cIOrig != clusterIndex) : "cIOrig = clusterIndex: " +
				cIOrig + " cNOrig = " + cNOrig;
				
				// Decrement the values
				if (cIOrig > clusterIndex)
					temp.clusterIndex--;
				
				if (cIOrig > nearestIndex)
					temp.clusterIndex--;
				
				if (cNOrig > clusterIndex)
					temp.nearestIndex--;
				
				if (cNOrig > nearestIndex)
					temp.nearestIndex--;
				
				// Make sure we didn't set them equal unless we'r planning 
				// on resetting them
				assert(temp.nearestIndex != temp.clusterIndex ||
						cNOrig == nearestIndex || cNOrig == clusterIndex);
				
				if (cNOrig == nearestIndex || 
					cNOrig == clusterIndex)
				{
					assert(newCluster != temp.clusterIndex);
					float tempDist = 
						getMinDist(clusters.get(newCluster), 
								clusters.get(temp.clusterIndex));
					
					if (tempDist < temp.nearestDistance)
					{
						
						temp.nearestDistance = tempDist;
						temp.nearestIndex = newCluster;
						assert(temp.nearestIndex != temp.clusterIndex);
						assert(cIOrig != clusterIndex);
						toAdd.add(temp);
						iter.remove();
					}
					else
					{
						toAdd.add(findClosestCluster(temp.clusterIndex));
						iter.remove();
					}
				}
			}
		}
		for (int i = 0; i < toAdd.size(); i++)
		{
			assert(toAdd.get(i).clusterIndex != toAdd.get(i).nearestIndex);
			distances.add(toAdd.get(i));
		}
		ClusterAndNearest newClosest = findClosestCluster(newCluster);
		assert(newClosest.clusterIndex != newClosest.nearestIndex);
		distances.add(newClosest);
	}
	
	private void outputClusters(int collectionID)
	{
		for (int i = 0; i < clusters.size(); i++)
		{
			ArrayList<PeaksAndID> tempCluster = clusters.get(i);
			int thisClusterID = 
				db.createEmptyCollection(
						collectionID,"cluster " + i,"no comment");
			for (int j = 0; j < tempCluster.size(); j++)
			{
				db.addAtom(tempCluster.get(j).atomID, thisClusterID);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#divide()
	 */
	public int divide() 
	{
		ArrayList<PeaksAndID> tempCentroid;
		
		// Load each particle into its own cluster
		while (curs.next())
		{
			tempCentroid = new ArrayList<PeaksAndID>();
			ParticleInfo tempPart = curs.getCurrent();
			tempCentroid.add(new PeaksAndID(normalize(tempPart.getBinnedList()),
					tempPart.getID()));
			clusters.add(tempCentroid);
		}
		
		// Load up the distance priority queue
		for (int i = 0; i < clusters.size(); i++)
		{
			distances.add(findClosestCluster(i));
		}
		// Create the main collection for holding the various levels 
		// of detail
		int hostCollectionID = db.createEmptyCollection(collectionID, 
				parameterString,"Agglomerates");
		
		// Agglomerate until we have only 2 clusters
		while (clusters.size() > 1)
		{
			System.out.println("Starting iteration.  Cluster.size = " + 
					clusters.size());
			assert(!distances.isEmpty()) : "queue is empty!";
			
			// Grab the closeset centroids
			ClusterAndNearest closest = distances.poll();
			System.out.println("Removing cluster " + closest.clusterIndex +
					" with closest: " + closest.nearestIndex);
			System.out.flush();
			assert(closest!=null);
			ArrayList<PeaksAndID> newCluster = new ArrayList<PeaksAndID>();
			assert(closest.clusterIndex != closest.nearestIndex);
			newCluster.addAll(clusters.get(closest.clusterIndex));
			newCluster.addAll(clusters.get(closest.nearestIndex));
			
			clusters.remove(closest.nearestIndex);
			if (closest.nearestIndex < closest.clusterIndex)
			{
				clusters.remove(closest.clusterIndex-1);
			}
			else
				clusters.remove(closest.clusterIndex);
			clusters.add(newCluster);

			removeOldAndComputeNewDistances(closest.nearestIndex, 
					closest.clusterIndex, clusters.size()-1);
			
			if (clusters.size() <= maxK)
			{
				System.out.println("Outputting clusters for " + clusters.size()
						+ ".");
				outputClusters(db.createEmptyCollection(
						hostCollectionID, 
						clusters.size() + 
						" clusters", "a glom"));
			}
		}
		
		return hostCollectionID;
	}
	
	public int cluster()
	{
		return divide();
	}

	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#setCursorType(int)
	 */
	public boolean setCursorType(int type) 
	{
		switch (type) {
		case CollectionDivider.DISK_BASED :
			//TODO: Change this back
			curs = new NonZeroCursor(db.getBinnedCursor(collectionID));
			return true;
		case CollectionDivider.STORE_ON_FIRST_PASS : 
		    curs = new NonZeroCursor(db.getMemoryBinnedCursor(collectionID));
			return true;
		default :
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see analysis.clustering.Cluster#setDistancMetric(int)
	 */
	public boolean setDistanceMetric(DistanceMetric method) 
	{
		distanceMetric = method;
		
		if (method == DistanceMetric.CITY_BLOCK)
		{
			return true;
		}
		else if (method == DistanceMetric.EUCLIDEAN_SQUARED)
		{
			return true;
		}
		else
			return false;
	}
	
	private float getMinDist(ArrayList<PeaksAndID> p1, 
			ArrayList<PeaksAndID> p2)
	{
		float minDist = Float.MAX_VALUE;
		float tempDist = 0;
		for (int i = 0; i < p1.size(); i++)
		{
			for (int j = 0; j < p2.size(); j++)
			{
				tempDist = getDistance(
						p1.get(i).peaks,
						p2.get(j).peaks);  
				if (tempDist < minDist)
				{
					minDist = tempDist;
				}
			}
		}
		return tempDist;
	}	
	
	public static void main(String args[])
	{
		InfoWarehouse db = new SQLServerDatabase();
		db.openConnection();
		
		System.out.println("collection size = " + db.getCollectionSize(1));
		Agglomerative agglomerator = new Agglomerative(1,db, 7,  
				DistanceMetric.EUCLIDEAN_SQUARED, "A comment");
		agglomerator.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
		agglomerator.setCursorType(STORE_ON_FIRST_PASS);
		agglomerator.divide();
		
		db.closeConnection();		
	}
}