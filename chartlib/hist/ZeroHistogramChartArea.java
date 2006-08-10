package chartlib.hist;
/**
 * Somewhat badly named, currently this chartarea displays the number of NONzero
 * peaks for each m/z value.
 * 
 * @author smitht
 */
import chartlib.AbstractMetricChartArea;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class ZeroHistogramChartArea extends AbstractMetricChartArea {
	HistogramDataset dataset;

	public ZeroHistogramChartArea(HistogramDataset dataset) {
		TOP_PADDING = 5; // down from 15
		this.dataset = dataset;
		this.setMinimumSize(new Dimension(400, 75));
		this.setPreferredSize(new Dimension(400, 75));
		
		yAxis.setRange(0, dataset.count);
		yAxis.setTicks(1, 1);
		xAxis.setThickness(0.5f);
		yAxis.setThickness(0.5f);
	}

	@Override
	protected void drawData(Graphics2D g2d) {
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
	
	@Override
	protected void drawTitle(Graphics2D g2d) {
		// do nothing, since there's no room for a title (we're right against
		// the x-axis of the graph above.)
	}
	
	public void removeDataset(HistogramDataset dataset) {
		System.out.println("not yet implemented: remove dataset from "
				+this.getClass());
	}
	
}
