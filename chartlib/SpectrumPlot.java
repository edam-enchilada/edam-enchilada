package chartlib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

public class SpectrumPlot extends Chart {
	public SpectrumPlot() {
		//numCharts = 2;
		numCharts = 2;
		title = "New Chart";
		hasKey = true;
		datasets = new Dataset[2];
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
		for (int count = 0; count < numCharts; count++) {
			ChartArea nextChart = new BarChartArea(datasets[count]);
			nextChart.setForegroundColor(DATA_COLORS[count]);
			chartAreas.add(nextChart);
			
			// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
			chartPanel.add(chartAreas.get(count));
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
		//setDataset(0, pos);
		chartAreas.get(0).setDataset(pos);
		//setDataset(1, neg);
		chartAreas.get(1).setDataset(neg);
		
		packData(false, true); //updates the Y axis scale.
	}
	
	public void displayPeaks(Dataset pos, Dataset neg) {
		setTitleY(0, "Area");
		setTitleY(1, "Area");
		//setDataset(0, pos);
		chartAreas.get(0).setDataset(pos);
		//setDataset(1, neg);
		chartAreas.get(1).setDataset(neg);
		
		/*Dataset[] temp = new Dataset[2];
		temp[1] = pos;
		temp[0] = neg;
		chartAreas.get(0).setDatasets(temp);
		*/
		packData(false, true); //updates the Y axis scale.
	}
}
