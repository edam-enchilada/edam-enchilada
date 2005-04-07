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
 * The Original Code is EDAM Enchilada's DataSetProcessor class.
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
 * Created on Jul 28, 2004
 *
 */
package msanalyze;

import atom.*;
import database.*;

import java.awt.FlowLayout;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import gui.ImportParsDialog;
import gui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JFrame;
import java.awt.Container;
import externalswing.*;

import java.util.zip.DataFormatException;

/**
 * @author ritza
 *
 * Processes datasets and inserts them into the database.
 */
public class DataSetProcessor {
	
	/* Variables read in from the particle table */
	private String name;
	private String massCalFile;
	private String sizeCalFile;
	private boolean autoCal;
	private Container parentContainer;
	private ImportParsDialog ipd;
	
	protected JDialog waitBarDialog = null;
	protected JProgressBar pBar = null;
	protected JLabel pLabel = null;
	protected int particleNum;
	protected int totalParticles;
	
	/* '.par' file */
	private File parFile;
	
	/* contains the collectionID and particleID */
	private int[] id;
	protected int positionInBatch, totalInBatch;
	
	/* Object that reads the spectrum */
	ReadSpec read;
	
	/* SQLServerDatabase object */
	SQLServerDatabase db;
	
	/* Lock to make sure database is only accessed in one batch at a time */
	private static Integer dbLock = new Integer(0);
	
	public DataSetProcessor(File file, String mCalFile, String sCalFile, CalInfo cInfo, PeakParams pParams, 
			Container parent, ImportParsDialog i, int thisDataset, int totDatasets) 
	throws IOException, NullPointerException, DataFormatException {
		parFile = file;
		ATOFMSParticle.currCalInfo = cInfo;
		ATOFMSParticle.currPeakParams = pParams;
		positionInBatch = thisDataset;
		totalInBatch = totDatasets;
		parentContainer = parent;
		ipd = i;
		sizeCalFile = sCalFile;
		massCalFile = mCalFile;
		readParFileAndCreateEmptyCollection();	
	}
	
	/**
	 * Reads the pertinent information from the '.par' file and creates
	 * an empty collection ready to populate with that dataset's particles.
	 */
	public void readParFileAndCreateEmptyCollection() 
	throws IOException, NullPointerException, DataFormatException {
		//Read '.par' info.
		String[] data = parVersion();
			//CreateEmptyCollectionandDataset
		db = MainFrame.db;
		id = new int[2];
		System.out.println(data[0]);
		System.out.println(data[2]);
		System.out.println(massCalFile);
		System.out.println(sizeCalFile);
		id = db.createEmptyCollectionAndDataset(0,data[0],data[2],
				massCalFile,sizeCalFile, ATOFMSParticle.currCalInfo,
				ATOFMSParticle.currPeakParams);
		readSpectraAndCreateParticle();
	}
	
	/**
	 * Reads the filename of each spectrum from the '.set' file, finds that file, reads 
	 * the file's information, and creates the particle.
	 * 
	 */
	public void readSpectraAndCreateParticle() 
	throws IOException, NumberFormatException {
		synchronized (dbLock) {
			//Read spectra & create particle.  
			File parent = parFile.getParentFile();
			File grandParent = parent.getParentFile();
			//System.out.println("Data set: " + parent.toString());
			
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
				
				totalParticles = tParticles;
				
				final SwingWorker worker = new SwingWorker() {
					String particleName;
					
					public Object construct() {
						try{
							//Read spectra & create particle.  
							File parent = parFile.getParentFile();
							File grandParent = parent.getParentFile();
							//System.out.println("Data set: " + parent.toString());
							
							String name = parent.getName();
							name = parent.toString()+ File.separator + name + ".set";
							
							
							BufferedReader readSet = new BufferedReader(new FileReader(name));
							StringTokenizer token;
							String particleFileName;
							particleNum = 0;
							//int doDisplay = 4;
							int nextID = db.getNextID();
							while (readSet.ready()) { // repeat until end of file.
								token = new StringTokenizer(readSet.readLine(), ",");
								token.nextToken();
								particleName = token.nextToken();
								particleFileName = grandParent.toString() + File.separator + particleName;
								
								read = new ReadSpec(particleFileName);
								db.insertATOFMSParticle(read.getParticle(),id[0],id[1],nextID);
								nextID++;
								particleNum++;
								//doDisplay++;
								if(particleNum % 5 == 0)
									//doDisplay = 0;
									try {
										SwingUtilities.invokeAndWait(new Runnable() {
											public void run()
											{
												if (waitBarDialog != null)
												{
													pBar.setValue(particleNum);
													pLabel.setText("Processing particle " +
															particleNum + " of " 
															+ totalParticles + ".");
													waitBarDialog.validate();
												}
											}
										});
									} catch (InvocationTargetException e) {
										String[] s = {"Progress Bar Error: ", e.toString()};
										ipd.displayException(s);
									} catch (InterruptedException e){
										String[] s = {"Progress Bar Error: ", e.toString()};
										ipd.displayException(s);
									}
							}
							SwingUtilities.invokeLater(new Runnable() {
								public void run()
								{
									waitBarDialog.setVisible(false);
									waitBarDialog = null;
								}
							});
						readSet.close();
						final String exceptionFile = particleName;
					}catch (Exception e) {
						try {
							final String exception = e.toString();
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run()
								{
									String[] s = 
									{"Corrupt particle: " + particleName + ": ", exception};
									ipd.displayException(s);
								}
							});
						} catch (Exception e2) {
							String[] s = {"ParticleException: ", e2.toString()};
							ipd.displayException(s);
						}
					}
					return null;
				}
				
			};
			worker.start();
			
			waitBarDialog = new JDialog((JFrame)parentContainer, "Processing dataset #" + 
					positionInBatch + " of " + totalInBatch, true);
			waitBarDialog.setLayout(new FlowLayout());
			pBar = new JProgressBar(0,totalParticles);
			pBar.setValue(0);
			pBar.setStringPainted(true);
			pLabel = new JLabel("       Processing particle 1 of " 
					+ totalParticles + ".                 ");
			pLabel.setLabelFor(pBar);
			waitBarDialog.add(pBar);
			waitBarDialog.add(pLabel);
			
			waitBarDialog.pack();
			waitBarDialog.validate();
			waitBarDialog.setVisible(true);
			
		} else System.out.println("Dataset has no hits.");
	}
	}
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
	
	
}
