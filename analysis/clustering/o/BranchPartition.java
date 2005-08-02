/**
 * BranchPartition - a Partition that has a static rule to split data.
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
	public BranchPartition(Partition parent, SplitRule cutPoint,
			Partition l, Partition r) {
		this.parent = parent;
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

	/**
	 * Divides the incoming data according to its SplitRule, and feeds
	 * the appropriate data to its children!
	 */
	public int split(DataWithSummary atoms) {
		List<DataWithSummary> divided = rule.splitAtoms(atoms);
		atoms = null; // so maybe atoms can get garbage collected.
		return left.split(divided.get(0)) 
			+ right.split(divided.get(1));
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
