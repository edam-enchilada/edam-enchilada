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
 * The Original Code is EDAM Enchilada's ChartArea class.
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
 * Created on Feb 1, 2005
 *
 * 
 */
package chartlib;

import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;
import java.util.Iterator;

/**
 * @author sulmanj
 *
 * A single data chart with labeled axes and data.
 */
public class ChartArea extends JComponent {
	
	private GraphAxis xAxis;
	private GraphAxis yAxis, yAxis2;
	private Dataset dataset1, dataset2;
	private Color color, color2;
	private double[] bars = null; //the data bars.  used for hit detection.
	
	//spacers between axes and edges to allow for tick mark labels and axis titles.
	private static final int H_AXIS_PADDING = 15;
	private static final int V_AXIS_PADDING = 50;
	private static final int H_TITLE_PADDING = 20;
	private static final int V_TITLE_PADDING = 20;
	private static final int RIGHT_HAND_V_TITLE_PADDING = 5;
	private static final int RIGHT_PADDING = 15;
	private static final int TOP_PADDING = 15;
	
	//length of big ticks
	private static final int BIG_TICK_LENGTH = 10;    
	private static final int SMALL_TICK_LENGTH = 5;
	
	
	//variable values of display elements
	private int barWidth = 5, barWidth2 = 5;  //how wide each bar is, in pixels
	private int dotWidth = 0;	//how big dots are in line graphs, in pixels
	private double xmin, xmax, ymin, ymax, ymin2 = -1, ymax2 = -1;  //bounds of the axes
	private double bigTicksX = 0, bigTicksY = 0; //big ticks are multiples of these
	private int smallTicksX = 1, smallTicksY = 1; //number of small ticks between each big tick
	private String titleX, titleY, titleY2;
	private int numSmartTicksX = -1;	//if negative, set ticks by using multiples of a number.
	private int numSmartTicksY = -1;						// if positive, always have this many ticks.
	
	private boolean showBars = true, showBars2 = true;
	private boolean showLines = false, showLines2 = false;
	
	
	/**
	 * Default constructor.  Makes a new ChartArea with default
	 * limits of 0 - 10 for both axes.
	 *
	 */
	public ChartArea()
	{
		this(0,10,0,10);
	}
	
	/**
	 * Creates a chart area with the specified axis limits but no data.
	 * @param xmin Minimum of x axis.
	 * @param xmax Maximum of x axis.
	 * @param ymin Minimum of y axis.
	 * @param ymax Maximum of y axis.
	 */
	public ChartArea(double xmin, double xmax, double ymin, double ymax)
	{
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
		bigTicksX = 0;
		bigTicksY = 0;
		color = Chart.DATA_COLORS[0];
		
		titleX = "X Axis";
		titleY = "Y Axis";
		
		createAxes();
	}
	
	/**
	 * Creates a chart area based on a dataset, setting bounds
	 * to include all the data.
	 * 
	 * @param ds The dataset.
	 */
	public ChartArea(Dataset ds)
	{
		titleX = "X Axis";
		titleY = "Y Axis";
		bigTicksX = 0;
		bigTicksY = 0;
		color = Chart.DATA_COLORS[0];
		
		dataset1 = ds;
		
		pack();
	}
	
	public ChartArea(Dataset dataset1, Dataset dataset2) {
		titleX = "X Axis";
		titleY = "Y Axis";
		bigTicksX = 0;
		bigTicksY = 0;
		color = Chart.DATA_COLORS[0];

		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		
		pack();
	}
	
	/**
	 * Sets a new dataset to be displayed.  Does not alter any other values.
	 * @param ds The new dataset to be displayed.
	 */
	public void setDataset(Dataset ds) {
		setDataset(0, ds);
	}
	
	/**
	 * Sets a new dataset to be displayed.  Does not alter any other values.
	 * @param ds The new dataset to be displayed.
	 * @param index The index of the dataset in this area
	 */
	public void setDataset(int index, Dataset ds)
	{
		if (index == 0)
			dataset1 = ds;
		else
			dataset2 = ds;
		
		//generateAxisLimits();
		createAxes();
		repaint();
	}
	
	/**
	 * Sets new values for the X axis ticks.
	 * @param bigX Big ticks on the X axis are multiples of this.
	 * @param smallX Number of small ticks on the X axis between each big tick.
	 */
	public void setTicksX(double bigX, int smallX)
	{
		bigTicksX = bigX;
		smallTicksX = smallX;
		numSmartTicksX = -1;
		
		createAxes();
		repaint();
	}
	
	/**
	 * Sets new values for the x axis ticks by ensuring that there are always
	 * bigTicks number of big ticks and smallTicks number of small ticks between
	 * each big tick.
	 * @param numTicks Number of big ticks on the X axis.
	 * @param smallX Number of small ticks between each big tick.
	 */
	public void setNumTicksX(int bigTicks, int smallTicks)
	{
		assert(bigTicks > 1 && smallTicks >= 0);
		numSmartTicksX = bigTicks;
		createAxes();
		repaint();
	}
	
	/**
	 * Sets new values for the Y axis ticks.
	 * @param bigY Big ticks on the Y axis are multiples of this.
	 * @param smallY Number of small ticks on the Y axis between each big tick.
	 */
	public void setTicksY(double bigY, int smallY)
	{
		bigTicksY = bigY;
		smallTicksY = smallY;
		numSmartTicksY = -1;
		
		createAxes();
		repaint();
	}
	
	/**
	 * Sets new values for the y axis ticks by ensuring that there are
	 * bigTicks number of big ticks and smallTicks number of small ticks between
	 * each big tick.  The number of ticks may be off by one or two because
	 * ticks may be on endpoints.
	 * @param numTicks Number of big ticks on the Y axis.
	 * @param smallX Number of small ticks between each big tick.
	 */
	public void setNumTicksY(int bigTicks, int smallTicks)
	{
		assert(bigTicks > 1 && smallTicks >= 0);
		numSmartTicksY = bigTicks;
		createAxes();
		repaint();
	}
	
	
	/**
	 * Sets new boundaries for the axes and displayed data.
	 * @param xmin Minimum of X axis.
	 * @param xmax Maximum of X axis.
	 * @param ymin Minimum of Y axis.
	 * @param ymax Maximum of Y axis.
	 */
	public void setAxisBounds(int index, double xmin, double xmax, double ymin, double ymax)
	throws IllegalArgumentException
	{
		//check for errors
		if(xmin >= xmax) throw new IllegalArgumentException("Xmin >= Xmax.");
		else if(ymin >= ymax) throw new IllegalArgumentException("Ymin >= Ymax.");
		
		
		this.xmin = xmin;
		this.xmax = xmax;
		
		if (index == 0) {
			this.ymin = ymin;
			this.ymax = ymax;
		} else {
			this.ymin2 = ymin;
			this.ymax2 = ymax;
		}
		
		createAxes();
		repaint();
	}
	
	
	/**
	 * Sets a new title for the X axis.
	 * @param titleX New X axis title.
	 */
	public void setTitleX(String titleX)
	{
		this.titleX = titleX;
		
		repaint();
	}
	
	/**
	 * Sets a new title for the Y axis.
	 * @param titleY New Y axis title.
	 */
	public void setTitleY(int index, String titleY)
	{
		if (index == 0)
			this.titleY = titleY;
		else
			this.titleY2 = titleY;
		
		repaint();
	}
	
	/**
	 * Sets the width of the bars of the graph in pixels.
	 * @param width New width for graph bars, in pixels.
	 */
	public void setBarWidth(int index, int width)
	{
		if (index == 0)
			this.barWidth = width;
		else
			this.barWidth2 = width;

		repaint();
	}
	
	
	
	
	/**
	 * Sets a new color for the data.
	 */
	public void setColor(int index, Color c)
	{
		if (index == 0)
			color = c;
		else
			color2 = c;
		
		repaint();
	}
	
	/**
	 * Determines how data is displayed.  If neither is true, no data
	 * will be displayed.
	 * @param showBars If true, chart will display data in bars.
	 * @param showLines If true, chart will dislay data in lines.
	 */
	public void setDataDisplayType(int index, boolean showBars, boolean showLines)
	{
		if (index == 0) {
			this.showBars = showBars;
			this.showLines = showLines;
		} else {
			this.showBars2 = showBars;
			this.showLines2 = showLines;
		}
		//bars = null;
		repaint();
	}
	
	/**
	 * Registers the given data x coordinates to be used for hit detection.
	 * Overwrites previous hit detection data.
	 * In Enchilada, this allows a graph to display a full spectrum while still
	 * allowing the user to find the peaks.
	 * @param xCoords An array of x coordinates in data space.
	 */
	public void setHitDetectCoords(double[] xCoords)
	{
		Rectangle dataArea = getDataAreaBounds();
		bars = new double[xCoords.length];
		for(int count = 0; count < bars.length; count++)
		{
			bars[count] = xCoords[count];
		}
	}

	/**
	 * @return Returns the bar width.
	 */
	public int getBarWidth() {
		return barWidth;
	}
	
	/**
	 * @return Returns the big ticks factor for the x axis.
	 * Big ticks are multiples of this number.
	 */
	public double getBigTicksX() {
		return bigTicksX;
	}
	
	/**
	 * @return Returns the big ticks factor for the y axis.
	 * Big ticks are multiples of this number.
	 */
	public double getBigTicksY() {
		return bigTicksY;
	}
	
	/**
	 * @return Returns the color.
	 */
	public Color getColor(int index) {
		return index == 0 ? color : color2;
	}
	
	/**
	 * @return YAxis, depending on index
	 */
	
	public GraphAxis getYAxis(int index) {
		return index == 0 ? yAxis : yAxis2;
	}
	
	/**
	 * @return Returns the dataset.
	 */
	public Dataset getDataset(int index) {
		return index == 0 ? dataset1 : dataset2;
	}
	
	/**
	 * @return Returns the number of small ticks between each big tick on the X axis.
	 */
	public int getSmallTicksX() {
		return smallTicksX;
	}
	/**
	 * @return Returns the number of small ticks between each big tick on the X axis.
	 */
	public int getSmallTicksY() {
		return smallTicksY;
	}
	/**
	 * @return Returns the title of the X axis.
	 */
	public String getTitleX() {
		return titleX;
	}
	/**
	 * @return Returns the title of the Y axis.
	 */
	public String getTitleY() {
		return titleY;
	}
	
	
	/**
	 * @return Returns upper limit of the x axis.
	 */
	public double getXmax() {
		return xmax;
	}
	/**
	 * @return Returns the lower limit of the x axis.
	 */
	public double getXmin() {
		return xmin;
	}
	/**
	 * @return Returns the upper limit of the y axis.
	 */
	public double getYmax() {
		return ymax;
	}
	/**
	 * @return Returns the lower limit of the y axis.
	 */
	public double getYmin() {
		return ymin;
	}
	
	
	/**
	 * Tells whether a point is in the data area of the
	 * chartArea (not the title or axis areas).
	 * @param p The point to check.
	 * @return True if the point is in the data display area of
	 * the chart.
	 */
	public boolean isInDataArea(Point p)
	{
		Dimension size = getSize();
		Rectangle dataArea = getDataAreaBounds();
		int x = p.x;	
		int y = p.y;
		
		if(x > dataArea.x
				&& x < dataArea.x + dataArea.width
				&& y > dataArea.y
				&& y < dataArea.y + dataArea.height
		)
		{
			return true;
		}
		else return false;
	}
	
	/**
	 * If a bar drawn at point p, returns the corresponding data point.
	 * @param p A point in screen coordinates.
	 * @param buf A point within buf pixels of the bar will count as part of the bar.
	 * @return The X coordinate in data space the found bar represents.
	 */
	public Double getBarAt(Point p, int buf)
	{
		if(bars == null) return null;
		int i;
		Rectangle testbar;
		Rectangle dataArea = getDataAreaBounds();
		//Iterator<DataPoint> dataIt = dataset.iterator();
		//DataPoint dp;
		

		for(i = 0; i < bars.length; i++)
		{
			//dp = dataIt.next();

			testbar = new Rectangle((int) (xAxis.relativePosition(bars[i]) * dataArea.width 
						+ dataArea.x) - buf,
					dataArea.y,
					barWidth + buf*2, dataArea.height);
			if(testbar.contains(p)) return new Double(bars[i]);
			
		}
		return null;
	}
	
	
	/**
	 * Translates a point in screen space to chart coordinates.
	 * @param p The point in screen coordinates.
	 * @return The point translated to the chart's coordinate system,
	 * or null if the point is not within the data area.
	 */
	public Point2D.Double getDataValueForPoint(Point p)
	{
		if(!isInDataArea(p)) return null;
		
		Dimension size = getSize();
		Point2D.Double result = new Point2D.Double();
		Rectangle dataArea = getDataAreaBounds();
		
		double x = p.x, y = p.y;
		//translate to data area origin
		x = x - dataArea.x; 
		y = dataArea.y + dataArea.height - y; //screen coordinate origin is at top,
									// but data origin is at bottom, so we subtract.
		
		//scale to chart coordinates.
		x = x * (xmax - xmin) / (dataArea.width);
		y = y * (ymax - ymin) / (dataArea.height);
		
		//translate to axis origins
		x = x + xmin;
		y = y + ymin;
		
		
		result.x = x;
		result.y = y;
		return result;
	}
	
	
//	/**
//	 * Finds the coordinate in the chart's display space
//	 * corresponding to the given data value.
//	 * @param x The data value to transform to screen coordinates.
//	 * @return The X coordinate in screen space of x, relative to the chart's
//	 * data area.  Returns -1 if x is not within the chart's bounds.
//	 */
//	public int getXCoordForDataValue(double x)
//	{
//		if(x < xmin || x > xmax)
//			return -1;
//		
//		Dimension size = getSize();
//		return  (int)(xAxis.relativePosition(x)
//				* (getDataAreaBounds().width));
//	}
	
	
	/**
	 * Sets the bounds of the chart to new values that fit the dataset.
	 */
	public void pack()
	{
		pack(0, true, true);
		pack(1, true, true);
	}
	
	/**
	 * Sets the bounds of either or both axes to fit the dataset.
	 * If the dataset is empty, leaves the axes alone.
	 * If only packY is true, adjusts the data to fit the y values of the points
	 * within the current x axis window.
	 * @param packX Whether to change the x axis.
	 * @param packY Whether to change the y axis.
	 */
	public void pack(int index, boolean packX, boolean packY)
	{
		Dataset dataset = getDataset(index);
		
		//empty dataset: do nothing
		if (dataset == null || dataset.size() == 0 || (packX == false && packY == false))
			return;
		
		java.util.Iterator iterator = dataset.iterator();
		//these duplicate variables prevent changing the globals
		// unless we really want to.
		double xmin, ymin, xmax, ymax;	
		xmin = ymin = Double.MAX_VALUE;
		xmax = ymax = Double.MIN_VALUE;
		DataPoint dp;
		int size = dataset.size();
		
		//this loop finds the minimum and maximum values for x and y
		//when x is also to be packed
		if(packX == true)
			while(iterator.hasNext() )
			{
				dp = (DataPoint)(iterator.next());
				if(dp.y < ymin) ymin = dp.y;
				if(dp.y > ymax) ymax = dp.y;
				if(dp.x < xmin) xmin = dp.x;
				if(dp.x > xmax) xmax = dp.x;
			}
		//if X is not being packed, only look at points within the x window
		else
		{
			size = 0;
			while(iterator.hasNext() )
			{
				dp = (DataPoint)(iterator.next());
				if(dp.x > this.xmin && dp.x < this.xmax)
				{
					if(dp.y < ymin) ymin = dp.y;
					if(dp.y > ymax) ymax = dp.y;
					size++;
				}
			}
			if(size == 0) return;
		}
		
		
		double newXmin = 0, newXmax = 0, newYmin = 0, newYmax = 0;
		
		//one element:
		if(size == 1)
		{
			newXmin = xmin - xmin / 2;
			newXmax = xmax + xmax / 2;
			newYmin = 0;
			newYmax = ymax + ymax / 10;
		}
		
//		adds some extra space on the edges
		else
		{
			newXmin = xmin - ((xmax - xmin) / 10);
			newXmax = xmax + ((xmax - xmin) / 10);
			newYmin = ymin - ((ymax - ymin) / 10);
			newYmax = ymax + ((ymax - ymin ) /10);
		}
		
		if (packX) {
			this.xmin = newXmin;
			this.xmax = newXmax;
		}
		if (packY) {
			if (index == 0) {
				this.ymin = newYmin;
				this.ymax = newYmax;
			} else {
				this.ymin2 = newYmin;
				this.ymax2 = newYmax;
			}
		}
			
		createAxes();
		repaint();
	}

	
	
	/**
	 * Indicates the portion of the chart area in which data is displayed.
	 * @return A rectangle containing the data display area.
	 */
	public Rectangle getDataAreaBounds()
	{
		Dimension size = getSize();
		Insets insets = getInsets();
		
		int width = size.width - V_AXIS_PADDING - V_TITLE_PADDING - RIGHT_PADDING
		- insets.left - insets.right;
		
		if (dataset2 != null)
			width -= V_AXIS_PADDING;
		
		return new Rectangle(V_AXIS_PADDING + V_TITLE_PADDING + insets.left,
				TOP_PADDING + insets.top,
				width,
				size.height - H_AXIS_PADDING - H_TITLE_PADDING - TOP_PADDING
					- insets.top - insets.bottom);
	}
	
	
	
	/**
	 * Creates the axes using the current tick, title and range values.
	 * Creates default axes if these values are not set.
	 */
	private void createAxes()
	{
		
		if(numSmartTicksX > 0)
		{
			bigTicksX = (xmax - xmin) / numSmartTicksX;
		}
		
		if(numSmartTicksY > 0)
		{
			bigTicksY = (ymax - ymin) / numSmartTicksY;
		}

		if(bigTicksX == 0 || xmin >= xmax)
			xAxis = new GraphAxis(xmin, xmax);
		else
			xAxis = new GraphAxis(xmin, xmax, bigTicksX, smallTicksX);
		
		if(bigTicksY == 0 || ymin >= ymax)
			yAxis = new GraphAxis(ymin, ymax);
		else
			yAxis = new GraphAxis(ymin, ymax, bigTicksY, smallTicksY);
		
		if (ymin2 != -1 && ymax2 != -1) {			
			if(numSmartTicksY <= 0 || ymin2 >= ymax2)
				yAxis2 = new GraphAxis(ymin2, ymax2);
			else
				yAxis2 = new GraphAxis(ymin2, ymax2, (ymax2 - ymin2) / numSmartTicksY, smallTicksY);
			
			yAxis2.setTitle(titleY2);
		}
		
		xAxis.setTitle(titleX);
		yAxis.setTitle(titleY);
	}
	
	/**
	 * Draws the graph
	 */
	public void paintComponent(Graphics g)
	{
		
		//gets the bounds of the drawing area
		Dimension size = this.getSize();
		Insets insets = getInsets();
		
		//paints the background first
		g.setColor(Color.WHITE);
		g.fillRect(insets.left,insets.top,size.width - insets.left - insets.right,
				size.height - insets.top - insets.bottom);
		
		//Graphics2D has useful functions like transforms and
		// stroke styles, so we change the Graphics into one.
		Graphics2D g2d = (Graphics2D)g.create();
		
		drawAxisLineX(g2d);
		drawAxisTicksX(g2d);
		drawAxisTitleX(g2d);
		
		if(dataset1 != null)
		{
			if(showBars)  drawDataBars(0, g2d, dataset1);
			if(showLines) drawDataLinesSmart(0, g2d, dataset1);
			
			if (dataset2 == null)
				g2d.setColor(Color.BLACK);
			
			drawAxisTitleY(g2d, 0);
			drawAxisLineY(g2d, 0);
			drawAxisTicksY(g2d, 0);
		}
		
		if (dataset2 != null)
		{
			if(showBars2)  drawDataBars(1, g2d, dataset2);
			if(showLines2) drawDataLinesSmart(1, g2d, dataset2);

			drawAxisTitleY(g2d, 1);
			drawAxisLineY(g2d, 1);
			drawAxisTicksY(g2d, 1);
		}
		
		
		
		
		
		//Sun recommends cleanup of extra Graphics objects for efficiency
		g2d.dispose();
	}

	
	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.

	 * which of overlapping data points to draw.
	 * @param g2d
	 * @param ds
	 */
	
	private void drawDataLinesSmart(int index, Graphics2D g2d, Dataset ds)
	{
		GraphAxis actualYAxis = getYAxis(index);
		Rectangle dataArea = getDataAreaBounds();
		
		DataPoint curPoint; //holder for data retrieved from dataset
		int pixindex = dataArea.x;	//what pixel we are on
		int xCoord, yCoord;	//coordinates of point to draw
		
		//	previous point - connect line from it to current point.
		int prevX=Integer.MIN_VALUE, prevY = Integer.MIN_VALUE;
		int maxYCoord = Integer.MAX_VALUE;
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(getColor(index));
		g2d.clip(dataArea);	//constrains drawing to the data area
		g2d.setStroke(new BasicStroke(2));
		
		//	loops through all data points
		//GeneralPath gp = new GeneralPath();
		Iterator i = ds.iterator();
		while(i.hasNext() && pixindex < dataArea.x + dataArea.width)
		{
			
			
			curPoint = (DataPoint)i.next();
			
			//find screen coordinates of point
			xCoord = (int) (dataArea.x + xAxis.relativePosition(curPoint.x) * dataArea.width);
			yCoord = (int) (dataArea.y + dataArea.height 
					- (actualYAxis.relativePosition(curPoint.y) * dataArea.height));
			
			if(xCoord < dataArea.x) continue;
			
			//new pixel
			if(xCoord > pixindex)
			{
				//draw
				if(prevY != Integer.MIN_VALUE)
				{
					g2d.draw(new Line2D.Double(prevX, prevY, pixindex, maxYCoord));
				}
				
				//reset for next pixel
				prevX = pixindex;
				prevY = maxYCoord;
				maxYCoord = yCoord;
				pixindex = xCoord;
				//break;
			}
			//same pixel
			else
			{
				if(maxYCoord > yCoord) maxYCoord = yCoord;
			}
			
			
			
		}
		//g2d.draw(gp);
		
		//cleanup
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}
	
	
//	/**
//	 * Draws the data in a continuous line by drawing only one
//	 * data point per horizontal pixel.
//	 * TODO: improve display accuracy by using a better algorithm for deciding
//	 * which of overlapping data points to draw.
//	 * @param g2d
//	 * @param ds
//	 */
//	
//	private void drawDataLines(Graphics2D g2d, Dataset ds)
//	{
//		Rectangle dataArea = getDataAreaBounds();
//		
//		DataPoint curPoint; //holder for data retrieved from dataset
//		int pixindex = 0;	//what pixel we are on
//		double xCoord, yCoord;	//coordinates of point to draw
//		
//		//	previous point - connect line from it to current point.
//		double prevX=Double.NaN, prevY = Double.NaN;
//		
//		Shape oldClip = g2d.getClip();
//		Stroke oldStroke = g2d.getStroke();
//		g2d.setColor(color);
//		g2d.clip(dataArea);	//constrains drawing to the data area
//		g2d.setStroke(new BasicStroke(2));
//		
//		//	loops through all data points
//		//GeneralPath gp = new GeneralPath();
//		Iterator i = ds.iterator();
//		while(i.hasNext() && pixindex < dataArea.x + dataArea.width)
//		{
//			curPoint = (DataPoint)i.next();
//			
//			//find screen coordinates of point
//			xCoord = dataArea.x + xAxis.relativePosition(curPoint.x) * dataArea.width;
//			yCoord = dataArea.y + dataArea.height 
//				- (yAxis.relativePosition(curPoint.y) * dataArea.height);
//			
//			
//			//only draw the first point at each pixel
//			if(pixindex < (int)xCoord)
//			{
//				//if there is no previous point, draw the line on the next pass
//				if(!(Double.isNaN(prevX) || Double.isNaN(prevY)))
//				{
//					//gp.append(new Line2D.Double(prevX, prevY, xCoord, yCoord), false);
//					g2d.draw(new Line2D.Double(prevX, prevY, xCoord, yCoord));
//				}
//				prevX = xCoord;
//				prevY = yCoord;
//				pixindex = (int)xCoord;
//				
//			}
//				//overlapping points: record highest y coordinate value
//			else
//			{
//				
//			}
//		}
//		//g2d.draw(gp);
//		
//		//cleanup
//		g2d.setClip(oldClip);
//	}
	
	/**
	 * Draws the data as bars.  Also saves them in the array bars[] for later access.
	 * @param g2d The Graphics context.
	 * @param ds The dataset to draw.
	 */
	private void drawDataBars(int index, Graphics2D g2d, Dataset ds)
	{
		GraphAxis actualYAxis = getYAxis(index);
		Rectangle dataArea = getDataAreaBounds();

		DataPoint curPoint;
		Rectangle bar;
		int width = barWidth;
		double xCoord;
		double height;
		
		//loops through all data points, drawing each one.
		Iterator i = ds.iterator();
		//Rectangle barsTemp[] = new Rectangle[ds.size()];
		while(i.hasNext())
		{
			curPoint = (DataPoint)(i.next());
			
			//checks if point is in bounds
			xCoord = xAxis.relativePosition(curPoint.x);
			if(xCoord >= 0 && xCoord <= 1)
			{
				//x coordinate of the point
				xCoord = dataArea.x + xCoord * (dataArea.width);
				
				//height of the bar - clipped to top or bottom if out of bounds
				height = actualYAxis.relativePosition(curPoint.y);
				if(height < 0)
					height = dataArea.y + dataArea.height;
				else if(height > 1)
					height = dataArea.y;
				
				else
					height = height * (dataArea.height);

				
				bar = new Rectangle(
						(int)( xCoord - width / 2), //centers the bar on the value
						(int)(dataArea.y + dataArea.height - height),
						(int)(width),
						(int)(height) );
				
				//barsTemp[index] = bar; //saves in global array
				
				
				//fills in bar
				g2d.setColor(getColor(index));				
				g2d.fill(bar);
				
				//draws border around bar
				g2d.setColor(Color.BLACK);
				g2d.draw(bar);
			}
			else
			{
				//puts a null bar in the array to hold its place
				//barsTemp[index] = null;
			}
		}
		//saves in global array
		//bars = new Rectangle[index];
		//for(int c = 0; c < index; c++)
		//	bars[c] = barsTemp[c];
	}
	
	/**
	 * Draws the X axis line, without tick marks.
	 * @param g2d The graphics context.
	 */
	private void drawAxisLineX(Graphics2D g2d)
	{
		Dimension size = getSize();
		Rectangle dataArea = getDataAreaBounds();

		g2d.setPaint(Color.BLACK);
		g2d.setStroke(new BasicStroke(2));
		
		g2d.draw(new Line2D.Double(dataArea.x ,dataArea.y + dataArea.height,
				dataArea.x + dataArea.width, dataArea.y + dataArea.height));
		
	}
	
	/**
	 * Draws the Y axis line without tick marks.
	 * @param g2d The graphics context.
	 */ 
	private void drawAxisLineY(Graphics2D g2d, int index)
	{
		
		Dimension size = getSize();
		Rectangle dataArea = getDataAreaBounds();
		
		//Y Axis line
		g2d.setPaint(Color.BLACK);
		g2d.setStroke(new BasicStroke(2));
		if (index == 0)
			g2d.draw(new Line2D.Double(dataArea.x, dataArea.y,
					dataArea.x, dataArea.y + dataArea.height));
		else
			g2d.draw(new Line2D.Double(dataArea.x + dataArea.width, dataArea.y,
					dataArea.x + dataArea.width, dataArea.y + dataArea.height));
	}
	
	
	private void drawAxisTitleX(Graphics2D g2d)
	{
		Dimension size = getSize();
		
		int xCoord;
		java.awt.font.GlyphVector vector; //the visual representation of the string.
		

		//middle of space
		Rectangle dataArea = getDataAreaBounds();
		xCoord = dataArea.x + dataArea.width/2;
		// center the string by finding its graphical representation
		vector = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(),xAxis.getTitle());
		xCoord = xCoord - vector.getOutline().getBounds().width / 2;
		g2d.drawString(xAxis.getTitle(), xCoord, size.height - getInsets().bottom - 3);
	}
	
	private void drawAxisTitleY(Graphics2D g2d, int index)
	{
		GraphAxis actualYAxis = getYAxis(index);
		
//		y axis title - rotated so as to read vertically
		Rectangle dataArea = getDataAreaBounds();
		int yCoord = dataArea.y + dataArea.height / 2;
		int xCoord = V_TITLE_PADDING - getInsets().left - 4;
		if (index == 1)
			xCoord = getSize().width - getInsets().right - 4;
		
		java.awt.font.GlyphVector vector; //the visual representation of the string.
		vector = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), actualYAxis.getTitle());
		yCoord = yCoord + vector.getOutline().getBounds().width / 2;
		
		g2d.rotate(- Math.PI/2, xCoord, yCoord);
		g2d.drawString(actualYAxis.getTitle(), xCoord, yCoord);
		g2d.rotate(Math.PI/2, xCoord, yCoord);
	}
	
	
	private void drawAxisTicksX(Graphics2D g2d)
	{
		Dimension size = getSize();
		Rectangle dataArea = getDataAreaBounds();
		
		g2d.setStroke(new BasicStroke(1));
		
//gets big ticks as proportions of the axis length
		double[] xBigTicks = xAxis.getBigTicksRel();
		double[] xBigTicksLabels = xAxis.getBigTicksVals();
		
		if(xBigTicks.length == 0 || xBigTicksLabels.length == 0)
			return;
		
		String label;
		int count=0;
		double tickValue = xBigTicks[0];
		
		while(tickValue >= 0 && tickValue <= 1 && count < xBigTicks.length)
		{
			tickValue = xBigTicks[count];
			if(tickValue >= 0 && tickValue <= 1)
				drawTick(g2d,tickValue,true,true,false);
			
			
			//label on each big tick, rounded to nearest hundredth
			label = Double.toString((double)(Math.round(xBigTicksLabels[count] * 100))/100);
			//label = Integer.toString((int)(xBigTicksLabels[count]));
			g2d.drawString(label, 
					(int)(dataArea.x 
							+ tickValue * dataArea.width) - 8,
									size.height - V_TITLE_PADDING);
			
			count++;
		}
		
		
		// x axis small ticks
		double[] xSmallTicks = xAxis.getSmallTicks();
		count = 0;
		tickValue = xSmallTicks[0];
		
		while(tickValue >= 0 && tickValue <= 1 && count < xSmallTicks.length)
		{
			tickValue = xSmallTicks[count];
			if(tickValue >= 0 && tickValue <= 1)
				drawTick(g2d,tickValue,true,false,false);
			count++;
			
		}
	}
	
	private void drawAxisTicksY(Graphics2D g2d, int index)
	{
		GraphAxis realYAxis = getYAxis(index);
		
		Dimension size = getSize();
		Rectangle dataArea = getDataAreaBounds();
		
		//an arrays of the ticks, as proportions of the axis length
		double[] yBigTicks = realYAxis.getBigTicksRel();
		double[] yBigTicksLabels = realYAxis.getBigTicksVals();
		if(yBigTicks.length == 0 || yBigTicksLabels.length == 0)
			return;
		
		int count=0;
		String label;
		
		g2d.setStroke(new BasicStroke(1));

		double tickValue = yBigTicks[0];
		while(tickValue >= 0 && tickValue <= 1 && count < yBigTicks.length)
		{
			tickValue = yBigTicks[count];
			if(tickValue >= 0 && tickValue <= 1)
				drawTick(g2d,tickValue,false,true, (index == 0));
			
			int tickX = (index == 0) ? V_TITLE_PADDING : dataArea.x + dataArea.width + RIGHT_HAND_V_TITLE_PADDING;
			
			label = Double.toString((double)(Math.round(yBigTicksLabels[count] * 100))/100);

			g2d.drawString(label, 
					tickX,
					(int)(dataArea.y + dataArea.height 
							- tickValue * (dataArea.height) + 4));
			count++;
		}
		
		
		// y axis small ticks
		double[] ySmallTicks = realYAxis.getSmallTicks();
		count = 0;
		tickValue = ySmallTicks[0];
		
		while(tickValue >= 0 && tickValue <= 1 && count < ySmallTicks.length)
		{
			tickValue = ySmallTicks[count];
			if(tickValue >= 0 && tickValue <= 1)
				drawTick(g2d,tickValue,false,false, (index == 0));
			count++;
			
		}
		
	}
	
	/**
	 * Draws a tick on a graph axis.
	 *
	 * @param g The graphics context for the graph.
	 * @param relPos The relative position of the tick on the axis as a double
	 * between 0 and 1.
	 * @param xAxis True to draw on X axis, false to draw on Y axis.
	 * @param big True to draw big tick, false to draw small tick.
	 * @param leftSide True if drawing left-side ticks, false for right side... only applicable to y axis
	 */
	private void drawTick(Graphics2D g2d, double relPos, boolean xAxis, boolean big, boolean leftSide)
	{
		Dimension size = this.getSize();
		Rectangle dataArea = getDataAreaBounds();
		
		int tickSize;
		if(big)
			tickSize = BIG_TICK_LENGTH;
		else
			tickSize = SMALL_TICK_LENGTH;
		
		//for x axis
		if(xAxis)
		{
			//converts from relative position to screen coordinates
			double xCoord = dataArea.x + relPos * (dataArea.width);

			g2d.draw(new Line2D.Double(
					xCoord,
					dataArea.y + dataArea.height,
					xCoord,
					dataArea.y + dataArea.height - tickSize
			));
		}
		
		//for y axis
		else
		{
			int xCoord = leftSide ? dataArea.x : (dataArea.x + dataArea.width - tickSize);
			
			double yCoord = dataArea.y + dataArea.height - relPos * (dataArea.height);
			g2d.draw(new Line2D.Double(
					xCoord,
					yCoord,
					xCoord + tickSize,
					yCoord));
		}
	}
	
	public boolean isDoubleBuffered()
	{
		return false;
	}
}
