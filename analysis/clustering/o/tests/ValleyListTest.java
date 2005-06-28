package analysis.clustering.o.tests;

//import java.util.Iterator;
import java.util.List;

import analysis.clustering.o.Extremum;
import analysis.clustering.o.ValleyList;

import junit.framework.TestCase;

public class ValleyListTest extends TestCase {
	ValleyList vl = new ValleyList();
	ValleyList insig = new ValleyList();
	Extremum e, f;
	
	protected void setUp() throws Exception {
		super.setUp();
		vl.add(new Extremum(0, 10));
		vl.add(new Extremum(5, 2));
		vl.add(new Extremum(10, 10));
		vl.add(new Extremum(15, 9));
		vl.add(new Extremum(20, 25));
		e = new Extremum(25, 4);
		f = new Extremum(30, 16);
		vl.add(e);
		vl.add(f);
		
		insig.add(new Extremum(0, 10));
		insig.add(new Extremum(5, 9));
		insig.add(new Extremum(10, 15));
	}

	public void testAdd() {
		assertSame(e, vl.get(5));
		assertSame(f, vl.get(6));
		
	}
	
	public void testGetValleyNeighborhood() {
		List<Extremum> n0 = vl.getValleyNeighborhood(0);
		List<Extremum> n1 = vl.getValleyNeighborhood(1);
		assertSame(vl.get(0), n0.get(0));
		assertSame(vl.get(1), n0.get(1));
		assertSame(vl.get(2), n0.get(2));
		assertSame(vl.get(2), n1.get(0));
		assertSame(vl.get(3), n1.get(1));
		assertSame(vl.get(4), n1.get(2));
		
		assertSame(e, vl.getValleyNeighborhood(2).get(1));
	}

	public void testGetValley() {
		assertSame(e, vl.getValley(2));
		assertSame(vl.get(1), vl.getValley(0));
		assertSame(vl.get(3), vl.getValley(1));
	}

	public void testNumValleys() {
		assertEquals(3, vl.numValleys());
	}

	public void testRemoveInsignificant() {
		ValleyList sig = vl.removeInsignificant(95);
		
		assertSame(vl.get(0), sig.get(0));
		assertSame(vl.get(1), sig.get(1));
		// a peak and valley should be removed.
		assertSame(vl.get(4), sig.get(2));
		assertSame(vl.get(5), sig.get(3));
		assertSame(vl.get(6), sig.get(4));

		// For yet more reassurance, if you want it...
//		Iterator<Extremum> i = sig.iterator();
//		while (i.hasNext()) {
//			System.out.println(i.next());
//		}
		
		assertEquals(vl.getValleyNeighborhood(2),
				    sig.getValleyNeighborhood(1));
		assertEquals(2, sig.numValleys());
		
		ValleyList empty = insig.removeInsignificant(95);
		// now "empty" actually contains just one peak
		assertEquals(0, empty.numValleys());
		
		ValleyList emptier = (new ValleyList()).removeInsignificant(95);
		// this is just to make sure running it on an empty list doesn't
		// do anything too bad.
		assertEquals(0, emptier.numValleys());

	}

}
