package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Collection;

import analysis.BinnedPeakList;


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
	
	public float stdDev(int dimension) {
		return (float) stats.stdDev(dimension);
	}
	
	public StatSummary getStats() {
		return stats;
	}
	
	public ArrayList<BinnedPeakList> getAtoms() {
		return atoms;
	}
	
	public int size() {
		return atoms.size();
	}
}
