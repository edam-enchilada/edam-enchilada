/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's GraphAxis class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Jonathan Sulman sulmanj@carleton.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

/*
 * Created on Feb 2, 2005
 *
 * 
 */
package chartlib;
import java.util.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;

/**
 * Contains data necessary for drawing an axis, and the ability to draw one.
 * 
 * An important thing to understand is that graph axes ALSO hold information
 * on how to draw data points---the relativePosition(double) method.
 * 
 * @author sulmanj
 * @author smitht
 *
 */
public class GraphAxis {
	private class Coord extends Point2D.Double {
		public Coord(double x, double y) {
			super(x, y);
		}
		public String toString() {
			return "Special "+super.toString();
		}
	}
	
	/**
	 * Orientation - say whether the axis is horizontal or vertical.
	 * 
	 * @author smitht
	 */
	public enum Orientation {
		HORIZONTAL, VERTICAL
	}
	
	// whether this axis is horizontal or vertical.
	private Orientation orientation;
	
//	private static SimpleDateFormat dateFormat = 
//		new SimpleDateFormat("M/dd/yy");
//	private static SimpleDateFormat timeFormat = 
//		new SimpleDateFormat("HH:mm:ss");

	//the range of the axis
	private double min;
	private double max;
	
	// the position in the graphics area of the axis
	private Line2D position;
	
	//tick marks
	//	big ticks will appear on multiples of this factor	
	private double bigTicksFactor;
	
	//number of small ticks between each big tick
	private int smallTicks;
	
	// these three arrays are generated from the above variables by the
	// makeTicks method.
	private double[] bigTicksRel;	//relative locations of the big ticks
	private double[] bigTicksVals;	//numerical values of the big ticks
	private double[] smallTicksRel;	//relative locations of the small ticks
	
	// how big the ticks are on the screen, in pixels I assume
	private static final int BIG_TICK_LENGTH = 10;
	private static final int SMALL_TICK_LENGTH = 5;
	
	/**
	 * Though labelling the ticks is not implemented yet, this will be how
	 * you can control the labels of the tick marks.
	 * 
	 * @author smitht
	 *
	 */
	public interface AxisLabeller {
		public String label(double value);
	}
	
	/**
	 * This is the AxisLabeler that the axis will use to draw labels for its
	 * big tick marks.
	 * <p>
	 * The default one is to draw the value at the tick mark, rounded to the 
	 * 100ths place.
	 */
	protected AxisLabeller labeller = new AxisLabeller() {
		// a default labeller, which reports the value rounded to the 100s place.
		public String label(double value) {
			return  Double.toString((double)(Math.round(value * 100)) / 100); 
		}
	};
	
	/**
	 * Create a new graph axis on the line in this position.
	 */
	public GraphAxis(Line2D position) {
		setPosition(position);
		min = 0;
		max = 1;
		bigTicksFactor = (max - min) / 10;
		smallTicks = 1;
		makeTicks();
		orientation = findOrientation();
	}
	
	/**
	 * Figures out whether this axis is vertical or horizontal, using the line.
	 */
	private Orientation findOrientation() {
		if (position.getX1() == position.getX2()) {
			return Orientation.VERTICAL;
		} else {
			if (position.getY1() != position.getY2()) {
				throw new RuntimeException("Axes should be axial!");
			}
			return Orientation.HORIZONTAL;
		}
	}
	
	/**
	 * Call this to draw the axis.  Actually, GenericChartArea will do it for 
	 * you, but you could if you wanted.
	 * <p>
	 * The Graphics2D object you send in must be in the same coordinate space
	 * as the GraphAxis is in, otherwise it will get drawn in a weird space.
	 * This is taken care of for you by GenericChartArea in the normal
	 * case.
	 * 
	 * @param g2d a Graphics2D object with coordinates that work.
	 */
	public void draw(Graphics2D g2d) {
		g2d.setPaint(Color.BLACK);
		g2d.setStroke(new BasicStroke(2));
		g2d.draw(position);
		drawTicks(g2d);
	}
	
	private Map<Coord, GlyphVector> getLabelsForDrawing
		(double[] ticks, String[] labels, Graphics2D g2d) 
	{
		Map<Coord, GlyphVector> drawable 
			= new HashMap<Coord, GlyphVector>();
		GlyphVector thisVec;
		Coord thisPoint;
		Font f = g2d.getFont();
		Map.Entry<Coord, GlyphVector> lastDrawn = null;
		
		for (int tickNum = 0; tickNum < ticks.length; tickNum++) {
			thisVec =  f.createGlyphVector(
					g2d.getFontRenderContext(), labels[tickNum]);
			thisPoint = getLabelOrigin(thisVec, ticks[tickNum]);
			
			if (lastDrawn == null 
				|| !boundsAt(lastDrawn).intersects(boundsAt(thisPoint, thisVec)))
			{
				try {
					drawable.put(thisPoint, thisVec);
				} catch (ClassCastException e) {
					System.out.println("A " +Coord.class 
							+" is not a " +e.getMessage());
				}
			}
		}
		return drawable;
	}
	
	private static Rectangle boundsAt(Coord point, GlyphVector vec) {
		return vec.getOutline((float) point.getX(), (float) point.getY())
		.getBounds();
	}
	
	private static Rectangle boundsAt(Map.Entry<Coord, GlyphVector> entry) {
		return boundsAt(entry.getKey(), entry.getValue());
	}
	
	private Coord getLabelOrigin(GlyphVector gVec, double tickRel) {
		double length = position.getP1().distance(position.getP2());
		Rectangle bounds = gVec.getOutline().getBounds();
		if (orientation == Orientation.VERTICAL) {
			return new Coord(
				position.getX1() - 3 - bounds.width,
				position.getY2() - length * tickRel + (bounds.height / 2));
		} else {
			return new Coord(
				(float) (position.getX1() + length * tickRel - (bounds.width / 2)),
				(float) (position.getY1() + bounds.height + 3));
		}
	}

	/**
	 * Draws all of the ticks, big and small, using drawTick().
	 */
	private void drawTicks(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(1));
		
		// gets big ticks as proportions of the axis length
		double[] bigTicks = getBigTicksRel();
		
		
		
		if(bigTicks.length == 0)
			return;

		int count=0;
		double tickValue = bigTicks[0];
		
		// Draw big ticks
		while(tickValue >= 0 && tickValue <= 1 && count < bigTicks.length)
		{
			tickValue = bigTicks[count];
			if(tickValue >= 0 && tickValue <= 1)
				drawTick(g2d,tickValue, true);
			count++;
		}
		
		// draw tick labels
		String[] bigTicksLabels = getBigTicksLabels();
		Map<Coord, GlyphVector> drawableLabels 
				= getLabelsForDrawing(bigTicks, bigTicksLabels, g2d);
		for (Map.Entry<Coord, GlyphVector> label 
				: drawableLabels.entrySet()) 
		{
			Coord p = label.getKey();
			g2d.drawGlyphVector(label.getValue(), (float) p.getX(), (float) p.getY());
		}
		
		
		// x axis small ticks
		double[] smallTicks = getSmallTicks();
		count = 0;
		tickValue = smallTicks[0];
		
		while(tickValue >= 0 && tickValue <= 1 && count < smallTicks.length)
		{
			tickValue = smallTicks[count];
			if(tickValue >= 0 && tickValue <= 1)
				drawTick(g2d,tickValue, false);
			count++;
			
		}
	}

	/**
	 * Draws a tick on a graph axis.
	 *
	 * @param g2d The graphics context for the graph.
	 * @param relPos The relative position of the tick on the axis as a double
	 * between 0 and 1.
	 * @param big True to draw big tick, false to draw small tick.
	 */
	private void drawTick(Graphics2D g2d, double relPos, boolean big) {
		int tickSize;
		if(big)
			tickSize = BIG_TICK_LENGTH;
		else
			tickSize = SMALL_TICK_LENGTH;
		
		//for x axis
		if(orientation == Orientation.HORIZONTAL)
		{
			double left = Math.min(position.getX1(), position.getX2());
			double length = position.getP1().distance(position.getP2());
			//converts from relative position to screen coordinates
			double xCoord = left + relPos * length;
	
			g2d.draw(new Line2D.Double(
					xCoord,
					position.getY1(),
					xCoord,
					position.getY1() - tickSize
			));
		}
		
		//for y axis
		else
		{
			double xCoord = position.getX1();
			double axisLen = position.getP1().distance(position.getP2());
			double bottom = Math.max(position.getY1(), position.getY2());
			double yCoord = bottom - relPos * axisLen;
			g2d.draw(new Line2D.Double(
					xCoord,
					yCoord,
					xCoord + tickSize,
					yCoord));
		}
	}

	/**
	 * The minimum value represented on the axis.
	 */
	public double getMin() 
	{
		return min;
	}
	
	/**
	 * The maximum value represented on the axis.
	 */
	public double getMax() 
	{
		return max;
	}
	
	/**
	 * Sets a new range, keeping tick marks at the same numeric intervals.
	 * 
	 * @param newMin
	 * @param newMax
	 * @throws IllegalArgumentException
	 */
	public void setRange(double newMin, double newMax)
	throws IllegalArgumentException
	{
		if(newMin > newMax) throw new IllegalArgumentException();
		min = newMin;
		max = newMax;
		makeTicks();
	}
	
	/**
	 * Sets new Tick parameters.
	 * 
	 * @param tickFactor Big ticks will be on multiples of this number.
	 * @param smallTicks Number of small ticks between each big tick.
	 */
	public void setTicks(int bigTicks, int smallTicks)
	{
		System.out.println("setting ticks for axis " + this.orientation);
		bigTicksFactor = (max - min) / bigTicks;
		this.smallTicks = smallTicks;
		makeTicks();
	}
	
	
	private static boolean isDivisible(double a, double b)
	{
		return Math.abs(new Double((a / b)).intValue()
				- a/b) < .01;
	}
	
	
	
	/**
	 * Calculates the number of big ticks on the axis.
	 * 
	 * @return the number of big ticks on the axis.
	 */
	public int numTicks()
	{
		int numTicks = (int)Math.round((max - min)/bigTicksFactor);
		
		//corrects off by one errors
		if(isDivisible(min , bigTicksFactor)
				|| isDivisible(max, bigTicksFactor))
		{
			numTicks++;
		}
		return numTicks;
	}
	
	/**
	 * Fills the three tick arrays with appropriate values.
	 *
	 * @return
	 */
	public void makeTicks()
	{
		
		//int count = 0;
		
		double range = max - min;
		double relInc = bigTicksFactor / range;	//amount by which the relative
												// key increases
		
		//error check
		if(range == 0 || bigTicksFactor == 0)
		{
			System.err.println("chartlib: Bad range or tick factor values." +
					"  Ticks not initialized.");
			return;
		}
		
		ArrayList<Double> bigRel = new ArrayList<Double>(numTicks() + 3);
		ArrayList<Double> bigVal = new ArrayList<Double>(numTicks() + 3);
		
		//this is the lowest multiple of bigTicksFactor greater than min
			//actual numerical values
		double valTickValue = Math.ceil(min/bigTicksFactor) * bigTicksFactor;
			//relative values
		double relTickValue = (valTickValue - min)/range;
		
		/*
		System.out.println("Max: " + max + " Min: " + min);
		System.out.println("Range:" + range);
		System.out.println("RelTickValue: " + relTickValue);
		*/
		//fills tick arrays
		while(relTickValue <= 1)
		{
			
			bigRel.add(new Double(relTickValue));
			relTickValue += relInc;
			
			bigVal.add(new Double(valTickValue));
			valTickValue += bigTicksFactor;
			
		}
	
		
		//this ugly code block converts the mutable ArrayList
		//into a happy array of doubles.
		bigTicksRel = new double[bigRel.size()];
		bigTicksVals = new double[bigRel.size()];
		for(int count = 0; count < bigTicksRel.length; count++)
		{
			bigTicksRel[count] = ((Double)(bigRel).get(count)).doubleValue();
			bigTicksVals[count] = ((Double)(bigVal).get(count)).doubleValue();
		}
		
		//System.out.println("Number of ticks: " + bigTicksRel.length);
		
			//gets relative locations of small ticks
		int smallTicks = this.smallTicks + 1;
		ArrayList<Double> smallTicksRel = new ArrayList<Double>(bigTicksRel.length * smallTicks);
		double smallInc = bigTicksFactor / (smallTicks * range);
		
		//value of lowest tick
		double smallTickValue = (Math.ceil(min/(bigTicksFactor / smallTicks)) * (bigTicksFactor/smallTicks) - min) / range;
		
		//fills values
		while(smallTickValue <= 1)
		{
			smallTicksRel.add(new Double(smallTickValue));
			smallTickValue += smallInc;
		}
		
		this.smallTicksRel = new double[smallTicksRel.size()];
		for(int count = 0; count < this.smallTicksRel.length; count++)
		{
			this.smallTicksRel[count] = ((Double)(smallTicksRel).get(count)).doubleValue();
		}

	}
	
	/**
	 * Returns an array containing the relative locations of the big ticks.
	 */
	public double[] getBigTicksRel()
	{
			//copies the array to prevent mischief
		double[] ticks = new double[bigTicksRel.length];
		for(int count = 0; count < ticks.length; count++)
			ticks[count] = bigTicksRel[count];
		return ticks;
	}
	
	/**
	 * Returns an array containing the locations of the big ticks, in data
	 * numbers.
	 */
	public double[] getBigTicksVals()
	{
		double[] ticks = new double[bigTicksVals.length];
		for(int count = 0; count < ticks.length; count++)
			ticks[count] = bigTicksVals[count];
		return ticks;
	}
	
	/**
	 * Not working yet<p>
	 * deprecated cuz that's a reasonable way to remind me to fix it.  sort of.
	 * @param drawAsDateTime
	 * @return
	 */
	public String[] getBigTicksLabels() {		
		String[] labels = new String[bigTicksVals.length];
		for(int count = 0; count < labels.length; count++)
			labels[count] = getLabelFor(bigTicksVals[count]);
		return labels;
	}
	
	protected String getLabelFor(double value) {
		return labeller.label(value);
	}

	/**
	 * Returns an array of doubles representing the relative locations of all small ticks
	 * as numbers between zero and one.
	 * Note: This implementation does not eliminate small ticks that are in the same locations as big ticks.
	 *
	 * @return An array representing the small ticks.
	 */
	public double[] getSmallTicks()
	{
	    double[] ticks = new double[smallTicksRel.length];
	    for(int count = 0; count < ticks.length; count++)
			ticks[count] = smallTicksRel[count];
		return ticks;
	}
	
	
	/**
	 * Returns the relative width of each big tick.
	 * @return The relative width of each big tick.
	 */
	public double getTickWidth()
	{
		return bigTicksFactor / (max - min);
	}
	
	
	/**
	 * Returns the position of the point x relative to the axis
	 *  as a double between 0 and 1.
	 *  A point outside the range of the axis will return less than zero
	 *  or greater than one.
	 * @param x Actual value of the point.
	 * @return Position of the value relative to the axis.
	 */
	public double relativePosition(double x)
	{
		return (x - min) / (max - min);
	}

	public Line2D getPosition() {
		return position;
	}

	/**
	 * Set the axis to a new place in screen space.  This gets called fairly
	 * often cuz of screen resizing and stuff.
	 */
	public void setPosition(Line2D position) {
		double x1, y1, x2, y2;
		if (position.getX1() < position.getX2()) {
			x1 = position.getX1();
			x2 = position.getX2();
		} else {
			x1 = position.getX2();
			x2 = position.getX1();
		}
		if (position.getY1() < position.getY2()) {
			y1 = position.getY1();
			y2 = position.getY2();
		} else {
			y1 = position.getY2();
			y2 = position.getY1();
		}
		
		
		this.position = new Line2D.Double(x1, y1, x2, y2);
		assert(orientation == null || orientation == findOrientation());
		//don't want to change from horizontal to vertical, do we?
		orientation = findOrientation();
	}

	public AxisLabeller getLabeller() {
		return labeller;
	}

	public void setLabeller(AxisLabeller labeller) {
		this.labeller = labeller;
	}

	public void setMax(double max) {
		this.max = max;
		makeTicks();
	}

	public void setMin(double min) {
		this.min = min;
		makeTicks();
	}
	

}
