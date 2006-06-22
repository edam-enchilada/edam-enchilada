package experiments;

import java.awt.*;
import java.util.*;
import java.sql.*;

import javax.swing.*;

import collection.Collection;

import database.*;

import ATOFMS.ParticleInfo;
import analysis.*;

public class Histogrammer {
	private SQLServerDatabase db;
	private static final int maxMZ = 200;
	private static final int graphHeight = 100;
	
	
	// once this is in a full gui, these could be controlled by a slider,
	// to set something like the "resolution" of the histogram.  
	private static final float maxBinCount = 60;
	private static final float paintIncrement = 1f / maxBinCount;

	
	private JPanel cpane;
	private JFrame window;
	
	private class DrawInfo {
		public ChainingHistogram[] hists;
		public int count;
		public Color color;
		public DrawInfo(int count, ChainingHistogram[] hists, Color color) {
			this.count = count;
			this.hists = hists;
			this.color = color;
		}
	}
	
	/**
	 * This histogram actually stores references to the source of
	 * the hit in each bin.  erm, like, by looking at a bin, you can
	 * find out what objects are there.  It's a lot like a chaining
	 * hash table, except that the hash function is meaningful.
	 */
	private class ChainingHistogram 
		extends BinAddressableArrayList<ArrayList<BinnedPeakList>>
	{	
		private int hitCount;
		
		public ChainingHistogram(float binWidth) {
			super(binWidth);
		}

		public void addPeak(float peakHeight, ParticleInfo pInfo) {
			if (peakHeight > 1) {throw new IllegalArgumentException();} 
			ArrayList<BinnedPeakList> target;
			
			target = get(peakHeight);
			if (target == null) { // if the list is not this long, or if it is but nothing has been added to this bin yet.
				target = new ArrayList<BinnedPeakList>();
				expandAndSet(peakHeight, target);
			}
			
			assert(pInfo.getBinnedList() != null) : "Wrong kind of cursor used on database";
			
			target.add(pInfo.getBinnedList());
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
	
	private Map<Integer, DrawInfo> collectionHistograms = new TreeMap<Integer, DrawInfo>();
	private Canvas canvas;
	
	public Histogrammer() {
		super();
		db = new SQLServerDatabase();
		if (!db.openConnection()) throw new RuntimeException();

		window = new JFrame("Mockup of Pretty Histogram Display");
		cpane = new JPanel();
		cpane.setBackground(new Color(1f, 1f, 1f));
		window.setContentPane(cpane);
		cpane.setPreferredSize(new Dimension(maxMZ * 2, graphHeight));


		canvas = new Canvas() {
			public void paint(Graphics g) {
				if (g == null) { System.out.println("g is null!!"); }
				for (DrawInfo dataset : collectionHistograms.values()) {
					float R = dataset.color.getRed() / 255f,
					      G = dataset.color.getGreen() / 255f,
					      B = dataset.color.getBlue() / 255f;
					
					for (int mz = 0; mz < maxMZ; mz++) {
						if (dataset.hists[mz] == null) continue;
						for (int i = 0; i < graphHeight; i++) {
							g.setColor(new Color(R,G,B, 
//								min(dataset.hists[mz].getCountAtIndex(i) * paintIncrement,
//								    1)));
//							g.fillOval(2*mz, graphHeight - i, 2, 2);

							min(60 * 
							((float) dataset.hists[mz].getCountAtIndex(i)) / dataset.count, 1)));
						g.drawLine(2 * mz, graphHeight - i, 2 * mz + 1, graphHeight - i);
						}
					}
				}
			}
		};
		canvas.setPreferredSize(new Dimension(maxMZ*2, graphHeight));
		cpane.add(canvas);
		
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		
		
	}

	
	public void drawCollection(int collID, Color c) {
		Collection coll = db.getCollection(collID);
		CollectionCursor b = db.getBinnedCursor(coll);
		BinnedPeakList peakList;
		int partnum = 0;

		final ChainingHistogram[] histograms = new ChainingHistogram[maxMZ];
		
		while (b.next()) {
			ParticleInfo pInfo = b.getCurrent();
			peakList = pInfo.getBinnedList();
			peakList.normalize(DistanceMetric.CITY_BLOCK);

			++partnum;
			
			for (BinnedPeak p : peakList) {
				if (p.key >= maxMZ || p.key < 0)
					continue;
				if (histograms[p.key] == null) {
					histograms[p.key] = new ChainingHistogram(1.0f / graphHeight);
				}
				histograms[p.key].addPeak(p.value, pInfo);
			}
		}
		
		collectionHistograms.put(coll.getCollectionID(), 
				new DrawInfo(partnum, histograms, c));
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				canvas.paint(canvas.getGraphics());
			}
		});
	}
	
	/**
	 * @param args not used
	 */
	public static void main(String[] args) throws SQLException {
		Histogrammer h = new Histogrammer();
		
		h.drawCollection(24, new Color(0f, 0f, 0f));
		h.drawCollection(827, new Color(0f, 0.5f, 0f));
	}
	
	public static float min(float a, float b) {
		return a < b ? a : b;
	}

}
//
//// here is an old attempt to do something interesting.
//
//package analysis;
//
//import java.util.*;
//
//import database.CollectionCursor;
//import database.InfoWarehouse;
//import database.SQLServerDatabase;
//
///**
// * @author Thomas Smith.
// * 
// * This class is incomplete, it's designed simply to wire up MedianFinder to
// * arbitrary collections and display pretty results in a graph eventually.
// * At the moment it just prints out some results.  Wheeeeee.
// */
//

//public class CollectionSummary {
//	MedianFinder medFinder;
//	ArrayList<BinnedPeakList> atoms;
//	// private static final eternal electric-fenced stonecarved int 
//	private static final int MAX_ITER = 5000;
//	
//	public CollectionSummary(int collID) {
//		InfoWarehouse db = new SQLServerDatabase();
//		atoms = new ArrayList<BinnedPeakList>();
//	
//		db.openConnection();
//		CollectionCursor curs = db.getBinnedCursor(db.getCollection(collID));
//	
//		
//		while (curs.next()) {
//			atoms.add(curs.getCurrent().getBinnedList());
//		}
//		
//		for (Iterator<BinnedPeakList> i = atoms.iterator();
//			i.hasNext(); )
//		{
//			i.next().normalize(DistanceMetric.CITY_BLOCK);
//		}
//		
//		medFinder = new MedianFinder(atoms, true);
//		
//		BinnedPeakList firstQuarter 
//			= medFinder.getPercentElement(0.75f);
//		BinnedPeakList middle = medFinder.getMedian();
//		BinnedPeakList lastQuarter
//			= medFinder.getPercentElement(0.25f);
//		
//		
//		for (int i = 0; i < MAX_ITER; i++) {
//			if (lastQuarter.getAreaAt(i) == 0.0f) {
//				continue;
//			}
//			System.out.println(i +"\t"+ firstQuarter.getAreaAt(i)
//					+"\t"+ middle.getAreaAt(i)
//					+"\t"+ lastQuarter.getAreaAt(i));
//		}
//		
//		
//	}
//	
//	public static void main(String[] args) {
//		CollectionSummary s = new CollectionSummary(7);
//	}
//}

