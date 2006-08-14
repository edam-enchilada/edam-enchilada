package database;

import gui.LabelingIon;
import gui.ProgressBarWrapper;

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
				"root",
				"sa-account-password");
	}
	
	public void notdone() {
		StackTraceElement sti = null;
		try {
			throw new Exception();
		}
		catch (Exception ex) {
			sti = ex.getStackTrace()[1];
		}
		
		String message = "Not done: ";
		message += sti.getClassName() + "." + sti.getMethodName();
		message += "(" + sti.getFileName() + ":" + sti.getLineNumber() + ")";
		System.err.println(message);
	}
	
	public boolean addAtom(int atomID, int parentID) {
		notdone(); // TODO fix
		return false;
	}

	public boolean addAtomBatch(int atomID, int parentID) {
		notdone();// TODO fix
		return false;
	}

	public boolean addCenterAtom(int centerAtomID, int centerCollID) {
		notdone();// TODO fix
		return false;
	}

	public void addCompressedDatatype(String newDatatype, String oldDatatype) {
		notdone();// TODO fix
		
	}

	public String aggregateColumn(DynamicTable atomInfoDense, String string, ArrayList<Integer> curIDs, String oldDatatype) {
		notdone();// TODO fix
		return null;
	}

	public int applyMap(String mapName, Vector<int[]> map, Collection collection) {
		notdone();// TODO fix
		return 0;
	}

	public void atomBatchExecute() {
		notdone();// TODO fix
		
	}

	public void atomBatchInit() {
		notdone();// TODO fix
		
	}

	public boolean beginTransaction() {
		notdone();// TODO fix
		return false;
	}

	public void buildAtomRemovedIons(int atomID, ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		notdone();// TODO fix
		
	}

	public boolean checkAtomParent(int atomID, int isMemberOf) {
		notdone();// TODO fix
		return false;
	}

	public boolean commitTransaction() {
		notdone();// TODO fix
		return false;
	}

	public int copyCollection(Collection collection, Collection toCollection) {
		notdone();// TODO fix
		return 0;
	}

	public boolean createAggregateTimeSeries(ProgressBarWrapper progressBar, int rootCollectionID, Collection curColl, int[] mzValues) throws InterruptedException {
		notdone();// TODO fix
		return false;
	}

	public ArrayList<TreeMap<Date, Double>> createAndDetectPlumesFromMedian(Collection collection, double magnitude, int minDuration) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<TreeMap<Date, Double>> createAndDetectPlumesFromPercent(Collection collection, double magnitude, int minDuration) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<TreeMap<Date, Double>> createAndDetectPlumesFromValue(Collection collection, double magnitude, int minDuration) {
		notdone();// TODO fix
		return null;
	}

	public int createEmptyCollection(String datatype, int parent, String name, String comment, String description) {
		notdone();// TODO fix
		return 0;
	}

	public int[] createEmptyCollectionAndDataset(String datatype, int parent, String datasetName, String comment, String params) {
		notdone();// TODO fix
		return null;
	}

	public void createTempAggregateBasis(Collection c, Collection basis) {
		notdone();// TODO fix
		
	}

	public void createTempAggregateBasis(Collection c, Calendar start, Calendar end, Calendar interval) {
		notdone();// TODO fix
		
	}

	public boolean deleteAtomBatch(int atomID, Collection collection) {
		notdone();// TODO fix
		return false;
	}

	public boolean deleteAtomsBatch(String atomIDs, Collection collection) {
		notdone();// TODO fix
		return false;
	}

	public void deleteTempAggregateBasis() {
		notdone();// TODO fix
		
	}

	public Date exportToMSAnalyzeDatabase(Collection collection, String newName, String sOdbcConnection) {
		notdone();// TODO fix
		return null;
	}

	public Set<Integer> getAllDescendantCollections(int collectionID, boolean includeTopLevel) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<Integer> getAllDescendedAtoms(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public CollectionCursor getAtomInfoOnlyCursor(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public BPLOnlyCursor getBPLOnlyCursor(Collection collection) throws SQLException {
		notdone();// TODO fix
		return null;
	}

	public CollectionCursor getBinnedCursor(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public CollectionCursor getClusteringCursor(Collection collection, ClusterInformation cInfo) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<String> getColNames(String datatype, DynamicTable table) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<ArrayList<String>> getColNamesAndTypes(String datatype, DynamicTable table) {
		notdone();// TODO fix
		return null;
	}

	public Collection getCollection(int collectionID) {
		notdone();// TODO fix
		return null;
	}

	public String getCollectionComment(int collectionID) {
		notdone();// TODO fix
		return null;
	}

	public String getCollectionDatatype(int subCollectionNum) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<Date> getCollectionDates(Collection seq1, Collection seq2) {
		notdone();// TODO fix
		return null;
	}

	public String getCollectionDescription(int collectionID) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<Integer> getCollectionIDsWithAtoms(java.util.Collection<Integer> collectionIDs) {
		notdone();// TODO fix
		return null;
	}

	public String getCollectionName(int collectionID) {
		notdone();// TODO fix
		return null;
	}

	public int getCollectionSize(int collectionID) {
		notdone();// TODO fix
		return 0;
	}



	public Hashtable<Date, Double> getConditionalTSCollectionData(Collection seq, ArrayList<Collection> conditionalSeqs, ArrayList<String> conditionStrs) {
		notdone();// TODO fix
		return null;
	}

	public String getDynamicTableName(DynamicTable table, String datatype) {
		notdone();// TODO fix
		return null;
	}

	public int getFirstAtomInCollection(Collection collection) {
		notdone();// TODO fix
		return 0;
	}

	public ArrayList<Integer> getImmediateSubCollections(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public void getMaxMinDateInCollections(Collection[] collections, Calendar minDate, Calendar maxDate) {
		notdone();// TODO fix
		
	}

	public CollectionCursor getMemoryBinnedCursor(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public CollectionCursor getMemoryClusteringCursor(Collection collection, ClusterInformation cInfo) {
		notdone();// TODO fix
		return null;
	}

	public int getNextID() {
		notdone();// TODO fix
		return 0;
	}

	public double getNumber() {
		notdone();// TODO fix
		return 0;
	}

	public int getParentCollectionID(int collectionID) {
		notdone();// TODO fix
		return 0;
	}

	public CollectionCursor getPeakCursor(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<Peak> getPeaks(String datatype, int atomID) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<String> getPrimaryKey(String datatype, DynamicTable atomInfoSparse) {
		notdone();// TODO fix
		return null;
	}

	public CollectionCursor getRandomizedCursor(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public CollectionCursor getSQLCursor(Collection collection, String where) {
		notdone();// TODO fix
		return null;
	}

	public HashMap<Integer, ArrayList<Integer>> getSubCollectionsHierarchy(Collection collection) {
		notdone();// TODO fix
		return null;
	}

	public int[] getValidSelectedMZValuesForCollection(Collection collection, Date startDate, Date endDate) {
		notdone();// TODO fix
		return null;
	}

	public Vector<int[]> getValueMapRanges() {
		notdone();// TODO fix
		return null;
	}

	public Hashtable<Integer, String> getValueMaps() {
		notdone();// TODO fix
		return null;
	}

	public int insertParticle(String dense, ArrayList<String> sparse, Collection collection, int datasetID, int nextID) {
		notdone();// TODO fix
		return 0;
	}

	public int insertParticle(String dense, ArrayList<String> sparse, Collection collection, int nextID) {
		notdone();// TODO fix
		return 0;
	}

	public boolean isDirty() {
		notdone();// TODO fix
		return false;
	}

	public boolean moveAtom(int atomID, int fromParentID, int toCollectionID) {
		notdone();// TODO fix
		return false;
	}

	public boolean moveCollection(Collection collection, Collection toCollection) {
		notdone();// TODO fix
		return false;
	}

	public boolean orphanAndAdopt(Collection collection) {
		notdone();// TODO fix
		return false;
	}

	public boolean recursiveDelete(Collection collection) {
		notdone();// TODO fix
		return false;
	}

	public boolean rollbackTransaction() {
		notdone();// TODO fix
		return false;
	}

	public void saveAtomRemovedIons(int atomID, ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		notdone();// TODO fix
		
	}

	public int saveMap(String name, Vector<int[]> mapRanges) {
		notdone();// TODO fix
		return 0;
	}

	public void seedRandom(int seed) {
		notdone();// TODO fix
		
	}

	public boolean setCollectionDescription(Collection collection, String description) {
		notdone();// TODO fix
		return false;
	}

	public void syncWithIonsInDB(ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		notdone();// TODO fix
		
	}

	public void updateAncestors(Collection collection) {
		notdone();// TODO fix
		
	}

	public void updateInternalAtomOrder(Collection collection) {
		notdone();// TODO fix
		
	}

	public Vector<Vector<Object>> updateParticleTable(Collection collection, Vector<Vector<Object>> particleTable, int lowAtomID, int hightAtomID) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<String> getKnownDatatypes() {
		notdone();// TODO fix
		return null;
	}

	public int getRepresentedCluster(int atomID) {
		notdone();// TODO fix
		return 0;
	}

	public String getVersion() {
		notdone();// TODO fix
		return null;
	}

	public boolean containsDatatype(String type) {
		notdone();// TODO fix
		return false;
	}

	public String getATOFMSFileName(int atomID) {
		notdone();// TODO fix
		return null;
	}

	public String getAtomDatatype(int atomID) {
		notdone();// TODO fix
		return null;
	}

	public ArrayList<Integer> getImmediateSubCollections(ArrayList<Integer> collections) {
		notdone();// TODO fix
		return null;
	}

	public boolean removeEmptyCollection(Collection collection) {
		notdone();// TODO fix
		return false;
	}
}
