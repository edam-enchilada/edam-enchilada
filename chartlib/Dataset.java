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
 * Contains a set of x, y coordinate pairs.
 */
public class Dataset extends TreeSet<DataPoint>
{
	
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
		try
		{
			double x = d.x, y = d.y;
			if(Double.isInfinite(x )
					|| Double.isNaN(x)
					|| Double.isInfinite(y)
					|| Double.isNaN(y))
				throw new IllegalArgumentException("Infinite or NaN Datapoint value.");
			
		}
		catch(IllegalArgumentException e)
		{
			throw e;
		}
		catch(ClassCastException c)
		{
			throw new IllegalArgumentException("Non-Datapoint object added to Dataset.");
		}
		
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
		DataPoint dp;
		for(int count=0; count < size(); count++)
		{
			dp = (DataPoint)get(count);
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

}
