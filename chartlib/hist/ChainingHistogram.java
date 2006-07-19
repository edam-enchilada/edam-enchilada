package chartlib.hist;

import java.util.ArrayList;

import ATOFMS.ParticleInfo;
import analysis.BinnedPeakList;

/**
 * This histogram actually stores references to the source of
 * the hit in each bin.  erm, like, by looking at a bin, you can
 * find out what objects are there.  It's a lot like a chaining
 * hash table, except that the hash function is meaningful.
 */
public class ChainingHistogram 
	extends BinAddressableArrayList<ArrayList<BinnedPeakList>>
{	
	private int hitCount;
	
	public ChainingHistogram(float binWidth) {
		super(binWidth);
	}

	public void addPeak(float peakHeight, BinnedPeakList bpl) {
		if (peakHeight > 1) {throw new IllegalArgumentException();} 
		ArrayList<BinnedPeakList> target;
		
		target = get(peakHeight);
		if (target == null) { // if the list is not this long, or if it is but nothing has been added to this bin yet.
			target = new ArrayList<BinnedPeakList>();
			expandAndSet(peakHeight, target);
		}
		
		target.add(bpl);
		hitCount++;
	}
	
	public int getCountAt(float peakHeight) {
		return getCountAtIndex(heightToIndex(peakHeight));
		
	}
	
	public int getCountAtIndex(int index) {
		ArrayList target;
		
		target = getByIndex(index);
		if (target == null) { return 0; }
		else { return target.size(); }
	}
	
	public int getHitCount() {
		// TODO: assert that the hitcount here is equal to the sum of the hits in each arraylist.  how?
		return hitCount;
	}
}
