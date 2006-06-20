package analysis.clustering.o;

/**
 * Hmm, this Partition thing could really benefit from refactoring using a
 * has-a type relationship rather than is-a.  Changing the type of a Partition
 * could be as easy as setting a variable, but instead it's all complicated
 * and silly with trasmogrifyChild and stuff.
 * 
 * Interface for different kinds of Partitions.  See OClusterThoughts for what
 * this means, although that document is rather out of date.
 * @author Thomas Smith
 */


import analysis.CollectionDivider;

public abstract class Partition {
	/*
	 * The Partition tree is a binary tree.  SplitRules do the work of
	 * dividing into left and right bits, the part that is less than the
	 * split point goes into the left child, the greater goes to the right.
	 */
	protected Partition parent;
	protected Partition left;
	protected Partition right;
	
	/*
	 * collectionSources would be important if we decided to actually finish
	 * this implementation, so that it could save its clustering results back
	 * to the database.  you ask a CollectionDivider to make a new Collection
	 * and it does so and you stick atoms in it.  A FrozenPartition would do
	 * this.
	 */
	protected CollectionDivider collectionSource;
	
	/**
	 * Override this method in subclasses in order to deal with incoming data.
	 * Partition types each have something reasonable to do with the data:
	 * either store it to a collection, ponder it in histogram form, or
	 * whatever.
	 * @param atoms a list of atoms with summary statistics.
	 * @return the number of atoms that have been kept in memory because 
	 *   of uncertainty.
	 */
	public abstract int split(DataWithSummary atoms);
	public abstract String toString();
	
	/**
	 * Print out this partition's rule and pass on the call to the children, if
	 * there are any.
	 */
	public abstract void printRulesDown();
	
	public CollectionDivider getCollectionSource() {
		if (collectionSource != null) {
			return collectionSource;
		} else if (parent != null) {
			collectionSource = parent.getCollectionSource();
			return collectionSource;
		} else {
			throw new Error("Can't find a collection source!");
		}
	}
	
	public void setCollectionSource(CollectionDivider collectionSource) {
		this.collectionSource = collectionSource;
	}
	
	/**
	 * An UndeterminedPartition is a different class from a BranchPartition
	 * or FrozenPartition.  When it nees to change to one of those final
	 * types, it must call this method, so that pointers to it are updated.
	 * 
	 * 		Partition p = new FrozenPartition(...);
	 * 		
	 * 		parent.transmogrifyChild(this, p);
	 * 
	 * @param oldChild The child to replace.
	 * @param newChild The child that will take its place.
	 */
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


	public Partition getLeftChild() {
		return left;
	}
	
	public Partition getRightChild() {
		return right;
	}
	
	public Partition getParent() {
		return parent;
	}

	public void setParent(Partition parent) {
		this.parent = parent;
	}



}