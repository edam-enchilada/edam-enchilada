package chartlib.hist;

import chartlib.AbstractMetricChartArea;

public abstract class UnpaddedTopChartArea extends AbstractMetricChartArea {
	public UnpaddedTopChartArea() {
		TOP_PADDING = 5; // down from 15
	}
}
