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
 * The Original Code is EDAM Enchilada's MedianFinder unit 
 * test class.
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
 * Created on Jan 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package analysis;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * @author andersbe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MedianFinderTest extends TestCase {
	private BinnedPeakList bpl1,bpl2,bpl3,bpl4,bpl5;
	private ArrayList<BinnedPeakList> allFive;
	private ArrayList<BinnedPeakList> firstFour;
	private ArrayList<BinnedPeakList> onlyOne;
	private ArrayList<BinnedPeakList> none;
	protected void setUp()
	{
		bpl1 = new BinnedPeakList();
		bpl2 = new BinnedPeakList();
		bpl3 = new BinnedPeakList();
		bpl4 = new BinnedPeakList();
		bpl5 = new BinnedPeakList();
		
		allFive = new ArrayList<BinnedPeakList>(5);
		firstFour = new ArrayList<BinnedPeakList>(4);
		onlyOne = new ArrayList<BinnedPeakList>(1);
		none = new ArrayList<BinnedPeakList>();
		
		bpl1.add(1,10);
		bpl1.add(-3,30);
		bpl1.add(-1,50);
		
		bpl2.add(1,20);
		bpl2.add(-3,60);
		bpl2.add(-2,50);
		
		bpl3.add(1,30);
		bpl3.add(-3,90);
		bpl3.add(3,50);
		
		bpl4.add(1,40);
		bpl4.add(-3,120);
		bpl4.add(-4,50);
		
		bpl5.add(1,50);
		bpl5.add(-3,150);
		bpl5.add(-5,50);
		
		allFive.add(bpl1);
		allFive.add(bpl2);
		allFive.add(bpl3);
		allFive.add(bpl4);
		allFive.add(bpl5);
		
		firstFour.add(bpl1);
		firstFour.add(bpl2);
		firstFour.add(bpl3);
		firstFour.add(bpl4);
		
		onlyOne.add(bpl1);
	}
	public void testGetMedian() {
		MedianFinder mf = new MedianFinder(allFive);
		
		BinnedPeakList median = mf.getMedian();
		assertTrue(median.getAreaAt(1) == 30);
		assertTrue(median.getAreaAt(-3) == 90);
		assertTrue(median.getAreaAt(-1) == 0);
		assertTrue(median.getAreaAt(-2) == 0);
		assertTrue(median.getAreaAt(3) == 0);
		assertTrue(median.getAreaAt(-4) == 0);
		assertTrue(median.getAreaAt(-5) == 0);
		
		mf = new MedianFinder(firstFour);
		median = mf.getMedian();
		assertTrue(median.getAreaAt(1) == 25);
		assertTrue(median.getAreaAt(-3) == 75);
		assertTrue(median.getAreaAt(-1) == 0);
		assertTrue(median.getAreaAt(-2) == 0);
		assertTrue(median.getAreaAt(3) == 0);
		assertTrue(median.getAreaAt(-4) == 0);
		
		mf = new MedianFinder(onlyOne);
		median = mf.getMedian();
		assertTrue(median.getAreaAt(1) == 10);
		assertTrue(median.getAreaAt(-3) == 30);
		assertTrue(median.getAreaAt(-1) == 50);
		boolean exception = false;
		try{
			mf = new MedianFinder(none);
		}catch (IllegalArgumentException e)
		{
			exception = true;
		}
		assertTrue(exception);
	}
	
	public void testGetKthElement() {
		MedianFinder mf = new MedianFinder(allFive);
		
		BinnedPeakList median = mf.getKthElement(0);
		assertTrue(median.getAreaAt(1) == 10);
		assertTrue(median.getAreaAt(-3) == 30);
		assertTrue(median.getAreaAt(-1) == 0);
		assertTrue(median.getAreaAt(-2) == 0);
		assertTrue(median.getAreaAt(3) == 0);
		assertTrue(median.getAreaAt(-4) == 0);
		assertTrue(median.getAreaAt(-5) == 0);
		
		median = mf.getKthElement(4);
		assertTrue(median.getAreaAt(1) == 50);
		assertTrue(median.getAreaAt(-3) == 150);
		assertTrue(median.getAreaAt(-1) == 50);
		assertTrue(median.getAreaAt(-2) == 50);
		assertTrue(median.getAreaAt(3) == 50);
		assertTrue(median.getAreaAt(-4) == 50);
		assertTrue(median.getAreaAt(-5) == 50);
		
		mf = new MedianFinder(firstFour);
		median = mf.getKthElement(2);
		assertTrue(median.getAreaAt(1) == 30);
		assertTrue(median.getAreaAt(-3) == 90);
		assertTrue(median.getAreaAt(-1) == 0);
		assertTrue(median.getAreaAt(-2) == 0);
		assertTrue(median.getAreaAt(3) == 0);
		assertTrue(median.getAreaAt(-4) == 0);
		
		mf = new MedianFinder(onlyOne);
		median = mf.getKthElement(0);
		assertTrue(median.getAreaAt(1) == 10);
		assertTrue(median.getAreaAt(-3) == 30);
		assertTrue(median.getAreaAt(-1) == 50);
		boolean exception = false;
		try{
			median = mf.getKthElement(1);
		}catch (IndexOutOfBoundsException e)
		{
			exception = true;
		}
		assertTrue(exception);
		try{
			mf = new MedianFinder(none);
		}catch (IllegalArgumentException e)
		{
			exception = true;
		}
		assertTrue(exception);
	}
	
	public void testGetPercentElement() {
		MedianFinder mf = new MedianFinder(allFive);
		
		BinnedPeakList median = mf.getPercentElement(1.0f);
		assertTrue(median.getAreaAt(1) == 10);
		assertTrue(median.getAreaAt(-3) == 30);
		assertTrue(median.getAreaAt(-1) == 0);
		assertTrue(median.getAreaAt(-2) == 0);
		assertTrue(median.getAreaAt(3) == 0);
		assertTrue(median.getAreaAt(-4) == 0);
		assertTrue(median.getAreaAt(-5) == 0);
		
		median = mf.getPercentElement(0.0f);
		assertTrue(median.getAreaAt(1) == 50);
		assertTrue(median.getAreaAt(-3) == 150);
		assertTrue(median.getAreaAt(-1) == 50);
		assertTrue(median.getAreaAt(-2) == 50);
		assertTrue(median.getAreaAt(3) == 50);
		assertTrue(median.getAreaAt(-4) == 50);
		assertTrue(median.getAreaAt(-5) == 50);
		
		mf = new MedianFinder(firstFour);
		median = mf.getPercentElement(.26f);
		assertTrue(median.getAreaAt(1) == 30);
		assertTrue(median.getAreaAt(-3) == 90);
		assertTrue(median.getAreaAt(-1) == 0);
		assertTrue(median.getAreaAt(-2) == 0);
		assertTrue(median.getAreaAt(3) == 0);
		assertTrue(median.getAreaAt(-4) == 0);
		
		mf = new MedianFinder(onlyOne);
		median = mf.getPercentElement(0);
		assertTrue(median.getAreaAt(1) == 10);
		assertTrue(median.getAreaAt(-3) == 30);
		assertTrue(median.getAreaAt(-1) == 50);
		boolean exception = false;
		try{
			median = mf.getPercentElement(1.2f);
		}catch (IndexOutOfBoundsException e)
		{
			exception = true;
		}
		assertTrue(exception);
		try{
			mf = new MedianFinder(none);
		}catch (IllegalArgumentException e)
		{
			exception = true;
		}
		assertTrue(exception);
	}
}
