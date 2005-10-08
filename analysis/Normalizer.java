package analysis;

import java.util.Iterator;
import java.util.Map;

public class Normalizer implements Normalizable {

	public BinnedPeakList normalize(BinnedPeakList peakList, DistanceMetric dMetric) {
		float magnitude = peakList.getMagnitude(dMetric);	
		
		Map.Entry<Integer,Float> entry;
		Iterator<Map.Entry<Integer,Float>> iterator = peakList.peaks.entrySet().iterator();
		
		while (iterator.hasNext()) {
			entry = iterator.next();
			entry.setValue(entry.getValue() / magnitude);
		}
		return peakList;		
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
