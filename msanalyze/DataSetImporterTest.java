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

import java.sql.*;

import database.CreateTestDatabase;
import database.SQLServerDatabase;
import junit.framework.TestCase;

import gui.ParTableModel;

/**
 * @author ritza
 */
public class DataSetImporterTest extends TestCase {
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
		// Create the test database
		try {
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load current driver.");
			
		} // end catch
		
		con = null;
		
		try {
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://localhost:1433;SpASMSdb;SelectMethod=cursor;","SpASMS","finally");
		} catch (Exception e) {
			System.err.println("Failed to establish a connection to SQL Server");
			System.err.println(e);
		}
		
		new CreateTestDatabase(con); 		
		db = new SQLServerDatabase("localhost","1433","TestDB");
		
	}
	
	protected void tearDown()
	{
		db.closeConnection();
		try {
			System.runFinalization();
			System.gc();
			con.close();
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://localhost:1433;SpASMSdb;SelectMethod=cursor;","SpASMS","finally");
			con.createStatement().executeUpdate("DROP DATABASE TestDB");
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void testProcessAllDataSets() {
		
		// Insert test row.
		//Statement statement = .CreateStatement();
	}
	
	public void testNullRows() {
	}

}
