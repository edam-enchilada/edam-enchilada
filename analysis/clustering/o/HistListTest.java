package analysis.clustering.o;

import junit.framework.TestCase;

public class HistListTest extends TestCase {
	private HistList histogram;
	
	protected void setUp() throws Exception {
		super.setUp();
		histogram = new HistList(1.0f);
	}

	/*
	 * Class under test for void add(int, int)
	 */
	public void testAddintint() {
		histogram.clear();
		histogram.add(100);
		histogram.add(50);
		histogram.incrementBy(5, 5);

		assertEquals(100, (int) histogram.get(0));
		assertEquals( 50, (int) histogram.get(1));
		assertEquals("Extra 0s should be added as spacers.",
				       0, (int) histogram.get(2));
		assertEquals(  5, (int) histogram.get(5));
		assertEquals(  0, (int) histogram.get(15));
		

		
		histogram.incrementBy(0, 1);
		assertEquals("Adding to a specific bin should be additive rather than"
				+ "replacing the old contents of the bin.",
				101, (int) histogram.get(0));

	}

	public void testAddPeak() {
		histogram.clear();
		histogram.addPeak(0.5f);
		histogram.addPeak(0.9f);
		histogram.addPeak(1.2f);
		histogram.addPeak(12.1f);
		
		assertEquals(2, (int) histogram.get(0));
		assertEquals(1, (int) histogram.get(1));
		assertEquals("Adding spacers should still work",
				0, (int) histogram.get(2));
		assertEquals(0, (int) histogram.get(3));
		assertEquals(1, (int) histogram.get(12));
	}

	public void testGet() {
		histogram.clear();
		assertEquals(0, (int) histogram.get(5));
		assertEquals(0, (int) histogram.get(-1));
	}
}
