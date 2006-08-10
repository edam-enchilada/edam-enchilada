package chartlib.hist;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * This is a special kind of mouse event that also stores the coordinates of the
 * clicked point in graph coordinates (like, mz value and relative area).
 * 
 * @author smitht
 *
 */

public class DataMouseEvent extends MouseEvent {
	private Point2D dataPoint;
	private boolean inDataArea;
	// chartnumber: 0 for positive spectrum, 1 for negative.
	private int chartNumber;
	
	public DataMouseEvent(Component source, int id, long when, int modifiers,
			int x, int y, int clickCount, boolean popupTrigger, int button,
			boolean inDataArea, Point2D dataPoint, int chartNumber) {
		super(source, id, when, modifiers, x, y, clickCount,
				popupTrigger, button);
		this.dataPoint = dataPoint;
		this.inDataArea = inDataArea;
		this.chartNumber = chartNumber;
	}
	
	public boolean isInDataArea() {
		return inDataArea;
	}

	public Point2D getPoint2D() {
		return dataPoint;
	}

	public int getChartNumber() {
		return chartNumber;
	}
	
}
