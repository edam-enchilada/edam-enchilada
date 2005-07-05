/**
 * 
 */
package analysis.clustering.o;

import analysis.CollectionDivider;

/**
 * @author smitht
 *
 */
public class FrozenPartition implements Partition {
	private Partition parent;
	private CollectionDivider collectionSource;
	
	// TODO: possibly implement assigning particles to the right collection.
	// if this happens, want another constructor that does it to input data.

	
	/*
	 * Constructors
	 */
	public FrozenPartition(Partition par) {
		parent = par;
	}


	public int split(DataWithSummary atoms) {
		// TODO: assign particles to the correct collection!
		collectionSource = parent.getCollectionSource();
		// ...
		return 0;
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

	/*
	 * Boring little accessor methods
	 */
	
	public Partition getLeftChild() {
		return null;
	}
	public Partition getRightChild() {
		return null;
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


	public boolean transmogrifyChild(Partition oldChild, Partition newChild) {
		throw new Error("Frozen partitions have no children!");
	}

	public String toString() {
		return "Frozen partition!";
	}

}
