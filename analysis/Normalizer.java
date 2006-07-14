package analysis;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Normalizer extends Normalizable {

	public void normalize(BinnedPeakList peakList, DistanceMetric dMetric) {
		
		//first normalize negative peaks
		float magnitude = peakList.getPartialMag(dMetric, true);
		BinnedPeak bp;
		Iterator<BinnedPeak> iter = peakList.posNegIterator(true);
		while (iter.hasNext()){
			bp = iter.next();
			bp.value = bp.value/magnitude;
		}
		
		//next normalize the positive peaks (and zero)
		magnitude = peakList.getPartialMag(dMetric, false);
		iter = peakList.posNegIterator(false);
		while (iter.hasNext()){
			bp = iter.next();
			bp.value = bp.value/magnitude;
		}
		
		
		//normalize altogether
		magnitude = peakList.getMagnitude(dMetric);	
		Map.Entry<Integer,Float> entry;
		Iterator<Entry<Integer, Float>> iterator = peakList.peaks.entrySet().iterator();
		
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
