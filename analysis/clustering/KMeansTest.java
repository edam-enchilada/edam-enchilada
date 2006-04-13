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
 * The Original Code is EDAM Enchilada's KMeans unit test.
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


package analysis.clustering;

import java.util.ArrayList;

import collection.Collection;

import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.DistanceMetric;
import analysis.Normalizer;


import database.CreateTestDatabase;
import database.SQLServerDatabase;
import junit.framework.TestCase;
/*
 * Created on Dec 16, 2004
 *
 *
 *NOTE:  refined centroids not tested; not enough test particles generated.
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author dmusican
 *
 */
public class KMeansTest extends TestCase {

    private KMeans kmeans;
    private SQLServerDatabase db;
    String dbName = "TestDB";
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
		new CreateTestDatabase();
		db = new SQLServerDatabase("TestDB");
		db.openConnection();
		
        int cID = 2;
        int k = 2;
        String name = "Test clustering";
        String comment = "Test comment";
        boolean refine = false;
        ArrayList<String> list = new ArrayList<String>();
        list.add("ATOFMSAtomInfoSparse.PeakArea");
    	ClusterInformation cInfo = new ClusterInformation(list, "ATOFMSAtomInfoSparse.PeakLocation", null, false, true);
        kmeans = new KMeans(cID,db,k,name,comment,refine, cInfo);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
		db.closeConnection();
		System.runFinalization();
		System.gc();
	    SQLServerDatabase.dropDatabase(dbName);
        
    }

    public void testGetDistance() {
        BinnedPeakList list1 = new BinnedPeakList(new Normalizer());
        BinnedPeakList list2 = new BinnedPeakList(new Normalizer());
        list1.add(1,0.1f);
        list1.add(2,0.2f);
        list2.add(1,0.3f);
        list2.add(3,0.3f);

        kmeans.setDistanceMetric(DistanceMetric.CITY_BLOCK);
        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.CITY_BLOCK)*100)/100. == 0.7);
        kmeans.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.EUCLIDEAN_SQUARED)*100)/100.
                == 0.17);
        kmeans.setDistanceMetric(DistanceMetric.DOT_PRODUCT);
        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.DOT_PRODUCT)*100)/100.
                == 0.97);
    }
    
    public void testKMeans() {
    	kmeans.setCursorType(CollectionDivider.STORE_ON_FIRST_PASS);
    	int collectionID = kmeans.cluster();
    	
    	assertTrue(collectionID == 7);
    	
    	Collection cluster1 = db.getCollection(8);
    	Collection cluster2 = db.getCollection(9);

    	assertTrue(cluster1.containsData());
    	assertTrue(cluster1.getComment().equals("1"));
    	assertTrue(cluster1.getDatatype().equals("ATOFMS"));
    	assertTrue(cluster1.getDescription().startsWith("Key:\tValue:"));
    	assertTrue(cluster1.getName().equals("1"));
    	assertTrue(cluster1.getParentCollection().getCollectionID() == 7);
    	ArrayList<Integer> particles = cluster1.getParticleIDs();
       	assertTrue(particles.get(0) == 2);
    	assertTrue(particles.get(1) == 3);
    	assertTrue(particles.get(2) == 5);
    	assertTrue(cluster1.getSubCollectionIDs().isEmpty());
    	
    	assertTrue(cluster2.containsData());
    	assertTrue(cluster2.getComment().equals("2"));
    	assertTrue(cluster2.getDatatype().equals("ATOFMS"));
    	assertTrue(cluster2.getDescription().startsWith("Key:\tValue:"));
    	assertTrue(cluster2.getName().equals("2"));
    	assertTrue(cluster2.getParentCollection().getCollectionID() == 7);
    	particles = cluster2.getParticleIDs();
    	assertTrue(particles.get(0) == 4);
    	assertTrue(cluster2.getSubCollectionIDs().isEmpty());
    	
    	/** Output:
Error: 1.8666667342185974
Change in error: -1.1920928955078125E-7
Zero count = 1
returning
Clustering Parameters: 
Test clusteringKMeans,K=2


Number of ignored particles with zero peaks = 1
Total clustering passes during sampling = 0
Total number of centroid clustering passes = 0
Total number of passes = 3
Average distance of all points from their centers at each iteration:
0.46666665375232697
0.46666668355464935
0.46666668355464935
average distance of all points from their centers on final assignment:
0.46666668355464935

Peaks in centroids:
Centroid 1: Number of particles = 3
Centroid 2: Number of particles = 1

Centroid 1:
Number of particles in cluster: 3
Key:	Value:
-300	0.06666667
-30	0.34444448
-20	0.06666667
6	0.06666667
30	0.34444448
45	0.11111111
Centroid 2:
Number of particles in cluster: 1
Key:	Value:
-30	0.25
-20	0.25
-10	0.25
20	0.25
    	 */
    	
    }

}
