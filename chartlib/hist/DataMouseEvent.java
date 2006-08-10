package chartlib.hist;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class DataMouseEvent extends MouseEvent {
	private Point2D dataPoint;
	private boolean inDataArea;
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
