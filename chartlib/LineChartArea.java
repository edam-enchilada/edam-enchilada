package chartlib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JFrame;

public class LineChartArea extends ChartArea {
	private AxisTitle at;
	
	public LineChartArea(Dataset dataset) {
		super(dataset);
		setPreferredSize(new Dimension(400, 400));
		at = new AxisTitle("W00t", AxisTitle.AxisPosition.LEFT, new Point(200, 200));
	}
	
	public LineChartArea(Dataset dataset, Color color) {
		this(dataset);
		this.foregroundColor = color;
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
		drawDataLines(g2d,datasets.get(0));
		drawDataPoints(g2d,datasets.get(0));
	}
	
	protected void drawPoint(Graphics2D g2d,double xCoord, double yCoord){
		drawPointX(g2d,xCoord,yCoord);
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
		lca.setTicksX(1, 1);
		lca.setTicksY(1, 1);
		
		
		
		JFrame f = new JFrame("woopdy doo");
		f.getContentPane().add(lca);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
	}

}
