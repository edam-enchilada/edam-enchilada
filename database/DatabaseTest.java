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
 * The Original Code is EDAM Enchilada's SQLServerDatabase unit test class.
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
 * Created on Jul 29, 2004
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package database;

import gui.ProgressBarWrapper;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JDialog;

import collection.Collection;


import ATOFMS.ATOFMSParticle;
import ATOFMS.ATOFMSPeak;
import ATOFMS.CalInfo;
import ATOFMS.ParticleInfo;
import ATOFMS.Peak;
import ATOFMS.PeakParams;
import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;

/**
 * @author andersbe
 *
 */
public class DatabaseTest extends TestCase {
	private Database db;
	
	public DatabaseTest(String aString)
	{
		
		super(aString);
	}
	
	protected void setUp()
	{
		new CreateTestDatabase(); 		
		db = (Database) Database.getDatabase("TestDB");
	}
	
	protected void tearDown()
	{
		db.closeConnection();
		try {
			System.runFinalization();
			System.gc();
			db = (Database) Database.getDatabase("");
			db.openConnection();
			Connection con = db.getCon();
			con.createStatement().executeUpdate("DROP DATABASE TestDB");
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Assert that the Runnable passed throws an exception
	 * @param r the Runnable to invoke run() on. Note: not invoked in a separate thread.
	 * @author shaferia
	 */
	public static void assertException(Runnable r) {
		try {
			r.run();
			fail("Test should have thrown exception.");
		}
		catch (Exception ex) {
			//test passed.
		}
	}
	
	public void testOpenandCloseConnection() {
		assertTrue(db.openConnection());
		assertTrue(db.closeConnection());
	}

	public void testGetImmediateSubcollections() {
	
		
		db.openConnection();
		
		ArrayList<Integer> test = db.getImmediateSubCollections(db.getCollection(0));
		
		assertTrue(test.size() == 4);
		assertTrue(test.get(0).intValue() == 2);
		assertTrue(test.get(1).intValue() == 3);
		assertTrue(test.get(2).intValue() == 4);
		assertTrue(test.get(3).intValue() == 5);
		
		ArrayList<Integer> collections = new ArrayList<Integer>();
		collections.add(new Integer(0));
		collections.add(new Integer(3));
		test = db.getImmediateSubCollections(collections);
		assertTrue(test.size() == 4);
		assertTrue(test.get(0).intValue() == 2);
		assertTrue(test.get(1).intValue() == 3);
		assertTrue(test.get(2).intValue() == 4);
		assertTrue(test.get(3).intValue() == 5);
		
		db.closeConnection();
	}
	
	
	public void testCreateEmptyCollectionAndDataset() {
		db.openConnection();
		
		int ids[] = db.createEmptyCollectionAndDataset("ATOFMS", 0,
				"dataset",  "comment", "'mCalFile', 'sCalFile', 12, 20, 0.005, 0");
		
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			ResultSet rs = con.createStatement().executeQuery(
					"USE TestDB\n" +
					"SELECT *\n" +
					"FROM ATOFMSDataSetInfo\n" +
					"WHERE DataSetID = " + ids[1]);
			assertTrue(rs.next());
			assertTrue(rs.getString(2).equals("dataset"));
			assertTrue(rs.getString(3).equals("mCalFile"));
			assertTrue(rs.getString(4).equals("sCalFile"));
			assertTrue(rs.getInt(5) == 12);
			assertTrue(rs.getInt(6) == 20);
			assertTrue(Math.abs(rs.getFloat(7) - (float)0.005) <= 0.00001);
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT * FROM Collections\n" +
					"WHERE CollectionID = " + ids[0]);
			rs.next();
			assertTrue(rs.getString("Name").equals("dataset"));
			assertTrue(rs.getString("Comment").equals("comment"));
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT ParentID FROM CollectionRelationships\n" +
					"WHERE ChildID = " + ids[0]);
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 0);
			assertFalse(rs.next());
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.closeConnection();
	}

	public void testCreateEmptyCollection() {
		db.openConnection();
		int collectionID = db.createEmptyCollection("ATOFMS", 0,"Collection",  "collection","");
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT Name, Comment\n" +
					"FROM Collections\n" +
					"WHERE CollectionID = " + collectionID);
			assertTrue(rs.next());
			assertTrue(rs.getString(1).equals("Collection"));
			assertTrue(rs.getString(2).equals("collection"));
			assertFalse(rs.next());
			
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT ParentID\n" +
					"FROM CollectionRelationships\n" +
					"WHERE ChildID = " + collectionID);
			
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 0);
			assertFalse(rs.next());
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.closeConnection();
	}
	
	/**
	 * @author steinbel
	 * Tests updateInteralAtomOrder() by manually creating sub-collections
	 * then calling the update method.
	 * @throws SQLException 
	 */
	public void testUpdateInternalAtomOrder() throws SQLException{
		String manual = "USE TestDB INSERT INTO Collections VALUES "+
			"(7,'Seven', 'seven', 'sevendescrip', 'ATOFMS')";
		db.openConnection();
		Connection con = db.getCon();
		Statement stmt = con.createStatement();
		stmt.addBatch(manual);
		manual = "USE TestDB DELETE FROM CollectionRelationships WHERE "
			+ "ChildID = 2 OR ChildID = 3";
		stmt.addBatch(manual);
		manual = "USE TestDB INSERT INTO CollectionRelationships VALUES(7,2)"
			+ "INSERT INTO CollectionRelationships VALUES(7,3)";
		stmt.addBatch(manual);
		stmt.executeBatch();
		
		//now update IAO
		db.updateInternalAtomOrder(db.getCollection(7));
		
		//and assert correct insertion
		ResultSet rs = stmt.executeQuery("SELECT * FROM InternalAtomOrder "
				+ "WHERE CollectionID = 7");
		for (int i=1; i<11; i++){
			rs.next();
			assertEquals(rs.getInt(1), i);
		}
		rs.close();
		stmt.close();
		db.closeConnection();
				
	}
	
	/**
	 * @author steinbel
	 * Manually give children to an existing collection, then test.
	 * @throws SQLException 
	 */
	public void testUpdateAncestors() throws SQLException{
		/*
		 * setup collection 7 (atoms 22, 23, 24) and 8 (atoms 25, 26, 27)
		 * as children of 2
		 */
		db.openConnection();
		String sqlstring = "USE TestDB"
			+ " INSERT INTO AtomMembership VALUES (7, 22)"
			+ " INSERT INTO AtomMembership VALUES (7, 23)"
			+ " INSERT INTO AtomMembership VALUES (7, 24)"
			+ " INSERT INTO AtomMembership VALUES (8, 25)"
			+ " INSERT INTO AtomMembership VALUES (8, 26)"
			+ " INSERT INTO AtomMembership VALUES (8, 27)";
		Connection con = db.getCon();
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sqlstring);
		
		sqlstring = "USE TestDB"
			+ " INSERT INTO InternalAtomOrder VALUES (22, 7)"
			+ " INSERT INTO InternalAtomOrder VALUES (23, 7)"
			+ " INSERT INTO InternalAtomOrder VALUES (24, 7)"
			+ " INSERT INTO InternalAtomOrder VALUES (25, 8)"
			+ " INSERT INTO InternalAtomOrder VALUES (26, 8)"
			+ " INSERT INTO InternalAtomOrder VALUES (27, 8)";
		stmt.executeUpdate(sqlstring);
			
		
		sqlstring = "USE TestDB"
			+ " INSERT INTO ATOFMSAtomInfoDense VALUES (22,'9/2/2003 5:30:38 PM',22,0.22,22,'tweTwo')"
			+ " INSERT INTO ATOFMSAtomInfoDense VALUES (23,'9/2/2003 5:30:38 PM',23,0.23,23,'tweThree')"
			+ " INSERT INTO ATOFMSAtomInfoDense VALUES (24,'9/2/2003 5:30:38 PM',24,0.24,24,'tweFour')"
			+ " INSERT INTO ATOFMSAtomInfoDense VALUES (25,'9/2/2003 5:30:38 PM',25,0.25,25,'tweFive')"
			+ " INSERT INTO ATOFMSAtomInfoDense VALUES (26,'9/2/2003 5:30:38 PM',26,0.26,26,'tweSix')"
			+ " INSERT INTO ATOFMSAtomInfoDense VALUES (27,'9/2/2003 5:30:38 PM',27,0.27,27,'tweSeven')";
		stmt.executeUpdate(sqlstring);
		
		sqlstring = "USE TestDB"
			+ " INSERT INTO Collections VALUES (7,'Seven', 'seven', 'sevdescrip', 'ATOFMS')"
			+ " INSERT INTO Collections VALUES (8,'Eight', 'eight', 'eightdescrip', 'ATOFMS')";
		stmt.executeUpdate(sqlstring);
		
		sqlstring = "USE TestDB"
			+ " INSERT INTO CollectionRelationships VALUES (2, 7)"
			+ " INSERT INTO CollectionRelationships VALUES (2, 8)";
		stmt.executeUpdate(sqlstring);
		
		/* Call updateAncestors and check results.*/
		db.updateAncestors(db.getCollection(7));
		
		ResultSet rs = stmt.executeQuery("SELECT * FROM InternalAtomOrder "
				+ "WHERE CollectionID = 2 ORDER BY AtomID");
		
		//test for original particles in collection 2
		for (int i=1; i<6; i++){
			assertTrue(rs.next());
			System.out.println(i + " was i and atomid = " + rs.getInt(1));//TESTING
			assertEquals(rs.getInt(1), i);
		}
			
		//test for particles from children collections 7 and 8
		for (int i=22; i<28; i++){
			assertTrue(rs.next());
			System.out.println(i + " was i and atomid = " + rs.getInt(1));//TESTING
			assertEquals(rs.getInt(1), i);
		}
			
		
		db.closeConnection();
	}

	/**
	 * Copies CollectionID = 3 to CollectionID = 2
	 *
	 */
	public void testCopyCollection() {
		db.openConnection();
		
		int newLocation = db.copyCollection(db.getCollection(3),db.getCollection(2));
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT Name, Comment\n" +
					"FROM Collections\n" +
					"WHERE CollectionID = 3");
			Statement stmt2 = con.createStatement();
			ResultSet rs2 = stmt2.executeQuery(
					"USE TestDB\n" +
					"SELECT Name, Comment\n" +
					"FROM Collections\n" +
					"WHERE CollectionID = " + newLocation);
			assertTrue(rs.next());
			assertTrue(rs2.next());
			assertTrue(rs.getString(1).equals(rs2.getString(1)));
			assertTrue(rs.getString(2).equals(rs2.getString(2)));
			assertFalse(rs.next());
			assertFalse(rs2.next());
			rs.close();
			rs2.close();
			
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT ParentID\n" +
					"FROM CollectionRelationships\n" +
					"WHERE ChildID = " + newLocation);
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 2);
			assertFalse(rs.next());
			rs.close();
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT AtomID\n" +
					"FROM AtomMembership\n" +
					"WHERE CollectionID = 3\n" +
					"ORDER BY AtomID");
			rs2 = stmt2.executeQuery(
					"USE TestDB\n" +
					"SELECT AtomID\n" +
					"FROM AtomMembership\n" +
					"WHERE CollectionID = " + newLocation +
					"ORDER BY AtomID");
			while (rs.next())
			{
				assertTrue(rs2.next());
				assertTrue(rs.getInt(1) == rs2.getInt(1));
			}
			assertFalse(rs2.next());
			rs = stmt.executeQuery("USE TestDB SELECT DISTINCT AtomID FROM AtomMembership WHERE " +
					"CollectionID = "+newLocation+" OR CollectionID = 2 ORDER BY AtomID");
			rs2 = stmt2.executeQuery("USE TestDB SELECT AtomID FROM InternalAtomOrder WHERE " +
					"CollectionID = 2 ORDER BY AtomID");
			while (rs.next())
			{
				assertTrue(rs2.next());
				assertTrue(rs.getInt(1) == rs2.getInt(1));
			}
			assertFalse(rs2.next());	
			rs.close();
			stmt.close();
			rs2.close();
			stmt2.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//test to make sure that a collection can't be copied to itself
		//this should print an exception message.
		System.out.println("Two exceptions about copying a collection into itself" +
				" should follow.");
		assertEquals(-1, db.copyCollection(db.getCollection(0), db.getCollection(0)));
		assertEquals(-1, db.copyCollection(db.getCollection(2), db.getCollection(2)));
		
		db.closeConnection();
	}

	public void testMoveCollection() {
		db.openConnection();
		assertTrue(db.moveCollection(db.getCollection(3),db.getCollection(2)));
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			Statement stmt2 = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT ParentID\n" +
					"FROM CollectionRelationships\n" +
					"WHERE ChildID = 3");
			
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 2);
			assertFalse(rs.next());

			rs = stmt.executeQuery("USE TestDB SELECT AtomID FROM AtomMembership " +
					"WHERE CollectionID = 2 OR CollectionID = 3 ORDER BY AtomID");
			ResultSet rs2 = stmt2.executeQuery("USE TestDB SELECT AtomID FROM InternalAtomOrder" +
					" WHERE CollectionID = 2");
			while (rs.next())
			{
				assertTrue(rs2.next());
				assertTrue(rs.getInt(1) == rs2.getInt(1));
			}
			assertFalse(rs2.next());
			db.closeConnection();
			rs.close();
			stmt.close();
			rs2.close();
			stmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.closeConnection();
	}
	
	public void testInsertATOFMSParticle() {
		db.openConnection();
		final String filename = "'ThisFile'";
		final String dateString = "'1983-01-19 05:05:00.0'";
		final float laserPower = (float)0.01191983;
		final float size = (float)0.5;
		final float digitRate = (float)0.1;
		final int scatterDelay = 10;
		
		ATOFMSParticle.currCalInfo = new CalInfo();
//		ATOFMSParticle.currPeakParams = new PeakParams(12,20,(float)0.005);
		
		
		int posPeakLocation1 = 19;
		int negPeakLocation1 = -20;
		int peak1Height = 80;
		int posPeakLocation2 = 100;
		int negPeakLocation2 = -101;
		int peak2Height = 100;
		
		ArrayList<String> sparseData = new ArrayList<String>();
		sparseData.add(posPeakLocation1 + ", " +  peak1Height + ", 0.1, " + peak1Height);
		sparseData.add(negPeakLocation1 + ", " +  peak1Height + ", 0.1, " + peak1Height);
		sparseData.add(posPeakLocation2 + ", " +  peak2Height + ", 0.1, " + peak2Height);
		sparseData.add(negPeakLocation2 + ", " +  peak2Height + ", 0.1, " + peak2Height);
		

		int collectionID, datasetID;
		collectionID = 2;
		datasetID = db.getNextID();
		int particleID = db.insertParticle(dateString + "," + laserPower + "," + digitRate + ","	
				+ scatterDelay + ", " + filename, sparseData, db.getCollection(collectionID),datasetID,db.getNextID());

		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT PeakLocation, PeakArea, RelPeakArea," +
					" PeakHeight\n" +
					"FROM ATOFMSAtomInfoSparse \n" +
					"WHERE AtomID = " + particleID + "\n" +
					"ORDER BY PeakLocation ASC");
			
			assertTrue(rs.next());
			
			assertTrue(rs.getFloat(1) == (float) negPeakLocation2);
			assertTrue(rs.getInt(2) == peak2Height);
			assertTrue(rs.getFloat(3) == (float) 0.1);
			assertTrue(rs.getInt(4) == peak2Height);
			
			assertTrue(rs.next());
				
			assertTrue(rs.getFloat(1) == (float) negPeakLocation1);
			assertTrue(rs.getInt(2) == peak1Height);
			assertTrue(rs.getFloat(3) == (float) 0.1);
			assertTrue(rs.getInt(4) == peak1Height);
			
			assertTrue(rs.next());

			assertTrue(rs.getFloat(1) == (float) posPeakLocation1);
			assertTrue(rs.getInt(2) == peak1Height);
			assertTrue(rs.getFloat(3) == (float) 0.1);
			assertTrue(rs.getInt(4) == peak1Height);
			
			assertTrue(rs.next());
			
			assertTrue(rs.getFloat(1) == (float) posPeakLocation2);
			assertTrue(rs.getInt(2) == peak2Height);
			assertTrue(rs.getFloat(3) == (float) 0.1);
			assertTrue(rs.getInt(4) == peak2Height);
			
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT [Time], LaserPower, [Size], ScatDelay, " +
					"OrigFilename\n" +
					"FROM ATOFMSAtomInfoDense \n" +
					"WHERE AtomID = " + particleID);
			rs.next();
			assertTrue(rs.getString(1).equals(dateString.substring(1,dateString.length()-1)));
			assertTrue(rs.getFloat(2) == laserPower);
			assertTrue(rs.getFloat(3) == digitRate); // size
			assertTrue(rs.getInt(4) == scatterDelay);
			assertTrue(rs.getString(5).equals(filename.substring(1,filename.length()-1)));
			
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT CollectionID\n" +
					"FROM AtomMembership\n" +
					"WHERE AtomID = " + particleID);
			rs.next();
			
			assertTrue(rs.getInt(1) == collectionID);
			
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT OrigDataSetID\n" +
					"FROM DataSetMembers \n" +
					"WHERE AtomID = " + particleID);
			
			rs.next();
			assertTrue(rs.getInt(1) == datasetID);		
			db.closeConnection();
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void testGetNextId(){
		db.openConnection();
		
		assertTrue(db.getNextID() >= 0);
	
		db.closeConnection();
	}
	
	public void testOrphanAndAdopt(){
		
		db.openConnection();
		//Insert 5,21 into the database to tell if an error occurs when an item
		//is present in a parent and its child
		try {
			Connection con1 = db.getCon();
			Statement stmt1 = con1.createStatement();
			String query = "USE TestDB\n" +
				"INSERT INTO AtomMembership VALUES(5,21)\n";
			System.out.println(query);
			stmt1.executeUpdate(query);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		assertTrue(db.orphanAndAdopt(db.getCollection(6)));
		//make sure that the atoms collected before are in collection 4
		ArrayList<Integer> collection5Info = new ArrayList<Integer>();
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			Statement stmt2 = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("USE TestDB SELECT * FROM InternalAtomOrder WHERE" +
			" CollectionID = 6");
			assertFalse(rs.next());
	
			rs = stmt.executeQuery("USE TestDB\n" +
			"SELECT AtomID\n" +
			"FROM AtomMembership\n" +
			"WHERE CollectionID = 5 ORDER BY AtomID");

			ResultSet rs2 = stmt2.executeQuery("USE TestDB SELECT AtomID" +
					" FROM InternalAtomOrder WHERE CollectionID = 5 ORDER BY AtomID");
			
			int count = 0;
			while (rs.next()) {
				assertTrue(rs2.next());
				assertTrue(rs.getInt(1) == rs2.getInt(1));
				count++;
			}
			assertFalse(rs2.next());
			assertTrue(count == 6);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Error message to follow:");
		// removed an assert false here - changed the code to give an error 
		// if a collectionID is passed that isn't really a collection in the db.
		assertFalse(db.orphanAndAdopt(db.getCollection(2))); //is not a subcollection (prints this)
		
		//Make sure orphan and adopt can't be performed on either root
		//these should print error messages
		assertFalse(db.orphanAndAdopt(db.getCollection(0)));
		assertFalse(db.orphanAndAdopt(db.getCollection(1)));
		
		db.closeConnection();
	}

	public void testRecursiveDelete(){
		db.openConnection();
		
		ArrayList<Integer> atomIDs = new ArrayList<Integer>();
		ResultSet rs;
		Statement stmt;
		try {
			Connection con = db.getCon();
			stmt = con.createStatement();
			StringBuilder sql = new StringBuilder();
			sql.append("USE TestDB;\n ");
			
			//Store a copy of all the relevant tables with just the things that should be left after deletion
			// AKA Figure out what the database should look like after deletion
			
			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp0')\n"+
			"DROP TABLE #temp0;\n");
			sql.append("CREATE TABLE #temp0 (AtomID INT);\n");
			
			//collect a list of AtomID that should be deleted
			sql.append("insert #temp0 (AtomID) \n" +
							"SELECT AtomID\n"
					+" FROM AtomMembership\n"
					+" WHERE CollectionID = 5 OR CollectionID = 6;\n");
			
			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp1')\n"
					+ "BEGIN\n"
					+ "DROP TABLE #temp1\n"
					+ "END;\n");
			sql.append("CREATE TABLE #temp1 (AtomID INT);\n");
			//ATOFMSAtomInfoDense
			sql.append("insert #temp1 (AtomID) \n" +
					"SELECT AtomID FROM ATOFMSAtomInfoDense\n"
					+ " WHERE NOT AtomID IN\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp0)\n;\n");

			
			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp2')\n"+
			"DROP TABLE #temp2;\n");
			sql.append("CREATE TABLE #temp2 (AtomID INT, CollectionID INT);\n");
			//AtomMembership
			sql.append("insert #temp2 (AtomID, CollectionID) \n" +
					"SELECT AtomID, CollectionID FROM AtomMembership\n"
					+ " WHERE NOT AtomID IN\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp0)\n;\n");

			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp3')\n"+
			"DROP TABLE #temp3;\n");
			sql.append("CREATE TABLE #temp3 (AtomID INT, PeakLocation INT);\n");
			//ATOFMSAtomInfoSparse
			sql.append("insert #temp3 (AtomID, PeakLocation) \n" +
					"SELECT AtomID, PeakLocation FROM ATOFMSAtomInfoSparse\n"
					+ " WHERE NOT AtomID IN\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp0)\n;\n");

			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp4')\n"+
			"DROP TABLE #temp4;\n");
			sql.append("CREATE TABLE #temp4 (AtomID INT, CollectionID INT);\n");
			//InternalAtomOrder
			sql.append("insert #temp4 (AtomID, CollectionID) \n" +
					"SELECT AtomID, CollectionID FROM InternalAtomOrder\n"
					+ " WHERE NOT AtomID IN\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp0)\n;\n");

			
			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp5')\n"+
			"DROP TABLE #temp5;\n");
			sql.append("CREATE TABLE #temp5 (ParentID INT, ChildID INT);\n");
			//CollectionRelationships
			sql.append("insert #temp5 (ParentID, ChildID) \n" +
					"SELECT ParentID, ChildID FROM CollectionRelationships\n"
					+ " WHERE NOT (ChildID = 5"
					+ " OR ParentID = 5);\n");

			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#temp6')\n"+
			"DROP TABLE #temp6;\n");
			sql.append("CREATE TABLE #temp6 (CollectionID INT);\n");
			//Collections
			sql.append("insert #temp6 (CollectionID) \n" +
					"SELECT CollectionID FROM Collections\n"
					+ " WHERE NOT (CollectionID = 5 OR CollectionID = 6);\n");

			stmt.execute(sql.toString());
			
			assertTrue(db.recursiveDelete(db.getCollection(5)));
				
			
			//Check to make sure that the database is as it should be
			//Both that information that should be gone is gone 
			// and that no information was deleted that shouldn't have been

			//InternalAtomOrder
			rs = stmt.executeQuery(
					"SELECT AtomID, CollectionID FROM InternalAtomOrder X\n"
					+ " WHERE NOT EXISTS\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp4 Y\n"
					+ "		WHERE X.CollectionID = Y.CollectionID AND X.AtomID = Y.AtomID);\n");
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"SELECT * FROM #temp4 X\n"
					+ " WHERE NOT EXISTS\n"
					+ "		(SELECT AtomID, CollectionID FROM InternalAtomOrder Y\n"
					+ "		WHERE X.CollectionID = Y.CollectionID AND X.AtomID = Y.AtomID);\n");
			assertFalse(rs.next());
			
			//CollectionRelationships
			rs = stmt.executeQuery(
					"SELECT ParentID, ChildID FROM CollectionRelationships X\n"
					+ " WHERE NOT EXISTS (SELECT * FROM #temp5 Y\n"
					+ "						WHERE X.ParentID = Y.ParentID AND X.ChildID = Y.ChildID);\n");
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"SELECT * FROM #temp5 X\n"
					+ " WHERE NOT EXISTS\n"
					+ "		(SELECT ParentID, ChildID FROM CollectionRelationships Y\n"
					+ "						WHERE X.ParentID = Y.ParentID AND X.ChildID = Y.ChildID);\n");
			assertFalse(rs.next());
			
			//Collections
			rs = stmt.executeQuery(
					"SELECT * FROM Collections\n"
					+ " WHERE NOT CollectionID IN\n"
					+ " 	(SELECT CollectionID\n"
					+ " 	FROM #temp6)\n;\n");
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"SELECT * FROM #temp6 X\n"
					+ " WHERE NOT X.CollectionID IN\n"
					+ "		(SELECT CollectionID FROM Collections);\n");
			assertFalse(rs.next());
			
			final ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(null, "Compacting Database",100);
			progressBar.constructThis();
			progressBar.setIndeterminate(true);
			progressBar.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			progressBar.setVisible(false);
			
			assertTrue(db.compactDatabase(progressBar));
			

			//ATOFMSAtomInfoDense
			rs = stmt.executeQuery(
					"SELECT AtomID FROM ATOFMSAtomInfoDense\n"
					+ " WHERE NOT AtomID IN\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp1)\n;\n");
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"SELECT * FROM #temp1\n"
					+ " WHERE NOT AtomID IN\n"
					+ "(SELECT AtomID FROM ATOFMSAtomInfoDense);\n");
			assertFalse(rs.next());
			
			//AtomMembership
			rs = stmt.executeQuery(
					"SELECT AtomID, CollectionID FROM AtomMembership X\n"
					+ " WHERE NOT EXISTS\n"
					+ " 	(SELECT *\n FROM #temp2 Y\n"
					+ "		WHERE X.CollectionID = Y.CollectionID AND X.AtomID = Y.AtomID);\n");
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"SELECT * FROM #temp2 X\n"
					+ " WHERE NOT EXISTS\n"
					+ "		(SELECT AtomID, CollectionID FROM AtomMembership Y\n"
					+ "		WHERE X.CollectionID = Y.CollectionID AND X.AtomID = Y.AtomID);\n");
			assertFalse(rs.next());
			
			//ATOFMSAtomInfoSparse
			rs = stmt.executeQuery(
					"SELECT AtomID, PeakLocation FROM ATOFMSAtomInfoSparse X\n"
					+ " WHERE NOT EXISTS\n"
					+ " 	(SELECT *\n"
					+ " 	FROM #temp3 Y\n"
					+ "		WHERE X.PeakLocation = Y.PeakLocation AND X.AtomID = Y.AtomID);\n");
			assertFalse(rs.next());
			rs = stmt.executeQuery(
					"SELECT * FROM #temp3 X\n"
					+ " WHERE NOT EXISTS\n"
					+ "		(SELECT * FROM ATOFMSAtomInfoSparse Y\n"
					+ "		WHERE X.PeakLocation = Y.PeakLocation AND X.AtomID = Y.AtomID);\n");
			assertFalse(rs.next());
			
			
			stmt.execute("DROP TABLE #temp0;\n");
			stmt.execute("DROP TABLE #temp1;\n");
			stmt.execute("DROP TABLE #temp2;\n");
			stmt.execute("DROP TABLE #temp3;\n");
			stmt.execute("DROP TABLE #temp4;\n");
			stmt.execute("DROP TABLE #temp5;\n");
			stmt.execute("DROP TABLE #temp6;\n");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.closeConnection();

	}
	
	public void testGetCollectionName(){
		db.openConnection();
		
		assertTrue( "One".equals(db.getCollectionName(2)) );
		assertTrue( "Two".equals(db.getCollectionName(3)) );
		assertTrue( "Three".equals(db.getCollectionName(4)) );
		assertTrue( "Four".equals(db.getCollectionName(5)) );
		
		db.closeConnection();
	}
	
	public void testGetCollectionComment(){
		db.openConnection();
		
		assertTrue( "one".equals(db.getCollectionComment(2)) );
		assertTrue( "two".equals(db.getCollectionComment(3)) );
		assertTrue( "three".equals(db.getCollectionComment(4)) );
		assertTrue( "four".equals(db.getCollectionComment(5)) );
		
		db.closeConnection();
	}
	
	public void testGetCollectionSize(){
		db.openConnection();
		
		final String filename = "'FirstFile'";
		final String dateString = "'1983-01-19 05:05:00.0'";
		final float laserPower = (float)0.01191983;
		final float size = (float)5;
		final float digitRate = (float)0.1;
		final int scatterDelay = 10;
		ArrayList<String> sparseData = new ArrayList<String>();
		int collectionID = 2;
		int datasetID = 1;
		System.out.println(db.getCollectionSize(collectionID));
		assertTrue(db.getCollectionSize(collectionID) == 5);
		
		db.insertParticle(dateString + "," + laserPower + "," + digitRate + ","	
				+ scatterDelay + ", " + filename, sparseData, db.getCollection(collectionID),datasetID,db.getNextID()+1);
		System.out.println(db.getCollectionSize(collectionID));
		assertTrue(db.getCollectionSize(collectionID) == 6);
			
		db.closeConnection();
	}

	public void testGetAllDescendedAtoms(){
		db.openConnection();
		
//		case of one child collection
		int[] expected = {16,17,18,19,20,21};
		ArrayList<Integer> actual = db.getAllDescendedAtoms(db.getCollection(5));
		
		for (int i=0; i<actual.size(); i++)
			assertTrue(actual.get(i) == expected[i]);
		
//		case of no child collections
		int[] expected2 = {1,2,3,4,5};
		actual = db.getAllDescendedAtoms(db.getCollection(2));
		
		for (int i=0; i<actual.size(); i++) 
			assertTrue(actual.get(i) == expected2[i]);
		
		db.closeConnection();
	}

	/** Depreciated 12/05 - AR
	public void testGetCollectionParticles(){
		db.openConnection();
		
		//we know the particle info from inserting it	
		ArrayList<GeneralAtomFromDB> general = db.getCollectionParticles(db.getCollection(2));
		
		assertEquals(general.size(), 5);
		ATOFMSAtomFromDB actual = general.get(0).toATOFMSAtom();
		assertEquals(actual.getAtomID(), 1);
//		assertEquals(actual.getLaserPower(), 1);  not retrieved in method
		assertEquals(actual.getSize(), (float)0.1);
//		assertEquals(actualgetScatDelay(), 1);	not retrieved in method
		assertEquals(actual.getFilename(), "One");
			
		db.closeConnection();
	}
	*/
	
	public void testRebuildDatabase() {

		try {
			db.closeConnection();
			assertTrue(Database.rebuildDatabase("TestDB"));			
			db.openConnection();
			
			InfoWarehouse mainDB = Database.getDatabase();
			mainDB.openConnection();
			assertTrue(mainDB.isPresent());
			mainDB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}


	
	public void testIsPresent() {
		db.openConnection();
		InfoWarehouse db = Database.getDatabase("TestDB");
		assertTrue(db.isPresent());
		db = Database.getDatabase("shouldntexist");
		assertFalse(db.isPresent());
		
		//uses default database
		db = Database.getDatabase();
		assertTrue(db.isPresent());
		db.closeConnection();
	}
	

	public void testExportToMSAnalyzeDatabase() {
		db.openConnection();
		java.util.Date date = db.exportToMSAnalyzeDatabase(db.getCollection(2),"MSAnalyzeDB","MS-Analyze");
		assertTrue(date.toString().equals("Tue Sep 02 17:30:38 CDT 2003"));
		db.closeConnection();
	
	}
	
	
	public void testMoveAtom() {
		db.openConnection();
		assertTrue(db.moveAtom(1,1,2));
		assertTrue(db.moveAtom(1,2,1));
		db.closeConnection();
	}
	
	/*public void testMoveAtomBatch() {
		db.openConnection();
		db.atomBatchInit();
		assertTrue(db.moveAtomBatch(1,1,2));
		assertTrue(db.moveAtomBatch(1,2,1));
		db.atomBatchExecute();
		db.closeConnection();
	}*/
	
	/**
	 * Tests AddAtom and DeleteAtomBatch
	 *
	 */
	public void testAddAndDeleteAtom() {
		db.openConnection();
		db.atomBatchInit();
		assertTrue(db.deleteAtomBatch(1,db.getCollection(1)));
		db.atomBatchExecute();
		assertTrue(db.addAtom(1,1));
		db.closeConnection();		
	}
	
	/**
	 * Tests AddAtomBatch and DeleteAtomsBatch
	 *
	 */
	public void testAddAndDeleteAtomBatch() {
		db.openConnection();
		db.atomBatchInit();
		assertTrue(db.deleteAtomsBatch("1",db.getCollection(1)));
		assertTrue(db.addAtomBatch(1,1));
		db.atomBatchExecute();
		db.closeConnection();
	}
	
	public void testCheckAtomParent() {
		db.openConnection();
		assertTrue(db.checkAtomParent(1,2));
		assertFalse(db.checkAtomParent(1,4));
		db.closeConnection();
	}
	
	public void testGetAndSetCollectionDescription() {
		db.openConnection();
		String description = db.getCollectionDescription(2);
		assertTrue(db.setCollectionDescription(db.getCollection(2),"new description"));		
		assertTrue(db.getCollectionDescription(2).equals("new description"));
		db.setCollectionDescription(db.getCollection(2),description);
		db.closeConnection();
	}
	
	/* Can't try dropping db because it's in use.
	public void testDropDatabase() {
		db.openConnection();
		assertTrue(Database.dropDatabase("TestDB"));
		setUp();
	} */
	
	public void testGetPeaks() {
		db.openConnection();
		Peak peak = db.getPeaks("ATOFMS",2).get(0);
		assertTrue(((ATOFMSPeak)peak).area == 15);
		assertTrue(((ATOFMSPeak)peak).relArea == 0.006f);
		assertTrue(((ATOFMSPeak)peak).height == 12);
		db.closeConnection();
	}

/*
	public void testInsertGeneralParticles() {
		db.openConnection();
		 int[] pSpect = {1,2,3};
		 int[] nSpect = {1,2,3};
		EnchiladaDataPoint part = new EnchiladaDataPoint("newpart");
		ArrayList<EnchiladaDataPoint> array = new ArrayList<EnchiladaDataPoint>();
		array.add(part);
		int id = db.insertGeneralParticles(array,1);
		assertTrue(db.checkAtomParent(id,1));
		db.atomBatchInit();
		db.deleteAtomBatch(id,1);
		db.executeBatch();
		db.closeConnection();
		}
*/
	public void testGetAtomDatatype() {
		db.openConnection();
		assertTrue(db.getAtomDatatype(2).equals("ATOFMS"));
		assertTrue(db.getAtomDatatype(18).equals("Datatype2"));
		db.closeConnection();
	}
	
	// TODO: ERROR WITH SEED RANDOM!! DON'T KNOW WHY - AR
	public void testSeedRandom()
	{
	    db.openConnection();
	    //db.getNumber();
	    db.seedRandom(12345);
	    double rand1 = db.getNumber();
	    db.getNumber();
	    db.getNumber();
	    
	    System.out.println();
	    db.seedRandom(12345);
	    double rand2 = db.getNumber();
	    db.getNumber();
	    db.getNumber();
	    
	    System.out.println();
	    db.seedRandom(12345);
	    db.getNumber();
	    db.getNumber();
	    db.getNumber();
	    
	    assertTrue(rand1==rand2);
	    db.closeConnection();
	    
	}
	public void testGetParticleInfoOnlyCursor() {
		db.openConnection();
		CollectionCursor curs = db.getAtomInfoOnlyCursor(db.getCollection(2));
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetSQLCursor() {
		db.openConnection();
		CollectionCursor curs = db.getSQLCursor(db.getCollection(2), "ATOFMSAtomInfoDense.AtomID != 20");
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetPeakCursor() {
		db.openConnection();
		CollectionCursor curs = db.getPeakCursor(db.getCollection(2));
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetBinnedCursor() {
		db.openConnection();
		CollectionCursor curs = db.getBinnedCursor(db.getCollection(2));
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetMemoryBinnedCursor() {
		db.openConnection();
		CollectionCursor curs = db.getMemoryBinnedCursor(db.getCollection(2));	
		testCursor(curs);	
		db.closeConnection();
	}
	
	public void testGetRandomizedCursor() {
		db.openConnection();
		CollectionCursor curs = db.getRandomizedCursor(db.getCollection(2));	
		testCursor(curs);	
		db.closeConnection();
	}
	
	private void testCursor(CollectionCursor curs)
	{
		ArrayList<ParticleInfo> partInfo = new ArrayList<ParticleInfo>();
		
		ParticleInfo temp = new ParticleInfo();
		ATOFMSAtomFromDB tempPI = 
			new ATOFMSAtomFromDB(
					1,"One",1,0.1f,
					new Date("9/2/2003 5:30:38 PM"));
		//int aID, String fname, int sDelay, float lPower, Date tStamp
		temp.setParticleInfo(tempPI);
		temp.setID(1);

		partInfo.add(temp);
		
		int[] ids = new int[5];
		for (int i = 0; i < 5; i++)
		{
			assertTrue(curs.next());
			assertTrue(curs.getCurrent()!= null);
			ids[i] = curs.getCurrent().getID();
		}
		assertFalse(curs.next());	
		curs.reset();
		
		for (int i = 0; i < 5; i++)
		{
			assertTrue(curs.next());
			assertTrue(curs.getCurrent() != null);
			assertTrue(curs.getCurrent().getID() == ids[i]);
		}
		
		assertFalse(curs.next());
		curs.reset();
	}

	/**
	 * @author shaferia
	 */
	public void testJoin()
	{
		int[] intsraw = {1, 2, 3, 4, 5};
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for (int i : intsraw)
			ints.add(i);
		ArrayList<String> strings = new ArrayList<String>();
		for (int i : intsraw)
			strings.add(i + "");
		
		ArrayList<Object> mixed = new ArrayList<Object>();
		mixed.add(new Integer(2));
		mixed.add("hey");
		mixed.add(new Float(2.0));
		
		assertEquals(SQLServerDatabase.join(ints, ","), "1,2,3,4,5");
		assertEquals(SQLServerDatabase.join(ints, ""), "12345");
		assertEquals(SQLServerDatabase.join(new ArrayList<String>(), ","), "");
		assertEquals(SQLServerDatabase.join(ints, "-"), SQLServerDatabase.join(strings, "-"));
		assertEquals(SQLServerDatabase.join(strings, ",,"), "1,,2,,3,,4,,5");
		assertEquals(SQLServerDatabase.join(mixed, "."), "2.hey.2.0");
		
		ArrayList<Integer> oneint = new ArrayList<Integer>();
		oneint.add(new Integer(2));
		
		assertEquals(SQLServerDatabase.join(oneint, ","), "2");
	}

	/**
	 * Use java.util.Foramtter to create a formatted string
	 * @param format the format specification
	 * @param args variables to format
	 * @return the formatted string
	 * @author shaferia
	 */
	private String sprintf(String format, Object... args) {
		return (new java.util.Formatter().format(format, args)).toString();
	}
	
	/**
	 * Print a justified text table of a database table
	 * @param name the database table to output
	 * @author shaferia
	 */
	public static void printDBSection(InfoWarehouse database, String name) {
		printDBSection(database, name, Integer.MAX_VALUE);
	}
	
	/**
	 * Print a justified text table of a database table. Requires an open connection to db.
	 * @param name the database table to output
	 * @param rows a single argument giving the maximum number of rows to output
	 * @author shaferia
	 */
	public static void printDBSection(InfoWarehouse database, String name, int rows) {
		Statement stmt = null;
		ResultSet rs = null;
		
		Connection con = database.getCon();
		try	{
			if (con == null) {
				database.openConnection();
				con = database.getCon();
			}
			
			stmt = con.createStatement();
			rs = stmt.executeQuery(
					"USE TestDB;\n" +
					"SELECT * FROM " + name);
			java.sql.ResultSetMetaData rsmd = rs.getMetaData();
			
			ArrayList<String[]> data = new ArrayList<String[]>();
			int colcount = rsmd.getColumnCount();
			int[] cwidth = new int[colcount];
			
			data.add(new String[colcount]);
			for (int i = 0; i < colcount; ++i) {
				data.get(0)[i] = rsmd.getColumnName(i + 1);
				cwidth[i] = data.get(0)[i].length();
			}
			
			for (int i = 1; rs.next() && (i < rows); ++i){
				data.add(new String[colcount]);
				for (int j = 0; j < colcount; ++j) {
					data.get(i)[j] = rs.getObject(j + 1).toString();
					cwidth[j] = Math.max(cwidth[j], data.get(i)[j].length());
				}
			}

			for (int i = 0; i < data.size(); ++i) {
				for (int j = 0; j < data.get(i).length; ++j) {
					System.out.printf(
									"%-" + cwidth[j] + "." + cwidth[j] + "s | ", data.get(i)[j]);
				}
				System.out.println();
			}

			stmt.close();
			rs.close();
		}
		catch (SQLException ex) {
			System.err.println("Couldn't print database section.");
			ex.printStackTrace();
		}
		finally {
			database.closeConnection();
		}
	}

	/**
	 * @author shaferia
	 */
	public void testAddCenterAtom() {	
		db.openConnection();
		
		assertTrue(db.addCenterAtom(2, 3));
		
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM CenterAtoms WHERE AtomID = 2");
			
			assertTrue(rs.next());
			assertEquals(rs.getInt("AtomID"), 2);
			assertEquals(rs.getInt("CollectionID"), 3);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		assertTrue(db.addCenterAtom(3, 4));
		
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM CenterAtoms ORDER BY CollectionID");
			
			assertTrue(rs.next());
			assertEquals(rs.getInt("AtomID"), 2);
			assertEquals(rs.getInt("CollectionID"), 3);
			
			assertTrue(rs.next());
			assertEquals(rs.getInt("AtomID"), 3);
			assertEquals(rs.getInt("CollectionID"), 4);
			
			assertFalse(rs.next());
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Three primary key constraint errors to follow.");
		//this should print an exception message
		assertFalse(db.addCenterAtom(3, 4));;
		
		//this should print an exception message
		assertFalse(db.addCenterAtom(2, 5));
		
		//this should print an exception message
		assertFalse(db.addCenterAtom(5, 4));
		
		db.closeConnection();
	}

	/**
	 * @author steinbel - got rid of IAO.OrderNumber
	 * @author shaferia
	 * @throws SQLException 
	 */
	public void testAddSingleInternalAtomToTable() throws SQLException {
		db.openConnection();
		
		//test adding to end
		db.addSingleInternalAtomToTable(6, 2);

	
		Connection con = db.getCon();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"SELECT * FROM InternalAtomOrder WHERE AtomID > 4 " +
				"AND AtomID <8 ORDER BY CollectionID, AtomID");
		
		//since ordernumber isn't relevant anymore, instead checking to make
		//sure that each atomID = 6 got into the correct collection
		rs.next();
		assertEquals(rs.getInt(2), 2);
		assertEquals(rs.getInt(1), 5);
		rs.next();
		assertEquals(rs.getInt(2), 2);
		assertEquals(rs.getInt(1), 6);
		rs.next();
		assertEquals(rs.getInt(2), 3);
		assertEquals(rs.getInt(1), 6);
		rs.next();
		assertEquals(rs.getInt(2), 3);
		assertEquals(rs.getInt(1), 7);
	
	
		
		
		db.atomBatchInit();
		db.deleteAtomsBatch("'1','2','3','4','5'", db.getCollection(2));
		db.atomBatchExecute();
		db.updateInternalAtomOrder(db.getCollection(2));

		//addSingleInternalAtomToTable should order things correctly - make sure that happens
		db.addSingleInternalAtomToTable(4, 2);
		db.addSingleInternalAtomToTable(3, 2);
		db.addSingleInternalAtomToTable(1, 2);
		db.addSingleInternalAtomToTable(5, 3);
		db.addSingleInternalAtomToTable(2, 2);

		try {
			con = db.getCon();
			stmt = con.createStatement();
			rs = stmt.executeQuery(
					"SELECT * FROM InternalAtomOrder WHERE CollectionID = 2");
			
			for (int i = 0; rs.next(); ++i) {
				assertEquals(rs.getInt(1), i + 1);
			}
			assertFalse(rs.next());
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		db.closeConnection();	
	}
	
	/**
	 * @author shaferia
	 */
	public void testAggregateColumn() {
		db.openConnection();
		
		int[] intsraw = {1,2,3,4,5};
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for (int i : intsraw)
			ints.add(i);
		
		assertEquals(db.aggregateColumn(
				DynamicTable.AtomInfoDense, 
				"AtomID", 
				ints, 
				"ATOFMS"), "15.0");
		
		assertEquals(db.aggregateColumn(
				DynamicTable.AtomInfoDense, 
				"Size", 
				ints, 
				"ATOFMS").substring(0, 7), "1.50000");
		
		ints.remove(0);
		assertEquals(db.aggregateColumn(
				DynamicTable.AtomInfoSparse, 
				"PeakHeight", 
				ints, 
				"ATOFMS"), 12*(2+3+4+5) + ".0");	
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testCreateIndex() {
		db.openConnection();
		
		assertTrue(db.createIndex("ATOFMS", "Size, LaserPower"));
		assertTrue(db.createIndex("ATOFMS", "Size, Time"));
		System.out.println("Two exceptions to follow - there are already indices.");
		assertFalse(db.createIndex("ATOFMS", "Size, LaserPower"));
		assertFalse(db.createIndex("ATOFMS", "size, time"));
		
		try {
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM ATOFMSAtomInfoDense WHERE Size = 0.2");
			
			assertTrue(rs.next());
			assertTrue(rs.getFloat("LaserPower") < 2.0001 && rs.getFloat("LaserPower") > 1.9999);
			assertFalse(rs.next());
			
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testExportImportDatabase() {
		db.openConnection();
		
		System.out.printf("Current working directory is %s\n", System.getProperty("user.dir"));
		
		try {
			//db.exportDatabase("test1.out", 1);
			//java.io.File f = new java.io.File("test1.out");
			//assertTrue(f.exists());
			
			//TODO: Determine status of [(export)(import)]Database
			/*
			tearDown();
			db = Database.getDatabase();
	        try {
				Database.rebuildDatabase("TestDB");
			} catch (SQLException e2) {
				e2.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Could not rebuild the database." +
						"  Close any other programs that may be accessing the database and try again.");
			}
			db.openConnection();
			db.importDatabase("test1.out", 1);
			db.exportDatabase("test2.out", 1);
			*/
			
			//f.delete();
		}
		catch (Exception ex) {
			System.out.println("Exception handling file.");
			ex.printStackTrace();
			fail();
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetAdjacentAtomInCollection() {
		db.openConnection();
		
		int[] adj = db.getAdjacentAtomInCollection(2, 3, 1);
		assertEquals(adj[0], 4);
		assertEquals(adj[1], 4);
		
		adj = db.getAdjacentAtomInCollection(3, 7, -1);
		assertEquals(adj[0], 6);
		assertEquals(adj[1], 1);
		
		adj = db.getAdjacentAtomInCollection(4, 12, 2);
		assertEquals(adj[0], 13);
		assertEquals(adj[1], 3);
		
		//The following two assertions should print SQLExceptions.
		
		adj = db.getAdjacentAtomInCollection(2, 1, -1);
		assertTrue((adj[0] < 0) && (adj[1] < 0));
		
		adj = db.getAdjacentAtomInCollection(2, 5, 1);
		assertTrue((adj[0] < 0) && (adj[1] < 0));
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetATOFMSFileName() {
		db.openConnection();
		
		assertEquals(db.getATOFMSFileName(1), "One");
		assertEquals(db.getATOFMSFileName(11), "Eleven");
		
		//for non-ATOFMS data - these assertions should print SQLExceptions.
		System.out.println("Three exceptions about being unable to find a filename follow.");
		assertEquals(db.getATOFMSFileName(12), "");
		assertEquals(db.getATOFMSFileName(15), "");
		assertEquals(db.getATOFMSFileName(22), "");
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetCollectionDatatype() {
		db.openConnection();
		
		String[] expectedDatatypes = {"ATOFMS", "ATOFMS", "Datatype2", "Datatype2", "Datatype2"};
		for (int i = 0; i < expectedDatatypes.length; ++i)
			assertEquals(db.getCollectionDatatype(i + 2), expectedDatatypes[i]);
		
		//Should print an SQLException
		System.out.println("Exception getting datatype follows.");
		assertEquals(db.getCollectionDatatype(8), "");
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetCollectionIDsWithAtoms() {
		db.openConnection();

		ArrayList<Integer> colls = new ArrayList<Integer>();
		for (int i = 0; i < 20; ++i)
			colls.add(i);
		
		ArrayList<Integer> ids = db.getCollectionIDsWithAtoms(colls);
		
		assertTrue(ids.size() == 5);
		assertEquals((int) ids.get(0), 2);
		assertEquals((int) ids.get(1), 3);
		assertEquals((int) ids.get(2), 4);
		assertEquals((int) ids.get(3), 5);
		assertEquals((int) ids.get(4), 6);
		
		colls = new ArrayList<Integer>();
		assertTrue(db.getCollectionIDsWithAtoms(colls).size() == 0);
		
		colls.add(-1);
		assertTrue(db.getCollectionIDsWithAtoms(colls).size() == 0);
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetColNames() {
		db.openConnection();
		
		//Test Metadata given by database (from ResultSetMetaData) 
		//	against hardcoded MetaData in database
		for (String type : db.getKnownDatatypes()) {
			for (DynamicTable dynt : DynamicTable.values()) {
				ArrayList<String> names = db.getColNames(type, dynt);
				
				try	{
					Connection con = db.getCon();
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(
							"USE TestDB;\n" +
							"SELECT * FROM " + db.getDynamicTableName(dynt, type));
					java.sql.ResultSetMetaData rsmd = rs.getMetaData();
					
					for (int i = 1; i < rsmd.getColumnCount() + 1; ++i) {
						assertTrue(names.get(i - 1).equalsIgnoreCase(
								"[" + rsmd.getColumnName(i) + "]"));
					}
				}
				catch (SQLException ex) {
					//this isn't an error.
					System.out.printf("Couldn't test database MetaData for Table %s %s" +
							" - table doesn't exist.\n", type, dynt);
				}
			}
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetColNamesAndTypes() {
		db.openConnection();

		//ugh... java.sql.Type isn't an enum.
		//Manually map the int types in the fake enum to String type values
		java.util.HashMap<Integer, String> typeConv = new java.util.HashMap<Integer, String>();
		typeConv.put(-7, "BIT");
		typeConv.put(4, "INT");
		typeConv.put(7, "REAL");
		typeConv.put(12, "VARCHAR(8000)");
		typeConv.put(93, "DATETIME");
		
		//Test Metadata given by database (from ResultSetMetaData) 
		//	against hardcoded MetaData in database, as in testGetColNames
		for (String type : db.getKnownDatatypes()) {
			for (DynamicTable dynt : DynamicTable.values()) {
				ArrayList<ArrayList<String>> names = db.getColNamesAndTypes(type, dynt);
				
				//System.out.printf("*** %s%s *** \n", type, dynt);
				
				try	{
					Connection con = db.getCon();
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(
							"USE TestDB;\n" +
							"SELECT * FROM " + db.getDynamicTableName(dynt, type));
					java.sql.ResultSetMetaData rsmd = rs.getMetaData();

					for (int i = 1; i < rsmd.getColumnCount() + 1; ++i) {
						//System.out.printf("Name: %-20.20s - %-20.20s Type: %-20.20s - %-20.20s\n",
						//		names.get(i - 1).get(0), rsmd.getColumnName(i),
						//		names.get(i - 1).get(1), rsmd.getColumnTypeName(i));
						assertTrue(names.get(i - 1).get(0).equalsIgnoreCase(
								rsmd.getColumnName(i)));
						assertTrue(names.get(i - 1).get(1).equals(
								typeConv.get(rsmd.getColumnType(i))));
					}
				}
				catch (SQLException ex) {
					//this isn't an error.
					System.out.printf("Couldn't test database MetaData for Table %s %s" +
							" - table doesn't exist.\n", type, dynt);
				}
			}
		}
		
		db.closeConnection();		
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetDatabaseVersion() {
		db.openConnection();
		
		try {
			//an empty database has version "Sept2006.1" currently
			assertEquals(db.getVersion(), "Sept2006.1");
		}
		catch (Exception ex) {
			fail();
			ex.printStackTrace();
		}
		
		try {
			db.getCon().createStatement().executeUpdate(
					"UPDATE DBInfo SET Value = 'New!' WHERE Name = 'Version'");
			
			assertEquals(db.getVersion(), "New!");
		}
		catch (Exception ex) {
			fail();
			ex.printStackTrace();
		}
		
		try {
			db.getCon().createStatement().executeUpdate(
					"DELETE FROM DBInfo WHERE Name = 'Version'");
		}
		catch (SQLException ex) {
			System.err.println("Error deleting version while testing");
			ex.printStackTrace();
		}
		
		try {
			db.getVersion();
			
			//shouldn't get this far.
			fail();
		}
		catch (Exception ex) {
			System.out.println("This should be an error: \"" + ex.getMessage() + "\"");
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetFirstAtomInCollection() {
		db.openConnection();
		
		assertEquals(db.getFirstAtomInCollection(db.getCollection(2)), 1);
		assertEquals(db.getFirstAtomInCollection(db.getCollection(3)), 6);
		
		try {
			db.getCon().createStatement().executeUpdate(
					"DELETE FROM AtomMembership WHERE CollectionID = 2");
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}	
		
		assertEquals(db.getFirstAtomInCollection(db.getCollection(2)), -99);
		
		try {
			//rebuild the bit deleted above
			Statement stmt = db.getCon().createStatement();
			for (int i = 1; i < 6; ++i)
				stmt.addBatch("INSERT INTO AtomMembership VALUES (2, " + i + ")");
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}		
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetKnownDatatypes() {
		db.openConnection();
		
		ArrayList<String> types = db.getKnownDatatypes();
		assertTrue(types.contains("AMS"));
		assertTrue(types.contains("ATOFMS"));
		assertTrue(types.contains("TimeSeries"));
		assertFalse(types.contains("timeseries"));
		assertFalse(types.contains("NO EXISTENTE!"));
	
		//getKnownDatatypes uses Java for creating distinctness of datatypes:
		//	see if that's the same result as using SQL SELECT DISTINCT
		try {
			ResultSet rs = db.getCon().createStatement().executeQuery(
					"SELECT DISTINCT Datatype FROM MetaData");
			
			for (int i = 0; rs.next(); ++i){
				assertEquals(types.get(i), rs.getString(1));
			}
			
			rs.close();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetMaxMinDateInCollections() {
		db.openConnection();
		
		//test with default data
		collection.Collection[] colls = new collection.Collection[1];
		colls[0] = db.getCollection(2);
		
		java.util.Calendar min = java.util.Calendar.getInstance();
		java.util.Calendar max = java.util.Calendar.getInstance();		
		
		db.getMaxMinDateInCollections(colls, min, max);
		assertTrue(min != null);
		assertTrue(max != null);
		assertEquals(min.getTime().toString(), max.getTime().toString());

		//put in some more diverse dates
		try {
			String pre = "UPDATE ATOFMSAtomInfoDense SET TIME = '%s' WHERE AtomID = %s";
			Statement stmt = db.getCon().createStatement();

			//CollectionID = 2
			stmt.addBatch(sprintf(pre, "9/1/2003 4:30:38 PM", 1));
			stmt.addBatch(sprintf(pre, "9/12/2003 3:30:38 PM", 2));
			stmt.addBatch(sprintf(pre, "8/4/2003 2:30:38 PM", 3));
			stmt.addBatch(sprintf(pre, "7/4/2003 8:30:38 PM", 5));

			//CollectionID = 3
			stmt.addBatch(sprintf(pre, "10/1/2003 4:30:38 PM", 6));
			stmt.addBatch(sprintf(pre, "8/1/2003 5:30:38 PM", 7));
			stmt.addBatch(sprintf(pre, "11/6/2003 2:30:38 PM", 10));
			
			stmt.executeBatch();

			stmt.close();
		}
		catch (SQLException ex) {
			System.err.println("Couldn't update time values in database");
			ex.printStackTrace();
		}
		
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("M/d/yyyy hh:mm:ss aa");
		
		db.getMaxMinDateInCollections(colls, min, max);
		assertEquals(formatter.format(min.getTime()), "7/4/2003 08:30:38 PM");
		assertEquals(formatter.format(max.getTime()), "9/12/2003 03:30:38 PM");
		
		//try on multiple collections
		colls = new collection.Collection[2];
		colls[0] = db.getCollection(3);
		colls[1] = db.getCollection(2);
		
		db.getMaxMinDateInCollections(colls, min, max);
		
		assertEquals(formatter.format(min.getTime()), "7/4/2003 08:30:38 PM");
		assertEquals(formatter.format(max.getTime()), "11/6/2003 02:30:38 PM");
		
		//try with nulls
		min.setTime(new Date(0));
		max.setTime(new Date(0));

		try {
			String pre = "UPDATE ATOFMSAtomInfoDense SET TIME = NULL WHERE AtomID = ";
			Statement stmt = db.getCon().createStatement();
			for (int i = 1; i <= 11; ++i) {
				stmt.addBatch(pre + i);
			}
			stmt.executeBatch();
			stmt.close();
		}
		catch (SQLException ex) {
			System.err.println("Couldn't update time values in database");
			ex.printStackTrace();
		}
		
		db.getMaxMinDateInCollections(colls, min, max);
		
		assertEquals(min.getTimeInMillis(), 0);
		assertEquals(max.getTimeInMillis(), 0);
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetPrimaryKey() {
		db.openConnection();
		
		ArrayList<String> ret = db.getPrimaryKey("ATOFMS", DynamicTable.DataSetInfo);
		assertTrue(ret != null);
		assertEquals(ret.size(), 0);
		
		ret = db.getPrimaryKey("ATOFMS", DynamicTable.AtomInfoDense);
		assertTrue(ret != null);
		assertEquals(ret.size(), 0);
		
		ret = db.getPrimaryKey("ATOFMS", DynamicTable.AtomInfoSparse);
		assertTrue(ret != null);
		assertEquals(ret.size(), 1);
		assertTrue(ret.get(0).equalsIgnoreCase("[PeakLocation]"));
		
		ret = db.getPrimaryKey("AMS", DynamicTable.DataSetInfo);
		assertTrue(ret != null);
		assertEquals(ret.size(), 0);
		
		ret = db.getPrimaryKey("AMS", DynamicTable.AtomInfoSparse);
		assertTrue(ret != null);
		assertEquals(ret.size(), 1);
		assertTrue(ret.get(0).equalsIgnoreCase("[PeakLocation]"));		
		
		ret = db.getPrimaryKey("Datatype2", DynamicTable.AtomInfoSparse);
		assertTrue(ret != null);
		assertEquals(ret.size(), 1);
		assertTrue(ret.get(0).equalsIgnoreCase("[Delay]"));		
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetRepresentedCluster() {
		db.openConnection();
		db.addCenterAtom(2, 2);
		db.addCenterAtom(6, 3);
	
		assertEquals(db.getRepresentedCluster(2), 2);
		assertEquals(db.getRepresentedCluster(6), 3);
		assertEquals(db.getRepresentedCluster(4), -1);
		assertEquals(db.getRepresentedCluster(3), -1);
		
		db.closeConnection();
	}
	
	/**
	 * Casts an array of objects to an array of uncasted Integers
	 * @param o the array to cast
	 * @return an equally-sized array of ints
	 * @author shaferia
	 */
	private int[] castint(Object[] objs) {
		int[] ret = new int[objs.length];
		
		for (int i = 0; i < ret.length; ++i)
			ret[i] = ((Integer)objs[i]).intValue();
		
		return ret;
	}
	
	/**
	 * @author shaferia
	 */
	public void testGetAllDescendantCollections() {
		db.openConnection();
		
		java.util.Set<Integer> subcolls = db.getAllDescendantCollections(2, true);		
		assertEquals(subcolls.size(), 1);
		assertTrue(((Integer)(subcolls.toArray()[0])).equals(new Integer(2)));
		
		subcolls = db.getAllDescendantCollections(2, false);		
		assertEquals(subcolls.size(), 0);	
		
		db.createEmptyCollection("ATOFMS", 2, "Emptycoll", "Hi!", "");
		
		int[] subids = castint(db.getAllDescendantCollections(2, true).toArray(new Integer[0]));
		assertEquals(subids.length, 2);
		assertEquals(subids[0], 2);
		assertEquals(subids[1], 7);
		
		db.createEmptyCollection("ATOFMS", 7, "Emptycoll2", "", "");
		subcolls = db.getAllDescendantCollections(2, true);
		java.util.Set<Integer> comparer = new java.util.TreeSet<Integer>();
		comparer.add(2);
		comparer.add(7);
		comparer.add(8);
		assertTrue(subcolls.equals(comparer));
		
		db.createEmptyCollection("AMS", 7, "stuff", "", "");
		db.createEmptyCollection("AMS", 8, "two", "", "");

		subcolls = db.getAllDescendantCollections(2, true);
		comparer = new java.util.TreeSet<Integer>();
		comparer.add(2);
		comparer.add(7);
		comparer.add(8);
		comparer.add(9);
		comparer.add(10);
		assertTrue(subcolls.equals(comparer));	
		
		db.closeConnection();
	}

	/**
	 * @author shaferia
	 */
	public void testContainsDatatype() {
		db.openConnection();

		assertTrue(db.containsDatatype("AMS"));
		
		//shouldn't be case sensitive
		assertTrue(db.containsDatatype("ams"));
		
		assertTrue(db.containsDatatype("ATOFMS"));
		assertTrue(db.containsDatatype("atofMS"));
		
		assertTrue(db.containsDatatype("Datatype2"));
		assertTrue(db.containsDatatype("SimpleParticle"));
		assertTrue(db.containsDatatype("TimeSeries"));
		
		try {
			//won't happen in reality, but quotes ought to be stripped in general
			db.containsDatatype("'ATOFMS'");
			fail();
		}
		catch (RuntimeException ex) {
			System.out.println("This should be an error: " + ex.getMessage());
		}
		
		try {
			db.containsDatatype("' OR Datatype = 'AMS");
			fail();
		}
		catch (RuntimeException ex) {
			System.out.println("This should be an error: " + ex.getMessage());
		}

		try {
			db.containsDatatype("''AMS''");
			fail();
		}
		catch (RuntimeException ex) {
			System.out.println("This should be an error: " + ex.getMessage());
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testBatchExecuter() {
		db.openConnection();
		
		try {
			Statement stmt = db.getCon().createStatement();
			Database.BatchExecuter ex = db.getBatchExecuter(stmt);
			ex.append("UPDATE DBInfo SET Value = 'fooo!' WHERE Name = 'Version'");
			ex.execute();
			stmt.close();
			
			stmt = db.getCon().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Value FROM DBInfo WHERE Name = 'Version'");
			assertTrue(rs.next());
			assertEquals(rs.getString(1), "fooo!");
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			fail("Batch executer did not complete properly");
		}
		
		try {
			Statement stmt = db.getCon().createStatement();
			Database.BatchExecuter ex = db.getBatchExecuter(stmt);
			ex.execute();
			
			fail("Batch executer should not execute empty statements.");
		}
		catch (SQLException ex) {
			//success
		}
		
		try {
			int[][] values = 
			{
					{1,2},	
					{2,5},
					{3,3},
					{4,4},
					{5,1},
					{6,6}
			};
			Statement stmt = db.getCon().createStatement();
			Database.BatchExecuter ex = db.getBatchExecuter(stmt);
			ex.append("DELETE FROM AtomMembership");
			for (int i = 0; i < values.length; ++i)
				//make sure it works for newlines and no newlines on the end.
				ex.append("INSERT INTO AtomMembership VALUES(" + 
						values[i][0] + "," + values[i][1] + ")" + ((i % 2 == 0) ? "" : "\n"));
			ex.execute();
			stmt.close();
			
			stmt = db.getCon().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM AtomMembership ORDER BY CollectionID");
			for (int i = 0; i < values.length; ++i) {
				assertTrue(rs.next());
				assertEquals(rs.getInt(1), values[i][0]);
				assertEquals(rs.getInt(2), values[i][1]);
			}
			assertFalse(rs.next());
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			fail("Problem with multiple BatchExecuter comparison");
		}
		
		db.closeConnection();
	}
	
	/**
	 * @author shaferia
	 */
	public void testInserter() {
		db.openConnection();		
		try {
			Statement stmt = db.getCon().createStatement();
			Database.BatchExecuter ex = db.getBatchExecuter(stmt);
			Database.Inserter bi = db.getInserter(ex, "Collections");
			bi.close();
			ex.execute();
		}
		catch (SQLException ex) {
			fail("Inserting nothing into from bulk file should be okay.");
		}
		
		String iname = "[Couldn't create inserter]";
		try {
			Object[][] values = 
			{
					{2, "Hi!", "Foo!", "Desck!", "ATOFMS"},
					{3, "Baz", "Gir", "Zim", "AMS"},
					{4, "-", "-", "-", "-"}
			};
			
			Statement stmt = db.getCon().createStatement();
			Database.BatchExecuter ex = db.getBatchExecuter(stmt);
			Database.Inserter bi = db.getInserter(ex, "Collections");
			iname = bi.getClass().getName();
			
			db.getCon().createStatement().executeUpdate("DELETE FROM Collections");
			
			for (int i = 0; i < values.length; ++i)
				bi.append(values[i][0] + "," + values[i][1] + "," + 
						values[i][2] + "," + values[i][3] + "," + values[i][4]);
			bi.close();
			ex.execute();
			
			
			ResultSet rs = (stmt = db.getCon().createStatement()).executeQuery(
					"SELECT * FROM Collections ORDER BY CollectionID");
			
			for (int i = 0; i < values.length; ++i) {
				assertTrue(rs.next());
				assertEquals(rs.getInt(1), values[i][0]);
				assertEquals(rs.getString(2), values[i][1]);
				assertEquals(rs.getString(3), values[i][2]);
				assertEquals(rs.getString(4), values[i][3]);
				assertEquals(rs.getString(5), values[i][4]);
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			fail("Problem with Inserter " + iname + " execution");
		}
		
		db.closeConnection();
	}
	
	// ***SLH
	public void testgetDatabulkBucket() {
		
		String tempdir = System.getenv("TEMP");
		String tempFilename;
		String[] tables = {"ATOFMSAtomInfoDense", "AtomMembership", "DataSetMembers", "ATOFMSAtomInfoSparse", "InternalAtomOrder"};
		Database.Data_bulkBucket buckets = db.getDatabulkBucket(tables);
		
		for( int i = 0; i< tables.length; i++) {
			assertEquals(buckets.buckets[i].table, tables[i]);
			tempFilename = tempdir + File.separator + buckets.buckets[i].table + ".txt";
			assertEquals(buckets.buckets[i].sqlCmd(), "BULK INSERT " +  tables[i] + " FROM '" + tempFilename + "' WITH (FIELDTERMINATOR=',')\n");
		}	
	}
	// ***SLH  BulkInsertDataParticles saveDataParticle
	public void testsaveAtofmsParticle_BulkInsertAtofmsParticles() {
		 String[] tables= {"ATOFMSAtomInfoDense"};
		 String dense_str = "12-30-06 10:59:49, 1.89E-4, 2.4032946, 4286,E:\\Data\\12-29-2003\\h\\h-031230105949-00001.amz";
		String[] sparse_str = {"23.0, 56673, 0.60352063, 2625", "40.0, 5289, 0.05632348, 450", "-16.0, 17893, 0.06354161, 2607"};
		
		int datasetid = 100;
		int nextAtomID = 100;
		Database.Data_bulkBucket bkts = db.getDatabulkBucket(tables);
		Scanner in;
		ArrayList<String> sparse_arraylist = new ArrayList<String>();
		
		for(int i = 0; i<sparse_str.length; i++)
			sparse_arraylist.add(sparse_str[i]);
		
		
		// ATOFMS table insertion
		db.openConnection();
		Collection c = db.getCollection(0);
		assertEquals(db.saveDataParticle(dense_str, sparse_arraylist, c, datasetid, nextAtomID, bkts), nextAtomID);
		// atofms_bkt.close();
		db.BulkInsertDataParticles(bkts);
		db.closeConnection();
		
		//check ATOFMS data
		db.openConnection();
		try {
			
			Statement stmt = db.getCon().createStatement();
			ResultSet rs = stmt.executeQuery("USE TestDB;\n" + "SELECT * FROM ATOFMSAtomInfoDense where AtomID = 100" );
			assertTrue(rs.next());
			assertEquals(rs.getInt(1), 100);
			// Date are the same but different format. 
			//assertEquals(rs.getDate(2), "12-30-06");
			assertEquals(rs.getTime(2).toString(), "10:59:49");
			assertEquals(rs.getTime(2).toString(), "10:59:49");
			assertEquals(rs.getFloat(3), (float)1.89E-4 );
			assertEquals(rs.getFloat(4), (float)2.4032946);
			assertEquals(rs.getFloat(5), (float)4286.0);
			assertEquals(rs.getString(6), "E:\\Data\\12-29-2003\\h\\h-031230105949-00001.amz");
		}
		catch(SQLException e) {
			fail("Problem inserting ATOFMS data with saveDataParticle() or BulkInsertDataParticles");
		}
		db.closeConnection();
		
		//Only checked the content of one file, others should work the same way.
		File dense_file = new File(System.getenv("TEMP") + "\\ATOFMSAtomInfoDense.txt");
		assertTrue(dense_file.isFile());
		assertTrue(dense_file.canRead());
		
		try {
			String line = "";
			String[] tokens;
			in = new Scanner(dense_file);
			while (in.hasNext()) {
				line = in.nextLine();
				tokens = line.split(",");
				assertEquals(tokens[0], Integer.toString(nextAtomID));
				assertEquals(tokens[1], "12-30-06 10:59:49");
				assertEquals(tokens[2], " 1.89E-4");
				assertEquals(tokens[3], " 2.4032946");
				assertEquals(tokens[4], " 4286");
				assertEquals(tokens[5], "E:\\Data\\12-29-2003\\h\\h-031230105949-00001.amz");
				return;
			}

		} catch (IOException e) {
			System.err.println("IOException occurs when checking the output file content");
		}
		
	}
	
	public void testsaveAmsParticle_BulkInsertAmsParticles() {
		
		String[] tables= {"AMSAtomInfoDense"};
		String ams_dense_str = "12-30-06 10:59:50";
		String[] ams_sparse_str = {"13,14.0,0.0140019", "13,30.0,0.2438613", "13,31.0,9.876385E-4", "13,32.0,4.877227E-4"};
		
		int datasetid = 100;
		int nextAtomID = 100;
		Database.Data_bulkBucket bkts = db.getDatabulkBucket(tables);
		Scanner in;
		ArrayList<String> ams_sparse_arraylist = new ArrayList<String>();
		
		for(int i = 0; i<ams_sparse_str.length; i++) 
			ams_sparse_arraylist.add(ams_sparse_str[i]);
		
		// AMS table insertion
		db.openConnection();
		Collection c = db.getCollection(0);
		assertEquals(db.saveDataParticle(ams_dense_str, ams_sparse_arraylist, c, datasetid, nextAtomID, bkts), nextAtomID);
		// atofms_bkt.close();
		db.BulkInsertDataParticles(bkts);
		db.closeConnection();
		
		//check AMS data
		db.openConnection();
		try {
			
			Statement stmt = db.getCon().createStatement();
			ResultSet rs = stmt.executeQuery("USE TestDB;\n" + "SELECT * FROM AMSAtomInfoDense where AtomID = 100" );
			assertTrue(rs.next());
			assertEquals(rs.getInt(1), 100);
			// Date are the same but different format. 
			//assertEquals(rs.getDate(2), "12-30-06");
			assertEquals(rs.getTime(2).toString(), "10:59:50");
		}
		catch(SQLException e) {
			fail("Problem inserting ATOFMS data with saveDataParticle() or BulkInsertDataParticles");
		}
		db.closeConnection();
		
		//Only checked the content of one file, others should work the same way.
		File dense_file = new File(System.getenv("TEMP") + "\\AMSAtomInfoDense.txt");
		assertTrue(dense_file.isFile());
		assertTrue(dense_file.canRead());
		
		try {
			String line = "";
			String[] tokens;
			in = new Scanner(dense_file);
			while (in.hasNext()) {
				line = in.nextLine();
				tokens = line.split(",");
				assertEquals(tokens[0], Integer.toString(nextAtomID));
				assertEquals(tokens[1], "12-30-06 10:59:50");
				return;
			}

		} catch (IOException e) {
			System.err.println("IOException occurs when checking the output file content");
		}
		
	}
	

	/**
	 * @author shaferia
	 */
	public void testGetCollection() {
		db.openConnection();
		
		Collection c = db.getCollection(0);
		assertEquals(c.getDatatype(), "root");
		assertEquals(c.getDescription(), "root");
		assertEquals(c.getComment(), "root for unsynchronized data");
		assertEquals(c.getName(), "ROOT");
		assertEquals(c.getCollectionID(), 0);
		
		c = db.getCollection(1);
		assertEquals(c.getDatatype(), "root");
		assertEquals(c.getDescription(), "root");
		assertEquals(c.getComment(), "root for synchronized data");
		assertEquals(c.getName(), "ROOT-SYNCHRONIZED");
		assertEquals(c.getCollectionID(), 1);
		
		c = db.getCollection(2);
		assertEquals(c.getDatatype(), "ATOFMS");
		assertEquals(c.getDescription(), "onedescrip");
		assertEquals(c.getComment(), "one");
		assertEquals(c.getName(), "One");
		assertEquals(c.getCollectionID(), 2);
		
		db.closeConnection();
	}
}