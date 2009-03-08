package chartlib.hist;

import java.util.ArrayList;


/**
 * This histogram actually stores references to the source of
 * the hit in each bin.  erm, like, by looking at a bin, you can
 * find out what atoms from the database are there.  It's a lot like a chaining
 * hash table, except that the hash function is meaningful: it is the relative
 * area of a particular m/z value for a particle.  If that doesn't make sense,
 * just look at the addPeak() method.
 * 
 * @author smitht
 */
public class ChainingHistogram 
	extends BinAddressableArrayList<ArrayList<Integer>>
{	
	private int hitCount;
	
	public ChainingHistogram(float binWidth) {
		super(binWidth);
	}

	public void addPeak(float peakHeight, Integer atomID) {
		if (peakHeight > 1) {throw new IllegalArgumentException();} 
		ArrayList<Integer> target;
		
		target = get(peakHeight);
		if (target == null) { 
			// if the list is not this long,
			// or if it is but nothing has been added to this bin yet.
			target = new ArrayList<Integer>();
			expandAndSet(peakHeight, target);
		}
		
		target.add(atomID);
		hitCount++;
	}
	
	public int getCountAt(float peakHeight) {
		return getCountAtIndex(heightToIndex(peakHeight));
		
	}
	
	public int getCountAtIndex(int index) {
		ArrayList<Integer> target;
		
		target = getByIndex(index);
		if (target == null) { return 0; }
		else { return target.size(); }
	}
	
	public int getHitCount() {
		// TODO: assert that the hitcount here is equal to the sum of the 
		// hits in each arraylist.  how?
		return hitCount;
	}
	
	void setListAt(ArrayList<Integer> newList, float peakHeight) {
		int subtract, add;
		subtract = getCountAt(peakHeight);
		add = newList.size();
		expandAndSet(peakHeight, newList);
		hitCount = hitCount - subtract + add; 
	}
	
	public boolean equals(Object thatObject) {
		if (thatObject == null || !(thatObject instanceof ChainingHistogram))
			return false;
	
		ChainingHistogram that = (ChainingHistogram) thatObject;	
		if (this.hitCount != that.hitCount)  
			return false;
		
		return super.equals(that);
	}
}
