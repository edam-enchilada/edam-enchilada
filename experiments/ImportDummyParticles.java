/**
 * ImportDummyParticles is a FAST way of importing lots of data. Used to 
 * test new optimized particle table, etc.  Talks directly to the database
 * to make this fast - this is the only class other than SQLServerDatabase 
 * to do so.
 * 
 */
package experiments;

import java.util.ArrayList;
import java.sql.*;

import ATOFMS.ATOFMSParticle;
import ATOFMS.CalInfo;
import ATOFMS.PeakParams;
import database.SQLServerDatabase;

public class ImportDummyParticles {
	private ArrayList<Integer> collectionIDs;
	private SQLServerDatabase db;
	private int counter = 0;
	private int superCounter = 0;
	
	public ImportDummyParticles() {
				
		//Open database connection:
		db = new SQLServerDatabase();
		db.openConnection();
				
		try {
			int[] id = new int[2];
			int newAtomID = db.getNextID();
			ReadExpSpec readSpec = new ReadExpSpec("Particles for Clustering\\a-020801071636-00055.amz"); 
			String denseString = readSpec.getParticle().particleInfoDenseString();
			ArrayList<String> sparseString = readSpec.getParticle().particleInfoSparseString();
			
			id = emptyCollection(0);
			int parent = id[0];
			System.out.println("Collection 1: 10,000");
			id = emptyCollection(parent);
			for (int i = 0; i < 10000; i++) {
				if (i%100==0 && i>=100)
					System.out.println(superCounter+=100);
				db.insertParticle(denseString, sparseString, db.getCollection(id[0]),id[1],newAtomID++);
			}
			System.out.println("Collection 2: 20,000");
			int p = id[0];
			id = emptyCollection(p);
			for (int i = 0; i < 20000; i++) {
				if (i%100==0 && i>=100)
					System.out.println(superCounter+=100);
				db.insertParticle(denseString, sparseString, db.getCollection(id[0]),id[1],newAtomID++);
			}
			System.out.println("Collection 3: 30,000");
			id = emptyCollection(p);
			for (int i = 0; i < 30000; i++) {
				if (i%100==0 && i>=100)
					System.out.println(superCounter+=100);
				db.insertParticle(denseString, sparseString, db.getCollection(id[0]),id[1],newAtomID++);
			}
			System.out.println("Collection 4: 40,000");
			id = emptyCollection(parent);
			for (int i = 0; i < 40000; i++) {
				if (i%100==0 && i>=100)
					System.out.println(superCounter+=100);
				db.insertParticle(denseString, sparseString, db.getCollection(id[0]),id[1],newAtomID++);
			}
			System.out.println("Collection 5: 50,000");
			id = emptyCollection(id[0]);
			for (int i = 0; i < 50000; i++) {
				if (i%100==0 && i>=100)
					System.out.println(superCounter+=100);
				db.insertParticle(denseString, sparseString, db.getCollection(id[0]),id[1],newAtomID++);
			}
			// Collection 6
			System.out.println("Collection 6: 60,000");
			id = emptyCollection(parent);
			for (int i = 0; i < 60000; i++) {
				if (i%100==0 && i>=100)
					System.out.println(superCounter+=100);
				db.insertParticle(denseString, sparseString, db.getCollection(id[0]),id[1],newAtomID++);
			}
		}catch (Exception exception) {
			System.out.println("Caught exception");
			exception.printStackTrace();
		}
	}
	
	private int[] emptyCollection(int parent) {
		counter++;
		return db.createEmptyCollectionAndDataset("ATOFMS", parent,"Dummy","Dummy",
				"'Particles for Clustering\\040215a_33.cal', '.noz file', " +
				ATOFMSParticle.currPeakParams.minHeight + ", " + 
				ATOFMSParticle.currPeakParams.minArea + ", " + 
				ATOFMSParticle.currPeakParams.minRelArea + ", 1"); 
	}
	
	public static void main(String[] args) {
		new ImportDummyParticles();
		System.out.println("done.");
	}

}
