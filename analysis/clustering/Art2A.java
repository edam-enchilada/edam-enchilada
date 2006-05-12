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

import ATOFMS.ParticleInfo;
import analysis.*;

import database.InfoWarehouse;
import database.NonZeroCursor;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;

/**
 * @author andersbe
 *
 */
public class Art2A extends Cluster 
{
	private float vigilance;
	private float learningRate;
	private int size;
	protected NonZeroCursor curs;
	private ClusterInformation cInfo;
	// stableIterations contains the number of iterations that ART-2a
	// terminates after if there has not been an improvement in totalDistance
	private final int stableIterations = 10;
	
	/**
	 * @param cID
	 * @param database
	 */
	public Art2A(int cID, InfoWarehouse database, float v, float lr, 
			int passes,  DistanceMetric dMetric, String comment, ClusterInformation c) {
		super(cID, database, "Art2A,V=" + v + ",LR=" + lr +",Passes=" +
				passes + ",DMetric=" + dMetric, comment, c.normalize);
		parameterString = "Art2A,V=" + v + ",LR=" + lr +",Passes=" +
		passes + ",DMetric=" + dMetric;
		vigilance = v;
		learningRate = lr;
		numPasses = passes;
		distanceMetric = dMetric;
		collectionID = cID;
		totalDistancePerPass = new ArrayList<Double>();
		size = db.getCollectionSize(collectionID);	
		cInfo = c;
	}
	
	private BinnedPeakList adjustByLearningRate(
			BinnedPeakList addedParticle,
			BinnedPeakList centroid)
	{
		BinnedPeakList returnList;
		if (isNormalized)
			returnList = new BinnedPeakList(new Normalizer());
		else
			returnList = new BinnedPeakList(new DummyNormalizer());
		
		BinnedPeak addedPeak;
		// keep track of locations that are in both lists so we don't 
		// redo them.
		Iterator<BinnedPeak> iter = addedParticle.iterator();
		ArrayList<Integer> locationsGrabbed = new ArrayList<Integer>();
		float centroidArea;
		while (iter.hasNext())
		{
			addedPeak = iter.next();
			centroidArea = centroid.getAreaAt(addedPeak.key);
			locationsGrabbed.add(new Integer(addedPeak.key));
			
			returnList.addNoChecks(addedPeak.key, 
					centroidArea + 
					(addedPeak.value-centroidArea)*learningRate);
		}
		
		BinnedPeak centroidPeak;
		float addedArea;
		boolean alreadyAdded;
		
		iter = centroid.iterator(); // iterator is now over CENTROID
		while (iter.hasNext())
		{
			centroidPeak = iter.next();
			alreadyAdded = false;
			for (int j = 0; j < locationsGrabbed.size(); j++)
				if (centroidPeak.key == 
					locationsGrabbed.get(j).intValue())
					alreadyAdded = true;
			if (!alreadyAdded)
			{
				addedArea = addedParticle.getAreaAt(
						centroidPeak.key);
				
				returnList.addNoChecks(centroidPeak.key,
						centroidPeak.value +
						(addedArea-centroidPeak.value) *
						learningRate);
			}
		}
		return returnList;
	}
	
	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#divide()
	 */
	public int divide() 
	{
		int returnThis = assignAtomsToNearestCentroid(
				processPart(new ArrayList<Centroid>(), curs), curs, vigilance);
		return returnThis;
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
		// TODO: no memory binned cursor here anymore; have to fix eventually.
		switch (type) {
		case CollectionDivider.DISK_BASED :
			curs = new NonZeroCursor(db.getClusteringCursor(db.getCollection(collectionID), cInfo));
			return true;
		case CollectionDivider.STORE_ON_FIRST_PASS : 
		    curs = new NonZeroCursor(db.getMemoryClusteringCursor(db.getCollection(collectionID), cInfo));
			return true;
		default :
			return false;
		}
	}
	
	private ArrayList<Centroid> processPart(ArrayList<Centroid> centroidList,
			NonZeroCursor curs)
	{
		int particleCount;
		ParticleInfo thisParticleInfo = null;
		BinnedPeakList thisBinnedPeakList = null;
		int closestCentroidIndex = -1;
		double nearestDistance;
		boolean withinVigilance = false;
		double distance;
		int chosenCluster = 0;
		
		double minTotalStableDistance = Double.POSITIVE_INFINITY;
		int iterationsSinceNewMin = 0;
		boolean stable = false;
		
		for (int passIndex = 0; passIndex < numPasses && !stable; passIndex++)
		{ // for each pass
			System.out.println("Pass #:" + passIndex);
			particleCount = 0;
			totalDistancePerPass.add(new Double(0));
			ArrayList<BinnedPeakList> array;
			while(curs.next())
			{ // while there are particles remaining
				particleCount++;
				//System.out.println("particleCount = " + 
				//		particleCount);
				thisParticleInfo = curs.getCurrent();
				thisBinnedPeakList = thisParticleInfo.getBinnedList();
				thisBinnedPeakList.normalize(distanceMetric);
				
						
				// no centroid will be found further than the vigilance
				// since that centroid would not be considered
				nearestDistance = vigilance + 1;
				withinVigilance = false;
				for (int centroidIndex = 0; 
					 centroidIndex < centroidList.size(); 
					 centroidIndex++)
				{// for each centroid
					distance = centroidList.get(centroidIndex).peaks.
						getDistance(thisBinnedPeakList,distanceMetric);
					if (distance <= vigilance)
					{// if cluster is within the vigilance
						if (distance < nearestDistance)
						{
							nearestDistance = distance;
							chosenCluster = centroidIndex;
							withinVigilance = true;
						}
					}// end if each cluster is within the vigilance
				}// end for each centroid
				if (withinVigilance)
				{// if atom falls within existing cluster
					Centroid temp = centroidList.get(chosenCluster);
					totalDistancePerPass.set(passIndex,
							new Double(totalDistancePerPass.get(
										passIndex).doubleValue() 
											+ nearestDistance));

					temp.numMembers++;
					temp.peaks = adjustByLearningRate(thisBinnedPeakList, 
							centroidList.get(chosenCluster).peaks);
					temp.peaks.normalize(distanceMetric);
				}// end if atom falls within existing cluster
				
				else
				{
					System.out.println("Adding new centroid");
					centroidList.add(new Centroid (thisBinnedPeakList,1));
				}
			}// end while there are particles remaining
			System.out.println("about to reset");
			curs.reset();
			
			// remove outliers (an outlier is defined as any cluster
			// containing less than .5% of the total number of particles
			float outlierThreshold = 0.005f;
			int i = 0;
			int tempNumMembers;
			while(i < centroidList.size())
			{ // for each centroid
				Centroid temp = centroidList.get(i);
				tempNumMembers = temp.numMembers;
				temp.numMembers = 0;
				if (tempNumMembers < outlierThreshold * particleCount)
				{
					System.out.println("Removing outlier centroid");
					centroidList.remove(i);
				}
				else
					i++;
			} // end for each centroid
			
			// Update stable distances, and see if can quit early.
		    float distNow = totalDistancePerPass.get(passIndex).floatValue();
		    if (distNow < minTotalStableDistance) {
			    // Still made some progress, keep going.
		        iterationsSinceNewMin = 0;
		        minTotalStableDistance = distNow;
		    } else if (iterationsSinceNewMin < stableIterations)
		        // Made no progress, but might make more with time.
		        iterationsSinceNewMin++;
		    else
			    // Made no progress in many iterations. Stop.
			    stable = true;
	
		} // end for each pass
		//curs.close();
		zeroPeakListParticleCount = curs.getZeroCount();
		return centroidList;
	}
}