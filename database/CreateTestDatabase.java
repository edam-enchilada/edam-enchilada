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
 * The Original Code is EDAM Enchilada's SQLException class.
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
 * Created on Aug 25, 2004
 */
package database;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

/**
 * @author ritza
 */
public class CreateTestDatabase {
	SQLServerDatabase tempDB;
	Connection con;
	
	public CreateTestDatabase() {
        tempDB = new SQLServerDatabase();
        tempDB.openConnection();
        con = tempDB.getCon();
        SQLServerDatabase.rebuildDatabase("TestDB");
    		
		try {
			assert (con.createStatement() != null): "con should not be null";
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		generateDynamicTables();
    		
	    try {
			Statement stmt = con.createStatement();
			// Create a database with tables mirroring those in the 
			// real one so we can test on that one and make sure we
			// know what the results should be.
			//stmt.executeUpdate("CREATE DATABASE TestDB");
			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (1,'9/2/2003 5:30:38 PM',1,0.1,1,'One') " +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (2,'9/2/2003 5:30:38 PM',2,0.2,2,'Two') " +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (3,'9/2/2003 5:30:38 PM',3,0.3,3,'Three') " +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (4,'9/2/2003 5:30:38 PM',4,0.4,4,'Four')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (5,'9/2/2003 5:30:38 PM',5,0.5,5,'Five')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (6,'9/2/2003 5:30:38 PM',6,0.6,6,'Six')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (7,'9/2/2003 5:30:38 PM',7,0.7,7,'Seven')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (8,'9/2/2003 5:30:38 PM',8,0.8,8,'Eight')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (9,'9/2/2003 5:30:38 PM',9,0.9,9,'Nine')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (10,'9/2/2003 5:30:38 PM',10,0.01,10,'Ten')\n" +
					"INSERT INTO ATOFMSAtomInfoDense VALUES (11,'9/2/2003 5:30:38 PM',11,0.11,11,'Eleven')\n");
					
			stmt.executeUpdate(
					"USE TestDB\n" +		
			"INSERT INTO Datatype2AtomInfoDense VALUES (12, 1, 2)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (13, 1, 2)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (14, 1, 1)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (15, 3, 21)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (16, .5, 1)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (17, .4, 5)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (18, 10, 1)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (19, .75, 50)\n" +
					"INSERT INTO Datatype2AtomInfoDense VALUES (20, 1, 2)\n" + 
					"INSERT INTO Datatype2AtomInfoDense VALUES (21, 5, 2)\n");
		
			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO Collections VALUES (2,'One', 'one', 'onedescrip', 'ATOFMS')\n" +
					"INSERT INTO Collections VALUES (3,'Two', 'two', 'twodescrip', 'ATOFMS')\n" +
					"INSERT INTO Collections VALUES (4,'Three', 'three', 'threedescrip', 'Datatype2')\n" +
					"INSERT INTO Collections VALUES (5,'Four', 'four', 'fourdescrip', 'Datatype2')\n" +
					"INSERT INTO Collections VALUES (6, 'Five', 'five', 'fivedescrip', 'Datatype2')\n");
					
			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO AtomMembership VALUES(2,1)\n" +
					"INSERT INTO AtomMembership VALUES(2,2)\n" +
					"INSERT INTO AtomMembership VALUES(2,3)\n" +
					"INSERT INTO AtomMembership VALUES(2,4)\n" +
					"INSERT INTO AtomMembership VALUES(2,5)\n" +
					"INSERT INTO AtomMembership VALUES(3,6)\n" +
					"INSERT INTO AtomMembership VALUES(3,7)\n" +
					"INSERT INTO AtomMembership VALUES(3,8)\n" +
					"INSERT INTO AtomMembership VALUES(3,9)\n" +
					"INSERT INTO AtomMembership VALUES(3,10)\n" +
					"INSERT INTO AtomMembership VALUES(4,11)\n" +
					"INSERT INTO AtomMembership VALUES(4,12)\n" +
					"INSERT INTO AtomMembership VALUES(4,13)\n" +
					"INSERT INTO AtomMembership VALUES(4,14)\n" +
					"INSERT INTO AtomMembership VALUES(4,15)\n" +
					"INSERT INTO AtomMembership VALUES(5,16)\n" +
					"INSERT INTO AtomMembership VALUES(5,17)\n" +
					"INSERT INTO AtomMembership VALUES(5,18)\n" +
					"INSERT INTO AtomMembership VALUES(5,19)\n" +
					"INSERT INTO AtomMembership VALUES(5,20)\n" +
					"INSERT INTO AtomMembership VALUES(6,21)\n" );

			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO CollectionRelationships VALUES(0,2)\n" +
					"INSERT INTO CollectionRelationships VALUES(0,3)\n" +
					"INSERT INTO CollectionRelationships VALUES(0,4)\n" +
					"INSERT INTO CollectionRelationships VALUES(0,5)\n" +
					"INSERT INTO CollectionRelationships VALUES(5,6)");

			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO ATOFMSDataSetInfo VALUES(1,'One','aFile','anotherFile',12,20,0.005,1)");
					stmt.executeUpdate(
							"USE TestDB\n" +
							"INSERT INTO Datatype2DataSetInfo VALUES(1,'9/2/2003 5:30:38 PM',100)");	

			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO DataSetMembers VALUES(1,1)\n" +
					"INSERT INTO DataSetMembers VALUES(1,2)\n" +
					"INSERT INTO DataSetMembers VALUES(1,3)\n" +
					"INSERT INTO DataSetMembers VALUES(1,4)\n" +
					"INSERT INTO DataSetMembers VALUES(1,5)\n" +
					"INSERT INTO DataSetMembers VALUES(1,6)\n");
		 
			stmt.executeUpdate(
					"USE TestDB\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(2,-30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(2,30,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(3,-30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(3,30,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(3,45,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(4,-30,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(4,-20,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(4,-10,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(4,20,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(5,-300,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(5,-30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(5,-20,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(5,6,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(5,30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(6,-306,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(6,-300,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(6,-30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(6,30,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(6,300,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(6,306,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,-307,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,-300,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,-30,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,-15,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,30,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,230,15,0.006,12)\n" +
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(7,300,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,-430,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,-308,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,-300,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,-30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,70,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,80,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(8,800,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(9,-30,15,0.006,12)\n" + 
					"INSERT INTO ATOFMSAtomInfoSparse VALUES(10,-30,15,0.006,12)\n");
					
			stmt.executeUpdate(
					"USE TestDB\n" +
			"INSERT INTO Datatype2AtomInfoSparse VALUES(11,1,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(11,2,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(12,1,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(12,2,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(12,3,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(13,1,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(13,2,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(13,3,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(14,1,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(15,2,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(15,3,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(16,1,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(16,2,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(17,1,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(18,1,0)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(19,1,1)\n" + 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(20,2,1)\n"+ 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(21,1,0)\n"+ 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(21,2,0)\n"+ 
					"INSERT INTO Datatype2AtomInfoSparse VALUES(21,3,0)\n"); 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		tempDB.closeConnection();
	}
	
	private void generateDynamicTables() {
		try {
			Statement stmt = con.createStatement();
			
			stmt.executeUpdate(
					"Use TestDB " + 
			"INSERT INTO MetaData VALUES ('Datatype2','[DataSetID]','INT',1,0,1)\n");
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[Time]','DATETIME',0,0,2)\n");
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[Number]','INT',0,0,3)\n");
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[AtomID]','INT',1,1,1)\n" );
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[Size]','REAL',0,1,2)\n" );
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[Magnitude]','REAL',0,1,3)\n" );
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[AtomID]','INT',1,2,1)\n");
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[Delay]','INT',1,2,2)\n");
			stmt.executeUpdate("INSERT INTO MetaData VALUES ('Datatype2','[Valid]','BIT',0,2,3)\n");
			stmt.executeUpdate("CREATE TABLE Datatype2DataSetInfo ([DataSetID] INT, [Time] DATETIME, [Number] INT,  PRIMARY KEY ([DataSetID]))\n" );
			stmt.executeUpdate("CREATE TABLE Datatype2AtomInfoDense ([AtomID] INT, [Size] REAL, [Magnitude] REAL,  PRIMARY KEY ([AtomID]))\n" );
			stmt.executeUpdate("CREATE TABLE Datatype2AtomInfoSparse ([AtomID] INT, [Delay] INT, [Valid] BIT, PRIMARY KEY ([AtomID], [Delay]))");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
