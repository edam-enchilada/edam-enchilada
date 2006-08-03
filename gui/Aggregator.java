package gui;

import java.util.*;

import javax.swing.*;

import collection.AggregationOptions;
import collection.Collection;
import database.*;
import externalswing.SwingWorker;

public class Aggregator {
	private JFrame parentFrame;
	private String timeBasisSQLstring;
	private boolean baseOnCollection;
	private InfoWarehouse db;
	
	private Calendar start, end, interval;
	private Collection basisCollection;
	
	private Aggregator(JFrame parentFrame, InfoWarehouse db, boolean baseOnCollection) {
		this.parentFrame = parentFrame;
		this.baseOnCollection = baseOnCollection;
		this.db = db;
	}
	
	public Aggregator(JFrame parentFrame, InfoWarehouse db, Collection basisCollection) {
		this(parentFrame, db, true);
		
		this.basisCollection = basisCollection;
	}
	
	public Aggregator(JFrame parentFrame, InfoWarehouse db, Calendar start, Calendar end, Calendar interval) {
		this(parentFrame, db, false);
		
		this.start = start;
		this.end = end;
		this.interval = interval;
		baseOnCollection = false;
	}

	/**
	 * @param collections the collections to be aggregated
	 * @return initial progressBar for createAggregateTimeSeries
	 * 
	 * This method should always be called before createAggregateTimeSeries;
	 * it generates and displays the initial modal progress bar.
	 * This is done outside of the actual createAggregateTimeSeries method
	 * because display of this method needs to happen from the EDT before any
	 * other events have the opportunity to get in between.
	 */
	
	public ProgressBarWrapper createAggregateTimeSeriesPrepare(Collection[] collections) {
		ProgressBarWrapper progressBar = 
			new ProgressBarWrapper(parentFrame, "Retrieving Valid M/Z Values", collections.length);
		progressBar.constructThis();
		return progressBar;
	}

	/**
	 * @param syncRootName name for time series
	 * @param collections the collections to be aggregated
	 * @param progressBar the initial progress bar, created via createAggregateTimeSeriesPrepare
	 * @param mainFrame main Enchilada frame containing tree to be updated
	 * @return collection id for the new collection
	 * 
	 * This method should always be called from outside the EDT, e.g. via
	 * SwingWorker.
	 */
	public int createAggregateTimeSeries(String syncRootName,
			Collection[] collections, ProgressBarWrapper progressBar,
			MainFrame mainFrame) {
		int rootCollectionID = db.createEmptyCollection("TimeSeries", 1, syncRootName, "", "");
		return createAggregateTimeSeries(rootCollectionID, collections,
				progressBar, mainFrame);
	}

	/**
	 * @param rootCollectionID the CollectionID for the new time-series data
	 * @param collections the collections to be aggregated
	 * @return
	 * 
	 * This method should be private: it depends on the above overloaded version.
	 */
	private int createAggregateTimeSeries(final int rootCollectionID,
			final Collection[] collections,
			final ProgressBarWrapper progressBar1, MainFrame mainFrame) {
		final int[][] mzValues = new int[collections.length][];
		final int[] numSqlCalls = {1};

		//get the valid m/z values for each collection individually
		Date s,e; // start and end dates.
		for (int i = 0; i < collections.length; i++) {
			final String text = "Retrieving Valid M/Z Values for Collection # "+(i+1)+" out of "+collections.length;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressBar1.increment(text);
				}
			});
			Collection curColl = collections[i];
			AggregationOptions options = curColl.getAggregationOptions();
			if (options == null)
				curColl.setAggregationOptions(options = new AggregationOptions());				
			if (curColl.getDatatype().equals("ATOFMS") || 
					curColl.getDatatype().equals("AMS")) {
				if (baseOnCollection) {
					Calendar startDate = new GregorianCalendar();
					Calendar endDate = new GregorianCalendar();
					Collection[] array = {basisCollection};
					long begin = new Date().getTime();
					db.getMaxMinDateInCollections(array,startDate,endDate);
					long end = new Date().getTime();
					System.out.println("getMaxMinDateInCollections: "+(end-begin)/1000+" sec.");
					s = startDate.getTime();
					e = endDate.getTime();
				}
				else {
					s = start.getTime();
					e = end.getTime();
				}
				long begin = new Date().getTime();
				mzValues[i] = db.getValidSelectedMZValuesForCollection(curColl, s, e);	
				long end = new Date().getTime();
				System.out.println("getValidMZValuesForCollection: "+(end-begin)/1000+" sec.");
				if (mzValues[i] != null)
					numSqlCalls[0] += mzValues[i].length;		
				if (options.produceParticleCountTS)
					numSqlCalls[0]++;
			} else if (curColl.getDatatype().equals("TimeSeries")) {
				numSqlCalls[0]++;
			}
		}
		if(progressBar1.wasTerminated()){
			progressBar1.disposeThis();
			return -1;
		}
		final ProgressBarWrapper progressBar2 = 
			new ProgressBarWrapper(parentFrame, "Aggregating Time Series", numSqlCalls[0]+1);
		
		progressBar2.constructThis();
		
		// By constructing progressBar2 BEFORE disposing progressBar1, the
		// request for making progressBar2 is in the EDT queue waiting to go
		// when progressBar1 is finally disposed. If we dispose progressBar1
		// first (which is modal), we technically run the risk that another
		// event will get in between the two.
		progressBar1.disposeThis();
		
		//actually do the aggregation
		for (int i = 0; i < collections.length; i++) {
			final String name = collections[i].getName();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressBar2.increment("Constructing time basis for "+name);
				}
			});
			long begin = new Date().getTime();
			if (baseOnCollection)
				db.createTempAggregateBasis(collections[i],basisCollection);
			else {
				db.createTempAggregateBasis(collections[i],start,end,interval);
			}
			if(progressBar2.wasTerminated()){
				progressBar2.disposeThis();
				return -1;
			}
			long end = new Date().getTime();
			System.out.println("createTempAggBasis: "+(end-begin)/1000+" sec.");
			begin = new Date().getTime();
			db.createAggregateTimeSeries(progressBar2, rootCollectionID, collections[i], mzValues[i]);
			if(progressBar2.wasTerminated()){
				progressBar2.disposeThis();
				return -1;
			}
			end = new Date().getTime();
			System.out.println("createAggregateTimeSeries: "+(end-begin)/1000+" sec.");
			db.deleteTempAggregateBasis();
		}
		if(progressBar2.wasTerminated()){
			progressBar2.disposeThis();
			return -1;
		}
		mainFrame.updateSynchronizedTree(rootCollectionID);
		
		progressBar2.disposeThis();
		
		return rootCollectionID;
	}
}
