package chartlib.hist;

import java.awt.*;

import chartlib.*;

/**
 * This ChartArea has no decorations on the X axis and no padding between that
 * axis and the bottom of the chartarea.
 * 
 * @author smitht
 *
 */
public abstract class UndecoratedXChartArea extends AbstractMetricChartArea {
	
	public UndecoratedXChartArea() {
		H_AXIS_PADDING = 5;
		H_TITLE_PADDING = 0;
		
		setTitleX("");
	}
	
	@Override
	protected void createAxes() {
		super.createAxes();
		xAxis.setLabeller(new chartlib.GraphAxis.AxisLabeller() {
			public String[] label(double value) {
				return new String[] {""};
			}
		});
	}

	@Override
	protected void drawAxisTitles(Graphics2D g2d) {
		super.drawAxisTitles(g2d);
	}
}
