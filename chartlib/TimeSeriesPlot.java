package chartlib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class TimeSeriesPlot extends Chart {
	private int numCharts;
	private Dataset[] datasets;
	JLayeredPane layeredPane;
	

	public TimeSeriesPlot(Dataset[] datasets) {
		numCharts = datasets.length;
		title = "New Chart";
		hasKey = true;
		this.datasets = datasets;
		setupLayout();
		packData();
		
		//this should not all be here!
		//It should go in createPanel
		/*this.setHasKey(false);
		this.setTitleX(0, "Time");
		this.drawXAxisAsDateTime(0);
		
		this.setNumTicks(10, 10, 1, 1);
		this.setBarWidth(3);
		this.setPreferredSize(new Dimension(400, 400));
		*/
		//Overwrite the size 
		
	}
	
	
	
	protected JPanel createChartPanel(){
		if (datasets == null) {
			datasets = new Dataset[0];
		}
		
		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new GridLayout(0, 1)); //one column of chart areas
		int height = 405;
		int width = 460;
		
		//Layered pane for overlapping the graphs
		layeredPane = new JLayeredPane();
		layeredPane.setOpaque(true);
		layeredPane.setBackground(Color.WHITE);
		//MUST SET SIZE or it won't work!
		layeredPane.setPreferredSize(new Dimension(width,height));
		
		chartAreas = new ArrayList<AbstractMetricChartArea>();
		for (int count = 0; count < datasets.length; count++) {
			//anonymous class, just like normal except doesn't draw a background
			//this makes it transparent
			ChartArea nextChart = new LinePointsChartArea(datasets[count]){
				protected void drawBackground(Graphics2D g2d){
					;
				}
			};
			nextChart.setSeriesNumber(count+1);
			//also need to setOpaque to false
			nextChart.setOpaque(false);
			nextChart.setTitleY( "Sequence " + (count + 1) + " Value");
			//MUST SET SIZE or it won't work!
			nextChart.setSize(new Dimension(width,height));
			nextChart.setMinimumSize(new Dimension(width,height));
			nextChart.setAxisBounds( 0, 1, 0, 1);
			nextChart.setNumTicksX(5, 3);
			System.out.println("size: "+nextChart.getSize().width+"\t"+nextChart.getSize().width);
			nextChart.setForegroundColor(DATA_COLORS[count]);
			chartAreas.add(nextChart);
			//MUST ADD WITH AN INTEGER or it won't work
			layeredPane.add(nextChart,new Integer(count));
		}
		chartPanel.add(layeredPane);
		
		coordinateChartAreas();
		
		return chartPanel;
	}
	
	/**
	 * Overrides the ChartArea.getChartLocation method because 
	 * all ChartAreas are drawn on top of each other, so you don't
	 *  want to add the height of the other ChartAreas 
	 * @param index Which chart to locate.
	 * @return A Point containing the key.
	 */
	protected Point getChartLocation(int index)
	{
		Point p = new Point();
		p.x = 10;
		p.y = titleLabel.getHeight();
		return p;
	}
	
	public void packData(){
		coordinateChartAreas();
		packData(false, true, true);
	}
	
	public void packData(int index, boolean packX, boolean packY) {
		coordinateChartAreas();
		packData(false, true, true);
	}
	/*public void packData(boolean packX, boolean packY) {
		double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
		double newXmin = 0, newXmax = 0, newYmin = 0, newYmax = 0;

		for (int i = 0; i < datasets.length; i++) {
			// we assume that all chart areas in this chart are controlled by this code,
			// so that we can cast them to ChartArea.
			ChartArea chartArea = (ChartArea) chartAreas.get(i);
			Dataset dataset = datasets[i];

			// empty dataset: do nothing
			if (dataset == null || dataset.size() == 0
					|| (packX == false && packY == false))
				return;

			if (packX == true) {
				double[][] bounds = chartArea.findAllMinsMaxes(dataset);
				if (xmin > bounds[0][0])
					xmin = bounds[0][0];
				if (xmax < bounds[0][1])
					xmax = bounds[0][1];
				if (ymin > bounds[1][0])
					ymin = bounds[1][0];
				if (ymax < bounds[1][1])
					ymax = bounds[1][1];
			} else {
				double[] bounds = chartArea.findYMinMax(dataset);
				if (bounds == null)
					return;
				if (ymin > bounds[0])
					ymin = bounds[0];
				if (ymax < bounds[1])
					ymax = bounds[1];
				if (xmin > chartArea.xAxis.getMin())
					xmin = chartArea.xAxis.getMin();
				if (xmax < chartArea.xAxis.getMax())
					xmax = chartArea.xAxis.getMax();
			}
		}

		// one element:
		if (xmin == xmax && ymin == ymax) {
			if (packX) {
				newXmin = xmin - xmin / 2;
				newXmax = xmax + xmax / 2;
			}
			newYmin = 0;
			newYmax = ymax + ymax / 10;
		} else {
			// adds some extra space on the edges
			if (packX) {
				newXmin = xmin - ((xmax - xmin) / 10);
				newXmax = xmax + ((xmax - xmin) / 10);
			} else {
				newXmin = xmin;
				newXmax = xmax;
			}
			if(packY){
				newYmin = ymin - ((ymax - ymin) / 10);
				newYmax = ymax + ((ymax - ymin) / 10);
			}
		}
		System.out.println("newX: " + newXmin + "-" + newXmax + "\nnewY: "
				+ newYmin + "-" + newYmax);
		
		for (int i = 0; i < datasets.length; i++) {
			ChartArea chartArea = (ChartArea) chartAreas.get(i);
			if(packX)chartArea.setXAxisBounds(newXmin, newXmax);
			if(packY)chartArea.setYAxisBounds(newYmin, newYmax);
			chartArea.createAxes();
			chartArea.repaint();
		}
	}*/
	
	/**
	 * Set the chart to draw the x-axis as a date instead
	 * of as a number...
	 * @param index The chart to act on
	 */
	public void drawXAxisAsDateTime(int index) {
		((LinePointsChartArea)chartAreas.get(index)).drawXAxisAsDateTime();
	}
	
	
	public void coordinateChartAreas() {
		double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
		double newXmin = 0, newXmax = 0, newYmin = 0, newYmax = 0;

		for (int i = 0; i < datasets.length; i++) {
			// we assume that all chart areas in this chart are controlled by this code,
			// so that we can cast them to ChartArea.
			ChartArea chartArea = (ChartArea) chartAreas.get(i);
			Dataset dataset = datasets[i];

			// empty dataset: do nothing
			if (dataset == null || dataset.size() == 0)
				return;

				double[][] bounds = chartArea.findAllMinsMaxes(dataset);
				if (xmin > bounds[0][0])
					xmin = bounds[0][0];
				if (xmax < bounds[0][1])
					xmax = bounds[0][1];
				if (ymin > bounds[1][0])
					ymin = bounds[1][0];
				if (ymax < bounds[1][1])
					ymax = bounds[1][1];
			
		}

		// one element:
		if (xmin == xmax && ymin == ymax) {
				newXmin = xmin - xmin / 2;
				newXmax = xmax + xmax / 2;
			
		} else {
			// adds some extra space on the edges
				newXmin = xmin - ((xmax - xmin) / 10);
				newXmax = xmax + ((xmax - xmin) / 10);
			
		}
		/*System.out.println("newX: " + newXmin + "-" + newXmax + "\nnewY: "
				+ newYmin + "-" + newYmax);
		*/
		for (int i = 0; i < datasets.length; i++) {
			ChartArea chartArea = (ChartArea) chartAreas.get(i);
			chartArea.setXAxisBounds(newXmin, newXmax);
			chartArea.createAxes();
			chartArea.repaint();
		}
	}
	
	/*public void addMouseListener(MouseListener ml){
		super.addMouseListener(ml);
		layeredPane.addMouseListener(ml);
		for(AbstractMetricChartArea chartArea : chartAreas){
			chartArea.addMouseListener(ml);
		}
		
	}
	
	public void addMouseMotionListener(MouseMotionListener ml){
		super.addMouseMotionListener(ml);
		layeredPane.addMouseMotionListener(ml);
		for(AbstractMetricChartArea chartArea : chartAreas){
			chartArea.addMouseMotionListener(ml);
		}
		
	}*/
	public static void main(String[] args){
		Dataset d = new Dataset();
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 1));
		Dataset[] datasets = new Dataset[1];
		datasets[0] = d;
		
		TimeSeriesPlot plot = new TimeSeriesPlot(datasets);
		plot.setPreferredSize(new Dimension(400,400));
		JFrame f = new JFrame("woopdy doo");
		f.getContentPane().add(plot);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
	}

}
