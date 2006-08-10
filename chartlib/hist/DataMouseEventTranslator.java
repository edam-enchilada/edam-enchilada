package chartlib.hist;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

import javax.swing.Box;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import chartlib.AbstractMetricChartArea;


/**
 * A thing that sits between you and the things you're listening to, and
 * adds information about what the given points are on the graph.
 * 
 * @author smitht
 *
 */
public class DataMouseEventTranslator implements
		MouseInputListener, HierarchyListener {
	private Component dispatcher;
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
		c.addMouseListener(this);
		c.addMouseMotionListener(this);
		
		
		// sadly, we can't do this since we need to be able to switch this listener
		// in and out (with MouseRedirector).
//		java.util.List<Component> found 
//			= externalswing.Useful.findAll(AbstractMetricChartArea.class, c);
//		if (found.size() == 0) {
//			throw new IllegalArgumentException();
//		}
//		for (Component comp : found) {
//			AbstractMetricChartArea ca = (AbstractMetricChartArea) comp;
//			ca.addMouseListener(this);
//			ca.addMouseMotionListener(this);
//			ca.addHierarchyListener(this);
//		}
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
		Object source = SwingUtilities.getDeepestComponentAt(e.getComponent(), 
				e.getX(), e.getY());
		Point2D tPoint = null;
		boolean inDataArea = false;
		int chartNum = -1;
		
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			chartNum = -1;
		}
		
		HistogramsPlot hplot;
		if (e.getComponent() instanceof HistogramsPlot) {
			hplot = (HistogramsPlot) e.getComponent();
			//Point newPoint = SwingUtilities.convertMouseEvent(source, sourceEvent, destination)
			chartNum = hplot.whichGraph(gPoint);
			// why not Chart.getChartIndexAt()?  Because there is more than one
			// chartarea per spectrum.
		} else {
			System.err.println("DataMouseEventTranslator doesn't know what to do");
		}
		
		if (source instanceof AbstractMetricChartArea) {
			Point convPoint = 
				SwingUtilities.convertPoint(e.getComponent(), gPoint, 
						(Component) source);
			AbstractMetricChartArea s = (AbstractMetricChartArea) source;
			tPoint = s.getDataValueForPoint(convPoint); // translated point
			inDataArea = s.isInDataArea(convPoint);
		}
		
		// Wow, that's a lot of parameters.
		return new DataMouseEvent((Component) e.getSource(), e.getID(), 
				e.getWhen(), e.getModifiers(), gPoint.x, gPoint.y, 
				e.getClickCount(), e.isPopupTrigger(), e.getButton(), 
				inDataArea, tPoint, chartNum);
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
