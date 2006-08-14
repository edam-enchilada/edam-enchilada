package gui;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.SwingUtilities;

import collection.Collection;

import database.CreateTestDatabase2;
import database.InfoWarehouse;
import database.Database;
import junit.framework.TestCase;

public class AggregatorTest extends TestCase {
	private InfoWarehouse db;
	private Aggregator aggregator;
	
	public AggregatorTest(String aString)
	{
		super(aString);
	}
	
	protected void setUp()
	{
		new CreateTestDatabase2(); 		
		db = Database.getDatabase("TestDB2");
	}
	
	protected void tearDown()
	{
		db.closeConnection();
		try {
			System.runFinalization();
			System.gc();
			db = Database.getDatabase("");
			db.openConnection();
			Connection con = db.getCon();
			//con.createStatement().executeUpdate("DROP DATABASE TestDB2");
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This test aggregates an ATOFMS collection, a TimeSeries collection,
	 * and an AMS collections, all of which overlap in TestDB2.  It chooses
	 * a collection for each aggregated datatype and tests it.  For ATOFMS and
	 * AMS, it chooses an m/z value and tests it; for TimeSeries, it just tests
	 * a time.
	 *
	 */
	public void testCreateAggregateTimeSeries(){

/*		db.openConnection();
		ResultSet rs;
		Statement stmt;
		try {
			stmt = db.getCon().createStatement();

		aggregator = new Aggregator(null, db, db.getCollection(2));
		final Collection[] collections = {db.getCollection(2),
				db.getCollection(4),db.getCollection(5)};
		final ProgressBarWrapper progressBar =
			aggregator.createAggregateTimeSeriesPrepare(collections);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame.db = db;
				MainFrame mf = new MainFrame();
				try {
					int cID = aggregator.createAggregateTimeSeries("aggregated",collections,
							progressBar, mf);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		// check number of collections:
		rs = stmt.executeQuery("SELECT COUNT(*) FROM Collections;\n");
		assertTrue(rs.next());
		assertEquals(rs.getInt(1),35);
		
		// check ATOFMS m/z collection:
		rs = stmt.executeQuery("SELECT AtomID FROM AtomMembership" +
				" WHERE CollectionID = 13 ORDER BY AtomID;\n");
		assertTrue(rs.next());
		assertEquals(rs.getInt(1),35);
		assertTrue(rs.next());
		assertEquals(rs.getInt(1),36);
		assertTrue(rs.next());
		assertEquals(rs.getInt(1),37);
		
		rs = stmt.executeQuery("SELECT Time, Value FROM " +
				"TimeSeriesAtomInfoDense WHERE AtomID = 35;");
		rs.next();
		assertTrue(rs.getDate(1).toString().equals("2003-09-02"));
		assertTrue(rs.getInt(2)==12);
		
		// check TimeSeries collection:
		rs = stmt.executeQuery("SELECT AtomID FROM AtomMembership" +
		" WHERE CollectionID = 29 ORDER BY AtomID;\n");
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==67);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==68);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==69);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==70);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==71);
		
		rs = stmt.executeQuery("SELECT Time, Value FROM " +
				"TimeSeriesAtomInfoDense WHERE AtomID = 67;");
		assertTrue(rs.next());
		assertTrue(rs.getDate(1).toString().equals("2003-09-02"));
		assertTrue(rs.getInt(2)==0);
		
		// check AMS m/z collections:
		rs = stmt.executeQuery("SELECT AtomID FROM AtomMembership" +
		" WHERE CollectionID = 32 ORDER BY AtomID;\n");
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==72);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==73);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==74);
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==75);
		
		rs = stmt.executeQuery("SELECT Time, Value FROM " +
		"TimeSeriesAtomInfoDense WHERE AtomID = 72;");
		assertTrue(rs.next());
		assertTrue(rs.getDate(1).toString().equals("2003-09-02"));
		assertTrue(rs.getInt(2)==1);
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		db.closeConnection();*/
	}
	
	/**
	 * This was written in response to a bug that the parent collection wasn't 
	 * getting aggregated properly.  All it does is count the number of 
	 * collections when aggregating a parent collection and then count the number
	 * of collections when aggregating the parent's child.  This is ok, since the
	 * test above acutally tests the aggregation.
	 *
	 */
	public void testParentAggregation() {

		/*db.openConnection();
		ResultSet rs;
		Statement stmt;
		try {
			stmt = db.getCon().createStatement();

		aggregator = new Aggregator(null, db, db.getCollection(2));
		final Collection[] collections1 = {db.getCollection(2)};
		final ProgressBarWrapper progressBar1 =
			aggregator.createAggregateTimeSeriesPrepare(collections1);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame.db = db;
				MainFrame mf = new MainFrame();
				try {
					int cID = aggregator.createAggregateTimeSeries("aggregated1",
							collections1,progressBar1, mf);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		// check number of collections:
		rs = stmt.executeQuery("SELECT COUNT(*) FROM Collections;\n");
		assertTrue(rs.next());
		System.out.println("Output = " + rs.getInt(1));
		assertTrue(rs.getInt(1)==29);
		
		
		aggregator = new Aggregator(null, db, db.getCollection(3));
		final Collection[] collections2 = {db.getCollection(3)};
		final ProgressBarWrapper progressBar2 =
			aggregator.createAggregateTimeSeriesPrepare(collections2);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame.db = db;
				MainFrame mf = new MainFrame();
				try {
					int cID = aggregator.createAggregateTimeSeries("aggregated2",
							collections2,progressBar2, mf);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		// check number of collections:
		rs = stmt.executeQuery("SELECT COUNT(*) FROM Collections;\n");
		assertTrue(rs.next());
		assertTrue(rs.getInt(1)==47);
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		db.closeConnection();*/
	}
	
	/*
	 * Need a unit test here for interval aggregation
	 */
	
}