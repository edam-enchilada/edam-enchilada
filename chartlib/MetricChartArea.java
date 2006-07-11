package chartlib;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.*;


/**
 * This is a GenericChartArea that knows about numbers.  Its axes have reasonable
 * ranges that you can set, and there are convenient methods for finding the 
 * location of a point or the length of a line in screen coordinates (XAbs and friends).
 * <p>
 * note: the getters and setters of xmax and min don't cause this to repaint.
 * setAxisBounds does.
 * 
 * @author smitht
 *
 */

public abstract class MetricChartArea extends GenericChartArea {
	
	// these are handled somewhat magically.  see setNumTicksX, for example.
	private double bigTicksX = 0;
	private double bigTicksY = 0;
	private int numSmartTicksX = -1;
	private int numSmartTicksY = -1;
	private int smallTicksX = 1;
	private int smallTicksY = 1;
	
	/**
	 * Translates a point in screen space to chart coordinates.
	 * @param p The point in screen coordinates.
	 * @return The point translated to the chart's coordinate system,
	 * or null if the point is not within the data value.
	 */
	public Point2D.Double getDataValueForPoint(Point p) {
		if(!isInDataArea(p)) return null;
		
		double xMax = getXMax(), yMax = getYMax(), 
			xMin = getXMin(), yMin = getYMin();
		
		Dimension size = getSize();
		Point2D.Double result = new Point2D.Double();
		Rectangle dataArea = getDataAreaBounds();
		
		double x = p.x, y = p.y;
		//translate to data value origin
		x = x - dataArea.x; 
		y = dataArea.y + dataArea.height - y; //screen coordinate origin is at top,
									// but data origin is at bottom, so we subtract.
		
		//scale to chart coordinates.
		x = x * (xMax - xMin) / (dataArea.width);
		y = y * (yMax - yMin) / (dataArea.height);
		
		//translate to axis origins
		x = x + xMin;
		y = y + yMin;
		
		
		result.x = x;
		result.y = y;
		return result;
	}

	/**
	 * Translates a length in data coordinates to a length in screen coordinates,
	 * as long as that length is parallel to the X axis.
	 */
	public int XLen(double dataValue) {
		return (int) (xAxis.relativePosition(dataValue) 
						* getDataAreaBounds().width);
	}
	
	/**
	 * Translates a length in data coordinates to a length in screen coordinates,
	 * as long as that length is parallel to the Y axis.
	 */
	public int YLen(double dataValue) {
		return (int) (yAxis.relativePosition(dataValue) 
						* getDataAreaBounds().height);
	}
	
	/**
	 * Translates a value in data coordinates to its absolute x-position in 
	 * screen coordinates.
	 */
	public int XAbs(double dataValue) {
		return XLen(dataValue) + getDataAreaBounds().x;
	}
	
	/**
	 * Translates a value in data coordinates to its absolute y-position in 
	 * screen coordinates.
	 */
	public int YAbs(double dataValue) {
		Rectangle dataArea = getDataAreaBounds();
		return dataArea.y + dataArea.height - YLen(dataValue);
	}

	/**
	 * Translates a point in chart space to a point in screen space.  Then you
	 * can draw with it.
	 * 
	 * @param dataCoords a data point
	 * @return a point suitable for use in a Graphics2D.something call.
	 */
	public Point getGraphicCoords(Point2D.Double dataCoords) {
		return new Point(XAbs(dataCoords.x), YAbs(dataCoords.y));
	}
	
	/**
	 * Translates a point in chart space to a point in screen space.  Then you
	 * can draw with it.
	 * 
	 * @return a point suitable for use in a Graphics2D.something call.
	 */
	public Point getGraphicCoords(double dataX, double dataY) {
		return getGraphicCoords(new Point2D.Double(dataX, dataY));
	}
	
	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	protected void drawDataLines(Graphics2D g2d,Dataset dataset){
		Rectangle dataArea = getDataAreaBounds();
		// these booleans show whether we've drawn indicators that 
		// more data exist in each direction
		boolean drawnMoreLeft = false, drawnMoreRight = false;
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(foregroundColor);
		g2d.clip(dataArea);	//constrains drawing to the data value
		g2d.setStroke(new BasicStroke(1.5f));
		
		double[] coords = new double[dataArea.width];
		
		//	loops through all data points building array of points to draw
		Iterator<DataPoint> iterator = dataset.iterator();
		while(iterator.hasNext())
		{
			DataPoint curPoint = iterator.next();
			
			double pointPos = xAxis.relativePosition(curPoint.x);
			if (pointPos < 0 && !drawnMoreLeft) {
				drawnMoreLeft = true;
				drawMorePointsIndicator(0, g2d);
			}
			else if (pointPos > 1 && !drawnMoreRight) {
				drawnMoreRight = true;
				drawMorePointsIndicator(1, g2d);
			}
			else {
				int xCoord = (int) (dataArea.x+xAxis.relativePosition(curPoint.x) * dataArea.width);
				double yCoord = (dataArea.y + dataArea.height 
						- (yAxis.relativePosition(curPoint.y) * dataArea.height));
				
				if (yCoord > 0 && yCoord <= (dataArea.y + dataArea.height) && xCoord >= 0 && xCoord < dataArea.width) {
					if (coords[xCoord] == 0 || yCoord < coords[xCoord])
						coords[xCoord] = yCoord;
				} else if (curPoint.y == -999)
					coords[xCoord] = -999.0;
			}
		}
		
		// Then draws them:
		int lastX = 0;
		double lastY = -999.0;
		int numPoints = 0;
		//boolean firstPoint = true;
		for (int i = 0; i < coords.length; i++) {
			if (coords[i] == 0)
				continue;

			int xPos = i;
			if (coords[i] != -999.0 && lastY != -999.0){
				g2d.draw(new Line2D.Double((double) lastX, lastY, (double) xPos, coords[i]));
				numPoints++;
			}
			else if (coords[i] != -999.0) {
				// Point is valid, but last point wasn't... so just draw a large point:
				g2d.draw(new Line2D.Double((double) dataArea.x+0,(dataArea.y + dataArea.height 
						- (yAxis.relativePosition(0) * dataArea.height)), (double) xPos, coords[i]));
				numPoints++;
			}
			
			lastX = xPos;
			lastY = coords[i];
		}
		
		if(lastX<=dataArea.x+coords.length-1){
			g2d.draw(new Line2D.Double((double) lastX, lastY, (double) dataArea.x+coords.length-1,(dataArea.y + dataArea.height 
				- (yAxis.relativePosition(0) * dataArea.height))));
		}
		//cleanup
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
		
	}

	/**
	 * Draws the a (small) X for each data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	protected void drawDataPoints(Graphics2D g2d, Dataset dataset) {
		GraphAxis actualYAxis = yAxis;
		Rectangle dataArea = getDataAreaBounds();
		
		/*Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		*/
		boolean drawnMoreLeft = false, drawnMoreRight = false;
		
		int maxX = 0;
		
		//	loops through all data points, drawing each one as
		//  a scatter plot...
		Iterator<DataPoint> iterator = dataset.iterator();
		while(iterator.hasNext())
		{
			DataPoint curPoint = iterator.next();
			
			double x = curPoint.y, y = curPoint.y;
			//System.out.println("X: "+x+"\tY:: "+y);
			/*if (x >= 0 && x <= 1) {
*/
				int xCoord = (int) (dataArea.x + xAxis
						.relativePosition(curPoint.x)
						* dataArea.width);
				double yCoord = (dataArea.y + dataArea.height - (actualYAxis
						.relativePosition(curPoint.y) * dataArea.height));
				drawPoint(g2d, xCoord, yCoord);
			/*} else if (x < 0 && !drawnMoreLeft) {
				drawnMoreLeft = true;
				drawMorePointsIndicator(0, g2d);
				// puts a null bar in the array to hold its place
				// barsTemp[index] = null;
			} else if (!drawnMoreRight) {
				drawnMoreRight = true;
				drawMorePointsIndicator(1, g2d);
			}*/
		}
		
		
		//cleanup
		/*g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
		*/
	}
	
	protected void drawPoint(Graphics2D g2d,double xCoord, double yCoord){
		//drawPointBar(g2d,xCoord,yCoord);
		drawPointX(g2d,xCoord,yCoord);
	}
	
	protected void drawPointBar(Graphics2D g2d,double xCoord, double yCoord){
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		Rectangle dataArea = getDataAreaBounds();
		Rectangle bar;
		int width = 5;
		bar = new Rectangle(
				(int)( xCoord - width / 2), //centers the bar on the value
				(int)( yCoord),
				(int)(width),
				(int)(-1*yCoord)+ (dataArea.y + dataArea.height) );
		
		//draw the bar
		g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(foregroundColor);				
		g2d.fill(bar);
		
		//draws border around bar
		g2d.setColor(Color.BLACK);
		g2d.draw(bar);
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}
	
	protected void drawPointX(Graphics2D g2d,double xCoord, double yCoord){
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		int radius = 3;
		g2d.draw(new Line2D.Double((double)xCoord-radius, yCoord-radius, (double)xCoord+radius, yCoord+radius));
		g2d.draw(new Line2D.Double((double)xCoord+radius, yCoord-radius, (double)xCoord-radius, yCoord+radius));
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}
	
	
	
	/**
	 * This is called automatically 
	 * after you fiddle with smartticks or setticks or anything.
	 * It notifies the axes of the fact that they have new tick values to
	 * think about.
	 * <p>
	 * Doesn't repaint.
	 */
	private void recalculateTicks() {
		if(numSmartTicksX > 0)
		{
			bigTicksX = (getXMax() - getXMin()) / numSmartTicksX;
		}
		
		if(numSmartTicksY > 0)
		{
			bigTicksY = (getYMax() - getYMin()) / numSmartTicksY;
		}
	
		if(bigTicksX != 0)
			xAxis.setTicks(bigTicksX, smallTicksX);
		
		if(bigTicksY != 0)
			yAxis.setTicks(bigTicksY, smallTicksY);
	}
	

	/**
	 * Sets new values for the X axis ticks.
	 * 
	 * @param bigX Big ticks on the X axis are multiples of this.
	 * @param smallX Number of small ticks on the X axis between each big tick.
	 */
	public void setTicksX(double bigX, int smallX) {
		bigTicksX = bigX;
		smallTicksX = smallX;
		numSmartTicksX = -1;
		
		recalculateTicks();
		repaint();
	}

	/**
	 * Sets new values for the x axis ticks by ensuring that there are always
	 * bigTicks number of big ticks and smallTicks number of small ticks between
	 * each big tick.
	 * 
	 * @param numTicks Number of big ticks on the X axis.
	 * @param smallX Number of small ticks between each big tick.
	 */
	public void setNumTicksX(int bigTicks, int smallTicks) {
		assert(bigTicks > 1 && smallTicks >= 0);
		// BUG!! nothing is ever done with smallTicks.
		numSmartTicksX = bigTicks;
		
		recalculateTicks();
		repaint();
	}

	/**
	 * Sets new values for the Y axis ticks.
	 * 
	 * @param bigY Big ticks on the Y axis are multiples of this.
	 * @param smallY Number of small ticks on the Y axis between each big tick.
	 */
	public void setTicksY(double bigY, int smallY) {
		bigTicksY = bigY;
		smallTicksY = smallY;
		numSmartTicksY = -1;
		
		recalculateTicks();
		repaint();
	}

	/**
	 * Sets new values for the y axis ticks by ensuring that there are
	 * bigTicks number of big ticks and smallTicks number of small ticks between
	 * each big tick.  The number of ticks may be off by one or two because
	 * ticks may be on endpoints.
	 * 
	 * @param numTicks Number of big ticks on the Y axis.
	 * @param smallX Number of small ticks between each big tick.
	 */
	public void setNumTicksY(int bigTicks, int smallTicks) {
		assert(bigTicks > 1 && smallTicks >= 0);
		numSmartTicksY = bigTicks;

		recalculateTicks();
		repaint();
	}

	/**
	 * Sets new boundaries for the axes and displayed data.
	 * 
	 * @param xmin Minimum of X axis.
	 * @param xmax Maximum of X axis.
	 * @param ymin Minimum of Y axis.
	 * @param ymax Maximum of Y axis.
	 */
	public void setAxisBounds(double xmin, double xmax, double ymin, double ymax) throws IllegalArgumentException {
		//check for errors
		if(xmin >= xmax) throw new IllegalArgumentException("Xmin >= Xmax.");
		else if(ymin >= ymax) throw new IllegalArgumentException("Ymin >= Ymax.");
		
		
		setXMin(xmin);
		setXMax(xmax);
		
		setYMin(ymin);
		setYMax(ymax);
		
		repaint();
	}

	

	/**
	 * @return Returns the number of small ticks between each big tick on the X axis.
	 */
	public int getSmallTicksX() {
		return smallTicksX;
	}

	/**
	 * @return Returns the number of small ticks between each big tick on the Y axis.
	 */
	public int getSmallTicksY() {
		return smallTicksY;
	}


	/*
	 * 
	 * BORING GETTERS AND SETTERS.
	 * 
	 */

	public double getXMax() {
		return xAxis.getMax();
	}
	
	public double getXMin() {
		return xAxis.getMin();
	}

	public double getYMax() {
		return yAxis.getMax();
	}

	public double getYMin() {
		return yAxis.getMin();
	}
 
	public void setYMax(double ymax) {
		yAxis.setMax(ymax);
	}

	public void setYMin(double ymin) {
		yAxis.setMin(ymin);
	}

	public void setXMax(double xmax) {
		xAxis.setMax(xmax);
	}

	public void setXMin(double xmin) {
		xAxis.setMin(xmin);
	}
	
	
	public static void main(String[] args) {
		JFrame f = new JFrame("woopdy doo");
		MetricChartArea mca = new MetricChartArea() {
			public void drawData(Graphics2D g2d) {
				
				g2d.setColor(Color.RED);
				
				g2d.draw(new Rectangle(XAbs(20), YAbs(20), XLen(5), YLen(5)));
				g2d.draw(new Rectangle(XAbs(40), YAbs(20), XLen(5), YLen(5)));
				g2d.draw(new Rectangle(XAbs(20), YAbs(45), XLen(25), YLen(10)));

			}
		};
		mca.setAxisBounds(0, 100, 0, 100);
		mca.setPreferredSize(new Dimension(400, 400));
		mca.setNumTicksY(5, 1);
		f.getContentPane().add(mca);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
		
	}
	
}