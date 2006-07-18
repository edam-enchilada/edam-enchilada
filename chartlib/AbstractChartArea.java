package chartlib;

import java.awt.*;
import java.awt.geom.*;
import static chartlib.AxisTitle.AxisPosition;

import javax.swing.JComponent;

/**
 * This class just handles the basic task of being a place where charts are drawn.
 * <p>
 * It doesn't know anything about x and y coordinates of data, just about drawing itself.
 * It has axes but doesn't pay too much attention to them.
 * It has axis labels that you can manipulate.
 * See LineChartArea for an example of a use of this.
 * 
 * @author smitht
 *
 */

public abstract class AbstractChartArea extends JComponent {
	protected GraphAxis xAxis;
	protected GraphAxis yAxis;
	
	protected AxisTitle xAxisTitle, yAxisTitle;

	protected Color foregroundColor = Color.RED;
	protected Color backgroundColor = Color.WHITE;
	
	protected int H_AXIS_PADDING = 15;
	protected int V_AXIS_PADDING = 50;
	protected int H_TITLE_PADDING = 20;
	protected int V_TITLE_PADDING = 20;
	protected int RIGHT_PADDING = 15;
	protected int TOP_PADDING = 15;
	
	public AbstractChartArea() {
		super();
		createAxes();
		this.setMinimumSize(new Dimension(10, 10));
	}



	/**
	 * Indicates the portion of the chart value in which data is displayed.
	 * <p>
	 * This gets called a lot, it might be worth it to implement cacheing.
	 * <p>
	 * It could also be worthwhile to check the size of the components that padding
	 * is being allocated for, and use those sizes, rather than the private static
	 * final variables.
	 * 
	 * @return A rectangle containing the data display value.
	 */
	public Rectangle getDataAreaBounds() {
		Dimension size = getSize();
		Insets insets = getInsets();
		
		int xStart = V_AXIS_PADDING + V_TITLE_PADDING + insets.left;
		int yStart = TOP_PADDING + insets.top;
		int width = size.width - xStart - RIGHT_PADDING - insets.right;
		int height = size.height - yStart - H_AXIS_PADDING - H_TITLE_PADDING - insets.bottom;
		
		return new Rectangle(xStart, yStart, width, height);
	}
	
	/**
	 * Called after the location of the axes on the screen might have changed,
	 * this method tells them so.
	 * 
	 * It also updates the AxisTitles.
	 *
	 */
	protected void updateAxes() {
		Rectangle dataArea = getDataAreaBounds();
		if (xAxis == null) {xAxis = new GraphAxis(getXBaseLine(dataArea));}
		if (yAxis == null) {yAxis = new GraphAxis(getYBaseLine(dataArea));}
		xAxis.setPosition(getXBaseLine(dataArea));
		yAxis.setPosition(getYBaseLine(dataArea));
		
		Point xAnch = getAxisTitlePointX(), yAnch = getAxisTitlePointY();
		if (xAxisTitle == null)
			xAxisTitle = new AxisTitle("", AxisPosition.BOTTOM, xAnch);
		if (yAxisTitle == null)
			yAxisTitle = new AxisTitle("", AxisPosition.LEFT, yAnch);
		xAxisTitle.setAnchorPoint(xAnch);
		yAxisTitle.setAnchorPoint(yAnch);
	}
	
	/**
	 * updateAxes() tests for the axes not existing yet, so this method actually
	 * just calls that one.
	 *
	 */
	protected void createAxes() {
		updateAxes();
	}
	
	/**
	 * From the given dataArea, calculates a line suitable for being the X axis.
	 * 
	 * @param dataArea
	 * @return an X axis line
	 */
	protected Line2D.Double getXBaseLine(Rectangle dataArea) {
		return new Line2D.Double(dataArea.x ,dataArea.y + dataArea.height,
				dataArea.x + dataArea.width, dataArea.y + dataArea.height);
	}
	
	/**
	 * From the given dataArea (gotten from getDataAreaBounds), calculates a line
	 * suitable for being the Y axis (on the left).
	 * 
	 * @param dataArea
	 */
	protected Line2D.Double getYBaseLine(Rectangle dataArea) {
		return new Line2D.Double(dataArea.x, dataArea.y,
				dataArea.x, dataArea.y + dataArea.height);
	}
	

	/**
	 * Tells whether a point is in the data area of the
	 * chartArea (not the title or axis areas).
	 * 
	 * @param p The point to check.
	 * @return True if the point is in the data display value of the chart.
	 */
	public boolean isInDataArea(Point p) {
		return getDataAreaBounds().contains(p);
	}

	
	/**
	 * Draws the graph.  This call might be overridden, but probably shouldn't be,
	 * you should probably just override one of:
	 * drawBackground(Graphics2D), drawAxes(Graphics2D), drawAxisTitles(Graphics2d),
	 * or drawData(Graphics2D).  updateAxes() is also called.
	 * 
	 * For instance, say you want to make a chart with a transparent background.
	 * Then just override drawBackground to do nothing, and make sure you do
	 * .setOpaque(false).
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g.create();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawBackground((Graphics2D) g2d.create());
		updateAxes();
		drawAxes((Graphics2D) g2d.create());
		drawAxisTitles((Graphics2D) g2d.create());
		
		Graphics2D dataG = (Graphics2D) g2d.create();
		dataG.clip(getDataAreaBounds());
		drawData(dataG);

		//Sun recommends cleanup of extra Graphics objects for efficiency
		g2d.dispose();
	}
	
	protected void drawAxisTitles(Graphics2D g2d) {
		xAxisTitle.draw(g2d);
		yAxisTitle.draw(g2d);
	}

	/**
	 * This draws a white rectangle that covers the background of the chart.
	 * <p>
	 * You probably want to override this if you're making a transparent chart.
	 * <p>
	 * The method is called by the default implementation of paintComponent(Graphics).
	 * 
	 * @param g2d
	 */
	protected void drawBackground(Graphics2D g2d) {
			//gets the bounds of the drawing value
			Dimension size = this.getSize();
			Insets insets = getInsets();
			//paints the background first
			g2d.setColor(backgroundColor);
			g2d.fillRect(insets.left,insets.top,size.width - insets.left - insets.right,
					size.height - insets.top - insets.bottom);
	}


	/**
	 * If you didn't want to draw both axes, or if you wanted to draw extra ones,
	 * you would override this method.
	 * @param g2d
	 */
	protected void drawAxes(Graphics2D g2d) {
		xAxis.draw(g2d);
		yAxis.draw(g2d);
	}
	
	
	/**
	 * drawMorePointsIndicator - draw symbols indicating more points exist.
	 * 
	 * When there are more points off the graph area to the left or right,
	 * this method gets called, and draws arrows which indicate that this 
	 * is the case.
	 * 
	 * To set the color, make sure to set the color of the G2d object you send
	 * in.
	 * 
	 * @param i 0 for a left arrow, 1 for a right arrow.
	 * @param g the graphics2d object that runs the pane with the graph on it.
	 */
	protected void drawMorePointsIndicator(int i, Graphics2D g) {
		Shape oldClip = g.getClip();
		
		Rectangle dataArea = getDataAreaBounds();
	
		int arrowShaftY = dataArea.y + dataArea.height - 3;
		
		// these draw little arrows facing left or right, as appropriate.
		if (i <= 0) {
			g.setClip(new Rectangle(dataArea.x - 20, dataArea.y,
					dataArea.width + 20, dataArea.height + 5));
			g.draw(new Line2D.Double(dataArea.x - 15, arrowShaftY,
					dataArea.x - 5, arrowShaftY));
			g.draw(new Line2D.Double(dataArea.x - 15, arrowShaftY,
					dataArea.x - 10, arrowShaftY + 5));
			g.draw(new Line2D.Double(dataArea.x - 15, arrowShaftY,
					dataArea.x - 10, arrowShaftY - 5));
		} else {
			g.setClip(new Rectangle(dataArea.x, dataArea.y,
					dataArea.width + 20, dataArea.height + 5));
			int leftX = dataArea.x + dataArea.width;
			g.draw(new Line2D.Double(leftX + 15, arrowShaftY,
					leftX + 5, arrowShaftY));
			g.draw(new Line2D.Double(leftX + 15, arrowShaftY,
					leftX + 10, arrowShaftY + 5));
			g.draw(new Line2D.Double(leftX + 15, arrowShaftY,
					leftX + 10, arrowShaftY - 5));
		}
		g.setClip(oldClip);
	}

	/**
	 * Override this method to draw the data in some way.  If you want, you can
	 * use some of the methods in ChartArea.java, in which case you should subclass
	 * it.  Otherwise, you can implement your own drawing area.  If you do this,
	 * you might take a look at chartlib.hist.HistogramsChartArea, for an example
	 * of how to redraw efficiently, if your chart type will be complex.  
	 */
	protected abstract void drawData(Graphics2D g2d);
	
	public boolean isDoubleBuffered() {
		return false;
	}
	
	/**
	 * Used by updateAxes() to position the label on the Y axis.
	 * 
	 * @return the point that will be at the center of the text, on the side closest to the axis
	 */
	protected Point getAxisTitlePointY() {
		Rectangle dataArea = getDataAreaBounds();
		return new Point(dataArea.x - V_AXIS_PADDING,
				dataArea.y + (dataArea.height / 2));
	}
	
	/**
	 * Used by updateAxes() to position the label for the X axis.
	 * 
	 * @return the point that will be at the center of the text, on the side closest to the axis
	 */
	protected Point getAxisTitlePointX() {
		Rectangle dataArea = getDataAreaBounds();
		return new Point(dataArea.x + (dataArea.width / 2),
				dataArea.y + dataArea.height + H_AXIS_PADDING);
	}

	
	/**
	 * Sets a new title for the X axis.
	 * @param titleX New X axis title.
	 */
	public void setTitleX(String titleX) {
		xAxisTitle.setTitle(titleX);
		
		repaint();
	}

	/**
	 * Sets a new title for the Y axis.
	 * @param titleY New Y axis title.
	 */
	public void setTitleY(String titleY) {
		yAxisTitle.setTitle(titleY);
		
		repaint();
	}

	/**
	 * @return Returns the title of the X axis.
	 */
	public String getTitleX() {
		return xAxisTitle.getTitle();
	}

	/**
	 * @return Returns the title of the Y axis.
	 */
	public String getTitleY() {
		return yAxisTitle.getTitle();
	}
}
