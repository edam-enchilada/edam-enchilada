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
 * The Original Code is EDAM Enchilada's ATOFMSCalInfo unit test class.
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
 * Created on Sep 16, 2004
 *
 * TODO This isn't working - I think it might be the files I wrote.
 * TODO:  Who wrote the above todo and what does it mean? -Ben
 */
package ATOFMS;

import junit.framework.TestCase;
import java.io.*;

/**
 * @author ritza
 */
public class ATOFMSCalInfoTest extends TestCase {

	private File sizeFile;
	private File massFile;
	private ATOFMSCalInfo calInfo;
	
	public ATOFMSCalInfoTest(String aString)
	{
		super(aString);
	}
	
	protected void setUp() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter("Test.cal"));
			for (int i=0;i<2;i++) {
			writer.println("0.999");
			writer.println("-0.999");
			}
			for (int i=0;i<8;i++)
			writer.println("9999,9.999");
			
			writer.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		massFile = new File("Test.cal");
		
		try {
			PrintWriter writer = new PrintWriter(new FileWriter("Test.noz"));
			writer.println("[ATOFMS Particle Size Calibration]");
			writer.println("Comment=this is a test");
			writer.println("C1=0.999");
			writer.println("C2=0.999");
			writer.println("C3=0.999");
			writer.println("C4=0.999");
			writer.println("this is a test");
			writer.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		sizeFile = new File("Test.noz");
	}
	
	protected void tearDown() {
		sizeFile.delete();
		massFile.delete();
	}
	
	public void testCalInfo() {
		calInfo = new ATOFMSCalInfo();
		assertTrue(calInfo.negSlope == 1);
		assertTrue(calInfo.posSlope == 1);
		assertTrue(calInfo.negIntercept == 0);
		assertTrue(calInfo.posIntercept == 0);
		assertTrue(calInfo.c1 == 1);
		assertTrue(calInfo.c2 == 0);
		assertTrue(calInfo.c3 == 0);
		assertTrue(calInfo.c4 == 0);
		assertFalse(calInfo.sizecal);
		assertFalse(calInfo.autocal);
	}
	
	public void testCalInfoMassBoolean() {
		try {
			calInfo = new ATOFMSCalInfo(massFile.toString(),true);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(calInfo.negSlope == 0.999);
		assertTrue(calInfo.posSlope == 0.999);
		assertTrue(calInfo.negIntercept == -0.999);
		assertTrue(calInfo.posIntercept == -0.999);
		assertTrue(calInfo.c1 == 0);
		assertTrue(calInfo.c2 == 0);
		assertTrue(calInfo.c3 == 0);
		assertTrue(calInfo.c4 == 0);
		assertFalse(calInfo.sizecal);
		assertTrue(calInfo.autocal);
	}
	
	public void testCalInfoMassSizeBoolean() {
		try {
			calInfo = new ATOFMSCalInfo(massFile.toString(), sizeFile.toString(),true);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(calInfo.negSlope == 0.999);
		assertTrue(calInfo.posSlope == 0.999);
		assertTrue(calInfo.negIntercept == -0.999);
		assertTrue(calInfo.posIntercept == -0.999);
		assertTrue(calInfo.c1 == 0.999f);
		assertTrue(calInfo.c2 == 0.999f);
		assertTrue(calInfo.c3 == 0.999f);
		assertTrue(calInfo.c4 == 0.999f);
		assertTrue(calInfo.sizecal);
		assertTrue(calInfo.autocal);
	}
}