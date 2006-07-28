package chartlib;
/**
 * Abstracted out from Chart so that ChartAreas themselves could be zoomy.
 * <p>
 * It would probably make sense to change the way charts zoom even further:  an
 * event/listener/source type design pattern might be the way to go.
 * 
 * @author smitht
 */

import java.awt.Point;

public interface Zoomable {
	/**
	 * Given a point in screen coordinates, find out whether it is over a graph,
	 * and could therefore be the start or end of a zoom request.
	 * 
	 * @param p
	 * @return
	 */
	public abstract boolean isInDataArea(Point p);

	/**
	 * Given a point in screen coordinates that is on a chart,
	 * finds what key in chart
	 * coordinates the screen point is at.
	 * 
	 * @param p The point in screen coordinates.
	 * @return A Point2D.Double object containing the key of p
	 * in the chart, converted to chart coordinates.  Returns null if
	 * the point is not within the data value of a chart.
	 */
	public abstract java.awt.geom.Point2D.Double getDataValueForPoint(Point p);

	/**
	 * Sets new boundaries for the axes and displayed data of all charts.
	 * Does not change the tick parameters. To keep a bound at its current
	 * value, use the flag CURRENT_VALUE.
	 * 
	 * @param xmin Minimum of X axis.
	 * @param xmax Maximum of X axis.
	 * @param ymin Minimum of Y axis.
	 * @param ymax Maximum of Y axis.
	 */
	public abstract void setXAxisBounds(double xmin, double xmax) throws IllegalArgumentException;

	/**
	 * Sets all the charts' axis limits to new values that fit the dataset.
	 * If only the Y axis is specified, packs the Y axis to fit the data that is
	 * visible with the current x values.
	 * 
	 * @param packX Whether to pack the x axis.
	 * @param packY Whether to pack the y axis.
	 */
	public abstract void packData(boolean packX, boolean packY);

	public abstract double[] getVisibleXRange();

}