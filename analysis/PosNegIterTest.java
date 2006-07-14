package analysis;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Tests the PosNegIter - an iterator that returns either only negative peaks 
 * or only non-negative peaks (zero is included with the positive peaks).
 * @author steinbel
 *
 */
public class PosNegIterTest extends TestCase {
	
	//test peaklist (modified from testDB2) looks like this:
	/*atomid	peakloc	
	 * 8		-430	
	 * 8		-308	
	 * 8		-300	
	 * 8		-30		
	 * 8		0		
	 * 8		30		
	 * 8		70		
	 * 8		80		
	 * 8		800		
	 */

	private BinnedPeakList bpl;
	private Iterator<BinnedPeak> posIter;
	private Iterator<BinnedPeak> negIter;

	protected void setUp() throws Exception {
		super.setUp();
		bpl = new BinnedPeakList();
		//peaks not added in order to test methods more thoroughly.
		bpl.add(-430, 15);
		bpl.add(-300, 15);
		bpl.add(800, 15);
		bpl.add(0, 15);
		bpl.add(30, 15);
		bpl.add(70, 15);
		bpl.add(-30, 15);
		bpl.add(80, 15);
		bpl.add(-308, 15);
		posIter = bpl.posNegIterator(false);
		negIter = bpl.posNegIterator(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testHasNext(){
		//testing on the non-neg peaks
		int counter = 0;
		while (posIter.hasNext())
			counter++;
		assert(counter == 5): "Didn't get correct number of non-negative peaks.";
		
		//testing on the negative peaks
		counter = 0;
		while (negIter.hasNext())
			counter++;
		assert(counter == 4): "Didn't get correct number of negative peaks.";
	}
	
	public void testNext(){
		//test the non-neg peaks
		int loc = posIter.next().key;
		assert(loc == 800): "Didn't progress correctly to 800. key = " + loc;
		//NOTE:  This includes 0 as a positive peak.
		loc = posIter.next().key;
		assert(loc == 0): "Didn't progress correctly to 0. key = " + loc;
		loc = posIter.next().key;
		assert(loc == 30): "Didn't progress correctly to 30. key = " + loc;;
		
		//test the negative peaks
		loc = negIter.next().key;
		assert(loc == -430): "Didn't progress correctly to -430. key = " + loc;
		loc = negIter.next().key;
		assert(loc == -300): "Didn't progress correctly to -300. key = " + loc;
		loc = negIter.next().key;
		assert(loc == -30): "Didn't progress correctly to -30. key = " + loc;
	}
	
	public void testRemove(){
		//remove doesn't do anything at present, so no need to do anything here.
	}

}
