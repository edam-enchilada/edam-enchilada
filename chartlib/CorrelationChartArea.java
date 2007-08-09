/**
 * 
 */
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

import javax.swing.JFrame;


/**
 * @author olsonja
 *
 */
public class CorrelationChartArea extends ChartArea {
	protected AxisTitle at;
	private static final int EXTRA_DATETIME_SPACE = 15;
	
	/**
	 * 
	 */
	public CorrelationChartArea(Dataset correlationDataset,String title) {
		super();
		datasets.add(correlationDataset);
		setMinimumSize(new Dimension(100, 100));
		at = new AxisTitle(title, AxisTitle.AxisPosition.LEFT, new Point(200, 200));
		
		
	}
	
	/**
	 * 
	 */
	public CorrelationChartArea(Dataset correlationDataset) {
		this(correlationDataset,"Correlation Comparison");		
	}
	
	

	/* (non-Javadoc)
	 * @see chartlib.GenericChartArea#drawData(java.awt.Graphics2D)
	 */
	@Override
	protected void drawData(Graphics2D g2d) {
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		Rectangle dataArea = getDataAreaBounds();
		
		Dataset correlationData = datasets.get(0);
		this.drawDataPoints(g2d,correlationData);
		
		Dataset.Statistics stats = correlationData.getCorrelationStats();
		//System.out.println("b: "+stats.b+"\ta: "+stats.a+"\tr^2: "+stats.r2);
		double leftSideY = stats.b * xAxis.getMin() + stats.a;
		double rightSideY = stats.b * xAxis.getMax() + stats.a;
		
		double startX = dataArea.x;
		double startY = dataArea.y + dataArea.height 
						- (yAxis.relativePosition(leftSideY) * dataArea.height);
		
		double endX = dataArea.width + dataArea.x;
		double endY = dataArea.y + dataArea.height 
						- (yAxis.relativePosition(rightSideY) * dataArea.height);
		
		g2d.setColor(Color.RED);
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.draw(new Line2D.Double(startX, startY, endX, endY));
		
		//cleanup
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}

	protected void drawPoint(Graphics2D g2d,double xCoord, double yCoord){
		drawPointX(g2d,xCoord,yCoord);
	}
	
	/**
	 * updateAxes() tests for the axes not existing yet, so this method actually
	 * just calls that one.
	 *
	 *//*
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
	
	*//**
	 * Indicates the portion of the chart value in which data is displayed.
	 * Creates special dimensions for displaying DateTimes on the x-axis
	 * @return A rectangle containing the data display value.
	 *//*
	public Rectangle getDataAreaBounds()
	{
		Rectangle area = super.getDataAreaBounds();
		area.setSize(area.height-EXTRA_DATETIME_SPACE,
				area.width - EXTRA_DATETIME_SPACE);
		return area;
	}*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dataset d = new Dataset();
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 3));
		
		CorrelationChartArea cca = new CorrelationChartArea(d);
		
		cca.setAxisBounds(0, 4, 0, 4);
		
		
		
		JFrame f = new JFrame("woopdy doo");
		f.getContentPane().add(cca);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);

	}

}
/*private Dataset dataset;
private Color color = Color.RED;
private AxisTitle at;

public LineChartArea(Dataset dataset) {
	this.dataset = dataset;
	setPreferredSize(new Dimension(400, 400));
	at = new AxisTitle("W00t", AxisTitle.AxisPosition.LEFT, new Point(200, 200));
}

public LineChartArea(Dataset dataset, Color color) {
	this(dataset);
	this.color = color;
}

@Override
public void drawAxes(Graphics2D g2d) {
	super.drawAxes(g2d);
	at.draw(g2d);
}

*//**
 * Draws the data in a continuous line by drawing only one
 * data point per horizontal pixel.
 * 
 * @param g2d
 *//*
@Override
public void drawData(Graphics2D g2d) {
	drawDataLines(g2d,dataset);
	drawDataPoints(g2d,dataset);
}



*//**
 * @param args
 *//*
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
*/