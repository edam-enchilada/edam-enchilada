package analysis.clustering.o;

/**
 * Interface for different kinds of Partitions.  See OClusterThoughts for what
 * this means.
 * @author Thomas Smith
 */


import java.util.List;
import analysis.BinnedPeakList;

public interface Partition {
	public Partition getParent();
	public Partition getLeftChild();
	public Partition getRightChild();
	
	public int classify(BinnedPeakList bpl);
	public int split(List<BinnedPeakList> atoms);
	public String rulesUp();
	public String rulesDown();
}