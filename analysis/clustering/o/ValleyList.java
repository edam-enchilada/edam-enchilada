package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValleyList extends ArrayList<Extremum> {
	/**
	 * again, no idea what a serialVersionUID is..
	 */
	private static final long serialVersionUID = 7308337869915628972L;

	public List<Extremum> getValleyNeighborhood(int index) {
		int realIndex = (index * 2) + 1;
		return subList(realIndex - 1, realIndex + 2);
	}
	
	public Extremum getValley(int index) {
		return super.get((index * 2) + 1);
	}
	
	public int numValleys() {
		return (super.size() - 1) / 2;
	}
	
	public void setLast(Extremum elem) {
		set(super.size() - 1, elem);
	}
	public Extremum getLast() {
		return super.get(super.size() - 1);
	}

	public ValleyList removeInsignificant(int certaintyPercent) {
		float targetChiSquared = percentToChiSquared(certaintyPercent);
		
		ValleyList newCopy = new ValleyList();
		if (super.size() < 2) {
			return newCopy;
		}
		Extremum e;
		Iterator<Extremum> extrema = super.iterator();
		newCopy.add(extrema.next());
		boolean maximizePeak = true;;
		
		while (extrema.hasNext()) {
			e = extrema.next();
			if (maximizePeak) {
				// look for a better max
				if (e.count > newCopy.getLast().count) {
					newCopy.setLast(e);
				}
				// or a significant dip -- in which case we change state
				else if (chiSquared(newCopy.getLast(), e) > targetChiSquared)
				{
					maximizePeak = false;
					newCopy.add(e);
				}
			} else {
				// look for a better min
				if (e.count < newCopy.getLast().count) {
					newCopy.setLast(e);
				}
				// or a significant jump
				else if (chiSquared(e, newCopy.getLast()) > targetChiSquared)
				{
					maximizePeak = true;
					newCopy.add(e);
				}
			}
		}
		
		// if the last piece of newCopy is a valley, remove it, since
		// it's not usable as a split plane.
		if (! maximizePeak) {
			super.remove(super.size() - 1);
		}
		
		return newCopy;
	}

	private float percentToChiSquared(int percent) {
		// percent is percent certainty.
		// TODO: find out how the statistics work for calculating a chi-squared
		// target thingie.
		if (percent == 95) {
			return 3.843f;
		} else if (percent == 90) {
			return 2.706f;	
		} else {
			throw new Error("Finding target chi-squareds is not yet implemented!");
		}
	}

	private float chiSquared(Extremum peak, Extremum valley) {
		float expected = (peak.count + valley.count) / 2;
		return 2*((float) (Math.pow(valley.count - expected,2))
							/expected);
	}
	
}
