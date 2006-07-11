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
 * The Original Code is EDAM Enchilada's Dataset class.
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
 */
package chartlib;

import java.util.*;
//import java.awt.Color;

/**
 * @author sulmanj
 * Manages a collection of data for the graph.  Also can provide some display hints.
 * Contains a set of x, y coordinate pairs ordered by x coordinate.
 */
public class Dataset extends TreeSet<DataPoint>
{
	private Statistics cachedStats = null;
	private Dataset lastCorrelatedDataset = null;
	
	/**
	 * Empty dataset.
	 *
	 */
	public Dataset()
	{
		super();
	}
	
	/**
	 * Initializes the dataset with an array of data points.
	 * @param d An array of the data points.
	 */
	public Dataset(DataPoint[] d)
	{
		this();
		for(int count=0; count < d.length; count++)
			add(d[count]);	
	}
	
	/**
	 * Adds a datapoint to the set.
	 * @throws IllegalArgumentException if an Object is added that is not a 
	 * DataPoint, or if the datapoint added contains an Infinite or NaN
	 * x or y coordinate.
	 */
	public boolean add(DataPoint d) throws IllegalArgumentException
	{
		double x = d.x, y = d.y;
		if(Double.isInfinite(x )
				|| Double.isNaN(x)
				|| Double.isInfinite(y)
				|| Double.isNaN(y))
			throw new IllegalArgumentException("Infinite or NaN Datapoint value.");
	
		return super.add(d);
	}

	
	/**
	 * Searches the dataset for an element with a particular x coordinate 
	 * within a specified tolerance.
	 * If no element is found, returns null.
	 * 
	 * @param x The x coordinate to look for.
	 * @return The first element found within the tolerance, or null if none
	 * is found.
	 */
	public DataPoint get(double x, double tolerance)
	{
		Iterator<DataPoint> iterator = iterator();
		while(iterator.hasNext()) {
			DataPoint dp = iterator.next();
			if(Math.abs(dp.x - x) <= tolerance)
				return dp;
		}
		return null;
	}
	
	/**
	 * Finds a data point with the specified x coordinate with a
	 * tolerance of 1.
	 * @param x The x coordinate.
	 * @return The data point found, or null if none is found.
	 */
	public DataPoint get(double x)
	{
		return get(x, 1);
	}

	public Statistics getCorrelationStats(Dataset dataset2) {
		if (lastCorrelatedDataset == dataset2)
			return cachedStats;
		
		double sumxx = 0, sumxy = 0, sumyy = 0, sumx = 0, sumy = 0;
		int numValidPoints = 0;
		
		Iterator<DataPoint> iterator = iterator();
		while(iterator.hasNext())
		{
			DataPoint dpX = iterator.next();
			DataPoint dpY = dataset2.get(dpX.x,.50);
			
			if (dpY != null) {
				numValidPoints++;
				double x = dpX.y, y = dpY.y;
				
				sumx += x;
				sumy += y;
				sumxx += x * x;
				sumxy += x * y;
				sumyy += y * y;
			}
		}

		double Sxx = sumxx - (sumx * sumx / numValidPoints);
		double Sxy = sumxy - (sumx * sumy / numValidPoints);
		double Syy = sumyy - (sumy * sumy / numValidPoints);

		Statistics ret = new Statistics();
		ret.b = Sxy / Sxx;
		ret.a = (sumy - ret.b * sumx) / numValidPoints;
		ret.r2 = (Sxy * Sxy) / (Sxx * Syy);
		
		lastCorrelatedDataset = dataset2;
		cachedStats = ret;
		
		return ret;
	}
	
	public Statistics getCorrelationStats() {
		return getCorrelationStats(this);
	}
	
	public static class Statistics {
		public double a, b, r2;
	}
}
