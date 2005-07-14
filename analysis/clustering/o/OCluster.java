package analysis.clustering.o;


import analysis.CollectionDivider;
import database.InfoWarehouse;

public class OCluster extends CollectionDivider
{
	RootPartition root;
	InfoWarehouse db;
	
	public OCluster(int cID, InfoWarehouse database, String name, String comment) {
		super(cID, database, name, comment);
		
		root = new RootPartition(this);
		db = database;
	}

	//@Override
	public boolean setCursorType(int type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int divide() {
		// so where are all these nifty little atoms stored, anyway?
		return 0;
	}


}