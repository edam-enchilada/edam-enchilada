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
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author sulmanj
 *
 * Contains data necessary for drawing an axis.
 * 
 */
public class GraphAxis {
	private static SimpleDateFormat dateFormat = 
		new SimpleDateFormat("M/dd/yy");
	private static SimpleDateFormat timeFormat = 
		new SimpleDateFormat("HH:mm:ss");

	//the range of the axis
	private double min;
	private double max;
	
	//tick marks
	private double bigTicksFactor; //big ticks will appear on multiples of 
	//this factor
	private int smallTicks; //number of small ticks between each big tick
	
	private double[] bigTicksRel;	//relative locations of the big ticks
	private double[] bigTicksVals;	//numerical values of the big ticks
	private double[] smallTicksRel;	//relative locations of the small ticks
	
	
	//title of this axis	
	private String title;
	
	
	/**
	 * Constructs an empty axis.
	 *
	 */
	public GraphAxis()
	{
		min = 0;
		max = 0;
		bigTicksFactor = 0;
		smallTicks = 0;
		bigTicksRel = null;
		bigTicksVals = null;
		smallTicksRel = null;
		title = "";
	}
	
	
	/**
	 * Constructs a new axis with a specified range
	 * and a default 10 big ticks and 1 small tick between each big.
	 * @param newMin Minimum of range.
	 * @param newMax Maximum of range.
	 * @throws IllegalArgumentException if max is less than min.
	 */
	public GraphAxis(double newMin, double newMax)  throws IllegalArgumentException
	{
		if(newMin > newMax) throw new IllegalArgumentException();
		min = newMin;
		max = newMax;
		
		bigTicksFactor = (max - min)/10;
		
		smallTicks = 1;
		makeTicks();
		title = "";
	}
	
	/**
	 * Constructs a new axis with specified range and tick marks.
	 * @param newMin Minimum of range.
	 * @param newMax Maximum of range.
	 * @param tickFactor Big ticks will be multiples of this factor.
	 * @param smallTicks Number of small ticks between each big tick.
	 * @throws IllegalArgumentException
	 */
	public GraphAxis(double newMin, double newMax, 
			double tickFactor, int smallTicks) 
	throws IllegalArgumentException
	{
		if(newMin > newMax
				|| tickFactor < 0
				|| smallTicks < 0) throw new IllegalArgumentException();
		min = newMin;
		max = newMax;
		bigTicksFactor = tickFactor;
		this.smallTicks = smallTicks;
		makeTicks();
		title = "";
	}
	
	
	public void setTitle(String newTitle)
	{
		title = newTitle;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public double getMin() 
	{
		return min;
	}
	
	public double getMax() 
	{
		return max;
	}
	/**
	 * Sets a new range, keeping tick marks as they are.
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
	 * @param tickFactor Big ticks will be on multiples of this number.
	 * @param smallTicks Number of small ticks between each big tick.
	 */
	public void setTicks(double tickFactor, int smallTicks)
	{
		bigTicksFactor = tickFactor;
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
	private void makeTicks()
	{
		
		//int count = 0;
		
		double range = max - min;
		double relInc = bigTicksFactor / range;	//amount by which the relative
												// location increases
		
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
	 * @return
	 */
	public double[] getBigTicksRel()
	{
			//copies the array to prevent mischief
		double[] ticks = new double[bigTicksRel.length];
		for(int count = 0; count < ticks.length; count++)
			ticks[count] = bigTicksRel[count];
		return ticks;
	}
	
	public double[] getBigTicksVals()
	{
		double[] ticks = new double[bigTicksVals.length];
		for(int count = 0; count < ticks.length; count++)
			ticks[count] = bigTicksVals[count];
		return ticks;
	}
	
	public String[] getBigTicksLabelsTop(boolean drawAsDateTime) {		
		String[] labels = new String[bigTicksVals.length];
		for(int count = 0; count < labels.length; count++)
			labels[count] = 
				drawAsDateTime ? 
						dateFormat.format(new Date((long) this.bigTicksVals[count])) : 
				        Double.toString((double)(Math.round(bigTicksVals[count] * 100)) / 100);
		return labels;
	}
	
	public String[] getBigTicksLabelsBottom() {
		String[] labels = new String[bigTicksVals.length];
		for(int count = 0; count < labels.length; count++)
			labels[count] = timeFormat.format(new Date((long) this.bigTicksVals[count]));
		return labels;
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
}
