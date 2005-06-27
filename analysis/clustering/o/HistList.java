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
	
	public void addPeak(float height) {
		this.add((int)(height / binWidth), 1);
	}
	
	public float getBinWidth() {
		return binWidth;
	}

	public void setBinWidth(float binWidth) {
		this.binWidth = binWidth;
	}
}
