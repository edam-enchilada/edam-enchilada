package chartlib;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class DataMouseEvent extends MouseEvent {
	private Point2D dataPoint, nearDataPoint;
	private boolean inDataArea;
	
	public DataMouseEvent(Component source, int id, long when, int modifiers,
			int x, int y, int clickCount, boolean popupTrigger, int button,
			boolean inDataArea, Point2D dataPoint, Point2D nearDataPoint) {
		super(source, id, when, modifiers, x, y, clickCount,
				popupTrigger, button);
		dataPoint = new Point2D.Double(x, y);
		this.nearDataPoint = nearDataPoint;
		this.inDataArea = inDataArea;
	}
	
	public boolean isInDataArea() {
		return inDataArea;
	}


	public Point2D getNearDataPoint() {
		return nearDataPoint;
	}

	public Point2D getPoint2D() {
		return dataPoint;
	}
	
}
