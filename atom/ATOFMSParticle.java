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
 * The Original Code is EDAM Enchilada's ATOFMSParticle class.
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
 * Created on Jul 16, 2004
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package atom;

import msanalyze.*;
import java.util.ArrayList;


/**
 * @author ritza
 * Specific to ATOFMS data.
 */

public class ATOFMSParticle extends Particle {

	private final int MAX_BIN_NUMBER;

	public String filename;
	public String time;
	public float laserPower;
	public float digitRate;
	public int scatDelay;

	public static PeakParams currPeakParams;
	public static CalInfo currCalInfo;
	
	protected ArrayList<Peak> peakList = null;
	
	private double autoPosSlope, autoNegSlope, autoPosIntercept,
		autoNegIntercept;
	
	public ATOFMSParticle()
	{
		super();
		MAX_BIN_NUMBER = 30000;
		
	}
	
	public ATOFMSParticle(String fname, 
						  String timestr, 
						  float lasPow,
						  float dRate,
						  int sDelay,
						  int[] pSpect,
						  int[] nSpect
						  )
	{
		MAX_BIN_NUMBER = pSpect.length;
		filename = fname;
		time = timestr;
		laserPower = lasPow;
		if (currCalInfo.sizecal)
		{
			size = 
				currCalInfo.c1 +
				currCalInfo.c2*sDelay +
				currCalInfo.c3*sDelay*sDelay +
				currCalInfo.c4*sDelay*sDelay*sDelay;
			if (size < 0)
				size = 0;
		}
		else
			size = 0;
		posSpectrum = pSpect;
		negSpectrum = nSpect;
		digitRate = dRate;
		scatDelay = sDelay;
		
		if (currCalInfo.autocal)
		{
			double returnVals[] = new double[2];
			//System.out.println("About to autocalibrate");
			returnVals = AutoCalibrator.autoCalibrate(digitRate, AutoCalibrator.POS, posSpectrum);
			if (returnVals != null)
			{
				autoPosSlope = returnVals[0];
				autoPosIntercept = returnVals[1];
			}
			else
			{
				autoPosSlope = currCalInfo.posSlope;
				autoPosIntercept = currCalInfo.posIntercept;
			}
			returnVals = AutoCalibrator.autoCalibrate(digitRate, AutoCalibrator.NEG, negSpectrum);
			if (returnVals != null)
			{
				autoNegSlope = returnVals[0];
				autoNegIntercept = returnVals[1];
			}
			else
			{
				autoNegSlope = currCalInfo.negSlope;
				autoNegIntercept = currCalInfo.negIntercept;
			}
			//System.out.println("Finished calibrating");
		}
	}
	
	public ArrayList<Peak> getPeakList()
	{
		//System.out.println("testing nullity");

		if (peakList!= null)
			return peakList;
		//System.out.println("creating new PeakList");
		peakList = new ArrayList<Peak>(20);
		
		int baselines[] = findBaseLines();
		
		getPosPeaks(baselines[0]);
		getNegPeaks(baselines[1]);
		
		return peakList;
	}
	
	private int[] findBaseLines()
	{
		int baselines[] = new int[2];
		
		baselines[0] = 0;
		baselines[1] = 0;
		
		// This calculation does not take into account the last
		// bin
		for (int i = MAX_BIN_NUMBER - MAX_BIN_NUMBER/10; i < MAX_BIN_NUMBER; i++)
		{
			baselines[0] += posSpectrum[i];
			baselines[1] += negSpectrum[i];
		}
		baselines[0] /= MAX_BIN_NUMBER/10;
		baselines[1] /= MAX_BIN_NUMBER/10;
		return baselines;
	}
	
	private boolean getPosPeaks(int baseline)
	{
		int i = 0;
		int startLoc = 0;
		int endLoc = 0;
		int centerIndex = 0;
		int peakHeight;
		int peakArea;
		int temp = 0;
		int totalArea = 0;
		boolean foundPeak = false;
		while (i < MAX_BIN_NUMBER)
		{
			startLoc = i;
			
			//Set peakHeight and peakArea to zero.
			peakHeight = 0;
			peakArea = 0;
			
			// if the index is above the baseline find where it 
			// goes back below, this range (startLoc-endLoc) is 
			// the peak's location
			while (i < MAX_BIN_NUMBER && posSpectrum[i] > baseline + currPeakParams.minHeight)
			{
				foundPeak = true;
				endLoc = i;
				temp = posSpectrum[i] - baseline;
				if (temp > peakHeight)
					peakHeight = temp;
				i++;
			}// while (posSpectrum[i] > baseline)
			// We found a peak and its range, now let's find out
			// about it.
			if (foundPeak == true)
			{ 
				// This is how MS-Analyze calculates peak centers,
				// the main effect of the -1 is that peaks centered
				// on even bins get pushed up a bin instead of back one
				// from the *.5 value.
				centerIndex = startLoc + (endLoc-(startLoc-1))/2;
				
				for (int j = startLoc; j <= endLoc; j++)
				{
					peakArea = peakArea + posSpectrum[j];
				}
				peakArea = peakArea - baseline*(endLoc-startLoc+1);
				totalArea += peakArea;

				peakList.add(new Peak(peakHeight, peakArea, getPosMZ(centerIndex)));
				foundPeak = false;
			} // if (foundPeak == true)
			else
				i++;
		} // while (i < MAX_BIN_NUMBER)
		int k = 0;
		
		while (k < peakList.size())
		{
			Peak peak = peakList.get(k);
			peak.relArea = (float) peak.area/totalArea;
			if(peak.relArea <= currPeakParams.minRelArea ||
			   peak.area <= currPeakParams.minArea)
			{
				peakList.remove(k);
			}
			else
				k++;
		}
		
		//totalArea = 0;
		//for (int n = 0; n < peakList.size(); n++)
		//	totalArea += peakList.get(n).area;
		// Calculate the relative areas.
		//for (int l = 0; l < peakList.size(); l++)
		//{
		//	Peak peak = peakList.get(l);
		//	peak.relArea = (float)peak.area/totalArea;
		//	peakList.set(l, peak);
		//}
		return true;
	}
	
	private boolean getNegPeaks(int baseline)
	{
		int startingListSize = peakList.size();
		int i = 0;
		int startLoc = 0;
		int endLoc = 0;
		int centerIndex = 0;
		int peakHeight = 0;
		int peakArea = 0;
		int temp = 0;
		int totalArea = 0;
		boolean foundPeak = false;
		while (i < MAX_BIN_NUMBER)
		{
			peakHeight = 0;
			peakArea = 0;
			startLoc = i;
			// if the index is above the baseline find where it 
			// goes back below, this range (startLoc-endLoc) is 
			// the peak's location
			//if (negSpectrum[i] < 0)
			//	System.out.println(negSpectrum[i]);
			while (i < MAX_BIN_NUMBER && negSpectrum[i] > (baseline
	               + currPeakParams.minHeight))
			{
				foundPeak = true;
				endLoc = i;
				temp = negSpectrum[i] - baseline;
				if (temp > peakHeight)
					peakHeight = temp;
				i++;
			}// while (negSpectrum[i] > baseline)
			// We found a peak and it's range, now let's find out
			// about it.  
			if (foundPeak == true)
			{
				centerIndex = startLoc + (endLoc-(startLoc-1))/2;
				
				peakArea = 0;
				for (int j = startLoc; j <= endLoc; j++)
				{
					peakArea = peakArea + negSpectrum[j];
				}
				peakArea = peakArea - baseline*(endLoc-startLoc+1);
				totalArea += peakArea;
				
				// lets see if we can pre cut a peak from the list
				// In an effor to match the MSAnalyze results, I'm 
				// reducing total area by the area of the peak as we 
				// cut it.  Nevermind.

				peakList.add(new Peak(peakHeight, peakArea, getNegMZ(centerIndex)));

				peakHeight = 0;
				foundPeak = false;
			} // if (foundPeak == true)
			else
				i++;
		} // while (i < MAX_BIN_NUMBER)
		
		int k = startingListSize;

		
		while (k < peakList.size())
		{
			Peak peak = peakList.get(k);
			peak.relArea = (float) peak.area/totalArea;
			if(peak.relArea <= currPeakParams.minRelArea ||
			   peak.area <= currPeakParams.minArea)
			{
				peakList.remove(k);
			}
			else
				k++;
		}
		

		
		//totalArea = 0;
		//for (int n = startingListSize; n < peakList.size(); n++)
		//	totalArea += peakList.get(n).area;
		
		// Calculate the relative areas.
		for (int l = startingListSize; l < peakList.size(); l++)
		{
			Peak peak = peakList.get(l);
			peak.relArea = (float)peak.area/totalArea;
			peakList.set(l, peak);
		}
		return true;
	}
	
	private double getPosMZ(int bin)
	{
		double squareThis = 0;
		if (currCalInfo.autocal)
		{
			squareThis = autoPosSlope*bin + autoPosIntercept;
		}
		else
		{
			squareThis = currCalInfo.posSlope*bin + currCalInfo.posIntercept;
		}
		
		return squareThis * squareThis;
	}
	
	private double getNegMZ(int bin)
	{
		double squareThis = 0;
		if (currCalInfo.autocal)
		{
			squareThis = autoNegSlope*bin + autoNegIntercept;
		}
		else
		{
			squareThis = currCalInfo.negSlope*bin + currCalInfo.negIntercept;
		}
		return -(squareThis * squareThis);
	}
	public static void setDataSetInfo(PeakParams p, CalInfo c) {
		currPeakParams = p;
		currCalInfo = c;
	}
	
	/*public void printAutoCalParams()
	{
		System.out.println("autoPosSlope" + autoPosSlope);
		System.out.println("autoPosIntercept" + autoPosIntercept);
		System.out.println("autoNegSlope" + autoNegSlope);
		System.out.println("autoNegIntercept" + autoNegIntercept);
	}*/
}
