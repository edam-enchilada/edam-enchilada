package database;

import java.text.SimpleDateFormat;
import java.sql.*;

public class TSBulkInserter {
	private StringBuilder vals, membership, dataset;
	private String tabName;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean started = false;
	private int collectionID, datasetID, nextID, firstID;
	private int maxBufferSize = 1024 * 950 * 3; // a bit before 1M for each StringBuilder.
	
	
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
		vals = new StringBuilder(2048);
		membership = new StringBuilder(2048);
		dataset = new StringBuilder(2048);
		tabName = db.getDynamicTableName(DynamicTable.AtomInfoDense, "TimeSeries");
		con = db.getCon();
		nextID = firstID = collectionID = datasetID = -1;
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

	
	public void addPoint(java.util.Date time, float val) throws SQLException {
		if (!started) {
			throw new Error("Haven't called startDataset() before adding a point.");
		}
		vals.append("INSERT INTO " + tabName 
				+ " VALUES (" + nextID + ",'"
				+ df.format(time) + "',"
				+ val +")");
		membership.append("INSERT INTO AtomMembership" +
				"(CollectionID, AtomID)" +
				"VALUES (" + collectionID + "," +
				nextID + ")");
		dataset.append("INSERT INTO DataSetMembers" +
			"(OrigDataSetID, AtomID)" +
			" VALUES (" + datasetID + "," + nextID + ")");
		
		nextID++;
		
		if (vals.length() + membership.length() + dataset.length() > maxBufferSize) {
			interimCommit();
		}
	
	}
	
	private void interimCommit() throws SQLException {
		System.out.println("Committing some particles.");
		if (db.getNextID() != firstID) {
			throw new RuntimeException("Database has changed under a batch insert.. you can't do that!");
		}
		
		Statement st = con.createStatement();
		st.execute(membership.toString());
		st.execute(vals.toString());
		st.execute(dataset.toString());
		st.close();
		
		vals.setLength(0); // make it into nothing!
		dataset.setLength(0);
		membership.setLength(0);
		
		firstID = nextID = db.getNextID();
	}
	
	public int commit() throws SQLException {
		interimCommit();
		started = false;
		
		db.updateAncestors(db.getCollection(collectionID));
		
		collectionID = -1;
		datasetID = -1;
		nextID = firstID = -1;
		
		return collectionID;
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}
}
