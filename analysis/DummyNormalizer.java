package analysis;

public class DummyNormalizer implements Normalizable{

	public BinnedPeakList normalize(BinnedPeakList peakList, DistanceMetric dMetric) {
		return peakList;
	}

	public float roundDistance(BinnedPeakList peakList, BinnedPeakList toList, DistanceMetric dMetric, float distance) {
		return distance;
	}

}
