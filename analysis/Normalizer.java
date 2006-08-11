package analysis;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Normalizer extends Normalizable {

	public void normalize(BinnedPeakList peakList, DistanceMetric dMetric) {

		//set up stuff
		float magnitude;
		Map.Entry<Integer,Float> entry;
		Iterator<Entry<Integer, Float>> iterator;
		
		magnitude = peakList.getMagnitude(dMetric);
		iterator = peakList.peaks.entrySet().iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			entry.setValue(entry.getValue() / magnitude);
		}
	}
	
	/**
	 * @author steinbel
	 * Normalizes a peak list according to the distance metric, normalizing the
	 * positive and negative spectra separately, then together.
	 * @param peakList	The peak list to normalize (note: values are changed).
	 * @param dMetric	The distance metric to use.
	 */
	public void posNegNormalize(BinnedPeakList peakList, DistanceMetric dMetric){
		float posMag, negMag;
		Map.Entry<Integer, Float> entry;
		Iterator<Map.Entry<Integer, Float>> iterator;
		
		negMag = peakList.getPartialMag(dMetric, true);
		posMag = peakList.getPartialMag(dMetric, false);
		iterator = peakList.peaks.entrySet().iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			if(entry.getKey() < 0)
				entry.setValue(entry.getValue() / negMag);
			else 
				entry.setValue(entry.getValue() / posMag);
		}
		
		//normalize altogether
		normalize(peakList, dMetric);
	}


	public float roundDistance(BinnedPeakList peakList, BinnedPeakList 
toList, DistanceMetric dMetric, float distance) {
		assert distance < 2.01 : 		    "Distance should be <= 2.0, actually is " + distance +"\n" 		   + "Magnitudes: toList = " + 
		toList.getMagnitude(dMetric) + " this = "
		+ peakList.getMagnitude(dMetric) + "\n";

		if (distance > 2) {
			//System.out.println("Rounding off " + distance +
			//		" to 2.0");
			distance = 2.0f;
		}
		return distance;
	}

}



