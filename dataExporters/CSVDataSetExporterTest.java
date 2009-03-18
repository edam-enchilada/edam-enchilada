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
 * The Original Code is EDAM Enchilada's Database class.
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
 * Greg Cipriano gregc@cs.wisc.edu
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.JFrame;

import collection.Collection;

import junit.framework.TestCase;
import database.CreateTestDatabase;
import database.Database;
import database.InfoWarehouse;
import gui.ProgressBarWrapper;

/**
 * Tests CSV export as implemented in CSVDataSetExporter
 * We don't really need to test the db parts since that's already tested in
 * DatabaseTest.  We only test the file stuff, really.
 * 2009-03-12
 * @author jtbigwoo
 */
public class CSVDataSetExporterTest extends TestCase {
	CSVDataSetExporter exporter;
	InfoWarehouse db;
	File csvFile;
	
	public CSVDataSetExporterTest(String s) {
		super(s);
	}
	
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		new CreateTestDatabase();
		
		db = (Database) Database.getDatabase("TestDB");
		if (! db.openConnection("TestDB")) {
			throw new Exception("Couldn't open DB con");
		}
		JFrame mf = new JFrame();
		final ProgressBarWrapper progressBar = 
			new ProgressBarWrapper(mf, "Exporting to CSV", 100);
		//progressBar.constructThis();
		//final JFrame frameRef = frame;
		//final ATOFMSBatchTableModel aRef = a;
		exporter = new CSVDataSetExporter(mf, db, progressBar);
	}

	public void testCollectionExport() throws Exception {
		boolean result;
		Collection coll = db.getCollection(2);
		csvFile = File.createTempFile("test", ".csv");
		result = exporter.exportToCSV(coll, csvFile.getPath(), 30);
		assertTrue("Failure during exportToCSV in a normal export", result);
		
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		
		assertEquals("****** Particle: 1 ******", reader.readLine());
		assertEquals("Negative Spectrum", reader.readLine());
		assertEquals("-30,0.00", reader.readLine());
		assertEquals("-29,0.00", reader.readLine());
		for (int i = 0; i < 60; i++)
			reader.readLine();
		assertEquals("****** Particle: 2 ******", reader.readLine());
		assertEquals("Negative Spectrum", reader.readLine());
		assertEquals("-30,15.00", reader.readLine());
		for (int i = 0; i < 29; i++)
			reader.readLine();
		assertEquals("Positive Spectrum", reader.readLine());
	}
	
	public void testSingleParticleExport() throws Exception {
		boolean result;
		ArrayList<Integer> atomIDs = new ArrayList<Integer>();
		atomIDs.add(new Integer(3));
		csvFile = File.createTempFile("test", ".csv");
		result = exporter.exportToCSV(atomIDs, csvFile.getPath(), 30);
		assertTrue("Failure during exportToCSV in a normal export", result);
		
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		
		assertEquals("****** Particle: 3 ******", reader.readLine());
		assertEquals("Negative Spectrum", reader.readLine());
		assertEquals("-30,15.00", reader.readLine());
		assertEquals("-29,0.00", reader.readLine());
		for (int i = 0; i < 28; i++)
			reader.readLine();
		assertEquals("Positive Spectrum", reader.readLine());
		assertEquals("0,0.00", reader.readLine());
		for (int i = 0; i < 29; i++)
			reader.readLine();
		assertEquals("30,15.00", reader.readLine());
		assertEquals(null, reader.readLine());
	}
	
	public void testMultipleParticleExport() throws Exception {
		boolean result;
		ArrayList<Integer> atomIDs = new ArrayList<Integer>();
		atomIDs.add(new Integer(3));
		atomIDs.add(new Integer(4));
		csvFile = File.createTempFile("test", ".csv");
		result = exporter.exportToCSV(atomIDs, csvFile.getPath(), 30);
		assertTrue("Failure during exportToCSV in a normal export", result);
		
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		
		assertEquals("****** Particle: 3 ******", reader.readLine());
		assertEquals("Negative Spectrum", reader.readLine());
		assertEquals("-30,15.00", reader.readLine());
		for (int i = 0; i < 60; i++)
			reader.readLine();
		assertEquals("30,15.00", reader.readLine());
		assertEquals("****** Particle: 4 ******", reader.readLine());
		assertEquals("Negative Spectrum", reader.readLine());
		assertEquals("-30,15.00", reader.readLine());
		for (int i = 0; i < 61; i++)
			reader.readLine();
		assertEquals(null, reader.readLine());
	}
	
	public void tearDown()
	{
		if (csvFile != null) csvFile.delete();
		db.closeConnection();
	}
}
