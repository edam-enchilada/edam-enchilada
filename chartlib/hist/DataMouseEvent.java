package chartlib.hist;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class DataMouseEvent extends MouseEvent {
	private Point2D dataPoint;
	private boolean inDataArea;
	
	public DataMouseEvent(Component source, int id, long when, int modifiers,
			int x, int y, int clickCount, boolean popupTrigger, int button,
			boolean inDataArea, Point2D dataPoint) {
		super(source, id, when, modifiers, x, y, clickCount,
				popupTrigger, button);
		dataPoint = new Point2D.Double(x, y);
		this.inDataArea = inDataArea;
	}
	
	public boolean isInDataArea() {
		return inDataArea;
	}

	public Point2D getPoint2D() {
		return dataPoint;
	}
	
}
