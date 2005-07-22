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
		return area < atom.getAreaAt(location);
	}
	
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
		return "Split along dimension " + location + " at value " + area +
			" (goodness " + goodness + ")";
	}
}
