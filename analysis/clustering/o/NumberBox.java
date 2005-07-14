package analysis.clustering.o;

import java.util.Collection;
import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class NumberBox {
	private StatSummary stats;
	private Histogram[] histograms;
	private int MAX_LOCATION;
	
	public NumberBox(int numDims, StatSummary initialStats) {
		//super;
		MAX_LOCATION = numDims;
		histograms = new Histogram[MAX_LOCATION * 2];
		stats = initialStats;
	}

	public NumberBox(int numDims) {
		this(numDims, null);
	}

	private void histAtom(BinnedPeakList bpl) {
		BinnedPeak p;
		Iterator<BinnedPeak> i = bpl.iterator();
		while (i.hasNext()) {
			p = i.next();
			if (histograms[p.location + MAX_LOCATION] == null) {
				histograms[p.location + MAX_LOCATION] 
				           = new Histogram((float) stats.stdDev(p.location),
				        		   stats.count(), p.location);
			}
			histograms[p.location + MAX_LOCATION].addPeak(p.area);
		}
	}

	private void histAtoms(Collection<BinnedPeakList> atoms) {
		Iterator<BinnedPeakList> i = atoms.iterator();
		while (i.hasNext()) {
			histAtom(i.next());
		}
	}


	private void addAtom(BinnedPeakList atom) {
		assert(stats != null);
		stats.addAtom(atom);
		// TODO: maybe resize histogram
		histAtom(atom);
	}

	/*
	 * These two methods are the same, but, I think there need to be two
	 * of them because the addAll of DataWithSummary is O(1) while the
	 * addAll of Collection<BinnedPeakList> is O(n).
	 */
	public boolean addAll(DataWithSummary that) {
		if (stats == null) {
			stats = new StatSummary(that);
		} else {
			stats.addAll(that);
		}
		// TODO: maybe resize histograms.
		histAtoms(that);
		return false;
	}
	public boolean addAll(Collection<BinnedPeakList> atoms) {
		if (stats == null) {
			stats = new StatSummary(atoms);
		} else {
			stats.addAll(atoms);
		}
		// TODO: maybe resize histograms.
		histAtoms(atoms);
		return true;
	}

	public SplitRule getBestSplit(int confidencePercent) {
		SplitRule best = null;
		for (int i = 0; i < MAX_LOCATION * 2; i++) {
			if (best == null && histograms[i] != null) {
				best = histograms[i].getBestSplit(confidencePercent);
			} else if (best != null && histograms[i] != null) {
				SplitRule temp = histograms[i].getBestSplit(confidencePercent);
				if (temp != null &&
						temp.goodness > best.goodness) {
					best = temp;
				}
			}
		}
		return best;
	}
	
	public void printDimension(int dim) {
		System.out.println("Trying to print stats for dim " + dim);
		if (dim < MAX_LOCATION && dim > - MAX_LOCATION) {
			System.out.println(stats.toString(dim));
			histograms[dim + MAX_LOCATION].printHistogram(true);
		}
	}
	
}
