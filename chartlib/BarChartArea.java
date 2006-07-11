package chartlib;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

public class BarChartArea extends ChartArea{
	private AxisTitle at;
	
	public BarChartArea(Dataset dataset) {
		super(dataset);
		setPreferredSize(new Dimension(400, 400));
		at = new AxisTitle("W00t", AxisTitle.AxisPosition.LEFT, new Point(200, 200));
	}

	@Override
	public void drawAxes(Graphics2D g2d) {
		super.drawAxes(g2d);
		at.draw(g2d);
	}
	
	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	@Override
	public void drawData(Graphics2D g2d) {
		drawDataPoints(g2d,datasets.get(0));
	}
	
	/**
	 * If a bar drawn at point p, returns the corresponding data point.
	 * @param p A point in screen coordinates.
	 * @param buf A point within buf pixels of the bar will count as part of the bar.
	 * @return The X coordinate in data space the found bar represents.
	 */
	public Double getBarAt(Point p, int buf)
	{
		Rectangle testbar;
		Rectangle dataArea = getDataAreaBounds();
		Iterator<DataPoint> iterator = datasets.get(0).iterator();
		while (iterator.hasNext()) {
			DataPoint curPoint = iterator.next();

			double x = curPoint.y, y = curPoint.y;

			int xCoord = (int) (dataArea.x + xAxis.relativePosition(curPoint.x)
					* dataArea.width);
			double yCoord = (dataArea.y + dataArea.height - (yAxis
					.relativePosition(curPoint.y) * dataArea.height));

			testbar = new Rectangle(
					(int)( xCoord - barWidth / 2), //centers the bar on the value
					(int)( yCoord),
					(int)(barWidth),
					(int)(-1*yCoord)+ (dataArea.y + dataArea.height) );
			if (testbar.contains(p)){
				return new Double(x);
			}

		}
		return null;
	}
	
	protected void drawPoint(Graphics2D g2d,double xCoord, double yCoord){
		drawPointBar(g2d,xCoord,yCoord);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
