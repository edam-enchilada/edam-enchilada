package analysis.clustering.o;

import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;
import database.*;

public class Cheater {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InfoWarehouse db = new SQLServerDatabase();
		db.openConnection();
		CollectionCursor curs = db.getBinnedCursor(db.getCollection(24));
		
		DataWithSummary data = new DataWithSummary();
		
		// using a DataWithSummary accomplishes making an ArrayList of
		// everything, while simultaneously compiling some statistical
		// information needed to make a histogram.
		while (curs.next()) {
			data.add(normalize(curs.getCurrent().getBinnedList()));
		}
		
		// a RootPartition handles all of the object transformation stuff
		// that needs to happen when an Undetermined changes to a Branch
		// or Frozen partition.
		RootPartition r = new RootPartition(null);
		// This is the cute little sexy call that runs the algorithm
		// on all the data.  Neat!  If the data are ambiguous, this
		// would return a nonzero value, but this is Cheater.java:
		// I don't care.
		r.split(data);
		
		Partition p = r.getLeftChild();
		
		// prints out the hierarchy of rules.
		p.printRulesDown();
		
//		
//		NumberBox n = new NumberBox(5000);
//		n.addAll(data);
//				
//		n.printDimension(12);
//		n.printDimension(24);
//		n.printDimension(27);
//		n.printDimension(29);
//		n.printDimension(37);
//		n.printDimension(39);
//		n.printDimension(48);
	}
	
	/**
	 * Copied from Cluster.java.
	 */
	protected static BinnedPeakList normalize(BinnedPeakList list)
	{
		float magnitude = list.getMagnitude(DistanceMetric.CITY_BLOCK);
		BinnedPeakList returnList = new BinnedPeakList(new Normalizer());
		BinnedPeak temp;
		Iterator<BinnedPeak> iter = list.iterator();
		while (iter.hasNext()) {
			temp = iter.next();
			if ((float)(temp.value / magnitude) != 0.0f)
				returnList.addNoChecks(temp.key, 
						temp.value / magnitude);
		}
		return returnList;
	}

}
