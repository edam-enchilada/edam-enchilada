package analysis;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Normalizer extends Normalizable {

	/**
	 * @author steinbel
	 */
	public void normalize(BinnedPeakList peakList, DistanceMetric dMetric) {
		
		//set up stuff
		BinnedPeak bp;
		float magnitude, negMag, posMag;
		BinnedPeak entry;
		Iterator<BinnedPeak> iterator;
		//necessary because the iterator can't change peakList
		BinnedPeakList firstNorm = new BinnedPeakList();
		BinnedPeakList finalNorm = new BinnedPeakList();

		//first normalize postive and negative peaks
		negMag = peakList.getPartialMag(dMetric, true);	
		posMag = peakList.getPartialMag(dMetric, false);
		iterator = peakList.posNegIterator(true);
		//NOTE: putting any peaks at 0 into the positive spectrum
		while (iterator.hasNext()) {
			entry = iterator.next();
			firstNorm.add(entry.key, (entry.value / negMag));
		}	
		iterator = peakList.posNegIterator(false);
		while (iterator.hasNext()){
			entry = iterator.next();
			firstNorm.add(entry.key, (entry.value / posMag));
		}
	
		//normalize altogether
		magnitude = peakList.getMagnitude(dMetric);	
		iterator = firstNorm.iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			finalNorm.add(entry.key,(entry.value / magnitude));
		}	
		
		//reassign the original peaklist to the normalized values
		peakList = finalNorm;
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
