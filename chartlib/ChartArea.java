/**
 * 
 */
package chartlib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.*;

/**
 * @author olsonja
 *
 */
public class ChartArea extends AbstractMetricChartArea {
	protected ArrayList<Dataset> datasets;
	protected boolean drawXAxisAsDateTime;
	protected int barWidth = 3;
	
	/**
	 * 
	 */
	public ChartArea() {
		super();
		drawXAxisAsDateTime = false;
		datasets = new ArrayList<Dataset>();
		// TODO Auto-generated constructor stub
	}
	
	public ChartArea(Dataset dataset) {
		super();
		drawXAxisAsDateTime = false;
		datasets = new ArrayList<Dataset>();
		datasets.add(dataset);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	protected void drawDataLines(Graphics2D g2d,Dataset dataset){
		if(dataset == null) return;
		Rectangle dataArea = getDataAreaBounds();
		// these booleans show whether we've drawn indicators that 
		// more data exist in each direction
		boolean drawnMoreLeft = false, drawnMoreRight = false;
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(foregroundColor);
		g2d.clip(dataArea);	//constrains drawing to the data value
		g2d.setStroke(new BasicStroke(1.5f));
		
		//double[] coords = new double[dataArea.width];
		
		//	loops through all data points building array of points to draw
		int lastX = 0;
		double lastY = -999.0;
		int numPoints = 0;
		Iterator<DataPoint> iterator = dataset.iterator();
		while(iterator.hasNext())
		{
			DataPoint curPoint = iterator.next();
			
			double x = curPoint.y, y = curPoint.y;
			//System.out.println("X: "+x+"\tY:: "+y);
			/*if (x >= 0 && x <= 1) {
*/
			double pointPos = xAxis.relativePosition(x);
			if (pointPos < 0 && !drawnMoreLeft) {
				drawnMoreLeft = true;
				drawMorePointsIndicator(0, g2d);
			}
			else if (pointPos > 1 && !drawnMoreRight) {
				drawnMoreRight = true;
				drawMorePointsIndicator(1, g2d);
			}
			else {
				int xCoord = (int) (dataArea.x+xAxis.relativePosition(curPoint.x) 
						* dataArea.width);
				double yCoord = (dataArea.y + dataArea.height 
						- (yAxis.relativePosition(curPoint.y) * dataArea.height));
			
				if(numPoints==0){
				g2d.draw(new Line2D.Double((double) dataArea.x+0,(dataArea.y + dataArea.height 
					- (yAxis.relativePosition(0) * dataArea.height)), (double) xCoord, yCoord));
				}else{
					g2d.draw(new Line2D.Double((double) lastX, lastY, (double) xCoord, yCoord));
				}
				numPoints++;
				lastX = xCoord;
				lastY = yCoord;
			}
		}
		/*
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
		
		*/
		if(lastX<=dataArea.x+dataArea.width-1){
			g2d.draw(new Line2D.Double((double) lastX, lastY, (double) dataArea.x+dataArea.width-1,(dataArea.y + dataArea.height 
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
			double pointPos = xAxis.relativePosition(x);
			if (pointPos < 0 && !drawnMoreLeft) {
				drawnMoreLeft = true;
				drawMorePointsIndicator(0, g2d);
			}
			else if (pointPos > 1 && !drawnMoreRight) {
				drawnMoreRight = true;
				drawMorePointsIndicator(1, g2d);
			}else{
				int xCoord = (int) (dataArea.x + xAxis
						.relativePosition(curPoint.x)
						* dataArea.width);
				double yCoord = (dataArea.y + dataArea.height - (yAxis
						.relativePosition(curPoint.y) * dataArea.height));
				drawPoint(g2d, xCoord, yCoord);
			}
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
		Object oldAntialias = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		
		Rectangle dataArea = getDataAreaBounds();
		Rectangle bar;
		bar = new Rectangle(
				(int)( xCoord - barWidth / 2), //centers the bar on the value
				(int)( yCoord),
				(int)(barWidth),
				(int)(-1*yCoord)+ (dataArea.y + dataArea.height) );
		
		//draw the bar
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(foregroundColor);				
		g2d.fill(bar);
		
		//draws border around bar
		g2d.setColor(Color.BLACK);
		g2d.draw(bar);
		
		
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				oldAntialias);
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
	
	/* (non-Javadoc)
	 * @see chartlib.GenericChartArea#drawData(java.awt.Graphics2D)
	 */
	@Override
	protected void drawData(Graphics2D g2d) {
		// TODO Auto-generated method stub
		drawDataPoints(g2d,datasets.get(0));
	}
	

	/**
	 * Sets the bounds of either or both axes to fit the dataset.
	 * If the dataset is empty, leaves the axes alone.
	 * If only packY is true, adjusts the data to fit the y values of the points
	 * within the current x axis window.
	 * @param packX Whether to change the x axis.
	 * @param packY Whether to change the y axis.
	 */
	public void packData(boolean packX, boolean packY){
		
			Dataset dataset = datasets.get(0);
			
			//empty dataset: do nothing
			if (dataset == null || dataset.size() == 0 || (packX == false && packY == false))
				return;

			double xmin = 0, ymin = 0, xmax = 0, ymax = 0;

			if(packX == true) {
				double[][] bounds = findAllMinsMaxes(dataset);
				xmin = bounds[0][0];
				xmax = bounds[0][1];
				ymin = bounds[1][0];
				ymax = bounds[1][1];
			} else {
				double[] bounds = findYMinMax(dataset);
				if (bounds == null) return;
				ymin = bounds[0];
				ymax = bounds[1];
			}
			
			double newXmin = 0, newXmax = 0, newYmin = 0, newYmax = 0;
			
			//one element:
			if(xmin == xmax && ymin == ymax)
			{
				newXmin = xmin - xmin / 2;
				newXmax = xmax + xmax / 2;
				newYmin = 0;
				newYmax = ymax + ymax / 10;
			}
			else
			{
//				adds some extra space on the edges
				newXmin = xmin - ((xmax - xmin) / 10);
				newXmax = xmax + ((xmax - xmin) / 10);
				newYmin = ymin - ((ymax - ymin) / 10);
				newYmax = ymax + ((ymax - ymin) / 10);
			}
			
			if (packX) {
				this.setXMin(newXmin);
				this.setXMax(newXmax);
			}
			if (packY) {
				this.setYMin(newYmin);
				this.setYMax(newYmax);
			}
				
			createAxes();
			repaint();
	}
	
	public Dataset getDataset(int i){
		return datasets.get(i);
	}
	
	public void setDataset(int i, Dataset newDataset){
		datasets.set(i,newDataset);
	}
	
	public void setDataset(Dataset newDataset){
		datasets.clear();
		datasets.add(newDataset);
	}
	
	public void setDatasets(Dataset[] newDatasets){
		datasets.clear();
		for(Dataset newDataset : newDatasets){
			datasets.add(newDataset);
		}
	}
	
	
	/**
	 * Finds the maximum and minimum x and y.  
	 * (Finds the (complete) range and domain of the dataset)
	 * @param dataset
	 * @return
	 */
	public double[][] findAllMinsMaxes(Dataset dataset) {
		double xmin, ymin, xmax, ymax;	
		xmin = ymin = Double.MAX_VALUE;
		xmax = ymax = Double.MIN_VALUE;
		DataPoint dp;
		double[][] ret = new double[2][2];
		ret[0][0] = 0;
		ret[0][1] = 0;
		ret[1][0] = 0;
		ret[1][1] = 0;
		if(dataset == null){
			return ret;
		}
		
		int size = dataset.size();
		java.util.Iterator iterator = dataset.iterator();

		while(iterator.hasNext())
		{
			dp = (DataPoint)(iterator.next());
			if(dp.y < ymin) ymin = dp.y;
			if(dp.y > ymax) ymax = dp.y;
			if(dp.x < xmin) xmin = dp.x;
			if(dp.x > xmax) xmax = dp.x;
		}
		
		ret[0][0] = xmin;
		ret[0][1] = xmax;
		ret[1][0] = ymin;
		ret[1][1] = ymax;
		return ret;
	}
	
	
	/**
	 * Finds the minimum and maximum y values PRESERVING the current domain of x.
	 * (Finds the range of the data for the current (restricted) domain.)
	 * @param dataset
	 * @return
	 */
	public double[] findYMinMax(Dataset dataset) {
		double xmin, ymin, xmax, ymax;	
		xmin = ymin = Double.MAX_VALUE;
		xmax = ymax = Double.MIN_VALUE;
		DataPoint dp;
		
		double[] ret = new double[2];
		ret[0] = 0;
		ret[1] = 0;
		if(dataset == null){
			return ret;
		}
		
		int size = 0;
		
		SortedSet<DataPoint> visible 
			= dataset.subSet(new DataPoint(xAxis.getMin(), 0),
					         new DataPoint(xAxis.getMax(), 0));
		
		java.util.Iterator iterator = visible.iterator();
		
		while(iterator.hasNext())
		{
			dp = (DataPoint)(iterator.next());
		
			if(dp.y < ymin) ymin = dp.y;
			if(dp.y > ymax) ymax = dp.y;
			size++;
		}
		if (size == 0) return null;
		
		ret[0] = ymin;
		ret[1] = ymax;
		return ret;
	}
	
	

	/**
	 * @return Returns the barWidth.
	 */
	public int getBarWidth() {
		return barWidth;
	}

	/**
	 * @param barWidth The barWidth to set.
	 */
	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	/**
	 * @param backgroundColor The backgroundColor to set.
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	/**
	 * @return Returns the foregroundColor.
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}
	/**
	 * @param foregroundColor The foregroundColor to set.
	 */
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
