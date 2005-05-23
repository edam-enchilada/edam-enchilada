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

import javax.swing.event.MouseInputListener;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


/**
 * @author sulmanj
 * ZoomableChart is an extended wrapper for Chart.
 * It implements mouse and keyboard-controlled zooming
 * with visual feedback.
 * In order to provide visual feedback for mouse zooming,
 * this class is implemented as a JLayeredPane with two layers:
 * a lower layer for drawing the chart, and an upper layer for drawing
 * mouse feedback over the chart.
 */
public class ZoomableChart extends JLayeredPane implements MouseInputListener,
		AdjustmentListener {

	//the two layers
	private Chart chart;
	private ChartZoomGlassPane glassPane;
	
	private JScrollBar scrollBar;
	
	private double defaultXmin = 0;
	private double defaultXmax = 400;
	
	private final int MIN_ZOOM = 5;
/**
 * Constructs a new ZoomableChart.
 * @param chart The chart the zoomable chart will display.
 */
	public ZoomableChart(Chart chart)
	{
		this.chart = chart;
		this.glassPane = new ChartZoomGlassPane();


		
		//on an unzoomed chart, the bar fills the whole range.
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 100, 0, 100);
		scrollBar.setModel(new ScrollBarRangeModel());
		scrollBar.addAdjustmentListener(this);
		
		//layout for stacking components
		setLayout(new OverlayLayout(this));
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		
		bottomPanel.add(chart,BorderLayout.CENTER);
		bottomPanel.add(scrollBar, BorderLayout.SOUTH);
		add(bottomPanel, JLayeredPane.DEFAULT_LAYER);
		add(glassPane, JLayeredPane.DRAG_LAYER);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	
	public double getDefaultXmax() {
		return defaultXmax;
	}
	public void setDefaultXmax(double defaultXmax) {
		this.defaultXmax = defaultXmax;
	}
	public double getDefaultXmin() {
		return defaultXmin;
	}
	public void setDefaultXmin(double defaultXmin) {
		this.defaultXmin = defaultXmin;
	}
	
	
	/**
	 * This lets the class know where a drag may have started.
	 * Updates the GlassPane's start point variable.
	 * If the mouse isn't in a chart, sets the start point to null.
	 */
	public void mousePressed(MouseEvent e) 
	{
		if(e.getButton() == MouseEvent.BUTTON1 )
		{
			if(chart.getChartIndexAt(e.getPoint(),true) != -1)
				
			{
				glassPane.start = e.getPoint();
				//glassPane.setOpaque(true);
			}
			else glassPane.start = null;
		}
		else glassPane.start = null;
	}
	
	/** 
	 * If the drag is within one of the charts, (if the start point is non-null)
	 * draws a pattern following the x coordinate of the drag.
	 * Updates the glass pane's end point variable.
	 */
	public void mouseDragged(MouseEvent e) {
		if(glassPane.start != null)
		{
			glassPane.drawLine = true;
			
			if(chart.getChartIndexAt(e.getPoint(),true) != -1)
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
	}
	
	/**
	 * Adjusts when scroll bar is scrolled. 
	 *
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		double xmin = e.getValue();
		double xmax = xmin + scrollBar.getVisibleAmount();
//		System.out.println(e.toString());
//		System.out.println(scrollBar.getVisibleAmount());
//		System.out.println(scrollBar.getMaximum());
		if(xmin >= xmax) return;
		try
		{
			chart.setAxisBounds(xmin, xmax, Chart.CURRENT_VALUE, Chart.CURRENT_VALUE);
			chart.packData(false, true);
		}
		catch (IllegalArgumentException ex){}
	}
	
	/**
	 * Zooms the graph using the x bounds from the last mouse drag.
	 *
	 */
	private void performZoom()
	{
		Point minPoint = new Point(glassPane.start);
		Point maxPoint = new Point(glassPane.end);
		double xmin, xmax;
		
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
		
		if(xmin >= xmax)
		{
			repaint();
			return; //another case of zooms that are too small
		}
		
		zoom((int)xmin, (int)xmax);
		//chart.setAxisBounds(xmin, xmax, Chart.CURRENT_VALUE, Chart.CURRENT_VALUE);

	}
	
	public void zoom(double newXmin, double newXmax)
	{
		chart.packData(false, true);
		scrollBar.setValues((int)newXmin, (int)(newXmax - newXmin),
				(int)defaultXmin, (int)defaultXmax);
		
		scrollBar.updateUI();
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
					end.x, start.y);
			g.fillRect(end.x-5, start.y-5, 10, 10);
		}
	}
	
	private class ScrollBarRangeModel extends DefaultBoundedRangeModel
	{
		public int getMinimum()
		{
			return (int)defaultXmin;
		}
		public int getMaximum()
		{
			return (int)defaultXmax;
		}
//		public int getValue()
//		{
//			
//			int val =  (int) chart.getXmin(0);
//			//System.out.println(val);
//			return val;
//		}
//		public int getExtent()
//		{
//			int val = (int) (chart.getXmax(0) - chart.getXmin(0));
//			return val;
//		
//		}
	}


	
}
