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
import java.util.Iterator;

import analysis.clustering.PeakList;

import collection.Collection;

import ATOFMS.ParticleInfo;
import ATOFMS.Peak;
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
		throws DisplayException
	{
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
				PeakList peakList = new PeakList();
				peakList.setPeakList(db.getPeaks("ATOFMS", particleInfo.getID()));
				particleInfo.setPeakList(peakList);
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
	
	private void writeOutParticlesToFile(ArrayList<ParticleInfo> particleList, String fileName, int fileIndex, int maxMZValue)
		throws IOException
	{
		PrintWriter out = null;
		File csvFile;
		ArrayList<Peak> currentPeakForAllParticles;
		ArrayList<Iterator<Peak>> peakLists;
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
		currentPeakForAllParticles = new ArrayList<Peak>(particleList.size());
		peakLists = new ArrayList<Iterator<Peak>>(particleList.size());
		StringBuffer sbHeader = new StringBuffer();
		StringBuffer sbNegLabels = new StringBuffer();
		StringBuffer sbPosLabels = new StringBuffer();
		for (ParticleInfo particleInfo : particleList) {
			sbHeader.append("****** Particle: ");
			String choppedName = particleInfo.getATOFMSParticleInfo().getFilename();
			choppedName = choppedName.indexOf('\\') > 0 ? choppedName.substring(choppedName.lastIndexOf('\\') + 1) : choppedName;
			sbHeader.append(choppedName);
			sbHeader.append(" ******,,");
			Iterator<Peak> peaks = particleInfo.getPeakList().getPeakList().iterator();
			Peak peak = null;
			while (peaks.hasNext()) {
				peak = peaks.next();
				if (peak.massToCharge >= -maxMZValue)
					break;
			}
			if (peak == null || peak.massToCharge < -maxMZValue)
				peak = null;
			currentPeakForAllParticles.add(peak);
			peakLists.add(peaks);
			sbNegLabels.append("Negative Spectrum,,");
			sbPosLabels.append("Positive Spectrum,,");
		}
//		sbHeader.setLength(sbHeader.length() - 2);
//		sbNegLabels.setLength(sbNegLabels.length() - 2);
//		sbPosLabels.setLength(sbPosLabels.length() - 2);
		out.println(sbHeader.toString());
		out.println(sbNegLabels.toString());

		boolean showingNegatives = true;
		StringBuffer sbValues = new StringBuffer();
		
		for (int location = -maxMZValue; location <= maxMZValue; location++) {
			if (showingNegatives && location >= 0) {
				showingNegatives = false;
				out.println(sbPosLabels.toString());
			}
			for (int particleIndex = 0; particleIndex < peakLists.size(); particleIndex++) {
				Peak peak = currentPeakForAllParticles.get(particleIndex);
				if (peak == null || location < peak.massToCharge) {
					sbValues.append(location);
					sbValues.append(",0.00,");
				}
				else {
					// write it out
					sbValues.append(new Double(peak.massToCharge).intValue());
					sbValues.append(",");
					sbValues.append(formatter.format(peak.value));
					sbValues.append(",");
					currentPeakForAllParticles.set(particleIndex, peakLists.get(particleIndex).hasNext() ? peakLists.get(particleIndex).next() : null);
				}
			}
			out.println(sbValues.toString());
			sbValues.setLength(0);
		}
		out.close();
	}
}
