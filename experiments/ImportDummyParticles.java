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
	
	// global variables for efficiency:
	String[] array;
	int count;
	SimpleDateFormat dateFormat;
	GregorianCalendar arrayTime;
	int arrayRand;
	StringBuilder collInfo;
	int time;
	String[] timeArray;
	StringBuilder particleInfo;
	ArrayList<Double> locations;
	double rand;

	
	
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
			import2MillionParticles();
			
			stmt.close();
		}catch (Exception exception) {
			System.out.println("Caught exception");
			exception.printStackTrace();
		}
	}
	
	public void import200000particles() throws SQLException {
		String[] array;
		System.out.println("IMPORTING ~200,000 PARTICLES ");
		System.out.println();
		System.out.println("Collection 1: 10,000");
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);
		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Parent','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES (0,"+newCollectionID+")");
		int parent = newCollectionID;
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);
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
	System.out.println("Collection 5: 50,000");
		newCollectionID++;
		newDatasetID = newCollectionID;
		collectionIDs.add(newCollectionID);

		stmt.addBatch("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection5','','','ATOFMS')");
		stmt.addBatch("INSERT INTO CollectionRelationships VALUES ("+parent+","+newCollectionID+")");
		stmt.addBatch("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset5','mass.cal','size.noz',10,20,0.01,1)");
		array = generateTimeArray(50000);
		for (int i = 1; i <= 50000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+array[i-1]+"',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+random.nextDouble()*100+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")");
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
		array = generateTimeArray(60000);
		for (int i = 1; i <= 60000; i++) {
			stmt.addBatch("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+array[i-1]+"',0.005,1.5,20,'Atom"+newAtomID+"')");
			stmt.addBatch("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+random.nextDouble()*100+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")");
			newAtomID++;
		}
		System.out.println("     executing batch...");
		stmt.executeBatch();

		//		 update InternalAtomOrderTable;
		System.out.println("updating InternalAtomOrder table...");
		for (int i = 0; i < collectionIDs.size(); i++) 
			db.updateInternalAtomOrder(db.getCollection(collectionIDs.get(i)));
	}
	
	public String[] generateTimeArray(int length) {
		array = new String[length];
		count = 0;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		arrayTime = new GregorianCalendar(2004,8,4,10,0,0);
		while (count < length) {
			arrayTime.add(Calendar.SECOND,1);
			arrayRand = (int) (random.nextInt(500));
			if (count+arrayRand > length)
				arrayRand = length - count;
			for (int i = 0; i < arrayRand; i++) {
				array[count] = dateFormat.format(arrayTime.getTime());
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
		collInfo = new StringBuilder();
		collInfo.append("INSERT INTO Collections VALUES ("+newCollectionID+",'Collection1','','','ATOFMS')\n");
		collInfo.append("INSERT INTO CollectionRelationships VALUES (0,"+newCollectionID+")\n");
		collInfo.append("INSERT INTO ATOFMSDataSetInfo VALUES ("+newDatasetID+",'Dataset1','mass.cal','size.noz',10,20,0.01,1)\n");
		System.out.println("     executing batch pertaining to collection info...");
		stmt.execute(collInfo.toString());
		time = 0;
		timeArray = generateTimeArray(50000);
		particleInfo = new StringBuilder();
		
		System.out.println("     Beginning to insert particles...");
		for (int i = 1; i <= 2000000; i++) {
			if (time == 49999) {
				timeArray = generateTimeArray(50000);
				time = 0;
			}
			locations = new ArrayList<Double>();
			while (locations.size() != 20) {
				rand = random.nextDouble();
				if (!locations.contains(new Double(rand)));
					locations.add(rand);
			}
			stmt.execute("INSERT INTO DataSetMembers VALUES ("+newDatasetID+","+newAtomID+")\n");
			stmt.execute("INSERT INTO AtomMembership VALUES ("+newCollectionID+","+newAtomID+")\n");
			stmt.execute("INSERT INTO ATOFMSAtomInfoDense VALUES ("+newAtomID+",'"+timeArray[time]+"',0.005,1.5,20,'Atom"+newAtomID+"')\n");
			stmt.execute("INSERT INTO InternalAtomOrder VALUES ("+newAtomID+","+newCollectionID+","+i+")\n");
			for (int j = 0; j < 20; j++) 
				stmt.execute("INSERT INTO ATOFMSAtomInfoSparse VALUES ("+newAtomID+","+locations.get(j)+","+random.nextInt(25)+",0.05,"+random.nextInt(25)+")\n");
			if (i%100==0 && i>=100) 
				System.out.println("     finished executing query for particle # "+i);

			newAtomID++;
			time++;
		}
	}
		
	public static void main(String[] args) {
		new ImportDummyParticles();
		System.out.println("done.");
	}

}
