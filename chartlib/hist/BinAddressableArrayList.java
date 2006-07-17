package chartlib.hist;

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
 * TODO: Try different rules for splitting at 0.
 * Probably want to split where there is a significant population both with
 * and without 0.  The number of bins with at least Sensitivity hits in them
 * is actually not as nice a statistic as the straight-up number of hits.
 * 
 * @author Thomas Smith
 */


import java.util.ArrayList;

/*
 * Basically, a HistList is an ArrayList that can be accessed by peak height
 * rather than integral index.  You tell it how wide an index will be, and
 * it does the rest.
 */

public class BinAddressableArrayList<T> {
	/**
	 * I have no idea what a serialVersionUID is.
	 */
	private static final long serialVersionUID = 6682890280462067242L;
	private float binWidth;

	
	protected ArrayList<T> list;
	
	public BinAddressableArrayList(float binWidth) {
		super();
		list = new ArrayList<T>();
		this.binWidth = binWidth;
	}

	// revision that has 0's stored in the list: 1.11. HistListTest 1.3.
	/**
	 * Map float index to integral index
	 */
	protected int heightToIndex(float height) {
		return (int)(height / binWidth);
	}
	
	/**
	 * Map integral index to (minimum) float index
	 */
	protected float indexToMinHeight(int index) {
		return (index) * binWidth;
	}
	
	/**
	 * Get this index from the ArrayList
	 * 
	 * @note This used to be implemented using a try/catch block, which was
	 * bad coding style.  But it also made it at least 10 times slower.  There's
	 * another copy of this code floating around somewhere, which would benefit
	 * from this speedup if it ever gets used again.
	 */
	public T getByIndex(int index) {
		if (list.size() <= index) {
			return null;
		} else {
			return list.get(index);
		}
	}
	
	/**
	 * Get the contents of the bin that *height* would fall into.
	 */
	public T get(float height) throws IndexOutOfBoundsException {
		int index = heightToIndex(height);
		if (list.size() <= index) {
			return null;
		} else {
			return list.get(index);
		}
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
	
	public float getBinWidth() {
		return binWidth;
	}

	protected void setBinWidth(float binWidth) {
		this.binWidth = binWidth;
	}
	
	public void clear() {
		list.clear();
	}
	
	/**
	 * The maximum bin index + 1, not the number of particles represented 
	 * in the histogram.
	 */
	public int size() {
		return list.size();
	}
	
	protected void expandAndSet(float peakHeight, T value) {
		ensureBinExists(peakHeight);
		list.set(heightToIndex(peakHeight), value);
	}
	
	protected void ensureBinExists(float targetHeight) {
		int binsToAdd = 1 + 
			(int) ((targetHeight - indexToMinHeight(list.size())) / binWidth);
		while (binsToAdd >= 0) {
			list.add(null);
			binsToAdd--;
		}
	}
}
