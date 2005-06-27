package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import analysis.BinnedPeak;

public class ValleyList extends ArrayList<Extremum> {
	public List<Extremum> getValley(int index) {
		int realIndex = (index * 2) + 1;
		return subList(realIndex - 1, realIndex + 2);
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
		boolean findValley = false;
		
		while (extrema.hasNext()) {
			e = extrema.next();
			if (findValley) {
				// look for a better max
				if (e.count > newCopy.getLast().count) {
					newCopy.setLast(e);
				}
				// or a significant dip
				else if (chiSquared(newCopy.getLast(), e) > targetChiSquared)
				{
					findValley = false;
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
					findValley = true;
					newCopy.add(e);
				}
			}
		}
		
		// TODO: if the last piece of newCopy is a valley, remove it, since
		// it's not usable as a split plane.
		
		return newCopy;
	}

	private float percentToChiSquared(int certaintyPercent) {
		// TODO Auto-generated method stub
		return 0;
	}

	private float chiSquared(Extremum peak, Extremum valley) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
