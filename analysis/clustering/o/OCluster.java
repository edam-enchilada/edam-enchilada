package analysis.clustering.o;



import java.util.ArrayList;
import java.util.List;

import analysis.BinnedPeakList;
import analysis.CollectionDivider;
import database.InfoWarehouse;

public class OCluster extends CollectionDivider //implements Partition {
{
	Partition root;
	InfoWarehouse db;
	
	public OCluster(int cID, InfoWarehouse database, String name, String comment) {
		super(cID, database, name, comment);
		
		root = new UndeterminedPartition(this);
		root.setCollectionSource(this);
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

		
		//while (false) {
			//this.split(null);
		//}
		return 0;
	}

	public CollectionDivider getCollectionSource() {
		return this;
	}

	public void setCollectionSource(CollectionDivider collectionSource) {
		return;
	}

	public Partition getParent() {
		return null;
	}

	public Partition getLeftChild() {
		return root;
	}

	public Partition getRightChild() {
		return null;
	}

	public int split(List<BinnedPeakList> atoms) {
		return root.split(atoms);
	}

	public String rulesUp() {
		return "";
	}

	public String rulesDown() {
		// TODO Auto-generated method stub
		return null;
	}
	
}