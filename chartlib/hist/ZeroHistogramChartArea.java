package chartlib.hist;

import java.awt.Color;
import chartlib.AbstractMetricChartArea;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class ZeroHistogramChartArea extends AbstractMetricChartArea {
	HistogramDataset dataset;

	public ZeroHistogramChartArea(HistogramDataset dataset) {
		TOP_PADDING = 5; // down from 15
		this.dataset = dataset;
		this.setPreferredSize(new Dimension(400, 100));
		
		yAxis.setRange(0, dataset.count);
		yAxis.setTicks(1, 1);
	}

	@Override
	protected void drawData(Graphics2D g2d) {

		System.out.println(getDataAreaBounds());
		int max = dataset.count;
		for (int i = 0; i < dataset.hists.length; i++) {
			if (dataset.hists[i] == null) continue;
			Rectangle2D bar 
				= new Rectangle2D.Double(XAbs(i),
						YAbs(dataset.hists[i].getHitCount()),
						XLen(1), YLen(dataset.hists[i].getHitCount()));
			g2d.draw(bar);
			g2d.fill(bar);
		}
	}

	public HistogramDataset getDataset() {
		return dataset;
	}

	public void setDataset(HistogramDataset dataset) {
		this.dataset = dataset;
	}

}
