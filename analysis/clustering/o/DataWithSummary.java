package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Collection;

import analysis.BinnedPeakList;


// should this actually extend StatSummary and have-a ArrayList?
// nah, too many methods i'd have to deal with overriding and stuff.
public class DataWithSummary extends ArrayList<BinnedPeakList> {
	private StatSummary stats;

	/* (non-Javadoc)
	 * @see java.util.ArrayList#add(E)
	 */
	@Override
	public boolean add(BinnedPeakList atom) {
		stats.add(atom);
		// TODO Auto-generated method stub
		return super.add(atom);
	}

	/* (non-Javadoc)
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends BinnedPeakList> atoms) {
		stats.add((Collection<BinnedPeakList>) atoms);
		return super.addAll(atoms);
	}
	
	public boolean add(DataWithSummary that) {
		stats.add(that.stats);
		return super.addAll(that);
	}
	
	public float stdDev(int dimension) {
		return (float) stats.stdDev(dimension);
	}
	
}
