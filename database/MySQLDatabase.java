package database;

import gui.LabelingIon;
import gui.ProgressBarWrapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import ATOFMS.Peak;
import analysis.clustering.ClusterInformation;
import collection.Collection;
import database.SQLServerDatabase.BPLOnlyCursor;

public class MySQLDatabase extends Database {
	public MySQLDatabase() {
		url = "localhost";
		port = "3306";
		database = "SpASMSdb";
		loadConfiguration("MySQL");
	}
	
	public MySQLDatabase(String dbName) {
		this();
		database = dbName;
	}
	
	public boolean isPresent() {
		return isPresentImpl("SHOW DATABASES");
	}
	
	public boolean openConnection() {
		return openConnectionImpl(
				"com.mysql.jdbc.Driver",
				"jdbc:mysql://" + url + ":" + port + "/" + database,
				"SpASMS",
				"finally");
	}
	
	public boolean addAtom(int atomID, int parentID) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addAtomBatch(int atomID, int parentID) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean addCenterAtom(int centerAtomID, int centerCollID) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public void addCompressedDatatype(String newDatatype, String oldDatatype) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public String aggregateColumn(DynamicTable atomInfoDense, String string, ArrayList<Integer> curIDs, String oldDatatype) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int applyMap(String mapName, Vector<int[]> map, Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public void atomBatchExecute() {
		System.err.println("Not done!"); // TODO fix
		
	}

	public void atomBatchInit() {
		System.err.println("Not done!"); // TODO fix
		
	}

	public boolean beginTransaction() {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public void buildAtomRemovedIons(int atomID, ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public boolean checkAtomParent(int atomID, int isMemberOf) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean closeConnection() {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean commitTransaction() {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public int copyCollection(Collection collection, Collection toCollection) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public boolean createAggregateTimeSeries(ProgressBarWrapper progressBar, int rootCollectionID, Collection curColl, int[] mzValues) throws InterruptedException {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public ArrayList<TreeMap<Date, Double>> createAndDetectPlumesFromMedian(Collection collection, double magnitude, int minDuration) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<TreeMap<Date, Double>> createAndDetectPlumesFromPercent(Collection collection, double magnitude, int minDuration) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<TreeMap<Date, Double>> createAndDetectPlumesFromValue(Collection collection, double magnitude, int minDuration) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int createEmptyCollection(String datatype, int parent, String name, String comment, String description) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public int[] createEmptyCollectionAndDataset(String datatype, int parent, String datasetName, String comment, String params) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public void createTempAggregateBasis(Collection c, Collection basis) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public void createTempAggregateBasis(Collection c, Calendar start, Calendar end, Calendar interval) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public boolean deleteAtomBatch(int atomID, Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean deleteAtomsBatch(String atomIDs, Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public void deleteTempAggregateBasis() {
		System.err.println("Not done!"); // TODO fix
		
	}

	public Date exportToMSAnalyzeDatabase(Collection collection, String newName, String sOdbcConnection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public Set<Integer> getAllDescendantCollections(int collectionID, boolean includeTopLevel) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<Integer> getAllDescendedAtoms(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public CollectionCursor getAtomInfoOnlyCursor(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public BPLOnlyCursor getBPLOnlyCursor(Collection collection) throws SQLException {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public CollectionCursor getBinnedCursor(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public CollectionCursor getClusteringCursor(Collection collection, ClusterInformation cInfo) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<String> getColNames(String datatype, DynamicTable table) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<ArrayList<String>> getColNamesAndTypes(String datatype, DynamicTable table) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public Collection getCollection(int collectionID) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public String getCollectionComment(int collectionID) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public String getCollectionDatatype(int subCollectionNum) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<Date> getCollectionDates(Collection seq1, Collection seq2) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public String getCollectionDescription(int collectionID) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<Integer> getCollectionIDsWithAtoms(java.util.Collection<Integer> collectionIDs) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public String getCollectionName(int collectionID) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int getCollectionSize(int collectionID) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public Connection getCon() {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public Hashtable<Date, Double> getConditionalTSCollectionData(Collection seq, ArrayList<Collection> conditionalSeqs, ArrayList<String> conditionStrs) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public String getDynamicTableName(DynamicTable table, String datatype) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int getFirstAtomInCollection(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public ArrayList<Integer> getImmediateSubCollections(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public void getMaxMinDateInCollections(Collection[] collections, Calendar minDate, Calendar maxDate) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public CollectionCursor getMemoryBinnedCursor(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public CollectionCursor getMemoryClusteringCursor(Collection collection, ClusterInformation cInfo) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int getNextID() {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public double getNumber() {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public int getParentCollectionID(int collectionID) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public CollectionCursor getPeakCursor(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<Peak> getPeaks(String datatype, int atomID) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public ArrayList<String> getPrimaryKey(String datatype, DynamicTable atomInfoSparse) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public CollectionCursor getRandomizedCursor(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public CollectionCursor getSQLCursor(Collection collection, String where) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public HashMap<Integer, ArrayList<Integer>> getSubCollectionsHierarchy(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int[] getValidSelectedMZValuesForCollection(Collection collection, Date startDate, Date endDate) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public Vector<int[]> getValueMapRanges() {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public Hashtable<Integer, String> getValueMaps() {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

	public int insertParticle(String dense, ArrayList<String> sparse, Collection collection, int datasetID, int nextID) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public int insertParticle(String dense, ArrayList<String> sparse, Collection collection, int nextID) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public boolean isDirty() {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean moveAtom(int atomID, int fromParentID, int toCollectionID) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean moveCollection(Collection collection, Collection toCollection) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean orphanAndAdopt(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean recursiveDelete(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public boolean rollbackTransaction() {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public void saveAtomRemovedIons(int atomID, ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public int saveMap(String name, Vector<int[]> mapRanges) {
		System.err.println("Not done!"); // TODO fix
		return 0;
	}

	public void seedRandom(int seed) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public boolean setCollectionDescription(Collection collection, String description) {
		System.err.println("Not done!"); // TODO fix
		return false;
	}

	public void syncWithIonsInDB(ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public void updateAncestors(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public void updateInternalAtomOrder(Collection collection) {
		System.err.println("Not done!"); // TODO fix
		
	}

	public Vector<Vector<Object>> updateParticleTable(Collection collection, Vector<Vector<Object>> particleTable, int lowAtomID, int hightAtomID) {
		System.err.println("Not done!"); // TODO fix
		return null;
	}

}
