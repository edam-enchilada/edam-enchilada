package chartlib;

import java.util.ArrayList;

public class SpectrumPlot extends Chart {
	private int datatype;
	private Dataset[] datasets;
	private static final int numCharts = 2;
	public static final int UNDEFINED = -1;
	public static final int PEAK_DATA = 1;
	public static final int SPECTRUM_DATA = 2;
	
	
	public SpectrumPlot() {
		//numCharts = 2;
		title = "New Chart";
		hasKey = false;
		datasets = new Dataset[2];
		datatype = SpectrumPlot.UNDEFINED;
		
		makeChartAreas();
		
		setupLayout();
		
		packData(false, true, true); //updates the Y axis scale.
		
	}
	public void packData(boolean packX, boolean packY, boolean forceY){
		for(AbstractMetricChartArea ca : chartAreas){
		}
		super.packData(packX, packY, forceY);
	}
	
	public SpectrumPlot(Dataset pos, Dataset neg){
		this();
		datasets[0] = pos;
		datasets[1] = neg;
		packData(false, true, true); //updates the Y axis scale.
	}
	
	protected void makeChartAreas(){
		chartAreas = new ArrayList<AbstractMetricChartArea>();
		switch(datatype){
		case SpectrumPlot.UNDEFINED:
			for (int count = 0; count < numCharts; count++) {
				ChartArea nextChart = new BarChartArea(new Dataset());
				nextChart.setForegroundColor(DATA_COLORS[count]);
				chartAreas.add(nextChart);
			}
			break;
		case SpectrumPlot.PEAK_DATA:
			for (int count = 0; count < numCharts; count++) {
				ChartArea nextChart = new BarChartArea(datasets[count]);
				nextChart.setForegroundColor(DATA_COLORS[count]);
				nextChart.setTitleX("m/z");
				nextChart.setTitleY("Peak Area");
				chartAreas.add(nextChart);
			}
			break;
		case SpectrumPlot.SPECTRUM_DATA:
			for (int count = 0; count < numCharts; count++) {
				ChartArea nextChart = new SpectrumLineChartArea(datasets[count], datasets[count + 2]);
				nextChart.setForegroundColor(DATA_COLORS[count]);
				nextChart.setTitleX("m/z");
				nextChart.setTitleY("Intensity");
				chartAreas.add(nextChart);
			}
			break;
		}
		
		chartAreas.get(0).setTitle("Positive Spectrum");
		chartAreas.get(1).setTitle("Negative Spectrum");
	}
	
	public void displaySpectra(Dataset pos, Dataset neg, 
			Dataset posPeaks, Dataset negPeaks) {
		setTitleY(0, "Intensity");
		setTitleY(1, "Intensity");
		datasets = new Dataset[] { pos, neg, posPeaks, negPeaks };
		datatype = SpectrumPlot.SPECTRUM_DATA;
		makeChartAreas();
		setupLayout();
		
	}
	
	public void displayPeaks(Dataset pos, Dataset neg) {
		setTitleY(0, "Area");
		setTitleY(1, "Area");
		
		datasets = new Dataset[2];
		datasets[0] = pos;
		datasets[1] = neg;
		datatype = SpectrumPlot.PEAK_DATA;
		
		makeChartAreas();
		setupLayout();
	}
}
