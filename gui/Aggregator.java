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
			new ProgressBarWrapper(parentFrame, "Retrieving Valid M/Z Values", collections.length);
		
		final SwingWorker worker2 = new SwingWorker() {
			public Object construct() {
				Date s,e; // start and end dates.
				for (int i = 0; i < collections.length; i++) {
					progressBar1.increment("Retrieving Valid M/Z Values for Collection # "+(i+1)+" out of "+collections.length);
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
						mzValues[i] = db.getValidMZValuesForCollection(curColl, s, e);	
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
				progressBar1.disposeThis();
				
				return null;
			}
		};
		// XXX: this demonstrates a race condition.. oops!
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
					long begin = new Date().getTime();
					if (baseOnCollection)
						db.createTempAggregateBasis(collections[i],basisCollection);
					else {
						db.createTempAggregateBasis(collections[i],start,end,interval);
					}
					long end = new Date().getTime();
					System.out.println("createTempAggBasis: "+(end-begin)/1000+" sec.");
					begin = new Date().getTime();
					db.createAggregateTimeSeries(progressBar2, rootCollectionID, collections[i], mzValues[i]);
					end = new Date().getTime();
					System.out.println("createAggregateTimeSeries: "+(end-begin)/1000+" sec.");
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
