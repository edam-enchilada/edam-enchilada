package gui;

import java.text.*;
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
		int numSqlCalls = 1;

		for (int i = 0; i < collections.length; i++) {
			Collection curColl = collections[i];
			AggregationOptions options = curColl.getAggregationOptions();
			if (options == null)
				curColl.setAggregationOptions(options = new AggregationOptions());
			
			if (curColl.getDatatype().equals("ATOFMS")) {
				mzValues[i] = db.getValidMZValuesForCollection(curColl);
				
				if (mzValues[i] != null)
					numSqlCalls += mzValues[i].length;
				
				if (options.produceParticleCountTS)
					numSqlCalls++;
			} else if (curColl.getDatatype().equals("TimeSeries")) {
				numSqlCalls++;
			}
		}
		
		final ProgressBarWrapper progressBar = 
			new ProgressBarWrapper(parentFrame, "Aggregating Time Series", numSqlCalls);
		
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				progressBar.increment("Constructing time basis... (this could take a while)");
				
				if (baseOnCollection)
					db.createTempAggregateBasis(basisCollection);
				else
					db.createTempAggregateBasis(start, end, interval);

				
				for (int i = 0; i < collections.length; i++)
					db.createAggregateTimeSeries(progressBar, rootCollectionID, collections[i], mzValues[i]);

				progressBar.disposeThis();
				
				return null;
			}
		};
		worker.start();
		
		progressBar.constructThis();
		
		db.deleteTempAggregateBasis();
		return rootCollectionID;
	}
}
