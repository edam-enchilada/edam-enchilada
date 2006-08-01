package chartlib;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.event.MouseInputListener;


/**
 * A ChartArea that can have mouselistener-like things but they see the data
 * coordinates 
 * @author smitht
 *
 */
public abstract class MouseyChartArea extends AbstractMetricChartArea implements
		MouseInputListener {
	private ArrayList<MouseMotionListener> motionListeners;
	private ArrayList<MouseListener> mouseListeners;

	public MouseyChartArea() {
		motionListeners = new ArrayList<MouseMotionListener>();
		mouseListeners = new ArrayList<MouseListener>();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	/**
	 * Override this method to be able to say whether there is a data point near
	 * the given graphical (mouse) point.
	 * 
	 * <p>It's left up to you to decide what means you are "near" a data point,
	 * but the method should return null when there is no nearby data point.
	 */
	public Point2D nearDataPoint(Point p) {
		return null;
	}
	
	protected DataMouseEvent makeTranslatedEvent(MouseEvent e) {
		Point gPoint = e.getPoint(); // graphical point
		Point2D tPoint = getDataValueForPoint(gPoint); // translated point
		boolean inDataArea = isInDataArea(gPoint);
		Point2D nearPoint = nearDataPoint(gPoint);
		
		// Wow, that's a lot of parameters.
		return new DataMouseEvent((Component) e.getSource(), e.getID(), 
				e.getWhen(), e.getModifiers(), gPoint.x, gPoint.y, 
				e.getClickCount(), e.isPopupTrigger(), e.getButton(), 
				inDataArea, tPoint, nearPoint);
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
	
	
	/*
	 * MouseMotionListener methods.
	 */

	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
