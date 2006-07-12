package chartlib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

public class SpectrumPlot extends Chart {
	private int datatype;
	public static final int UNDEFINED = -1;
	public static final int PEAK_DATA = 1;
	public static final int SPECTRUM_DATA = 2;
	
	
	public SpectrumPlot() {
		//numCharts = 2;
		numCharts = 2;
		title = "New Chart";
		hasKey = true;
		datasets = new Dataset[2];
		datatype = SpectrumPlot.UNDEFINED;
		setupLayout();
		
		
		//this should not all be here!
		//It should go in createPanel
		/*this.setHasKey(false);
		this.setTitle("Positive and negative peak values");
		this.setTitleX(0,"Positive mass-to-charge ratios");
		this.setTitleY(0,"Area");
		//this.setTitleY(1,"Area");
		//this.setTitleX(1,"Negative mass-to-charge ratios");
		this.setAxisBounds(0,400, CURRENT_VALUE, CURRENT_VALUE);
		this.setNumTicks(10,10, 1,1);
		this.setBarWidth(3);
		this.setColor(0,Color.red);
		*///this.setColor(1,Color.blue);
		
		packData(false, true); //updates the Y axis scale.
	}
	
	public SpectrumPlot(Dataset pos, Dataset neg){
		this();
		datasets[0] = pos;
		datasets[1] = neg;
		packData(false, true); //updates the Y axis scale.
	}
	
	protected JPanel createChartPanel(){
		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new GridLayout(0, 1)); //one column of chart areas
		
		chartAreas = new ArrayList<ChartArea>();
		switch(datatype){
		case SpectrumPlot.UNDEFINED:
			for (int count = 0; count < numCharts; count++) {
			//ChartArea nextChart = new BarChartArea(datasets[count]);
			ChartArea nextChart = new BarChartArea(new Dataset());
			nextChart.setForegroundColor(DATA_COLORS[count]);
			chartAreas.add(nextChart);
			
			// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
			chartPanel.add(chartAreas.get(count));
		}
			break;
		case SpectrumPlot.PEAK_DATA:
			for (int count = 0; count < numCharts; count++) {
			//ChartArea nextChart = new BarChartArea(datasets[count]);
			ChartArea nextChart = new BarChartArea(datasets[count]);
			nextChart.setForegroundColor(DATA_COLORS[count]);
			chartAreas.add(nextChart);
			
			// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
			chartPanel.add(chartAreas.get(count));
		}
			break;
		case SpectrumPlot.SPECTRUM_DATA:
			for (int count = 0; count < numCharts; count++) {
				System.out.println("building spectrum chart");
				ChartArea nextChart = new LineChartArea(datasets[count]);
				nextChart.setForegroundColor(DATA_COLORS[count]);
				chartAreas.add(nextChart);
				
				// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
				chartPanel.add(chartAreas.get(count));
			}
				break;
		

		}
		
		/*ChartArea chart = new BarChartArea(datasets[0]){
			protected void drawBackground(Graphics2D g2d){
				;
			}
		};
		chart.setOpaque(false);
		for (int count = 1; count < numCharts; count++) {
			ChartArea nextChart = new BarChartArea(datasets[count]){
				protected void drawBackground(Graphics2D g2d){
					;
				}
			};
			nextChart.setOpaque(false);
			chart.add(nextChart);
			
			// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
			
		}
		chartPanel.add(chart);
		chartAreas.add(chart);
		*/
		return chartPanel;
	}
	
	public void displaySpectra(Dataset pos, Dataset neg) {
		setTitleY(0, "Intensity");
		setTitleY(1, "Intensity");
		datasets = new Dataset[2];
		datasets[0] = pos;
		datasets[1] = neg;
		datatype = SpectrumPlot.SPECTRUM_DATA;
		
		this.ckPanel.remove(this.chartPanel);
		this.ckPanel.remove(this.key);
		chartPanel = this.createChartPanel();
		this.ckPanel.add(chartPanel);
		this.ckPanel.add(key);
		this.ckPanel.repaint();
		packData(false, true); //updates the Y axis scale.
	}
	
	public void displayPeaks(Dataset pos, Dataset neg) {
		setTitleY(0, "Area");
		setTitleY(1, "Area");
		
		datasets = new Dataset[2];
		datasets[0] = pos;
		datasets[1] = neg;
		datatype = SpectrumPlot.PEAK_DATA;
		
		this.ckPanel.remove(this.chartPanel);
		this.ckPanel.remove(this.key);
		chartPanel = this.createChartPanel();
		this.ckPanel.add(chartPanel);
		this.ckPanel.add(key);
		this.ckPanel.repaint();
		packData(false, true); //updates the Y axis scale.
	}
}
