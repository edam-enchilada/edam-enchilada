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
 * The Original Code is EDAM Enchilada's Datatype class.
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

package database;

import gui.MainFrame;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @author ritza
 *
 */
public class Datatype {

	private String datatype = null;	
	private Connection con;
	private Statement stmt;
	
	public Datatype(String ext) {
		try {
			con = MainFrame.db.getCon();
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.err.println("Error creating statement.");
			e.printStackTrace();
		}
		if (ext.equals("par")) 
			datatype = "ATOFMS";
		else if (ext.equals("edmf") || (ext.equals("edsf")))
			datatype = "Text";
		else if (ext.equals("ams"))
			datatype = "AMS";
		else if (ext.equals("merc"))
			datatype = "Mercury";
		else datatype = "Unknown";
	}
	
	public boolean tableExists() {
		try {
			ResultSet rs = stmt.executeQuery("SELECT * from DenseMetaData " +
					"WHERE Datatype = " + datatype);
			while (rs.next()) {
				if (datatype.equals(rs.getString(1)))
					return true;
			}
			return false;
		} 
		catch (SQLException e) { 
			System.err.println("Exception testing table for existence.");
			e.printStackTrace();
			return false;
		} 
	}
	
	public String getDatatype() {
		return datatype;
	}
	
	public void createTable() {
		try {
		if (datatype.equals("par")) {
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoDense " +
					"(AtomID INT PRIMARY KEY, [Time] DATETIME, " +
					"LaserPower REAL, [Size] REAL, ScatDelay INT, " +
					"OrigFilename VARCHAR(8000))");
			stmt.addBatch("CREATE TABLE [" + datatype + "]DataSetInfo " +
					"(DataSetID INT PRIMARY KEY, DataSet VARCHAR(1000), " +
					"MassCalFile VARCHAR(1000), SizeCalFile VARCHAR(1000), " +
					"MinHeight INT, MinArea INT, MinRelArea REAL, " +
					"Autocal BIT)");
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoSparse " +
					"(AtomID INT, PeakLocation REAL, PeakArea INT, " +
					"RelPeakArea REAL, PeakHeight INT, PRIMARY KEY " +
					"(AtomID, PeakLocation), " +
					"FOREIGN KEY (AtomID) REFERENCES AtomInfo(AtomID))");
		}
		else if (datatype.equals("edmf") || (datatype.equals("edsf"))) {
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoDense " +
					"(AtomID INT PRIMARY KEY, [Time] DATETIME, " +
					"LaserPower REAL, [Size] REAL, ScatDelay INT, " +
					"OrigFilename VARCHAR(8000))");
			stmt.addBatch("CREATE TABLE [" + datatype + "]DataSetInfo " +
					"(DataSetID INT PRIMARY KEY, DataSet VARCHAR(1000), " +
					"MassCalFile VARCHAR(1000), SizeCalFile VARCHAR(1000), " +
					"MinHeight INT, MinArea INT, MinRelArea REAL, " +
					"Autocal BIT)");
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoSparse " +
					"(AtomID INT, PeakLocation REAL, PeakArea INT, " +
					"RelPeakArea REAL, PeakHeight INT, PRIMARY KEY " +
					"(AtomID, PeakLocation), " +
					"FOREIGN KEY (AtomID) REFERENCES AtomInfo(AtomID))");
		}
		else if (datatype.equals("ams")) {
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoDense " +
			"(AtomID INT PRIMARY KEY");
			stmt.addBatch("CREATE TABLE [" + datatype + "]DataSetInfo " +
					"(DataSetID INT PRIMARY KEY, DataSet VARCHAR(1000), " +
			"Datatype VARCHAR(200)");
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoSparse " +
			"(AtomID INT, ColIndex INT");
		}
		else if (datatype.equals("merc")) {
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoDense " +
			"(AtomID INT PRIMARY KEY");
			stmt.addBatch("CREATE TABLE [" + datatype + "]DataSetInfo " +
					"(DataSetID INT PRIMARY KEY, DataSet VARCHAR(1000), " +
			"Datatype VARCHAR(200)");
			stmt.addBatch("CREATE TABLE [" + datatype + "]AtomInfoSparse " +
			"(AtomID INT, ColIndex INT");
		}
		else 
			System.err.println("Unknown Datatype.");
		stmt.executeBatch();
		} catch (SQLException e) {
			System.err.println("Exception creating tables for datatype.");
			e.printStackTrace();
		}
	}
}
