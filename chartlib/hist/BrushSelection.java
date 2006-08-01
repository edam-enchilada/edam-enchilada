package chartlib.hist;

public class BrushSelection implements Comparable {
	public int spectrum;
	public int mz;
	public float relArea;
	
	public BrushSelection(int spec, int mz, float relArea) {
		this.spectrum = spec;
		this.mz = mz;
		this.relArea = relArea;
	}
	
	public int compareTo(Object o) {
		BrushSelection that = (BrushSelection) o;
		if (spectrum != that.spectrum) {
			if (spectrum < that.spectrum) return -1;
			else return 1;
		} else if (mz != that.mz) {
			if (mz < that.mz) return -1;
			else return 1;
		} else if (relArea != that.relArea) {
			if (relArea < that.relArea) return -1;
			else return 1;
		} else return 0;
	}
	
	public String toString() {
		return "BrushSelection["+spectrum+","+mz+","+relArea+"]";
	}
}
