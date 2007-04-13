package analysis.clustering;

import java.util.ArrayList;

import collection.Collection;

import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.DistanceMetric;
import analysis.Normalizer;


import database.CreateTestDatabase;
import database.InfoWarehouse;
import database.Database;
import junit.framework.TestCase;

public class KMediansTest extends TestCase {

	    private KMedians kmedians;
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
	        String name = "Test clustering";
	        String comment = "Test comment";
	        boolean refine = false;
	        ArrayList<String> list = new ArrayList<String>();
	        list.add("ATOFMSAtomInfoSparse.PeakArea");
	    	ClusterInformation cInfo = new ClusterInformation(list, "ATOFMSAtomInfoSparse.PeakLocation", null, false, true);
	        kmedians = new KMedians(cID,db,k,name,comment,refine,cInfo);
	    }

	    /*
	     * @see TestCase#tearDown()
	     */
	    protected void tearDown() throws Exception {
	        super.tearDown();
			db.closeConnection();
			System.runFinalization();
			System.gc();
		    Database.dropDatabase(dbName);
	    }

	    public void testGetDistance() {
	        BinnedPeakList list1 = new BinnedPeakList(new Normalizer());
	        BinnedPeakList list2 = new BinnedPeakList(new Normalizer());
	        list1.add(1,0.1f);
	        list1.add(2,0.2f);
	        list2.add(1,0.3f);
	        list2.add(3,0.3f);

	        kmedians.setDistanceMetric(DistanceMetric.CITY_BLOCK);
	        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.CITY_BLOCK)*100)/100. == 0.7);
	        kmedians.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
	        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.EUCLIDEAN_SQUARED)*100)/100.
	                == 0.17);
	        kmedians.setDistanceMetric(DistanceMetric.DOT_PRODUCT);
	        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.DOT_PRODUCT)*100)/100.
	                == 0.97);
	    }
	    
	    public void testKMedians() {
	    
	    	kmedians.setCursorType(CollectionDivider.STORE_ON_FIRST_PASS);
	    	int collectionID = kmedians.cluster();
	    	
	    	assertTrue(collectionID == 7);
	    	
	    	Collection cluster1 = db.getCollection(8);
	    	Collection cluster2 = db.getCollection(9);

	    	assertTrue(cluster1.containsData());
	    	assertTrue(cluster1.getComment().equals("1"));
	    	assertTrue(cluster1.getDatatype().equals("ATOFMS"));
	    	assertTrue(cluster1.getDescription().startsWith("Key:\tValue:"));
	    	assertTrue(cluster1.getName().equals("1"));
	    	assertEquals(cluster1.getParentCollection().getCollectionID(), 7);
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
	    	assertEquals(cluster2.getParentCollection().getCollectionID(),7);
	    	particles = cluster2.getParticleIDs();
	    	assertTrue(particles.get(0) == 4);
	    	assertTrue(cluster2.getSubCollectionIDs().isEmpty());
	    	
	    	/** Output:
Error: 1.866666555404663
Change in error: 5.9604644775390625E-8
Zero count = 1
returning
Clustering Parameters: 
Test clusteringKMedians,K=2


Number of ignored particles with zero peaks = 1
Total clustering passes during sampling = 0
Total number of centroid clustering passes = 0
Total number of passes = 3
Average distance of all points from their centers at each iteration:
0.46666665375232697
0.46666663885116577
0.46666663885116577
average distance of all points from their centers on final assignment:
0.46666663885116577

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
