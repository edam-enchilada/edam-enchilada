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
 * The Original Code is EDAM Enchilada's ATOFMSDataSetImporter unit test class.
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
 * Created on Aug 25, 2004s
 */
package dataImporters;

import java.awt.Window;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;

import ATOFMS.CalInfo;
import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;


import database.CreateTestDatabase;
import database.SQLServerDatabase;
import errorframework.DisplayException;
import errorframework.ErrorLogger;
import externalswing.SwingWorker;
import junit.framework.TestCase;

import gui.ImportParsDialog;
import gui.ParTableModel;
import gui.ProgressBarWrapper;

/**
 * @author ritza
 * 
 * 
 * the problem here is that CreateTestDatabase populates the database, but
 * we really want to be looking at the particles that are *imported*... huh.
 */
public class ATOFMSDataSetImporterTest extends TestCase {
	ATOFMSDataSetImporter importer;
	SQLServerDatabase db;
	ParTableModel table;
	/*
	 * @see TestCase#setUp()
	 */
	public ATOFMSDataSetImporterTest(String aString)
	{
		super(aString);
	}
	
	protected void setUp()
	{
		//TODO: commented this out AR
		try {
			SQLServerDatabase.rebuildDatabase("TestDB");
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Could not rebuild the database." +
					"  Close any other programs that may be accessing the database and try again.");
		}
		db = new SQLServerDatabase("TestDB");
		assertEquals(true, db.openConnection());
		
		// create table with one entry.
		table = new ParTableModel(true);
		// TODO: insert dummy row.
		table.setValueAt("testRow\\b\\b.par", 0, 1);   // dataset
		table.setValueAt("testRow\\b\\cal.cal", 0, 2); // mass cal file
		table.setValueAt(10, 0, 4);    // Min height
		table.setValueAt(20, 0, 5);	   // Min area
		table.setValueAt(new Float(0.1), 0, 6);  // Min relative area
		table.setValueAt(new Float(0.5), 0, 7);  // Max peak error
		table.setValueAt(true, 0, 8);  // autocal
		
		table.tableChanged(new TableModelEvent(table, 0));
		
		JFrame mf = new JFrame();
		final ProgressBarWrapper progressBar = 
			new ProgressBarWrapper(mf, "Importing ATOFMS Datasets", 100);
		progressBar.constructThis();
		//final JFrame frameRef = frame;
		//final ATOFMSBatchTableModel aRef = a;
		importer = new ATOFMSDataSetImporter(table, mf, db, progressBar);
			
	}
	
	protected void tearDown()
	{
		db.closeConnection();
		try {
			System.runFinalization();
			System.gc();
			
			SQLServerDatabase tempDB = new SQLServerDatabase();
			tempDB.openConnection();
			Connection con = tempDB.getCon();
			con.createStatement().executeUpdate("DROP DATABASE TestDB");
			tempDB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table = null;
	}
	
	
	public void testParVersion() {
		try {
			importer.parFile = new File((String)table.getValueAt(0,1));
			String[] info = importer.parVersion();
			assertTrue(info[0].equals("b"));
			assertTrue(info[1].equals("08/04/2004 15:38:47"));
			assertTrue(info[2].equals("ambient"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method in turn calls processDataSet,
	 * readParFileAndCreateEmptyCollection() and
	 * readSpectraAndCreateParticle(),
	 * so unit tests are not needed for these two.
	 *
	 */
	public void testCollectTableInfo() throws SQLException, DisplayException, InterruptedException {
		importer.setParentID(0);
		importer.checkNullRows();

		importer.collectTableInfo();

		synchronized (importer) {
			try {
				importer.wait();
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		
		Statement stmt = db.getCon().createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ATOFMSAtomInfoDense " +
				"ORDER BY AtomID");
		assertEquals(true,rs.next());
		int rowCount = 1;

		// to construct more tests like below, this is useful.
//		ResultSetMetaData rsmd = rs.getMetaData();
//		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//			System.out.println("assertEquals("+rs.getObject(i).toString()
//					+",rs.get" + rsmd.getColumnClassName(i) +"("+i+"));");
//			
//		}
		
		String dir = System.getProperty("user.dir");

		assertEquals(1,rs.getInt(1));
		assertEquals("2004-08-04 15:39:00.0",rs.getString(2));
		assertEquals(1.031E-6,rs.getFloat(3), 0.0001);
		assertEquals(0.0,rs.getFloat(4), 0.0001);
		assertEquals(3129,rs.getInt(5));
		assertEquals(dir + "\\testRow\\b\\b-040804153913-00001.amz",rs.getString(6));
		
		while (rs.next()) {
			rowCount++;
		}
		assertEquals(10, rowCount);
		
		ArrayList<Integer> atomIDs = db.getAllDescendedAtoms(db.getCollection(0));
		java.util.Collections.sort(atomIDs);
		for(int i = 1; i <= atomIDs.size(); i++)
			assertEquals(i, atomIDs.get(i).intValue());
		
		assertEquals("b", db.getCollectionName(2));
	}
	
	/**
	 * Creates some functionality for a file that can be modified and then have its contents reset.
	 * Potentially useful for writing tests that depend upon files to run.
	 * Must invoke close() to write back the original contents.
	 * @author shaferia
	 */
	public static class ResettableFile {
		private File file;
		private String current;
		private String original;
		
		public ResettableFile(File f) {
			this(f.getAbsolutePath());
		}
		
		public ResettableFile(String filename) {
			file = new File(filename);
			
			if (!file.exists())
				throw new IllegalArgumentException("ResettableFile: file doesn't exist: " + filename);
			
			original = readFile(file);
		}
		
		private String readFile(File f) {
			try {
				StringBuffer in = new StringBuffer();
				java.io.FileReader fw = new java.io.FileReader(f);
				while (fw.ready()) {
					in.append((char) fw.read());
				}
				return in.toString();
			}
			catch (java.io.IOException ex) {
				System.err.println("ResettableFile: couldn't read file: " + f.getAbsolutePath());
				return null;
			}
		}
		
		private void writeFile(File f, String contents) {
			try {
				java.io.FileWriter fw = new java.io.FileWriter(f);
				fw.write(contents);
			}
			catch (java.io.IOException ex) {
				System.err.println("ResettableFile: couldn't write file: " + f.getAbsolutePath());
			}			
		}
		
		public String getCurrent() {
			return current;
		}
		
		public void setCurrent(String current) {
			this.current = current;
			writeFile(file, this.current);
		}
		
		public String getOriginal() {
			return original;
		}
		
		public void replaceFirst(String regex, String replacement) {
			current.replaceFirst(regex, replacement);
			writeFile(file, current);
		}
		
		public void replaceAll(String regex, String replacement) {
			current.replaceAll(regex, replacement);
			writeFile(file, current);
		}
		
		public void close() {
			writeFile(file, original);
		}
	}
	
	public void testReadParFileAndCreateEmptyCollection() {
		try {
			importer.parFile = new File((String)table.getValueAt(0,1));
			ATOFMS.ATOFMSParticle.currCalInfo = new CalInfo("testRow\\b\\cal.cal", true);
			ATOFMS.ATOFMSParticle.currPeakParams = new ATOFMS.PeakParams(10, 20, .1f, .5f);			
		}
		catch (java.io.IOException ex) {
			fail("Couldn't open necessary files to create empty collection");
		}
		
		try {
			importer.readParFileAndCreateEmptyCollection();
		}
		catch (java.io.IOException ex) {
			fail();
		}
		catch (DataFormatException ex) {
			fail();
		}
		
		try {
			Connection con = db.getCon();
			
			ResultSet rs = con.createStatement().executeQuery(
					"SELECT * FROM Collections WHERE CollectionID = 2");

			assertTrue(rs.next());
			assertEquals(rs.getString("Comment"), "ambient");
			assertEquals(rs.getString("Description"), "b: ambient");
			assertEquals(rs.getString("Datatype"), "ATOFMS");
			
			assertFalse(rs.next());
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			fail("Couldn't analyze success of readParFileAndCreateEmptyCollection");
		}
	}
}