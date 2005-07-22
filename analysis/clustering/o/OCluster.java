package analysis.clustering.o;


import analysis.CollectionDivider;
import database.InfoWarehouse;

public class OCluster extends CollectionDivider
{
	protected RootPartition root;
	
	
	public OCluster(int cID, InfoWarehouse database, String name, String comment) {
		super(cID, database, name, comment);
		
		root = new RootPartition(this);
		db = database;
	}

	@Override
	public int divide() {
		// TODO: copy stuff from Cluster-y things with curs.next an' shit
		return 0;
	}
	
	public boolean setCursorType(int type) 
	{
		switch (type) {
		case CollectionDivider.DISK_BASED :
			curs = db.getBinnedCursor(collection);
			return true;
		case CollectionDivider.STORE_ON_FIRST_PASS : 
			return false;
		default :
			return false;
		}
	}


}