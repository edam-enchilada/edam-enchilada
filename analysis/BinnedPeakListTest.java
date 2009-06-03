package analysis;

import java.util.ArrayList;

import junit.framework.TestCase;
import static analysis.DistanceMetric.*;

/**
 * 
 * @author smitht
 *
 */
public class BinnedPeakListTest extends TestCase {
	OldBinnedPeakList old1, old2;
	BinnedPeakList new1, new2;
	
	float delta = 0.00001f;
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * @author steinbel
	 * Includes creation of bpl for testing - when this unit test is rewritten
	 * that can be removed to setUp().
	 */
	public void testGetPartialMag(){
		Normalizable norm = new Normalizer(); //testing with only real normalizer
											  //as dummy doesn't do anything
		BinnedPeakList bpl = new BinnedPeakList(norm);
		bpl.add(-200, 3);
		bpl.add(-100, 4);
		bpl.add(0, 3);
		bpl.add(100, 4);
		assertEquals("incorrect negative magnitude for city-block, should be 7", 
				bpl.getPartialMag(DistanceMetric.CITY_BLOCK, true), 7.0f);
		assertEquals("incorrect positive magnitude for city-block, should be 7", 
				bpl.getPartialMag(DistanceMetric.CITY_BLOCK, false), 7.0f);
		assertEquals("incorrect negative magnitude for other, should be 5", 
				bpl.getPartialMag(DistanceMetric.DOT_PRODUCT, true), 5.0f);
		assertEquals ("incorrect positive magnitude for other, should be 5", 
				bpl.getPartialMag(DistanceMetric.EUCLIDEAN_SQUARED, false), 5.0f);
	}
	
	/*
	 *@author steinbel
	 *Temporary tester method until unit test is rewritten.
	 */
	public void testMultiply(){
		BinnedPeakList bpl = new BinnedPeakList();
		bpl.add(-1, (float)-1.0);
		bpl.add(1, (float)1.0);
		bpl.add(2, (float)2.0);
		bpl.add(3, (float)3.0);
		
		bpl.multiply((float)20000);
		
		assertEquals("failed at location -1, value: " + bpl.getAreaAt(-1), bpl.getAreaAt(-1), -20000f);
		assertEquals("failed at location 1, value: " + bpl.getAreaAt(1), bpl.getAreaAt(1), 20000f);
		assertEquals("failed at location 2, value: " + bpl.getAreaAt(2), bpl.getAreaAt(2), 40000f);
		assertEquals("failed at location 3, value: " + bpl.getAreaAt(3), bpl.getAreaAt(3), 60000f);
		
	}

	/*
	 * @author jtbigwoo
	 * adding a quick test for dividing the areas
	 */
	public void testDivideAreasByFloat(){
		BinnedPeakList bpl = new BinnedPeakList();
		bpl.add(-1, (float)-0.001);
		bpl.add(1, (float)0.001);
		bpl.add(2, (float)0.002);
		bpl.add(3, (float)0.003);
		
		bpl.divideAreasBy((float).0005);
		
		assertEquals("failed at location -1, value: " + bpl.getAreaAt(-1), bpl.getAreaAt(-1), -2f);
		assertEquals("failed at location 1, value: " + bpl.getAreaAt(1), bpl.getAreaAt(1), 2f);
		assertEquals("failed at location 2, value: " + bpl.getAreaAt(2), bpl.getAreaAt(2), 4f);
		assertEquals("failed at location 3, value: " + bpl.getAreaAt(3), bpl.getAreaAt(3), 6f);
	}
	
	/**
	 * @author shaferia
	 */
	public void testMultiply2() {
		BinnedPeakList bp1 = new BinnedPeakList(null);
		
		for (int x = -50; x < 50; x += 5)
			bp1.add(x, (float) Math.random());
		
		bp1.multiply(Float.MAX_VALUE);

		for (BinnedPeak bp : bp1) {
			assertTrue(bp.getValue() != Float.POSITIVE_INFINITY && bp.getValue() != Float.NEGATIVE_INFINITY);
		}
		
		float[] comp = new float[50];
		for (int x = 0; x < comp.length; ++x)
			comp[x] = ((float) Math.random()) * (x - 25);
		
		bp1 = new BinnedPeakList(null);
		BinnedPeakList bp2 = new BinnedPeakList(null);
		
		for (int x = -25; x < 25; ++x) {
			bp1.add(x, comp[x + 25]);
			bp2.add(new BinnedPeak(x, comp[x + 25]));
		}
		
		//multiply to infinity with float multipliers
		for (int x = 0; x < 500; ++x) {
			float multiplier = ((x << (Integer.SIZE - 1)) >>> (Integer.SIZE - 2) == 0) ?
					((float) Math.random()) * 5 :
					(float) (Math.random() * 5);
			for (int i = 0; i < comp.length; ++i)
				comp[i] *= multiplier;
			bp1.multiply(multiplier);
			bp2.multiply(multiplier);
			
			int i = 0;
			for (BinnedPeak bp : bp1) {
				assertEquals(bp.getKey(), i - 25);
				assertEquals(bp.getValue(), comp[i]);
				i += 1;
			}
			
			i = 0;
			for (BinnedPeak bp : bp2) {
				assertEquals(bp.getKey(), i - 25);
				assertEquals(bp.getValue(), comp[i]);
				i += 1;
			}
		}
		
		//multiply to infinity with int multipliers
		for (int x = 0; x < 500; ++x) {
			float multiplier = ((x << (Integer.SIZE - 1)) >>> (Integer.SIZE - 2) == 0) ?
					((int) Math.random()) * 10 :
					(int) (Math.random() * 10);
			for (int i = 0; i < comp.length; ++i)
				comp[i] *= multiplier;
			bp1.multiply(multiplier);
			bp2.multiply(multiplier);
			
			int i = 0;
			for (BinnedPeak bp : bp1) {
				assertEquals(bp.getKey(), i - 25);
				assertEquals(bp.getValue(), comp[i]);
				i += 1;
			}
			
			i = 0;
			for (BinnedPeak bp : bp2) {
				assertEquals(bp.getKey(), i - 25);
				assertEquals(bp.getValue(), comp[i]);
				i += 1;
			}
		}
	}
	
	public void testAdd() {
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-250, (float) 1);
		bp1.add(-200, (float) 1);
		bp1.add(-95, (float) 1);
		bp1.add(-25, (float) 1);
		bp1.add(30, (float) 1);
		bp1.add(100, (float) 1);
		bp1.add(125, (float) 1);
		//null peaks should never appear in the code, if they appear, an assert should fire
	//	bp1.peaks.put(180, null);
		
		bp1.add(-95, (float) 1);
		bp1.add(-25, (float) 1);
		bp1.add(30, (float) 1);
		bp1.add(100, (float) 1);
		bp1.add(180, (float) 1);

		assertEquals(bp1.peaks.get(-250), (float) 1);
		assertEquals(bp1.peaks.get(-200), (float) 1);
		assertEquals(bp1.peaks.get(-95), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(-25), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(30), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(100), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(125), (float) 1);
	}
	public void testAddWeightedParticle() {
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-250, (float) 25);
		bp1.add(-200, (float) 20);
		bp1.add(-150, (float) 15);
		bp1.add(-100, (float) 10);
		bp1.add(-50, (float) 5);
		bp1.add(0, (float) 1);
		bp1.add(50, (float) 5);
		bp1.add(100, (float) 10);
		bp1.add(150, (float) 15);
		bp1.add(200, (float) 20);
		bp1.add(250, (float) 25);
		bp1.printPeakList();
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-170, (float) 17);
		bp2.add(-70, (float) 7);
		bp2.add(-50, (float) 5);
		bp2.add(0, (float) 1);
		bp2.add(50, (float) 5);
		bp2.add(70, (float) 7);
		bp2.add(170, (float) 17);
		
		bp1.addWeightedParticle3(bp2, 2);
		
		bp1.printPeakList();
	}
	public void testEmptyList() {
		BinnedPeakList bpl = new BinnedPeakList(new Normalizer());
		BinnedPeakList bpl2 = new BinnedPeakList(new Normalizer());
		
		bpl.addAnotherParticle(bpl2);
		
		assertEquals(0, bpl.length());
		assertFalse(bpl.iterator().hasNext());
		
		bpl.divideAreasBy(3);
		bpl.multiply(3);
		assertEquals(0.0f, bpl.getDistance(bpl2, CITY_BLOCK));
		bpl.divideAreasBy(3f);
		bpl.multiply(3f);
		assertEquals(0.0f, bpl.getDistance(bpl2, CITY_BLOCK));
		bpl2.add(3, .2f);
		assertEquals(.2f, bpl.getDistance(bpl2, CITY_BLOCK));
		
		bpl.normalize(CITY_BLOCK); // what should this actually do?
		
	}
	
	public void testGetDistance2() {
		float location;
		float area;
		
		Normalizer n = new Normalizer();
		for (int i = 0; i < 1000; i++) {
			old1 = new OldBinnedPeakList();
			old2 = new OldBinnedPeakList();
			
			new1 = new BinnedPeakList(n);
			new2 = new BinnedPeakList(n);
			
			int numPeaks = 40;
			
			for (int peaks = 0; peaks < numPeaks; peaks++) {
				location = (float) (Math.random() - 0.5) * 4500;
				area = (float) Math.random();
				if (Math.random() > 0.5) {
					old1.add(location, area);
					new1.add(location, area);
				} else {
					old2.add(location, area);
					new2.add(location, area);
				}
			}
			
			old1.divideAreasBy(numPeaks);
			old2.divideAreasBy(numPeaks);
			new1.divideAreasBy(numPeaks);
			new2.divideAreasBy(numPeaks);
			
			assertEquals(old1.getMagnitude(CITY_BLOCK), new1.getMagnitude(CITY_BLOCK), 0.0001);
			assertEquals(old2.getMagnitude(CITY_BLOCK), new2.getMagnitude(CITY_BLOCK), 0.0001);
			
			assertEquals(old1.getDistance(old2, EUCLIDEAN_SQUARED),
					new1.getDistance(new2, EUCLIDEAN_SQUARED), delta);
			assertEquals(old1.getDistance(old2, EUCLIDEAN_SQUARED),
					old2.getDistance(old1, EUCLIDEAN_SQUARED), delta);
			assertEquals(new1.getDistance(new2, EUCLIDEAN_SQUARED),
					new2.getDistance(new1, EUCLIDEAN_SQUARED), delta);
			
			assertEquals(old1.getDistance(old2, DOT_PRODUCT),
					new1.getDistance(new2, DOT_PRODUCT), delta);
			assertEquals(old1.getDistance(old2, DOT_PRODUCT),
					old2.getDistance(old1, DOT_PRODUCT), delta);
			assertEquals(new1.getDistance(new2, DOT_PRODUCT),
					new2.getDistance(new1, DOT_PRODUCT), delta);	
			
			assertEquals(old1.getDistance(old2, CITY_BLOCK),
					new1.getDistance(new2, CITY_BLOCK), delta);
			assertEquals(old1.getDistance(old2, CITY_BLOCK),
					old2.getDistance(old1, CITY_BLOCK), delta);
			assertEquals(new1.getDistance(new2, CITY_BLOCK),
					new2.getDistance(new1, CITY_BLOCK), delta);
			
		}
	}

	/**
	 * Get testing BinnedPeak data
	 * @param size the number of (key, value) pairs to return
	 * @param keyMag the maximum magnitude of peak location (keys)
	 * @param valMag the maximum magnitude of peak area (values)
	 * @param zeroKeys whether any keys should be zero
	 * @param zeroVals whether any values should be zero
	 * @return an unordered list of peaks
	 * @author shaferia
	 */
	private BinnedPeak[] getTestData(
			int size,
			int keyMag,
			float valMag,
			boolean zeroKeys, 
			boolean zeroVals) {
		BinnedPeak[] data = new BinnedPeak[size];
		
		int key;
		float val;
		for (int i = 0; i < data.length; ++i) {
			do {
				key = ((int) (Math.random() * keyMag * 2)) - keyMag;
				val = ((float) (Math.random() * valMag * 2)) - valMag;
			} 
			while (key == 0 && val == 0f); //highly unlikely, but just to be sure.
			
			data[i] = new BinnedPeak(key, val);
		}
		
		//stick in a few zero keys, zero values, and a combination thereof
		int sprinkle = Math.max(1, size / 10);
		
		if (zeroKeys) {
			for (int i = 0; i < sprinkle; ++i) {
				data[(int) (Math.random() * data.length)].setKey(0);
			}
		}
		
		if (zeroVals) {
			for (int i = 0; i < sprinkle; ++i) {
				data[(int) (Math.random() * data.length)].setValue(0);
			}			
		}
		
		if (zeroKeys && zeroVals) {
			for (int i = 0; i < sprinkle; ++i) {
				data[(int) (Math.random() * data.length)].setKey(0);
				data[(int) (Math.random() * data.length)].setValue(0);
			}				
		}
		
		return data;
	}
	
	/**
	 * Ensure what we're testing with is what we think it is
	 * @author shaferia
	 */
	public void testGetTestData() {
		boolean zeroKeys = false;
		boolean zeroVals = false;
		boolean zeroBoth = false;
		float maxKey = 0f, maxVal = 0f;
		BinnedPeak[] data = getTestData(100, 10, 0.005f, true, true);
		assertEquals(data.length, 100);
		for (BinnedPeak b : data) {
			zeroKeys |= b.getKey() == 0;
			zeroVals |= b.getValue() == 0;
			zeroBoth |= (b.getKey() == 0) && (b.getValue() == 0);
			maxKey = Math.max(maxKey, b.getKey());
			maxVal = Math.max(maxVal, b.getValue());
		}
		assertTrue(zeroKeys && zeroVals && zeroBoth);
		assertTrue(maxKey <= 10);
		assertTrue(maxVal <= 0.005f);
		
		maxKey = maxVal = 0f;
		zeroKeys = zeroVals = zeroBoth = false;
		data = getTestData(10, 10, 100f, true, false);
		assertEquals(data.length, 10);
		for (BinnedPeak b : data) {
			zeroKeys |= b.getKey() == 0;
			zeroVals |= b.getValue() == 0;
			zeroBoth |= (b.getKey() == 0) && (b.getValue() == 0);
			maxKey = Math.max(maxKey, b.getKey());
			maxVal = Math.max(maxVal, b.getValue());
		}
		assertTrue(zeroKeys && !zeroVals && !zeroBoth);
		assertTrue(maxKey <= 10);
		assertTrue(maxVal <= 100f);
		
		maxKey = maxVal = 0f;
		zeroKeys = zeroVals = zeroBoth = false;
		data = getTestData(2345, 10, 100f, true, false);
		assertEquals(data.length, 2345);
		for (BinnedPeak b : data) {
			zeroKeys |= b.getKey() == 0;
			zeroVals |= b.getValue() == 0;
			zeroBoth |= (b.getKey() == 0) && (b.getValue() == 0);
			maxKey = Math.max(maxKey, b.getKey());
			maxVal = Math.max(maxVal, b.getValue());
		}
		assertTrue(zeroKeys && !zeroVals && !zeroBoth);
		assertTrue(maxKey <= 10);
		assertTrue(maxVal <= 100f);
		
		maxKey = maxVal = 0f;
		zeroKeys = zeroVals = zeroBoth = false;
		data = getTestData(12, 10000, 100f, false, false);
		assertEquals(data.length, 12);
		for (BinnedPeak b : data) {
			zeroKeys |= b.getKey() == 0;
			zeroVals |= b.getValue() == 0;
			zeroBoth |= (b.getKey() == 0) && (b.getValue() == 0);
			maxKey = Math.max(maxKey, b.getKey());
			maxVal = Math.max(maxVal, b.getValue());
		}
		assertTrue(!zeroKeys && !zeroVals && !zeroBoth);
		assertTrue(maxKey <= 10000);
		assertTrue(maxVal <= 100f);
	}
	
	/**
	 * @author shaferia
	 */
	public void testCopyBinnedPeakList() {
		BinnedPeakList one = new BinnedPeakList();
		
		BinnedPeak[] data = getTestData(1000, 200, 200f, true, true);
		for (BinnedPeak b : data)
			one.add(b);
		
		BinnedPeakList two = new BinnedPeakList();
		two.copyBinnedPeakList(one);
		
		assertTrue(one.comparePeakLists(two));
		
		one.setNormalizer(new Normalizer());
		assertTrue(one.comparePeakLists(two));
		
		one = new BinnedPeakList();
		two.copyBinnedPeakList(one);
		assertTrue(one.comparePeakLists(two));
	}
	
	/**
	 * @author shaferia
	 */
	public void testContainsZeros() {
		BinnedPeakList bpl = new BinnedPeakList();
		
		bpl.add(0, 2f);
		bpl.add(1, 3f);
		bpl.add(3, 4f);
		assertFalse(bpl.containsZeros());
		
		bpl.add(4, 0f);
		assertTrue(bpl.containsZeros());
		
		bpl.add(4.05f, 0f);
		bpl.add(4.1f, 0f);
		assertTrue(bpl.containsZeros());
		
		bpl.add(4, 1f);
		assertFalse(bpl.containsZeros());
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetFilteredZerosList() {
		BinnedPeakList one = new BinnedPeakList();
		BinnedPeak[] data = getTestData(1000, 1000, 20f, true, true);
		
		int numZeroVals = 0;
		for (BinnedPeak b : data)
			one.add(b);
		
		for (BinnedPeak b : one)
			if (b.getValue() == 0)
				++numZeroVals;
		
		BinnedPeakList nz = one.getFilteredZerosList();
		assertFalse(nz.containsZeros());
		
		assertEquals(nz.getPeaks().size(), one.getPeaks().size() - numZeroVals);
		assertTrue(numZeroVals > 0);
		
		data = new BinnedPeak[100];
		for (int i = 0; i < data.length; ++i)
			data[i] = new BinnedPeak(i, 0f);
		
		one = new BinnedPeakList();
		for (BinnedPeak b : data)
			one.add(b);
		
		nz = one.getFilteredZerosList();
		assertFalse(nz.containsZeros());
		assertEquals(nz.getPeaks().size(), 0);
	}
	
	/**
	 * @author shaferia
	 */
	public void testIsNormalized() {
		int[] sizes = {1, 20, 100, 1000};
		float[] magnitudes = {1f, 10f, 100f, 1000f};
		
		int sumerrors = 0;
		
		for (int size : sizes)
			for (float magnitude : magnitudes)
				for (DistanceMetric metric : DistanceMetric.values()) {
					BinnedPeakList bpl = new BinnedPeakList();
					BinnedPeak[] data = getTestData(size, 100, magnitude, true, true);
					
					for (BinnedPeak b : data)
						bpl.add(b);
					
					//in theory, there's a tiny chance the data already has zero magnitude.
					if (bpl.isNormalized(metric));
						bpl.add(new BinnedPeak(100, 3f));
					
					assertFalse(bpl.isNormalized(DistanceMetric.CITY_BLOCK));
					assertFalse(bpl.isNormalized(DistanceMetric.EUCLIDEAN_SQUARED));
					assertFalse(bpl.isNormalized(DistanceMetric.DOT_PRODUCT));
					
					bpl.normalize(metric);
					
					//System.out.printf("Normalized Magnitude: %s (%s, %d elements)\n",
					//		bpl.getMagnitude(metric),
					//		metric.name(),
					//		size);
					
					assertTrue(bpl.isNormalized(metric));
				}
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetMagnitude() {
		int[] sizes = {1, 20, 100, 1000};
		float[] magnitudes = {1f, 10f, 100f, 1000f};
		
		for (int size : sizes)
			for (float magnitude : magnitudes) {
				BinnedPeakList bpl = new BinnedPeakList();
				BinnedPeak[] data = getTestData(size, 100, magnitude, true, true);
				for (BinnedPeak b : data)
					bpl.add(b);
				
				//calculate the sum and standard deviation of the data we made
				float sum = 0f;
				float stdDev = 0f;
				for (BinnedPeak b : bpl) {
					sum += b.getValue();
					stdDev += b.getValue() * b.getValue();
				}
				stdDev = (float) Math.sqrt(stdDev);
				
				assertEquals(bpl.getMagnitude(DistanceMetric.CITY_BLOCK), sum, 0.0001f);
				assertEquals(bpl.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED), stdDev, 0.0001f);
				assertEquals(bpl.getMagnitude(DistanceMetric.DOT_PRODUCT), stdDev, 0.0001f);
			}
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetAreaAt() {
		BinnedPeakList bpl = new BinnedPeakList();
		BinnedPeak[] data = new BinnedPeak[50];
		
		for (int i = 0; i < data.length; ++i)
			data[i] = new BinnedPeak(i + 1, (float) Math.random() * 100);
		
		for (BinnedPeak b : data)
			bpl.add(b);
		
		for (BinnedPeak b : data)
			assertEquals(bpl.getAreaAt(b.getKey()), b.getValue(), 0.0001f);
		
		//look for some values that shouldn't be there: area should be zero.
		int[] vals = {200, 10000, -1000, -113, -1424, 42523, 233};
		for (int i : vals)
			assertEquals(bpl.getAreaAt(i), 0, 0.0001f);
	}
	
	/**
	 * @author shaferia
	 */
	public void testLength() {
		BinnedPeakList bpl = new BinnedPeakList();
		bpl.add(1, 1f);
		assertEquals(bpl.length(), 1);
		
		bpl.add(2, 1f);
		assertEquals(bpl.length(), 2);
		
		bpl.add(1, 2f);
		assertEquals(bpl.length(), 2);
		
		bpl.add(4, 3f);
		assertEquals(bpl.length(), 3);
		
		bpl = new BinnedPeakList();
		assertEquals(bpl.length(), 0);
		
		BinnedPeak[] data = getTestData(1000, 100, 100f, true, true);
		
		for (BinnedPeak b : data)
			bpl.add(b);
		
		assertTrue(bpl.length() <= 1000);
	}
	
	/**
	 * @author shaferia
	 */
	public void testDivideAreasBy() {
		int[] factors = {0, 1, 2, 3, 34, 467};
		
		for (int factor : factors) {
			BinnedPeak[] data = getTestData(1000, 100, 100f, true, true);
			BinnedPeakList bpl = new BinnedPeakList();
			
			for (BinnedPeak b : data)
				bpl.add(b);
			
			BinnedPeakList divided = new BinnedPeakList();
			
			for (BinnedPeak b : bpl)
				divided.add(b.getKey(), b.getValue() / factor);
			
			bpl.divideAreasBy(factor);
			
			assertTrue(bpl.comparePeakLists(divided));
		}
		
		//make sure this doesn't explode
		BinnedPeakList bpl = new BinnedPeakList();
		bpl.divideAreasBy(2);
	}
	
	private class OldBinnedPeakList {

		private ArrayList<Integer> locations;
		private ArrayList<Float> areas;
		int position = -1;

		private static final int MAX_LOCATION = 2500;
		private int DOUBLE_MAX = MAX_LOCATION * 2;
		private float[] longerLists = new float[MAX_LOCATION * 2];
		/**
		 * A constructor for the peaklist, initializes the underlying
		 * ArrayLists to a size of 20.
		 */
		public OldBinnedPeakList()
		{
			locations = new ArrayList<Integer>(20);
			areas = new ArrayList<Float>(20);
		}
		
		public float getMagnitude(DistanceMetric dMetric)
		{
			float magnitude = 0;
			
			resetPosition();
			//if (list.length() == 0)
			//{
			//	BinnedPeakList returnThis = new BinnedPeakList();
			//	returnThis.add(0.0f,1.0f);
			//	return returnThis;
			//}
			if (dMetric == DistanceMetric.CITY_BLOCK)
				for (int i = 0; i < length(); i++)
				{
					magnitude += getNextLocationAndArea().getValue();
				}
			else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED ||
			         dMetric == DistanceMetric.DOT_PRODUCT)
			{
				float currentArea;
				for (int i = 0; i < length(); i++)
				{
					currentArea = getNextLocationAndArea().getValue();
					magnitude += currentArea*currentArea;
				}
				magnitude = (float) Math.sqrt(magnitude);
			}
			resetPosition();
			return magnitude;
		}
		
		// TODO: Update this to the real thing
		public float getDistance(OldBinnedPeakList toList, DistanceMetric dMetric)
		{
//			TODO: Make this more graceful
			
			//This seems to take a 2 seconds longer?
			//Arrays.fill(longerLists, 0.0f);
			
			resetPosition();
			toList.resetPosition();
			
		    // longerLists keeps track of which peak locations have nonzero areas
			for (int i = 0; i < DOUBLE_MAX; i++)
			{
				longerLists[i] = 0;
			}
			float distance = 0;
			OldBinnedPeakList longer;
			OldBinnedPeakList shorter;
			resetPosition();
			toList.resetPosition();
			if (length() < toList.length())
			{
				shorter = this;
				longer = toList;
			}
			else
			{
				longer = this;
				shorter = toList;
			}
			
			BinnedPeak temp;
			
			for (int i = 0; i < longer.length(); i++)
			{
				temp = longer.getNextLocationAndArea();
				longerLists[temp.getKey() + MAX_LOCATION] = temp.getValue();
				//Do we need this?: - nope
				//bCheckedLocs[temp.location + MAX_LOCATION] = true;

				// Assume optimistically that each key is unmatched in the
				// shorter peak list.
				if (dMetric == DistanceMetric.CITY_BLOCK)
				    distance += temp.getValue();
				else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
					distance += temp.getValue()*temp.getValue();
				else if (dMetric == DistanceMetric.DOT_PRODUCT)
				    ; // If no match in shorter list, contributes nothing
				else {
				    fail("Invalid distance metric: " + dMetric);
					distance = -1.0f;
				}
			}	
			
			shorter.resetPosition();
			longer.resetPosition();
			float eucTemp = 0;
			for (int i =  0; i < shorter.length(); i++)
			{
				temp = shorter.getNextLocationAndArea();
				if (longerLists[temp.getKey()+MAX_LOCATION] != 0)
				{
					if (dMetric == DistanceMetric.CITY_BLOCK)
					{
						distance -= longerLists[temp.getKey()+MAX_LOCATION];
					}
					else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
					{
						distance -= longerLists[temp.getKey()+MAX_LOCATION]*
							longerLists[temp.getKey()+MAX_LOCATION];
					}
					else if (dMetric == DistanceMetric.DOT_PRODUCT)
					    ; // Again, nothing to subtract off here
					else {
					    fail("Invalid distance metric: " + dMetric);
						distance = -1.0f;
					}
					
					if (dMetric == DistanceMetric.CITY_BLOCK)
						distance += Math.abs(temp.getValue()-longerLists[temp.getKey()+MAX_LOCATION]);
					else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
					{
						eucTemp = temp.getValue()-longerLists[temp.getKey()+MAX_LOCATION];
						distance += eucTemp*eucTemp;
					}
					else if (dMetric == DistanceMetric.DOT_PRODUCT) {
					    distance +=
					        temp.getValue()*longerLists[temp.getKey()+MAX_LOCATION];
					}
					else {
					    fail("Invalid distance metric: " + dMetric);
						distance = -1.0f;
					}
					
				}
				else
				{
					if (dMetric == DistanceMetric.CITY_BLOCK)
						distance += temp.getValue();
					else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
						distance += temp.getValue()*temp.getValue();
					else if (dMetric == DistanceMetric.DOT_PRODUCT)
					    ; // Nothing to add here if new match
					else {
					    fail("Invalid distance metric: " + dMetric);
						distance = -1.0f;
					}
				}
				
			}
			
			// Dot product distance actually ranges from 0 to 1 (since data is
			// normalized). A value of 1 indicates two points are the same, 0
			// indicates completely different. In order to make rest of code work
			// (small distance is considered good), negate distance and 1 to it.
			// This places distance between 0 and 1 like other measures and doesn't
			// affect anything else. (Admittedly, this is a hack, but dot product
			// distance is ultimately the same thing as Euclidean squared anyway).
			if (dMetric == DistanceMetric.DOT_PRODUCT)
			    distance = 1-distance;

			assertTrue("Distance should be <= 2.0, actually is " + distance +"\n" 
				    + "Magnitudes: toList = " + toList.getMagnitude(dMetric) + " this = "
				    + getMagnitude(dMetric) + "\n", distance < 2.01 );

			if (distance > 2) {
				//System.out.println("Rounding off " + distance +
				//		"to 2.0");
				distance = 2.0f;
			}
			
			return distance;
		}
		
		/**
		 * Retrieve the value of the peaklist at a given key
		 * @param key	The key of the value you wish to
		 * 					retrieve.
		 * @return			The value at the given key.
		 */
		public float getAreaAt(int location)
		{
			Integer temp = null;
			for (int i = 0; i < locations.size(); i++)
			{
				if (locations.get(i).intValue() == location)
					return areas.get(i).floatValue();
			}
			return 0;
		}
		
		/**
		 * Add a regular peak to the peaklist.  This actually involves
		 * quite a bit of processing.  First, each float key is
		 * rounded to its nearest integer value.  Then, that key
		 * is checked in the current peak to see if it already exists.
		 * If it does, it adds the value of the new peak to the 
		 * preexisting value.  This is done so that when you have two
		 * peaks right next to eachother (ie 1.9999 and 2.0001) that
		 * probably should be both considered the same element, the
		 * signal is doubled.  
		 * 
		 * @param key
		 * @param value
		 */
		public void add(float location, float area)
		{
			assertTrue("Location to add is out of bounds" + location, 
					(location < MAX_LOCATION && location > - MAX_LOCATION));
			float temp = 0;
			boolean exists = false;
			int locationInt;
			
			// If the key is positive or zero, then add 0.5 to round.
			// Otherwise, subtract 0.5 to round.
			if (location >= 0.0f)
				locationInt = (int) ((float) location + 0.5);
			else
				locationInt = (int) ((float) location - 0.5);
			
			for (int i = 0; i < locations.size(); i++)
			{
				if(locations.get(i).intValue() == locationInt)
				{
					temp = areas.get(i).floatValue() + area;
					areas.set(i,new Float(temp));
					exists = true;
					return;
				}
			}
			if (!exists)
			{
				locations.add(new Integer(locationInt));
				areas.add(new Float(area));
			}
		}
		
		/**
		 * Returns the number of locations represented by this 
		 * Binned peaklist
		 * @return the number of locations in the list
		 */
		public int length()
		{
			return locations.size();
		}
		
		/**
		 * This skips all the checks of add().  Do not use this unless
		 * you are copying from another list, not taking care to make
		 * sure that you are not adding duplicate locations can result
		 * in undesired behavior!!!!
		 * @param key	The key of the peak
		 * @param value	The value of the peak at that key.
		 */
		public void addNoChecks(int location, float area)
		{
			assertTrue("key is out of bounds: " + location, 
					(location < MAX_LOCATION && location > - MAX_LOCATION));
			//peaks.add(new BinnedPeak(key,value));
			locations.add(new Integer(location));
			areas.add(new Float(area));
		}
		
		/**
		 * Reset this peaklist to the beginning for the 
		 * getNextLocationAndArea function.
		 *
		 */
		public void resetPosition() 
		{
			position = -1;
		}
		
		/**
		 * Returns a BinnedPeak representing the next peak in the list.
		 * @return the next peak.
		 */
		public BinnedPeak getNextLocationAndArea()
		{
			position++;
			return new BinnedPeak(locations.get(position).intValue(), 
					areas.get(position).floatValue());
			//return peaks.get(position);
			
		}
		
		public void divideAreasBy(int divisor) {
			for (int i = 0; i < areas.size(); i++)
			{
				areas.set(i, new Float(areas.get(i).floatValue() / divisor));
			}
		}
			
		public void printPeakList() {
			System.out.println("printing peak list");
			boolean exception = false;
			int counter = 0;
			resetPosition();
			BinnedPeak p;
			while (!exception) {
				try {
					p = getNextLocationAndArea();
					System.out.println(p.getKey() + ", " + p.getValue());
				}catch (Exception e) {exception = true;}
			}
			resetPosition();
		}
		
		public int getLastLocation() {
			int lastLoc = -30000;
			for (int i = 0; i < locations.size(); i++) 
				if (locations.get(i).intValue() > lastLoc)
					lastLoc = locations.get(i).intValue();
			return lastLoc;
		}
		
		public int getFirstLocation() {
			int firstLoc = 30000;
			for (int i = 0; i < locations.size(); i++) 
				if (locations.get(i).intValue() < firstLoc)
					firstLoc = locations.get(i).intValue();
			return firstLoc;
		}
		
		public float getLargestArea() {
			float largestArea = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < locations.size(); i++) 
				if (areas.get(i).floatValue() > largestArea)
					largestArea = areas.get(i).floatValue();
			return largestArea;
		}
		
		public void addAnotherParticle(OldBinnedPeakList peaks) {
			BinnedPeak peak = null;
			for (int i = 0; i < peaks.length(); i++) {
				peak = new BinnedPeak(peaks.locations.get(i).intValue(), 
						peaks.areas.get(i).floatValue());
				add(peak.getKey(), peak.getValue());
			}
		}
	}
}
