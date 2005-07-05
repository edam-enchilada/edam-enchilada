package analysis.clustering.o.tests;

import java.util.*;

import analysis.BinnedPeakList;
import analysis.clustering.o.StatSummary;

import junit.framework.TestCase;

public class StatSummaryTest extends TestCase {
	Random r;
	List<BinnedPeakList> atoms;
	
	private final int NUM_PEAKS = 6;
	
	
	protected void setUp() throws Exception {
		r = new Random(11235); // explicit seed, making the test deterministic
		super.setUp();
		atoms = new ArrayList<BinnedPeakList>();
		BinnedPeakList tmp;
		
		float[] sums = new float[NUM_PEAKS];
		
		for (int atom = 0; atom < 5000; atom++) {
			tmp = new BinnedPeakList();
			for (int i = 0; i < NUM_PEAKS; i++) {
				tmp.add(i, area(i));
				sums[i] += area(i);
			}
			atoms.add(tmp);
		}
		
		for (int i = 0; i < NUM_PEAKS; i++) {
			assertEquals(sums[i] / 5000, baseArea(i), 0.05f);
		}
		
	}
	
	/*
	 * These are arbitrary functions to make the contents of the BPL very
	 * predictable.
	 */
	private float area(int peakno) {
		return (float) (r.nextGaussian() + baseArea(peakno));
	}
	private float baseArea(int peakno) {
		return peakno;
	}

	private int binnedLocation(int peakno) {
		BinnedPeakList tmp = new BinnedPeakList();
		tmp.add(peakno, 5);
		return tmp.getFirstLocation();
	}
	
	public void testAddAllCollection() {
		StatSummary s = new StatSummary(new ArrayList<BinnedPeakList>());
		s.addAll(atoms);
		peerAtOutput(s, "addAll(Collection<BinnedPeakList>)");
	}
	
	public void testAddAtom() {
		StatSummary s = new StatSummary(new ArrayList<BinnedPeakList>());
		
		Iterator<BinnedPeakList> i = atoms.iterator();
		
		while (i.hasNext()) {
			s.addAtom(i.next());
		}
		peerAtOutput(s, "addAtom(BinnedPeakList)");
	}
	
	public void testAddStats() {
		StatSummary s = new StatSummary(new ArrayList<BinnedPeakList>());
		s.addStats(new StatSummary(atoms));
		peerAtOutput(s, "addStats(StatSummary)");
	}
	
	private void peerAtOutput(StatSummary s, String description) {
		for (int i = 0; i < NUM_PEAKS; i++) {
			assertEquals(description, baseArea(i), 
					                 (float) s.mean(binnedLocation(i)),
					                 (float) 0.05);
			assertEquals(description + " std dev",
					1.0f,
					(float) s.stdDev(binnedLocation(i)),
					(float) 0.05);
		}
	}
	
	public void testSimpleData() {
		ArrayList<BinnedPeakList> a = new ArrayList<BinnedPeakList>();
		BinnedPeakList b = new BinnedPeakList();
		b.add(5, 10);
		b.add(0, 0);
		BinnedPeakList c = new BinnedPeakList();
		c.add(5, 11);
		c.add(0, 1);
		a.add(b);
		a.add(c);
		
		StatSummary s = new StatSummary(a);
		
		assertEquals(10.5, s.mean(5), 0.01);
		assertEquals(0.5, s.mean(0), 0.01);		
	}
	
}
