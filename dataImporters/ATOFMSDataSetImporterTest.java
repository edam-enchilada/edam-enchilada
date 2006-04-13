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

import javax.swing.JFrame;

import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;


import database.CreateTestDatabase;
import database.SQLServerDatabase;
import junit.framework.TestCase;

import gui.ImportParsDialog;
import gui.ParTableModel;

/**
 * @author ritza
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
		//SQLServerDatabase.rebuildDatabase("TestDB");
	
		new CreateTestDatabase(); 
		db = new SQLServerDatabase("TestDB");
		
		// create table with one entry.
		table = new ParTableModel();
		// TODO: insert dummy row.
		table.setValueAt("testRow/b.par", 1, 1);
		table.setValueAt("testRow/cal.cal", 1, 2);
		table.setValueAt(10, 1, 4);
		table.setValueAt(20, 1, 5);
		table.setValueAt(new Float(0.1), 1, 6);
		table.setValueAt(true, 1, 7);
		
		Window mf = (Window)new JFrame();
		importer = new ATOFMSDataSetImporter(table, mf);
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
	
	/* NullRows needs some work; write the test later.
	 * 
	 */
	public void testNullRows() {
		
	}
	
	public void testParVersion() {
		try {
			importer.parFile = new File((String)table.getValueAt(1,1));
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
	 * This method in turn calls 
	 * readParFileAndCreateEmptyCollection() and
	 * readSpectraAndCreateParticle(),
	 * so unit tests are not needed for these two.
	 *
	 */
	public void testCollectTableInfo() {
		// Table values for this row.
		String name = (String)table.getValueAt(1,1);
		String massCalFile = (String)table.getValueAt(1,2);
		String sizeCalFile = (String)table.getValueAt(1,3);
		int height= ((Integer)table.getValueAt(1,4)).intValue();
		int area = ((Integer)table.getValueAt(1,5)).intValue();
		float relArea = ((Float)table.getValueAt(1,6)).floatValue();
		boolean autoCal = ((Boolean)table.getValueAt(1,7)).booleanValue();
		
		assertTrue(name.equals("testRow/b.par"));
		assertTrue(massCalFile.equals("testRow/cal.cal"));
		assertTrue(height == 10);
		assertTrue(area == 20);
		assertTrue(relArea == (float)0.1);
		assertTrue(autoCal = true);
	}
	
	public void testProcessDataSet() {
		db.openConnection();
		boolean success = true;
		try {
			importer.processDataSet(1);			
		} catch (Exception e) {success = false; }
		assertTrue(success);
		
		assertTrue(db.getCollectionComment(2).equals("one"));
		assertTrue(db.getCollectionDescription(2).equals("onedescrip"));
		assertTrue(db.getCollectionName(2).equals("One"));
		
		Statement stmt;
		try {
			stmt = db.getCon().createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ATOFMSAtomInfoDense " +
				"WHERE AtomID = 1 OR AtomID = 5 ORDER BY AtomID");
		// Check the first and last particles to see if they have been
		// imported properly.
		
		assertTrue(rs.next());
		assertTrue(rs.getInt(1) == 1);
		assertTrue(rs.getTimestamp(2).toString().equals("2003-09-02 17:30:38.0"));
		System.out.println(rs.getFloat(3));
		assertEquals(0, rs.getInt(3));
		assertTrue(rs.getFloat(4) == 0.1f);
		assertTrue(rs.getInt(5) == 0);
		assertTrue(rs.getString(6).equals("One"));


		assertTrue(rs.next());
		assertTrue(rs.next());
		assertTrue(rs.getInt(1) == 5);
		assertTrue(rs.getTimestamp(2).toString().equals("09/02/2003 05:30:38 PM"));
		assertTrue(rs.getInt(3) == 0);
		assertTrue(rs.getFloat(4) == 0.5f);
		assertTrue(rs.getInt(5) == 0);
		assertTrue(rs.getString(6).equals("Five"));
		
		stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.closeConnection();	
	}	
}
