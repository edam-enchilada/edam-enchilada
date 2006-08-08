package chartlib.hist;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.event.MouseInputListener;

import chartlib.AbstractMetricChartArea;

/*
 * Change so it can work on a Chart?  not too bad maybe?
 */

/**
 * A thing that sits between you and the things you're listening to, and
 * adds information about what the given points are on the graph.
 * <p>
 * To function, it must be a mouseinputlistener on the ChartAreas, not on
 * Charts or anything else. 
 * 
 * @author smitht
 *
 */
public class DataMouseEventTranslator implements
		MouseInputListener, HierarchyListener {
	private Component dispatcher;
	private Container topLevel;
	
	/**
	 * This constructor searches the Container for ChartAreas and adds self
	 * as a listener to them.
	 * <p>
	 * In the future, it will try to keep track of when the chartAreas get
	 * removed, and rescan the container.  Not yet implemented.  
	 * @param c
	 */
	public DataMouseEventTranslator(Container c) {
		this();
		topLevel = c;
		
		java.util.List<Component> found 
			= externalswing.Useful.findAll(AbstractMetricChartArea.class, c);
		if (found.size() == 0) {
			throw new IllegalArgumentException();
		}
		for (Component comp : found) {
			AbstractMetricChartArea ca = (AbstractMetricChartArea) comp;
			ca.addMouseListener(this);
			ca.addMouseMotionListener(this);
			ca.addHierarchyListener(this);
		}
	}
	
	public DataMouseEventTranslator() {
		dispatcher = new Box(1); // doesn't matter what it is.
	}
	
	public void addMouseMotionListener(MouseMotionListener listener) {
		dispatcher.addMouseMotionListener(listener);
	}

	public void addMouseListener(MouseListener listener) {
		dispatcher.addMouseListener(listener);
	}
	
	protected DataMouseEvent makeTranslatedEvent(MouseEvent e) {
		Point gPoint = e.getPoint(); // graphical point
		Object source = e.getSource();
		Point2D tPoint = null;
		boolean inDataArea = false;
		
		if (source instanceof AbstractMetricChartArea) {
			AbstractMetricChartArea s = (AbstractMetricChartArea) source;
			tPoint = s.getDataValueForPoint(gPoint); // translated point
			inDataArea = s.isInDataArea(gPoint);
		}
		// the event translator isn't yet used for anything except histograms,
		// and they don't need this part.  so it's commented out.
//		if (source instanceof LocatablePeaks) {
//			nearPoint = ((LocatablePeaks) source).getBarAt(gPoint, 3);
		// might be right, might not...
//		}
		
		// Wow, that's a lot of parameters.
		return new DataMouseEvent((Component) e.getSource(), e.getID(), 
				e.getWhen(), e.getModifiers(), gPoint.x, gPoint.y, 
				e.getClickCount(), e.isPopupTrigger(), e.getButton(), 
				inDataArea, tPoint);
	}
	
	public void hierarchyChanged(HierarchyEvent e) {
		System.out.println("Warning!  The identity of the charts is changing," +
				" and I don't know what to do! (DataMouseEventTranslator)");
	}
	
	public void mouseClicked(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}

	public void mouseEntered(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}

	public void mouseExited(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}

	public void mousePressed(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}

	public void mouseReleased(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}
	
	/*
	 * MouseMotionListener methods.
	 */

	public void mouseDragged(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}

	public void mouseMoved(MouseEvent e) {
		dispatcher.dispatchEvent(makeTranslatedEvent(e));
	}
}
