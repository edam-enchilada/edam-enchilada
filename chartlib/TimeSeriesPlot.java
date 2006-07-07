package chartlib;

import java.awt.Color;
import java.awt.Dimension;

public class TimeSeriesPlot extends Chart {
	public TimeSeriesPlot(Dataset[] datasets) {
		super(datasets.length, true);
		
		int numSequences = datasets.length;

		this.setHasKey(false);
		this.setTitleX(0, "Time");
		this.drawXAxisAsDateTime(0);
		
		for (int i = 0; i < numSequences; i++) {
			this.setTitleY(i, "Sequence " + (i + 1) + " Value");
			this.setColor(i, i == 0 ? Color.red : Color.blue);
			this.setAxisBounds(i, 0, 1, 0, 1);
			//this.setDataset(i, datasets[i]);
			chartAreas[0].setDataset(i, datasets[i]);
			this.setDataDisplayType((datasets[i].size() == 1), true);
		}

		this.setNumTicks(10, 10, 1, 1);
		this.setBarWidth(3);
		this.setPreferredSize(new Dimension(400, 400));
	}
}
