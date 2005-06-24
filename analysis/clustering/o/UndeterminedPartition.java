/**
 * 
 */
package analysis.clustering.o;

import java.util.List;

import analysis.BinnedPeakList;

/**
 * @author smitht
 *
 */
public class UndeterminedPartition implements Partition {
	private DeterminedPartition parent;
	private Partition left;
	private Partition right;
	private List<BinnedPeakList> dataBuffer;
	private List<Histogram> histograms;
	
	public UndeterminedPartition(DeterminedPartition par) {
		parent = par;
	}
	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#getParent()
	 */
	public Partition getParent() {
		return parent;
	}

	public Partition getLeftChild() {
		return null;
	}
	public Partition getRightChild() {
		return null;
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
	public int split(List<BinnedPeakList> atoms) {
		// TODO Auto-generated method stub
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

}
