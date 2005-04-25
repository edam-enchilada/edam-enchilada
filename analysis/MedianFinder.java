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
 * The Original Code is EDAM Enchilada's MedianFinder class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
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
 * Created on Jan 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package analysis;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author andersbe
 *
 * A helper class to find the k smallest peak in a peaklist.
 */
public class MedianFinder {
	private static final int MAX_LOCATION = 2500;
	private static int DOUBLE_MAX = MAX_LOCATION * 2;
	
	private ArrayList<BinnedPeakList> particles;
	private float[][] sortedList;
	private boolean [] locationsUsed = new boolean [DOUBLE_MAX];
	
	public MedianFinder(ArrayList<BinnedPeakList> particles)
	{
		this.particles = particles;
		if (particles.size() != 0)
		{
			sortedList = new float[DOUBLE_MAX][particles.size()];
			fill();
			sort();
		}
		else
			throw new IllegalArgumentException("Input list contains no " +
					"peaklists.");
	}
	
	private void fill()
	{
		BinnedPeakList tempPL;
		for (int i = 0; i < particles.size(); i++)
		{
			tempPL = particles.get(i);
			tempPL.resetPosition();
			for (int j = 0; j < tempPL.length(); j++)
			{
				BinnedPeak peak = tempPL.getNextLocationAndArea();
				sortedList[MAX_LOCATION+peak.location][i] = 
					peak.area;
				locationsUsed[MAX_LOCATION+peak.location] = true;
			}
			tempPL.resetPosition();
		}
	}
	
	private void sort()
	{	
		for (int i = 0; i < DOUBLE_MAX; i++)
		{
			if (locationsUsed[i])
				Arrays.sort(sortedList[i]);
		}
	}
	
	public BinnedPeakList getMedian()
	{
		if (particles.size() == 0)
			return null;
		BinnedPeakList returnThis = new BinnedPeakList();
		if (particles.size()%2 == 0)
		{
			float subMid, supMid;
			for (int i = 0; i < DOUBLE_MAX; i++)
			{
				subMid = sortedList[i][particles.size()/2-1];
				supMid = sortedList[i][particles.size()/2];
				if (subMid == 0.0f &&
					supMid == 0.0f)
					;
				else
					returnThis.addNoChecks(i-MAX_LOCATION, 
							(subMid+supMid)/2.0f);
			}
		
			return returnThis;
		}
		else
		{
			return getKthElement(particles.size()/2);
		}
	}
	
	public BinnedPeakList getKthElement(int k)
	{
		if (particles.size() == 0)
			return null;
		if (k >= particles.size() || k < 0)
			throw new IndexOutOfBoundsException(
					"k must be a number from 0 to " +
					(particles.size()-1));
		BinnedPeakList returnThis = new BinnedPeakList();
		for (int i = 0; i < DOUBLE_MAX; i++)
		{
			if (sortedList[i][k] == 0)
				;
			else
				returnThis.addNoChecks(i-MAX_LOCATION,
						sortedList[i][k]);
		}
		
		return returnThis;
	}
	
	/**
	 * Returns the minimum value that at least percent elements 
	 * contain.
	 * @param percent
	 * @return
	 */
	public BinnedPeakList getPercentElement(float percent)
	{
		if (particles.size() == 0)
			return null;
		if (percent > 1 || percent < 0)
			throw new IndexOutOfBoundsException(
					"percent must be greater than 0 and less" +
					"than or equal to 1.");
		int index = (int) ( 0.5f + (1.0-percent) * (float) (particles.size()-1));
		
		if (index == 0)
			return getKthElement(0);
		else
			return getKthElement(index);
	}
	
	/**
	 * Take the loaded set of spectra and produce a "normalized" median, 
	 * that is a peaklist of magnitude of 1, which, is the closest peaklist 
	 * as possible to all the particles that is still of length 1.
	 * @return a "normalized" median
	 */
	public BinnedPeakList getMedianSumToOne()
	{
		// Get the median and calculate the starting magnitude
		float magnitude = 0.0f;
		BinnedPeakList median = getMedian();
		median.resetPosition();
		for (int i=0; i < median.length(); i++)
		{
			magnitude += median.getNextLocationAndArea().area;
		}
		
		assert(magnitude <= 1.02f) : "Median was larger than 1: " +
			magnitude;
		
		// If the median is not normalized, normalize it already
		if (magnitude < 1.0f)
		{
			// For each location, Find out how many spectra have peaks 
			// bigger than those in the median 
			int [] numEntriesGreaterThanMedian = new int[DOUBLE_MAX];
			int maxIndex = -1;
			int maxNumEntries = 0; 
			float maxAreaDiff = 0;
			float tempArea = 0;
			for (int i = 0; i < sortedList.length; i++)
			{
				int j = sortedList[i].length -1;
				// We have to subtract MAX_LOCATION here since inside
				// peaklists, neg. values are actually negative, but
				// since we can't have negative array indices
				// when we declare arrays, negative values are 
				// positive and positive values are value+MAX_LOCATION
				tempArea = median.getAreaAt(i-MAX_LOCATION);
				while (j >= 0 && sortedList[i][j] - tempArea > 0.0f)
				{
					numEntriesGreaterThanMedian[i]++;
					j--;
					
					assert(j >= 0) :
						"j is less than 0 which shouldn't happen if tempArea " +
						"is really the median.\n" +
						"median.getAreaAt(i-MAX_LOCATION) = " + tempArea + "\n" +
						"sortedList[i][0] = " + sortedList[i][0];
					
				}
				//assert (j != sortedList[i].length - 1) : 
					//"j did not decrease";
				// Find the location where the most peaklists have 
				// values higher than the median
				if (numEntriesGreaterThanMedian[i] > maxNumEntries)
				{
					maxNumEntries = numEntriesGreaterThanMedian[i];
					maxIndex = i;
					maxAreaDiff = sortedList[i]
						   [sortedList[i].length - 
							numEntriesGreaterThanMedian[i]] - tempArea;
				}
			}

			assert (maxNumEntries > 0) : 
				"maxNumEntries remained 0";
			
			// Magnify the median at this location, adjust the count
			// at this location for how many peaklists are bigger
			// than the median and go again until magnitude reaches 1.0f
			while (magnitude < 1.0f && numEntriesGreaterThanMedian.length > 0)
			{
				System.out.println("Magnitude = " + magnitude);
				assert maxAreaDiff > 0.0f : 
					"areadiff to add is negative: " + maxAreaDiff;
				if (maxAreaDiff + magnitude <= 1.0f)
				{
					assert(maxIndex < DOUBLE_MAX) :
						"maxIndex is out of bounds: " + maxIndex;
					median.add(maxIndex-MAX_LOCATION, maxAreaDiff);
					magnitude += maxAreaDiff;
					float currentValue = sortedList[maxIndex]
													[sortedList[maxIndex]
													.length - 
													numEntriesGreaterThanMedian
													[maxIndex]];
					while (numEntriesGreaterThanMedian[maxIndex] > 0
							&& currentValue == 
								sortedList[maxIndex][sortedList[maxIndex].length 
													 - 
													 numEntriesGreaterThanMedian
													 [maxIndex]])
					{
						numEntriesGreaterThanMedian[maxIndex]--;
					}
					//numEntriesGreaterThanMedian[maxIndex] = 0;
					maxIndex = 0;
					maxAreaDiff = 0.0f;
					maxNumEntries = 0;
					for (int i = 0; 
						 i < numEntriesGreaterThanMedian.length; 
						 i++)
					{
						if (numEntriesGreaterThanMedian[i] > maxNumEntries)
						{
							maxNumEntries = numEntriesGreaterThanMedian[i];
							maxIndex = i;
							maxAreaDiff = sortedList[i]
									 [sortedList[i].length - 
										numEntriesGreaterThanMedian[i]]   
								- median.getAreaAt(i-MAX_LOCATION); 
						}
					}
					assert (maxNumEntries > 0) : 
						"maxValue remained 0";
						
				}
				else
				{
					assert (1.0f-magnitude > 0.0f) : 
						"1.0f-magnitude is negative";
					assert(maxIndex < DOUBLE_MAX) : 
						"maxIndex is out of bounds: " + maxIndex;
					median.add(maxIndex-MAX_LOCATION,1.0f-magnitude);
					magnitude += 1.0f-magnitude;
					return median;
				}
				
				//assert(magnitude > tempMag) : 
					//"magnitude has not changed positively: tempMag = " +
					//tempMag + " magnitude = " + magnitude;
			}
			return median;
		}
		else
			return median;
	}
}
