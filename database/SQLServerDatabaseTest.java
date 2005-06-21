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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import msanalyze.CalInfo;

import analysis.ParticleInfo;
import atom.ATOFMSParticle;
import atom.EnchiladaDataPoint;
import atom.Particle;
import atom.Peak;
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
	
	public void testGetNextId(){
		db.openConnection();
		
		assertTrue(db.getNextID() >= 0);
	
		db.closeConnection();
	}
	
	public void testOrphanAndAdopt(){
		
		db.openConnection();
		//get info on atoms in collection 4
		ArrayList<Integer> collection4Info = new ArrayList<Integer>();
		collection4Info.add(10);
		collection4Info.add(11);
		collection4Info.add(12);
		
		assertTrue(db.orphanAndAdopt(4));
		//make sure that the atoms collected before are in collection 3
		ArrayList<Integer> collection3Info = new ArrayList<Integer>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("USE TestDB\n" +
			"SELECT AtomID\n" +
			"FROM AtomMembership\n" +
			"WHERE CollectionID = 3");
			while (rs.next()){
				collection3Info.add(rs.getInt(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(collection3Info.containsAll(collection4Info));
		
		assertFalse(db.orphanAndAdopt(7)); //not in database
		assertFalse(db.orphanAndAdopt(1)); //is not a subcollection (prints this)
		
		db.closeConnection();
	}

	public void testRecursiveDelete(){
		db.openConnection();
		
		ArrayList<Integer> atomIDs = new ArrayList<Integer>();
		Statement stmt;
		try {
				stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("USE TestDB\n" +
					"SELECT AtomID\n"
					+" FROM AtomMembership\n"
					+" WHERE CollectionID = 3"
					+" OR CollectionID = 4");
			while (rs.next()){
				atomIDs.add(rs.getInt(1));
			}
			
			assertTrue(db.recursiveDelete(3));
			
			//make sure info for 3 and 4 is gone from database
			for (Integer atomID : atomIDs){
				rs = stmt.executeQuery("USE TestDB\n" +
						"SELECT * FROM AtomInfo\n"
						+ " WHERE AtomID = " + atomID);
				assertFalse(rs.next());
				rs = stmt.executeQuery("USE TestDB\n" +
						"SELECT * FROM AtomMembership\n"
						+ " WHERE AtomID = " + atomID);
				assertFalse(rs.next());
				rs = stmt.executeQuery("USE TestDB\n" +
						"SELECT * FROM OrigDataSets\n"
						+ " WHERE AtomID = " + atomID);
				assertFalse(rs.next());
				rs = stmt.executeQuery("USE TestDB\n" +
						"SELECT * FROM Peaks\n"
						+ " WHERE AtomID = " + atomID);
				assertFalse(rs.next());
			}
			//make sure collection info and relationship info is gone
			rs = stmt.executeQuery("USE TestDB\n" +
					"SELECT * FROM CollectionRelationships\n"
					+ " WHERE ChildID = 3"
					+ " OR ChildID = 4"
					+ " OR ParentID = 3"
					+ " OR ParentID = 4");
			assertFalse(rs.next());
			rs = stmt.executeQuery("USE TestDB\n" +
					"SELECT * FROM Collections\n"
					+ " WHERE CollectionID = 3"
					+ " OR CollectionID = 4");
			assertFalse(rs.next());
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.closeConnection();

	}
	
	public void testGetCollectionName(){
		db.openConnection();
		
		assertTrue( "One".equals(db.getCollectionName(1)) );
		assertTrue( "Two".equals(db.getCollectionName(2)) );
		assertTrue( "Three".equals(db.getCollectionName(3)) );
		assertTrue( "Four".equals(db.getCollectionName(4)) );
		
		db.closeConnection();
	}
	
	public void testGetCollectionComment(){
		db.openConnection();
		
		assertTrue( "one".equals(db.getCollectionComment(1)) );
		assertTrue( "two".equals(db.getCollectionComment(2)) );
		assertTrue( "three".equals(db.getCollectionComment(3)) );
		assertTrue( "four".equals(db.getCollectionComment(4)) );
		
		db.closeConnection();
	}
	
	public void testGetCollectionSize(){
		db.openConnection();
		
		final String filename = "file1";
		final String dateString = "1983-01-19 05:05:00.0";
		final float laserPower = (float)0.01191983;
		final float size = (float)0.5;
		final float digitRate = (float)0.1;
		final int scatterDelay = 10;
		int posSpectrum[] = new int[30000];
		int negSpectrum[] = new int[30000];
		int collectionID = 1;
		int datasetID = 1;
		
		assertTrue( db.getCollectionSize(collectionID) == 3 );
		
		ATOFMSParticle particle = 
			new ATOFMSParticle(filename, dateString, laserPower, digitRate,
				scatterDelay, posSpectrum, negSpectrum);
		
		db.insertATOFMSParticle(particle, collectionID, datasetID,
				db.getNextID());
		
		assertTrue( db.getCollectionSize(collectionID) == 4 );
	
		//should return size of collection + subcollection
		assertTrue( db.getCollectionSize(3) == 6);
			
		db.closeConnection();
	}

	public void testGetAllDescendedAtoms(){
		db.openConnection();
		
		//case of no child collections
		int[] expected = {1,2,3};
		ArrayList<Integer> actual = db.getAllDescendedAtoms(1);
		
		for (int i=0; i<actual.size(); i++)
			assertTrue(actual.get(i) == expected[i]);
		
		//case of one child collection
		int[] expected2 = {7,8,9,10,11,12};
		ArrayList<Integer> actual2 = db.getAllDescendedAtoms(3);
		
		for (int i=1; i<=actual.size(); i++)
			assertTrue(actual2.get(i) == expected2[i]);
		
		db.closeConnection();
	}

	public void testGetCollectionParticles(){
		db.openConnection();
		
		//we know the particle info from inserting it	
		ArrayList<ATOFMSParticleInfo> actual = db.getCollectionParticles(1);
		assertEquals(actual.size(), 3);
		
		assertEquals(actual.get(0).getAtomID(), 1);
//		assertEquals(actual.get(0).getLaserPower(), 1);  not retrieved in method
		assertEquals(actual.get(0).getSize(), (float)0.1);
//		assertEquals(actual.get(0).getScatDelay(), 1);	not retrieved in method
		assertEquals(actual.get(0).getFilename(), "One");
			
		//testing for subcollection detection
		ArrayList<ATOFMSParticleInfo> actual2 = db.getCollectionParticles(3);
		assertEquals(actual2.size(), 6);
				
		db.closeConnection();
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
	
	public void testIsPresent() {
		db.openConnection();
		assertTrue(SQLServerDatabase.isPresent("localhost","1433","TestDB"));
		db.closeConnection();
	}
	
	public void testExportToMSAnalyzeDatabase() {
		db.openConnection();
		java.util.Date date = db.exportToMSAnalyzeDatabase(1,"MSAnalyzeDB","MS-Analyze");
		assertTrue(date.toString().equals("2003-09-02"));
		db.closeConnection();
	}
	
	private void testCursor(CollectionCursor curs)
	{
		ArrayList<ParticleInfo> partInfo = new ArrayList<ParticleInfo>();
		
		ParticleInfo temp = new ParticleInfo();
		ATOFMSParticleInfo tempPI = 
			new ATOFMSParticleInfo(
					1,"One",1,0.1f,
					new Date("9/2/2003 5:30:38 PM"));
		//int aID, String fname, int sDelay, float lPower, Date tStamp
		temp.setParticleInfo(tempPI);
		temp.setID(1);
		
		
		
		partInfo.add(temp);
		
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
	
	public void testGetParticleInfoOnlyCursor() {
		db.openConnection();
		CollectionCursor curs = db.getParticleInfoOnlyCursor(1);
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetSQLCursor() {
		db.openConnection();
		CollectionCursor curs = db.getSQLCursor(1, "AtomInfo.AtomID != 20");
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetPeakCursor() {
		db.openConnection();
		CollectionCursor curs = db.getPeakCursor(1);
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetBinnedCursor() {
		db.openConnection();
		CollectionCursor curs = db.getBinnedCursor(1);
		testCursor(curs);
		db.closeConnection();
	}
	
	public void testGetMemoryBinnedCursor() {
		db.openConnection();
		CollectionCursor curs = db.getMemoryBinnedCursor(1);	
		testCursor(curs);	
		db.closeConnection();
	}
	
	public void testGetRandomizedCursor() {
		db.openConnection();
		CollectionCursor curs = db.getRandomizedCursor(1);	
		testCursor(curs);	
		db.closeConnection();
	}
	
	public void testMoveAtom() {
		db.openConnection();
		assertTrue(db.moveAtom(1,1,2));
		assertTrue(db.moveAtom(1,2,1));
		db.closeConnection();
	}
	
	public void testMoveAtomBatch() {
		db.openConnection();
		db.atomBatchInit();
		assertTrue(db.moveAtomBatch(1,1,2));
		assertTrue(db.moveAtomBatch(1,2,1));
		db.executeBatch();
		db.closeConnection();
	}
	
	/**
	 * Tests AddAtom and DeleteAtomBatch
	 *
	 */
	public void testAddAndDeleteAtom() {
		db.openConnection();
		db.atomBatchInit();
		assertTrue(db.deleteAtomBatch(1,1));
		db.executeBatch();
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
		assertTrue(db.deleteAtomsBatch("1",1));
		assertTrue(db.addAtomBatch(1,1));
		db.executeBatch();
		db.closeConnection();
	}
	
	public void testCheckAtomParent() {
		db.openConnection();
		assertTrue(db.checkAtomParent(1,1));
		assertFalse(db.checkAtomParent(1,3));
		db.closeConnection();
	}
	
	public void testGetAndSetCollectionDescription() {
		db.openConnection();
		String description = db.getCollectionDescription(1);
		assertTrue(db.setCollectionDescription(1,"new description"));		
		assertTrue(db.getCollectionDescription(1).equals("new description"));
		db.setCollectionDescription(1,description);
		db.closeConnection();
	}
	
	/* Can't try dropping db because it's in use.
	public void testDropDatabase() {
		db.openConnection();
		assertTrue(SQLServerDatabase.dropDatabase("TestDB"));
		setUp();
	} */
	
	public void testGetPeaks() {
		db.openConnection();
		Peak peak = db.getPeaks(2).get(0);
		assertTrue(peak.area == 15);
		assertTrue(peak.relArea == 0.006f);
		assertTrue(peak.height == 12);
		db.closeConnection();
	}


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

	
}
