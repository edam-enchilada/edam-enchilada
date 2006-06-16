package analysis;

public interface Normalizable {
	public void normalize(BinnedPeakList peakList, DistanceMetric dMetric);
	public float roundDistance(BinnedPeakList peakList, BinnedPeakList toList, DistanceMetric dMetric, float distance);
}
