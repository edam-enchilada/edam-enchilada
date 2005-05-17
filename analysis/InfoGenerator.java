package analysis;

import database.CollectionCursor;
import database.InfoWarehouse;

/**
 * Creates a subcollection of the current collection that is 
 * identical to the current collection, but contains different text 
 * in the description field.  Can be used for outputting text info on
 * the current collection.  
 * 
 * @author andersbe
 */
public abstract class InfoGenerator 
{
	/**
	 * The id of the collection you are processing
	 */
	protected int collectionID;

	private String name;
	private String comment;
	
	/**
	 * A pointer to an active InfoWarehouse
	 */
	protected InfoWarehouse db;
	
	protected CollectionCursor curs = null;
	
	/**
	 * Construct a CollectionDivider.
	 * @param cID		The id of the collection to be divided
	 * @param database	The open InfoWarehouse to write to
	 * @param name		A name for the new host collection
	 * @param comment	A comment for the collection
	 */
	public InfoGenerator(int cID, InfoWarehouse database, String name, 
			String comment)
	{
	    if (database == null)
	        throw new IllegalArgumentException(
	                "Parameter 'database' should not be null");
	    
		collectionID = cID;
		this.name = name;
		this.comment = comment;
		db = database;
		if (db.getCollectionSize(cID) < 500000)
			curs = db.getMemoryBinnedCursor(cID);
		else
			curs = db.getBinnedCursor(cID);
		
	}

	public void generateInfo()
	{
		int newHostID = db.createEmptyCollection(collectionID, name, 
				comment,generateNewDescription());
		curs.reset();
		while (curs.next())
		{
			db.addAtom(curs.getCurrent().getID(),newHostID);
		}
		curs.reset();
	}
	
	abstract protected String generateNewDescription();
}
