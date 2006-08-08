package chartlib.hist;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import database.SQLServerDatabase;
import experiments.Tuple;
import externalswing.ProgressTask;


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
	
	public static HistogramDataset[] analyseCollection(int collID, Color c) 
		throws SQLException
	{
		SQLServerDatabase db = HistogramsPlot.getDB();
		collection.Collection coll = db.getCollection(collID);
		SQLServerDatabase.BPLOnlyCursor particleCursor = db.getBPLOnlyCursor(coll);
		
		HistogramDataset[] ret = analyseBPLs(particleCursor, c);
		
		particleCursor.close();
		
		return ret;
	}
	
	public static HistogramDataset[] 
	    analyseBPLs(Iterator<Tuple<Integer, BinnedPeakList>> iter, Color c)
	{
		BinnedPeakList peakList;
		int partnum = 0;
	
		ChainingHistogram[] histograms, posHists = new ChainingHistogram[maxMZ],
			negHists = new ChainingHistogram[maxMZ];
		
		while (iter.hasNext()) {
			Tuple<Integer, BinnedPeakList> t = iter.next();
			peakList = t.getValue();
			peakList.normalize(DistanceMetric.EUCLIDEAN_SQUARED);
	
			++partnum;
			
			for (BinnedPeak p : peakList) {
				if (p.key >= maxMZ)
					continue;
				else if (p.key >= 0)
					histograms = posHists;
				else if (p.key > - maxMZ) {
					histograms = negHists;
					p.key = - p.key;
				} else
					continue;
				if (histograms[p.key] == null) {
					histograms[p.key] = new ChainingHistogram(binWidth);
				}
				histograms[p.key].addPeak(p.value, t.getKey());
			}
		}
		
		return new HistogramDataset[] {
				new HistogramDataset(partnum, posHists, c),
				new HistogramDataset(partnum, negHists, c)
		};
	}

	
	/**
	 * Warning!  As currently implemented, this will indeed take the intersection,
	 * but the count of particles in the resulting dataset will simply be the length
	 * of the list of AtomIDs, rather than the actual count of particles.  To
	 * fix this, change the HashSet to a HashMap<Integer, Boolean>, set an atom's
	 * entry to true if it is used, and return the count of true ones.  But that's
	 * more complicated.
	 * 
	 */
	public static HistogramDataset[] intersect(HistogramDataset[] spectra, 
			ArrayList<Integer> atomIDs)
	{
		HashSet<Integer> keep = new HashSet<Integer>(atomIDs);
		HistogramDataset[] intersected = new HistogramDataset[spectra.length];
		
		// probably once each for positive and negative spectra
		for (int ds = 0; ds < spectra.length; ds++) {
			intersected[ds] = new HistogramDataset(atomIDs.size(),
					new ChainingHistogram[maxMZ], spectra[ds].color);
			
			for (int mz = 0; mz < spectra[ds].hists.length; mz++) {
				if (spectra[ds].hists[mz] == null) continue;
				
				intersected[ds].hists[mz] = new ChainingHistogram(binWidth);
				ChainingHistogram src = spectra[ds].hists[mz], 
					dest = intersected[ds].hists[mz];
				
				ArrayList<Integer> srcList, destList;
				for (int index = 0; index < src.size(); index++) {
					srcList = src.get(src.getIndexMiddle(index));
					if (srcList == null) continue;
					
					destList = new ArrayList<Integer>();
					
					for (Integer id : srcList)
						if (keep.contains(id))
							destList.add(id);

					dest.setListAt(destList, src.getIndexMiddle(index));
				}
			}
		}
		return intersected;
	}
	
	public static HistogramDataset[] getSelection(HistogramDataset[] spectra,
			List<BrushSelection> selection) {
		return null;
	}
	
	@Override
	public boolean equals(Object thatObject) {
		if (thatObject == null || !(thatObject instanceof HistogramDataset)) 
			return false;
		HistogramDataset that = (HistogramDataset) thatObject;
		
		if (this.count != that.count) return false;
		
		for (int i = 0; i < hists.length; i++) {
			if (hists[i] == null || hists[i].getHitCount() == 0)
				if (that.hists[i] == null || that.hists[i].getHitCount() == 0) {
					continue;
				} else {
					return false;
				}
			
			if (!(hists[i].equals(that.hists[i]))) return false;
		}
		
		return true;
	}
	
}
