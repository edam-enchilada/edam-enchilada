package analysis;

import junit.framework.TestCase;

public class NormalizerTest extends TestCase {

	protected void setUp() throws Exception {

		super.setUp();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		
	}
	
	/**
	 * @author steinbel
	 * @author dmusican
	 */
	public void testNormalize() {
			
		//normalize with city-block distance
		
		
		Float noSeparateNorm3 = (float)(3.0/(3+4+3+4));
		Float noSeparateNorm4 = (float)(4.0/(3+4+3+4));
		
		Normalizer norm = new Normalizer();
		BinnedPeakList normalizeThis = generateSquarePeaks(norm);
		normalizeThis.normalize(DistanceMetric.CITY_BLOCK);
		
		// Test for normalize properly with city block distance
		assertEquals(normalizeThis.getMagnitude(DistanceMetric.CITY_BLOCK),1.0f);
		assertEquals(normalizeThis.getAreaAt(-200),noSeparateNorm3); 
		assertEquals(normalizeThis.getAreaAt(-100),noSeparateNorm4);
		assertEquals(normalizeThis.getAreaAt(0),noSeparateNorm3);
		assertEquals(normalizeThis.getAreaAt(100),noSeparateNorm4);
		
		
		//normalize with dot-product distance	
		
		normalizeThis = generateSquarePeaks(norm);
		normalizeThis.normalize(DistanceMetric.DOT_PRODUCT);

		noSeparateNorm3 = (float)(3.0/Math.sqrt(3*3+4*4+3*3+4*4));
		noSeparateNorm4 = (float)(4.0/Math.sqrt(3*3+4*4+3*3+4*4));
		
		// Did not normalize properly with city block distance.
		// Note that these all need to be changed back to secondNorm
		// (from firstNorm) once normalize puts pos/neg back in --DRM

		assertEquals(normalizeThis.getMagnitude(DistanceMetric.DOT_PRODUCT),1.0f);
		assertEquals(normalizeThis.getAreaAt(-200),noSeparateNorm3); 
		assertEquals(normalizeThis.getAreaAt(-100),noSeparateNorm4);
		assertEquals(normalizeThis.getAreaAt(0),noSeparateNorm3);
		assertEquals(normalizeThis.getAreaAt(100),noSeparateNorm4);
		
		
		//normalize with Euclidean squared
		
		normalizeThis = generateSquarePeaks(norm);
		normalizeThis.normalize(DistanceMetric.EUCLIDEAN_SQUARED);

		assertEquals("Did not normalize properly with city block distance.",
				normalizeThis.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED), 1.0f);
		assertEqualsDelta(normalizeThis.getAreaAt(-200), noSeparateNorm3); 
		assertEqualsDelta(normalizeThis.getAreaAt(-100), noSeparateNorm4);
		assertEqualsDelta(normalizeThis.getAreaAt(0), noSeparateNorm3);
		assertEqualsDelta(normalizeThis.getAreaAt(100), noSeparateNorm4);
		
	}
	
	/**
	 * @author steinbel
	 */
	public void testPosNegNormalize(){

		Float firstNorm3 = (float) 3.0/ (3 + 4);
		Float firstNorm4 = (float) 4.0/ (3 + 4);
		Float firstMag = (firstNorm3 + firstNorm4)*2;
		Float secondNorm3 = firstNorm3 / (firstMag);
		Float secondNorm4 = firstNorm4 / (firstMag);
		Float secondMag = (secondNorm3 + secondNorm4) * 2;	
		
		Normalizer norm = new Normalizer();
		BinnedPeakList normalizeThis = generateSquarePeaks(norm);
		
		normalizeThis.posNegNormalize(DistanceMetric.CITY_BLOCK);
		assertEquals(normalizeThis.getMagnitude(DistanceMetric.CITY_BLOCK),1.0f);
		assertEquals(normalizeThis.getAreaAt(-200),secondNorm3); 
		assertEquals(normalizeThis.getAreaAt(-100),secondNorm4);
		assertEquals(normalizeThis.getAreaAt(0),secondNorm3);
		assertEquals(normalizeThis.getAreaAt(100),secondNorm4);
		
		firstNorm3 = (float) 3.0/ (float) Math.sqrt(3*3 + 4*4);
		firstNorm4 = (float) 4.0/ (float) Math.sqrt(3*3 + 4*4);
		firstMag = (firstNorm3*firstNorm3 + firstNorm4*firstNorm4)*2;
		secondNorm3 = firstNorm3 / (float) Math.sqrt(firstMag);
		secondNorm4 = firstNorm4 / (float) Math.sqrt(firstMag);
		secondMag = (secondNorm3 * secondNorm3 + secondNorm4 * secondNorm4) * 2;	
		
		normalizeThis = generateSquarePeaks(norm);
		normalizeThis.posNegNormalize(DistanceMetric.DOT_PRODUCT);
		
		assertEquals(normalizeThis.getMagnitude(DistanceMetric.DOT_PRODUCT),1.0f);
		assertEquals(normalizeThis.getAreaAt(-200),secondNorm3); 
		assertEquals(normalizeThis.getAreaAt(-100),secondNorm4);
		assertEquals(normalizeThis.getAreaAt(0),secondNorm3);
		assertEquals(normalizeThis.getAreaAt(100),secondNorm4);
	
		normalizeThis = generateSquarePeaks(norm);
		normalizeThis.posNegNormalize(DistanceMetric.EUCLIDEAN_SQUARED);
		
		assertEquals("Did not normalize properly with city block distance.",
				normalizeThis.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED), 1.0f);
		assertEqualsDelta(normalizeThis.getAreaAt(-200), secondNorm3); 
		assertEqualsDelta(normalizeThis.getAreaAt(-100), secondNorm4);
		assertEqualsDelta(normalizeThis.getAreaAt(0), secondNorm3);
		assertEqualsDelta(normalizeThis.getAreaAt(100), secondNorm4);
		
	}

	/**
	 * @author dmusican
	 */
	public void assertEqualsDelta(float one, float two) {
		final float delta = 0.0000001f;
		assertTrue(one < (two + delta) && one > (two - delta));
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
		assertTrue("Distance too great with city-block.", distance <= 2.0);
		
		//test dot product
		bpl.normalize(DistanceMetric.DOT_PRODUCT);
		other.normalize(DistanceMetric.DOT_PRODUCT);
		distance = bpl.getDistance(other, DistanceMetric.DOT_PRODUCT);
		distance = norm.roundDistance(bpl, other, DistanceMetric.DOT_PRODUCT, distance);
		assertTrue("Distance too great with dot product.", distance <= 2.0);
		
		//test Euclidean squared
		bpl.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
		other.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
		distance = bpl.getDistance(other, DistanceMetric.EUCLIDEAN_SQUARED);
		distance = norm.roundDistance(bpl, other, DistanceMetric.EUCLIDEAN_SQUARED, distance);
		assertTrue("Distance too great with Euclidean squared.", distance <= 2.0);
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
