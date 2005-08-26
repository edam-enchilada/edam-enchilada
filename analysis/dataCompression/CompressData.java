package analysis.dataCompression;

import java.util.ArrayList;

import collection.Collection;
import database.CollectionCursor;
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


	/**
	 * The CollectionCursor used to access the atoms of the 
	 * collection you are dividing.  Initialize this to one of the
	 * implementations using a get method from InfoWarehouse
	 */
	protected CollectionCursor curs = null;
	
	public CompressData(Collection c, InfoWarehouse database) {
		db = database;
		oldCollection = c;
		oldDatatype = c.getDatatype();
		newDatatype = "Compressed" + oldDatatype;		
		
	}
	
	public boolean setDatatype() {		
		return false;
	}
	
	public void compress() {
		
	}
	
	private void writeCollectionToDB() {
		
	}
}
