package analysis.dataCompression;

import java.util.ArrayList;
import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;
import junit.framework.TestCase;

public class ClusterFeatureTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}
	public void testUpdateCF() {
		DistanceMetric dMetric = DistanceMetric.EUCLIDEAN_SQUARED;
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-250, (float) 0.0014387778);
		bp1.add(-200, (float) 0.059778336);
		bp1.add(-95, (float) 0.08888895);
		ClusterFeature test = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		test.updateCF(bp1, 1);
		
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-25, (float) 0.066068016);
		bp2.add(30, (float) 0.0012005854);
		bp2.add(100, (float) 0.0033571478);
		bp2.add(125, (float) 0.06318322);
		test.updateCF(bp2, 2);
		
		ArrayList<Integer> expected = new ArrayList<Integer>();
		expected.add(1);
		expected.add(2);
		assertEquals(test.getAtomIDs(), expected);
		assertEquals(test.getCount(), 2);
		bp1.normalize(dMetric);
		bp1.addAnotherParticle(bp2);
		bp1.normalize(dMetric);
		assert test.getSums().getDistance(bp1, dMetric)==0.0 : "not equal";
	}
	
	public void testAbsorbCF() {
		DistanceMetric dMetric = DistanceMetric.EUCLIDEAN_SQUARED;
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-210, (float) 0.1);
		bp1.add(-160, (float) 0.2);
		bp1.add(-100, (float) 0.3);
		bp1.add(-30, (float) 0.4);
		bp1.add(20, (float) 0.5);
		bp1.add(90, (float) 0.6);
		bp1.add(120, (float) 0.7);
		ClusterFeature testCF1 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		testCF1.updateCF(bp1, 1);
		
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-200, (float) 0.1);
		bp2.add(-150, (float) 0.2);
		bp2.add(-90, (float) 0.3);
		bp2.add(-20, (float) 0.4);
		bp2.add(30, (float) 0.5);
		bp2.add(100, (float) 0.6);
		bp2.add(130, (float) 0.7);
		testCF1.updateCF(bp2, 2);
		
		BinnedPeakList bp3 = new BinnedPeakList(new Normalizer());
		bp3.add(-210, (float) 0.1);
		bp3.add(-200, (float) 0.1);
		bp3.add(-160, (float) 0.1);
		bp3.add(-150, (float) 0.1);
		ClusterFeature testCF2 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		testCF2.updateCF(bp3, 3);

		testCF1.absorbCF(testCF2);

		ArrayList<Integer> expected = new ArrayList<Integer>();
		expected.add(1);
		expected.add(2);
		expected.add(3);
		assertEquals(testCF1.getAtomIDs(), expected);
		assertEquals(testCF1.getCount(), 3);

		bp1.addAnotherParticle(bp2);
		bp1.normalize(dMetric);
		bp1.multiply(2);
		bp1.addAnotherParticle(bp3);
		bp1.normalize(dMetric);
		assert testCF1.getSums().getDistance(bp1, dMetric)==0.0 : "not equal";
	}
	
	public void testIsEqual() {
		
	}

	public void testMakesSumsSparse() {
		
	}
	
	public void testUpdateCFBoolean() {
		
	}
	
	public void testUpdatePointers() {
		
	}
}
