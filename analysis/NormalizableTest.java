package analysis;

import java.util.Iterator;

import junit.framework.TestCase;

public class NormalizableTest extends TestCase {
	
	BinnedPeakList square;
	BinnedPeakList cube;
	Normalizable norm;
	
	protected void setUp() throws Exception {
		super.setUp();
		norm = new Normalizer();
		
		square = new BinnedPeakList();
		//using perfect squares to get easy results
		square.add(2, 4);
		square.add(3, 9);
		square.add(4, 16);
		square.add(5, 25);
		square.add(6, 36);
		
		cube = new BinnedPeakList();
		//using perfect cubes for easy results
		cube.add(2, 8);
		cube.add(3, 27);
		cube.add(4, 64);
		cube.add(5, 125);
		cube.add(6, 216);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testReducePeaks(){
		//testing with square root
		norm.reducePeaks(square, .5);
		
		//because the "location" is the same as the square root of the "peak area"
		//testing is easy
		Iterator<BinnedPeak> iter = square.iterator();
		BinnedPeak bp;
		while(iter.hasNext()){
			bp = iter.next();
			assertEquals("Key: " + bp.key + " Value: " + bp.value, bp.key, Math.round(bp.value));
		}
		
		//testing with cube root
		norm.reducePeaks(cube, (1.0/3.0));
		
		iter = cube.iterator();
		while(iter.hasNext()){
			bp = iter.next();
			assertEquals("Key: " + bp.key + " Value: " + bp.value, bp.key, Math.round(bp.value));
		}
		
	}

}
