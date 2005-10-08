package analysis.clustering.o;

import java.util.*;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class SplitRule extends BinnedPeak {
	public double goodness;
	
	public SplitRule(int location, float area) {
		super(location, area);
	}
	
	public SplitRule(int location, float area, double goodness) {
		this(location, area);
		this.goodness = goodness;
	}
	
	public boolean isAtomGreater(BinnedPeakList atom) {
		return value < atom.getAreaAt(key);
	}
	
	/**
	 * Split up a list of atoms according to the rule that this SplitRule
	 * represents.  Splits are always with regard to a greater-than test.
	 * If the split is at 0, numbers that are not greater than zero (like,
	 * oh, say, 0) go in one set and numbers that are greater than zero
	 * go in t'other.
	 */
	public List<DataWithSummary> splitAtoms(List<BinnedPeakList> atoms) {
		List<DataWithSummary> bucket;
		bucket = new ArrayList<DataWithSummary>(2);
		bucket.add(new DataWithSummary());
		bucket.add(new DataWithSummary());
		BinnedPeakList atom;
		
		Iterator<BinnedPeakList> i = atoms.iterator();
		
		while (i.hasNext()) {
			atom = i.next();
			if (isAtomGreater(atom)) {
				bucket.get(1).add(atom);
			} else {
				bucket.get(0).add(atom);
			}
		}
		return bucket;
	}
	
	public List<DataWithSummary> splitAtoms(DataWithSummary atoms) {
		return splitAtoms(atoms.getAtoms());
	}
	
	public String toString() {
		return "Split along dimension " + key + " at value " + value +
			" (goodness " + goodness + ")";
	}
}
