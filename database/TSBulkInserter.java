package database;

import java.text.SimpleDateFormat;
import java.sql.*;

public class TSBulkInserter {
	private StringBuilder vals, membership, dataset;
	private String tabName;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean started = false;
	private int collectionID, datasetID, nextID, firstID;
	
	private SQLServerDatabase db;
	private Connection con;
	
	public TSBulkInserter() {
		db = new SQLServerDatabase();
		db.openConnection();
		setUp();
	}

	public TSBulkInserter(SQLServerDatabase db) {
		this.db = db;
		setUp();
	}

	private void setUp() {
		vals = new StringBuilder(1024);
		membership = new StringBuilder(1024);
		dataset = new StringBuilder(1024);
		tabName = db.getDynamicTableName(DynamicTable.AtomInfoDense, "TimeSeries");
		con = db.getCon();
	}
	
	public int[] startDataset(String collName) {
		int[] collectionInfo = db.createEmptyCollectionAndDataset(
				"TimeSeries",
				0,
				collName,
				"",
				"-1,0");
		collectionID = collectionInfo[0];
		datasetID = collectionInfo[1];
		nextID = firstID = db.getNextID();
		started = true;
		return collectionInfo;
	}

	
	public void addPoint(java.util.Date time, float val) {
		if (!started) {
			throw new Error("Haven't called startDataset() before adding a point.");
		}
		vals.append("INSERT INTO " + tabName 
				+ " VALUES (" + nextID + ",'"
				+ df.format(time) + "',"
				+ val +")\n");
		membership.append("INSERT INTO AtomMembership" +
				"(CollectionID, AtomID)" +
				"VALUES (" + collectionID + ", " +
				nextID + ")");
		dataset.append("INSERT INTO DataSetMembers" +
			"(OrigDataSetID, AtomID)" +
			" VALUES (" + datasetID + "," + nextID + ")");
		
		nextID++;
	
	}
	
	public int commit() throws SQLException {
		if (vals.length() == 0) {
			System.err.println("Committing 0 particles... weird!  Doing it anyway.");
			Thread.dumpStack();
		}
		if (db.getNextID() != firstID) {
			throw new RuntimeException("Database has changed under a batch insert.. you can't do that!");
		}
		
		Statement st = con.createStatement();
		st.execute(membership.toString());
		st.execute(vals.toString());
		st.execute(dataset.toString());
		st.close();
	
		started = false;
		
		db.updateAncestors(db.getCollection(collectionID));
		
		collectionID = -1;
		datasetID = -1;
		nextID = firstID = -1;
		vals.setLength(0); // make it into nothing!
		dataset.setLength(0);
		membership.setLength(0);
		
		
		return collectionID;
	}
}
