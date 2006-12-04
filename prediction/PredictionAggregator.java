package prediction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
			//TODO: doesn't deal well with switches from day to day
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
	
			BinnedPeakList total = new BinnedPeakList();
			BinnedPeakList bpl = new BinnedPeakList();

			double mass = 0.0;
			boolean isEmpty = true;
			double factor;
			double size;
			while (rs.next()){
				isEmpty = false;
				System.out.println("atomID " + rs.getInt("AtomID")); //debugging
		
				size = rs.getDouble("Size"); /* If size=0, then mass is NaN, and
											  * we have problems for Weka
											  * so what to do?
											  */
				factor = calculateDE(size);
				//increment total mass seen by size^3 adjusted for capture rate
				mass += size*size*size*factor;
			
				// add the adjusted peaklist for this particle to total spectra
				bpl = producePeaks(factor, rs.getInt("AtomID"));
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
				total.printPeakList();
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
	 * Produces a peaklist from an atom, factoring in the capture rate.
	 * @param percent - the percentage of particles with this size caputered
	 * @param atomID - the atom for which the peaklist needs to be adjusted
	 * @return the adjusted peaklist
	 */
	public BinnedPeakList producePeaks(double percent, int atomID){
		BinnedPeakList bpl = new BinnedPeakList();
		
		//retrieve peaks from db
		ArrayList<Peak> peaks = db.getPeaks("ATOFMS", atomID);
		//factor in the capture rate
		for (Peak pk: peaks)
			bpl.add((float)pk.massToCharge, (float)(pk.area*percent));

		return bpl;
	}
	
	/**
	 * @author steinbel
	 * Calculates the detection efficiency for Gromit for that particle size.
	 * NOTE: this method is Gromit-specific!
	 * 
	 * @param size -	the size of the particle in question
	 * @return double -	the number of particles a particle this size should 
	 * 					represent
	 */
	public double calculateDE(double size){
		double factor = 1.0;
		//our results in micros (10^-6).  but need nano(10^-9) - convert
		double nanos = size * (Math.pow(10., 3.0));
		if (size > nanos)
			System.err.println("micros: " + size + " nanos: " + nanos);
		double m = 0.0;
		double b = 0.0;
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
			System.err.println("size out of range: " + nanos + " nanos");
			/* For now, we're keeping m & b at 0, which will render the
			 * factor value to be 1, so we'll only count this particle as
			 * one particle.  
			 * 
			 */
		}
		//DE = (actual size)^(number given, depends on size,m)*e^(given,b)
		//from Deborah: DetectionEfficiency_Gromit.pdf
		factor = Math.pow(nanos, m)*Math.exp(b);
		
		//particle/DE = appropriately scaled particle factor
		factor = 1/factor;
		
		return factor;
	}

}
