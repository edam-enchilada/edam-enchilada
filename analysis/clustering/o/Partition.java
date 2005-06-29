package analysis.clustering.o;

/**
 * Interface for different kinds of Partitions.  See OClusterThoughts for what
 * this means.
 * @author Thomas Smith
 */


import java.util.List;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;

public interface Partition {
	public CollectionDivider getCollectionSource();
	public void setCollectionSource(CollectionDivider collectionSource);
	
	public Partition getParent();
	public Partition getLeftChild();
	public Partition getRightChild();
	
//	public int classify(BinnedPeakList bpl);
	// i don't know how that's going to work...
	public int split(List<BinnedPeakList> atoms, float[] sum, float[] sumsq);
	public String rulesUp();
	public String rulesDown();
}