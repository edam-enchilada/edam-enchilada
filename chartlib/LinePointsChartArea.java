package chartlib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JFrame;

import chartlib.GraphAxis.AxisLabeller;

public class LinePointsChartArea extends ChartArea {
	private static final int EXTRA_DATETIME_SPACE = 15;
	
	public LinePointsChartArea(Dataset dataset) {
		super(dataset);
		setPreferredSize(new Dimension(400, 400));
	}
	
	public LinePointsChartArea(Dataset dataset, Color color) {
		this(dataset);
		this.foregroundColor = color;
		setPreferredSize(new Dimension(400, 400));
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
	
	
	
	public void drawXAxisAsDateTime(){
		
	}
	
	/**
	 * updateAxes() tests for the axes not existing yet, so this method actually
	 * just calls that one.
	 *
	 */
	protected void createAxes() {
		super.createAxes();
		this.xAxis.setLabeller(
				new AxisLabeller() {
					private SimpleDateFormat dateFormat = 
						new SimpleDateFormat("M/dd/yy");
					private SimpleDateFormat timeFormat = 
						new SimpleDateFormat("HH:mm:ss");
					// a default labeller, which reports the value rounded to the 100s place.
					public String[] label(double value) {
						Date date = new Date();
						date.setTime((long)value);
						String[] label = new String[2];
						label[0] = dateFormat.format(date);
						label[1] = timeFormat.format(date);
						return  label;
					}
				}
		);
		
	}
	
	/**
	 * Indicates the portion of the chart value in which data is displayed.
	 * Creates special dimensions for displaying DateTimes on the x-axis
	 * @return A rectangle containing the data display value.
	 */
	public Rectangle getDataAreaBounds()
	{
		Rectangle area = super.getDataAreaBounds();
		area.setSize(area.height-EXTRA_DATETIME_SPACE,
				area.width - EXTRA_DATETIME_SPACE);
		return area;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dataset d = new Dataset();
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 1));
		
		LinePointsChartArea lca = new LinePointsChartArea(d);
		
		lca.setAxisBounds(0, 4, 0, 4);
		lca.setTitleX("Boogie");
		lca.setTitleY("Groove");
		
		
		JFrame f = new JFrame("woopdy doo");
		f.getContentPane().add(lca);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
	}

}
