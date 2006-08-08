package chartlib.hist;

import java.awt.Color;
import java.util.*;
import experiments.Tuple;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;
import junit.framework.TestCase;

public class HistogramDatasetTest extends TestCase {

	Random rand = new Random(31337);
	private HistogramDataset[] baseHist, anotherBaseHist, compHist;
	ArrayList<Integer> keep = new ArrayList<Integer>();
	
	
	private class ALBPL extends ArrayList<Tuple<Integer, BinnedPeakList>> {
		// to save typing.
		public ALBPL() {
			super();
		}
		public ALBPL(int capacity) {
			super(capacity);
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		ALBPL base = new ALBPL(100), compare = new ALBPL(100);

		
		for (int i = 1; i <= 100; i++) {
			BinnedPeakList bpl = new BinnedPeakList();
			for (int j = 0; j < rand.nextInt(60); j++) {
				bpl.add(rand.nextInt(10) - 30, rand.nextFloat());
			}
			
			bpl.normalize(DistanceMetric.CITY_BLOCK);
			base.add(new Tuple<Integer, BinnedPeakList>(i, bpl));
			if (i % 2 == 0) {
				compare.add(new Tuple<Integer, BinnedPeakList>(i, bpl));
				keep.add(i);
			}
		}
		baseHist = HistogramDataset.analyseBPLs(base.iterator(), Color.BLACK);
		anotherBaseHist = HistogramDataset.analyseBPLs(base.iterator(), Color.BLACK);
		
		
		compHist = HistogramDataset.analyseBPLs(compare.iterator(), Color.BLACK);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEquals() {
		for (int i = 0; i < baseHist.length; i++) {
			assertTrue(baseHist[i].equals(anotherBaseHist[i]));
		}
		
		baseHist[0].hists[20] = new ChainingHistogram(0.01f);
		anotherBaseHist[0].hists[20] = null;
		// an empty one should be the same as a null one.
		assertTrue(baseHist[0].equals(anotherBaseHist[0]));

	}
	
	public void testIntersect() {
		HistogramDataset[] destHist;
		
		destHist = HistogramDataset.intersect(baseHist, keep);
		// normalization?  eh, i don't think that's it.
		for (int i = 0; i < baseHist.length; i++) {
			assertTrue(destHist[i].equals(compHist[i]));
		}
	}

}
