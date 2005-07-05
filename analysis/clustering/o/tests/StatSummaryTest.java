package analysis.clustering.o.tests;

import java.util.*;

import analysis.BinnedPeakList;

import junit.framework.TestCase;

public class StatSummaryTest extends TestCase {
	List<BinnedPeakList> atoms;
	protected void setUp() throws Exception {
		super.setUp();
		atoms = new ArrayList<BinnedPeakList>();
		BinnedPeakList tmp;
		for (int atom = 0; atom < 10; atom++) {
			for (int i = 0; i < 6; i++) {
				tmp = new BinnedPeakList();
				tmp.add(location(atom, i), area(atom, i));
			}
		}
	}
	
	/*
	 * These are arbitrary functions to make the contents of the BPL very
	 * predictable.
	 */
	private float location(int atom, int peakno) {
		return (float) (Math.pow(atom - peakno, 3.0) * Math.pow(-1.0, peakno))
			% 2500;
	}
	private float area(int atom, int peakno) {
		return (float) (Math.pow(atom - peakno, 2.0) + 1.0);
	}


	private int binnedLocation(int atom, int peakno) {
		BinnedPeakList tmp = new BinnedPeakList();
		tmp.add(location(atom, peakno), 5);
		return tmp.getFirstLocation();
	}
	
	
}
