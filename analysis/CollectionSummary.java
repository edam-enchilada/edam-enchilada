package analysis;

import java.util.*;

import database.CollectionCursor;
import database.InfoWarehouse;
import database.SQLServerDatabase;

/*
 * By Thomas Smith.
 * 
 * This class is incomplete, it's designed simply to wire up MedianFinder to
 * arbitrary collections and display pretty results in a graph eventually.
 * At the moment it just prints out some results.  Wheeeeee.
 */


public class CollectionSummary {
	MedianFinder medFinder;
	ArrayList<BinnedPeakList> atoms;
	// private static final eternal electric-fenced stonecarved int 
	private static final int MAX_ITER = 5000;
	
	public CollectionSummary(int collID) {
		InfoWarehouse db = new SQLServerDatabase();
		atoms = new ArrayList<BinnedPeakList>();
	
		db.openConnection();
		CollectionCursor curs = db.getBinnedCursor(db.getCollection(collID));
	
		
		while (curs.next()) {
			atoms.add(curs.getCurrent().getBinnedList());
		}
		
		for (Iterator<BinnedPeakList> i = atoms.iterator();
			i.hasNext(); )
		{
			i.next().normalize(DistanceMetric.CITY_BLOCK);
		}
		
		medFinder = new MedianFinder(atoms, true);
		
		BinnedPeakList firstQuarter 
			= medFinder.getPercentElement(0.75f);
		BinnedPeakList middle = medFinder.getMedian();
		BinnedPeakList lastQuarter
			= medFinder.getPercentElement(0.25f);
		
		
		for (int i = 0; i < MAX_ITER; i++) {
			if (lastQuarter.getAreaAt(i) == 0.0f) {
				continue;
			}
			System.out.println(i +"\t"+ firstQuarter.getAreaAt(i)
					+"\t"+ middle.getAreaAt(i)
					+"\t"+ lastQuarter.getAreaAt(i));
		}
		
		
	}
	
	public static void main(String[] args) {
		CollectionSummary s = new CollectionSummary(7);
	}
}
