package chartlib.hist;

public class BrushSelection implements Comparable {
	public int mz;
	public float min;
	public float max;
	
	public BrushSelection(int mz, float min, float max) {
		this.mz = mz;
		this.min = min;
		this.max = max;
	}
	
	public BrushSelection(int mz, float relArea) {
		this(mz, relArea, relArea);
	}
	
	public int compareTo(Object o) {
		BrushSelection that = (BrushSelection) o;
		if (mz != that.mz) {
			if (mz < that.mz) return -1;
			else return 1;
		} else if (min != that.min) {
			if (min < that.min) return -1;
			else return 1;
		} else if (max != that.max) {
			if (max < that.max) return -1;
			else return 1;
		} else return 0;
	}
	
	public String toString() {
		return "BrushSelection["+mz+","+min+"-"+max+"]";
	}
}
