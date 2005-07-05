/**
 * 
 */
package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;

/**
 * @author smitht
 *
 */
public class CheatingPartition implements Partition {
	private Partition parent;
	private Partition left;
	private Partition right;
	private SplitRule rule;
	private CollectionDivider collectionSource;
	private boolean branched;
	private Histogram[] histograms;
	
	private float[] sum;
	private float[] sumsq;
	private int count;
	
	private static final int MAX_LOCATION = 2500;
	private static int DOUBLE_MAX = MAX_LOCATION * 2;
	
//	public CheatingPartition(Partition par, SplitRule cutPoint,
//			Partition l, Partition r) {
//		parent = par;
//		rule = cutPoint;
//		left = l;
//		right = r;
//		branched = true;
//	}
	
	public CheatingPartition(Partition par) {
		parent = par;
		left = right = null;
		rule = null;
		branched = false;
		histograms = new Histogram[DOUBLE_MAX];
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
	public int split(DataWithSummary atoms) {
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

	public void setParent(Partition parent) {
		this.parent = parent;
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
