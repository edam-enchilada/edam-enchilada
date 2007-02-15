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
package ATOFMS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Date;
import java.text.DateFormat;

/**
 * @author ritza
 * Specific to ATOFMS data.
 */

public class FlatFileATOFMSParticle{

	private final int MAX_BIN_NUMBER;

	public String filename;
	public Date time;
	public float laserPower;
	public float digitRate;
	public int scatDelay;
	public float size;
	public int[] posSpectrum;
	public int[] negSpectrum;
	protected ArrayList<Peak> peakList = null;
	public int atomID;

	public static PeakParams currPeakParams;
	public static CalInfo currCalInfo;
	
	private double autoPosSlope, autoNegSlope, autoPosIntercept,
		autoNegIntercept;

	public FlatFileATOFMSParticle()
	{
		super();
		MAX_BIN_NUMBER = 30000;
		
	}
	
	/** 
	 * Sets all variables about the particle.  If autocalibrate is true, then
	 * particle is autocalibrated.
	 * @param fname - filename
	 * @param timestr - time
	 * @param lasPow - laser power
	 * @param dRate - digit rate
	 * @param sDelay - scat delay
	 * @param pSpect - pos spectrum
	 * @param nSpect - neg spectrum
	 */
	public FlatFileATOFMSParticle(String fname, 
						  double diameter,
						  Date timet, 
						  int[] pSpect,
						  int[] nSpect
						  )
	{
		filename = fname;
		MAX_BIN_NUMBER = pSpect.length-1;
		
		time = timet;
		posSpectrum = pSpect;
		negSpectrum = nSpect;
		
	}
	
	/**
	 * Gets the particle's peak list.
	 */
	public ArrayList<Peak> getPeakList()
	{
		if (peakList!= null)
			return peakList;
		peakList = new ArrayList<Peak>(20);
		
		int baselines[] = findBaseLines();
		
		getPosPeaks(baselines[0]);
		getNegPeaks(baselines[1]);
		
		return peakList;
	}
	
	/**
	 * Finds the base lines.
	 * @return int[2], pos and neg baselines.
	 */
	private int[] findBaseLines()
	{
		int[] ret = {0,0}; 
		return ret;
	}
	
	/**
	 * get positive peaks and put them into a sparse peak list.
	 * @param baseline
	 * @return true if successful
	 */
	private boolean getPosPeaks(int baseline)
	{
		double totalArea = 0;
		for(int i=0;i<posSpectrum.length;i++){
			
			
			// if the index is above the baseline find where it 
			// goes back below, this range (startLoc-endLoc) is 
			// the peak's key
			if (posSpectrum[i] > baseline)
			{
				totalArea += posSpectrum[i];
				peakList.add(new ATOFMSPeak(posSpectrum[i], posSpectrum[i], i));
				
			}
		} 
		int k = 0;
		
		
		while (k < peakList.size())
		{
			Peak peak = peakList.get(k);
			((ATOFMSPeak)peak).relArea = (float) (((ATOFMSPeak)peak).area/totalArea);
			if(((ATOFMSPeak)peak).relArea <= 0 ||
					((ATOFMSPeak)peak).area <= 0)
			{
				peakList.remove(k);
			}
			else
				k++;
		}

		return true;
	}
	

	/**
	 * Gets neg peaks and puts them into a sparse peaklist.
	 * @param baseline
	 * @return true on success.
	 */
	private boolean getNegPeaks(int baseline)
	{
		int startingListSize = peakList.size();
		double totalArea = 0;
		for(int i=0;i<negSpectrum.length;i++){
			
			
			// if the index is above the baseline find where it 
			// goes back below, this range (startLoc-endLoc) is 
			// the peak's key
			if (negSpectrum[i] > baseline)
			{
				totalArea += negSpectrum[i];
				peakList.add(new ATOFMSPeak(negSpectrum[i], negSpectrum[i], i));
				
			}
		} 
		int k = startingListSize;

		
		while (k < peakList.size())
		{
			Peak peak = peakList.get(k);
			((ATOFMSPeak)peak).relArea = (float) (((ATOFMSPeak)peak).area/totalArea);
			if(((ATOFMSPeak)peak).relArea <= 0 ||
					((ATOFMSPeak)peak).area <= 0)
			{
				peakList.remove(k);
			}
			else
				k++;
		}

		return true;
		
	}

	
	/**
	 * Returns the calibrated positive spectrum of the particle.
	 */
	public chartlib.DataPoint[] getPosSpectrum()
	{
		chartlib.DataPoint[] spec = new chartlib.DataPoint[posSpectrum.length];
		for( int i=0; i < posSpectrum.length; i++)
			spec[i] = new chartlib.DataPoint(i,posSpectrum[i]);
		return spec;
	}
	
	/**
	 * Returns the calibrated negative spectrum of the particle.
	 */
	public chartlib.DataPoint[] getNegSpectrum()
	{
		chartlib.DataPoint[] spec = new chartlib.DataPoint[negSpectrum.length];
		for( int i=0; i < negSpectrum.length; i++)
		{
			spec[i] = new chartlib.DataPoint(-i,negSpectrum[i]);
		}
		return spec;
	}
	
	public String particleInfoDenseString(DateFormat d) {
		return "'" + d.format(time) + "', " + 
		laserPower + ", " + size + ", " + scatDelay + ", '" + filename + "'";
	}
	
	public ArrayList<String> particleInfoSparseString() {
		ArrayList<String> peaks = new ArrayList<String>();
		getPeakList();
		Map<Integer, Peak> map = new LinkedHashMap<Integer, Peak>();
		

		for (Peak p : peakList) {
			// see BinnedPeakList.java for source of this routine
			int mzInt; double mz = p.massToCharge;
			
			if (mz >= 0.0)
				mzInt = (int) (mz + 0.5);
			else
				mzInt = (int) (mz - 0.5);
			
			//new Peak(int height, int area, double masstocharge)
			if (map.containsKey(mzInt))
			{
				ATOFMSPeak soFar = (ATOFMSPeak)map.get(mzInt);
				map.put(mzInt, 
						new ATOFMSPeak(soFar.height + ((ATOFMSPeak)p).height,
								soFar.area + ((ATOFMSPeak)p).area,
								soFar.relArea + ((ATOFMSPeak)p).relArea,
								mzInt));
			} else {
				map.put(mzInt, new ATOFMSPeak(((ATOFMSPeak)p).height, ((ATOFMSPeak)p).area, ((ATOFMSPeak)p).relArea, mzInt));
			}
		}
		for(Peak peak : map.values()){
			peaks.add(((ATOFMSPeak)peak).massToCharge + ", "
					+ ((ATOFMSPeak)peak).area + ", " + ((ATOFMSPeak)peak).relArea
					+ ", " + ((ATOFMSPeak)peak).height);
		}
		
		/*for(Peak peak : peakList){
			peaks.add(peak.massToCharge + ", "
					+ peak.area + ", " + peak.relArea
					+ ", " + peak.height);
		}*/
		return peaks;	
	}
//	***SLH 	 
    public String particleInfoDenseStr(DateFormat d) { 	 
            return  d.format(time) + ", " + laserPower + ", " + size + ", " + scatDelay + "," + filename.trim(); 	 
    }
}
