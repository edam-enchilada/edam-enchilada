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

public abstract class AbstractMetricChartArea extends AbstractChartArea {
	// these are handled somewhat magically.  see setNumTicksX, for example.

	public AbstractMetricChartArea() {
		super();
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

	/**
	 * Translates a length in data coordinates to a length in screen coordinates,
	 * as long as that length is parallel to the X axis.
	 */
	public int XLen(double dataValue) {
		return (int) ((xAxis.relativePosition(1) - xAxis.relativePosition(0)) 
						* dataValue * getDataAreaBounds().width);
	}
	
	/**
	 * Translates a length in data coordinates to a length in screen coordinates,
	 * as long as that length is parallel to the Y axis.
	 */
	public int YLen(double dataValue) {
		return (int) ((yAxis.relativePosition(1) - yAxis.relativePosition(0))
						* dataValue * getDataAreaBounds().height);
	}
	
	/**
	 * Translates a value in data coordinates to its absolute x-position in 
	 * screen coordinates.
	 */
	public int XAbs(double dataValue) {
		Rectangle dataArea = getDataAreaBounds();
		return (int) ((xAxis.relativePosition(dataValue) * dataArea.width)
					+ getDataAreaBounds().x);
	}
	
	/**
	 * Translates a value in data coordinates to its absolute y-position in 
	 * screen coordinates.
	 */
	public int YAbs(double dataValue) {
		Rectangle dataArea = getDataAreaBounds();
		return (int) (dataArea.y + dataArea.height 
						- (yAxis.relativePosition(dataValue) * dataArea.height));
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
	
	

//	/**
//	 * Sets new values for the X axis ticks.
//	 * 
//	 * @param bigX Big ticks on the X axis are multiples of this.
//	 * @param smallX Number of small ticks on the X axis between each big tick.
//	 */
//	public void setTicksX(double bigX, int smallX) {
//		bigTicksX = bigX;
//		smallTicksX = smallX;
//		numSmartTicksX = -1;
//		
//		recalculateTicks();
//		repaint();
//	}

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
		xAxis.setTicks(bigTicks, smallTicks);
		repaint();
	}

//	/**
//	 * Sets new values for the Y axis ticks.
//	 * 
//	 * @param bigY Big ticks on the Y axis are multiples of this.
//	 * @param smallY Number of small ticks on the Y axis between each big tick.
//	 */
//	public void setTicksY(double bigY, int smallY) {
//		bigTicksY = bigY;
//		smallTicksY = smallY;
//		numSmartTicksY = -1;
//		
//		recalculateTicks();
//		repaint();
//	}

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
		yAxis.setTicks(bigTicks, smallTicks);
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
		
		xAxis.setRange(xmin, xmax);
		yAxis.setRange(ymin, ymax);
		
		repaint();
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
 
	protected void setYMax(double ymax) {
		yAxis.setMax(ymax);
	}

	protected void setYMin(double ymin) {
		yAxis.setMin(ymin);
	}

	protected void setXMax(double xmax) {
		xAxis.setMax(xmax);
	}

	protected void setXMin(double xmin) {
		xAxis.setMin(xmin);
	}
	
	
	public static void main(String[] args) {
		JFrame f = new JFrame("woopdy doo");
		AbstractMetricChartArea mca = new AbstractMetricChartArea() {
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
	

	public void setXAxisBounds(double xmin, double xmax) {
		xAxis.setRange(xmin, xmax);
		repaint();
	}
	
}