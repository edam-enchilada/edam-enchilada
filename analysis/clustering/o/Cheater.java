package analysis.clustering.o;

import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import database.*;

public class Cheater {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InfoWarehouse db = new SQLServerDatabase();
		db.openConnection();
		CollectionCursor curs = db.getBinnedCursor(db.getCollection(502));
		
		DataWithSummary data = new DataWithSummary();
		
		while (curs.next()) {
			data.add(normalize(curs.getCurrent().getBinnedList()));
		}
		
		RootPartition r = new RootPartition(null);
		r.split(data);
		
		Partition p = r.getLeftChild();
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
	
	protected static BinnedPeakList normalize(BinnedPeakList list)
	{
		float magnitude = list.getMagnitude(DistanceMetric.CITY_BLOCK);
		BinnedPeakList returnList = new BinnedPeakList();
		BinnedPeak temp;
		Iterator<BinnedPeak> iter = list.iterator();
		while (iter.hasNext()) {
			temp = iter.next();
			if ((float)(temp.area / magnitude) != 0.0f)
				returnList.addNoChecks(temp.location, 
						temp.area / magnitude);
		}
		return returnList;
	}

}
