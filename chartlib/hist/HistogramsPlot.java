package chartlib.hist;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D.Double;
import java.sql.SQLException;

import javax.swing.*;

import chartlib.Zoomable;
import chartlib.ZoomableChart;

import ATOFMS.ParticleInfo;
import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import database.CollectionCursor;
import database.SQLServerDatabase.BPLOnlyCursor;
import database.SQLServerDatabase;
import experiments.Tuple;

/**
 * Need to figure out how to do positive and negative spectra.  Maybe
 * put HistogramDataset generation here, and split them up into two datasets,
 * pos/neg.  Then make two of the chart areas.
 * <p>
 * Sure.
 * <p>
 * Then, make a HistogramsButtonPanel or something, 
 * @author smitht
 *
 */

public class HistogramsPlot extends JPanel implements ActionListener, Zoomable {
	private HistogramsChartArea histArea;
	private ZeroHistogramChartArea zerosArea;
	
	private final static int maxMZ = 500; // ugly!  should be fixed sometime!
	private static float binWidth = 0.01f; // fix!
	
	public HistogramsPlot(int collID) throws SQLException {
		SQLServerDatabase db = getDB();
		collection.Collection coll = db.getCollection(collID);
		SQLServerDatabase.BPLOnlyCursor particleCursor = db.getBPLOnlyCursor(coll);
		
		addDataset(analyseCollection(particleCursor, Color.BLACK));
		
		this.setLayout(new BorderLayout());
		this.add(histArea, BorderLayout.CENTER);
		this.add(zerosArea, BorderLayout.SOUTH);
		this.validate();
	}
	
	
	public static SQLServerDatabase getDB() {
		if (gui.MainFrame.db != null) {
			return gui.MainFrame.db;
		} else {
			SQLServerDatabase db = new SQLServerDatabase();
			db.openConnection();
			return db;
		}
	}


	public static HistogramDataset analyseCollection
		(BPLOnlyCursor particleCursor, Color c) 
		throws SQLException
	{
		BinnedPeakList peakList;
		int partnum = 0;
	
		final ChainingHistogram[] histograms = new ChainingHistogram[maxMZ];
		
		while (particleCursor.hasNext()) {
			Tuple<Integer, BinnedPeakList> t = particleCursor.next();
			peakList = t.getValue();
			peakList.normalize(DistanceMetric.CITY_BLOCK);
	
			++partnum;
			
			for (BinnedPeak p : peakList) {
				if (p.key >= maxMZ || p.key < 0)
					continue;
				if (histograms[p.key] == null) {
					histograms[p.key] = new ChainingHistogram(binWidth);
				}
				histograms[p.key].addPeak(p.value, peakList);
			}
		}
		
		particleCursor.close();
		
		return new HistogramDataset(partnum, histograms, c);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public Double getDataValueForPoint(Point p) {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] getXRange() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInDataArea(Point p) {
		// TODO Auto-generated method stub
		return false;
	}

	private void addDataset(HistogramDataset dataset) {
		// TODO Auto-generated method stub
		if (histArea == null) {
			histArea = new HistogramsChartArea(dataset);
			histArea.setAxisBounds(0, 400, 0, 1);
		} else {
			histArea.addDataset(dataset);
		}
		
		if (zerosArea == null) {
			zerosArea = new ZeroHistogramChartArea(dataset);
			zerosArea.setXAxisBounds(0, 400);
		} else {
			//zerosArea.addDataset(dataset);  // not implemented yet.
			System.err.println(
			"HistogramsPlot: zerosArea doesn't know about multiple datasets yet.");
		}
	}

	public void packData(boolean packX, boolean packY) {
		// TODO Auto-generated method stub
		
	}

	public void setXAxisBounds(double xmin, double xmax) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	

	


}
