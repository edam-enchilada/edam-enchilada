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
		
		//normalize with city-block distance
		
		//figure out what the square peaks should be if normalized with city block
		Float firstNorm3 = (float) 3.0/ (3 + 4);
		Float firstNorm4 = (float) 4.0/ (3 + 4);
		Float firstMag = (firstNorm3 + firstNorm4)*2;
		Float secondNorm3 = firstNorm3 / (firstMag);
		Float secondNorm4 = firstNorm4 / (firstMag);
		Float secondMag = (secondNorm3 + secondNorm4) * 2;		
		
		Normalizer norm = new Normalizer();
		BinnedPeakList normalizeThis = generateSquarePeaks(norm);
		normalizeThis.normalize(DistanceMetric.CITY_BLOCK);
		
		assert(normalizeThis.getMagnitude(DistanceMetric.CITY_BLOCK) == 1.0):
			"Did not normalize properly with city block distance.";
		assert(normalizeThis.getAreaAt(-200) == secondNorm3); 
		assert(normalizeThis.getAreaAt(-100) == secondNorm4);
		assert(normalizeThis.getAreaAt(0) == secondNorm3);
		assert(normalizeThis.getAreaAt(100) == secondNorm4);
		
		
		//normalize with dot-product distance
		
		//figure out what the square peaks should be if normalized with
		//dot product or euclidean squared
		firstNorm3 = (float) 3.0/ (float) Math.sqrt(3*3 + 4*4);
		firstNorm4 = (float) 4.0/ (float) Math.sqrt(3*3 + 4*4);
		firstMag = (firstNorm3*firstNorm3 + firstNorm4*firstNorm4)*2;
		secondNorm3 = firstNorm3 / (float) Math.sqrt(firstMag);
		secondNorm4 = firstNorm4 / (float) Math.sqrt(firstMag);
		secondMag = (secondNorm3 * secondNorm3 + secondNorm4 * secondNorm4) * 2;		
		
		normalizeThis = generateSquarePeaks(norm);
		normalizeThis.normalize(DistanceMetric.DOT_PRODUCT);
		
		assert(normalizeThis.getMagnitude(DistanceMetric.DOT_PRODUCT) == 1.0):
			"Did not normalize properly with city block distance.";
		assert(normalizeThis.getAreaAt(-200) == secondNorm3); 
		assert(normalizeThis.getAreaAt(-100) == secondNorm4);
		assert(normalizeThis.getAreaAt(0) == secondNorm3);
		assert(normalizeThis.getAreaAt(100) == secondNorm4);
		
		
		//normalize with Euclidean squared
		
		normalizeThis = generateSquarePeaks(norm);
		normalizeThis.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
		
		assert(normalizeThis.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED) == 1.0):
			"Did not normalize properly with city block distance.";
		assert(normalizeThis.getAreaAt(-200) == secondNorm3); 
		assert(normalizeThis.getAreaAt(-100) == secondNorm4);
		assert(normalizeThis.getAreaAt(0) == secondNorm3);
		assert(normalizeThis.getAreaAt(100) == secondNorm4);
	}


	public void testRoundDistance() {
		//set them up
		Normalizable norm = new Normalizer();
		BinnedPeakList bpl = generatePeaks(norm);
		BinnedPeakList other = new BinnedPeakList();
		other.add(-200, 35);
		other.add(100, 35);
		//test city-block
		bpl.normalize(DistanceMetric.CITY_BLOCK);
		other.normalize(DistanceMetric.CITY_BLOCK);
		float distance = bpl.getDistance(other, DistanceMetric.CITY_BLOCK);
		distance = norm.roundDistance(bpl, other, DistanceMetric.CITY_BLOCK, distance);
		assert (distance <= 2.0): "Distance too great with city-block.";
		
		//test dot product
		bpl.normalize(DistanceMetric.DOT_PRODUCT);
		other.normalize(DistanceMetric.DOT_PRODUCT);
		distance = bpl.getDistance(other, DistanceMetric.DOT_PRODUCT);
		distance = norm.roundDistance(bpl, other, DistanceMetric.DOT_PRODUCT, distance);
		assert (distance <= 2.0): "Distance too great with dot product.";
		
		//test Euclidean squared
		bpl.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
		other.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
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
	private BinnedPeakList generateSquarePeaks(Normalizable norm) {
		BinnedPeakList bpl = new BinnedPeakList(norm);
		bpl.add(-200, 3);
		bpl.add(-100, 4);
		bpl.add(0, 3);
		bpl.add(100, 4);
		return bpl;
	}
}
