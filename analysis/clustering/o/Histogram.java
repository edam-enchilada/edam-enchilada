package analysis.clustering.o;

import java.util.*;

public class Histogram {
	private ArrayList<Integer> histogram;
	private int numBins;
	private LinkedList<Integer> extremaLocations;
	
	
	
	public Histogram() {
		// TODO: write this method too
		// i have no idea how the constructor should work right now.
	}
	
	private int findAllExtrema() {
		extremaLocations = new LinkedList<Integer>();
		int localMinLoc = 0;
		for (int bin = 0; bin < numBins; bin++) {
			// we can access bin+1 and bin-1 without worrying: see getCountAt().
			
			// the condition for a peak can never be true twice in a row.
			if (getCountAt(bin - 1) < getCountAt(bin)
					&& getCountAt(bin) >= getCountAt(bin + 1))
			{
				// this is a peak.
				extremaLocations.add(localMinLoc);
				extremaLocations.add(bin);
				localMinLoc = bin + 1;
			} else {
				// between peaks: keep track of the minimum.
				if (getCountAt(bin) < getCountAt(localMinLoc)) {
					localMinLoc = bin;
				}
			}
		}
		
		extremaLocations.remove(); // the 0th element is where the local min
		// before 0 would go.  but there is none, so we remove it.
		
		return extremaLocations.size();
	}
	
	public List<Float> getSplitPoints(int confidencePercent) {
		// TODO: write method for weeding statistically insignificant points
		// then this method is just taking the bin # of the valley of each one
		// and binToReal-ing it.
		return null;
	}
	
	private float binToReal(int bin) {
		return 0f; // TODO: something with # of bins, and shit.
	}
	
	private float targetChiSquared(int confidencePercent) {
		// TODO: find out how the statistics work for calculating a chi-squared
		// target thingie.
		return 0f;
	}
	
	private int getCountAt(int bin) {
		if (bin < 0 || bin >= numBins) {
			return -1;
		} else {
			return histogram.get(bin);
		}
	}
	
	
}
