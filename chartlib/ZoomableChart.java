/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's ZoomableChart class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Jonathan Sulman sulmanj@carleton.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

/*
 * Created on Mar 3, 2005
 *
 */

package chartlib;

import gui.ParticleAnalyzeWindow;

import javax.swing.event.MouseInputListener;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Scanner;


/**
 * 
 * ZoomableChart is an extended wrapper for Chart.
 * It implements mouse and keyboard-controlled zooming
 * with visual feedback.
 * In order to provide visual feedback for mouse zooming,
 * this class is implemented as a JLayeredPane with two layers:
 * a lower layer for drawing the chart, and an upper layer for drawing
 * mouse feedback over the chart.
 * <p>
 * It could be a good project to make the chart be double buffered, so that it
 * does not have to redraw from scratch every time you drag the mouse to zoom.
 * The ComponentListener interface might be useful for detecting when the chart
 * really needs to be redrawn, rather than when the cached copy can get stuck
 * on the screen.
 * <p>
 * This might also be done with a RepaintManager, or with the Swing property
 * setDoubleBuffered, or something.  I don't understand how all that works.
 * http://java.sun.com/products/jfc/tsc/articles/painting/
 * 
 * @author sulmanj
 * @author smitht
 * 
 */
public class ZoomableChart extends JLayeredPane implements MouseInputListener,
		AdjustmentListener, KeyListener {

	//the two layers
	private Zoomable chart;
	private ChartZoomGlassPane glassPane;
	
	private JScrollBar scrollBar;
	
	// these are the maximum and minimum indices, in chart coordinates,
	// that are displayed.
	private double cScrollMin = 0;
	private double cScrollMax = 400;
	
	// this is the value of cScrollMax that is returned to when you go to
	// the default zoom level.
	private double defaultCScrollMax = cScrollMax;
	private double defaultCScrollMin = cScrollMin;
	
	// these are the minimum and maximum indices in scrollbar coordinates,
	// which are different from chart coordinates, sadly.
	private final int S_SCROLL_MIN = 0;
	private final int S_SCROLL_MAX = Integer.MAX_VALUE;
	// they can't be the same coordinates because some chart coordinates are
	// too big to be represented by integers, but integers are all that
	// scrollbars know how to deal with.
	
	// a rather arbitrary limit on the distance that you can zoom in.
	private final int MIN_ZOOM = 5;
	
	private boolean forceY = false;
	
	/**
	 * Constructs a new ZoomableChart.
	 * @param chart The chart the zoomable chart will display.
	 */
	public ZoomableChart(Zoomable chart)
	{
		this.chart = chart;
		this.glassPane = new ChartZoomGlassPane();

		double[] xRange = chart.getVisibleXRange();
		if (xRange != null) {
			cScrollMin = xRange[0];
			cScrollMax = xRange[1];
			defaultCScrollMax = xRange[1];
		}
		
		//on an unzoomed chart, the bar fills the whole range.
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, S_SCROLL_MIN, S_SCROLL_MAX,
					S_SCROLL_MIN, S_SCROLL_MAX);
		scrollBar.setModel(new DefaultBoundedRangeModel(S_SCROLL_MIN, S_SCROLL_MAX,
					S_SCROLL_MIN, S_SCROLL_MAX));
		scrollBar.addAdjustmentListener(this);
		
		//layout for stacking components
		setLayout(new OverlayLayout(this));
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		
		if (chart instanceof Component) {
			Component c = (Component) chart;
			bottomPanel.add(c, BorderLayout.CENTER);
			c.addMouseMotionListener(this);
			c.addMouseListener(this);
		} else {
			// I can't figure out how to re-jigger inheritance so that Zoomable
			// can force things to be Components.  Nor do I know if I should. -tom
			throw new RuntimeException("Whoops!  Time to redesign chartlib!");
		}
		bottomPanel.add(scrollBar, BorderLayout.SOUTH);
		add(bottomPanel, JLayeredPane.DEFAULT_LAYER);
		add(glassPane, JLayeredPane.DRAG_LAYER);
		
		setFocusable(true);
		// adding mouse listeners to the glass pane 
		// makes the scroll bar inaccessible to the mouse.
//		addMouseListener(this);
//		addMouseMotionListener(this);
		addKeyListener(this);
		
		glassPane.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	/**
	 * Find the maximum accessible value on the chart.
	 * @return a number in x-coordinates on the chart.
	 */
	public double getCScrollMax() {
		return cScrollMax;
	}
	/**
	 * Set the maximum value, in chart coordinates, that will be accessible
	 * with the scroll bar all the way to the right.
	 * <p>
	 * When the user does something like zoomOutHalf, more of the chart will
	 * be visible.  But when you go to another atom, or something like that,
	 * the maximum value will get set back to this.
	 */
	public void setCScrollMax(double defaultXmax) {
		this.cScrollMax = defaultXmax;
		this.defaultCScrollMax = defaultXmax;
	}
	/**
	 * Find the minimum accessible value on the chart.
	 * @return a number in x-coordinates on the chart.
	 */
	public double getCScrollMin() {
		return cScrollMin;
	}
	/**
	 * Sets the minimum value accessible with the scrollbar.
	 * @param defaultXmin
	 */
	public void setCScrollMin(double defaultXmin) {
		this.cScrollMin = defaultXmin;
	}
	
	
	/**
	 * This lets the class know where a drag may have started.
	 * Updates the GlassPane's start point variable.
	 * If the mouse isn't in a chart, sets the start point to null.
	 * 
	 * Part of the MouseListener interface.
	 */
	public void mousePressed(MouseEvent e) 
	{
		if(e.getButton() == MouseEvent.BUTTON1 )
		{
//			if (chart instanceof JComponent) {
//				Point p = e.getPoint();
//				// Find the component that the click happened on.
//				Component c = ((JComponent) chart).findComponentAt(e.getPoint());
//				
//				if (c instanceof Zoomable) {
//					// put the point into the coordinates of the
//					// destination component.
//					Point startRef = this.getLocationOnScreen(), 
//						endRef = c.getLocationOnScreen(); 
//					p.translate(endRef.x - startRef.x, endRef.y - startRef.y);
//					
//					if (((Zoomable) c).isInDataArea(p)) {
//						glassPane.start = e.getPoint();
//						return;
//					} else {
//						// click is not in data area
//						glassPane.start = null;
//					}
//				} else {
//					// click is not on a graph at all.
//					glassPane.start = null;
//				}
//			} else 
			if (chart.isInDataArea(e.getPoint())) {
				glassPane.start = e.getPoint();
				//glassPane.setOpaque(true);
			} else glassPane.start = null;
		} else glassPane.start = null;
	}
	

	/** 
	 * If the drag is within one of the charts, (if the start point is non-null)
	 * draws a pattern following the x coordinate of the drag.
	 * Updates the glass pane's end point variable.
	 * 
	 * Part of MouseListener
	 */
	public void mouseDragged(MouseEvent e) {
		if(glassPane.start != null)
		{
			glassPane.drawLine = true;
			
			/* 
			 * don't need to change for scrollbar changes, since this just
			 * sees if the point is on the chart or not. 
			 */
			if(chart.isInDataArea(e.getPoint()))
			{
				Point oldEnd;
				if(glassPane.end != null) oldEnd = glassPane.end;
				else oldEnd = e.getPoint();
				glassPane.end = e.getPoint();
				if(glassPane.start.x < oldEnd.x)
				{
					repaint(glassPane.start.x - 10,
							glassPane.start.y - 5,
							oldEnd.x + 20 - glassPane.start.x,
							10);
				}
				else
					repaint(oldEnd.x - 10,
							glassPane.start.y - 5,
							glassPane.start.x + 20 - oldEnd.x,
							10);
			}
		}
	}

	/**
	 * Lets the class know a drag has ended.
	 */
	public void mouseReleased(MouseEvent e) {
		//glassPane.end = e.getPoint();  //mouseDragged provides this info already.
										//and this may cause errors on chart edges.
		glassPane.drawLine = false;
		if(glassPane.start != null && glassPane.end != null)
		{
			performZoom();
		}
		//this.repaint();
	}
	
	/**
	 * Called whenever the scroll bar is scrolled, this changes the viewed
	 * area of the chart to fit the values of the scroll bar.
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		int scrollmin = e.getValue();
		int scrollmax = scrollmin + scrollBar.getVisibleAmount();
		if(scrollmin >= scrollmax) return;
		
		
		double xmin = scrollToChart(scrollmin);
		double xmax = scrollToChart(scrollmax);
		
		try
		{
			chart.setXAxisBounds(xmin, xmax);
			chart.packData(false, true, forceY);
		}
		catch (IllegalArgumentException ex){
			System.out.println("Illegal Argument");
		}
	}
	
	/**
	 * Convert from a number given by the scroll bar to a number that
	 * makes sense to the chart (like x-coordinates).
	 * @param scrollValue
	 * @return
	 */
	private double scrollToChart(int scrollValue) {
		double maxExtent = cScrollMax - cScrollMin;
		return ((((double) scrollValue) / Integer.MAX_VALUE) * maxExtent) 
			+ cScrollMin;
	}
	
	/**
	 * Convert from a number given by the chart to one for the scrollbar.
	 * @param chartValue
	 * @return
	 */
	private int chartToScroll(double chartValue) {
		double maxExtent = cScrollMax - cScrollMin;
		return (int) 
			(((chartValue - cScrollMin) / maxExtent) * Integer.MAX_VALUE);
	}
	
	/**
	 * Zooms the graph using the x bounds from the last mouse drag.
	 *
	 */
	private void performZoom()
	{
		//System.out.println("Performing zoom");
		Point minPoint = new Point(glassPane.start);
		Point maxPoint = new Point(glassPane.end);
		double xmin, xmax;
		
		// find the relevant chart
		int chartIndex = ((Chart)chart).getChartIndexAt(minPoint);
		// set the cScroll values to the max and min values of the relevant chartArea
		cScrollMax = ((Chart)chart).getXmax(chartIndex);
		cScrollMin = ((Chart)chart).getXmin(chartIndex);
		
		//makes a left-to-right drag equivalent to a right-to-left drag
		if(minPoint.x > maxPoint.x){
			minPoint.x = maxPoint.x;
			maxPoint.x = glassPane.start.x;
		}
		if(maxPoint.x - minPoint.x < MIN_ZOOM)
		{
			repaint();	//get rid of grey dots
			return; //zooms that are too small
		}
		
		xmin = chart.getDataValueForPoint(minPoint).x;
		xmax = chart.getDataValueForPoint(maxPoint).x;
		// these are in chart coordinates.
		
		if(xmin >= xmax)
		{
			repaint();
			return; //another case of zooms that are too small
		}
		zoom(xmin, xmax);
		//chart.setAxisBounds(xmin, xmax, Chart.CURRENT_VALUE, Chart.CURRENT_VALUE);

	}
	
	/**
	 * Set the zoom so that newXmin and newXmax will be the minimum and maximum
	 * visible coordinates on the chart---though not necessarily the maximum
	 * accessible, by moving the scroll bar.
	 * @param newXmin
	 * @param newXmax
	 */
	public void zoom(double newXmin, double newXmax)
	{
		if (newXmax > cScrollMax) {
			scrollBar.setEnabled(false);
		} else {
			scrollBar.setEnabled(true);
		}
		int scrollMin = chartToScroll(newXmin);
		int scrollMax = chartToScroll(newXmax);
		// changing the values on the scrollbars activates the adjustmentValueChanged method
		// since ZoomableChart is an ActionListener
		// Note: this may not actually change the values if the parameters don't meet a certain inequality
		// this results in the zooming not actually happening
		scrollBar.setValues(scrollMin + 1, scrollMax - scrollMin,
				S_SCROLL_MIN, S_SCROLL_MAX);
		scrollBar.setValues(scrollMin, scrollMax - scrollMin,
				S_SCROLL_MIN, S_SCROLL_MAX);
		// XXX: why twice?  to force the scrollBar to realise that something has
		// changed.  true, that's a dumb way to do it, but i'm not smart. -tom
		
		scrollBar.setBlockIncrement(scrollMax - scrollMin);
		
		scrollBar.updateUI();
	}
	
	/**
	 * Zoom out so that the current view of the Chart occupies half of the
	 * viewing area.  This also makes sure that nothing less than 0 is visible.
	 *
	 */
	public void zoomOutHalf() {
		double[] range = chart.getVisibleXRange();
		if (range == null) return;
		double xmin = range[0], xmax = range[1];
		double diff = (xmax - xmin) / 2.0;
		xmin -= diff; xmax += diff;
		
		/*
		 * if we would be zooming to the left of 0, change it so we're not.
		 */
		if (xmin < 0) {
			xmax = xmax + (- xmin);
			xmin = 0;
		}
		
		zoom(xmin, xmax);
	}
	
//	/**
//	 * For testing: outputs the chart point of the click.
//	 */
//	public void mouseClicked(MouseEvent e) {
//		int cIndex = chart.getChartAt(e.getPoint(),true);
//		
//		java.awt.geom.Point2D.Double p; 
//		if(cIndex != -1)
//		{
//			p = chart.getDataValueForPoint(cIndex, e.getPoint());
//			//System.out.println("Point clicked in chart " + cIndex);
//			//System.out.println("Coordinates: " + p.x + ", " + p.y);
//		}
//		
//	}
//	
	
	//extra mouseListener events.
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	
	/**
	 * 
	 * @author sulmanj
	 *
	 * Transparent pane that draws feedback for mouse zooming.
	 */
	private class ChartZoomGlassPane extends javax.swing.JPanel
	{
		public boolean drawLine = false;
		public Point start;
		public Point end;
		
		public ChartZoomGlassPane()
		{
			//well, you can see through it, can't you?
			setOpaque(false);
		}
		
		
		/**
		 * During a drag, paints a horizontal line following the mouse.
		 */
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2d = (Graphics2D)g.create();
			if(drawLine && start != null && end != null)
			{
				drawDragFeedback(g2d);
			}
			g2d.dispose();
		}
		
		
		/**
		 * Draws a a pattern to indicate where the mouse has been dragged.
		 * @param g
		 */
		public void drawDragFeedback(Graphics2D g)
		{
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(3));
			g.fillRect(start.x-5, start.y-5, 10,10);
			g.drawLine(start.x, start.y, 
					end.x, start.y); // a horizontal line
			g.fillRect(end.x-5, start.y-5, 10, 10);
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_Z) {
			zoom(cScrollMin, defaultCScrollMax);
		}
	}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {
		
	}

	/**
	 * Returns whether or not to force the bottom of the Y axis to be at 0.
	 * @author smitht
	 */
	public boolean isForceY() {
		return forceY;
	}

	/**
	 * Set whether or not to force the bottom of the Y axis to be at 0.
	 */
	public void setForceY(boolean forceY) {
		this.forceY = forceY;
	}

}