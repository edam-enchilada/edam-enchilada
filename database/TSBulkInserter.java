package database;

import java.text.SimpleDateFormat;
import java.sql.*;

/**
 * This class makes it fairly simple to insert a lot of time series data quickly.
 * <p>
 * It is not synchronized or anything, so only use one at a time!  
 * Otherwise you'll get conflicting AtomIDs.
 * 
 * @author smitht
 *
 */
public class TSBulkInserter {
	private StringBuilder vals, membership, dataset;
	private String tabName;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean started = false;
	private int collectionID, datasetID, nextID, firstID;
	private int maxBufferSize = 1024 * 950 * 3; // a bit before 1M for each StringBuilder.
	
	
	private InfoWarehouse db;
	private Connection con;
	
	/**
	 * Create a new TSBulkInserter with its own database connection.
	 *
	 */
	public TSBulkInserter() {
		db = Database.getDatabase();
		db.openConnection();
		setUp();
	}

	/**
	 * Create a new TSBulkInserter with an already-connected database.
	 * @param db
	 */
	public TSBulkInserter(InfoWarehouse db) {
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
	
	/**
	 * Creates the {@link Collection} that the time series info will go into,
	 * and sets everything up for adding the data.
	 * 
	 * @param collName
	 * @return an array containing the collectionID (index 0) and the datasetID (1).
	 */
	public int[] startDataset(String collName) {
		if (started) throw new Error("Bad order of calls to TSBulkInserter");
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

	/**
	 * Add a time, value pair to be inserted.  If there are enough pairs built up,
	 * this method may run some of the queries.
	 * 
	 * @param time the time at which the observation was made
	 * @param val the value of the observation
	 * @throws SQLException
	 */
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
		membership.append("INSERT INTO InternalAtomOrder" +
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
	
	/**
	 * commits the particles that are currently queued up in the StringBuffers.
	 * @throws SQLException
	 */
	private void interimCommit() throws SQLException {
		if (db.getNextID() != firstID) {
			throw new SQLException("Database has changed under a batch insert.. you can't do that!");
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
	
	/**
	 * Put all the time, value pairs that have been added using addPoint into the database.
	 * <p>
	 * Note that some may have been put into the database before you call this method.
	 * 
	 * @return the collectionID of the collection which holds the observations.
	 * @throws SQLException
	 */
	public int commit() throws SQLException {
		interimCommit();
		started = false;
		
		db.propagateNewCollection(db.getCollection(collectionID));
		
		int ret = collectionID;
		
		collectionID = -1;
		datasetID = -1;
		nextID = firstID = -1;
		
		return ret;
	}

	/**
	 * Returns the maximum size that TSBulkInserter will let its queues of 
	 * SQL insert statements grow to, in bytes.
	 */
	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	/**
	 * Set the maximum amount of data to queue up between commits to the database.
	 * @param maxBufferSize in bytes
	 */
	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}
}
