package analysis;

import java.util.Iterator;

import junit.framework.TestCase;

public class NewBinnedPeakListTest extends TestCase {
	BinnedPeakList a;
	NewBinnedPeakList b;
	
	BinnedPeakList compA;
	NewBinnedPeakList compB;
	
	// these are pulled out of the air
	float[] locations = {1.2f,  5.5f,  99.9f, -32f, -45.7f};
	float[] areas =     {0.02f, 0.08f, 0.1f,   0.5f, 0.3f};
	
	protected void setUp() throws Exception {
		super.setUp();
		
		a = new BinnedPeakList();
		b = new NewBinnedPeakList();
		
		compA = new BinnedPeakList();
		compB = new NewBinnedPeakList();
		
		for (int i = 0; i < locations.length; i++) {
			a.add(locations[i], areas[i]);
			b.add(locations[i], areas[i]);
			
			compA.add(locations[i]+i, areas[locations.length - (i+1)]);
			compB.add(locations[i]+i, areas[locations.length - (i+1)]);
		}
	}

	public void testGetMagnitude() {
		assertEquals(a.getMagnitude(DistanceMetric.CITY_BLOCK),
				b.getMagnitude(DistanceMetric.CITY_BLOCK));
		assertEquals(a.getMagnitude(DistanceMetric.DOT_PRODUCT),
				b.getMagnitude(DistanceMetric.DOT_PRODUCT));
		assertEquals(a.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED),
				b.getMagnitude(DistanceMetric.EUCLIDEAN_SQUARED));
	}

	public void testGetDistance() {
		assertEquals(a.getDistance(compA, DistanceMetric.CITY_BLOCK),
				b.getDistance(compB, DistanceMetric.CITY_BLOCK));
		assertEquals(a.getDistance(compA, DistanceMetric.DOT_PRODUCT),
				b.getDistance(compB, DistanceMetric.DOT_PRODUCT));
		assertEquals(a.getDistance(compA, DistanceMetric.EUCLIDEAN_SQUARED),
				b.getDistance(compB, DistanceMetric.EUCLIDEAN_SQUARED));
	}

	public void testGetAreaAt() {
		assertEquals(a.getAreaAt(1), b.getAreaAt(1));
		assertEquals(a.getAreaAt(5), b.getAreaAt(5));
		assertEquals(a.getAreaAt(10), b.getAreaAt(10));
		assertEquals(a.getAreaAt(-100), b.getAreaAt(-100));
		assertEquals(a.getAreaAt(-32), b.getAreaAt(-32));
	}

	public void testAdd() {
		a.add(21.3f, 1.1f);
		b.add(21.3f, 1.1f);
		// these next two should get added to other bins
		a.add(1.3f, 2.2f);
		b.add(1.3f, 2.2f);
		assertBPLEquals(a, b);
	}

	public void testLength() {
		assertEquals(a.length(), b.length());
	}

	public void testAddNoChecks() {
		// the new version shouldn't need this.
	}

	public void testGetNextStuff() {
		a.resetPosition();
		Iterator<BinnedPeak> iter = a.iterator();
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());

		
		a.resetPosition();
		iter = b.iterator();
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(a.getNextLocationAndArea(),
				iter.next());
		
		b.resetPosition();
		iter = a.iterator();
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());

		b.resetPosition();
		iter = b.iterator();
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());
		assertBinnedPeakEquals(b.getNextLocationAndArea(),
				iter.next());

		iter = a.iterator();
		Iterator<BinnedPeak> i2 = b.iterator();
		while (iter.hasNext()) {
			assertEquals(iter.hasNext(), i2.hasNext());
			assertBinnedPeakEquals(iter.next(), i2.next());
		}
		assertEquals(iter.hasNext(), i2.hasNext());
	
	}

	public void testDivideAreasBy() {
		a.divideAreasBy(3);
		b.divideAreasBy(3);
		assertBPLEquals(a, b);
	}

	public void testGetLastLocation() {
		assertEquals(a.getLastLocation(), b.getLastLocation());
	}

	public void testGetFirstLocation() {
		assertEquals(a.getFirstLocation(), b.getFirstLocation());
	}

	public void testGetLargestArea() {
		assertEquals(a.getLargestArea(), b.getLargestArea());
	}

	public void testAddAnotherParticle() {
		a.addAnotherParticle(compA);
		b.addAnotherParticle(compB);
		assertBPLEquals(a,b);
	}

	public void assertBPLEquals(BinnedPeakList foo, NewBinnedPeakList bar) {
		Iterator<BinnedPeak> i = foo.iterator();
		Iterator<BinnedPeak> iter = bar.iterator();
		while (i.hasNext()) {
			assertTrue(iter.hasNext());
			assertBinnedPeakEquals(i.next(),
					iter.next());
		}
		assertFalse(iter.hasNext());
	}
	
	public void assertBinnedPeakEquals(BinnedPeak u, BinnedPeak v) {
		assertEquals(u.location, v.location);
		assertEquals(u.area, v.area, 0.001f);
	}
	
}
