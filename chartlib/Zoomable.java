package chartlib;
/**
 * Abstracted out from Chart so that ChartAreas themselves could be zoomy.
 * <p>
 * It would probably make sense to change the way charts zoom even further:  an
 * event/listener/source type design pattern might be the way to go.
 * 
 * @author smitht
 * @author jtbigwoo
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
	 * Given a point that's in the data area (start), and a point that's off
	 * the edge (end) this method will give us a valid x value to use for 
	 * zooming.  We need this so that we can move the pane that shows the
	 * zoom area even when the mouse pointer is not in the data area.  (bug 2525223)
	 * @param start the point that's in the data area
	 * @param end the point that's off the edge
	 * @return the x on the screen for that will keep the zooming pane on the 
	 * data area.  If you supply a start that's not on the data area, we return
	 * -1.
	 */
	public abstract int getDataAreaEdge(Point start, Point end);

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
	 * @param forceY Whether to force the Y axis to end at 0.
	 */
	public abstract void packData(boolean packX, boolean packY, boolean forceY);

	public abstract double[] getVisibleXRange();

}