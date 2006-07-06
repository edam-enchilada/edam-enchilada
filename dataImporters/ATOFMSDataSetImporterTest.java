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
import javax.swing.event.TableModelEvent;

import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;


import database.CreateTestDatabase;
import database.SQLServerDatabase;
import errorframework.DisplayException;
import junit.framework.TestCase;

import gui.ImportParsDialog;
import gui.ParTableModel;

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
		SQLServerDatabase.rebuildDatabase("TestDB");
		db = new SQLServerDatabase("TestDB");
		assertEquals(true, db.openConnection());
		
		// create table with one entry.
		table = new ParTableModel(true);
		// TODO: insert dummy row.
		table.setValueAt("testRow\\b\\b.par", 0, 1);
		table.setValueAt("testRow\\b\\cal.cal", 0, 2);
		table.setValueAt(10, 0, 4);
		table.setValueAt(20, 0, 5);
		table.setValueAt(new Float(0.1), 0, 6);
		table.setValueAt(true, 0, 7);
		
		table.tableChanged(new TableModelEvent(table, 0));
		
		Window mf = (Window)new JFrame();
		importer = new ATOFMSDataSetImporter(table, mf, db);
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
	public void testCollectTableInfo() throws SQLException, DisplayException {
		importer.setParentID(0);
		importer.checkNullRows();

		importer.collectTableInfo();

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
//		
		assertEquals(1,rs.getInt(1));
		assertEquals("2004-08-04 15:39:00.0",rs.getString(2));
		assertEquals(1.031E-6,rs.getFloat(3), 0.0001);
		assertEquals(0.0,rs.getFloat(4), 0.0001);
		assertEquals(3129,rs.getInt(5));
		assertEquals("C:\\Documents and Settings\\smitht\\my documents\\workspace\\edam-enchilada\\testRow\\b\\b-040804153913-00001.amz",rs.getString(6));
		
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
}
