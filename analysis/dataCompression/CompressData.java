
package analysis.dataCompression;

import java.util.ArrayList;

import analysis.CollectionDivider;
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
public abstract class CompressData {//extends CollectionDivider {

	public static final int DISK_BASED = 0;
	public static final int STORE_ON_FIRST_PASS = 1;

	protected Collection oldCollection;
	protected Collection newCollection;
	protected String oldDatatype;
	protected String newDatatype;
	protected boolean isNormalized;
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
	
	public CompressData(Collection c, InfoWarehouse database, String name, String comment, boolean n, DistanceMetric d) {
		//super()
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
		System.out.println("setting datatype");
		db.addCompressedDatatype(newDatatype,oldDatatype);
	}
	
	/**
	 * actual data compression.  Overwritten in subclasses.
	 *
	 */
	public abstract void compress();
	
	protected abstract void putCollectionInDB(); 
	
	public boolean canAggregate(String string) {
		if (string.equals("INT") || string.equals("REAL"))
			return true;
		return false;
	}
	
	public String getDatasetParams(String datatype) {
		// get number of params:
		ArrayList<ArrayList<String>> namesAndTypes = db.getColNamesAndTypes(
				datatype, DynamicTable.DataSetInfo);
		int num = namesAndTypes.size();
		if (num <= 2)
			return "";
		String str = "";
		
		for (int i = 3; i <= num; i++) {
				if (namesAndTypes.get(num).get(1).equals("INT") ||
						namesAndTypes.get(num).get(1).equals("REAL"))
					str = "0, ";
				else
					str = "'Compressed', ";
			}
		return str.substring(0,str.length()-2);
	}
	
	private void printDescriptionToDB() {
		
	}
}
