package analysis.dataCompression;

import java.util.ArrayList;

import analysis.DistanceMetric;

import collection.Collection;
import database.CollectionCursor;
import database.DynamicTable;
import database.InfoWarehouse;


/**
 * CompressData will take large collections and compress them into smaller 
 * collections.  Currently, it will do this with BIRCH pre-processing.
 * It will contain dataset names, number of particles in each "atom," and 
 * the binnedpeaklists.
 * @author ritza
 *
 */
public abstract class CompressData {

	public static final int DISK_BASED = 0;
	public static final int STORE_ON_FIRST_PASS = 1;

	protected Collection oldCollection;
	protected Collection newCollection;
	protected String oldDatatype;
	protected String newDatatype;
	protected InfoWarehouse db;
	protected boolean isNormalized;
	protected DistanceMetric distanceMetric;
	protected ArrayList<ArrayList<Integer>> generalizedCompression;
	private String name, comment;
	
	/**
	 * The CollectionCursor used to access the atoms of the 
	 * collection you are dividing.  Initialize this to one of the
	 * implementations using a get method from InfoWarehouse
	 */
	protected CollectionCursor curs = null;
	
	public CompressData(Collection c, InfoWarehouse database, String name, String comment, boolean n, DistanceMetric d) {
		db = database;
		oldCollection = c;
		oldDatatype = c.getDatatype();
		newDatatype = "Compressed" + oldDatatype;
		isNormalized = n;
		distanceMetric = d;
		this.name = name;
		this.comment = comment;
		setDatatype();
	}
	
	/**
	 * Creates the compressed datatype in the db if it doesn't exist.
	 *
	 */
	public void setDatatype() {
		db.addCompressedData(newDatatype,oldDatatype);
	}
	
	/**
	 * actual data compression.  Overwritten in subclasses.
	 *
	 */
	public abstract void compress();
	
	/**
	 * Gets the compressed data into the format for putting in db.
	 */
	public abstract void generalizeCompressedData();
	
	private void putCollectionInDB() {
		assert (generalizedCompression != null) : "haven't initialized gen. data!";
		
		// create array of booleans saying whether we can aggregate or not
		// for dense and sparse atomInfo.
		ArrayList<ArrayList<String>> tempTypes = db.getColNamesAndTypes(newDatatype,DynamicTable.AtomInfoDense);
		boolean[] dInfo = new boolean[tempTypes.size()];
		for (int i = 0; i < dInfo.length; i++)
			dInfo[i] = canAggregate(tempTypes.get(i).get(1));
		
		tempTypes = db.getColNamesAndTypes(newDatatype,DynamicTable.AtomInfoSparse);
		boolean[] sInfo = new boolean[tempTypes.size()];
		for (int i = 0; i < sInfo.length; i++)
			sInfo[i] = canAggregate(tempTypes.get(i).get(1));
		
		tempTypes.clear();
		
		// Create new collection and dataset:
		int[] IDs = db.createEmptyCollectionAndDataset(newDatatype,0,name,comment,params); 
		int newCollectionID = IDs[0];
		int newDatasetID = IDs[1];
		
		// insert each CF as a new atom.
		int atomID = db.getNextID();
		for (int i = 0; i < generalizedCompression.size(); i++) {
		
		}
	}
	
	public boolean canAggregate(String string) {
		if (string.equals("INT") || string.equals("REAL"))
			return true;
		return false;
	}
	
	private void printDescriptionToDB() {
		
	}
}
