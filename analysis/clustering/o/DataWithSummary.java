package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Collection;

import analysis.BinnedPeakList;

/**
 * DataWithSummary - an ArrayList of atoms, and some Cluster Feature
 * kind of things.
 * 
 * Use DataWithSummary kinda like an ArrayList.  You add stuff to it.
 * I guess it's not iterable itself, but it has a getAtoms method, so
 * meh.  These statistics are important for making Histograms.  The stats
 * are StatSummary's.
 * 
 * @author smitht
 *
 */

public class DataWithSummary {
	private ArrayList<BinnedPeakList> atoms;
	private StatSummary stats;
	
	public DataWithSummary() {
		stats = new StatSummary();
		atoms = new ArrayList<BinnedPeakList>();
	}

	public boolean add(BinnedPeakList atom) {
		stats.addAtom(atom);
		return atoms.add(atom);
	}


	public boolean addAll(Collection<BinnedPeakList> BPLs) {
		stats.addAll((Collection<BinnedPeakList>) BPLs);
		// XXX: it might be a lot lot faster to just keep track of the
		// arraylist that the atoms were stored in, rather than copying
		// all of them over into this one's arraylist.
		return atoms.addAll(BPLs);
	}
	
	public boolean addAll(DataWithSummary that) {
		stats.addStats(that.stats);
		return atoms.addAll(that.atoms);
	}
	
	/**
	 * Returns the standard deviation of some dimension of the data,
	 * efficiently (O(1)).
	 * @param dimension Which dimension to find stdDev of? 
	 * @return the standard deviation.
	 */
	public float stdDev(int dimension) {
		return (float) stats.stdDev(dimension);
	}
	
	/**
	 * Returns an object which contains interesting statistical information
	 * about all the data in this object.
	 * @return a StatSummary object.
	 */
	public StatSummary getStats() {
		return stats;
	}
	
	/**
	 * Returns a list of all of the BinnedPeakLists in this object.
	 * @return the list of BPLs.
	 */
	public ArrayList<BinnedPeakList> getAtoms() {
		return atoms;
	}
	
	/**
	 * @return the number of BinnedPeakLists in this object.
	 */
	public int size() {
		return atoms.size();
	}
}
