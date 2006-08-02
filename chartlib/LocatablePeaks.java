package chartlib;

import java.awt.Point;

public interface LocatablePeaks {

	/**
	 * If a bar drawn at point p, returns the corresponding data point.
	 * @param p A point in screen coordinates.
	 * @param buf A point within buf pixels of the bar will count as part of the bar.
	 * @return The X coordinate in data space the found bar represents.
	 */
	public abstract Double getBarAt(Point p, int buf);

}