package chartlib;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


/**
 * A class for scatter plots that take two datasets and plot their y-values
 * against one another.  This class exists to encapsulate the weirdness of
 * chartlib, so that maybe it can get refactored more easily.
 * 
 * @author smitht
 *
 */
public class ScatterPlot extends Chart {

	private Dataset[] datasets;
	private Dataset correlationData;

	public ScatterPlot(Dataset ds1, Dataset ds2) {
		title = "New Chart";
		hasKey = true;
		this.datasets = new Dataset[3];
		datasets[0] = ds1;
		datasets[1] = ds2;
		
		correlationData = new Dataset();
		Iterator<DataPoint> iter1 = ds1.iterator();
		Iterator<DataPoint> iter2 = ds2.iterator();
		
		while(iter1.hasNext())
		{
			DataPoint dpX = iter1.next();
			DataPoint dpY = iter2.next();
			
			if (dpY != null) {
				double x = dpX.y, y = dpY.y;
				correlationData.add(new DataPoint(x,y));
			}
		}
		
		datasets[2] = correlationData;
		
		chartAreas.add(makeChartArea());
		
		setupLayout();
		packData();
	}
	
	protected ChartArea makeChartArea(){
		ChartArea nextChart = new CorrelationChartArea(correlationData);
		nextChart.setAxisBounds(0, 1, 0, 1);

		nextChart.setForegroundColor(DATA_COLORS[0]);
		return nextChart;
	}
	
	public void setTitle(String title) {
		Dataset.Statistics stats = correlationData.getCorrelationStats();
		super.setTitle(String.format(title, stats.r2));
	}
	
	public static void main(String[] args) {
		Dataset d = new Dataset();
		
		d.add(new DataPoint(0, 0));
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 3));
		
		ScatterPlot plot = new ScatterPlot(d,d);
		
		
		JFrame f = new JFrame("woopdy doo");
		f.getContentPane().add(plot);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(500, 400));
		f.pack();
		f.setVisible(true);

	}
}
