package testing;

import gui.FilePicker;

import java.io.File;
import java.util.*;

import analysis.clustering.*;

import dataImporters.ATOFMSBatchTableModel;
import dataImporters.ATOFMSDataSetImporter;
import database.SQLServerDatabase;
import junit.framework.TestCase;

public class LotsOfDataTest extends TestCase {
	ATOFMSBatchTableModel tab;
	SQLServerDatabase db;
	int collectionID;
	
	protected void setUp() throws Exception {
//		SQLServerDatabase.rebuildDatabase("SpASMSdb");
		db = new SQLServerDatabase();
		db.openConnection();
//		
//		collectionID = db.createEmptyCollection("ATOFMS", 0,"the big one","sooo much data","");
//	
//		FilePicker fpick = new FilePicker("Choose a dataset list to import",
//				"csv", null);
//		if (fpick.getFileName() == null) {
//			// they chose to cancel.
//			fail();
//		}
//
//		tab = new ATOFMSBatchTableModel(new File(fpick.getFileName()));
//		tab.setAutocal(true);
//		
//		ATOFMSDataSetImporter dsi = new ATOFMSDataSetImporter(tab, null);
//		dsi.setParentID(collectionID);
//
//		System.out.print("Started importing data at ");
//		System.out.println(new Date());
//		dsi.checkNullRows();
//		dsi.collectTableInfo(); // imports the data
//		System.out.print("Finished importing data at ");
//		System.out.println(new Date());
	}
	
	public void testClustering() {
		ArrayList<String> valuecol = new ArrayList<String>();
		valuecol.add("ATOFMSAtomInfoSparse.PeakArea");
	
		collectionID = 2;
		
		ClusterInformation cInfo = new ClusterInformation(valuecol,
				"[PeakLocation]", null, false, true);
		
		KMedians kmed = new KMedians(collectionID, db, 10, "", "comment",
				false, cInfo);
		kmed.setCursorType(Cluster.DISK_BASED);
		
		System.out.print("*********Started clustering data at ");
		System.out.println(new Date());
		
		kmed.divide();

		System.out.print("*********Finished clustering data at ");
		System.out.println(new Date());
	}

	protected void tearDown() throws Exception {
		db.closeConnection();
	}

}
