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
 * The Original Code is EDAM Enchilada's CalInfo unit test class.
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
package msanalyze;

import junit.framework.TestCase;
import java.io.*;

/**
 * @author ritza
 */
public class CalInfoTest extends TestCase {

	private File parFile;
	private File sizeFile;
	private File massFile;
	
	public CalInfoTest(String aString)
	{
		super(aString);
	}
	
	protected void setUp() {
		CreateTestFiles files = new CreateTestFiles();
		parFile = files.getParFile();
		sizeFile = files.getSizeFile();
		massFile = files.getMassFile();
	}
	
	/*
	 * Class under test for void CalInfo(String, String, boolean)
	 * In other words, the constructor with both a size file and a mass file.
	 */
	public void testCalInfoStringStringboolean() {
		boolean exceptionCaught = false;
		try {
			new CalInfo(massFile.toString(),true);
			new CalInfo(massFile.toString(), false);
		} catch (Exception e) {
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught == false);
	}

	/*
	 * Class under test for void CalInfo(String, boolean)
	 * In other words, the constructor with just a mass file.
	 */
	public void testCalInfoStringboolean() {
		boolean exceptionCaught = false;
		try {
			new CalInfo(massFile.toString(), sizeFile.toString(), true);
			new CalInfo(massFile.toString(), sizeFile.toString(), false);
		} catch (Exception e) {
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught == false);	
	}
}
