package analysis.clustering.o;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ValleyList {
	private class VLValley {
		public VLPeak left;
		public VLPeak right;
		public int count;
		
		public VLValley(VLPeak left, int count, VLPeak right) {
			super();
			// TODO Auto-generated constructor stub
			this.left = left;
			this.count = count;
			this.right = right;
		}
	}
	
	private class VLPeak {
		public VLValley left;
		public VLValley right;
		public int count;
		public VLPeak(VLValley left, int count, VLValley right) {
			super();
			// TODO Auto-generated constructor stub
			this.left = left;
			this.count = count;
			this.right = right;
		}
	}
	
	private VLPeak head;
	
//	public List<Integer> getValley(int index) {
//		int realIndex = (index * 2) + 1;
//		return subList(realIndex - 1, realIndex + 2);
//	}
	
	public void smooth(int index) {
		//TODO: uh yeah
	}
	
	public class VLIterator implements Iterator<List<Integer>> {
		private VLValley current;
		private boolean nextCalled;
		
		public VLIterator(VLPeak head) {
			current = new VLValley(null, 0, head);
			nextCalled = false;
		}

		/* (non-Javadoc)
		 * @see java.util.ListIterator#hasNext()
		 */
		public boolean hasNext() {
			return (head.right.right == null);
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#next()
		 */
		public List<Integer> next() {
			current = current.right.right;
			nextCalled = true;
			
			ArrayList<Integer> pvp = new ArrayList<Integer>(3);
			pvp.add(current.left.count);
			pvp.add(current.count);
			pvp.add(current.right.count);
			return pvp;
		}

		/* (non-Javadoc)
		 * @see java.util.ListIterator#remove()
		 */
		public void remove() {
			if (! nextCalled) {
				throw new Error("next() never called before remove()!");
			}
			nextCalled = false;
			
			// the lower-count (less significant) peak is the one we will
			// remove.
			VLPeak delPeak = current.left.count < current.right.count ?
					current.left : current.right;
			VLValley keeper;
			
			// they can't both be null, luckily..
			// except boo, this will refuse to delete if it's the only item
			// in the list.
			if (delPeak.left == null) {
				keeper = delPeak.right;
			} else if (delPeak.right == null) {
				keeper = delPeak.left;
			} else {
				// the lower-count (MORE significant) valley is the one
				// we will keep.
				keeper = delPeak.left.count < delPeak.right.count ?
						delPeak.left : delPeak.right;
			}
			
			VLPeak newLeft = delPeak.left == null ? 
					null : delPeak.left.left;
			VLPeak newRight = delPeak.right == null ?
					null : delPeak.right.right;
			
			keeper.left = newLeft;
			keeper.right = newRight;
			
			if (keeper.left != null) {
				keeper.left.right = keeper;
			}
			if (keeper.right != null) {
				keeper.right.left = keeper;
			}
			
			
			
		}
		
	}
}
