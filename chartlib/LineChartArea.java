package chartlib;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.Iterator;

import javax.swing.JFrame;

public class LineChartArea extends ChartArea {
	public LineChartArea(Dataset dataset) {
		super(dataset);
		setPreferredSize(new Dimension(400, 400));
	}
	
	public LineChartArea(Dataset dataset, Color color) {
		this(dataset);
		this.foregroundColor = color;
	}
	
	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	@Override
	public void drawData(Graphics2D g2d) {
		drawDataLines(g2d,datasets.get(0));
	}
	
	protected void drawPoint(Graphics2D g2d,double xCoord, double yCoord){
		drawPointX(g2d,xCoord,yCoord);
	}
	
	/**
	 * Modification of the basic drawDataLines which ensures that no two lines occupy the same pixel.
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 */
	protected void drawDataLines(Graphics2D g2d,Dataset dataset){
		if(dataset == null||dataset.isEmpty()) return;
		Rectangle dataArea = getDataAreaBounds();
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(foregroundColor);
		g2d.clip(dataArea);	//constrains drawing to the data value
		g2d.setStroke(new BasicStroke(1.5f));
		
		int numPoints = 0;
		Iterator<DataPoint> iterator 
			= dataset.subSet(new DataPoint(xAxis.getMin(), 0),
							new DataPoint(xAxis.getMax(), 0)).iterator();
		
		//by making an array the size of the display area and filling that
		// you ensure that no vertical lines will be drawn 
		// (you ignore points that are closer than the distance represented by a pixel)
		// This is done for the sake of efficiency and is not always a good idea.
		// If vertical lines should be considered acceptable, instead use the drawDataLines method in ChartArea
		
		double[] coords = new double[dataArea.width];
//		loops through all data points building array of points to draw
		while(iterator.hasNext())
		{
			DataPoint curPoint = iterator.next();
			
			double pointPos = xAxis.relativePosition(curPoint.x);
			int xCoord = (int) (xAxis.relativePosition(curPoint.x) * dataArea.width);
			double yCoord = (dataArea.y + dataArea.height 
					- (yAxis.relativePosition(curPoint.y) * dataArea.height));
			
		
			if (yCoord > 0 && yCoord <= (dataArea.y + dataArea.height) && xCoord >= 0 && xCoord < dataArea.width) {
				if (coords[xCoord] == 0 || yCoord < coords[xCoord]){
					coords[xCoord] = yCoord;
				}
			} else if (curPoint.y == -999)
				coords[xCoord] = -999.0;
		}
		
		// Then draws them:
		int lastX = 0;
		double lastY = -999.0;
		for (int i = 0; i < coords.length; i++) {
			if (coords[i] == 0)
				continue;

			int xCoord = dataArea.x + i;
			double yCoord = coords[i];
			
			if (yCoord != -999.0 && lastY != -999.0)
				g2d.draw(new Line2D.Double((double) lastX, lastY, (double) xCoord, yCoord));
			else if (yCoord != -999.0) {
				// Point is valid, but last point wasn't... so just draw a large point:

				g2d.setStroke(new BasicStroke(2.5f));
				g2d.draw(new Line2D.Double((double) xCoord, coords[i], (double) xCoord, coords[i]));
				g2d.setStroke(new BasicStroke(1.5f));
			}
			
			lastX = xCoord;
			lastY = yCoord;
		}
		
		if (dataset.first().x < xAxis.getMin()) {
			drawMorePointsIndicator(0, g2d);
		}
		if (dataset.last().x > xAxis.getMax()) {
			drawMorePointsIndicator(1, g2d);
		}
		
		
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
		
	}

	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Dataset d = new Dataset();
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 1));
		
		LineChartArea lca = new LineChartArea(d);
		
		lca.setAxisBounds(0, 4, 0, 4);
		lca.setTitleX("Boogie");
		lca.setTitleY("Groove");

		
		JFrame f = new JFrame("woopdy doo");
		f.setLayout(new BorderLayout());
		
		f.getContentPane().add(lca,BorderLayout.CENTER);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
	}

}
