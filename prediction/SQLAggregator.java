package prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

import database.Database;
import database.InfoWarehouse;

/*
 * Note: St.Louis min & max m/z values are (respectively) -1585 and 1691, so we
 * could boost by 1700 on attribute numbering and include all peaks.
 * Query run on 12.5.06 by steinbel
 *	 
 */

public class SQLAggregator {
	
	// Set maxAtomId to 0 to run the whole thing
	public static final int maxAtomId = 0;

	private InfoWarehouse db;
	private Connection con;

	private int maxAtt = 603; //the maximum number of attributes
	private int offset = 303; //the offset between the peak location and its
								//attribute number
	private double density = 1; //this will be given us by the atmo. scientists
	
	public SQLAggregator(){
		db = Database.getDatabase();
	}
	
	/**
	 * @author steinbel
	 * @author dmusican
	 * Gathers and formats the ATOFMS data appropriately.
	 * @param outputFile - name of the output file
	 */
	public void process(PrintWriter out) throws IOException{
		//send incoming string to create a temp table
		createTempTable();

		System.out.println("table created");//TESTING

		try {
			// Calculate total mass for each time bin, and connect it with
			// EC data. Throw away any bins with too few particles
			String denseQuery =
				"SELECT roundedTime, mass, value FROM\n" +
				"(SELECT roundedTime, COUNT(*) as cnt,\n" +
				"	SUM(size*size*size*" + density + "*(1/de)) as mass\n" +
				"   FROM RoundedDense\n";
			if (maxAtomId > 0)
				denseQuery += " WHERE atomID <= " + maxAtomId + "\n"; 
			denseQuery +=
				"   GROUP BY roundedTime\n" +
				"   HAVING COUNT(*) > 200) Masses, AggData\n" +
				"WHERE Masses.roundedTime = AggData.Timestamp\n" +
				"AND value <> 0\n" +
				"ORDER BY roundedTime";

			System.out.println(denseQuery);
			Statement denseStmt = con.createStatement();
			ResultSet denseSet = denseStmt.executeQuery(denseQuery); 

			/* Sum together the sizes of the particles and the areas of the
			 * peaks, both adjusted for detection efficiency. 
			 */
			String sparseQuery =
				"SELECT roundedTime, peaklocation,\n" +
				"	SUM(peakarea/de) as adjustedpeak\n" +
				"FROM RoundedDense d, ATOFMSAtomInfosparse s\n" +
				"WHERE d.atomid = s.atomid\n";
			if (maxAtomId > 0)
				sparseQuery += "AND d.atomID <= " + maxAtomId + "\n"; 
			sparseQuery +=
				"GROUP BY roundedTime, peaklocation\n" +
				"ORDER BY roundedTime, peaklocation";
			System.out.println(sparseQuery);
			Statement sparseStmt = con.createStatement();
			ResultSet sparseSet = sparseStmt.executeQuery(sparseQuery);

			boolean denseRead = denseSet.next();
			boolean sparseRead = sparseSet.next();
			boolean newMass = true;

			if (!denseRead)
				throw new RuntimeException("Dense set empty");
			if (!sparseRead)
				throw new RuntimeException("Sparse set empty");
			
			while (denseRead) {
				Timestamp denseTime = denseSet.getTimestamp("roundedTime");
				Timestamp sparseTime = sparseSet.getTimestamp("roundedTime");

				// If denseTime is greater than sparseTime, we may have thrown
				// away that dense row because mass was 0. Skim through sparse
				// time until we match
				while (denseTime.compareTo(sparseTime) > 0) {
					sparseRead = sparseSet.next();
					if (sparseRead) {
						sparseTime = sparseSet.getTimestamp("roundedTime");
					} else {
						throw new RuntimeException("No more sparse data");
					}
				}
				
				if (!denseTime.equals(sparseTime)) {
					// Should never happen, this is error checking
					throw new RuntimeException("Dense time does not equal sparse time");
				}
				
				// Only write out the row of the value is nonzero. If the mass
				// is zero, this corresponds to missing data.
				
				float value = denseSet.getFloat("value");
				
				// Starting new dense, write out the header. Chop off the ".0"
				// at the end of the time that seems to give Weka trouble.
				String wekaTime = (denseTime.toString().split("\\."))[0];

				// Adjust the units of mass to put it on a smaller scale:
				// this makes tools such as Weka happier.
				float massScaleFactor = 1e8f;
				out.print("{" + "0 " + '"' + wekaTime + '"' +
						",1 " + denseSet.getFloat("value") +
						",2 " + denseSet.getFloat("mass")/massScaleFactor);

				// Loop over all sparse data with matching time
				while (sparseRead && denseTime.equals(sparseTime)) {

					// Write out the sparse data. Adjust the units
					// of adjustpeak to put it on a smaller scale:
					// this makes tools such as Weka happier.
					float peakScaleFactor = 1e8f;
					int location = sparseSet.getInt("peaklocation");
					if ((location >= -300) && (location <= 300)){
						out.print("," + (location+303) + " " +
								sparseSet.getFloat("adjustedpeak") /
								peakScaleFactor);
					}

					// Grab next sparse row
					sparseRead = sparseSet.next();
					if (sparseRead) {
						sparseTime = sparseSet.getTimestamp("roundedTime");
					}

				}

				// Write out end of row
				out.println("}");

				denseRead = denseSet.next();
			}
			denseSet.close();
			sparseSet.close();
			denseStmt.close();
			sparseStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//drop temp table
		//dropTempTable();
		
	}
	
	/**
	 * @author steinbel
	 * Opens the connection to the database.
	 */
	public void open(){
		db.openConnection();
		con = db.getCon();
	}
	
	/**
	 * @author steinbel
	 * Closes the connection to the database.
	 */
	public void close(){
		db.closeConnection();
	}
	
	/**
	 * @author steinbel
	 * @author dmusican
	 * Creates a temporary table with all atomIDs of interest, and calculates
	 * necessary detection efficiency.
	 */
	private void createTempTable(){
		try {
			Statement stmt = con.createStatement();
			/* Create a temp table with atomID and detection efficiency 
			 * (initially populated with the size of the particles) during the
			 * hour-long timebin around the given time.
			 */
			dropTempTable();

			System.out.println("Creating temp table.");
			String order = 
				"CREATE TABLE RoundedDense (\n" +
				"	atomid int,\n" +
				"	roundedTime DATETIME,\n" +
				"	size REAL,\n" +
				"	de REAL)\n" +
				"INSERT INTO RoundedDense (atomid, roundedTime, size, de)\n" +
				"SELECT atomid,\n" +
				//"	DATEADD(hour, DATEDIFF(hour, '20000101', DATEADD(minute, 30, time)), '20000101') as roundedTime,\n" +
				"	DATEADD(hour, DATEDIFF(hour, '20000101', time), '20000101') as roundedTime,\n" +
				//"	DATEADD(hour, DATEDIFF(hour, '20000101', DATEADD(minute, 60, time)), '20000101') as roundedTime,\n" +
				"	size, NULL\n" +
				"FROM ATOFMSAtomInfoDense\n" +
				"WHERE size >= 0.1 AND size <= 2.5\n";
			if (maxAtomId > 0)
				order += "AND atomID <= " + maxAtomId; 
			
			System.out.println(order);//TESTING
			stmt.executeUpdate(order);

			//calculate the Detection Efficiency for the three size bins
			//as described in DetectionEfficiency_Gomit.pdf
			System.out.println("Update 1");
			stmt.executeUpdate("update RoundedDense " +
					"set de = (select power(size*1000, 2.8574)*exp(-27.16)) " +
					"where size >= .1 and size <= .75;");
			System.out.println("Update 2");
			stmt.executeUpdate("update RoundedDense " +
					"set de = (select power(size*1000, -.58272)*exp(-4.803)) " +
					"where size > .75 and size < 1.;");
			System.out.println("Update 3");
			stmt.executeUpdate("update RoundedDense " +
					"set de = (select power(size*1000, -7.52)*exp(42.031)) " +
					"where size >= 1. and size <= 2.5;");
			
			// Verify that all de rows have been calculated
			ResultSet rs = 
				stmt.executeQuery("SELECT * FROM RoundedDense WHERE de IS NULL");
			if (rs.next())
				throw new RuntimeException("de not calculated for all values");
			rs.close();
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @author steinbel
	 * Drops the temporary table "#hourbin" from the database SpASMSdb.
	 */
	private void dropTempTable(){
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("IF (OBJECT_ID('RoundedDense') " +
					"IS NOT NULL)\n" +
					" DROP TABLE RoundedDense\n");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @author steinbel
	 * @author dmusican
	 * Writes the .arff header info for this file, which includes naming
	 * the m/z values as attributes so we can find them later. 
	 * @param relationName - the name of the relation
	 * @param predictThis - the attribute to be predicted (e.g., "ec" or "bc")
	 * @return the header for the .arff file in the form of a string
	 */
	public String assembleAttributes(String relationName, String predictThis){
		String attributeNames = "@relation " + relationName +"\n"
				+"@attribute time date \"yyyy-MM-dd HH:mm:ss\" \n"
				+"@attribute " + predictThis + " numeric \n"
				+"@attribute mass numeric \n";
		
		//start at 3 because of the attributes named above
		for (int i=3; i<=maxAtt; i++){
			attributeNames+="@attribute mz" + (i-offset) + " numeric \n";
		}
					
		attributeNames+="@data \n";
		return attributeNames;
	}
	
	/**
	 * @author dmusican
	 * Import the filter data
	 */
	private void importFilterData(String filename) {
		try {
			Statement stmt = con.createStatement();

			stmt.executeUpdate(
				"IF (OBJECT_ID('AggData') IS NOT NULL) DROP TABLE AggData"
			);
			
			stmt.executeUpdate(
				"CREATE TABLE AggData (TimeStamp DATETIME, Value FLOAT)"
			);

			stmt.executeUpdate(
				"BULK INSERT AggData\n" +
				"FROM '" + filename + "'\n" + 
				"WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\n')"
			);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
//	take in file of ec/bc/whatever data in csv format
	public static void main(String[] args){
		Date start = new Date();
		StringBuilder builder = new StringBuilder();
		SQLAggregator sa = new SQLAggregator();
		sa.open();
		try {
			PrintWriter out = new PrintWriter("C:/Documents and Settings/dmusican/workspace/edam-enchilada/prediction/EC200.arff"); 
			//write the .arff file headings
			out.print(sa.assembleAttributes("ecrelation", "ec"));

			//aggregate data and format into .arff format, write to file
			sa.importFilterData("C:/Documents and Settings/dmusican/workspace/edam-enchilada/prediction/EC.csv");
			System.out.println("Data imported");
			
			sa.process(out);
			out.close();
			sa.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = new Date();
		System.out.println("time taken = " + (end.getTime() - start.getTime())
				+ " milliseconds.");
	}
	 
}
