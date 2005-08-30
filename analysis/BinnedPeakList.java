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
 * The Original Code is EDAM Enchilada's sparse BinnedPeakList.
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

package analysis;

import java.util.*;


/*
 * NOTE! NOTE! NOTE!
 * when we generalize clustering and binnedpeaklists and such, we should
 * consider making the positive and negative spectra separate AtomInfoSparse
 * tables in the database, so that we don't have all the icky subtracting
 * and adding MAX_LOCATION and negative indices.
 */

/**
 * @author andersbe
 * @author smitht
 *
 * An implementation of a sparse array, this class is essentially
 * a peak list where every key is an integer value (rounded 
 * appropriately from a float).  Provides methods for adding peaks
 * from a regular peaklist, as well as methods for adding values
 * with no checks.
 */
public class BinnedPeakList implements Iterable<BinnedPeak> {
	protected SortedMap<Integer, Float> peaks;

	protected static final int MAX_LOCATION = 2500;
	protected static int DOUBLE_MAX = MAX_LOCATION * 2;
	protected static float[] longerLists = new float[MAX_LOCATION * 2];
	
	private Normalizable normalizable;

	/**
	 * A constructor for the peaklist, initializes the underlying
	 * ArrayLists to a size of 20.
	 */
	public BinnedPeakList(Normalizable norm)
	{
		peaks = new TreeMap<Integer, Float>();
		normalizable = norm;
	}
	
	/*public BinnedPeakList() {
		peaks = new TreeMap<Integer, Float>();
		normalizable = null;
	}*/
	
	public float getMagnitude(DistanceMetric dMetric)
	{
		float magnitude = 0;

		Iterator<BinnedPeak> i = iterator();
		if (dMetric == DistanceMetric.CITY_BLOCK)
			while (i.hasNext())
			{
				magnitude += i.next().value;
			}
		else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED ||
		         dMetric == DistanceMetric.DOT_PRODUCT)
		{
			float currentArea;
			while (i.hasNext())
			{
				currentArea = i.next().value;
				magnitude += currentArea*currentArea;
			}
			magnitude = (float) Math.sqrt(magnitude);
		}
		return magnitude;
	}
	
	// TODO: Update this to the real thing
	// TODO: change this to take advantage of sorted iteration.
	public float getDistance(BinnedPeakList toList, DistanceMetric dMetric)
	{
//		TODO: Make this more graceful
		
		//This seems to take a 2 seconds longer?
		//Arrays.fill(longerLists, 0.0f);

	    // longerLists keeps track of which peak locations have nonzero areas
		for (int i = 0; i < DOUBLE_MAX; i++)
		{
			longerLists[i] = 0;
		}
		float distance = 0;
		BinnedPeakList longer;
		BinnedPeakList shorter;

		if (length() < toList.length())
		{
			shorter = this;
			longer = toList;
		}
		else
		{
			longer = this;
			shorter = toList;
		}
		
		Iterator<BinnedPeak> longIter = longer.iterator();
		Iterator<BinnedPeak> shortIter = shorter.iterator();
		
		BinnedPeak temp;

		while (longIter.hasNext()) 
		{
			temp = longIter.next();
			longerLists[temp.key + MAX_LOCATION] = temp.value;

			// Assume optimistically that each key is unmatched in the
			// shorter peak list.
			if (dMetric == DistanceMetric.CITY_BLOCK)
			    distance += temp.value;
			else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				distance += temp.value*temp.value;
			else if (dMetric == DistanceMetric.DOT_PRODUCT)
			    ; // If no match in shorter list, contributes nothing
			else {
			    assert false :
			        "Invalid distance metric: " + dMetric;
				distance = -1.0f;
			}
		}	
		float eucTemp = 0;
		while (shortIter.hasNext())
		{
			temp = shortIter.next();
			if (longerLists[temp.key+MAX_LOCATION] != 0)
			{
				if (dMetric == DistanceMetric.CITY_BLOCK)
				{
					distance -= longerLists[temp.key+MAX_LOCATION];
				}
				else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				{
					distance -= longerLists[temp.key+MAX_LOCATION]*
						longerLists[temp.key+MAX_LOCATION];
				}
				else if (dMetric == DistanceMetric.DOT_PRODUCT)
				    ; // Again, nothing to subtract off here
				else {
				    assert false :
				        "Invalid distance metric: " + dMetric;
					distance = -1.0f;
				}
				
				if (dMetric == DistanceMetric.CITY_BLOCK)
					distance += Math.abs(temp.value-longerLists[temp.key+MAX_LOCATION]);
				else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
				{
					eucTemp = temp.value-longerLists[temp.key+MAX_LOCATION];
					distance += eucTemp*eucTemp;
				}
				else if (dMetric == DistanceMetric.DOT_PRODUCT) {
				    distance +=
				        temp.value*longerLists[temp.key+MAX_LOCATION];
				}
				else {
				    assert false :
				        "Invalid distance metric: " + dMetric;
					distance = -1.0f;
				}
				
			}
			else
			{
				if (dMetric == DistanceMetric.CITY_BLOCK)
					distance += temp.value;
				else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED)
					distance += temp.value*temp.value;
				else if (dMetric == DistanceMetric.DOT_PRODUCT)
				    ; // Nothing to add here if new match
				else {
				    assert false :
				        "Invalid distance metric: " + dMetric;
					distance = -1.0f;
				}
			}
			
		}
		
		// Dot product distance actually ranges from 0 to 1 (since data is
		// normalized). A value of 1 indicates two points are the same, 0
		// indicates completely different. In order to make rest of code work
		// (small distance is considered good), negate distance and 1 to it.
		// This places distance between 0 and 1 like other measures and doesn't
		// affect anything else. (Admittedly, this is a hack, but dot product
		// distance is ultimately the same thing as Euclidean squared anyway).
		if (dMetric == DistanceMetric.DOT_PRODUCT)
		    distance = 1-distance;
		
		return normalizable.roundDistance(this, toList, dMetric, distance);
	}
	
	/**
	 * Retrieve the value of the peaklist at a given key
	 * @param key	The key of the value you wish to
	 * 					retrieve.
	 * @return			The value at the given key.
	 */
	public float getAreaAt(int location)
	{
		Float area = peaks.get(location);
		if (area == null) {
			return 0;
		} else {
			return area;
		}
	}
	
	/**
	 * Add a regular peak to the peaklist.  This actually involves
	 * quite a bit of processing.  First, each float key is
	 * rounded to its nearest integer value.  Then, that key
	 * is checked in the current peak to see if it already exists.
	 * If it does, it adds the value of the new peak to the 
	 * preexisting value.  This is done so that when you have two
	 * peaks right next to eachother (ie 1.9999 and 2.0001) that
	 * probably should be both considered the same element, the
	 * signal is doubled.  
	 * 
	 * @param key
	 * @param value
	 */
	public void add(float location, float area)
	{
		assert(location < MAX_LOCATION && location > - MAX_LOCATION) :
			"Location to add is out of bounds" + location;
		int locationInt;
		
		// If the key is positive or zero, then add 0.5 to round.
		// Otherwise, subtract 0.5 to round.
		if (location >= 0.0f)
			locationInt = (int) ((float) location + 0.5);
		else
			locationInt = (int) ((float) location - 0.5);
		
		if (peaks.containsKey(locationInt))
		{
			peaks.put(locationInt, peaks.get(locationInt) + area);
		} else {
			peaks.put(locationInt, area);
		}
	}
	
	/**
	 * Adds a BinnedPeak, with the same checks as add(float, float).
	 * Equivalent to add(bp.location, bp.area).
	 * @param bp the BinnedPeak to add.
	 */
	public void add(BinnedPeak bp) {
		add((float) bp.key, bp.value);
	}
	
	/**
	 * Returns the number of locations represented by this 
	 * Binned peaklist
	 * @return the number of locations in the list
	 */
	public int length()
	{
		return peaks.size();
	}
	
	/**
	 * This skips all the checks of add().  Do not use this unless
	 * you are copying from another list: not taking care to make
	 * sure that you are not adding duplicate locations can result
	 * in undesired behavior!!!!
	 * @param key	The key of the peak
	 * @param value	The value of the peak at that key.
	 */
	public void addNoChecks(int location, float area)
	{
		assert(location < MAX_LOCATION && location > - MAX_LOCATION) : 
			"key is out of bounds: " + location;
		//peaks.add(new BinnedPeak(key,value));
		peaks.put(location, area);
	}
	
	public void divideAreasBy(int divisor) {
		// TODO: Map.Entry.setValue()
		Map.Entry<Integer,Float> e;
		Iterator<Map.Entry<Integer,Float>> i = peaks.entrySet().iterator();
		
		while (i.hasNext()) {
			e = i.next();
			e.setValue(e.getValue() / divisor);
		}
	}
		
	public void printPeakList() {
		System.out.println("printing peak list");
		Iterator<BinnedPeak> i = iterator();
		BinnedPeak p;
		while (i.hasNext()) {
			p = i.next();
			System.out.println(p.key + ", " + p.value);
		}
	}
	
	public int getLastLocation() {
		return peaks.lastKey();
	}
	
	public int getFirstLocation() {
		return peaks.firstKey();
	}
	
	public float getLargestArea() {
		return Collections.max(peaks.values());
	}
	
	public void addAnotherParticle(BinnedPeakList other) {
		Iterator<BinnedPeak> i = other.iterator();
		while (i.hasNext()) {
			add(i.next());
		}
	}
	
	/**
	 * A method to normalize this BinnedPeakList.  Depending 
	 * on which distance metric is
	 * used, this method will adapt to produce a distance of one 
	 * from <0,0,0,....,0> to the vector represented by the list.
	 * @param 	dMetric the distance metric to use to measure length
	 */
	public void normalize(DistanceMetric dMetric) {
		normalizable.normalize(this,dMetric);
	}
	
	public void setNormalizer(Normalizer norm) {
		normalizable = norm;
	}
	
	// used for testing BIRCH
	public boolean testForMax(int max) {
		Iterator<BinnedPeak> iterator = iterator();
		BinnedPeak peak;
		while (iterator.hasNext()) {
			peak = iterator.next();
			if (peak.value > max)
				return false;
		}
		return true;
	}
	
	public Iterator<BinnedPeak> iterator() {
		return new Iter(this);
	}
	
	public class Iter implements Iterator<BinnedPeak> {
		private Iterator<Map.Entry<Integer,Float>> entries;
		
		public Iter(BinnedPeakList bpl) {
			this.entries = bpl.peaks.entrySet().iterator();
		}

		public boolean hasNext() {
			return entries.hasNext();
		}

		public BinnedPeak next() {
			Map.Entry<Integer,Float> e = entries.next();
			return new BinnedPeak(e.getKey(), e.getValue());
		}

		public void remove() {
			throw new Error("Not implemented!");
		}
	}
}
