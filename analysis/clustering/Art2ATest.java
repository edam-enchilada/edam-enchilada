package analysis.clustering;

import java.util.ArrayList;

import junit.framework.TestCase;

import collection.Collection;

import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import analysis.DistanceMetric;
import analysis.Normalizer;


import database.CreateTestDatabase;
import database.SQLServerDatabase;

public class Art2ATest extends TestCase{
	  private Art2A art2a;
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
	        String name = "Test clustering";
	        String comment = "Test comment";
	        boolean refine = false;
	        ArrayList<String> list = new ArrayList<String>();
	        list.add("ATOFMSAtomInfoSparse.PeakArea");
	    	ClusterInformation cInfo = new ClusterInformation(list, "ATOFMSAtomInfoSparse.PeakLocation", null, false, true);
	    	art2a = new Art2A(cID,db,1.0f, 0.005f,25,DistanceMetric.CITY_BLOCK,comment,cInfo);
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

	        art2a.setDistanceMetric(DistanceMetric.CITY_BLOCK);
	        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.CITY_BLOCK)*100)/100. == 0.7);
	        art2a.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
	        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.EUCLIDEAN_SQUARED)*100)/100.
	                == 0.17);
	        art2a.setDistanceMetric(DistanceMetric.DOT_PRODUCT);
	        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.DOT_PRODUCT)*100)/100.
	                == 0.97);
	    }
	    
	    public void testArt2A() {
	    	art2a.setCursorType(CollectionDivider.STORE_ON_FIRST_PASS);
	    	int collectionID = art2a.cluster();
	    	
	    	assertTrue(collectionID == 7);
	    	
	    	Collection cluster1 = db.getCollection(8);
	    	Collection cluster2 = db.getCollection(9);
	    	Collection cluster3 = db.getCollection(10);

	    	assertTrue(cluster1.containsData());
	    	assertTrue(cluster1.getComment().equals("1"));
	    	assertTrue(cluster1.getDatatype().equals("ATOFMS"));
	    	assertTrue(cluster1.getDescription().equals("Name: 1 Comment: 1"));
	    	assertTrue(cluster1.getName().equals("1"));
	    	assertTrue(cluster1.getParentCollection() == null);
	    	ArrayList<Integer> particles = cluster1.getParticleIDs();
	       	assertTrue(particles.get(0) == 2);
	    	assertTrue(particles.get(1) == 3);
	    	assertTrue(cluster1.getSubCollectionIDs().isEmpty());
	    	
	    	assertTrue(cluster2.containsData());
	    	assertTrue(cluster2.getComment().equals("2"));
	    	assertTrue(cluster2.getDatatype().equals("ATOFMS"));
	    	assertTrue(cluster2.getDescription().equals("Name: 2 Comment: 2"));
	    	assertTrue(cluster2.getName().equals("2"));
	    	assertTrue(cluster2.getParentCollection() == null);
	    	particles = cluster2.getParticleIDs();
	    	assertTrue(particles.get(0) == 4);
	    	assertTrue(cluster2.getSubCollectionIDs().isEmpty());
	    	
	    	assertTrue(cluster3.containsData());
	    	assertTrue(cluster3.getComment().equals("3"));
	    	assertTrue(cluster3.getDatatype().equals("ATOFMS"));
	    	assertTrue(cluster3.getDescription().equals("Name: 3 Comment: 3"));
	    	assertTrue(cluster3.getName().equals("3"));
	    	assertTrue(cluster3.getParentCollection() == null);
	    	particles = cluster3.getParticleIDs();
	    	assertTrue(particles.get(0) == 5);
	    	assertTrue(cluster3.getSubCollectionIDs().isEmpty());
	    	
	    	/** Output:
Pass #:0
Adding new centroid
Adding new centroid
Adding new centroid
about to reset
Pass #:1
about to reset
Pass #:2
about to reset
Pass #:3
about to reset
Pass #:4
about to reset
Pass #:5
about to reset
Pass #:6
about to reset
Pass #:7
about to reset
Pass #:8
about to reset
Pass #:9
about to reset
Pass #:10
about to reset
Pass #:11
about to reset
Clustering Parameters: 
Art2A,V=1.0,LR=0.0050,Passes=25,DMetric=CITY_BLOCK


Number of ignored particles with zero peaks = 1
Total clustering passes during sampling = 0
Total number of centroid clustering passes = 0
Total number of passes = 13
Average distance of all points from their centers at each iteration:
0.166666641831398
0.16667083650827408
0.16667494922876358
0.1666790321469307
0.1666831150650978
0.16668709367513657
0.16669104248285294
0.16669496148824692
0.1666988506913185
0.16670271009206772
0.16670649498701096
0.1667102947831154
0.1666666641831398
average distance of all points from their centers on final assignment:
0.1666666641831398

Peaks in centroids:
Centroid 1: Number of particles = 2
Centroid 2: Number of particles = 1
Centroid 3: Number of particles = 1

Centroid 1:
Number of particles in cluster: 2
Key:	Value:
-30	0.4905308
30	0.4905308
45	0.018938426
Centroid 2:
Number of particles in cluster: 1
Key:	Value:
-30	0.25
-20	0.25
-10	0.25
20	0.25
Centroid 3:
Number of particles in cluster: 1
Key:	Value:
-300	0.2
-30	0.2
-20	0.2
6	0.2
30	0.2
	    	 */
	    	
	    }
}
