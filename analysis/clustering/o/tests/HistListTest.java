package analysis.clustering.o.tests;

import analysis.clustering.o.HistList;
import junit.framework.TestCase;

public class HistListTest extends TestCase {
	private HistList histogram;
	
	protected void setUp() throws Exception {
		super.setUp();
		histogram = new HistList(1.0f);
	}

	public void testZeroNiceness() {
		histogram.clear();
		histogram.setParticleCount(100);
		histogram.addPeak(0.5f);
		histogram.addPeak(1.7f);
		histogram.addPeak(2.3f);
		histogram.addPeak(22.5f);
		assertEquals(96, (int) histogram.get(0));
		
		histogram.clear();
		histogram.addPeak(0.5f);
		histogram.addPeak(1.7f);
		histogram.addPeak(2.3f);
		histogram.addPeak(22.5f);
		histogram.setParticleCount(100);
		assertEquals(96, (int) histogram.get(0));
	}
	
	public void testAdditivity() {

		histogram.clear();
		histogram.addPeak(0.5f);
		histogram.addPeak(0.5f);
		histogram.addPeak(1.5f);
		assertEquals("Adding to a specific bin should be additive rather than"
				+ "replacing the old contents of the bin.",
				2, (int) histogram.get(0.5f));
	}

	public void testAddPeak() {
		histogram.clear();
		histogram.addPeak(0.5f);
		histogram.addPeak(0.9f);
		histogram.addPeak(1.2f);
		histogram.addPeak(12.1f);
		
		assertEquals(2, (int) histogram.get(1));
		assertEquals(1, (int) histogram.get(2));
		assertEquals("Adding spacers should still work",
				0, (int) histogram.get(3));
		assertEquals(0, (int) histogram.get(4));
		assertEquals(1, (int) histogram.get(13));
	}

	public void testGet() {
		histogram.clear();
		assertEquals(0, (int) histogram.get(5));
		assertEquals(0, (int) histogram.get(-1));
	}
}
