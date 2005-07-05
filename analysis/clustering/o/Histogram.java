package analysis.clustering.o;

import java.util.*;

public class Histogram {
	private HistList histogram;
	private float binWidth;
	private ValleyList splitPoints;
	private int dimension;
	
	public Histogram(float stdDev, int count) {
		// "Scott's normal reference rule"
		this.binWidth = (float) (3.49 * stdDev * Math.pow(count, -1.0/3));
		histogram = new HistList(binWidth);
		splitPoints = new ValleyList();
	}
	
	public Histogram(float stdDev, int count, int dimension) {
		this(stdDev, count);
		this.dimension = dimension;
	}

	public void addPeak(float area) {
		histogram.addPeak(area);
	}
	
	private int findAllValleys() {
		LinkedList<Integer> extremaLocations = new LinkedList<Integer>();
		int localMinLoc = 0;
		for (int bin = 0; bin < histogram.size(); bin++) {
			// we can access past the ends of the array without worrying: see 
			// get() in the HistList class.
			
			if (histogram.get(bin - 1) < histogram.get(bin)
					&& histogram.get(bin) >= histogram.get(bin + 1))
			{
				// this bin contains a peak...
				
				// the second condition is >= so that if multiple
				// bins have the same count and are together a peak,
				// the first one will be detected as such.
				
				// the condition for a peak can never be true twice in a row:
				assert(localMinLoc != bin);

				extremaLocations.add(localMinLoc);
				extremaLocations.add(bin);
				localMinLoc = bin + 1;
			} else {
				// between peaks: keep track of the minimum.
				if (histogram.get(bin) < histogram.get(localMinLoc)) {
					localMinLoc = bin;
				}
			}
		}
		
		extremaLocations.remove(); // the 0th element is where the local min
		// before 0 would go.  but there is none, so we remove it.
		
		splitPoints.clear();
		Iterator<Integer> i = extremaLocations.iterator();
		Integer loc;
		Extremum e;
		while (i.hasNext()) {
			loc = i.next();
			e = new Extremum(loc, histogram.get(loc));
			//System.out.println(e.toString());
			splitPoints.add(e);
		}
		
		return splitPoints.numValleys();
	}
	
	public List<Float> getSplitPoints(int confidencePercent) {
		LinkedList<Float> splits = new LinkedList<Float>();
		if (findAllValleys() > 0) {
			splitPoints = splitPoints.removeInsignificant(confidencePercent);
			for (int i = 0; i < splitPoints.numValleys(); i++) {
				splits.add(histogram.getIndexMiddle(
						splitPoints.getValley(i).location));
			}
			return splits;
		} else {
			return null;
		}
	}
	

	// XXX: sometimes returns null and sometimes returns an empty list.
	//aklsdfjkajsdkfjkasjdkfjaks
	public List<SplitRule> getSplitRules(int confidencePercent) {
		List<SplitRule> rules = new LinkedList<SplitRule>();
		
		List<Float> areas = getSplitPoints(confidencePercent);
		
		if (areas == null) {
			return null;
		} else {
			Iterator<Float> i = areas.iterator();
			float thisArea;
			
			while (i.hasNext()) {
				thisArea = i.next();
				// goodness is the opposite of the density of this bin.
				rules.add(new SplitRule(dimension, thisArea,
						0 - histogram.get(thisArea)));
			}
			return rules;
		}
	}
	
	public SplitRule getBestSplit(int confidencePercent) {
		List<SplitRule> goodRules = getSplitRules(confidencePercent);
		SplitRule best = null;
		SplitRule temp;
		Iterator<SplitRule> i;
		
		if (goodRules == null) {
			return null;
		} else {
			i = goodRules.iterator();
			
			while (i.hasNext()) {
				if (best == null) {
					best = i.next();
				} else {
					temp = i.next();
					if (best.goodness < temp.goodness) {
						best = temp;
					}
				}
			}
			return best;
		}
	}
	
	public void printHistogram(boolean printSplits) {
		System.out.println("Histogram for Dimension " + dimension);
		System.out.println("Pk.Area\tCount");
		for (int i = 0; i < histogram.size(); i++) {
			System.out.println(histogram.getIndexMiddle(i) + "\t"
					+ histogram.get(i));
		}
		
		if (printSplits) {
			System.out.println("Valid splits at:");
			List<SplitRule> rules = getSplitRules(95);
			if (rules == null) {
				System.out.println("*NONE*");
			} else {
				for (SplitRule rule : rules) {
					System.out.println(rule.area);
				}
			}
		}
	}

	/**
	 * @return Returns the dimension.
	 */
	public int getDimension() {
		return dimension;
	}

	/**
	 * @param dimension The dimension to set.
	 */
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
}
