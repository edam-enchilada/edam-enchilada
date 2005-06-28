package analysis.clustering.o;

import java.util.*;

public class Histogram {
	private HistList histogram;
	private float binWidth;
	private ValleyList splitPoints;
	
	
	
	public Histogram() {
		// TODO: write this method too
		// i have no idea how the constructor should work right now.
	}
	
	private int findAllExtrema() {
		LinkedList<Integer> extremaLocations = new LinkedList<Integer>();
		int localMinLoc = 0;
		for (int bin = 0; bin < histogram.size(); bin++) {
			// we can access past the ends of the array without worrying: see 
			// get() in the HistList class.
			
			if (histogram.get(bin - 1) < histogram.get(bin)
					&& histogram.get(bin) >= histogram.get(bin + 1))
			{
				// this bin contains a peak...
				
				// the second condition is >= so that if multiple
				// bins have the same count and are together a peak,
				// the first one will be detected as such.
				
				// the condition for a peak can never be true twice in a row:
				assert(localMinLoc != bin);

				extremaLocations.add(localMinLoc);
				extremaLocations.add(bin);
				localMinLoc = bin + 1;
			} else {
				// between peaks: keep track of the minimum.
				if (histogram.get(bin) < histogram.get(localMinLoc)) {
					localMinLoc = bin;
				}
			}
		}
		
		extremaLocations.remove(); // the 0th element is where the local min
		// before 0 would go.  but there is none, so we remove it.
		
		splitPoints.clear();
		Iterator<Integer> i = extremaLocations.iterator();
		Integer loc;
		while (i.hasNext()) {
			loc = i.next();
			splitPoints.add(new Extremum(loc, histogram.get(loc)));
		}
		return splitPoints.numValleys();
	}
	
	public List<Float> getSplitPoints(int confidencePercent) {
		LinkedList<Float> splits = new LinkedList<Float>();
		if (findAllExtrema() > 1) {
			splitPoints = splitPoints.removeInsignificant(confidencePercent);
			for (int i = 0; i < splitPoints.numValleys(); i++) {
				splits.add(histogram.getIndexMiddle(
						splitPoints.getValley(i).location));
			}
			return splits;
		} else {
			return null;
		}
	}
	
	private float targetChiSquared(int confidencePercent) {
		// TODO: find out how the statistics work for calculating a chi-squared
		// target thingie.
		return 0f;
	}
	
	public void addPeak(float area) {
		histogram.addPeak(area);
	}

}
