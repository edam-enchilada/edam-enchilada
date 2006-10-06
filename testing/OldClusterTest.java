package testing;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.clustering.Centroid;
import analysis.clustering.Cluster;
import analysis.clustering.ClusterInformation;
import analysis.clustering.KMeans;

import database.CreateTestDatabase2;
import database.Database;
import database.InfoWarehouse;
import junit.framework.TestCase;

public class OldClusterTest extends TestCase {
	

	private Cluster cluster;
	private InfoWarehouse db;
	private Connection con;
	

	protected void setUp() throws Exception {
		new CreateTestDatabase2();
		db = Database.getDatabase("TestDB2");
		db.openConnection();
		con = db.getCon();
		//TODO: make this work with differnt types of clusters
		cluster = new KMeans(2, db, 2, "blah", "blah", false, new ClusterInformation());

	}

	protected void tearDown() throws Exception {

		//con.createStatement().executeUpdate("DROP DATABASE TestDB2");
		con.close();
		db.closeConnection();
		
	}

	/**
	 * Just a little place to play around and remember how JUnitTests work.
	 * Must be public or the tester gets shirty.
	 * @throws SQLException
	 */
	public void testFake() throws SQLException{
		con = db.getCon();
		System.out.println(con.isClosed());
		System.out.println(db.getAtomDatatype(1));
	}
	
	public void testAddInfo(){
		//TODO: write actual code
	}
	
	public void testAssignAtomsToNearestCentroid(){
		//TODO: write actual code
		//two cases
			//k-means
			//art-2a
	}
	
	public void testCreateCenterAtoms() throws SQLException{
		//get our fake clusters in order
		makeFakeClusters();
		//now make our fake centroids
		ArrayList<Centroid> centroidList = new ArrayList<Centroid>();
		BinnedPeak p = new BinnedPeak(15, -30);
		BinnedPeak q = new BinnedPeak(15, 30);
		BinnedPeak r = new BinnedPeak(15, (float) 22.5);
		BinnedPeakList bpList = new BinnedPeakList(null);
		bpList.add(p);
		bpList.add(q);
		bpList.add(r);
		Centroid addThis = new Centroid(bpList, 3, 1);
		centroidList.add(addThis);
		BinnedPeak s = new BinnedPeak(15, -30);
		BinnedPeak t = new BinnedPeak(15, -20);
		BinnedPeak u = new BinnedPeak(15, -10);
		BinnedPeak v = new BinnedPeak(15, 20);
		BinnedPeakList bpl = new BinnedPeakList(null);
		bpl.add(s);
		bpl.add(t);
		bpl.add(u);
		bpl.add(v);
		Centroid addMeToo = new Centroid(bpl, 1, 2);
		centroidList.add(addMeToo);
		ArrayList<Integer> clusterIDs = new ArrayList<Integer>();
		clusterIDs.add(7);
		clusterIDs.add(8);
		
		cluster.createCenterAtoms(centroidList, clusterIDs);
		
		//now do the checking blllllllllllah
	}
	
	public void testPrintDescriptionToDB(){
		//TODO: write actual code		
	}
	
	public void testPrintDistanceToNearestCentroid(){
		//TODO: write actual code		
	}
	
	public void testSetDistanceMetric(){
		//TODO: write actual code		
	}
	
	public void testWriteBinnedPeakListToFile(){
		//TODO: write actual code		
	}
	
	private void makeFakeClusters() throws SQLException{
		//write clusters directly to the db (in order to isolate the
		//method we're testing here)
		Statement stmt = con.createStatement();
		String update = "INSERT INTO Collections \n"
			+ " VALUES (7,'Cluster1','cluster','firstcluster','ATOFMS')";
		stmt.addBatch(update);
		update = "INSERT INTO Collections \n"
			+ " VALUES (8,'Cluster2','another cluster','secondcluster','ATOFMS')";
		stmt.addBatch(update);
		update = "INSERT INTO AtomMembership \n"
			+ " VALUES (7,1)";
		stmt.addBatch(update);
		update = "INSERT INTO AtomMembership \n"
			+ " VALUES (7,2)";
		stmt.addBatch(update);
		update = "INSERT INTO AtomMembership \n"
			+ " VALUES (7,3)";
		stmt.addBatch(update);
		update = "INSERT INTO AtomMembership \n"
			+ " VALUES (8,4)";
		stmt.addBatch(update);
		update = "INSERT INTO CollectionRelationships \n"
			+ " VALUES (1,7)";
		stmt.addBatch(update);
		update = "INSERT INTO CollectionRelationships \n"
			+ " VALUES (1,8)";
		stmt.addBatch(update);
		update = "INSERT INTO InternalAtomOrder \n"
			+ " VALUES (1,7)";
		stmt.addBatch(update);
		update = "INSERT INTO InternalAtomOrder \n"
			+ " VALUES (2,7)";
		stmt.addBatch(update);
		update = "INSERT INTO InternalAtomOrder \n"
			+ " VALUES (3,7)";
		stmt.addBatch(update);
		update = "INSERT INTO InternalAtomOrder \n"
			+ " VALUES (4,8)";
		stmt.addBatch(update);
		stmt.executeBatch();
		stmt.close();
	}
}
