package analysis;

public class DummyNormalizer extends Normalizable{

	public void normalize(BinnedPeakList peakList, DistanceMetric dMetric) {
		return;
	}

	public float roundDistance(BinnedPeakList peakList, BinnedPeakList toList, DistanceMetric dMetric, float distance) {
		return distance;
	}
	

}
