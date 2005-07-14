package analysis.clustering.o;

import analysis.CollectionDivider;

public class RootPartition extends Partition {
	
	public RootPartition(CollectionDivider collectionSource) {
		left = null;
		right = null;
		parent = null;
		this.collectionSource = collectionSource;
	}

	@Override
	public int split(DataWithSummary atoms) {
		// the left child is the only child.  
		if (left == null) {
			left = new UndeterminedPartition(this);
		}
		return left.split(atoms);
	}
	
	public String toString() {
		return "Root of Partition Hierarchy.";
	}

}
