package analysis;

import java.util.ArrayList;

import junit.framework.TestCase;
import static analysis.DistanceMetric.*;

/**
 * 
 * @author smitht
 *
 */
public class BinnedPeakListTest extends TestCase {
	OldBinnedPeakList old1, old2;
	BinnedPeakList new1, new2;
	
	float delta = 0.00001f;
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/*
	 *@author steinbel
	 *Temporary tester method until unit test is rewritten.
	 */
	public void testMultiply(){
		BinnedPeakList bpl = new BinnedPeakList();
		bpl.add(-1, (float)-1.0);
		bpl.add(1, (float)1.0);
		bpl.add(2, (float)2.0);
		bpl.add(3, (float)3.0);
		
		bpl.multiply((float)20000);
		
		assert(bpl.getAreaAt(-1) == -20000): "failed at location -1, value: " + bpl.getAreaAt(-1);
		assert(bpl.getAreaAt(1) == 20000): "failed at location 1, value: " + bpl.getAreaAt(1);
		assert(bpl.getAreaAt(2) == 40000): "failed at location 2, value: " + bpl.getAreaAt(2);
		assert(bpl.getAreaAt(3) == 60000): "failed at location 3, value: " + bpl.getAreaAt(3);
		
		
		
	}
	
	public void testAdd() {
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-250, (float) 1);
		bp1.add(-200, (float) 1);
		bp1.add(-95, (float) 1);
		bp1.add(-25, (float) 1);
		bp1.add(30, (float) 1);
		bp1.add(100, (float) 1);
		bp1.add(125, (float) 1);
		//null peaks should never appear in the code, if they appear, an assert should fire
	//	bp1.peaks.put(180, null);
		
		bp1.add(-95, (float) 1);
		bp1.add(-25, (float) 1);
		bp1.add(30, (float) 1);
		bp1.add(100, (float) 1);
		bp1.add(180, (float) 1);

		assertEquals(bp1.peaks.get(-250), (float) 1);
		assertEquals(bp1.peaks.get(-200), (float) 1);
		assertEquals(bp1.peaks.get(-95), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(-25), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(30), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(100), (float) 1 + (float) 1);
		assertEquals(bp1.peaks.get(125), (float) 1);
	}
	public void testAddWeightedParticle() {
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-250, (float) 25);
		bp1.add(-200, (float) 20);
		bp1.add(-150, (float) 15);
		bp1.add(-100, (float) 10);
		bp1.add(-50, (float) 5);
		bp1.add(0, (float) 1);
		bp1.add(50, (float) 5);
		bp1.add(100, (float) 10);
		bp1.add(150, (float) 15);
		bp1.add(200, (float) 20);
		bp1.add(250, (float) 25);
		bp1.printPeakList();
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-170, (float) 17);
		bp2.add(-70, (float) 7);
		bp2.add(-50, (float) 5);
		bp2.add(0, (float) 1);
		bp2.add(50, (float) 5);
		bp2.add(70, (float) 7);
		bp2.add(170, (float) 17);
		
		bp1.addWeightedParticle3(bp2, 2);
		
		bp1.printPeakList();
	}
	public void testEmptyList() {
		BinnedPeakList bpl = new BinnedPeakList(new Normalizer());
		BinnedPeakList bpl2 = new BinnedPeakList(new Normalizer());
		
		bpl.addAnotherParticle(bpl2);
		
		assertEquals(0, bpl.length());
		assertFalse(bpl.iterator().hasNext());
		
		bpl.divideAreasBy(3);
		bpl.multiply(3);
		assertEquals(0.0f, bpl.getDistance(bpl2, CITY_BLOCK));
		bpl2.add(3, .2f);
		assertEquals(.2f, bpl.getDistance(bpl2, CITY_BLOCK));
		
		bpl.normalize(CITY_BLOCK); // what should this actually do?
		
	}
	
	public void testGetDistance2() {
		float location;
		float area;
		
		Normalizer n = new Normalizer();
		for (int i = 0; i < 1000; i++) {
			old1 = new OldBinnedPeakList();
			old2 = new OldBinnedPeakList();
			
			new1 = new BinnedPeakList(n);
			new2 = new BinnedPeakList(n);
			
			int numPeaks = 40;
			
			for (int peaks = 0; peaks < numPeaks; peaks++) {
				location = (float) (Math.random() - 0.5) * 4500;
				area = (float) Math.random();
				if (Math.random() > 0.5) {
					old1.add(location, area);
					new1.add(location, area);
				} else {
					old2.add(location, area);
					new2.add(location, area);
				}
			}
			
			old1.divideAreasBy(numPeaks);
			old2.divideAreasBy(numPeaks);
			new1.divideAreasBy(numPeaks);
			new2.divideAreasBy(numPeaks);
			
			assertEquals(old1.getMagnitude(CITY_BLOCK), new1.getMagnitude(CITY_BLOCK), 0.0001);
			assertEquals(old2.getMagnitude(CITY_BLOCK), new2.getMagnitude(CITY_BLOCK), 0.0001);
			
			assertEquals(old1.getDistance(old2, EUCLIDEAN_SQUARED),
					new1.getDistance(new2, EUCLIDEAN_SQUARED), delta);
			assertEquals(old1.getDistance(old2, EUCLIDEAN_SQUARED),
					old2.getDistance(old1, EUCLIDEAN_SQUARED), delta);
			assertEquals(new1.getDistance(new2, EUCLIDEAN_SQUARED),
					new2.getDistance(new1, EUCLIDEAN_SQUARED), delta);
			
			assertEquals(old1.getDistance(old2, DOT_PRODUCT),
					new1.getDistance(new2, DOT_PRODUCT), delta);
			assertEquals(old1.getDistance(old2, DOT_PRODUCT),
					old2.getDistance(old1, DOT_PRODUCT), delta);
			assertEquals(new1.getDistance(new2, DOT_PRODUCT),
					new2.getDistance(new1, DOT_PRODUCT), delta);	
			
			assertEquals(old1.getDistance(old2, CITY_BLOCK),
					new1.getDistance(new2, CITY_BLOCK), delta);
			assertEquals(old1.getDistance(old2, CITY_BLOCK),
					old2.getDistance(old1, CITY_BLOCK), delta);
			assertEquals(new1.getDistance(new2, CITY_BLOCK),
					new2.getDistance(new1, CITY_BLOCK), delta);
			
		}
	}
	
	private class OldBinnedPeakList {

		private ArrayList<Integer> locations;
		private ArrayList<Float> areas;
		int position = -1;

		private static final int MAX_LOCATION = 2500;
		private int DOUBLE_MAX = MAX_LOCATION * 2;
		private float[] longerLists = new float[MAX_LOCATION * 2];
		/**
		 * A constructor for the peaklist, initializes the underlying
		 * ArrayLists to a size of 20.
		 */
		public OldBinnedPeakList()
		{
			locations = new ArrayList<Integer>(20);
			areas = new ArrayList<Float>(20);
		}
		
		public float getMagnitude(DistanceMetric dMetric)
		{
			float magnitude = 0;
			
			resetPosition();
			//if (list.length() == 0)
			//{
			//	BinnedPeakList returnThis = new BinnedPeakList();
			//	returnThis.add(0.0f,1.0f);
			//	return returnThis;
			//}
			if (dMetric == DistanceMetric.CITY_BLOCK)
				for (int i = 0; i < length(); i++)
				{
					magnitude += getNextLocationAndArea().value;
				}
			else if (dMetric == DistanceMetric.EUCLIDEAN_SQUARED ||
			         dMetric == DistanceMetric.DOT_PRODUCT)
			{
				float currentArea;
				for (int i = 0; i < length(); i++)
				{
					currentArea = getNextLocationAndArea().value;
					magnitude += currentArea*currentArea;
				}
				magnitude = (float) Math.sqrt(magnitude);
			}
			resetPosition();
			return magnitude;
		}
		
		// TODO: Update this to the real thing
		public float getDistance(OldBinnedPeakList toList, DistanceMetric dMetric)
		{
//			TODO: Make this more graceful
			
			//This seems to take a 2 seconds longer?
			//Arrays.fill(longerLists, 0.0f);
			
			resetPosition();
			toList.resetPosition();
			
		    // longerLists keeps track of which peak locations have nonzero areas
			for (int i = 0; i < DOUBLE_MAX; i++)
			{
				longerLists[i] = 0;
			}
			float distance = 0;
			OldBinnedPeakList longer;
			OldBinnedPeakList shorter;
			resetPosition();
			toList.resetPosition();
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
			
			BinnedPeak temp;
			
			for (int i = 0; i < longer.length(); i++)
			{
				temp = longer.getNextLocationAndArea();
				longerLists[temp.key + MAX_LOCATION] = temp.value;
				//Do we need this?: - nope
				//bCheckedLocs[temp.location + MAX_LOCATION] = true;

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
			
			shorter.resetPosition();
			longer.resetPosition();
			float eucTemp = 0;
			for (int i =  0; i < shorter.length(); i++)
			{
				temp = shorter.getNextLocationAndArea();
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

			assert distance < 2.01 :
			    "Distance should be <= 2.0, actually is " + distance +"\n" 
			    + "Magnitudes: toList = " + toList.getMagnitude(dMetric) + " this = "
			    + getMagnitude(dMetric) + "\n" ;
			if (distance > 2) {
				//System.out.println("Rounding off " + distance +
				//		"to 2.0");
				distance = 2.0f;
			}
			
			return distance;
		}
		
		/**
		 * Retrieve the value of the peaklist at a given key
		 * @param key	The key of the value you wish to
		 * 					retrieve.
		 * @return			The value at the given key.
		 */
		public float getAreaAt(int location)
		{
			Integer temp = null;
			for (int i = 0; i < locations.size(); i++)
			{
				if (locations.get(i).intValue() == location)
					return areas.get(i).floatValue();
			}
			return 0;
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
			float temp = 0;
			boolean exists = false;
			int locationInt;
			
			// If the key is positive or zero, then add 0.5 to round.
			// Otherwise, subtract 0.5 to round.
			if (location >= 0.0f)
				locationInt = (int) ((float) location + 0.5);
			else
				locationInt = (int) ((float) location - 0.5);
			
			for (int i = 0; i < locations.size(); i++)
			{
				if(locations.get(i).intValue() == locationInt)
				{
					temp = areas.get(i).floatValue() + area;
					areas.set(i,new Float(temp));
					exists = true;
					return;
				}
			}
			if (!exists)
			{
				locations.add(new Integer(locationInt));
				areas.add(new Float(area));
			}
		}
		
		/**
		 * Returns the number of locations represented by this 
		 * Binned peaklist
		 * @return the number of locations in the list
		 */
		public int length()
		{
			return locations.size();
		}
		
		/**
		 * This skips all the checks of add().  Do not use this unless
		 * you are copying from another list, not taking care to make
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
			locations.add(new Integer(location));
			areas.add(new Float(area));
		}
		
		/**
		 * Reset this peaklist to the beginning for the 
		 * getNextLocationAndArea function.
		 *
		 */
		public void resetPosition() 
		{
			position = -1;
		}
		
		/**
		 * Returns a BinnedPeak representing the next peak in the list.
		 * @return the next peak.
		 */
		public BinnedPeak getNextLocationAndArea()
		{
			position++;
			return new BinnedPeak(locations.get(position).intValue(), 
					areas.get(position).floatValue());
			//return peaks.get(position);
			
		}
		
		public void divideAreasBy(int divisor) {
			for (int i = 0; i < areas.size(); i++)
			{
				areas.set(i, new Float(areas.get(i).floatValue() / divisor));
			}
		}
			
		public void printPeakList() {
			System.out.println("printing peak list");
			boolean exception = false;
			int counter = 0;
			resetPosition();
			BinnedPeak p;
			while (!exception) {
				try {
					p = getNextLocationAndArea();
					System.out.println(p.key + ", " + p.value);
				}catch (Exception e) {exception = true;}
			}
			resetPosition();
		}
		
		public int getLastLocation() {
			int lastLoc = -30000;
			for (int i = 0; i < locations.size(); i++) 
				if (locations.get(i).intValue() > lastLoc)
					lastLoc = locations.get(i).intValue();
			return lastLoc;
		}
		
		public int getFirstLocation() {
			int firstLoc = 30000;
			for (int i = 0; i < locations.size(); i++) 
				if (locations.get(i).intValue() < firstLoc)
					firstLoc = locations.get(i).intValue();
			return firstLoc;
		}
		
		public float getLargestArea() {
			float largestArea = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < locations.size(); i++) 
				if (areas.get(i).floatValue() > largestArea)
					largestArea = areas.get(i).floatValue();
			return largestArea;
		}
		
		public void addAnotherParticle(OldBinnedPeakList peaks) {
			BinnedPeak peak = null;
			for (int i = 0; i < peaks.length(); i++) {
				peak = new BinnedPeak(peaks.locations.get(i).intValue(), 
						peaks.areas.get(i).floatValue());
				add(peak.key, peak.value);
			}
		}
	}
}
