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
 * @author Thomas Smith
 */


import java.util.ArrayList;

public class HistList extends ArrayList<Integer> {
	/**
	 * I have no idea what a serialVersionUID is.
	 */
	private static final long serialVersionUID = 6682790280462067261L;
	private float binWidth;
	
	public HistList(float width) {
		super();
		binWidth = width;
	}

	public void incrementBy(int index, int elem) {
		// changed semantics:  When you add something past the end of a list,
		// just add enough "0" elements for it to work.
		try {
			super.set(index, (super.get(index) + elem));
		} catch (IndexOutOfBoundsException e) {
			while(size() < index) {
				// the above condition is right because size = max index + 1.
				super.add(0);
			}
			super.add(index,elem);
			assert(get(index) == elem);
		}
	}
	
	public Integer get(int index) {
		try {
			return super.get(index);
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public void addPeak(float height) {
		this.incrementBy((int)(height / binWidth), 1);
	}
	
	public float getIndexMin(int index) {
		return index * binWidth;
	}
	public float getIndexMax(int index) {
		return (index+1) * binWidth;
	}
	public float getIndexMiddle(int index) {
		return (index + 0.5f) * binWidth;
	}
	
	public float getBinWidth() {
		return binWidth;
	}

	public void setBinWidth(float binWidth) {
		this.binWidth = binWidth;
	}
}
