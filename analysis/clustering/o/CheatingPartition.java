/**
 * 
 */
package analysis.clustering.o;

import analysis.CollectionDivider;

/**
 * @author smitht
 *
 */
public class CheatingPartition extends Partition {
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
	
	public void setLeftChild(Partition left) {
		this.left = left;
	}

	public void setRightChild(Partition right) {
		this.right = right;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
