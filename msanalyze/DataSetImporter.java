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
package msanalyze;

import gui.*;
import java.io.File;

import atom.PeakParams;
import java.awt.Window;
import java.util.zip.DataFormatException;
import java.io.*;

/**
* @author ritza
*
* Creates a new DataSetProcessor object for each dataset that processes the spectra and
* creates the particle.  It passes a file name, a CalInfo object and a PeakParams object 
* to DataSetProcessor.
*/
public class DataSetImporter {
	
	private ParTableModel table;
	private Window mainFrame;
	private ImportParsDialog ipd;
	//Table values - used repeatedly.
	private int rowCount;
	private String name = "";
	private String massCalFile, sizeCalFile;
	private int height, area;
	private float relArea;
	private boolean autoCal;
	
	/**
* Constructor.  Sets the particle table for the importer.
* @param t - particle table model.
*/
	public DataSetImporter(ParTableModel t, Window mf, ImportParsDialog dialog) {
		table = t;
		mainFrame = mf;
		ipd = dialog;
	}
	
/**
* Loops through each row, collects the information, and processes the
* datasets row by row.
*/
	public void collectTableInfo() {
		rowCount = table.getRowCount()-1;
		//Loops through each dataset and creates each collection.
		for (int i=0;i<rowCount;i++) {
			try {
				// Table values for this row.
				name = (String)table.getValueAt(i,1);
				massCalFile = (String)table.getValueAt(i,2);
				sizeCalFile = (String)table.getValueAt(i,3);
				height= ((Integer)table.getValueAt(i,4)).intValue();
				area = ((Integer)table.getValueAt(i,5)).intValue();
				relArea = ((Float)table.getValueAt(i,6)).floatValue();
				autoCal = ((Boolean)table.getValueAt(i,7)).booleanValue();
				processDataSet(i);
			} catch (Exception e) {
				String[] s = {name + " failed to import.", "Exception: ", 
						e.toString()};
				ipd.displayException(s);
			}
		}
	}
	
/**
* Sets the currCalInfo and currPeakParam 
* fields of ATOFMSParticle,
* creates an empty collection and fills that collection with the dataset's 
* particles.
*/
	public void processDataSet(int index)
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
			new DataFormatException("Corrupt Calibration File: " + e.toString());
			skipFile = true;
		}
		if (!skipFile) { // If we don't have to skip this row due to an error...
			
			// Create PeakParam Object.
			PeakParams peakParams = new PeakParams(height,area,relArea);
			
			//Read '.par' file and create collection to fill.
			File parFile = new File(name);
			
			new DataSetProcessor(parFile, massCalFile, sizeCalFile, calInfo, peakParams, mainFrame, ipd, 
					index + 1, rowCount);
		}
	}
	
/**
* This method loops through the table and checks to make sure that there is a
* .par file and a .cal file for every row, as well as non-zeros for the params.
* @return true if there is a null row, false if not.
*/
	public boolean nullRows() {
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
				String[] s = {"You must enter a '.par' file and a " +
						"'.cal' file at row # " + (i+1) + "."};
				ipd.displayException(s);
				return true;
			}
			height= ((Integer)table.getValueAt(i,4)).intValue();
			area = ((Integer)table.getValueAt(i,5)).intValue();
			relArea = ((Float)table.getValueAt(i,6)).floatValue();
			if (height == 0 || area == 0 || relArea == 0.0) {
				String[] s = {"The Peaklisting Parameters need to be greater " +
						"than 0 at row # " + (i+1) + "."};
				ipd.displayException(s);
				return true;
			}
		}
		return false;
	}
}
