package prediction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ATOFMS.Peak;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;

import database.InfoWarehouse;
import database.Database;

/**
 * @author steinbel
 * Aggregates spectra to hourly time bins, adjusting for particle capture rates.
 * NOTE: Currently there are no checks for the source of the particles.  The 
 * entire database is queried, so it is the user's repsonsibility to ensure that
 * only source-compatible data are stored.  (Since our datasets don't overlap
 * time-wise this shouldn't be a problem, but there are no checks to prevent two
 * ATOFMS particles gathered in two separate locations in the same hour from both
 * figuring into the BC or EC prediction for only one of those locations.)
 *
 */
public class PredictionAggregator {

	private InfoWarehouse db;
	private Connection con;
	private int maxAtt;
	private int offset = 2500;
	
	public PredictionAggregator(){
		db = Database.getDatabase();
		//assume the first three attributes (starting at zero) are taken by
		//time, mass, and EC or BC
		maxAtt = 3;
	}
	
	public void open(){
		db.openConnection();
		con = db.getCon();
	}
	
	public void close(){
		db.closeConnection();
	}
	
	/**
	 * @author steinbel
	 * Given a time (and EC or BC level), aggregates corresponding ATOFMS data,
	 * returns it all in .arff format.  Aggregates on one-hour basis.
	 * 
	 * @param input -	date(M/d/yyyy hh:mm:ss a format), EC/BC level
	 * @return String -	input, with aggregated mass and ATOFMS spectra appended
	 */
	public String grab(String input){
		String output = "";
		String piece = input.split(",")[0];
		String predictThis = input.split(",")[1];
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
		
		try {
			/*icky long section that parses the time and creates a times half-
			 * hour before and after the given time for the start & end dates to
			 *  give the database
			 */
			Date when = sdf.parse(piece);
			long temp = when.getTime();
			Timestamp ts = new Timestamp(temp);
			String time = ts.toString().substring(0, ts.toString().lastIndexOf('.'));
			int hour = Integer.parseInt(time.substring(11, 13));
			hour --;
			if (hour<0){
				hour+=24;
				//TODO: still doesn't deal with month/year changes
				int day = Integer.parseInt(time.substring(8, 10));
				day --;
				if (day < 10)
					time = time.substring(0,8) + 0 + day + time.substring(10);
				else 
					time = time.substring(0,8) + day + time.substring(10);
			}
			String before = time.substring(0,11) + hour + ":30" + time.substring(16);
			String after = time.substring(0, 13) + ":30" + time.substring(16);
			System.out.print("start time: " + before + " end time: " + after +
					" orig: "); //debugging
			System.out.println(time); //debugging
			
			//retrieve dense info from db
			Statement stmt = con.createStatement();
			String query = "SELECT * FROM ATOFMSAtomInfoDense WHERE Time > " + "'" +
					before + "' AND Time <= '" + after + "'";
			ResultSet rs = stmt.executeQuery(query);
	
			BinnedPeakList total = new BinnedPeakList();
			BinnedPeakList bpl = new BinnedPeakList();

			double mass = 0.0;
			boolean isEmpty = true;
			double factor;
			double size;
			while (rs.next()){
				isEmpty = false;
				System.out.println("atomID " + rs.getInt("AtomID")); //debugging
		
				size = rs.getDouble("Size");
				factor = calculateDE(size);
				if (factor != 0){
					//increment total mass seen by size^3 adjusted for capture 
					//rate
					mass += size*size*size*factor;
				
					// add the adjusted peaklist for this particle to total 
					// spectrum
					bpl = producePeaks(factor, rs.getInt("AtomID"));
					total.addAnotherParticle(bpl);
				}
			}
			if (!isEmpty) {
				// echo the input info and add the aggregated mass
				output += "{ " + "0 " + '"' + piece + '"' + ",1 " + mass
						+ ", 2 " + predictThis;
				Iterator<BinnedPeak> iter = total.iterator();
				BinnedPeak bp;
				int att;
				while (iter.hasNext()) {
					bp = iter.next();
					/*
					 * Weka needs its indices in order and all positive, so 
					 * we're giving all m/z values a boost of 2500.
					 */
					att = bp.key + offset;
					if (att > maxAtt)
						maxAtt = att;
					
					output += "," + att + " " + bp.value;

				}

			}
			output += "} \n";
			total.printPeakList();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	
	/**
	 * @author steinbel
	 * Produces a peaklist from an atom, factoring in the capture rate.
	 * @param numRepresented - the number of particles this particle should
	 * 							represent
	 * @param atomID - the atom for which the peaklist needs to be adjusted
	 * @return the adjusted peaklist
	 */
	public BinnedPeakList producePeaks(double numRepresented, int atomID){
		BinnedPeakList bpl = new BinnedPeakList();
		
		//retrieve peaks from db
		ArrayList<Peak> peaks = db.getPeaks("ATOFMS", atomID);
		//factor in the capture rate
		for (Peak pk: peaks)
			bpl.add((float)pk.massToCharge, (float)(pk.area*numRepresented));

		return bpl;
	}
	
	/**
	 * @author steinbel
	 * Calculates the detection efficiency for Gromit for that particle size.
	 * NOTE: this method is Gromit-specific!
	 * 
	 * @param size -	the size of the particle in question
	 * @return double -	the number of particles a particle this size should 
	 * 					represent, returns 0 if outside 100-2500nm size range
	 */
	public double calculateDE(double size){
		double factor = 1.0;
		//our results in micros (10^-6).  but need nano(10^-9) - convert
		double nanos = size * (Math.pow(10., 3.0));
		if (size > nanos)
			System.err.println("micros: " + size + " nanos: " + nanos);
		double m = 0.0;
		double b = 0.0;

		//fill in values
		if ( nanos > 100 && nanos <= 750 ){
			m = 2.8574;
			b = -27.16;
		} else if ( nanos > 750 && nanos <= 1000 ){
			m = -.58272;
			b = -4.803;
		} else if ( nanos > 1000 && nanos <= 2500){
			m = -7.52;
			b = 42.031;
		} else{
			//System.err.println("size out of range: " + nanos + " nanos");
			return 0;
		}
		//DE = (actual size)^(number given, depends on size,m)*e^(given,b)
		//from Deborah: DetectionEfficiency_Gromit.pdf
		factor = Math.pow(nanos, m)*Math.exp(b);
		
		//particle/DE = appropriately scaled particle factor
		factor = 1/factor;
		
		return factor;
	}
	
	/**
	 * Writes the .arff header info for this file, which includes naming
	 * the m/z values as attributes so we can find them later. 
	 * @return the header for the .arff file in the form of a string
	 */
	public String assembleAttributes(){
		String attributeNames = "@relation BC \n "
				+"@attribute time date \"MM/d/yyyy hh:mm:ss a\" \n"
				+"@attribute mass numeric \n"
				+"@attribute bc numeric \n";
		
		//start at 3 because of the attributes named above
		for (int i=3; i<=maxAtt; i++){
			attributeNames+="@attribute mz" + (i-offset) + " numeric \n";
		}
					
		attributeNames+="@data \n";
		return attributeNames;
	}

}
