/**
 * 
 */
package analysis.clustering.o;

import java.util.List;

import analysis.CollectionDivider;

/**
 * @author smitht
 *
 */
public class BranchPartition implements Partition {
	private Partition parent;
	private Partition left;
	private Partition right;
	private SplitRule rule;
	private CollectionDivider collectionSource;

	
	/*
	 * Constructors
	 */
	public BranchPartition(BranchPartition par, SplitRule cutPoint,
			Partition l, Partition r) {
		parent = par;
		rule = cutPoint;
		left = l;
		right = r;
	}
	
	public BranchPartition(Partition template, SplitRule cutPoint) {
		parent = template.getParent();
		left = template.getLeftChild();
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
	public String rulesDown() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean transmogrifyChild(Partition oldChild, Partition newChild) {
		if (left == oldChild) {
			left = newChild;
			return true;
		} else if (right == oldChild) {
			right = newChild;
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		return "Branch partition; rule: " + rule.toString();
	}
	
	/*
	 * Boring little accessor methods
	 */
	
	public Partition getLeftChild() {
		return left;
	}
	
	public Partition getRightChild() {
		return right;
	}

	public CollectionDivider getCollectionSource() {
		return collectionSource;
	}

	public void setCollectionSource(CollectionDivider collectionSource) {
		this.collectionSource = collectionSource;
	}

	public Partition getParent() {
		return parent;
	}

	public void setParent(Partition parent) {
		this.parent = parent;
	}


}
