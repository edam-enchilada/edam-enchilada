package chartlib;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

public class BarChartArea extends ChartArea implements LocatablePeaks {
	public BarChartArea(){
		super();
		setMinimumSize(new Dimension(100, 100));
	}
	
	public BarChartArea(Dataset dataset) {
		super(dataset);
		setMinimumSize(new Dimension(100, 100));
	}

	@Override
	public void drawAxes(Graphics2D g2d) {
		super.drawAxes(g2d);
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
	
	/* (non-Javadoc)
	 * @see chartlib.LocatablePeaks#getBarAt(java.awt.Point, int)
	 */
	public Double getBarAt(Point p, int buf)
	{
		return selectBar(p, buf, datasets.get(0));		
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
