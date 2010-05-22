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
 * The Original Code is EDAM Enchilada's ClusterDialog class.
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
 * Tom Bigwood tom.bigwood@nevelex.com
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

package dataExporters;

import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

import collection.Collection;

import ATOFMS.ParticleInfo;
import database.CollectionCursor;
import database.InfoWarehouse;
import errorframework.DisplayException;
import errorframework.ErrorLogger;
import gui.ExportCSVDialog;
import gui.ProgressBarWrapper;

/**
 * Exports the peak list for a particle or set of particles to a file as comma-
 * separated values.
 * 2009-03-12
 * @author jtbigwoo
 */

public class CSVDataSetExporter {

	/* window that spawned this process so we can send messages, etc. */
	Window mainFrame;

	/* Database object */
	InfoWarehouse db;

	ProgressBarWrapper progressBar;

	public static final String TITLE = "Exporting Data Set to File";

	public CSVDataSetExporter(Window mf, InfoWarehouse db, ProgressBarWrapper pbar) {
		mainFrame = mf;
		this.db = db;
		progressBar = pbar;
	}

	/**
	 * Exports the peak list data for the supplied collection
	 * @param coll the collection of the particles we want to 
	 * export
	 * @param fileName the path to the file that we're going to create
	 * @param maxMZValue this is the maximum mass to charge value to export
	 * (we often filter out the largest and smallest mass to charge values
	 * @return true if it worked
	 */
	public boolean exportToCSV(Collection coll, String fileName, int maxMZValue)
		throws DisplayException {
		double mzConstraint = new Double(maxMZValue);
		boolean showingNegatives;
		CollectionCursor atomInfoCur;
		ParticleInfo particleInfo;
		
		if (fileName == null) {
			return false;
		} else if (! fileName.endsWith(ExportCSVDialog.EXPORT_FILE_EXTENSION)) {
			fileName = fileName + "." + ExportCSVDialog.EXPORT_FILE_EXTENSION;
		}
		if (! coll.getDatatype().equals("ATOFMS")) {
			throw new DisplayException("Please choose a ATOFMS collection to export.");
		}

		fileName = fileName.replaceAll("'", "");

		try {
	
			progressBar.setText("Exporting peak data");
			progressBar.setIndeterminate(true);
			
			atomInfoCur = db.getAtomInfoOnlyCursor(coll);

			ArrayList<ParticleInfo> particleList = new ArrayList<ParticleInfo>();
			int fileIndex = 0;
			while (atomInfoCur.next()) {
				particleInfo = atomInfoCur.getCurrent();
				particleInfo.setBinnedList(atomInfoCur.getPeakListfromAtomID(particleInfo.getID()));
				particleList.add(particleInfo);
				if (particleList.size() == 127)
				{
					writeOutParticlesToFile(particleList, fileName, fileIndex++, maxMZValue);
					particleList.clear();
				}
			}
			if (particleList.size() > 0)
			{
				writeOutParticlesToFile(particleList, fileName, fileIndex++, maxMZValue);
			}
		} catch (IOException e) {
			ErrorLogger.writeExceptionToLogAndPrompt("CSV Data Exporter","Error writing file please ensure the application can write to the specified file.");
			System.err.println("Problem writing file: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Takes a list of ParticleInfo objects and writes it out to a file.
	 * You probably want to limit your list to 127 items since that's all that
	 * Excel will display.  If you have more than 127 items, call this method
	 * repeatedly using increasing file indices.
	 * @param particleList the list of particles to write out
	 * @param fileName the name of the file to write out.  please name it 
	 * &lt;filename&gt;.csv
	 * @param fileIndex the index if we need to name more than one file.  
	 * e.g. fileIndex = 0 would be filename.csv, fileIndex = 1 would be 
	 * filename_1.csv
	 * @param maxMZValue the largest (and smallest) mz value to export, e.g.
	 * if you choose maxMZValue=200, m/z = +201 and m/z = -201 will not be included
	 * @throws IOException
	 */
	private void writeOutParticlesToFile(ArrayList<ParticleInfo> particleList, String fileName, int fileIndex, int maxMZValue)
		throws IOException {
		PrintWriter out = null;
		File csvFile;
		ArrayList<BinnedPeak> currentPeakForAllParticles;
		ArrayList<Iterator<BinnedPeak>> peakLists;
		DecimalFormat formatter = new DecimalFormat("0.00");

		if (fileIndex == 0)
		{
			csvFile = new File(fileName);
		}
		else
		{
			csvFile = new File(fileName.replace(".csv", "_" + fileIndex + ".csv"));
		}

		out = new PrintWriter(new FileOutputStream(csvFile, false));
		currentPeakForAllParticles = new ArrayList<BinnedPeak>(particleList.size());
		peakLists = new ArrayList<Iterator<BinnedPeak>>(particleList.size());
		StringBuffer sbHeader = new StringBuffer();
		StringBuffer sbSubHeader1 = new StringBuffer();
		StringBuffer sbSubHeader2 = new StringBuffer();
		StringBuffer sbNegLabels = new StringBuffer();
		StringBuffer sbPosLabels = new StringBuffer();
		for (ParticleInfo particleInfo : particleList) {
			sbHeader.append(particleInfo instanceof AverageParticleInfo ? "****** Collection: " : "****** Particle: ");
			String choppedName = particleInfo instanceof AverageParticleInfo ? ((AverageParticleInfo)particleInfo).getCollection().getName() : particleInfo.getATOFMSParticleInfo().getFilename(); 
			choppedName = choppedName.indexOf('\\') > 0 ? choppedName.substring(choppedName.lastIndexOf('\\') + 1) : choppedName;
			sbHeader.append(choppedName);
			sbHeader.append(" ******,,");
			if (particleInfo instanceof AverageParticleInfo) {
				sbSubHeader1.append("Collection ID: ");
				sbSubHeader1.append(((AverageParticleInfo)particleInfo).getCollection().getCollectionID());
				sbSubHeader1.append(",,");
				sbSubHeader2.append("Parent Collection ID: ");
				sbSubHeader2.append(((AverageParticleInfo)particleInfo).getCollection().getParentCollection().getCollectionID());
				sbSubHeader2.append(",,");
			}
			Iterator<BinnedPeak> peaks = particleInfo.getBinnedList().iterator();
			BinnedPeak peak = null;
			while (peaks.hasNext()) {
				peak = peaks.next();
				if (peak.getKey() >= -maxMZValue)
					break;
			}
			if (peak == null || peak.getKey() < -maxMZValue)
				peak = null;
			currentPeakForAllParticles.add(peak);
			peakLists.add(peaks);
			sbNegLabels.append("Negative Spectrum,,");
			sbPosLabels.append("Positive Spectrum,,");
		}
		out.println(sbHeader.toString());
		if (sbSubHeader1.length() > 0) {
			out.println(sbSubHeader1.toString());
			out.println(sbSubHeader2.toString());
		}
		out.println(sbNegLabels.toString());

		boolean showingNegatives = true;
		StringBuffer sbValues = new StringBuffer();
		
		for (int location = -maxMZValue; location <= maxMZValue; location++) {
			if (showingNegatives && location >= 0) {
				showingNegatives = false;
				out.println(sbPosLabels.toString());
			}
			for (int particleIndex = 0; particleIndex < peakLists.size(); particleIndex++) {
				BinnedPeak peak = currentPeakForAllParticles.get(particleIndex);
				if (peak == null || location < peak.getKey()) {
					sbValues.append(location);
					sbValues.append(",0.00,");
				}
				else {
					// write it out
					sbValues.append(new Double(peak.getKey()).intValue());
					sbValues.append(",");
					sbValues.append(formatter.format(peak.getValue()));
					sbValues.append(",");
					currentPeakForAllParticles.set(particleIndex, peakLists.get(particleIndex).hasNext() ? peakLists.get(particleIndex).next() : null);
				}
			}
			out.println(sbValues.toString());
			sbValues.setLength(0);
		}
		out.close();
	}

	/**
	 * Exports the peak list data for the supplied collection
	 * @param coll the collection of the particles we want to 
	 * export
	 * @param fileName the path to the file that we're going to create
	 * @param maxMZValue this is the maximum mass to charge value to export
	 * (we often filter out the largest and smallest mass to charge values
	 * @return true if it worked
	 */
	public boolean exportHierarchyToCSV(Collection coll, String fileName, int maxMZValue)
		throws DisplayException {
		double mzConstraint = new Double(maxMZValue);
		boolean showingNegatives;
		ArrayList<Integer> collectionIDList;
		
		if (fileName == null) {
			return false;
		} else if (! fileName.endsWith(ExportCSVDialog.EXPORT_FILE_EXTENSION)) {
			fileName = fileName + "." + ExportCSVDialog.EXPORT_FILE_EXTENSION;
		}
		if (! coll.getDatatype().equals("ATOFMS")) {
			throw new DisplayException("Please choose a ATOFMS collection to export.");
		}

		fileName = fileName.replaceAll("'", "");

		try {
	
			progressBar.setText("Exporting peak data");
			progressBar.setIndeterminate(true);

			collectionIDList = new ArrayList<Integer>();
			collectionIDList.add(coll.getCollectionID());

			ArrayList<ParticleInfo> particleList = new ArrayList<ParticleInfo>();
			ArrayList<Integer> collectionIDs = new ArrayList<Integer>();
			collectionIDs.addAll(db.getAllDescendantCollections(coll.getCollectionID(), true));
			int fileIndex = 0;
			for (int collectionID : collectionIDs) {
				Collection collection = db.getCollection(collectionID);
				particleList.add(new AverageParticleInfo(collection, db.getAveragePeakListForCollection(collection)));
				if (particleList.size() == 127)
				{
					writeOutParticlesToFile(particleList, fileName, fileIndex++, maxMZValue);
					particleList.clear();
				}
			}
			if (particleList.size() > 0)
			{
				writeOutParticlesToFile(particleList, fileName, fileIndex++, maxMZValue);
			}
		} catch (IOException e) {
			ErrorLogger.writeExceptionToLogAndPrompt("CSV Data Exporter","Error writing file please ensure the application can write to the specified file.");
			System.err.println("Problem writing file: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}


	class AverageParticleInfo extends ParticleInfo {
		Collection collection;
		
		public AverageParticleInfo(Collection coll, BinnedPeakList peakList) {
			this.setBinnedList(peakList);
			this.collection = coll;
		}
		
		public Collection getCollection() {
			return collection;
		}
		
		
	}
}
