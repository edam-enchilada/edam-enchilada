package analysis.clustering.o;

import java.util.Collection;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class NumberBox {
	private StatSummary stats;
	private Histogram[] histograms;
	private int MAX_LOCATION;
	
	public NumberBox(int numDims) {
		//super;
		MAX_LOCATION = numDims;
		histograms = new Histogram[MAX_LOCATION * 2];
	}
	

	private void recordAtom(BinnedPeakList bpl) {
		BinnedPeak p;
		for (int i = 0; i < bpl.length(); i++) {
			p = bpl.getNextLocationAndArea();
			if (histograms[p.location + MAX_LOCATION] == null) {
				histograms[p.location + MAX_LOCATION] 
				           = new Histogram((float) stats.stdDev(p.location),
				        		   stats.count(), p.location);
			}
			histograms[p.location + MAX_LOCATION].addPeak(p.area);
		}
	}



	public void add(BinnedPeakList atom) {
		recordAtom(atom);
		stats.addAtom(atom);
	}


	public boolean add(DataWithSummary that) {
		//TODO: likely, logic about possibly changing histogram sizes
		// should go here.  Actually no, somewhere else.  But this method
		// does need more work ^^
		// wrong way: stats.add(that);
		
		return false;
	}


	public boolean addAll(Collection<BinnedPeakList> atoms) {
		// TODO Auto-generated method stub
		stats.addAll(atoms);
		
		return false;
	}
	
}
