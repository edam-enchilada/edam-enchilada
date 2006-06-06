
package analysis.dataCompression;

import java.util.ArrayList;

import analysis.DistanceMetric;

import collection.Collection;
import database.CollectionCursor;
import database.DynamicTable;
import database.InfoWarehouse;


/**
 * CompressData is an abstract class.  Classes extending this one will take 
 * large collections and compress them into smaller 
 * collections.  Currently, it will do this with BIRCH pre-processing.
 * It will contain dataset names, number of particles in each "atom," and 
 * the binnedpeaklists.
 * @author ritza
 *
 */
public abstract class CompressData {

	public static final int DISK_BASED = 0;
	public static final int STORE_ON_FIRST_PASS = 1;

	protected Collection oldCollection; // collection to compress
	protected Collection newCollection; // compressed collection
	protected String oldDatatype; 
	protected String newDatatype;
	
	protected DistanceMetric distanceMetric;
	
	protected String name;
	protected String comment;
	
	protected InfoWarehouse db;
	
	/**
	 * The CollectionCursor used to access the atoms of the 
	 * collection you are dividing.  Initialize this to one of the
	 * implementations using a get method from InfoWarehouse
	 */
	protected CollectionCursor curs = null;
	
	public CompressData(Collection c, InfoWarehouse database, String name, String comment, DistanceMetric d) {
		db = database;
		oldCollection = c;
		oldDatatype = c.getDatatype();
		newDatatype = "Compressed" + oldDatatype;
		distanceMetric = d;
		this.name = name;
		this.comment = comment;
		// set the datatype in the db if it is not already there.
		setDatatype();
	}
	
	/**
	 * Creates the compressed datatype in the db if it doesn't exist.
	 *
	 */
	public void setDatatype() {
		System.out.println("setting datatype");
		db.addCompressedDatatype(newDatatype,oldDatatype);
	}
	
	/**
	 * actual data compression.  Overwritten in subclasses.
	 */
	public abstract void compress();
	
	/**
	 * Method to write compressed collection to DB.  Overwritten in subclases.
	 */
	protected abstract void putCollectionInDB(); 
	
	/**
	 * Gets the dataset parameters for the given datatype; used when putting
	 * the collection in the database.
	 * @param datatype
	 * @return
	 */
	public String getDatasetParams(String datatype) {
		// get number of params:
		ArrayList<ArrayList<String>> namesAndTypes = db.getColNamesAndTypes(
				datatype, DynamicTable.DataSetInfo);
		int num = namesAndTypes.size();
		if (num <= 2)
			return "";
		String str = "";
		for (int i = 3; i < num; i++) {	
			if (namesAndTypes.get(i).get(1).equals("INT") ||
						namesAndTypes.get(i).get(1).equals("REAL"))
					str += "0, ";
				else
					str += "'Compressed', ";
			}
		return str.substring(0,str.length()-2);
	}	
}