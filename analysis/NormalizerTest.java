package analysis;

import junit.framework.TestCase;

/**
 * Testing!!!!
 * @author steinbel
 *
 */
public class NormalizerTest extends TestCase {

	
	protected void setUp() throws Exception {

		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
	}


	public void testNormalize() {
		
		Normalizer norm = new Normalizer();
		BinnedPeakList normalizeThis = generatePeaks(norm);
		//normalize with city-block distance
		normalizeThis.normalize(DistanceMetric.CITY_BLOCK);
		assert(normalizeThis.getMagnitude(DistanceMetric.CITY_BLOCK) <= 1.0):
			"Did not normalize properly with city block distance.";

		
		normalizeThis = generatePeaks(norm);
		//normalize with dot-product distance
		normalizeThis.normalize(DistanceMetric.DOT_PRODUCT);
		assert(normalizeThis.getMagnitude(DistanceMetric.DOT_PRODUCT) <= 1.0):
			"Did not normalize properly with city block distance.";
		
		normalizeThis = generatePeaks(norm);
		//normalize with Euclidean squared
		normalizeThis.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
		assert(normalizeThis.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED) <= 1.0):
			"Did not normalize properly with city block distance.";
	}


	public void testRoundDistance() {
		Normalizable norm = new Normalizer();
		BinnedPeakList bpl = generatePeaks(norm);
		BinnedPeakList other = new BinnedPeakList();
		other.add(-200, 35);
		other.add(100, 35);
		//test city-block
		float distance = bpl.getDistance(other, DistanceMetric.CITY_BLOCK);
		distance = norm.roundDistance(bpl, other, DistanceMetric.CITY_BLOCK, distance);
		assert (distance <= 2.0): "Distance too great with city-block.";
		
		//test dot product
		distance = bpl.getDistance(other, DistanceMetric.DOT_PRODUCT);
		distance = norm.roundDistance(bpl, other, DistanceMetric.DOT_PRODUCT, distance);
		assert (distance <= 2.0): "Distance too great with dot product.";
		
		//test Euclidean squared
		distance = bpl.getDistance(other, DistanceMetric.EUCLIDEAN_SQUARED);
		distance = norm.roundDistance(bpl, other, DistanceMetric.EUCLIDEAN_SQUARED, distance);
		assert (distance <= 2.0): "Distance too great with Euclidean squared.";
	}
	
	private BinnedPeakList generatePeaks(Normalizable norm){
		BinnedPeakList bpl = new BinnedPeakList(norm);
		bpl.add(-430, 15);
		bpl.add(-300, 20);
		bpl.add(800, 5);
		bpl.add(0, 7);
		bpl.add(30, 52);
		bpl.add(70, 15);
		bpl.add(-30, 13);
		bpl.add(80, 1);
		bpl.add(-308, 48);
		return bpl;
	}
}
