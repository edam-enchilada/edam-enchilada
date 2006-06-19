package experiments;

import java.awt.*;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.sql.*;

import javax.swing.*;

import collection.Collection;

import database.*;

import analysis.*;
import analysis.clustering.o.*;

public class Histogrammer {
	private SQLServerDatabase db;
	private static final int maxMZ = 300;
	private static final int graphHeight = 600;
	private JPanel cpane;
	private JFrame window;
	
	private class DrawInfo {
		public HistList[] hists;
		public int count;
		public Color color;
		public DrawInfo(int count, HistList[] hists, Color color) {
			this.count = count;
			this.hists = hists;
			this.color = color;
		}
	}
	
	private Map<Integer, DrawInfo> collectionHistograms = new TreeMap<Integer, DrawInfo>();
	
	public Histogrammer() {
		super();
		db = new SQLServerDatabase();
		if (!db.openConnection()) throw new RuntimeException();

		window = new JFrame("Mockup of Pretty Histogram Display");
		cpane = new JPanel();
		cpane.setBackground(new Color(1f, 1f, 1f));
		window.setContentPane(cpane);
		cpane.setPreferredSize(new Dimension(maxMZ * 2, graphHeight));


		Canvas c = new Canvas() {
			public void paint(Graphics g) {
				for (DrawInfo dataset : collectionHistograms.values()) {
					float R = dataset.color.getRed(), G = dataset.color.getGreen(),
						B = dataset.color.getBlue();
					R /= 255f; G /= 255f; B /= 255f;
					for (int mz = 0; mz < maxMZ; mz++) {
						if (dataset.hists[mz] == null) continue;
						for (int i = 0; i < graphHeight; i++) {
							g.setColor(new Color(R,G,B, 
									min(10 * 
								((float) dataset.hists[mz].get(i)) / dataset.count, 1)));
							g.drawLine(2 * mz, graphHeight - i, 2 * mz + 1, graphHeight - i);
						}
					}
				}
				System.out.println(new Date());
			}
		};
		c.setPreferredSize(new Dimension(maxMZ*2, graphHeight));
		cpane.add(c);
		
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		
		
	}

	
	public void drawCollection(int collID, Color c) {
		Collection coll = db.getCollection(collID);
		CollectionCursor b = db.getBinnedCursor(coll);
		BinnedPeakList particle;
		int partnum = 0;
		
		
		final HistList[] histograms = new HistList[maxMZ];
		
		while (b.next()) {
			particle = b.getCurrent().getBinnedList();
			particle.normalize(DistanceMetric.CITY_BLOCK);

			++partnum;
			
			for (BinnedPeak p : particle) {
				if (p.key >= maxMZ || p.key < 0)
					continue;
				if (histograms[p.key] == null) {
					histograms[p.key] = new HistList(1.0f / graphHeight);
				}
				histograms[p.key].addPeak(p.value);
			}
		}
		
		collectionHistograms.put(coll.getCollectionID(), 
				new DrawInfo(partnum, histograms, c));
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		Histogrammer h = new Histogrammer();
		
		h.drawCollection(24, new Color(0f, 0f, 0f));
		h.drawCollection(827, new Color(0f, 1f, 1f));
	}
	
	public static float min(float a, float b) {
		return a < b ? a : b;
	}

}
