package analysis;

import junit.framework.TestCase;

public class BinnedPeakListTest extends TestCase {
	OldBinnedPeakList old1, old2;
	BinnedPeakList new1, new2;
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGetDistance() {
		float location;
		float area;
		for (int i = 0; i < 1000; i++) {
			old1 = new OldBinnedPeakList();
			old2 = new OldBinnedPeakList();
			
			new1 = new BinnedPeakList();
			new2 = new BinnedPeakList();
			
			for (int peaks = 0; peaks < 40; peaks++) {
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
			assertEquals(old1.getDistance(old2, DistanceMetric.EUCLIDEAN_SQUARED),
					new1.getDistance(new2, DistanceMetric.EUCLIDEAN_SQUARED));
			assertEquals(old1.getDistance(old2, DistanceMetric.EUCLIDEAN_SQUARED),
					old2.getDistance(old1, DistanceMetric.EUCLIDEAN_SQUARED));
			assertEquals(new1.getDistance(new2, DistanceMetric.EUCLIDEAN_SQUARED),
					new2.getDistance(new1, DistanceMetric.EUCLIDEAN_SQUARED));
			
			assertEquals(old1.getDistance(old2, DistanceMetric.DOT_PRODUCT),
					new1.getDistance(new2, DistanceMetric.DOT_PRODUCT));
			assertEquals(old1.getDistance(old2, DistanceMetric.DOT_PRODUCT),
					old2.getDistance(old1, DistanceMetric.DOT_PRODUCT));
			assertEquals(new1.getDistance(new2, DistanceMetric.DOT_PRODUCT),
					new2.getDistance(new1, DistanceMetric.DOT_PRODUCT));
			
			
			assertEquals(old1.getDistance(old2, DistanceMetric.CITY_BLOCK),
					new1.getDistance(new2, DistanceMetric.CITY_BLOCK));
			assertEquals(old1.getDistance(old2, DistanceMetric.CITY_BLOCK),
					old2.getDistance(old1, DistanceMetric.CITY_BLOCK));
			assertEquals(new1.getDistance(new2, DistanceMetric.CITY_BLOCK),
					new2.getDistance(new1, DistanceMetric.CITY_BLOCK));
			
		}
	}
}
