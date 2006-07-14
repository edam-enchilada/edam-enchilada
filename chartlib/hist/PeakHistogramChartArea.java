/**
 * 
 */
package chartlib.hist;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import ATOFMS.ParticleInfo;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import chartlib.AbstractMetricChartArea;
import chartlib.ZoomableChart;
import static java.lang.Math.min;
import java.lang.Math;

import javax.swing.JFrame;

import database.CollectionCursor;
import database.SQLServerDatabase;

/**
 * @author smitht
 *
 */
public class PeakHistogramChartArea 
	extends AbstractMetricChartArea implements chartlib.Zoomable 
{
	private final List<HistogramDataset> collectionHistograms 
			= new LinkedList<HistogramDataset>();
	private static float binWidth = 0.01f; // should be adjustable.
	private final static int maxMZ = 500; // ugly!  should be fixed sometime!
	
	public PeakHistogramChartArea() {
		super();
	}
	
	public PeakHistogramChartArea(HistogramDataset d) {
		super();
		collectionHistograms.add(d);
	}
	


	@Override
	protected void drawData(Graphics2D g2d) {
		if (g2d == null) { System.out.println("graphics object is null!!");
							return; }
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setClip(getDataAreaBounds());
		for (HistogramDataset dataset : collectionHistograms) {
			float R = dataset.color.getRed() / 255f,
			      G = dataset.color.getGreen() / 255f,
			      B = dataset.color.getBlue() / 255f;
			
			float factor = 60f / (float) dataset.count;
			
			for (int mz = (int) Math.floor(xAxis.getMin());
					mz < xAxis.getMax() + 1; mz++) 
			{
				// revision with bars for the 0s: 1.7
				
//				float opacity;
//				if (dataset.hists[mz] == null) {
//					opacity = 1;
//				} else {
//					opacity = ((float) dataset.count 
//									- dataset.hists[mz].getHitCount()
//								) / dataset.count;
//				}
//				g2d.setColor(new Color(R, G, B, opacity));
//				g2d.fillRect(2 * mz, graphHeight + 2, 2, 10);
				
				if (dataset.hists[mz] == null) continue;
				float binWidth = dataset.hists[mz].getBinWidth();
				
				for (int i = (int) Math.floor(yAxis.getMin());
						i < yAxis.getMax() / binWidth + 1; i++) 
				{
					if (dataset.hists[mz].getCountAtIndex(i) > 1) {
						g2d.setColor(new Color(R,G,B,
//							min(dataset.hists[mz].getCountAtIndex(i) * paintIncrement,
//							    1)));
//						g.fillOval(2*mz, graphHeight - i, 2, 2);
							min(factor * ((float) dataset.hists[mz].getCountAtIndex(i)),
								1)));
						Rectangle2D.Float r = new Rectangle2D.Float(
								XAbs(mz), 
								YAbs(dataset.hists[mz].getIndexMin(i)),
								XLen(1), YLen(binWidth));
						g2d.fill(r);
						g2d.draw(r);
					}
				}
			}
		}
	}

	public double[] getXRange() {
		return new double[] { getXMin(), getXMax() };
	}

	public void packData(boolean packX, boolean packY) {
		if (packX) {
			int xmin = 0; // assuming this about the data.  ooo, bad.
			int xmax = Integer.MIN_VALUE;
			for (HistogramDataset ds : collectionHistograms) {
				ChainingHistogram[] hists = ds.hists;
				for (int i = 0; i < hists.length; i++) {
					if (hists[i] != null) {
						xmax = i;
					}
				}
			}

			if (xmax > xmin) { 
				xAxis.setRange(xmin, xmax);
			}
		}
		
		if (packY) {
			// be very lazy and say it's always 0..1
			yAxis.setRange(0, 1);
		}
	}
	
	public static void main(String[] args) {
		JFrame grr = new JFrame("woopdy doo");
		grr.setLayout(new BorderLayout());
		
		PeakHistogramChartArea p = new PeakHistogramChartArea(24);
		p.setAxisBounds(0, 400, 0, 1);
		ZoomableChart z = new ZoomableChart(p);
		
		grr.getContentPane().add(z,BorderLayout.CENTER);
		grr.validate();
		grr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		grr.setPreferredSize(new Dimension(400, 400));
		grr.pack();
		grr.setVisible(true);
	}

	public void addDataset(HistogramDataset newSet) {
		collectionHistograms.add(newSet);
	}
	
	public boolean removeDataset(HistogramDataset dset) {
		return collectionHistograms.remove(dset);
	}

	public PeakHistogramChartArea(CollectionCursor particleCursor) {
		super();
		addDataset(analyseCollection(particleCursor, Color.BLACK));
	}

	public PeakHistogramChartArea(int collID) {
		SQLServerDatabase db = getDB();
		collection.Collection coll = db.getCollection(collID);
		CollectionCursor particleCursor = db.getBinnedCursor(coll);
		addDataset(analyseCollection(particleCursor, Color.BLACK));
	}

	//	/**
	//	 * Gets called *before* the constructor... even before the static stuff like
	//	 * initializing chartArea!  ksjdfkajsdkfjaksjdfkj
	//	 */
	//	protected JPanel createChartPanel() {
	//		JPanel foo = new JPanel();
	//		chartArea.setAxisBounds(0, 400, 0, 1);
	//		chartArea.setPreferredSize(new Dimension(400, 400));
	//		foo.add(chartArea);
	//		foo.validate();
	//		return foo;
	//	}
	
	private SQLServerDatabase getDB() {
		if (gui.MainFrame.db != null) {
			return gui.MainFrame.db;
		} else {
			SQLServerDatabase db = new SQLServerDatabase();
			db.openConnection();
			return db;
		}
	}

	public static HistogramDataset analyseCollection
		(CollectionCursor particleCursor, Color c) 
	{
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
					histograms[p.key] = new ChainingHistogram(binWidth);
				}
				histograms[p.key].addPeak(p.value, pInfo);
			}
		}
		return new HistogramDataset(partnum, histograms, c);
	}


}

