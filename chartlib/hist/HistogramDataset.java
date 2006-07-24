package chartlib.hist;

import java.awt.Color;
import java.sql.SQLException;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import database.SQLServerDatabase;
import experiments.Tuple;


public class HistogramDataset {
	public ChainingHistogram[] hists;
	public int count;
	public Color color;
	
	private final static int maxMZ = 500; // ugly!  should be fixed sometime!
	private static float binWidth = 0.01f; // fix!
	
	public HistogramDataset(int count, ChainingHistogram[] hists, Color color) {
		this.count = count;
		this.hists = hists;
		this.color = color;
	}
	
	public HistogramDataset(int collID, Color c) 
		throws SQLException
	{
		SQLServerDatabase db = HistogramsPlot.getDB();
		collection.Collection coll = db.getCollection(collID);
		SQLServerDatabase.BPLOnlyCursor particleCursor = db.getBPLOnlyCursor(coll);
		
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
		
		this.count = partnum;
		this.hists = histograms;
		this.color = c;
	}

	
}
