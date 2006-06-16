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

	public float getDistance(BinnedPeakList other, DistanceMetric metric) {
		Map.Entry<Integer, Float> i = null, j = null;
		Iterator<Map.Entry<Integer, Float>> thisIter = peaks.entrySet().iterator(),
			thatIter = other.peaks.entrySet().iterator();
		
		float distance = 0;
		
		// if one of the peak lists is empty, do something about it.
		if (thisIter.hasNext()) {
			i = thisIter.next();
		}
		if (thatIter.hasNext()) {
			j = thatIter.next();
		}
		// both lists have some particles, so 
		while (i != null && j != null) {
			if (i.getKey().equals(j.getKey()))
			{
				distance += DistanceMetric.getDistance(i.getValue(),
						j.getValue(),
						metric);
				if (thisIter.hasNext())
					i = thisIter.next();
				else i = null;
				
				if (thatIter.hasNext())
					j = thatIter.next();
				else j = null;
				
				
			}
			else if (i.getKey() < j.getKey())
			{
				distance += DistanceMetric.getDistance(0, i.getValue(), metric);
				if (thisIter.hasNext())
					i = thisIter.next();
				else i = null;
			}
			else
			{
				distance += DistanceMetric.getDistance(0, j.getValue(), metric);
				if (thatIter.hasNext())
					j = thatIter.next();
				else j = null;
			}
		}
	
		if (i != null) {
			assert(j == null);
			distance += DistanceMetric.getDistance(0, i.getValue(), metric);
			while (thisIter.hasNext()) {
				distance += DistanceMetric.getDistance(0, 
						thisIter.next().getValue(), metric);
			}
		} else if (j != null) {
			distance += DistanceMetric.getDistance(0, j.getValue(), metric);
			while (thatIter.hasNext()) {
				distance += DistanceMetric.getDistance(0,
						thatIter.next().getValue(), metric);
			}
		}
		
		if (metric == DistanceMetric.DOT_PRODUCT)
		    distance = 1-distance;
		
		return normalizable.roundDistance(this, other, metric, distance);
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
		
		add(locationInt, area);
	}
	
	public void add(int location, float area) {
		if (peaks.containsKey(location))
		{
			peaks.put(location, peaks.get(location) + area);
		} else {
			peaks.put(location, area);
		}
	}
	
	/**
	 * Adds a BinnedPeak, with the same checks as add(float, float).
	 * Equivalent to add(bp.location, bp.area).
	 * @param bp the BinnedPeak to add.
	 */
	public void add(BinnedPeak bp) {
		add(bp.key, bp.value);
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
	
	/** 
	 * Multiply each value by a scalar factor.
	 * @param factor
	 */
	public void multiply(float factor) {
		Iterator<Map.Entry<Integer, Float>> iter = peaks.entrySet().iterator();
		Map.Entry<Integer,Float> temp;
		while (iter.hasNext()) {
			temp = iter.next();
			temp.setValue(temp.getValue() * factor);
		}
	}
	
	public Iterator<BinnedPeak> iterator() {
		return new Iter(this);
	}
	
	/**
	 * Warning!  This does not actually provide you with access to the
	 * underlying map structure, so any changes made to elements accessed by
	 * this iterator will NOT BE REFLECTED in the BPL itself.
	 * 
	 * @author smitht
	 *
	 */
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
