package dataImporters;

import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;


import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import collection.Collection;
import database.Database;
import database.InfoWarehouse;
import errorframework.*;
import externalswing.SwingWorker;
import gui.AMSTableModel;
import gui.ImportAMSDataDialog;
import gui.MainFrame;
import gui.ProgressBarWrapper;

public class AMSDataSetImporter {
	private AMSTableModel table;
	private Window mainFrame;
	private ImportAMSDataDialog ams;
	private boolean parent;
	
	//Table values - used repeatedly.
	private int rowCount;
	private String datasetName = "";
	private String timeSeriesFile, massToChargeFile;
	private ArrayList<Date> timeSeries = null;
	private ArrayList<Double> massToCharge = null;
	
	private String dense;
	private ArrayList<String> sparse;
	
	// Progress Bar variables
	protected JDialog waitBarDialog = null;
	protected JProgressBar pBar = null;
	protected JLabel pLabel = null;
	protected int particleNum;
	protected int totalParticles;
	private Container parentContainer;
	
	/* contains the collectionID and particleID */
	private int[] id;
	protected int positionInBatch, totalInBatch;
	
	/* Database object */
	InfoWarehouse db;
	
	/* Lock to make sure database is only accessed in one batch at a time */
	private static Integer dbLock = new Integer(0);
	
	/* for time conversion */
	private final Calendar startCalendar = new GregorianCalendar(1904,1,1,0,0,0);
	private Calendar convertedCalendar;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final float SEC_PER_YEAR  = 31556926;
	
	private Scanner readData; // for reading the dataset;
		
	/**
	 * 
	 * Constructor.  Sets the particle table for the importer.
	 * @param amsTableModel - particle table model.
	 */
	public AMSDataSetImporter(AMSTableModel t, Window mf, ImportAMSDataDialog dialog) {
		table = t;
		mainFrame = mf;
		ams = dialog;
		db = MainFrame.db;
	}
	
	/**
	 * Loops through each row, collects the information, and processes the
	 * datasets row by row.
	 * @throws WriteException 
	 */
	public void collectTableInfo() throws DisplayException, WriteException {
		
		rowCount = table.getRowCount()-1;
		totalInBatch = rowCount;
		//Loops through each dataset and creates each collection.
		for (int i=0;i<rowCount;i++) {
			try {
				// Table values for this row.
				datasetName = (String)table.getValueAt(i,1);
				timeSeriesFile = (String)table.getValueAt(i,2);
				massToChargeFile = (String)table.getValueAt(i,3);
				
				//System.out.println(datasetName);
				//System.out.println(timeSeriesFile);
				//System.out.println(massToChargeFile);
				
				positionInBatch = i + 1;
				// Call relevant methods
					processDataSet(i);

				// update the internal atom order table;
				db.updateAncestors(db.getCollection(id[0]));
			} catch (DisplayException e) {
				throw new DisplayException(datasetName + " failed to import. Exception: "+e.toString());
			} catch (WriteException e) {
				throw new WriteException(datasetName + "failed to import.  Exception: " + e.toString());
			}
		}
	}
	
	/**
	 * Dumps data from datasetInfo, timeSeriesFile, and massToChargeFile
	 * into database.
	 * 
	 * // NOTE: Datatype is already in the db.
	 */
	public void processDataSet(int index) throws DisplayException, WriteException {
		boolean skipFile = false;
		String[] AMS_tables = {"AMSAtomInfoDense", "AtomMembership", "DataSetMembers", "AMSAtomInfoSparse"};
		final Database.Data_bulkBucket ams_buckets = ((Database)db).getDatabulkBucket(AMS_tables);
		
		//put time series file and mz file into an array, since they will
		//be accessed in the same way for every atom.
		Scanner readTimeSeries;
		try {
			readTimeSeries = new Scanner(new File(timeSeriesFile));
		} catch (FileNotFoundException e1) {
			throw new WriteException(timeSeriesFile+" was not found.");
		}
		readTimeSeries.next(); // skip name
		timeSeries = new ArrayList<Date>();
		BigInteger maxInt = new BigInteger(""+Integer.MAX_VALUE);
		BigInteger bigInt;
		String tempStr;
		BigInteger prevBigInt = null, temp = null;
		while (readTimeSeries.hasNext()) {
			tempStr = readTimeSeries.next();
			if (tempStr.indexOf('.') != -1) {
				tempStr = tempStr.substring(0,tempStr.indexOf('.'));
				bigInt = new BigInteger(""+tempStr);
				bigInt = bigInt.add(new BigInteger(""+1));
			}
			else
				bigInt = new BigInteger(""+tempStr);
			//if this is the first time, then calculate it using the while loop.
			if (prevBigInt == null) {
				prevBigInt = bigInt;
				convertedCalendar = (Calendar) startCalendar.clone();
				while (bigInt.compareTo(maxInt) == 1) {
					convertedCalendar.add(Calendar.SECOND, Integer.MAX_VALUE);
					bigInt = bigInt.subtract(maxInt);
				}
				convertedCalendar.add(Calendar.SECOND, bigInt.intValue());
			}
			// else, subtract it from previous time to calculate.
			else {
				temp = bigInt.subtract(prevBigInt);
				convertedCalendar.add(Calendar.SECOND, temp.intValue());
				prevBigInt = bigInt;
			}
			//System.out.println(convertedCalendar.getTime().toString());
			timeSeries.add(convertedCalendar.getTime());
		}
		readTimeSeries.close();
		Scanner readMZ;
		try {
			readMZ = new Scanner(new File(massToChargeFile));
		} catch (FileNotFoundException e1) {
			throw new WriteException(massToChargeFile+" was not found.");
		}
		readMZ.next(); // skip name
		massToCharge = new ArrayList<Double>();
		while (readMZ.hasNext()) {
			massToCharge.add(readMZ.nextDouble());
		}
		readMZ.close();
		
		// create empty collection.
		try {
			id = db.createEmptyCollectionAndDataset("AMS",0,getName(),"AMS import",
					"'"+datasetName+"','"+timeSeriesFile+"','"+massToChargeFile+"'");
		} catch (FileNotFoundException e1) {
			throw new WriteException("Attempt to get name for collection not" +
					" found because the file was not found.");
		}
	
		
		// get total number of particles for progress bar.
		try {
			readData = new Scanner(new File(datasetName));
		} catch (FileNotFoundException e1) {
			throw new WriteException(datasetName+"was not found.");
		}
		readData.next();//skip name
		int tParticles = 0;
		while (readData.hasNext()) {
			for (int i = 0; i < massToCharge.size(); i++)
				readData.nextDouble();
			tParticles++;
		}
		readData.close();
		final int totalParticles = tParticles;
		System.out.println("total particles: " + tParticles);

		final ProgressBarWrapper progressBar = 
			new ProgressBarWrapper((JFrame)mainFrame, "Importing AMS Datasets", (totalParticles/10)+1);

		final SwingWorker worker = new SwingWorker() {
			
			public Object construct() {
				try{
					Collection destination = db.getCollection(id[0]);
					
					readData = new Scanner(new File(datasetName));
					readData.next(); // skip name

					particleNum = 0;
					int nextID = db.getNextID();
					while (readData.hasNext()) { // repeat until end of file.
						if(particleNum % 10 == 0 && particleNum >= 10) 
							progressBar.increment("Importing Particle # "+particleNum+" out of "+totalParticles);
						read(particleNum, nextID);
						if (sparse != null && sparse.size() > 0) {
							//db.insertParticle(dense,sparse,destination,id[1],nextID);
							((Database)db).saveDataParticle(dense,sparse,destination,id[1],nextID, ams_buckets);
							nextID++;
						}
						particleNum++;
					}
					((Database)db).BulkInsertDataParticles(ams_buckets);
					((Database)db).updateInternalAtomOrder(destination);
					progressBar.increment("Updating Ancestors...");
					db.updateAncestors(destination);
					progressBar.disposeThis();
				}catch (Exception e) {
					try {
						e.printStackTrace();
						final String exception = e.toString();
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run()
							{
								// don't throw an exception here because we want to keep going:
								ErrorLogger.writeExceptionToLog("Importing","Corrupt datatset file or particle: "+ exception);
							}
						});
					} catch (Exception e2) {
						e2.printStackTrace();
						// don't throw exception here because we want to keep going:
						ErrorLogger.writeExceptionToLog("Importing","ParticleException: "+e2.toString());
					}
				}
				return null;
			}
			
		};
		worker.start();	
		progressBar.constructThis();
	}
	
	/**
	 * This method loops through the table and checks to make sure that there is a
	 * are non-null values for all datasets.
	 */
	public void errorCheck() throws DisplayException{
		String name, timeFile, mzFile;
		File d,m,t;
		for (int i=0;i<table.getRowCount()-1;i++) {
			name = (String)table.getValueAt(i,1);
			timeFile = (String)table.getValueAt(i,2);
			mzFile = (String)table.getValueAt(i,3);
			//Check to make sure that .par and .cal files are present.
			if (name.equals("data file") || name.equals("") 
					|| timeFile.equals("time series file") 
					|| timeFile.equals("")
					|| mzFile.equals("mass to charge file") 
					|| mzFile.equals("")) {
				throw new DisplayException("You must enter a data file, a time series file," +
						" and a mass to charge file at row # " + (i+1) + ".");
			}
			
			// check to make sure all files are valid:
			d = new File(name);
			t = new File(timeFile);
			m = new File(mzFile);
			if (!d.exists() || !t.exists() || !m.exists()) {
				throw new DisplayException("One of the files does not exist at row # " + (i+1) +".");
			}
		}
	}
	
	/**
	 * Removes the file extension from datasetName and returns the 
	 * collection name.
	 * @return
	 * @throws FileNotFoundException 
	 */
	public String getName() throws FileNotFoundException {
		Scanner scan = new Scanner(new File(datasetName));
		String toReturn = scan.next();
		scan.close();
		System.out.println("Importing: " + toReturn);
		return toReturn;

	}
	
	/** 
	 * constructs a dense list and a sparse list for the atom at the given
	 * position in the data file.  These are global variables.
	 * @return
	 */
	public void read(int particleNum, int nextID) {
		// populate dense string
		//dense = "'"+dateFormat.format(timeSeries.get(particleNum))+"'";
		dense = dateFormat.format(timeSeries.get(particleNum));
		sparse = new ArrayList<String>();
		double tempNum;
		for (int i = 0; i < massToCharge.size(); i++) {
			tempNum = readData.nextDouble();
			//System.out.println(tempNum);
			if (tempNum != 0.0 && tempNum != -999.0)
				sparse.add(massToCharge.get(i)+","+tempNum);
		}	
	}
}
