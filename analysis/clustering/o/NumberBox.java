/**
 * A useful class that keeps track of StatSummaries and Histograms for all
 * dimensions.
 */

package analysis.clustering.o;

import java.util.Collection;
import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class NumberBox {
	private StatSummary stats;
	private Histogram[] histograms;
	private int MAX_LOCATION;
	private boolean histogramsAreUpdated = false;
	
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
			if (histograms[p.key + MAX_LOCATION] == null) {
				histograms[p.key + MAX_LOCATION] 
				           = new Histogram((float) stats.stdDev(p.key),
				        		   stats.count(), p.key);
			}
			histograms[p.key + MAX_LOCATION].addPeak(p.value);
		}
	}

	private void histAtoms(Collection<BinnedPeakList> atoms) {
		Iterator<BinnedPeakList> i = atoms.iterator();
		while (i.hasNext()) {
			histAtom(i.next());
		}
	}

	private void setImplicits() {
		if (! histogramsAreUpdated) {
			for (int i = 0; i < MAX_LOCATION * 2; i++) {
				if (histograms[i] != null) {
					histograms[i].setParticleCount(stats.count());
				}
			}
//			System.out.println("Set implicits to " + stats.count());
			histogramsAreUpdated = true;
		}
	}

	private void addAtom(BinnedPeakList atom) {
		histogramsAreUpdated = false;
		assert(stats != null);
		stats.addAtom(atom);
		// TODO: maybe resize histogram
		histAtom(atom);
	}

	/*
	 * These two methods are the same, but, I think there need to be two
	 * of them because the addAll of DataWithSummary is O(d) while the
	 * addAll of Collection<BinnedPeakList> is O(n*(avg. d)).
	 * 
	 * Although putting the atoms into histograms is still O(n) in both
	 * cases.  Muh.
	 * 
	 * Oh, DataWithSummary is no longer even a subclass of Coll<BPL> so it
	 * has to be 2.  yay!
	 */
	/**
	 * Add the contents of the DataWithSummary object to the statistics
	 * and histograms.
	 */
	public boolean addAll(DataWithSummary that) {
		histogramsAreUpdated = false;
		if (stats == null) {
			stats = new StatSummary(that.getAtoms());
		} else {
			stats.addAll(that);
		}
		// TODO: maybe resize histograms.
		histAtoms(that.getAtoms());
		return true;
	}
	/**
	 * Add the contents of the Colelction to the stats and histograms.
	 */
	public boolean addAll(Collection<BinnedPeakList> atoms) {
		histogramsAreUpdated = false;
		if (stats == null) {
			stats = new StatSummary(atoms);
		} else {
			stats.addAll(atoms);
		}
		// TODO: maybe resize histograms.
		histAtoms(atoms);
		return true;
	}

	/**
	 * @return the best way of splitting *any* dimension, or null if there
	 * are no good splits.
	 */
	public SplitRule getBestSplit(int confidencePercent) {
		setImplicits();
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
//		if (best == null) {
//			System.out.println("No split point found!");
//		} else {
//			System.out.println("Yay!  Using " + best.toString());
//		}
		return best;
	}
	
	/**
	 * Prints out the histogram of a dimension, with statistics.
	 */
	public void printDimension(int dim) {
		setImplicits();
		System.out.println("Trying to print stats for dim " + dim);
		System.out.println("Total particles: " + stats.count());
		if (dim < MAX_LOCATION && dim > - MAX_LOCATION
				&& histograms[dim + MAX_LOCATION] != null)
		{
			System.out.println(stats.toString(dim));
			histograms[dim + MAX_LOCATION].printHistogram(true);
		}
	}
	
	/**
	 * Prints the mean particle of the partition.  Partitions aren't
	 * determined by centroid, but centroids can still be useful to look at.
	 *
	 */
	public void printCentroid() {
		System.out.println("Partition centroid:");
		System.out.println("Particle count: " + stats.count());
		System.out.println("m/z\tarea");
		for (int i = - MAX_LOCATION; i < MAX_LOCATION; i++) {
			if (stats.mean(i) > 0.0001) {
				System.out.println(i + "\t" + stats.mean(i));
			}
		}
	}
}
