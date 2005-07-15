/**
 * 
 */
package analysis.clustering.o;

/**
 * @author smitht
 *
 */
public class CheatingPartition extends Partition {
	private NumberBox stats;

	public CheatingPartition(Partition par) {
		parent = par;
		left = right = null;
		stats = new NumberBox(5000);
	}

	/* (non-Javadoc)
	 * @see analysis.clustering.o.Partition#split(java.util.List)
	 */
	public int split(DataWithSummary atoms) {
		stats.addAll(atoms);
		return 0;
	}

	@Override
	public String toString() {
		return "Terminal partition of cheatingness:  keeps histograms.";
	}
	
	public void printDimension(int dim) {
		stats.printDimension(dim);
	}

}
