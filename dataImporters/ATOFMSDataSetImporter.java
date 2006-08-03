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
 * The Original Code is EDAM Enchilada's DatasetImporter class.
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


/*
 * Created on Aug 3, 2004
 *
 */
package dataImporters;

import database.SQLServerDatabase;
import errorframework.DisplayException;
import errorframework.ErrorLogger;
import externalswing.SwingWorker;
import gui.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ATOFMS.*;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.ZipException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import collection.Collection;


/**
 * Creates a new DataSetProcessor object for each dataset that processes the spectra and
 * creates the particle.  It passes a file name, a CalInfo object and a PeakParams object 
 * to DataSetProcessor.
 * 
 * 
 * @author ritza
 *
 */
public class ATOFMSDataSetImporter {
	
	private ParTable table;
	private Window mainFrame;
	private boolean parent;
	
	//Table values - used repeatedly.
	private int rowCount;
	private String name = "";
	private String massCalFile, sizeCalFile;
	private int height, area;
	private float relArea;
	private float peakError;
	private boolean autoCal;
	
	private String particleName;	
	
	// Progress Bar variables
	protected ProgressBarWrapper progressBar;
	protected JDialog waitBarDialog = null;
	protected JProgressBar pBar = null;
	protected JLabel pLabel = null;
	protected int particleNum;
	protected int totalParticles;
	private Container parentContainer;
	
	/* '.par' file */
	protected File parFile;
	
	/* contains the collectionID and particleID */
	private int[] id;
	protected int positionInBatch, totalInBatch;
	protected int constructsDone;
	
	/* Object that reads the spectrum */
	ReadSpec read;
	
	/* SQLServerDatabase object */
	SQLServerDatabase db;
	
	/* Lock to make sure database is only accessed in one batch at a time */
	private static Integer dbLock = new Integer(0);
	
	/* the parent collection */
	private int parentID = 0;

	
	/**
	 * 
	 * Constructor.  Sets the particle table for the importer.
	 * @param t - particle table model.
	 */
	public ATOFMSDataSetImporter(ParTable t, Window mf) {
		table = t;
		mainFrame = mf;
	}
	
	public ATOFMSDataSetImporter(ParTable t, Window mf, SQLServerDatabase db) {
		table = t;
		mainFrame = mf;
		this.db = db;
	}
	
	/**
	 * Loops through each row, collects the information, and processes the
	 * datasets row by row.
	 */
	public void collectTableInfo() {
		
		particleNum = 0;
		constructsDone = 0;
		rowCount = table.getRowCount()-1;
		totalInBatch = rowCount;
		
		//Loops through each dataset and creates each collection.
		for (int i=0;i<rowCount;i++) {
				// Table values for this row.
				int nextCol = 1;
				name = (String)table.getValueAt(i,nextCol++);
				massCalFile = (String)table.getValueAt(i,nextCol++);
				sizeCalFile = (String)table.getValueAt(i,nextCol++);
				height= ((Integer)table.getValueAt(i,nextCol++)).intValue();
				area = ((Integer)table.getValueAt(i,nextCol++)).intValue();
				relArea = ((Float)table.getValueAt(i,nextCol++)).floatValue();
				if(table.getColumnCount() == 9){
					peakError = ((Float)table.getValueAt(i,nextCol++)).floatValue();
				}else{
					peakError = .50f;
				}
				autoCal = ((Boolean)table.getValueAt(i,nextCol++)).booleanValue();
				positionInBatch = i + 1;
				// Call relevant methods
				try {
					processDataSet();
					readParFileAndCreateEmptyCollection();
					readSpectraAndCreateParticle();
					// update the internal atom order table;
					db.updateAncestors(db.getCollection(id[0]));
				} catch (Exception e) {
					//e.printStackTrace();
					ErrorLogger.writeExceptionToLog("Importing","File "+name+
							" failed to import: \n\tMessage : "+e.toString()+","+e.getMessage());
				} 
		}
		
		progressBar = 
			new ProgressBarWrapper((JFrame)mainFrame, "Importing ATOFMS Datasets", totalParticles/10);
		progressBar.constructThis();
	}
	
	/**
	 * Does some preprocessing before importing a datset.
	 * Sets the currCalInfo and currPeakParam fields of ATOFMSParticle,
	 * creates an empty collection and fills that collection with the dataset's 
	 * particles.
	 */
	public void processDataSet()
	throws IOException, DataFormatException {
		boolean skipFile = false;
		
		//Create CalInfo Object.
		CalInfo calInfo = null;
		try {
			if (sizeCalFile.equals(".noz file") || sizeCalFile.equals("")) 
				calInfo = new CalInfo(massCalFile, autoCal);
			else 
				calInfo = new CalInfo(massCalFile,sizeCalFile, autoCal);
		} catch (Exception e) {
			ErrorLogger.writeExceptionToLog("Importing","Corrupt calibration file : " +
					"\n\tMessage: "+e.getMessage());
			skipFile = true;
		}
		if (!skipFile) { // If we don't have to skip this row due to an error...
			
			// Create PeakParam Object.
			PeakParams peakParams = new PeakParams(height,area,relArea,peakError);
			
			//Read '.par' file and create collection to fill.
			parFile = new File(name);
			ATOFMSParticle.currCalInfo = calInfo;
			ATOFMSParticle.currPeakParams = peakParams;
			
			// NOTE: Datatype is already in the db.
		}
	}
	
	/**
	 * This method loops through the table and checks to make sure that there is a
	 * .par file and a .cal file for every row, as well as non-zeros for the params.
	 * @return returns if there are no null rows, throws exception if there are.
	 */
	public void checkNullRows() throws DisplayException {
		String name, massCalFile;
		int height, area;
		float relArea;
		for (int i=0;i<table.getRowCount()-1;i++) {
			name = (String)table.getValueAt(i,1);
			massCalFile = (String)table.getValueAt(i,2);
			//Check to make sure that .par and .cal files are present.
			if (name.equals(".par file") || name.equals("") 
					|| massCalFile.equals(".cal file") 
					|| massCalFile.equals("")) {
				throw new DisplayException("You must enter a '.par' file and a " +
						"'.cal' file at row # " + (i+1) + ".");
			}
			height= ((Integer)table.getValueAt(i,4)).intValue();
			area = ((Integer)table.getValueAt(i,5)).intValue();
			relArea = ((Float)table.getValueAt(i,6)).floatValue();
			if (height == 0 || area == 0 || relArea == 0.0) {
				throw new DisplayException("The Peaklisting Parameters need to be greater " +
						"than 0 at row # " + (i+1) + ".");
			}
		}
	}
	
	/**
	 * Reads the pertinent information from the '.par' file and creates
	 * an empty collection ready to populate with that dataset's particles.
	 *
	 */
	public void readParFileAndCreateEmptyCollection() 
	throws IOException, NullPointerException, DataFormatException {
		//Read '.par' info.
		String[] data = parVersion();
		//CreateEmptyCollectionandDataset
		if (db == null) {
			db = MainFrame.db;
		}
		if (db == null) { // still
			db = new SQLServerDatabase();
			db.openConnection();
		}
		id = new int[2];
		System.out.println(data[0]);
		System.out.println(data[2]);
		System.out.println(massCalFile);
		System.out.println(sizeCalFile);
		int bool = -1;
		if (ATOFMSParticle.currCalInfo.autocal)
			bool = 1;
		else bool = 0;
		String dSet = parFile.toString();
		dSet = dSet.substring(dSet.lastIndexOf(File.separator)+1, dSet.lastIndexOf("."));
		
		//if datasets are imported into a parent collection
		//pass parent's id in as second parameter, else parentID is root (0)
//		if (ipd.parentExists())
//			parentID = ipd.getParentID();
		
		id = db.createEmptyCollectionAndDataset("ATOFMS",parentID,data[0],data[2],
				"'" + massCalFile + "', '" + sizeCalFile + "', " +
				ATOFMSParticle.currPeakParams.minHeight + ", " + 
				ATOFMSParticle.currPeakParams.minArea  + ", " + 
				ATOFMSParticle.currPeakParams.minRelArea + ", " + 
				bool);
		
	}
	
	/**
	 * Reads the filename of each spectrum from the '.set' file, finds that file, reads 
	 * the file's information, and creates the particle.
	 * 
	 */
	public void readSpectraAndCreateParticle() 
	throws IOException, NumberFormatException {
		//Read spectra & create particle.
		File canonical = parFile.getAbsoluteFile();
		File parent = canonical.getParentFile();
		File grandParent = parent.getParentFile();
		//System.out.println("Data set: " + parent.toString());
		final ATOFMSDataSetImporter thisref = this;
		
		String name = parent.getName();
		name = parent.toString()+ File.separator + name + ".set";
		if (new File(name).isFile()) {
			BufferedReader countSet = new BufferedReader(
					new FileReader(name));
			
			int tParticles = 0;
			
			while(countSet.ready())
			{
				countSet.readLine();
				tParticles++;
			}
			countSet.close();
			
			totalParticles += tParticles;
			
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
						
						//Read spectra & create particle.  
						File parent = parFile.getAbsoluteFile().getParentFile();
						File grandParent = parent.getParentFile();
						//System.out.println("Data set: " + parent.toString());
						
						String name = parent.getName();
						name = parent.toString()+ File.separator + name + ".set";
						
						Collection destination = db.getCollection(id[0]);
						ATOFMSParticle currentParticle;
						try {
							BufferedReader readSet = new BufferedReader(new FileReader(name));
							
							synchronized (dbLock) {				
								StringTokenizer token;
								String particleFileName;
								//int doDisplay = 4;
								int nextID = db.getNextID();
								Collection curCollection = db.getCollection(id[0]);
								while (readSet.ready()) { // repeat until end of file.
									token = new StringTokenizer(readSet.readLine(), ",");
									
									// .set files are sometimes made with really strange line delims,
									// so we ignore empty lines.
									if (! token.hasMoreTokens()) {
										continue;
									}
									
									token.nextToken();
									particleName = token.nextToken().replace('\\', File.separatorChar);
									particleFileName = grandParent.toString() + File.separator + particleName;
									try {
									read = new ReadSpec(particleFileName);
									
									currentParticle = read.getParticle();
									db.insertParticle(
											
											currentParticle.particleInfoDenseString(),
											currentParticle.particleInfoSparseString(),
											destination,id[1],nextID, true);
									}
									catch (Exception e) {
										ErrorLogger.writeExceptionToLog("Importing","Error reading or inserting particle " + 
												particleFileName+":\n\tMessage: "+e.getMessage());
									}
									nextID++;
									particleNum++;
									//doDisplay++;
									if(particleNum % 10 == 0 && particleNum >= 10 && progressBar != null)
										progressBar.increment("Importing Particle # "+particleNum+" out of "+totalParticles);
									
								}
								db.updateAncestors(curCollection);
							}
							readSet.close();
						} catch (IOException e) {
							ErrorLogger.writeExceptionToLog("Importing","Error reading dataset for collection "+
									destination.getName()+"\n\tMessage: "+e.toString()+","+e.getMessage());
						}		
					final String exceptionFile = particleName;
					++constructsDone;
					return null;	
				}
				public void finished() {
					if (constructsDone == totalInBatch)
						progressBar.disposeThis();
					
					synchronized (thisref) {
						thisref.notifyAll();						
					}
				}
			};
			worker.start();
			
		} else {
			ErrorLogger.displayException(parentContainer, 
					"Dataset has no hits because "+name+" does not exist.");
		}
	
	}
	
	// tests for .par version (.ams,.amz)
	// String[] returned is Name, Comment, and Description.
	public String[] parVersion() throws IOException, DataFormatException {
		BufferedReader readPar = new BufferedReader(new FileReader(parFile));
		String test = readPar.readLine();
		String[] data = new String[3];
		if (test.equals("ATOFMS data set parameters")) {
			data[0] = readPar.readLine();
			data[1] = readPar.readLine();
			data[2] = "";
			while (readPar.ready()) {
				data[2] = data[2] + readPar.readLine() + " ";
			}
			readPar.close();
			return data;
		}
		else if (test.equals("[ATOFMS PARFile]")){
			StringTokenizer token = new StringTokenizer(readPar.readLine(),"=");
			token.nextToken();
			data[0] = token.nextToken();
			token = new StringTokenizer(readPar.readLine(), "=");
			token.nextToken();
			String time = token.nextToken();
			token = new StringTokenizer(readPar.readLine(), "=");
			token.nextToken();
			data[1] = token.nextToken() + " " + time;
			// Skip inlet type:
			readPar.readLine();
			token = new StringTokenizer(readPar.readLine(), "=");
			token.nextToken();
			//TODO:  This only takes the first line of comments.
			data[2] = token.nextToken();
			return data;
		}
		else {
			throw new DataFormatException
			("Corrupt data in " + parFile.toString() + " file.");
		}
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}
}
