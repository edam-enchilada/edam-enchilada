package analysis;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Normalizer extends Normalizable {

	public void normalize(BinnedPeakList peakList, DistanceMetric dMetric) {
		
		//set up stuff
		BinnedPeak bp;
		float magnitude, negMag, posMag;
		Map.Entry<Integer,Float> entry;
		Iterator<Entry<Integer, Float>> iterator;

		//first normalize postive and negative peaks
//		first normalize postive and negative peaks
		// THIS IS ACTUALLY PROBLEMATIC for clustering, because
		// the theory for spherical k-means says that to get the optimal
		// normalized cluster center, you need to find the optimal center,
		// then scale via a scaling factor. This does something different,
		// and if you do pos/neg scaling at each iteration, clustering doesn't
		// converge. --- DRM
		/*negMag = peakList.getPartialMag(dMetric, true);	
		posMag = peakList.getPartialMag(dMetric, false);
		iterator = peakList.peaks.entrySet().iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			if(entry.getKey() < 0)
				entry.setValue(entry.getValue() / negMag);
			else 
				entry.setValue(entry.getValue() / posMag);
		}*/	
	
		//normalize altogether
		magnitude = peakList.getMagnitude(dMetric);	
		iterator = peakList.peaks.entrySet().iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			entry.setValue(entry.getValue() / magnitude);
		}	
	}


	public float roundDistance(BinnedPeakList peakList, BinnedPeakList toList, DistanceMetric dMetric, float distance) {
		assert distance < 2.01 : 
		    "Distance should be <= 2.0, actually is " + distance +"\n" 
		   + "Magnitudes: toList = " + toList.getMagnitude(dMetric) + " this = "
		  + peakList.getMagnitude(dMetric) + "\n";
		
		if (distance > 2) {
			//System.out.println("Rounding off " + distance +
			//		" to 2.0");
			distance = 2.0f;
		}
		return distance;
	}

}