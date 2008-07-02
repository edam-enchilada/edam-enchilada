/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's Database class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
 * Greg Cipriano gregc@cs.wisc.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

/*****
 * 
 */
/******
 * 
 */

/**
 * Creates a new DataSetProcessor object for each dataset that processes the spectra and
 * creates the particle.  It passes a file name, a CalInfo object and a PeakParams object 
 * to DataSetProcessor.
 * 
 * 
 * @author ritza, Jamie Olson
 *
 */
package dataImporters;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import collection.Collection;
import database.Database;
import database.InfoWarehouse;
import errorframework.*;
import gui.SPASSTableModel;
import gui.ProgressBarWrapper;

/**Imports a SPASS type data file into the database as
 *  ATOFMS data points. 
 * 
 * @author rzeszotj with credit to AMS and ATOFMS import authors
 *
 */

public class SPASSDataSetImporter {
	private SPASSTableModel table;
	private Window mainFrame;
	private boolean parent;
	
	//Table values - used repeatedly.
	protected int rowCount;
	protected String datasetName = "";
	//protected massToCharge list, dense and sparse data strings/lists;
	protected ArrayList<Double> massToCharge = null;
	private String dense;
	private ArrayList<String> sparse;
	
	// Progress Bar variables
	public static final String TITLE = "Importing SPASS Dataset";
	protected ProgressBarWrapper progressBar;
	protected int particleNum;
	protected int totalParticles;
	
	/* contains the collectionID and particleID */
	private int[] id;
	protected int positionInBatch, totalInBatch;
	
	/* Database object */
	InfoWarehouse db;
	
	/* Lock to make sure database is only accessed in one batch at a time */
	private static Integer dbLock = new Integer(0);
	
	/* for time conversion */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private final float SEC_PER_YEAR  = 31556926;
	
	private Scanner readData; // for reading the dataset;
	
	//the parent collection to import into
	private int parentID = 0;
		
	/**
	 * Constructor.  Sets the particle table for the importer.
	 * @param SPASSTableModel - particle table model.
	 */
	public SPASSDataSetImporter(SPASSTableModel t, Window mf, InfoWarehouse db) {
		table = t; //The gui table with multiple datasets
		mainFrame = mf;
		this.db = db;
	}
	
	public SPASSDataSetImporter(SPASSTableModel t, Window mf, InfoWarehouse db, ProgressBarWrapper pbar) {
		this(t, mf, db);
		progressBar = pbar;
	}
	
	/**
	 * Loops through each gui row, collects the information, and processes the
	 * _datasets_ row by row.
	 * @throws WriteException 
	 */
	public void collectTableInfo() throws DisplayException, WriteException {

		rowCount = table.getRowCount()-1;
		totalInBatch = rowCount;
		//Loops through each dataset and creates each collection.
		for (int i=0;i<rowCount;i++) {
			try {
				// Indicate progress for this dataset
				progressBar.reset();
				progressBar.setTitle(TITLE+": "+(i+1)+" of "+rowCount);
				
				// Table values for this row.
				datasetName = (String)table.getValueAt(i,1);
				
				System.out.println(datasetName);
				
				positionInBatch = i + 1;
				// Call relevant method to read SPASS.txt
				processDataSet(i);

			} catch (DisplayException e) {
				throw new DisplayException(datasetName + " failed to import. Exception: " + e.toString());
			} catch (WriteException e) {
				throw new WriteException(datasetName + " failed to import.  Exception: " + e.toString());
			}
		}
	}
	
	/**
	 * Dumps data from particles in .txt as ATOFMS into database.
	 * 
	 * // NOTE: Datatype is already in the db.
	 * @author rzeszotj with credit to AMSimport author
	 */
	public void processDataSet(int index) throws DisplayException, WriteException {
		boolean skipFile = false;
		final String[] ATOFMS_tables = {"ATOFMSAtomInfoDense", "AtomMembership", 
				   "DataSetMembers", "ATOFMSAtomInfoSparse","InternalAtomOrder"};
		Database.Data_bulkBucket ATOFMS_buckets = ((Database)db).getDatabulkBucket(ATOFMS_tables) ;
		
		//get total number of particles for progress bar.
		progressBar.setIndeterminate(true);
		progressBar.setText("Finding number of items");
		try {
			readData = new Scanner(new File(datasetName));//.useDelimiter("\t");
		} catch (FileNotFoundException e1) {
			throw new WriteException(datasetName+" was not found.");
		}
		String headers = readData.nextLine();//grab headers for later
		
		//Count # particles by counting lines after header
		int tParticles = 0;
		while (readData.hasNextLine()) {
			readData.nextLine();
			tParticles++;
		}
		readData.close();
		final int totalParticles = tParticles;
		int progressTextStep = tParticles/20;
		System.out.println("total items: " + totalParticles);
		
		progressBar.setText("Reading data headers");
		//Skip past column labels
		Scanner readHeaders = new Scanner(headers);
		for (int i = 0; i < 6; i++)
			readHeaders.next();
	
		//Read m/z labels
		massToCharge = new ArrayList<Double>();
		while (readHeaders.hasNext()) {
			massToCharge.add(readHeaders.nextDouble());
		}
		readHeaders.close();
		System.out.println(massToCharge.size()+" mass/charge values found.");
		
		//Create empty ATOFMS collection
		id = db.createEmptyCollectionAndDataset("ATOFMS",parentID,getName(),
				"SPASS Import dummy ATOFMS",
				"'" + "SPASS" + "','" + "SPASS" + "'," +
				"0" + "," + "0"  + "," + "0" + ",0");
		
		progressBar.setMaximum((totalParticles/10)+1);
		progressBar.setIndeterminate(false);
		try{
			Collection destination = db.getCollection(id[0]);
			
			readData = new Scanner(new File(datasetName));
			readData.nextLine(); // skip headers again
			
			//Loop through particles in file
			particleNum = 0;
			int nextID = db.getNextID();
			while (readData.hasNext()) { // repeat until end of file.
				if(particleNum % 10 == 0 && particleNum >= 10) {
					String barText =
						"Importing Item # " + particleNum + " out of " 
									+ totalParticles;
					progressBar.increment(barText);
				}
				if ((particleNum % progressTextStep) == 0)
					System.out.println("Processing particle #" + particleNum);
				
				read(particleNum, nextID); //READ IN PARTICLE DATA HERE
				
				//Only copy in particles with peaks
				if (sparse != null && sparse.size() > 0) {
					((Database)db).saveDataParticle(dense,sparse,
							        destination,id[1],nextID, ATOFMS_buckets);
					nextID++;
				}
				particleNum++;
			}
			
			progressBar.setIndeterminate(true);
			progressBar.setText("Inserting Items...");
			((Database)db).BulkInsertDataParticles(ATOFMS_buckets);
			((Database)db).updateInternalAtomOrder(destination);
			
			//percolate possession of new atoms up the hierarchy
			progressBar.setText("Updating Ancestors...");
			db.propagateNewCollection(destination);
			readData.close();
		}catch (Exception e) {
			try {
				e.printStackTrace();
				final String exception = e.toString();
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run()
					{
						// don't throw an exception here because we want to keep going:
						ErrorLogger.writeExceptionToLogAndPrompt("Importing",
								"Corrupt datatset file or particle: "+ exception);
					}
				});
			} catch (Exception e2) {
				e2.printStackTrace();
				// don't throw exception here because we want to keep going:
				ErrorLogger.writeExceptionToLogAndPrompt("Importing","ParticleException: "+e2.toString());
			}
		}
	}

	
	/** 
	 * constructs a dense list and a sparse list for the atom at the given
	 * position in the data file.  These are global variables.
	 * @author rzeszotj
	 */
	public void read(int particleNum, int nextID) {
		//Import dense data
		String fName = readData.next();
		String dateS = (readData.next() + " " + readData.next());
		int acqNumber = readData.nextInt();
		double diameterV = readData.nextDouble();
		
		//Format date correctly	
		DateFormat dbDF = db.getDateFormat();
		ParsePosition p = new ParsePosition(0);
		Date currentDate = dateFormat.parse(dateS, p);
		String newD = dbDF.format(currentDate);
		
		//ATOFMSDense = Time, Laser Power, Size, Scatter Delay, File Name
		dense = (newD + ", 0.0, " + diameterV + ", " + acqNumber + "," + fName);
		
		//ATOFMSSparse = Location, Height, Area, Relative Area
		sparse = new ArrayList<String>();
		int temp;
		for (int i = 0; i < massToCharge.size(); i++) {
			temp = readData.nextInt();
			//System.out.println(tempNum);
			if (temp != 0.0)
				sparse.add(massToCharge.get(i)+", "+temp+", 0, 0");
		}
	}
	
	/**
	 * Removes the file extension and directory information 
	 * from datasetName and returns the collection name. 
	 * @return String
	 * @author rzeszotj
	 */
	public String getName(){
		String s = "SPASS file";
		Character c1 = '.';
		Character c2 = '\\';
		
		if (!(datasetName == null)){
			int begin = 0;
			int end = datasetName.length() - 1;
			for (int i = (datasetName.length()-1); i >= 0; i--) {
				Character c3 = datasetName.charAt(i);
				if (c1.equals(c3))
					end = i;
				if (c2.equals(c3)){
					begin = i+1;
					return datasetName.substring(begin,end);
				}
			}
			return datasetName;//datasetName had no extension etc.
		}
		//For some reason datasetName doesn't exist
		return "SPASS data";
	}
	
	/**
	 * Change the parent collection for incoming datasets from its default of 0
	 * @param id the parent collection ID
	 * @author shaferia
	 */
	public void setParentID(int id) {
		parentID = id;
	}
	
	public void errorCheck() throws DisplayException{
		String name;
		File d;
		for (int i=0;i<table.getRowCount()-1;i++) {
			name = (String)table.getValueAt(i,1);
			//Check to make sure that .txt files are present.
			if (name.equals(".txt file") || name.equals("")) 
				throw new DisplayException("You must enter a data file at row # " + (i+1) + ".");
			
			// check to make sure all files are valid:
			d = new File(name);
			if (!d.exists()) {
				throw new DisplayException("The file " + name +" does not exist.");
			}
		}
	}
}
