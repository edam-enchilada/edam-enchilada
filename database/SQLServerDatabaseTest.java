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

import gui.ATOFMSParticleInfo;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import msanalyze.CalInfo;

import analysis.ParticleInfo;
import atom.ATOFMSParticle;
import atom.PeakParams;

/**
 * @author andersbe
 *
 */
public class SQLServerDatabaseTest extends TestCase {
	private SQLServerDatabase db;
	Connection con;
	
	public SQLServerDatabaseTest(String aString)
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
	}

	public void testOpenandCloseConnection() {
		assertTrue(db.openConnection());
		assertTrue(db.closeConnection());
	}

	// Hopefully there's a better way to test this, but right now it
	// relies on the database having more collections than just root
	// and succeeds if the function returns pretty much anything.  
	public void testGetImmediateSubcollections() {
		
		db.openConnection();
		
		ArrayList<Integer> test = db.getImmediateSubCollections(0);
		
		assertTrue(test.size() == 3);
		assertTrue(test.get(0).intValue() == 1);
		assertTrue(test.get(1).intValue() == 2);
		assertTrue(test.get(2).intValue() == 3);
		
		ArrayList<Integer> collections = new ArrayList<Integer>();
		collections.add(new Integer(0));
		collections.add(new Integer(3));
		test = db.getImmediateSubCollections(collections);
		assertTrue(test.size() == 4);
		assertTrue(test.get(0).intValue() == 1);
		assertTrue(test.get(1).intValue() == 2);
		assertTrue(test.get(2).intValue() == 3);
		assertTrue(test.get(3).intValue() == 4);
		
		db.closeConnection();
	}
	
	
	public void testCreateEmptyCollectionAndDataset() {
		db.openConnection();
		
		int ids[] = db.createEmptyCollectionAndDataset(0,
				"Dataset","dataset","mCalFile","sCalFile",
				new CalInfo(), new PeakParams(12,20,(float)0.005));
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = con.createStatement().executeQuery(
					"USE TestDB\n" +
					"SELECT *\n" +
					"FROM PeakCalibrationData\n" +
					"WHERE DataSetID = " + ids[1]);
			assertTrue(rs.next());
			assertTrue(rs.getString(2).equals("Dataset"));
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
			assertTrue(rs.next());
			//System.out.println(rs.getString("Name"));
			//System.out.println(rs.getString("Comment"));
			assertTrue(rs.getString("Name").equals("Dataset"));
			assertTrue(rs.getString("Comment").equals("dataset"));
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
		int collectionID = db.createEmptyCollection(0,"Collection","collection");
		try {
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

	public void testCopyCollection() {
		db.openConnection();
		
		int newLocation = db.copyCollection(2,1);
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT Name, Comment\n" +
					"FROM Collections\n" +
					"WHERE CollectionID = 2");
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
			assertTrue(rs.getInt(1) == 1);
			assertFalse(rs.next());
			rs.close();
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT AtomID\n" +
					"FROM AtomMembership\n" +
					"WHERE CollectionID = 2\n" +
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
			db.closeConnection();
			rs.close();
			stmt.close();
			rs2.close();
			stmt2.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public void testMoveCollection() {
		db.openConnection();
		assertTrue(db.moveCollection(2,1));
		db.closeConnection();
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT ParentID\n" +
					"FROM CollectionRelationships\n" +
					"WHERE ChildID = 2");
			
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 1);
			assertFalse(rs.next());
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void testInsertATOFMSParticle() {
		db.openConnection();
		final String filename = "file1";
		final String dateString = "1983-01-19 05:05:00.0";
		final float laserPower = (float)0.01191983;
		final float size = (float)0.5;
		final float digitRate = (float)0.1;
		final int scatterDelay = 10;
		int posSpectrum[] = new int[30000];
		int negSpectrum[] = new int[30000];
		
		ATOFMSParticle.currCalInfo = new CalInfo();
		ATOFMSParticle.currPeakParams = new PeakParams(12,20,(float)0.005);
		
		for (int i = 0; i < 30000; i++)
		{
			posSpectrum[i] = negSpectrum[i] = 0;
		}
		
		int posPeakLocation1 = 19;
		int negPeakLocation1 = 20;
		int peak1Height = 80;
		posSpectrum[posPeakLocation1] = negSpectrum[negPeakLocation1] = 
			peak1Height;
		
		int posPeakLocation2 = 100;
		int negPeakLocation2 = 101;
		int peak2Height = 100;
		posSpectrum[posPeakLocation2] = negSpectrum[negPeakLocation2] = 
			peak2Height;
		
		int collectionID, datasetID;
		collectionID = 2;
		datasetID = 1;
		int particleID = db.insertATOFMSParticle(
				new ATOFMSParticle(
				filename,dateString,laserPower,digitRate,
				scatterDelay,
				posSpectrum, negSpectrum),
				collectionID,datasetID,db.getNextID());
		db.closeConnection();
		
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT PeakLocation, PeakArea, RelPeakArea," +
					" PeakHeight\n" +
					"FROM Peaks\n" +
					"WHERE AtomID = " + particleID + "\n" +
					"ORDER BY PeakLocation ASC");
			
			assertTrue(rs.next());

		
			assertTrue(rs.getFloat(1) == (float) 
					-(negPeakLocation2 * negPeakLocation2));
			assertTrue(rs.getInt(2) == peak2Height);
			assertTrue(rs.getFloat(3) == (float) peak2Height / 
					(peak1Height + peak2Height));
			assertTrue(rs.getInt(4) == peak2Height);
			
			assertTrue(rs.next());
			
			assertTrue(rs.getFloat(1) == 
				-(negPeakLocation1 * negPeakLocation1));
			assertTrue(rs.getInt(2) == peak1Height);
			assertTrue(rs.getFloat(3) == (float) peak1Height / 
					(peak1Height + peak2Height));
			assertTrue(rs.getInt(4) == peak1Height);
			
			assertTrue(rs.next());

			assertTrue(rs.getFloat(1) == 
				posPeakLocation1 * posPeakLocation1);
			assertTrue(rs.getInt(2) == peak1Height);
			assertTrue(rs.getFloat(3) == (float) peak1Height / 
					(peak1Height + peak2Height));
			assertTrue(rs.getInt(4) == peak1Height);
			
			assertTrue(rs.next());
			
			assertTrue(rs.getFloat(1) == posPeakLocation2
					* posPeakLocation2);
			assertTrue(rs.getInt(2) == peak2Height);
			assertTrue(rs.getFloat(3) == (float) peak2Height / 
					(peak1Height + peak2Height));
			assertTrue(rs.getInt(4) == peak2Height);
			assertFalse(rs.next());
			
			rs = stmt.executeQuery(
					"USE TestDB\n" +
					"SELECT [Time], LaserPower, [Size], ScatDelay, " +
					"OrigFilename\n" +
					"FROM AtomInfo\n" +
					"WHERE AtomID = " + particleID);
			rs.next();

			assertTrue(rs.getString(1).equals(dateString));
			assertTrue(rs.getFloat(2) == laserPower / 1000);
			assertTrue(rs.getFloat(3) == 0.0); // size
			assertTrue(rs.getInt(4) == scatterDelay);
			assertTrue(rs.getString(5).equals(filename));
			
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
					"FROM OrigDataSets \n" +
					"WHERE AtomID = " + particleID);
			
			rs.next();
			assertTrue(rs.getInt(1) == datasetID);
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void testRebuildDatabase() {

		try {
			con.close();
			db.closeConnection();
			
			assertTrue(SQLServerDatabase.rebuildDatabase("TestDB"));		
			
			db.openConnection();
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://localhost:1433;SpASMSdb;SelectMethod=cursor;","SpASMS","finally");	
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("EXEC sp_helpdb");
			boolean foundDatabase = false;
			while (!foundDatabase && rs.next())
				if (rs.getString(1).equals("TestDB"))
					foundDatabase = true;
			assertTrue(foundDatabase);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void testCursor(CollectionCursor curs)
	{
		ArrayList<ParticleInfo> partInfo = 
			new ArrayList<ParticleInfo>();
		
		ParticleInfo temp = new ParticleInfo();
		ATOFMSParticleInfo tempPI = 
			new ATOFMSParticleInfo(
					1,"One",1,0.1f,
					new Date("9/2/2003 5:30:38 PM"));
		//int aID, String fname, int sDelay, float lPower, Date tStamp
		temp.setParticleInfo(tempPI);
		temp.setID(1);
		
		
		
		partInfo.add(temp);
		
		//"VALUES (1,'9/2/2003 5:30:38 PM',1,0.1,1,'One')\n" +
		//"VALUES (2,'9/2/2003 5:30:38 PM',2,0.2,2,'Two')\n" +
		//"VALUES (3,'9/2/2003 5:30:38 PM',3,0.3,3,'Three')\n" +
		
		int[] ids = new int[3];
		for (int i = 0; i < 3; i++)
		{
			assertTrue(curs.next());
			assertTrue(curs.getCurrent()!= null);
			ids[i] = curs.getCurrent().getID();
		}
		
		assertFalse(curs.next());
		
		curs.reset();
		
		for (int i = 0; i < 3; i++)
		{
			assertTrue(curs.next());
			assertTrue(curs.getCurrent() != null);
			assertTrue(curs.getCurrent().getID() == ids[i]);
		}
		
		assertFalse(curs.next());
		
		curs.reset();
	}
	
	public void testGetBinnedCursor()
	{
		db.openConnection();
		
		CollectionCursor curs = db.getBinnedCursor(1);
		
		testCursor(curs);
		
		db.closeConnection();
	}
	
	public void testGetMemoryBinnedCursor()
	{
		db.openConnection();
		
		CollectionCursor curs = db.getMemoryBinnedCursor(1);
		
		testCursor(curs);
		
		db.closeConnection();
	}
	
	public void testSeedRandom()
	{
	    db.openConnection();
	    db.seedRandom(12345);
	    double rand1 = db.getNumber();
	    db.seedRandom(12345);
	    double rand2 = db.getNumber();
	    assertTrue(rand1==rand2);
	    db.closeConnection();
	    
	}

	public void testOrphanAndAdopt() 
	{
		//TODO Implement orphanAndAdopt().
		
	}

	public void testRecursiveDelete() 
	{
		//TODO Implement recursiveDelete().
	}
}
