/**
 * 
 */
package analysis.clustering.o;

import java.util.ArrayList;
import java.util.List;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;

/**
 * @author smitht
 *
 */
public class DeterminedPartition implements Partition {
	private Partition parent;
	private Partition left;
	private Partition right;
	private SplitRule rule;
	private CollectionDivider collectionSource;
	private boolean branched;
	
	public DeterminedPartition(DeterminedPartition par, SplitRule cutPoint,
			Partition l, Partition r) {
		parent = par;
		rule = cutPoint;
		left = l;
		right = r;
		branched = true;
	}
	
	public DeterminedPartition(DeterminedPartition par) {
		parent = par;
		left = right = null;
		rule = null;
		branched = false;
	}

	public Partition getParent() {
		return parent;
	}
	
//	public int classify(BinnedPeakList bpl) {
//		if (branched) {
//			// something
//			return -1;
//		} else {
//			return -1;
//		}
//	}

	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#split(java.util.List)
	 */
	public int split(List<BinnedPeakList> atoms, float[] sum, float[] sumsq) {
		if (branched) {
			// or should this do the sort and collect statistics thing?
			// uh?
			// what if left and right need to be created still?
			// I guess that shouldn't happen.
			List<List<BinnedPeakList>> divided = rule.splitAtoms(atoms);
			return left.split(divided.get(0), sum, sumsq) 
				+ right.split(divided.get(1), sum, sumsq);
		} else {
			// TODO: assign particles to the correct collection!
			collectionSource = parent.getCollectionSource();
			// ...
			return 0;
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

	public Partition getLeftChild() {
		return left;
	}

	public void setLeftChild(Partition left) {
		this.left = left;
	}

	public Partition getRightChild() {
		return right;
	}

	public void setRightChild(Partition right) {
		this.right = right;
	}

	public CollectionDivider getCollectionSource() {
		return collectionSource;
	}

	public void setCollectionSource(CollectionDivider collectionSource) {
		this.collectionSource = collectionSource;
	}
	

}