package prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

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
	 * Takes a string with the time and bc or ec level, gathers and formats the
	 * ATOFMS data appropriately.
	 * @param in - the string with the time and ec or bc level
	 * @return a formatted string for Weka with ATOFMS info added
	 */
	public String process(String in){
		//start the output string
		String output = "{" + "0 " + '"' + in.split(",")[0] + '"' +
						",1 " + in.split(",")[1] + ",";
		
		//send incoming string to create a temp table
		createTempTable(in);
		System.out.println("table created");//TESTING
		BinnedPeakList bpl = new BinnedPeakList();
		double mass = 0.0;
		try {
			Statement stmt = con.createStatement();
			/* Sum together the sizes of the particles and the areas of the
			 * peaks, both adjusted for detection efficiency. 
			 */
			ResultSet rs = stmt.executeQuery("select sum(d.size*d.size*d.size*" + density+ "*(1/h.de)) as mass," +
					" s.peaklocation, sum(s.peakarea/h.de) as adjustedpeak " +
					"from atofmsatominfodense d, atofmsatominfosparse s, #hourbin h " +
					"where h.atom = s.atomid and h.atom = d.atomid " +
					"group by s.peaklocation;");
			while (rs.next()){
				mass += rs.getDouble("mass");
				bpl.add(rs.getFloat("peaklocation"), rs.getFloat("adjustedpeak"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bpl.printPeakList();//TESTING
		//format the peaklist for the .arff format
		 output += format(bpl, mass);
		 output += "}\n";
		
		//drop temp table
		dropTempTable();
		
		return output;
	}
	
	/**
	 * @author steinbel
	 * Formats the mass and the peaks for feeding into Weka.
	 * @param list - the list of peaks and locations for this time bin
	 * @param m - the mass found during this time bin
	 * @return a string formatted for Weka
	 */
	private String format(BinnedPeakList list, double m){
		String out = "";
		out += "2 " + m;
		Iterator<BinnedPeak> iter = list.iterator();
		int location;
		while (iter.hasNext()){
			location = iter.next().key;
			//this is where the limit on peak locations recorded takes effect
			if ((location >= -300) && (location <= 300)){
				out += "," + (location+303) + " " + iter.next().value;
			}
		}
		return out;
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
	 * Creates a temporary table of atomID and detection efficiency of those 
	 * particles that were observed during the time period centered on the
	 * time given in the line of text coming into this method.
	 * @param lineIn - the time of the bc/ec collection and the amount of ec/bc
	 */
	private void createTempTable(String lineIn){
		//grab the time & date off the line coming in, create temp table
		String orig = lineIn.split(",")[0];
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
		try {
			Date given = sdf.parse(orig);
			Timestamp ts = new Timestamp(given.getTime());
			//this may be crazy and convoluted, but SQLServer likes the results.
			String time = ts.toString().substring(0, ts.toString().lastIndexOf('.'));
			Statement stmt = con.createStatement();
			/* Create a temp table with atomID and detection efficiency 
			 * (initially populated with the size of the particles) during the
			 * hour-long timebin around the given time.
			 */
			stmt.executeUpdate("IF (OBJECT_ID('tempdb..#hourbin') IS NOT NULL) DROP TABLE #hourbin;");
			String order = "create table #hourbin (atom int, de real);" +
			" insert into #hourbin(atom, de)" +
			" select d.atomid, d.size" +
			" from ATOFMSAtomInfoDense d" +
			" where d.time < (DATEADD (n, 30, '"+ time + "'))" +
			" and d.time >= (dateadd (n, -30, '"+ time + "'));";
			System.out.println(order);//TESTING
			stmt.executeUpdate(order);
			//remove particles outside of size range
			stmt.executeUpdate("delete from #hourbin" +
					" where de < .1 or de > 2.5;");
			//calculate the Detection Efficiency for the three size bins
			//as described in DetectionEfficiency_Gomit.pdf
			stmt.executeUpdate("update #hourbin " +
					"set de = (select power(de*100, 2.8574)*exp(-27.16)) " +
					"where de > .1 and de <= .75;");
			stmt.executeUpdate("update #hourbin " +
					"set de = (select power(de*100, -.58272)*exp(-4.803)) " +
					"where de > .75	and de <= 1.;");
			stmt.executeUpdate("update #hourbin " +
					"set de = (select power(de*100, -7.52)*exp(42.031)) " +
					"where de > 1. and de <= 2.5;");
			stmt.close();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			stmt.executeUpdate("IF (OBJECT_ID('tempdb..#hourbin') " +
					"IS NOT NULL)\n" +
					" DROP TABLE #hourbin\n");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @author steinbel
	 * Writes the .arff header info for this file, which includes naming
	 * the m/z values as attributes so we can find them later. 
	 * @param relationName - the name of the relation
	 * @param predictThis - the attribute to be predicted (e.g., "ec" or "bc")
	 * @return the header for the .arff file in the form of a string
	 */
	public String assembleAttributes(String relationName, String predictThis){
		String attributeNames = "@relation " + relationName +" \n "
				+"@attribute time date \"MM/d/yyyy hh:mm:ss a\" \n"
				+"@attribute " + predictThis + " numeric \n"
				+"@attribute mass numeric \n";
		
		//start at 3 because of the attributes named above
		for (int i=3; i<=maxAtt; i++){
			attributeNames+="@attribute mz" + (i-offset) + " numeric \n";
		}
					
		attributeNames+="@data \n";
		return attributeNames;
	}
	
	
//	take in file of ec/bc/whatever data in csv format
	public static void main(String[] args){
		Date start = new Date();
		String line;
		String result = "";
		StringBuilder builder = new StringBuilder();
		SQLAggregator sa = new SQLAggregator();
		try {
			Scanner scan = new Scanner(new File("C:/Documents and Settings/dmusican/workspace/edam-enchilada/prediction/small.csv"));
			FileWriter out = new FileWriter(new File("C:/Documents and Settings/dmusican/workspace/edam-enchilada/prediction/small.arff")); 
			//write the .arff file headings
			out.write(sa.assembleAttributes("small", "ec"));
			sa.open();
			//aggregate data and format into .arff format, write to file
			while (scan.hasNextLine()){

				line = scan.nextLine();
				out.append(sa.process(line));
			}
			sa.close();
			out.close();
			scan.close();
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
