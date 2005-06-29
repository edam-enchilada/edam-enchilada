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
	
	public boolean isAtomLess(BinnedPeakList atom) {
		return area > atom.getAreaAt(location);
	}
	
	public List<List<BinnedPeakList>> splitAtoms(List<BinnedPeakList> atoms) {
		List<List<BinnedPeakList>> bucket;
		bucket = new ArrayList<List<BinnedPeakList>>(2);
		bucket.add(new LinkedList<BinnedPeakList>());
		bucket.add(new LinkedList<BinnedPeakList>());
		BinnedPeakList atom;
		
		Iterator<BinnedPeakList> i = atoms.iterator();
		
		while (i.hasNext()) {
			atom = i.next();
			if (isAtomLess(atom)) {
				bucket.get(0).add(atom);
			} else {
				bucket.get(1).add(atom);
			}
		}
		return bucket;
	}
	
	public String toString() {
		return "Split along dimension " + location + " at value " + area +
			" (goodness " + goodness + ")";
	}
}
