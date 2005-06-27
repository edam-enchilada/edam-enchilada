package analysis.clustering.o;

import java.util.ArrayList;

public class HistList extends ArrayList<Integer> {
	private float binWidth;
	
	public void add(int index, int elem) {
		// changed semantics:  When you add something past the end of a list,
		// just add enough "0" elements for it to work.
		try {
			super.add(index, elem);
		} catch (IndexOutOfBoundsException e) {
			while(size() < index) {
				// the above condition is right because size = max index + 1.
				super.add(0);
			}
			super.add(index,elem);
			assert(get(index) == elem);
		}
	}
	
	// it might be faster to try { } catch (IndexOutOf...etc.).
	// since this puts an extra 2 tests in the common case.  It probably
	// doesn't actually matter?
	public Integer get(int index) {
		// In a histogram, if there's nothing stored somewhere, that's
		// implicitly a count of 0.
		if (index < 0 || index >= size()) {
			return 0;
		} else {
			return super.get(index);
		}
	}
	
	public void addPeak(float height) {
		this.add((int)(height / binWidth), 1);
	}
	
	public float getIndexMin(int index) {
		return index * binWidth;
	}
	public float getIndexMax(int index) {
		return (index+1) * binWidth;
	}
	
	public float getBinWidth() {
		return binWidth;
	}

	public void setBinWidth(float binWidth) {
		this.binWidth = binWidth;
	}
}
