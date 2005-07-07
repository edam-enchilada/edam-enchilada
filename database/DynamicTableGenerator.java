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
 * The Original Code is EDAM Enchilada's DynamicTableGenerator class.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * 
 * @author ritza
 * 
 * DynamicTableGenerator class has the ability to read a ".md" file and 
 * generate the corresponding dynamic tables.  It also inserts information
 * into the MetaData table.
 *
 */
public class DynamicTableGenerator {
	
	private Connection con;
	private Statement stmt;
	
	private Scanner scanner = null;
	private final String DELIMITER = "^^^^^^^^";
	
	private String datatype = null;	
	String primaryKey;
	
	public DynamicTableGenerator(File file, Connection connection) {
		
		try {
			con = connection;
			assert (con.createStatement() != null) : "connection shouldn't be null";
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.err.println("Error creating statement.");
			e.printStackTrace();
		}
		
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println(file + " is not found.");
			e.printStackTrace();
		}		
		assert(scanner != null) : "Error generating scanner.";
		
		datatype = scanner.nextLine();
		scanner.next();
	}
	
	/**
	 * method to see if a set of dynamic tables exist for this datatype
	 * @return true if they exist.
	 */
	public boolean tableExists() {
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM MetaData");
			while (rs.next()) {
				if (datatype.equals(rs.getString(1))) {
					//System.out.println("tables exist");
					return true;
				}
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

	/**
	 * This method iterates through the file and inserts the columns into
	 * the MetaData table.  it then uses the MetaData table to create the
	 * three dynamic tables.
	 * 
	 *Note, there were problems with fields named Size and Time, etc.
	 *I bracket every field name to prevent this.
	 *
	 *@return field that will be used for naming purposes.
	 */
	public String createTables() {
		String namingField = "";
		ResultSet rs;
		int counter = 0;
		System.out.println("Executing the following statements:");
		String tempStr,metaString = null;
		// Insert columns and types into MetaData table first.
		try {			
			// set DataSetInfo statement.
			tempStr = scanner.next();
			stmt.addBatch("INSERT INTO MetaData VALUES ('" + datatype + "', '[DataSetID]', 'INT', 1," + DynamicTable.DataSetInfo.ordinal() + ", " + counter + ")");
			counter++;
			while (!tempStr.equals(DELIMITER)) {
				metaString = "INSERT INTO MetaData VALUES ('" + datatype + "','[" + tempStr + "]','";
				tempStr = scanner.next().toUpperCase();
				assert (!tempStr.equals(DELIMITER)) : "No Column Type found.";
				if (tempStr.equals("VARCHAR")) 
					metaString += tempStr + "(8000)',0, " + DynamicTable.DataSetInfo.ordinal() + ", " + counter + ")";				
				else 
					metaString += tempStr + "',0," + DynamicTable.DataSetInfo.ordinal() + "," + counter + ")"; 
				counter++;
				System.out.println(metaString);
				stmt.addBatch(metaString);
				tempStr = scanner.next();
			}
			counter = 0;
						
			// set AtomInfoDense statement
			tempStr = scanner.next();
			stmt.addBatch("INSERT INTO MetaData VALUES ('" + datatype + "', '[AtomID]', 'INT',1, " + DynamicTable.AtomInfoDense.ordinal() + ", " + counter + ")");
			counter++;
			while (!tempStr.equals(DELIMITER)) {
				metaString = "INSERT INTO MetaData VALUES ('" + datatype + "','[" + tempStr + "]','";
				tempStr = scanner.next().toUpperCase();
				assert (!tempStr.equals(DELIMITER)) : "No Column Type found.";
				if (tempStr.equals("VARCHAR")) 
					metaString += tempStr + "(8000)',0," + DynamicTable.AtomInfoDense.ordinal() + "," + counter + ")";
				else 
					metaString += tempStr + "',0," + DynamicTable.AtomInfoDense.ordinal() + "," + counter + ")"; 
				counter++;
				System.out.println(metaString);
				stmt.addBatch(metaString);
				tempStr = scanner.next();
			}
			counter = 0;
			
			// set AtomInfoSparse statement				
			stmt.addBatch("INSERT INTO MetaData VALUES ('" + datatype + "', '[AtomID]', 'INT', 1," + DynamicTable.AtomInfoSparse.ordinal() + ", " + counter + ")");
			counter++;
			while (scanner.hasNext()) {
				tempStr = scanner.next();
				assert (!tempStr.equals(DELIMITER)) : "Too many tables specified.";
				int primary = 0;
				if (tempStr.equals("P")) {
					tempStr = scanner.next();
					primary = 1;
				}
					metaString = "INSERT INTO MetaData VALUES ('" + datatype + "','[" + tempStr + "]','";
				assert (scanner.hasNext()) : "No Column Type Found.";
				tempStr = scanner.next().toUpperCase();
				if (tempStr.equals("VARCHAR")) 
					metaString += tempStr + "(8000)'," + primary + ", " + DynamicTable.AtomInfoSparse.ordinal() + "," + counter + ")";
				else 
					metaString += tempStr + "'," + primary + ", " + DynamicTable.AtomInfoSparse.ordinal() + "," + counter + ")"; 
				counter++;
				System.out.println(metaString);
				stmt.addBatch(metaString);
			}
			stmt.executeBatch();
		} catch (NullPointerException e) {
			System.err.println("Improper file format");
			e.printStackTrace();
		}  catch (SQLException e) {
			System.err.println("Error inserting rows into MetaDatatable");
			e.printStackTrace();
		}
		
		// Create tables based on columns in MetaData table.
		String tableStr = null;
		Scanner wordScanner;
		try {
			
			// Create DataSetInfo table

			tableStr = "CREATE TABLE " + datatype + "DataSetInfo (";	
			rs = stmt.executeQuery("SELECT ColumnName, ColumnType, PrimaryKey FROM MetaData " +
					"WHERE Datatype = '" + datatype + "' AND TableID = " + 
					DynamicTable.DataSetInfo.ordinal() + "ORDER BY ColumnOrder");
			String pText = "PRIMARY KEY(";
			while (rs.next()) {
				if (rs.getBoolean(3)) {
					pText += rs.getString(1) + ", ";
				}
				tableStr += rs.getString(1) + " " + rs.getString(2) + ", ";
			}
			tableStr += pText.substring(0,pText.length()-2) + "))";
			System.out.println(tableStr);
			stmt.execute(tableStr);
			
			// Create AtomInfoDense table
			tableStr = "CREATE TABLE " + datatype + "AtomInfoDense (";		
			rs = stmt.executeQuery("SELECT ColumnName, ColumnType, PrimaryKey FROM MetaData " +
					"WHERE Datatype = '" + datatype + "' AND TableID = " + 
					DynamicTable.AtomInfoDense.ordinal() + "ORDER BY ColumnOrder");
			pText = "PRIMARY KEY(";
			while (rs.next()) {
				if (rs.getBoolean(3)) {
					pText += rs.getString(1) + ", ";
				}
				tableStr += rs.getString(1) + " " + rs.getString(2) + ", ";
			}
			tableStr += pText.substring(0,pText.length()-2) + "))";
			System.out.println(tableStr);
			stmt.execute(tableStr);
			
			// Create AtomInfoSparse table
			tableStr = "CREATE TABLE " + datatype + "AtomInfoSparse (";
			rs = stmt.executeQuery("SELECT ColumnName, ColumnType, PrimaryKey FROM MetaData " +
					"WHERE Datatype = '" + datatype + "' AND TableID = " + 
					DynamicTable.AtomInfoSparse.ordinal() + "ORDER BY ColumnOrder");
			pText = "PRIMARY KEY (";
			while (rs.next()) {
				if (rs.getBoolean(3)) {
					pText += rs.getString(1) + ", ";
				}
				tableStr += rs.getString(1) + " " + rs.getString(2) + ", ";
			}
			tableStr += pText.substring(0,pText.length()-2) + "))";
			System.out.println(tableStr);
			stmt.execute(tableStr);
			rs.close();
		} catch (SQLException e) {
			System.err.println("Error creating dynamic tables");
			e.printStackTrace();
		}
		assert (namingField.length() != 0) : "Naming field not specified.";
		return namingField;
	}
	
	public static void main(String[] args) {
		SQLServerDatabase db = new SQLServerDatabase();
		db.openConnection();
		Connection con = db.getCon();
		//SQLServerDatabase.rebuildDatabase("SpASMSdb");
		DynamicTableGenerator d = new DynamicTableGenerator(new File("ATOFMS.md"), con);
		if (!d.tableExists())
			d.createTables();
		db.closeConnection();
	}
}

