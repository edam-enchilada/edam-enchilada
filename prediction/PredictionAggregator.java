package prediction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import ATOFMS.ATOFMSPeak;
import ATOFMS.Peak;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import database.Database;
import database.InfoWarehouse;

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

	InfoWarehouse db;
	Connection con;
	
	public PredictionAggregator(){
		db = Database.getDatabase();
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
			/*icky long section that parses the time and creates a time an hour
			 * before for the start & end dates to give the database
			 */
			Date when = sdf.parse(piece);
			long temp = when.getTime();
			Timestamp ts = new Timestamp(temp);
			String time = ts.toString().substring(0, ts.toString().lastIndexOf('.'));
			int hour = Integer.parseInt(time.substring(11, 13));
			hour --;
			if (hour<0)
				hour+=24;
			String before = time.substring(0,11) + hour + time.substring(13);
			System.out.print("Orig " + time + " prev time "); //debugging
			System.out.println(before); //debugging
			
			//retrieve dense info from db
			Statement stmt = con.createStatement();
			String query = "SELECT * FROM ATOFMSAtomInfoDense WHERE Time > " + "'" +
					before + "' AND Time <= '" + time + "'";
			ResultSet rs = stmt.executeQuery(query);
			
			BinnedPeakList bpl = new BinnedPeakList();
			BinnedPeakList total = new BinnedPeakList();

			double mass = 0.0;
			boolean isEmpty = true;
			while (rs.next()){
				isEmpty = false;
				System.out.println("atomID " + rs.getInt("AtomID")); //debugging
				// send the size & id for processing
				process(rs.getDouble("Size"), mass, bpl, rs.getInt("AtomID"));

				// add the results to total spectra
				total.addAnotherParticle(bpl);
			}
			if (!isEmpty){
				//echo the input info and add the aggregated mass
				output += "{ " + "0 " +'"'+ piece + '"' + ",1 " + mass +", 2 " +
					predictThis;
				Iterator<BinnedPeak> iter = total.iterator();
				BinnedPeak bp;
				while(iter.hasNext()){
					bp = iter.next();
					/*what do we want to do in this case?  ignore?  increase range?
					 *issues here with Weka's ARFF format (indices all pos, in order)
					 *this is why the indexing starts after 2 (up to then is used)
					 */
					if (((bp.key+303) <= 2 ) || (bp.key+303) > 600){
						System.out.println("bad indices");
					} else{
						output += ",";
						//bump up peak location to avoid negative numbers.
						output += (bp.key+303) + " ";
						output += bp.value;
					}
				}
				output += "} \n";
				bpl.printPeakList();
			}
			
			
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
	 * Calculates mass, adjusts peaks to reflect particle capture rates.
	 * 
	 * @param s - size of the particle
	 * @param m - the current total mass for this time bin (gets updated)
	 * @param peakList - updated with the adjusted peaklist for the atom
	 * @param atomID -	the atom to adjust
	 */
	public void process(double s, double m, BinnedPeakList peakList, int atomID){
		//increment total mass seen by size^3 adjusted for capture rate
		double percent = getPercent(s);
		m += s*s*s*percent;
		//retrieve peaks from db
		ArrayList<Peak> peaks = db.getPeaks("ATOFMS", atomID);
		//factor in the capture rate
		for (Peak p: peaks)
			peakList.add((float)p.massToCharge, (float)(((ATOFMSPeak)p).area*percent));

		
	}
	
	/**
	 * @author steinbel
	 * Calculates the detection efficiency for Gromit for that particle size.
	 * NOTE: this method is Gromit-specific!
	 * 
	 * @param size -	the size of the particle in question
	 * @return double -	the detection efficiency for Gromit
	 */
	public double getPercent(double size){
		double percent = 1.0;
		//our results in microseconds (10^-6).  but need nano(10^-9) - convert
		double nanos = size * (Math.pow(10., 3.0));
		if (size > nanos)
			System.err.println("micros: " + size + " nanos: " + nanos);
		double m = 1.0;
		double b = 1.0;
		if ( nanos > 100 && nanos <= 750 ){
			//fill in values
			m = 2.8574;
			b = -27.16;
		} else if ( nanos > 750 && nanos <= 1000 ){
			m = -.58272;
			b = -4.803;
		} else if ( nanos > 1000 && nanos <= 2500){
			m = -7.52;
			b = 42.031;
		} else{
			//TODO: error!  size out of range!  What do we want here?
		}
		//DE = (actual size)^(number given, depends on size)*e^(different given)
		//from Deborah: DetectionEfficiency_Gromit.pdf
		percent = Math.pow(nanos, m)*Math.exp(b);
		
		//particle/DE = appropriately scaled particle factor
		percent = 1/percent;
		
		return percent;
	}

}
