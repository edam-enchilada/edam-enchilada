/*
 * Created on Dec 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package experiments;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import ATOFMS.ATOFMSParticle;
import ATOFMS.ATOFMSPeak;
import ATOFMS.Peak;



/**
 * @author dmusican
 */
public class ATOFMSParticleExp extends ATOFMSParticle {
	
	public static Random randNum = new Random(729102);
	
	
	public ATOFMSParticleExp()
	{
		super();
	}
	
	public ATOFMSParticleExp(String fname, 
			Date timestr, 
			float lasPow,
			float dRate,
			int sDelay,
			int[] pSpect,
			int[] nSpect
	)
	{
		super(fname,timestr,lasPow,dRate,sDelay,pSpect,nSpect);
	}
	
	public ArrayList<Peak> getPeakList() {
		
		if (peakList != null)
			return peakList;
		
		int amplitude = ImportExperimentParticles.getAmplitude();
		super.getPeakList();
		
		// Add noise to peaks that are there
		for (int i=0; i < peakList.size(); i++) {
			ATOFMSPeak peak = (ATOFMSPeak)peakList.get(i);
			peak.area = peak.area + (int)Math.round(amplitude*randNum.nextGaussian());
			if (peak.area < 1)
				peak.area = 1;
			peakList.set(i,peak);
		}
		
		// Add random peaks.  Instead of placing them every
		// 20 locations, we'll insert 20 peaks at random
		// locations and then add a few in the same key to 
		// get a bigger peak.
		
		int peakLoc = 0;
		boolean neg;
		for (int peakCount = 0; peakCount < 20; peakCount++) {
			int heightAndArea =
				(int)(Math.round(Math.abs(randNum.nextGaussian()) * amplitude));
			if(heightAndArea < 1)
				heightAndArea = 1;
			
			// choose a peakLoc that isn't in the peaklist yet.
			boolean duplicateLoc = true;
			while (duplicateLoc) {
				duplicateLoc = false;
				peakLoc = randNum.nextInt(300);
				neg = randNum.nextBoolean();
				if (neg)
					peakLoc = -peakLoc;
				for (int i = 0; i < peakList.size(); i++) {
					if (peakList.get(i).massToCharge == (double)peakLoc)
						duplicateLoc = true;
				}
			}
			peakList.add(new ATOFMSPeak(heightAndArea,heightAndArea,peakLoc));

			// add 8 larger peaks out of the locations -300, -275...25, 50, 75, 100, 125,
			// 150, etc. up to 300 (24 possible locations).
			int index = -1;
			for (int biggerPeak = 0; biggerPeak < 8; biggerPeak++) {
				// choose key
				neg = randNum.nextBoolean();
				peakLoc = randNum.nextInt(12) * 25;
				if (neg)
					peakLoc = -peakLoc;
				// find index in arraylist.
				for (int i = 0; i < peakList.size(); i++) {
					if (peakList.get(i).massToCharge == (double)peakLoc)
						index = i;
				}
				// find a random HeightAndArea.
				heightAndArea =
					(int)(Math.round(Math.abs(randNum.nextGaussian()) * amplitude));
				if (heightAndArea < 1)
					heightAndArea = 1;
				// If a peak exists at that key, add to it.
				if (index == -1)
					peakList.add(new ATOFMSPeak(heightAndArea, heightAndArea, peakLoc));
				else {
					heightAndArea = heightAndArea + (int)((ATOFMSPeak)peakList.get(index)).area;
				peakList.set(index, new ATOFMSPeak(heightAndArea,heightAndArea,peakLoc));
				}
			}
			
		}
		
		for (int i = 0; i < peakList.size(); i++) {
			assert ((ATOFMSPeak)peakList.get(i)).area > 0 : 
				"Area is negative: " + ((ATOFMSPeak)peakList.get(i)).area + " @ " + i;	    	
		}
		
		return peakList;
	}
	
}
