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
	
	public int createAggregateTimeSeries(String syncRootName, Collection[] collections) {
		int rootCollectionID = db.createEmptyCollection("TimeSeries", 1, syncRootName, "", "");
		return createAggregateTimeSeries(rootCollectionID, collections);
	}

	public int createAggregateTimeSeries(final int rootCollectionID, final Collection[] collections) {	
		final int[][] mzValues = new int[collections.length][];
		final int[] numSqlCalls = {1};

		final ProgressBarWrapper progressBar1 = 
			new ProgressBarWrapper(parentFrame, "Retreiving Valid M/Z Values", collections.length);
		
		final SwingWorker worker2 = new SwingWorker() {
			public Object construct() {
				for (int i = 0; i < collections.length; i++) {
					progressBar1.increment("Retreiving Valid M/Z Values for Collection # "+(i+1)+" out of "+collections.length);
					Collection curColl = collections[i];
					AggregationOptions options = curColl.getAggregationOptions();
					if (options == null)
						curColl.setAggregationOptions(options = new AggregationOptions());				
					if (curColl.getDatatype().equals("ATOFMS")) {
						mzValues[i] = db.getValidMZValuesForCollection(curColl);				
						if (mzValues[i] != null)
							numSqlCalls[0] += mzValues[i].length;		
						if (options.produceParticleCountTS)
							numSqlCalls[0]++;
					} else if (curColl.getDatatype().equals("TimeSeries")) {
						numSqlCalls[0]++;
					}
				}
				progressBar1.disposeThis();
				
				return null;
			}
		};
		worker2.start();
		progressBar1.constructThis();
		
		final ProgressBarWrapper progressBar2 = 
			new ProgressBarWrapper(parentFrame, "Aggregating Time Series", numSqlCalls[0]+1);
		
		final SwingWorker worker1 = new SwingWorker() {
			public Object construct() {
				// iterates through the collections and creates the time series for them.
				for (int i = 0; i < collections.length; i++) {
					String name = collections[i].getName();
					progressBar2.increment("Constructing time basis for "+name);
					if (baseOnCollection)
						db.createTempAggregateBasis(collections[i],basisCollection);
					else {
						//System.out.println("** START ** "+ start.getTimeInMillis());
						//System.out.println("** END ** "+ end.getTimeInMillis());
						//System.out.println("** INTERVAL ** "+ interval.getTimeInMillis());
						db.createTempAggregateBasis(collections[i],start,end,interval);
					}
					System.out.println("AGGREGATING COLLECTION "+name);
					db.createAggregateTimeSeries(progressBar2, rootCollectionID, collections[i], mzValues[i]);
					db.deleteTempAggregateBasis();				
					}
				progressBar2.disposeThis();
				
				return null;
			}
		};
		worker1.start();
		progressBar2.constructThis();
		
		return rootCollectionID;
	}
}
