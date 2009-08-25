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

import analysis.CollectionDivider;

import database.CreateTestDatabase;
import database.InfoWarehouse;
import database.Database;
import junit.framework.TestCase;

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
        String name = "";
        String comment = "Test comment";
        boolean refine = false;
        ArrayList<String> list = new ArrayList<String>();
        list.add("ATOFMSAtomInfoSparse.PeakArea");
    	ClusterInformation cInfo = new ClusterInformation(list, "ATOFMSAtomInfoSparse.PeakLocation", null, false, true);
    	clusterer = new ClusterHierarchical(cID,db,name,comment,cInfo, null);
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
    	assertEquals("Name: Hierarchical, Clusters Ward's,CLUST Comment: Test comment", clusterParent.getDescription());
    	
    	Collection cluster = db.getCollection(8);
    	assertTrue(cluster.containsData());
    	assertEquals("1", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("1", cluster.getName());
    	assertEquals(12, cluster.getParentCollection().getCollectionID()); 
    	ArrayList<Integer> particles = cluster.getParticleIDs();
       	assertEquals(2, particles.get(0).intValue());
    	assertTrue(cluster.getSubCollectionIDs().isEmpty());
    	
    	cluster = db.getCollection(9);
    	assertTrue(cluster.containsData());
    	assertEquals("2", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("2", cluster.getName());
    	assertEquals(12, cluster.getParentCollection().getCollectionID()); 
    	particles = cluster.getParticleIDs();
       	assertEquals(3, particles.get(0).intValue());
    	assertTrue(cluster.getSubCollectionIDs().isEmpty());
    	
    	cluster = db.getCollection(10);
    	assertTrue(cluster.containsData());
    	assertEquals("3", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("3", cluster.getName());
    	assertEquals(13, cluster.getParentCollection().getCollectionID()); 
    	particles = cluster.getParticleIDs();
       	assertEquals(4, particles.get(0).intValue());
    	assertTrue(cluster.getSubCollectionIDs().isEmpty());
    	
    	cluster = db.getCollection(11);
    	assertTrue(cluster.containsData());
    	assertEquals("4", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("4", cluster.getName());
    	assertEquals(13, cluster.getParentCollection().getCollectionID()); 
    	particles = cluster.getParticleIDs();
       	assertEquals(5, particles.get(0).intValue());
    	assertTrue(cluster.getSubCollectionIDs().isEmpty());
    	
    	cluster = db.getCollection(12);
    	assertTrue(cluster.containsData());
    	assertEquals("5", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("5", cluster.getName());
    	assertEquals(14, cluster.getParentCollection().getCollectionID()); 
    	particles = cluster.getParticleIDs();
       	assertEquals(2, particles.get(0).intValue());
       	assertEquals(3, particles.get(1).intValue());
    	assertTrue(cluster.getSubCollectionIDs().size() == 2);
    	
    	cluster = db.getCollection(13);
    	assertTrue(cluster.containsData());
    	assertEquals("6", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("6", cluster.getName());
    	assertEquals(14, cluster.getParentCollection().getCollectionID()); 
    	particles = cluster.getParticleIDs();
       	assertEquals(4, particles.get(0).intValue());
       	assertEquals(5, particles.get(1).intValue());
    	assertTrue(cluster.getSubCollectionIDs().size() == 2);
    	
    	cluster = db.getCollection(14);
    	assertTrue(cluster.containsData());
    	assertEquals("7", cluster.getComment());
    	assertEquals("ATOFMS", cluster.getDatatype());
    	assertEquals("7", cluster.getName());
    	assertEquals(7, cluster.getParentCollection().getCollectionID()); 
    	particles = cluster.getParticleIDs();
       	assertEquals(2, particles.get(0).intValue());
       	assertEquals(3, particles.get(1).intValue());
       	assertEquals(4, particles.get(2).intValue());
       	assertEquals(5, particles.get(3).intValue());
    	assertTrue(cluster.getSubCollectionIDs().size() == 2);
    }
    
}
