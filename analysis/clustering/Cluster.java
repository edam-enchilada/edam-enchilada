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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataImporters.EnchiladaDataSetImporter;
import database.CollectionCursor;
import database.DynamicTable;
import database.InfoWarehouse;
import ATOFMS.ParticleInfo;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.DistanceMetric;
import analysis.DummyNormalizer;
import analysis.Normalizer;

/**
 * @author andersbe
 * This abstract class implements methods specific to Cluster 
 * algorithms.
 */
public abstract class Cluster extends CollectionDivider {
	protected ArrayList<Double> totalDistancePerPass;
	protected int numPasses,collectionID;
	protected String parameterString;
	protected ClusterInformation clusterInfo;
	
	protected DistanceMetric distanceMetric = DistanceMetric.CITY_BLOCK;
	
	protected int zeroPeakListParticleCount = 0;
	protected int clusterCentroidIters = 0;
	protected int sampleIters = 0;
	
	protected boolean isNormalized;
	
	/**
	 * Builds the cluster name a bit, then sends information off
	 * to the CollectionDivider constructor
	 * @param cID		The id of the collection to cluster
	 * @param database	An active InfoWarehouse
	 * @param name		A name to append to ",CLUST"
	 * @param comment	A comment for the cluster.
	 */
	public Cluster(int cID, InfoWarehouse database, String name, 
			String comment, boolean norm) {
		super(cID,database,name.concat(",CLUST"),comment);
		isNormalized = norm;
	}
	
	/**
	 * Sets the cluster information for this cluster.
	 * @param cl
	 */
	public void addInfo(ClusterInformation cl){
		clusterInfo = cl;
	}
	
	/**
	 * 
	 * 
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
	
	
	/**
	 * Prints out each peak in a peaklist.  Used for reporting results.
	 * @param inputList		The list to print out
	 * @param out			A printwriter to print to
	 */
	protected void writeBinnedPeakListToFile(
			BinnedPeakList inputList,
			PrintWriter out)
	{
		out.println("Key:\tValue:");
		Iterator<BinnedPeak> iter = inputList.iterator();
		BinnedPeak tempPeak;
		while (iter.hasNext())
		{
			tempPeak = iter.next();
			out.println(tempPeak.key + "\t" + tempPeak.value);
		}
	}
		
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
		ArrayList<Integer> checkedLocations = new ArrayList<Integer>();
		double distance = 0;
		BinnedPeakList longer, shorter;
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
		Iterator<BinnedPeak> longIter = longer.iterator();
		while (longIter.hasNext())
		{
			temp = longIter.next();
			checkedLocations.add(new Integer(temp.key));
			if (distanceMetric == DistanceMetric.CITY_BLOCK)
				distance += Math.abs(temp.value - 
						shorter.getAreaAt(temp.key));
			else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
			{
				shorterTemp = shorter.getAreaAt(temp.key);
				distance += (temp.value - shorterTemp)
				* (temp.value - shorterTemp);
			}
			else
				distance = -1.0f;
		}
		boolean alreadyChecked = false;
		Iterator<BinnedPeak> shortIter = shorter.iterator();
		while (shortIter.hasNext())
		{
			alreadyChecked = false;
			temp = shortIter.next();
			double longerTemp;
			for (Integer loc : checkedLocations)
				if (temp.key == loc.intValue())
					alreadyChecked = true;
			if (!(alreadyChecked))
			{
				if (distanceMetric == DistanceMetric.CITY_BLOCK)
					distance += Math.abs(temp.value - 
							longer.getAreaAt(temp.key));

				else if (distanceMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				{
					longerTemp = longer.getAreaAt(temp.key);
					distance += (temp.value - longerTemp) *
					(temp.value - longerTemp);
				}
				else
					distance = -1.0f;
			}
		}
		
		if (distance > 2) {
			System.out.println("Rounding off " + distance +
					"to 2.0");
			distance = 2.0f;
		}
		assert (distance >= 0) : "distance between two peaklists is -1";
		return distance;
	}

	/**
	 * prints the distance to the nearest centroid.
	 * @param centroidList
	 * @param curs
	 */
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
			thisBinnedPeakList.normalize(distanceMetric);
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
	
	/**
	 * This method assigns atoms to nearest centroid using only centroidList
	 * and collection cursor. ClusterK only. This also averages the final
	 * centroids with k-medians, so it is easier to compare with k-means.
	 * @param centroidList
	 * @param curs
	 * @return
	 */
	protected int assignAtomsToNearestCentroid(
			ArrayList<Centroid> centroidList,
			CollectionCursor curs)
	{
		
		ArrayList<BinnedPeakList> sums = new ArrayList<BinnedPeakList>();
		if (isNormalized)
			for (int i = 0; i < centroidList.size(); i++)
				sums.add(new BinnedPeakList(new Normalizer()));
		else
			for (int i = 0; i < centroidList.size(); i++)
				sums.add(new BinnedPeakList(new DummyNormalizer()));
		
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
			thisBinnedPeakList.normalize(distanceMetric);
			nearestDistance = Float.MAX_VALUE;
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

			sums.get(chosenCluster).addAnotherParticle(thisBinnedPeakList);
			
			Centroid temp = centroidList.get(chosenCluster);
			totalDistance += nearestDistance;
			if (temp.numMembers == 0)
			{
				temp.subCollectionNum = createSubCollection();
				if (temp.subCollectionNum == -1)
					System.err.println(
							"Problem creating sub collection");
			}
			putInSubCollectionBatch(thisParticleInfo.getID(),
					temp.subCollectionNum);
			temp.numMembers++;
			
		}// end with no particle remaining
		putInSubCollectionBatchExecute();
		ArrayList<Integer> ids = this.subCollectionIDs;
		curs.reset();
		totalDistancePerPass.add(new Double(totalDistance));
		for (int i = 0; i < sums.size(); i++) {
			sums.get(i).divideAreasBy(centroidList.get(i).numMembers);
			centroidList.get(i).peaks = sums.get(i);
		}

		createCenterAtoms(centroidList, ids);
		
		printDescriptionToDB(particleCount, centroidList);
		
		return newHostID;
	}
	
	/**
	 * If Art2a is the clustering tool, we need to include vigilance as another
	 * parameter for assign atoms to nearest centroid.
	 * 
	 * @param centroidList
	 * @param curs
	 * @param vigilance
	 * @return
	 */
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
		while(curs.next())
		{ // while there are particles remaining
			particleCount++;
			thisParticleInfo = curs.getCurrent();
			thisBinnedPeakList = thisParticleInfo.getBinnedList();
			thisBinnedPeakList.normalize(distanceMetric);
			// no centroid will be found further than the 
			// vigilance since that centroid would not be 
			// considered
			nearestDistance = 3.0;
			for (int centroidIndex = 0; centroidIndex < centroidList.size(); 
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
				putInSubCollectionBatch(thisParticleInfo.getID(),
						outliers.subCollectionNum);
				outliers.numMembers++;
				System.out.println("Outlier #" + outliers.numMembers);
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
						thisParticleInfo.getID(), 
						temp.subCollectionNum);
				
				temp.numMembers++;
			}
			
		}// end while there are particles remaining
		putInSubCollectionBatchExecute();
		curs.reset();
		totalDistancePerPass.add(new Double(totalDistance));
		printDescriptionToDB(particleCount, centroidList);

		return newHostID;
	}
	
	/**
	 * Creates the "center atoms" from a list of centroids, and puts them into
	 * a new root-level collection.
	 * 	
	 * @param centerList	The list of centroids.
	 * @param ids			The list of subcollection IDs for the cluster 
	 * 						collections to which the centroids belong.
	 */
	public void createCenterAtoms(ArrayList<Centroid> centerList,
			ArrayList<Integer> ids){
		
		//new root-level collection
		int centerCollID = createCenterCollection(parameterString, "");
		int centerAtomID;

		CollectionCursor densecurs;
		ParticleInfo info;
		ArrayList<String> denseValues;
		ArrayList<String> denseNames = new ArrayList<String>();
		ArrayList<Float> totValues;
		Float temp = new Float(0);
		boolean first = true;
		String dense = "";
		ArrayList<ArrayList<String>> denseInfo;
		String datatype;
		String sqlType;
		String colName;
		ArrayList<String> avgValues;
		ArrayList<String> intCols = new ArrayList<String>();
		ArrayList<String> charCols = new ArrayList<String>();
		ArrayList<String> bitCols = new ArrayList<String>();
		ArrayList<String> numeric = new ArrayList<String>();
		Pattern intP = Pattern.compile(".*INT");
		Pattern charP = Pattern.compile(".*CHAR.*");
		Pattern dTime = Pattern.compile("DATETIME");
		Pattern bitP = Pattern.compile("BIT");
		Matcher m;
		int q = 0;
		
		for (Centroid center: centerList){
			
			totValues = new ArrayList<Float>();
			first = true;
			avgValues = new ArrayList<String>();
			
			//System.out.println("Attempting to make an atom of " + center.subCollectionNum);//Debugging
			datatype = db.getCollectionDatatype(centerCollID);
			
			denseInfo = db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoDense);
			
			//TODO: there's got to be a better way to do this - LES
			//identify column names of types that not are appropriate to average
			for (ArrayList<String> nameAndType : denseInfo){
				colName = nameAndType.remove(0);
				sqlType = nameAndType.remove(0);
				//System.out.println("col Name " + colName + " sql type " + sqlType); //Debugging
				m = intP.matcher(sqlType);
				if (m.matches())
					intCols.add(colName);
				else{
					m = charP.matcher(sqlType);
					if (m.matches())
						charCols.add(colName);
					else{
						m = dTime.matcher(sqlType);
						if (m.matches())
							//charCols.add(colName);
							bitCols.add(colName); //hack to get the cluster number to show up
						else{
							m = bitP.matcher(sqlType);
							if (m.matches())
								bitCols.add(colName);
							else
								numeric.add(colName);
						}
					}
				}
			}
			
			//get the dense info for each particle in the cluster.
			//iterate through the atoms in the cluster
			//adding their averageable and int columns together
			densecurs = db.getAtomInfoOnlyCursor(db.getCollection(ids.get(center.subCollectionNum-1)));
			
			//NOTE: this is currently only set up to deal with ATOFMS particles
			while (densecurs.next()){
				info = densecurs.getCurrent();
				denseNames = info.getATOFMSParticleInfo().getFieldNames();
				denseValues = info.getATOFMSParticleInfo().getFieldValues();

				if (first){
					for (int i=1; i<=denseNames.size(); i++)
						totValues.add(new Float(0.0));
						first = false;
				}
				for (int i=1; i<denseNames.size(); i++){
					if (numeric.contains(denseNames.get(i)) || 
							intCols.contains(denseNames.get(i))){
						temp = Float.parseFloat(denseValues.get(i));
						totValues.set(i, totValues.get(i) + temp);
					}else{
						totValues.set(i, new Float(-99));
					}
						
				}
			}
			densecurs.close();
			
			//average the values			
			for (int i=1; i<totValues.size(); i++){
				
				if (numeric.contains(denseNames.get(i)) || intCols.contains(denseNames.get(i))){
					temp = totValues.get(i) / center.numMembers;
					if (intCols.contains(denseNames.get(i))){
						int j = temp.intValue();
						avgValues.add(Integer.toString(j));
					} else
						avgValues.add(Float.toString(temp));
				} else if(charCols.contains(denseNames.get(i))){
					avgValues.add("Center for cluster " + center.subCollectionNum);
				} else
					avgValues.add("");

				dense = EnchiladaDataSetImporter.intersperse(avgValues.get(i-1),
						dense);
			
			
			}
			//System.out.println("DENSE " + dense);	//Debugging

			ArrayList<String> sparse = new ArrayList<String>();
			//put the sparse info into appropriate format
			Iterator<BinnedPeak> i = center.peaks.iterator();
			BinnedPeak p;
			//don't forget the rest of the info.  DUH.  Like PeakArea, Height, Rel.Area
			//get the names of fields and their field types
			ArrayList<ArrayList<String>> sparseNamesTypes = 
				db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoSparse);
			String s = "";
			while (i.hasNext()) {
				p = i.next();
				//TODO: waaaaaaaaay too type-specific
				//if (sparseNamesTypes.get(1).equals("INT"))
					s = EnchiladaDataSetImporter.intersperse(
							Integer.toString(new Float(p.value).intValue()), Integer.toString(p.key));
				//else
				//	s = EnchiladaDataSetImporter.intersperse(
				//		Float.toString(p.value), Integer.toString(p.key));
			
				for (int j=0; j<2; j++)
					s = EnchiladaDataSetImporter.intersperse("1", s);
				sparse.add(s);
			}
			
			/*debugging*/
		//	for (String sp : sparse)
		//		System.out.print(" SPARSE " + sp);	
			
			centerAtomID = db.insertParticle(dense, sparse, db.getCollection(centerCollID),
					db.getNextID());
			
			//associate the new center atoms with the collections they represent
			db.addCenterAtom(centerAtomID, ids.get(center.subCollectionNum - 1));

			dense = "";
			q++;
		}
	}
	
	/**
	 * prints the relevant info to the database
	 * @param particleCount
	 * @param centroidList
	 */
	private void printDescriptionToDB(int particleCount,
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
		out = new PrintWriter(sendToDB);
		
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
			
			StringWriter centroidPeaks = new StringWriter();
			PrintWriter pr = new PrintWriter(centroidPeaks);
			writeBinnedPeakListToFile(
					centroidList.get(centroidIndex).peaks,pr);
			out.print(centroidPeaks.toString());
			
			db.setCollectionDescription(db.getCollection(
					subCollectionIDs.get(centroidList.get(centroidIndex).subCollectionNum-1)),
				centroidPeaks.toString());
		}
		//out.close();
		System.out.println(sendToDB.toString());
		db.setCollectionDescription(db.getCollection(newHostID),sendToDB.toString());
	}
}
