/**
 * ImportDummyParticles is a FAST way of importing lots of data. Used to 
 * test new optimized particle table, etc.  Talks directly to the database
 * to make this fast - this is the only class other than SQLServerDatabase 
 * to do so.
 * 
 */
package experiments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import java.sql.*;

import database.SQLServerDatabase;

public class ImportDummyParticles {
	private ArrayList<Integer> collectionIDs;
	private SQLServerDatabase db;
	private int counter = 0;
	private Statement stmt;
	private int newCollectionID, newDatasetID;
	private int newAtomID;
	private Random random;
	
	public ImportDummyParticles() {
		collectionIDs = new ArrayList<Integer>();
		random = new Random(12345678);
		//Open database connection:
		db = new SQLServerDatabase();
		db.openConnection();
		try {
			stmt = db.getCon().createStatement();
			newAtomID = db.getNextID();
			
			ResultSet rs = stmt.executeQuery("SELECT MAX(CollectionID) FROM Collections");
			rs.next();
			newCollectionID = rs.getInt(1)+1;
			rs.close();
			
			/***TODO Swap the method here to change importing particles:***/
			import100000particles();
			
			stmt.close();
			// update InternalAtomOrderTable;
			System.out.println("updating InternalAtomOrder table...");
			for (int i = 0; i < collectionIDs.size(); i++) 
				db.updateInternalAtomOrder(db.getCollection(collectionIDs.get(i)));
		}catch (Exception exception) {
			System.out.println("Caught exception");
			exception.printStackTrace();
		}
	}
	
	public void import100000particles() throws SQLException {
		String[] array;
		System.out.println("IMPORTING ~100,000 PARTICLES ");
		System.out.println();
		System.out.println("Collection 1: 10,000");
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);
		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Parent','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES (0,"+newCollectionID+")");
		int parent = newCollectionID;
		newCollectionID++;
		newDatasetID = newCollectionID;
		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection1','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset1','mass.cal','size.noz',10,20,0.01,1)");
		array = generateTimeArray(10000);
		for (int i = 1; i <= 10000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+array[i-1]+"',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+random.nextDouble()*100+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")");
			newAtomID++;
		}
		System.out.println("     executing batch...");
		stmt.executeBatch();
		System.out.println("Collection 2: 20,000");
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);

		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection2','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset2','mass.cal','size.noz',10,20,0.01,1)");
		array = generateTimeArray(20000);
		for (int i = 1; i <= 20000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+array[i-1]+"',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+random.nextDouble()*100+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")");
			newAtomID++;
		}
		System.out.println("     executing batch...");
		stmt.executeBatch();
		System.out.println("Collection 3: 30,000");
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);

		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection3','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset3','mass.cal','size.noz',10,20,0.01,1)");
		array = generateTimeArray(30000);
		for (int i = 1; i <= 30000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+array[i-1]+"',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+random.nextDouble()*100+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")");
			newAtomID++;
		}		
		System.out.println("     executing batch...");
		stmt.executeBatch();
		System.out.println("Collection 4: 40,000");
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);

		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection4','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset4','mass.cal','size.noz',10,20,0.01,1)");
		array = generateTimeArray(40000);
		for (int i = 1; i <= 40000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+array[i-1]+"',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+random.nextDouble()*100+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")");
			newAtomID++;
		}
		System.out.println("     executing batch...");
		stmt.executeBatch();
	/*System.out.println("Collection 5: 50,000");
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);

		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection5','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset5','mass.cal','size.noz',10,20,0.01,1)");
		for (int i = 1; i <= 50000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+",100.0,10,0.05,10)");
			newAtomID++;
		}
		System.out.println("     executing batch...");
		stmt.executeBatch();

		System.out.println("Collection 6: 60,000");
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);

		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection6','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset6','mass.cal','size.noz',10,20,0.01,1)");
		for (int i = 1; i <= 60000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+",100.0,10,0.05,10)");
			newAtomID++;
		}
		System.out.println("     executing batch...");
		stmt.executeBatch();*/
	}
	
	public String[] generateTimeArray(int length) {
		String[] array = new String[length];
		int count = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		GregorianCalendar time = new GregorianCalendar(2005,12,1,8,0,0);
		while (count < length) {
			time.add(Calendar.SECOND,1);
			int rand = (int) (random.nextInt(500));
			if (count+rand > length)
				rand = length - count;
			for (int i = 0; i < rand; i++) {
				array[count] = dateFormat.format(time.getTime());
				count++;
			}
		}
		return array;
	}
	
	// 1 parent collection, 20 peaks per particle
	public void import2MillionParticles() throws SQLException {
		System.out.println("IMPORTING 2 MILLION PARTICLES ");
		System.out.println();
		System.out.println("Collection 1: 2,000,000");
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);
		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection1','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES (0,"+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset1','mass.cal','size.noz',10,20,0.01,1)");
		System.out.println("     executing batch pertaining to collection info...");
		stmt.executeBatch();
		for (int i = 1; i <= 2000000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",100.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",101.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",102.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",103.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",104.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",105.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",106.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",107.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",108.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",109.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",110.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",111.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",112.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",113.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",114.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",115.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",116.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",117.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",118.0,10,0.05,10)");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID + ",119.0,10,0.05,10)");
			if (i%10000==0 && i>=10000)
				System.out.println("executing batch for particle # "+i);
			stmt.executeBatch();
			newAtomID++;
		}
	}
		
	public static void main(String[] args) {
		new ImportDummyParticles();
		System.out.println("done.");
	}

}
