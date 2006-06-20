package analysis.clustering.o;

import java.util.*;

public class Histogram {
	private HistList histogram;
	private float binWidth;
	private ValleyList splitPoints;
	private int dimension;
	private int sensitivity;
	private int totalCount;
	
	/*
	 * We need to get Histograms to work with the dual nominal/weird nature
	 * of the ATOFMS data.
	 * 
	 * A split at 0 should be considered when there is a large number of peaks
	 * above the sensitivity value (which we don't even use yet).  Maybe?
	 * Aaaaaugh.  It's really hard to tell what to dooo!
	 * 
	 * Don't split if the number on one side is less than the Sensitivity.
	 * 
	 * Ask Dave what to do.
	 */
	
	public Histogram(float stdDev, int count) {
		// TODO: try different widths!!
		// "Scott's normal reference rule" -- probably should change to one
		// of Scott's rules that allows for tons of skew.
		this.binWidth = (float) (3.49 * stdDev * Math.pow(count, -1.0/3));
		histogram = new HistList(binWidth);
		splitPoints = new ValleyList();
		this.sensitivity = 12;
		totalCount = count;
	}
	
	public Histogram(float stdDev, int count, int dimension) {
		this(stdDev, count);
		this.dimension = dimension;
	}
	
	public Histogram(float stdDev, int count,
			int dimension, int sensitivity)
	{
		this.sensitivity = sensitivity;
	}

	/**
	 * This is the way to add data to the histogram.  The parameter is what
	 * you want histogram'd...
	 */
	public void addPeak(float area) {
		histogram.addPeak(area);
	}
	
	/**
	 * Set the count of particles, so that HistList can keep track of implicit
	 * zeroes.
	 * @param totalCount
	 */
	public void setParticleCount(int totalCount) {
		histogram.setParticleCount(totalCount);
	}
	
	private int findAllValleys() {
		LinkedList<Integer> extremaLocations = new LinkedList<Integer>();
		int localMinLoc = 0;
		for (int bin = 0; bin < histogram.size(); bin++) {
			// we can access past the ends of the array without worrying: see 
			// get() in the HistList class.
			
			if (histogram.get(bin - 1) < histogram.get(bin)
					&& histogram.get(bin) >= histogram.get(bin + 1)
					&& histogram.get(bin) > sensitivity)
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
		
		if (extremaLocations.size() > 0) {
			extremaLocations.remove();
			// the 0th element is where the local min
			// before 0 would go.  but there is none, so we remove it.
		}
		
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
	
	/**
	 * Get a list of SplitRules, a list of valid ways of splitting this
	 * dimension.
	 * 
	 * Returns either null or an empty list if there are no useful splits.
	 * Sorry about the inconsistency.  Use getBestSplit instead, it is
	 * better.
	 * 
	 * The only supported values of confidencePercent are currently 90 and 95.
	 */
	public List<SplitRule> getSplitRules(int confidencePercent) {
		LinkedList<SplitRule> splits = new LinkedList<SplitRule>();
		
		SplitRule zeroSplit = new SplitRule(dimension, 0,
				0 - Math.abs(histogram.getZeroCount() 
							- histogram.getHitCount()));
		if (zeroSplit.goodness > - totalCount / sensitivity) {
			splits.add(zeroSplit);
		}
		
		if (findAllValleys() > 0) {
			splitPoints = splitPoints.removeInsignificant(confidencePercent);
			for (int i = 0; i < splitPoints.numValleys(); i++) {
				splits.add(
						new SplitRule(
								dimension,
								histogram.getIndexMiddle(
										splitPoints.getValley(i).location),
								// goodness:
								// some would say, should be negative density.
								//splitPoints.chiSquared(i)));
								0 - histogram.get(
										splitPoints.getValley(i).location)));
									
			}
			return splits;
		} else {
			return null;
		}
	}
	
	/**
	 * Return the best SplitRule for this dimension.  If there are no
	 * valid ones, returns null.
	 */
	public SplitRule getBestSplit(int confidencePercent) {
		List<SplitRule> goodRules = getSplitRules(confidencePercent);
		SplitRule best = null;
		SplitRule temp;
		Iterator<SplitRule> i;
		
		if (goodRules == null || goodRules.size() == 0) {
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
	
	/**
	 * Prints out the bin values and their counts for this histogram.
	 * @param printSplits also print the valid split points?
	 */
	public void printHistogram(boolean printSplits) {
		//TODO: add count of added peaks, and let the number of implicit 0's
		// be figured out.
		System.out.println("Histogram for Dimension " + dimension);
		System.out.println("Pk.Area\tCount");
		System.out.println("0\t" + histogram.getZeroCount());
		for (int i = 0; i < histogram.size(); i++) {
			System.out.println(histogram.getIndexMiddle(i) + "\t"
					+ histogram.get(i));
		}
		
		if (printSplits) {
			System.out.println("Valid splits at:");
			List<SplitRule> rules = getSplitRules(95);
			if (rules == null || rules.size() == 0) {
				System.out.println("*NONE*");
			} else {
				for (SplitRule rule : rules) {
					System.out.println(rule.value+"\t"+rule.toString());
				}
			}
		}
	}

	/**
	 * @return Returns the dimension, which is just some number associated with
	 * the histogram.
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

	public void setSensitivity(int sensitivity) {
		this.sensitivity = sensitivity;
	}
}
