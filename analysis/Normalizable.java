package analysis;

import java.util.Iterator;

public abstract class Normalizable {
	public abstract void normalize(BinnedPeakList peakList, DistanceMetric dMetric);
	public abstract float roundDistance(BinnedPeakList peakList, BinnedPeakList toList, DistanceMetric dMetric, float distance);
	/**
	 * @author steinbel
	 * Raises peak areas to the power passed in for preprocessing.
	 * @param peakList	The list to process.
	 * @param powerValue The number to which the peaks' areas is raised.
	 * 						(.5 is a good value.)
	 */
	public void reducePeaks(BinnedPeakList peakList, double powerValue) {
		BinnedPeak entry;
		Iterator<BinnedPeak> iter = peakList.iterator();
		float newVal;
		while (iter.hasNext()){
			entry = iter.next();
			newVal = (float)Math.pow((double)entry.value, powerValue);
			//System.out.println("Old value " + entry.getValue() + " sqrt " + newVal); //DEBUGGING
			entry.value = newVal;
		}
	}
}
