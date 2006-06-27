package experiments;

/**
 * This is a prototype for a data visualization for looking at the contents
 * of a Collection.  It displays a histogram of the distribution of each 
 * dimension, vertically parallel to each other so that they sorta resemble
 * a spectrum.
 * 
 *   @author smitht
 */

/*
 * In order to make this prototype into a functional part of the real software,
 * a lot remains to be done.  
 * It might be nice to support visualizing several collections at once 
 * (same graph).  
 * 
 * Make the database scan part into a SwingWorker.
 * 
 * Possible enhancements to the graph itself:  better represent 0 values.  And
 * allow different numbers of histogram bins.
 * 
 * Then, the window needs to
 * be made prettier.  Deborah asked for a coordinate display so that the x,y
 * coordinates of the point under the mouse are displayed somewhere.  A switch
 * to go between absolute and relative bin counts for color determination, and
 * a slider for how dark it is.  This can all be done with only a rescan of the
 * histogram, not the database, which is nice.
 * 
 * It would
 * be great to fit the graphing functionality into Chartlib somehow, so that
 * support for zooming is easy.  The difficulty with this is that a Dataset
 * object from Chartlib is currently just a TreeSet<DataPoint>, which is not
 * enough dimensions for this class.  So something will have to be done there.
 * 
 * Then, zooming and scrolling, which should be very easy.  
 * Maybe then, displaying different collections in different colors (color
 * selection dialog or know colors already?)
 * Then, perhaps, work can start on selecting atoms by their value in the
 * histogram. 
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
		cpane.setPreferredSize(new Dimension(maxMZ * 2, graphHeight + 20));


		canvas = new GraphCanvas();
		canvas.setPreferredSize(new Dimension(maxMZ*2, graphHeight + 20));
		cpane.add(canvas);
		
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		
		
	}

	
	public void drawCollection(int collID, Color c) {
		Collection coll = db.getCollection(collID);
		CollectionCursor particleCursor = db.getBinnedCursor(coll);
		BinnedPeakList peakList;
		int partnum = 0;

		final ChainingHistogram[] histograms = new ChainingHistogram[maxMZ];
		
		while (particleCursor.next()) {
			ParticleInfo pInfo = particleCursor.getCurrent();
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

	
	private static class DrawInfo {
		public ChainingHistogram[] hists;
		public int count;
		public Color color;
		public DrawInfo(int count, ChainingHistogram[] hists, Color color) {
			this.count = count;
			this.hists = hists;
			this.color = color;
		}
	}
	
	private class GraphCanvas extends Canvas {
		public void paint(Graphics g) {
			if (g == null) { System.out.println("g is null!!"); }
			for (DrawInfo dataset : collectionHistograms.values()) {
				float R = dataset.color.getRed() / 255f,
				      G = dataset.color.getGreen() / 255f,
				      B = dataset.color.getBlue() / 255f;
				
				float factor = 60f / (float) dataset.count;
				
				for (int mz = 0; mz < maxMZ; mz++) {
					g.setColor(new Color(R, G, B, 0.5f));
					
					if (dataset.hists[mz] == null) {
						g.fillRect(2 * mz, graphHeight + 2, 2, 10);
						continue;
					}
					g.fillRect(2 * mz, graphHeight + 2, 2,
							(int) ((dataset.count - dataset.hists[mz].getHitCount())
												/ (float) dataset.count * 10f));
					for (int i = 0; i < graphHeight; i++) {
						if (dataset.hists[mz].getCountAtIndex(i) > 1) {
							g.setColor(new Color(R,G,B, 
	//							min(dataset.hists[mz].getCountAtIndex(i) * paintIncrement,
	//							    1)));
	//						g.fillOval(2*mz, graphHeight - i, 2, 2);
	
							min(factor * ((float) dataset.hists[mz].getCountAtIndex(i)),
								1)));
							g.drawLine(2 * mz, graphHeight - i, 2 * mz + 1, graphHeight - i);
							
						}
					}

				}
			}
		}
	}
	
	/**
	 * This histogram actually stores references to the source of
	 * the hit in each bin.  erm, like, by looking at a bin, you can
	 * find out what objects are there.  It's a lot like a chaining
	 * hash table, except that the hash function is meaningful.
	 */
	private static class ChainingHistogram 
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
}
