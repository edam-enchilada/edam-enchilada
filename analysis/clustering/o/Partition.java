package analysis.clustering.o;

/**
 * Interface for different kinds of Partitions.  See OClusterThoughts for what
 * this means.
 * @author Thomas Smith
 */


import analysis.CollectionDivider;

public abstract class Partition {
	protected Partition parent;
	protected Partition left;
	protected Partition right;
	protected CollectionDivider collectionSource;
	
	public abstract int split(DataWithSummary atoms);
	public abstract String toString();
	
	public CollectionDivider getCollectionSource() {
		if (collectionSource != null) {
			return collectionSource;
		} else if (parent != null) {
			return parent.getCollectionSource();
		} else {
			throw new Error("Can't find a collection source!");
		}
	}
	
	public void setCollectionSource(CollectionDivider collectionSource) {
		this.collectionSource = collectionSource;
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