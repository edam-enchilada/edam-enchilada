/**
 * 
 */
package analysis.clustering.o;

import java.util.List;

/**
 * @author smitht
 *
 */
public class BranchPartition extends Partition {
	private SplitRule rule;

	
	/*
	 * Constructors
	 */
	public BranchPartition(Partition par, SplitRule cutPoint,
			Partition l, Partition r) {
		parent = par;
		rule = cutPoint;
		left = l;
		right = r;
	}
	
	public BranchPartition(Partition template, SplitRule cutPoint) {
		parent = template.getParent();
		left = template.getLeftChild();
		if (left == null) {
			System.out.println("WTF?");
		}
		left.setParent(this);
		right = template.getRightChild();
		right.setParent(this);
		rule = cutPoint;
	}

	public int split(DataWithSummary atoms) {
		// or should this do the sort and collect statistics thing?
		// uh?
		// what if left and right need to be created still?
		// I guess that shouldn't happen.
		List<DataWithSummary> divided = rule.splitAtoms(atoms);
		atoms = null; // so maybe atoms can get garbage collected.
		return left.split(divided.get(0)) 
			+ right.split(divided.get(1));
	}

	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#rulesUp()
	 */
	public String rulesUp() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#rulesDown()
	 */
	public void printRulesDown() {
		System.out.print(toString() + "\nChildren:  Left\n<");
		left.printRulesDown();
		System.out.print(">\nRight \n<");
		right.printRulesDown();
		System.out.print(">");
	}
	
	public String toString() {
		return "Branch partition; rule: " + rule.toString();
	}
}
