package collection;

import java.util.*;

/**
 * Struct-like class to hold onto Collection-specific
 * aggregation options.
 * @author gregc
 *
 */
public class AggregationOptions {
	public static enum CombiningMethod { SUM, AVERAGE };
	
	// These are only relevant to ATOFMS:
	public double peakTolerance = .4;
	public boolean produceParticleCountTS = true;
	public boolean treatDataAsContinuous = false;
	public boolean allMZValues = false;
	public CombiningMethod combMethod = CombiningMethod.SUM;
	public ArrayList<Integer> mzValues;
	public String mzString = "";

	public String getGroupMethodStr() {
		return combMethod == AggregationOptions.CombiningMethod.SUM ? "SUM" : "AVG";
	}
	public void setMZValues(String mzString) {
		this.mzString = mzString;
		
		ArrayList<Integer> tempValues = new ArrayList<Integer>();
		
		if (mzString.trim().equals("")) {
			mzValues = tempValues;
			return;
		}
	
		
		String[] ranges = mzString.split(",");
		for (int i = 0; i < ranges.length; i++) {
			String range = ranges[i].trim();
			String[] splitRange = range.split(" to ");
			int low = Integer.parseInt(splitRange[0]);
			int high = splitRange.length == 2 ? Integer.parseInt(splitRange[1]) : low;
			
			// Swap if the user got them backwards...
			if (low > high) {
				int temp = low;
				high = temp;
				low = high;
			}
			
			if (low < -600 || low > 600 || high < -600 || high > 600)
				throw new NumberFormatException();
			
			while (low <= high)
				tempValues.add(low++);
		}
		
		Collections.sort(tempValues);
		mzValues = tempValues;
	}
	
	public void setDefaultOptions() {
		// These are only relevant to ATOFMS:
		peakTolerance = .4;
		produceParticleCountTS = true;
		treatDataAsContinuous = false;
		combMethod = CombiningMethod.SUM;
		mzValues = new ArrayList<Integer>();
		allMZValues = false;
		mzString = "";
	}
}
