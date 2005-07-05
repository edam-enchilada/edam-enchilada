package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Collection;

import analysis.BinnedPeakList;


// should this actually extend StatSummary and have-a ArrayList?
// nah, too many methods i'd have to deal with overriding and stuff.
// But should it just be its own thing?
public class DataWithSummary extends ArrayList<BinnedPeakList> {
	private StatSummary stats;
	
	public DataWithSummary() {
		stats = new StatSummary();
	}

	/* (non-Javadoc)
	 * @see java.util.ArrayList#add(E)
	 */
	@Override
	public boolean add(BinnedPeakList atom) {
		stats.addAtom(atom);
		// TODO Auto-generated method stub
		return super.add(atom);
	}

	/* (non-Javadoc)
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends BinnedPeakList> atoms) {
		stats.addAll((Collection<BinnedPeakList>) atoms);
		return super.addAll(atoms);
	}
	
	public boolean add(DataWithSummary that) {
		stats.addStats(that.stats);
		return super.addAll(that);
	}
	
	public float stdDev(int dimension) {
		return (float) stats.stdDev(dimension);
	}
	
	public StatSummary getStats() {
		return stats;
	}
}
