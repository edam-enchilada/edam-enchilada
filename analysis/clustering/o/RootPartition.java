package analysis.clustering.o;

import analysis.CollectionDivider;

/**
 * This is a simple class meant to help keep track of a Partition tree.  I
 * use it so that my controlling class doesn't need to be a Partition, and
 * I can set up a CollectionDivider if needed, and it can handle
 * transmogrifyChild, and that sort of thing.
 * 
 * Actually using the CollectionDividers is not implemented yet, and probably
 * won't be since OCluster sucks for spectra, but... meh.
 * 
 * @author smitht
 *
 */

public class RootPartition extends Partition {
	public RootPartition(CollectionDivider collectionSource) {
		left = null;
		right = null;
		parent = null;
		this.collectionSource = collectionSource;
	}

	/**
	 * Feed data to the Partition hierarchy.  Create it if needed.
	 */
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
	
	public void printRulesDown() {
		System.out.print("Root partition.\nChild: <<");
		left.printRulesDown();
		System.out.println(">>\n");
	}

}
