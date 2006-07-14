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

	public ScatterPlot(Dataset ds1, Dataset ds2) {
		numCharts = 1;
		title = "New Chart";
		hasKey = true;
		this.datasets = new Dataset[3];
		datasets[0] = ds1;
		datasets[1] = ds2;
		
		Dataset correlationData = new Dataset();
		Iterator<DataPoint> iterator = ds1.iterator();
		while(iterator.hasNext())
		{
			DataPoint dpX = iterator.next();
			DataPoint dpY = ds2.get(dpX.x,.50);
			
			if (dpY != null) {
				double x = dpX.y, y = dpY.y;
				correlationData.add(new DataPoint(x,y));
			}
		}
		
		datasets[2] = correlationData;
		
		setupLayout();
		packData();
		//this should not all be here!
		//It should go in createPanel
		/*this.setHasKey(false);
		this.setTitleY(0, "Sequence 1 Value");
		this.setTitleY(1, "Sequence 2 Value");
		this.setAxisBounds(0, 0, 1, 0, 1);
		this.setAxisBounds(1, 0, 1, 0, 1);
		this.setPreferredSize(new Dimension(400, 400));
		this.setBorder(new EmptyBorder(15, 0, 0, 0));
		*/
	}
	
	protected JPanel createChartPanel(){
		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new GridLayout(0, 1)); //one column of chart areas
		
		chartAreas = new ArrayList<ChartArea>();
		ChartArea nextChart = new CorrelationChartArea(datasets[0]);
		// nextChart.setTitleY( "Sequence " + (count + 1) + " Value");
		nextChart.setAxisBounds(0, 1, 0, 1);

		nextChart.setForegroundColor(DATA_COLORS[0]);
		chartAreas.add(nextChart);

		// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
		chartPanel.add(chartAreas.get(0));

		return chartPanel;
	}
	
	public void setTitle(String title) {
		Dataset.Statistics stats = getDataset(0).getCorrelationStats(getDataset(0));
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
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);

	}
}
