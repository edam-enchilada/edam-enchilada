package analysis.clustering.o;

/**
 * HistList.java - Slightly modified ArrayList<Integer> for holding histograms.
 * 
 * There are two methods with changed semantics: add(int,int) (that is, with
 * a target index) will not give you an IndexOutOfBoundsException if you add
 * past the end of the list: instead it just adds 0s between the current end
 * of the list and the new element.
 * 
 * Similarly, get(index) won't give you that exception, it will just give you
 * 0s.
 * 
 * A new method has been added, addPeak(float).  When you call it, it adds 1
 * to the count in the bin that the argument belongs in.
 * 
 * It's impossible to add a peak of height 0, because for ATOFMS data you
 * never want to do this.  But this HistList thing will have to change if
 * we ever want to store signed data (where negative values are valid).  Or
 * maybe it won't have to change, but it will definitey have to be thought
 * about.   
 * 
 * TODO: Sensitivity.
 * 
 * TODO: Try different rules for splitting at 0.
 * Probably want to split where there is a significant population both with
 * and without 0.  The number of bins with at least Sensitivity hits in them
 * is actually not as nice a statistic as the straight-up number of hits.
 * 
 * @author Thomas Smith
 */


import java.util.ArrayList;

public class HistList {
	/**
	 * I have no idea what a serialVersionUID is.
	 */
	private static final long serialVersionUID = 6682890280462067261L;
	private float binWidth;
	private int particleCount;
	private int hitCount;
	
	ArrayList<Integer> list;
	
	public HistList(float width) {
		super();
		list = new ArrayList<Integer>();
		binWidth = width;
		particleCount = 0;
		hitCount = 0;
	}

	// revision that has 0's stored in the list: 1.11. HistListTest 1.3.
	private int heightToIndex(float height) {
		return (int)(height / binWidth);
	}
	private float indexToMinHeight(int index) {
		return (index) * binWidth;
	}
	
	private void increment(int index) {
		// changed semantics:  When you add something past the end of a list,
		// just add enough "0" elements for it to work.
		try {
			list.set(index, (list.get(index) + 1));
		} catch (IndexOutOfBoundsException e) {
			while(list.size() < index) {
				// the above condition is right because size = max index + 1.
				list.add(0);
			}
			list.add(index,1);
			assert(get(index) == 1);
		}
	}
	
	public Integer get(int index) {
		try {
			return list.get(index);
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public Integer get(float height) {
		try {
			return list.get(heightToIndex(height));
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public void addPeak(float height) {
		if (height < 0) {
			throw new RuntimeException("Negative data are not yet " +
					"supported by the HistList of O-Cluster!");
		}
		this.increment(heightToIndex(height));
		hitCount++;
	}
	
	public void setParticleCount(int count) {
		particleCount = count;
	}
	
	public float getIndexMin(int index) {
		return indexToMinHeight(index);
	}
	public float getIndexMax(int index) {
		return indexToMinHeight(index + 1);
	}
	public float getIndexMiddle(int index) {
		return indexToMinHeight(index) + (0.5f * binWidth);
	}
	
	public int getZeroCount() {
		return particleCount - hitCount;
	}
	
	public float getBinWidth() {
		return binWidth;
	}

	public void setBinWidth(float binWidth) {
		this.binWidth = binWidth;
	}
	
	public void clear() {
		list.clear();
		particleCount = 0;
		hitCount = 0;
	}
	public int size() {
		return list.size();
	}
}
