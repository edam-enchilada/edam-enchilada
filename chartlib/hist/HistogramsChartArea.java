/**
 * 
 */
package chartlib.hist;

import java.awt.*;
import java.awt.geom.Point2D;
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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import database.CollectionCursor;
import database.SQLServerDatabase;

/**
 * @author smitht
 *
 */
public class HistogramsChartArea 
	extends AbstractMetricChartArea implements chartlib.Zoomable, ChangeListener 
{
	private final List<HistogramDataset> collectionHistograms 
			= new LinkedList<HistogramDataset>();
	
	private float brightness = 60;

	

	public HistogramsChartArea() {
		super();
		H_AXIS_PADDING = 5;
		H_TITLE_PADDING = 0;
		
		setTitleX("");
		setMinimumSize(new Dimension(300, 200));
		setPreferredSize(new Dimension(300, 200));
		
		xAxis.setThickness(0.5f);
		yAxis.setThickness(0.5f);
	}

	@Override
	protected void drawTickLabels(Graphics2D g2d) {
		yAxis.drawTickLabels(g2d);
		// don't draw x axis labels, since there's another graph just a few
		// pixels away.
	}
	
	
	public HistogramsChartArea(HistogramDataset d) {
		this();
		collectionHistograms.add(d);
	}
	


	/**
	 * Draws the special histogram.
	 * <p>
	 * Uses the clip from the Graphics2D object to decide how much of the
	 * histogram needs to be redrawn.
	 */
	@Override
	protected void drawData(Graphics2D g2d) {
		// getDataValueForPoint
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.clip(getDataAreaBounds());
		
		Rectangle clip = g2d.getClip().getBounds();
		
		Point2D.Double min 
			= getDataValueForPoint(new Point(clip.x, clip.y + clip.height));
		Point2D.Double max 
			= getDataValueForPoint(new Point(clip.x + clip.width, clip.y));
		
//		System.out.println("Repainting from " +min+" to "+max);
		
		
		for (HistogramDataset dataset : collectionHistograms) {
			float R = dataset.color.getRed() / 255f,
			      G = dataset.color.getGreen() / 255f,
			      B = dataset.color.getBlue() / 255f;
			
			float factor = brightness / (float) dataset.count;
			
			/*
			 * We define the y axis to be in integer-valued chunks.
			 */
			for (int mz = (int) min.x; mz <= max.x; mz++) 
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
				
				if (mz >= dataset.hists.length) break;
				if (dataset.hists[mz] == null) continue;
				float binWidth = dataset.hists[mz].getBinWidth();
				
				int maxInd = (int) (max.y / binWidth) + 2;
				int minInd = (int) (min.y / binWidth);
				
				/*
				 * This iterates over histogram bins, by index.
				 */
				for (int i = minInd; i < maxInd; i++) 
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

	public int getCountAt(int mz, float relArea) {
		if (collectionHistograms != null && collectionHistograms.size() > 0)
			if (collectionHistograms.get(0).hists.length > mz)
				if (collectionHistograms.get(0).hists[mz] != null)
					return collectionHistograms.get(0).hists[mz].getCountAt(relArea);
		return 0;
	}
	
	public double[] getVisibleXRange() {
		return new double[] { getXMin(), getXMax() };
	}

	public void packData(boolean packX, boolean packY, boolean forceY) {
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
	

	public void addDataset(HistogramDataset newSet) {
		collectionHistograms.add(newSet);
	}
	
	public boolean removeDataset(HistogramDataset dset) {
		return collectionHistograms.remove(dset);
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
		repaint();
	}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
//		this.setBrightness(1f / (source.getValue() / 200f));
		this.setBrightness(source.getValue() * source.getValue());
	}
}

