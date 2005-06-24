package analysis.clustering.o;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class SplitRule extends BinnedPeak {
	public SplitRule(int location, float area) {
		super(location, area);
	}
	
	public boolean isAtomLess(BinnedPeakList atom) {
		return area > atom.getAreaAt(location);
	}
}
