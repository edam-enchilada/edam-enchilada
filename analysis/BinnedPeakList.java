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
import java.util.Map.Entry;

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
	
	public BinnedPeakList() {
		peaks = new TreeMap<Integer, Float>();
		normalizable = new Normalizer();
	}
	
	public Normalizable getNormalizable(){
		return normalizable;
	}
	/**
	 * Creates a copy of a binnedPeakList
	 * @param original
	 */
	public void copyBinnedPeakList(BinnedPeakList original) {
		Iterator<BinnedPeak> i = original.iterator();
		BinnedPeak p;
		while (i.hasNext()) {
			p = i.next();
			add(p.key, p.value);
		}
		normalizable = original.getNormalizable();
	}
	
	/**
	 * Return the magnitude of this peaklist, according to the supplied
	 * distance metric (it varies according to measurement).
	 */
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
	//	if(magnitude>1.0)
	//		System.out.println("BAD MAGNITUDE " + magnitude);
		return magnitude;
	}
	
	/**
	 * Gets the magnitude of either the negative or non-negative peaks only.
	 * @param dMetric		distance metric to use
	 * @param negative		true if only negative peaks desired, false for non-neg
	 * @return	the magnitude
	 */
	public float getPartialMag(DistanceMetric dMetric, boolean negative){
		float magnitude = 0;
		Iterator<BinnedPeak> iter = posNegIterator(negative);
		if (dMetric == DistanceMetric.CITY_BLOCK){
			while (iter.hasNext())
				magnitude += iter.next().value;
		} else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED ||
					dMetric == DistanceMetric.DOT_PRODUCT){
			float currentArea;
			while (iter.hasNext()){
				currentArea = iter.next().value;
				magnitude += currentArea*currentArea;
			}
			magnitude = (float) Math.sqrt(magnitude);
		}
		
		return magnitude;
	}

	/**
	 * Find the distance between this peaklist and another one.
	 * @param other The peaklist to compare to
	 * @param metric The distance metric to use
	 * @return the distance between the lists.
	 */
	public float getDistance(BinnedPeakList other, DistanceMetric metric) {

		/*
		 * The following distance calculation algorithm is very similar to the
		 * merge part of merge sort, where you riffle through both lists looking
		 * for the lowest entry, and choosing that one.  The extra condition
		 * is when we have an entry for the same dimension in each list,
		 * in which case we don't choose one but calculate the distance between
		 * them. 
		 */
		
		
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
		// dot product actually comes up with similarity, rather than distance,
		// so we take 1- it to find "distance".
		
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
		int locationInt;
		
		// If the key is positive or zero, then add 0.5 to round.
		// Otherwise, subtract 0.5 to round.
		if (location >= 0.0f)
			locationInt = (int) ((float) location + 0.5);
		else
			locationInt = (int) ((float) location - 0.5);
		
		add(locationInt, area);
	}
	
	/**
	 * This is just like add(float, float) except that it is assumed that
	 * rounding the peaks to the right location has been done already.
	 * @param location
	 * @param area
	 */
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
	 * Equivalent to add(bp.key, bp.value).
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
	 * you are copying from another list: if you add a peak where
	 * another one is already, the first one will be lost.
	 * @param key	The key of the peak
	 * @param value	The value of the peak at that key.
	 */
	public void addNoChecks(int location, float area)
	{
		peaks.put(location, area);
	}
	
	/**
	 * Divide the value at each peak by this amount.
	 * @param divisor
	 */
	public void divideAreasBy(int divisor) {
		Map.Entry<Integer,Float> e;
		Iterator<Map.Entry<Integer,Float>> i = peaks.entrySet().iterator();
		
		while (i.hasNext()) {
			e = i.next();
			e.setValue(e.getValue() / divisor);
		}
	}
	
	/**
	 * Print a representation of this peak list.
	 */
	public void printPeakList() {
		System.out.println("printing peak list");
		Iterator<BinnedPeak> i = iterator();
		BinnedPeak p;
		while (i.hasNext()) {
			p = i.next();
			System.out.println(p.key + ", " + p.value);
		}
	}
	
	/**
	 * Find the highest key in the mapping.
	 * @return the highest key!!
	 */
	public int getLastLocation() {
		return peaks.lastKey();
	}
	
	/**
	 * Finds the lowest key in the mapping.
	 */
	public int getFirstLocation() {
		return peaks.firstKey();
	}
	
	/**
	 * Find the largest value contained in the peaklist.  (not its index, the
	 * value itself).
	 */
	public float getLargestArea() {
		return Collections.max(peaks.values());
	}
	
	/**
	 * Find the sum of two particles.
	 * @param other the particle to add to this one.
	 */
	public void addAnotherParticle(BinnedPeakList other) {
		Iterator<BinnedPeak> i = other.iterator();
		while (i.hasNext()) {
			add(i.next());
		}
	}
	
	
	/**
	 * A method to normalize this BinnedPeakList.  Depending 
	 * on which distance metric is
	 * used, this method will change the peaklist so that the distance 
	 * from <0,0,0,....,0> to the vector represented by the list is equal to 1.
	 * @param 	dMetric the distance metric to use to measure length
	 */
	public void normalize(DistanceMetric dMetric) {
		normalizable.normalize(this,dMetric);
	}
	
	/**
	 * A method to reduce the peak area by the power passed in (preprocessing
	 * to be used before clustering).
	 * @param power	The power to which to raise the area of the peaks.  (.5 is good.)
	 */
	public void preProcess(double power){
		normalizable.reducePeaks(this, power);
	}
	
	/**
	 * Change the Normalizer that will be used during calls to normalize() on
	 * this peaklist.
	 * @param norm the new normalizer.
	 */
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
	
	/**
	 * Return an iterator view of the binned peak list.  Note that modifying
	 * the elements accessed by the iterator will NOT modify the peaklist itself.
	 */
	public Iterator<BinnedPeak> iterator() {
		return new Iter(this);
	}
	
	/**
	 * Return a positive/negative iterator for the binned peak list.
	 * @param negative - true for negative peaks only, false for non-neg. only
	 * @return an iterator that only goes through either negative or non-neg. peaks
	 */
	public Iterator<BinnedPeak> posNegIterator(boolean negative){
		return new PosNegIter(this, negative);
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
		
		/*
		 * Copy the set of peaks into a list, and get an iterator on the list.
		 * This happens to be sorted, yay.
		 */
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
	
	/**
	 * Returns either entries with negative or non-negative keys (locations)
	 * depending on the requested spectrum.
	 * @author steinbel
	 */
	public class PosNegIter implements Iterator<BinnedPeak>{
		private Iterator<Map.Entry<Integer,Float>> entries;
		private Entry<Integer, Float> bp;
		private boolean negative;
		
		/**
		 * Creates an iterator over either negative or non-negative peaks.
		 * @param bpl	The list of peaks to iterate over
		 * @param negative	True for negative peaks, false for non-neg.
		 */
		public PosNegIter(BinnedPeakList bpl, boolean negative){
			this.entries = bpl.peaks.entrySet().iterator();
			this.negative = negative;
			this.bp = null;
		}

		/**
		 * Returns true if there is at least one peak left with the desired 
		 * sign of its location (neg or non-neg).
		 * (Note, advances iterator to the position of the next appropriate peak.)
		 */
		public boolean hasNext() {
			
			boolean remaining = false;
			int position;
			
			while (entries.hasNext()){
				bp = entries.next();
				position = bp.getKey();
				if (negative == false && position >= 0){
					remaining = true;
					break;
				} else if (negative == true && position < 0){
					remaining = true;
					break;
				}
			}
			return remaining;
		}

		/**
		 * Return the next appropriate entry.
		 * @return	the next appropriate peak
		 */
		public BinnedPeak next() {
			//if the current entry is null, get the next appropriate entry
			if (bp == null){
				if (!hasNext())
					throw new NoSuchElementException();
			}
			BinnedPeak next = new BinnedPeak(bp.getKey(), bp.getValue());
			return next;
		}

		//Doesn't make sense here, but needed to implement Iterator.
		public void remove() {
			throw new Error("Not implemented.");
			
		}
		
	}
}
