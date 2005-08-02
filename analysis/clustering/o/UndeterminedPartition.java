
package analysis.clustering.o;

import java.util.List;

import analysis.CollectionDivider;

/**
 * A Partition that needs to learn about itself.  Partitions are created
 * Undetermined and may become Branched or Frozen.  Undetermined partitions
 * do stuff with histograms and other kooky little numbers.  Actually no,
 * just NumberBoxes.
 * @author smitht
 *
 */
public class UndeterminedPartition extends Partition {
	private NumberBox nb;
	private CollectionDivider collectionSource;
	private DataWithSummary collectedData = new DataWithSummary();

	public UndeterminedPartition(Partition parent) {
		this.parent = parent;
		nb = new NumberBox(2500);
	}

	/**
	 * This version of split(data) actually tries to find a valid splitting
	 * point in the input data and to go with it.
	 */
	public int split(DataWithSummary data) {
		nb.addAll(data);
		
//		System.out.println("***************************************");
//		for (int i = 0; i < 4; i++) {
//			nb.printDimension(i);
//		}
//		// debug
		
		SplitRule rule = nb.getBestSplit(95);
		// no good split?
		if (rule == null) {
			if (nb.getBestSplit(90) != null) {
				// "ambiguous"
				collectedData.addAll(data.getAtoms());
				return collectedData.size();
			} else {
				// frozen!
				nb.printCentroid();
				parent.transmogrifyChild(this, new FrozenPartition(parent));
				return 0;
			}
		} else {
			// successful split!
			
			// the parents of these new undetermineds will be reset by the
			// constructor of the branchpartition.
			left = new UndeterminedPartition(parent);
			right = new UndeterminedPartition(parent);
			BranchPartition newBranch = new BranchPartition(this, rule);
			parent.transmogrifyChild(this, newBranch);
			collectedData.addAll(data);
			
			List<DataWithSummary> l = rule.splitAtoms(collectedData);
			collectedData = null;
			
			int n = left.split(l.remove(0)); 
			return n + right.split(l.remove(0));
		}
	}

	public String toString() {
		return "Undetermined partition.";
	}
	public void printRulesDown() {
		System.out.print("Undetermined partition.");
	}
}
