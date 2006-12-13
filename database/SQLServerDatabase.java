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
 * The Original Code is EDAM Enchilada's SQLServerDatabase class.
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
 * Created on Jul 20, 2004
 *
 * 
 */
package database;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Makes database work with SQL Server
 * @author andersbe, shaferia
 */
public class SQLServerDatabase extends Database
{	
	//This isn't used anymore. Safe for deletion.
	private static int instance = 0;
	
	/**
	 * Connect to the database using default settings, or overriding them with
	 * the SQLServer section from config.ini if applicable.
	 */
	public SQLServerDatabase()
	{
		url = "localhost";
		port = "1433";
		database = "SpASMSdb";
		loadConfiguration("SQLServer");
	}
	
	public SQLServerDatabase(String dbName) {
		this();
		database = dbName;
	}

	/**
	 * @see InfoWarehouse.java#isPresent()
	 */
	public boolean isPresent() {
		return isPresentImpl("EXEC sp_helpdb");
	}
	
	/**
	 * Open a connection to a MySQL database:
	 * uses the jtds driver from jtds-*.jar
	 * TODO: change security model
	 */
	public boolean openConnection() {

		return openConnectionImpl(
				"net.sourceforge.jtds.jdbc.Driver",
				"jdbc:jtds:sqlserver://" + url + ":" + port + ";DatabaseName=" + database + ";SelectMethod=cursor;",
				"SpASMS",
				"finally");
		
		
	}
	
	/**
	 * @return the SQL Server native DATETIME format
	 */
	public DateFormat getDateFormat() {
		return new SimpleDateFormat("MM-dd-yy HH:mm:ss");
	}
	
	/**
	 * @return a BatchExecuter that uses StringBuilder concatenation
	 * to build queries. According to earlier documentation,  this is 
	 * faster than equivalent addBatch() and executeBatch()
	 */
	protected BatchExecuter getBatchExecuter(Statement stmt) {
		return new StringBatchExecuter(stmt);
	}
	
	protected class StringBatchExecuter extends BatchExecuter {
		private StringBuilder sb;
		public StringBatchExecuter(Statement stmt) {
			super(stmt);
			sb = new StringBuilder();
		}
		
		public void append(String sql) throws SQLException  {
			if (sql.endsWith("\n"))
				sb.append(sql);
			else {
				sb.append(sql);
				sb.append("\n");
			}
		}

		public void execute() throws SQLException {
			stmt.execute(sb.toString());
		}
	}

	/**
	 * @return a SQL Server BulkInserter that reads from comma-delimited bulk files
	 */
	protected BulkInserter getBulkInserter(BatchExecuter stmt, String table) {
		return new BulkInserter(stmt, table) {
			protected String getBatchSQL() {
				return "BULK INSERT " + table + " FROM '" + tempFilename + "' WITH (FIELDTERMINATOR=',')";
			}
		};
	}
}