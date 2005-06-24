package analysis.clustering.o;



import java.util.ArrayList;

import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import database.InfoWarehouse;

public class OCluster extends CollectionDivider {
	Partition root;
	InfoWarehouse db;
	
	public OCluster(int cID, InfoWarehouse database, String name, String comment) {
		super(cID, database, name, comment);
		
		root = new UndeterminedPartition(null);
		db = database;
	}

	//@Override
	public boolean setCursorType(int type) {
		// TODO Auto-generated method stub
		return false;
	}

	//@Override
	public int divide() {
		// TODO Auto-generated method stub
		root.split(new ArrayList<BinnedPeakList>());
		return 0;
	}
	
}