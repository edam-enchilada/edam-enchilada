package chartlib;

import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;


/**
 * 
 * 
 * note: the getters and setters of xmax and min don't cause this to repaint.
 * setAxisBounds does.
 * 
 * @author smitht
 *
 */

public abstract class MetricChartArea extends GenericChartArea {


	protected double bigTicksX = 0;
	protected double bigTicksY = 0;
	private int numSmartTicksX = -1;
	private int numSmartTicksY = -1;
	private int smallTicksX = 1;
	private int smallTicksY = 1;
	
	
	protected double yDrawFactor() {
		return getGraphicCoords(0, 0).getY() - getGraphicCoords(0, 1).getY();
	}
	
	protected double xDrawFactor() {
		return getGraphicCoords(0, 0).getY() - getGraphicCoords(1, 0).getY();
	}

	
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

	public int XLen(double dataValue) {
		return (int) (xAxis.relativePosition(dataValue) 
						* getDataAreaBounds().width);
	}
	
	public int YLen(double dataValue) {
		return (int) (yAxis.relativePosition(dataValue) 
						* getDataAreaBounds().height);
	}
	
	public int XAbs(double dataValue) {
		return XLen(dataValue) + getDataAreaBounds().x;
	}
	
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
	
	public Point getGraphicCoords(double dataX, double dataY) {
		return getGraphicCoords(new Point2D.Double(dataX, dataY));
	}
	
	/**
	 * Doesn't repaint.
	 *
	 */
	protected void recalculateTicks() {
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
	 * @return Returns the number of small ticks between each big tick on the X axis.
	 */
	public int getSmallTicksY() {
		return smallTicksY;
	}


	/**
	 * @return Returns upper limit of the x axis.
	 */
	public double getXMax() {
		return xAxis.getMax();
	}

	/**
	 * @return Returns the lower limit of the x axis.
	 */
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
				double xFact = xDrawFactor();
				double yFact = yDrawFactor();
				
				g2d.draw(new Rectangle(XAbs(20), YAbs(20), XLen(5), YLen(5)));
				g2d.draw(new Rectangle(XAbs(40), YAbs(20), XLen(5), YLen(5)));
				g2d.draw(new Rectangle(XAbs(20), YAbs(45), XLen(25), YLen(10)));

			}
		};
		mca.setAxisBounds(0, 100, 0, 100);
		mca.setPreferredSize(new Dimension(400, 400));
		f.getContentPane().add(mca);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
		
	}
	
}