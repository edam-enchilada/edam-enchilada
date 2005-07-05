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
public class UndeterminedPartition implements Partition {
	private Partition parent;
	private Partition left, right;
	private NumberBox nb;
	private CollectionDivider collectionSource;
	private DataWithSummary collectedData = new DataWithSummary();

	public UndeterminedPartition(Partition parent) {
		this.parent = parent;
		nb = new NumberBox(2500);
	}
	


	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#classify(analysis.BinnedPeakList)
	 */
//	public int classify(BinnedPeakList bpl) {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#split(java.util.List)
	 */
	public int split(DataWithSummary data) {
		nb.addAll(data);
		
		/*
		 * FOR DEBUGGING ONLY
		 */
		System.out.println("***************************************");
		for (int i = 0; i < 4; i++) {
			nb.printDimension(i);
		}
		
		SplitRule r = nb.getBestSplit(95);
		if (r == null) {
			if (nb.getBestSplit(90) != null) {
				// "ambiguous"
				collectedData.addAll(data);
				return collectedData.size();
			} else {
				// frozen!
				parent.transmogrifyChild(this, new FrozenPartition(parent));
				return 0;
			}
		} else {
			// successful split!
			
			// the parents of these new undetermineds will be reset by the
			// constructor of the branchpartition.
			left = new UndeterminedPartition(parent);
			right = new UndeterminedPartition(parent);
			BranchPartition b = new BranchPartition(this, r);
			parent.transmogrifyChild(this, b);
			collectedData.addAll(data);
			
			List<DataWithSummary> l = r.splitAtoms(collectedData);
			collectedData = null;
			int n = left.split(l.remove(0)); 
			return n + right.split(l.remove(0));
		}
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

//	private void addAtom(BinnedPeakList l) {
//		BinnedPeak peak;
//		for (int i = 0; i < l.length(); i++) {
//			peak = l.getNextLocationAndArea();
//			histograms.get(peak.location).addPeak(peak.area);
//		}
//	}
//	
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

	public Partition getLeftChild() {
		return left;
	}
	public Partition getRightChild() {
		return right;
	}


	public String toString() {
		return "Undetermined partition.";
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
	
}
