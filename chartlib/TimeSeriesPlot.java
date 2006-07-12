package chartlib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

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
		
		chartAreas = new ArrayList<ChartArea>();
		for (int count = 0; count < numCharts; count++) {
			ChartArea nextChart = new LinePointsChartArea(datasets[count]);
			//nextChart.setTitleY( "Sequence " + (count + 1) + " Value");
			nextChart.setAxisBounds( 0, 1, 0, 1);
			
			nextChart.setForegroundColor(DATA_COLORS[count]);
			chartAreas.add(nextChart);
			
			// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
			chartPanel.add(chartAreas.get(count));
		}
		
		return chartPanel;
	}
}
