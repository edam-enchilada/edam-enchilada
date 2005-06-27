/**
 * 
 */
package analysis.clustering.o;

import java.util.List;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;

/**
 * @author smitht
 *
 */
public class UndeterminedPartition implements Partition {
	private Partition parent;
	private Partition left;
	private Partition right;
	private List<BinnedPeakList> dataBuffer;
	private List<Histogram> histograms;
	private CollectionDivider collectionSource;
	

	public UndeterminedPartition(Partition par) {
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

	private void addAtom(BinnedPeakList l) {
		BinnedPeak peak;
		for (int i = 0; i < l.length(); i++) {
			peak = l.getNextLocationAndArea();
			histograms.get(peak.location).addPeak(peak.area);
		}
	}
	
	public CollectionDivider getCollectionSource() {
		return collectionSource;
	}
	public void setCollectionSource(CollectionDivider collectionSource) {
		this.collectionSource = collectionSource;
	}
	
}
