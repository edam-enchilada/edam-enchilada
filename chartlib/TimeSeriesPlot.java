package chartlib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class TimeSeriesPlot extends Chart {
	public TimeSeriesPlot(Dataset[] datasets) {
//		numCharts = 2;
		numCharts = datasets.length;
		title = "New Chart";
		hasKey = true;
		this.datasets = datasets;
		setupLayout();
		
		int numSequences = datasets.length;

		//this should not all be here!
		//It should go in createPanel
		/*this.setHasKey(false);
		this.setTitleX(0, "Time");
		this.drawXAxisAsDateTime(0);
		
		this.setNumTicks(10, 10, 1, 1);
		this.setBarWidth(3);
		this.setPreferredSize(new Dimension(400, 400));
		*/
	}
	
	
	
	protected JPanel createChartPanel(){
		
		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new GridLayout(0, 1)); //one column of chart areas
		int height = 400, width = 400;
		
		//Layered pane for overlapping the graphs
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setOpaque(true);
		layeredPane.setBackground(Color.WHITE);
		//MUST SET SIZE or it won't work!
		layeredPane.setPreferredSize(new Dimension(height,width));
		
		chartAreas = new ArrayList<ChartArea>();
		for (int count = 0; count < numCharts; count++) {
			//anonymous class, just like normal except doesn't draw a background
			//this makes it transparent
			ChartArea nextChart = new LinePointsChartArea(datasets[count]){
				protected void drawBackground(Graphics2D g2d){
					;
				}
			};
			//also need to setOpaque to false
			nextChart.setOpaque(false);
			nextChart.setTitleY( "Sequence " + (count + 1) + " Value");
			//MUST SET SIZE or it won't work!
			nextChart.setSize(new Dimension(height,width));
			nextChart.setAxisBounds( 0, 1, 0, 1);
			
			nextChart.setForegroundColor(DATA_COLORS[count]);
			chartAreas.add(nextChart);
			//MUST ADD WITH AN INTEGER or it won't work
			layeredPane.add(nextChart,new Integer(count));
		}
		chartPanel.add(layeredPane);
		
		return chartPanel;
	}
}
