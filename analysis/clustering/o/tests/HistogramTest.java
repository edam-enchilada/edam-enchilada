package analysis.clustering.o.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import analysis.clustering.o.Histogram;
import analysis.clustering.o.SplitRule;

import junit.framework.TestCase;

public class HistogramTest extends TestCase {
	private ArrayList<Float> petalWidths;
	private Histogram hist;

	protected void setUp() throws Exception {
		super.setUp();
		
		petalWidths = new ArrayList<Float>();
		// irisData is declared at the end of the class.
		String iData = "";
		float petWid;
		double sum = 0;
		double sumsq = 0;
		int count = 0;
		
		
		for (int i = 0; i < irisData.length; i++) {
			petWid = irisData[i];
			petalWidths.add(petWid);
			sum += petWid;
			sumsq += Math.pow(petWid,2);
			count++;
		}
		float stdDev =
			(float) Math.sqrt((sumsq - (Math.pow(sum,2)/count))/count);
		
		hist = new Histogram(stdDev, count);
		
		Iterator<Float> i = petalWidths.iterator();
		while (i.hasNext()) {
			hist.addPeak(i.next());
		}
	}

	public void testGetSplitPoints() {
		List<SplitRule> splitRules = hist.getSplitRules(90);
		
		if (splitRules == null) {
			fail("Iris data has one split point, not zero.");
		} else {
			// the split point should be greater than 0.5 and less than 1.
			assertEquals(0.75f, splitRules.get(0).area, 0.24f);
		}
	}
	
	private Float[] irisData =
		{0.2f,
		0.2f,
		0.2f,
		0.2f,
		0.2f,
		0.4f,
		0.3f,
		0.2f,
		0.2f,
		0.1f,
		0.2f,
		0.2f,
		0.1f,
		0.1f,
		0.2f,
		0.4f,
		0.4f,
		0.3f,
		0.3f,
		0.3f,
		0.2f,
		0.4f,
		0.2f,
		0.5f,
		0.2f,
		0.2f,
		0.4f,
		0.2f,
		0.2f,
		0.2f,
		0.2f,
		0.4f,
		0.1f,
		0.2f,
		0.1f,
		0.2f,
		0.2f,
		0.1f,
		0.2f,
		0.2f,
		0.3f,
		0.3f,
		0.2f,
		0.6f,
		0.4f,
		0.3f,
		0.2f,
		0.2f,
		0.2f,
		0.2f,
		1.4f,
		1.5f,
		1.5f,
		1.3f,
		1.5f,
		1.3f,
		1.6f,
		1.0f,
		1.3f,
		1.4f,
		1.0f,
		1.5f,
		1.0f,
		1.4f,
		1.3f,
		1.4f,
		1.5f,
		1.0f,
		1.5f,
		1.1f,
		1.8f,
		1.3f,
		1.5f,
		1.2f,
		1.3f,
		1.4f,
		1.4f,
		1.7f,
		1.5f,
		1.0f,
		1.1f,
		1.0f,
		1.2f,
		1.6f,
		1.5f,
		1.6f,
		1.5f,
		1.3f,
		1.3f,
		1.3f,
		1.2f,
		1.4f,
		1.2f,
		1.0f,
		1.3f,
		1.2f,
		1.3f,
		1.3f,
		1.1f,
		1.3f,
		2.5f,
		1.9f,
		2.1f,
		1.8f,
		2.2f,
		2.1f,
		1.7f,
		1.8f,
		1.8f,
		2.5f,
		2.0f,
		1.9f,
		2.1f,
		2.0f,
		2.4f,
		2.3f,
		1.8f,
		2.2f,
		2.3f,
		1.5f,
		2.3f,
		2.0f,
		2.0f,
		1.8f,
		2.1f,
		1.8f,
		1.8f,
		1.8f,
		2.1f,
		1.6f,
		1.9f,
		2.0f,
		2.2f,
		1.5f,
		1.4f,
		2.3f,
		2.4f,
		1.8f,
		1.8f,
		2.1f,
		2.4f,
		2.3f,
		1.9f,
		2.3f,
		2.5f,
		2.3f,
		1.9f,
		2.0f,
		2.3f,
		1.8f};

}
