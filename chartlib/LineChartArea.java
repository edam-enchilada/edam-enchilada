package chartlib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

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
