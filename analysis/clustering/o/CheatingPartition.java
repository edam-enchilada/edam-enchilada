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
	public int split(List<BinnedPeakList> atoms, float[] sum, float[] sumsq) {
		for (int j = 0; j < DOUBLE_MAX; j++) {
			this.sum[j] += sum[j];
			this.sumsq[j] += sumsq[j];
		}

		Iterator<BinnedPeakList> i = atoms.iterator();

		while (i.hasNext()) {
			recordAtom(i.next());
		}
		return 0;
	}

	private void recordAtom(BinnedPeakList bpl) {
		BinnedPeak p;
		for (int i = 0; i < bpl.length(); i++) {
			p = bpl.getNextLocationAndArea();
			if (histograms[p.location + MAX_LOCATION] == null) {
				histograms[p.location + MAX_LOCATION] 
				           = new Histogram(stdDev(p.location),
				        		   count, p.location);
			}
			histograms[p.location + MAX_LOCATION].addPeak(p.area);
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
	
	private List<List<BinnedPeakList>> applyrule(List<BinnedPeakList> data) {
		//List<List<BinnedPeakList>> divided = new ArrayList();
		// TODO put actual logic here
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
	
	public float stdDev(int dim) {
		return (float) Math.sqrt((sumsq[dim + MAX_LOCATION] 
		                                - (Math.pow(sum[dim + MAX_LOCATION],2)
		                                		/count))
		                          /count);
	}

}
