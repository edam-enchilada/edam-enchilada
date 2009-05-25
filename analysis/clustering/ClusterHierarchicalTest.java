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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import collection.Collection;

import ATOFMS.ParticleInfo;
import analysis.CollectionDivider;

import database.CreateTestDatabase;
import database.InfoWarehouse;
import database.Database;
import junit.framework.TestCase;
import database.CollectionCursor;

/*
 * Created on April 23, 2009
 *
 *
 */

/**
 * @author jtbigwoo
 *
 */
public class ClusterHierarchicalTest extends TestCase {

    private ClusterHierarchical clusterer;
    private InfoWarehouse db;
    String dbName = "TestDB";
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
		new CreateTestDatabase();
		db = Database.getDatabase("TestDB");
		db.openConnection("TestDB");
		
        int cID = 2;
        int k = 2;
        String name = "";
        String comment = "Test comment";
        boolean refine = false;
        ArrayList<String> list = new ArrayList<String>();
        list.add("ATOFMSAtomInfoSparse.PeakArea");
    	ClusterInformation cInfo = new ClusterInformation(list, "ATOFMSAtomInfoSparse.PeakLocation", null, false, true);
    	clusterer = new ClusterHierarchical(cID,db,k,name,comment,cInfo);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
		db.closeConnection();
		System.runFinalization();
		System.gc();
//	    Database.dropDatabase(dbName);
    }

    public void testHierarchicalClustering() throws Exception {
    	clusterer.setCursorType(CollectionDivider.STORE_ON_FIRST_PASS);
    	int collectionID = clusterer.cluster(false);
    	
    	assertTrue(collectionID == 7);
    	
    	Collection clusterParent = db.getCollection(7);
    	BufferedReader descReader = new BufferedReader(new StringReader(clusterParent.getDescription()));
    	for (int i = 0; i < 4; i++) descReader.readLine();
    	assertEquals("Number of ignored particles with zero peaks = 0", descReader.readLine());
    	// the preceeding test assertion is actually incorrect, the correct test follows, see bug 2772661
    	//assertEquals("Number of ignored particles with zero peaks = 1", descReader.readLine());

    	// check the std deviations in the collection description
    	for (int i = 0; i < 15; i++) descReader.readLine();
		assertEquals("Mean size: 0.25 Std dev: +/-0.05000005", descReader.readLine());
		assertEquals("Geometric mean size: 0.24494898", descReader.readLine());
		for (int i = 0; i < 6; i++) descReader.readLine();
		assertEquals("Mean size: 0.45 Std dev: +/-0.050000273", descReader.readLine());
		assertEquals("Geometric mean size: 0.4472136", descReader.readLine());
    	
    	Collection cluster1 = db.getCollection(8);
    	Collection cluster2 = db.getCollection(9);
    	Collection clusterCenters = db.getCollection(10);

    	assertTrue(cluster1.containsData());
    	assertEquals("1", cluster1.getComment());
    	assertEquals("ATOFMS", cluster1.getDatatype());
    	assertTrue(cluster1.getDescription().startsWith("Key:\tValue:"));
    	assertEquals("1", cluster1.getName());
    	assertEquals(7, cluster1.getParentCollection().getCollectionID());
    	ArrayList<Integer> particles = cluster1.getParticleIDs();
       	assertEquals(2, particles.get(0).intValue());
    	assertEquals(3, particles.get(1).intValue());
    	assertTrue(cluster1.getSubCollectionIDs().isEmpty());
    	
    	assertTrue(cluster2.containsData());
    	assertEquals("2", cluster2.getComment());
    	assertEquals("ATOFMS", cluster2.getDatatype());
    	assertTrue(cluster2.getDescription().startsWith("Key:\tValue:"));
    	assertEquals("2", cluster2.getName());
    	assertEquals(7, cluster2.getParentCollection().getCollectionID());
    	particles = cluster2.getParticleIDs();
    	assertEquals(4, particles.get(0).intValue());
    	assertEquals(5, particles.get(1).intValue());
    	assertTrue(cluster2.getSubCollectionIDs().isEmpty());

    	CollectionCursor denseCurs = db.getAtomInfoOnlyCursor(clusterCenters);
    	denseCurs.next();
    	ParticleInfo info = denseCurs.getCurrent();
    	assertEquals(0.25f, info.getATOFMSParticleInfo().getSize());
    	denseCurs.next();
    	info = denseCurs.getCurrent();
    	assertEquals(.45f, info.getATOFMSParticleInfo().getSize());

    	CollectionCursor sparseCurs = db.getBPLOnlyCursor(clusterCenters);
    	sparseCurs.next();
    	info = sparseCurs.getCurrent();
    	Map<Integer, Float> peaks = info.getBinnedList().getPeaks();
    	assertEquals(5000.0f, peaks.get(-30).floatValue());
    	assertEquals(3750.0f, peaks.get(30).floatValue());
    	assertEquals(1250.0f, peaks.get(45).floatValue());
    	sparseCurs.next();
    	info = sparseCurs.getCurrent();
    	peaks = info.getBinnedList().getPeaks();
    	assertEquals(833.0f, peaks.get(-300).floatValue());
    	assertEquals(1666.0f, peaks.get(-30).floatValue());
    	assertEquals(1666.0f, peaks.get(-20).floatValue());
    	assertEquals(833.0f, peaks.get(-10).floatValue());
    	assertEquals(1250.0f, peaks.get(6).floatValue());
    	assertEquals(2500.0f, peaks.get(20).floatValue());
    	assertEquals(1250.0f, peaks.get(30).floatValue());
    	
    	/** Output:
			Clustering Parameters: 
			KMeans,K=2,Test comment
			
			
			Number of ignored particles with zero peaks = 0
			Total clustering passes during sampling = 0
			Total number of centroid clustering passes = 0
			Total number of passes = 3
			Average distance of all points from their centers at each iteration:
			0.416666641831398
			0.47222214937210083
			0.4722222685813904
			average distance of all points from their centers on final assignment:
			0.4722222685813904
			
			Peaks in centroids:
			Centroid 1: Number of particles = 3
			Centroid 2: Number of particles = 1
			
			Centroid 1:
			Number of particles in cluster: 3
			Mean size: 0.33333334 Std dev: +/-0.12472187
			Geometric mean size: 0.31072325
			Key:	Value:
			-300	555.5556
			-30	3888.8887
			-20	555.5556
			6	833.3334
			30	3333.3335
			45	833.3334
			Centroid 2:
			Number of particles in cluster: 1
			Mean size: 0.4 Std dev: +/-0.0
			Geometric mean size: 0.4
			Key:	Value:
			-30	1666.6667
			-20	1666.6667
			-10	1666.6667
			20	5000.0
		*/
    }
    
}
