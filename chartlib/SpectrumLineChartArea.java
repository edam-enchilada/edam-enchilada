package chartlib;

import java.awt.Point;

/**
 * This is a LineChartArea that also keeps track of detected peaks.
 * @author smitht
 *
 */

public class SpectrumLineChartArea extends LineChartArea implements LocatablePeaks {
	Dataset peaks;
	
	public SpectrumLineChartArea(Dataset dataset, Dataset peaks) {
		super(dataset);
		this.peaks = peaks;
	}

	/* (non-Javadoc)
	 * @see chartlib.LocatablePeaks#getBarAt(java.awt.Point, int)
	 */
	public Double getBarAt(Point p, int buf)
	{
		return selectBar(p, buf, peaks);		
	}
}
