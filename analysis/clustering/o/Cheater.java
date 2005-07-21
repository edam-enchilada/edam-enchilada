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
		CollectionCursor curs = db.getBinnedCursor(db.getCollection(1));
		
		DataWithSummary data = new DataWithSummary();
		
		for (int i = 0; i < 2964; i++) {
			curs.next();
			data.add(normalize(curs.getCurrent().getBinnedList()));
		}
		
		NumberBox n = new NumberBox(5000);
		n.addAll(data);
				
		n.printDimension(27);
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
