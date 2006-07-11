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
import java.util.Iterator;

import javax.swing.JFrame;

/**
 * @author olsonja
 *
 */
public class CorrelationChartArea extends MetricChartArea {
	protected Dataset dataset1,dataset2;
	protected AxisTitle at;

	/**
	 * 
	 */
	public CorrelationChartArea(Dataset dataset1,Dataset dataset2,String title) {
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		setPreferredSize(new Dimension(400, 400));
		at = new AxisTitle(title, AxisTitle.AxisPosition.LEFT, new Point(200, 200));
	}
	
	/**
	 * 
	 */
	public CorrelationChartArea(Dataset dataset1,Dataset dataset2) {
		this(dataset1,dataset2,"Correlation Comparison");		
	}
	
	/**
	 * 
	 */
	public CorrelationChartArea(Dataset dataset1,Dataset dataset2,Color color) {
		this(dataset1,dataset2);
		this.foregroundColor = color;
	}

	/* (non-Javadoc)
	 * @see chartlib.GenericChartArea#drawData(java.awt.Graphics2D)
	 */
	@Override
	protected void drawData(Graphics2D g2d) {
		assert(dataset1 != null && dataset2 != null);
		assert(dataset1.size() == dataset2.size());
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		
		Dataset correlationData = new Dataset();
		Rectangle dataArea = getDataAreaBounds();
		Iterator<DataPoint> iterator = dataset1.iterator();
		while(iterator.hasNext())
		{
			DataPoint dpX = iterator.next();
			System.out.println("Time: "+dpX.x);
			DataPoint dpY = dataset2.get(dpX.x,.50);
			
			if (dpY != null) {
				double x = dpX.y, y = dpY.y;
				if(dpX.x != dpY.x)System.out.println("UNEQUAL");
				System.out.println("Y1: "+x+"\tY2: "+y);
				correlationData.add(new DataPoint(x,y));
			}
		}
		this.drawDataPoints(g2d,correlationData);
		
		Dataset.Statistics stats = dataset1.getCorrelationStats();
		
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
	 * @param args
	 */
	public static void main(String[] args) {
		Dataset d = new Dataset();
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 1));
		
		CorrelationChartArea cca = new CorrelationChartArea(d,d);
		
		cca.setAxisBounds(0, 4, 0, 4);
		cca.setTicksX(1, 1);
		cca.setTicksY(1, 1);
		
		
		
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