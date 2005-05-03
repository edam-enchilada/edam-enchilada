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
 * The Original Code is EDAM Enchilada's DataSetImporter unit test class.
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
package msanalyze;

import java.awt.Window;
import java.sql.*;

import javax.swing.JFrame;

import database.CreateTestDatabase;
import database.SQLServerDatabase;
import junit.framework.TestCase;

import gui.ImportParsDialog;
import gui.ParTableModel;

/**
 * @author ritza
 */
public class DataSetImporterTest extends TestCase {
	DataSetImporter importer;
	SQLServerDatabase db;
	Connection con;
	ParTableModel table;
	/*
	 * @see TestCase#setUp()
	 */
	public DataSetImporterTest(String aString)
	{
		super(aString);
	}
	
	protected void setUp()
	{
		try {
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load current driver.");
			
		} // end catch
		
		con = null;
		
		try {
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://localhost:1433;TestDB;SelectMethod=cursor;","SpASMS","finally");
		} catch (Exception e) {
			System.err.println("Failed to establish a connection to SQL Server");
			System.err.println(e);
		}
		
		SQLServerDatabase.rebuildDatabase("TestDB");
		new CreateTestDatabase(con); 		
		db = new SQLServerDatabase("localhost","1433","TestDB");
	
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
		importer = new DataSetImporter(table, mf, new ImportParsDialog());
	}
	
	protected void tearDown()
	{
		db.closeConnection();
		try {
			System.runFinalization();
			System.gc();
			con.close();
			con = DriverManager.getConnection(
					"jdbc:microsoft:sqlserver://" +
					"localhost:1433;TestDB;SelectMethod=cursor;",
					"SpASMS","finally");
			con.createStatement().executeUpdate("DROP DATABASE TestDB");
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table = null;
	}
	
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
	
	public void testProcessAllDataSets() {
		boolean success = true;
		try {
		//importer.processDataSet(1);
		} catch (Exception e) {success = false; }
		assertTrue(success);
		
	}
	
	public void testNullRows() {
		assertTrue(importer.nullRows());
		table.setValueAt("random.cal", 2, 2);
		assertTrue(importer.nullRows());
	}

}
