package chartlib;

import java.awt.Dimension;

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
		super(2, true);
		this.setHasKey(false);
		this.setTitleY(0, "Sequence 1 Value");
		this.setTitleY(1, "Sequence 2 Value");
		this.setAxisBounds(0, 0, 1, 0, 1);
		this.setAxisBounds(1, 0, 1, 0, 1);
		this.setDataset(0, ds1);
		this.setDataset(1, ds2);
		this.drawAsScatterPlot();
		this.setPreferredSize(new Dimension(400, 400));
		this.setBorder(new EmptyBorder(15, 0, 0, 0));
	}
	
	public void setTitle(String title) {
		Dataset.Statistics stats = getDataset(0).getCorrelationStats(getDataset(1));
		super.setTitle(String.format(title, stats.r2));
	}
}
