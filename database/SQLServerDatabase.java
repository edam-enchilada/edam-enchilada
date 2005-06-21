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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.sql.*;

import analysis.BinnedPeakList;
import analysis.ParticleInfo;
import analysis.clustering.PeakList;
import atom.*;
import gui.*;
import java.io.*;
import java.util.Scanner;

import msanalyze.CalInfo;


/**
 * @author andersbe
 *
 */
public class SQLServerDatabase implements InfoWarehouse
{
	private Connection con;
	private String url;
	private String port;
	private String database;
	private int instance = 0;
	private String tempdir = System.getenv("TEMP");
	private Statement batchStatement;
	
	public SQLServerDatabase()
	{
		url = "localhost";
		port = "1433";
		database = "SpASMSdb";
	}
	
	public SQLServerDatabase(String u, String p, String db)
	{
		url = u;
		port = p;
		database = db;
	}
	
	/**
	 * Determine if the database is actually present (returns true if it is).
	 */
	public static boolean isPresent(String url, String port, String dbName) {

		boolean foundDatabase = false;
		try {
			SQLServerDatabase db = new SQLServerDatabase(url,port,"");
			db.openConnection();
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			
			// See if database exists. If it does, drop it.
			ResultSet rs = stmt.executeQuery("EXEC sp_helpdb");
			while (!foundDatabase && rs.next())
				if (rs.getString(1).equals(dbName))
					foundDatabase = true;
		} catch (SQLException e) {
			System.err.println("Error in testing if " + dbName + " is present.");
			e.printStackTrace();
		}
		return foundDatabase;
	}
	
	
	/**
	 * Opens a connection to the database, flat file, memory structure,
	 * or whatever you're working with.  
	 * @return true on success
	 */
	public boolean openConnection()
	{

		try {
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load current driver.");
			return false;
		} // end catch
		con = null;
		try {
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + url + ":" + port + ";DatabaseName=" + database + ";SelectMethod=cursor;","SpASMS","finally");
		} catch (Exception e) {
			System.err.println("Failed to establish a connection to SQL Server");
			System.err.println(e);
		}
		return true;
	}

	
	/**
	 * Closes existing connection
	 * @return true on success.
	 */
	public boolean closeConnection()
	{
		if (con != null)
		{
			try {
				con.close();
			} catch (Exception e) {
				System.err.println("Could not close the connection: ");
				System.err.println(e);
				return false;
			}
			return true;
		}
		else
			return false;
	}
	
	/**
	 * createEmptyCollectionAndDataset is used for the initial 
	 * importation of TSI ATOFMS data.  It creates an empty collection
	 * which can then be filled using insertATOFMSParticle, using the 
	 * return values as parameters.  
	 * @param parent The ID of the parent to insert this collection at
	 * (0 for root)
	 * @param datasetName The name of the dataset, 
	 * @param comment The comment from the dataset
	 * @param massCalFile the name of the mass cal file
	 * @param sizeCalFile the name of the size cal file
	 * @param params the peaklist parameters used to peaklist
	 * @return int[0] = collectionID, int[1] = datasetID
	 */
	public int[] createEmptyCollectionAndDataset(int parent,
											     String datasetName,
											     String comment,
											     String massCalFile,
											     String sizeCalFile,
												 CalInfo cInfo,
											     PeakParams params)
	{
		int[] returnVals = new int[2];
		if (sizeCalFile.equals(".par file"))
			sizeCalFile = "none";
		int autocal;
		if (cInfo.autocal)
			autocal = 1;
		else
			autocal = 0;
		
		returnVals[0] = createEmptyCollection(parent, datasetName,comment,
				"Dataset Name: " + datasetName + "\n" +
				"Mass Calibration File: " + massCalFile + "\n" +
				"Size Calibration File: " + sizeCalFile + "\n" + 
				"Minimum Area = " + params.minArea + "\n" +
				"Minimum Height = " + params.minHeight + "\n" +
				"Minimum Relative Area = " + params.minRelArea + "\n" +
				"Autocal = " + cInfo.autocal + "\n" +
				"Comment: " + comment);
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT MAX (DataSetID)\n" +
											 "FROM PeakCalibrationData");

			if (rs.next())
				returnVals[1] = rs.getInt(1)+1;
			else
				returnVals[1] = 0;
				
			stmt.executeUpdate("INSERT INTO PeakCalibrationData\n" +
							   "(DataSetID, DataSet, MassCalFile, " +
							   "SizeCalFile, MinHeight, MinArea, " +
							   "MinRelArea, Autocal)\n" +
							   "VALUES(" + 
							   returnVals[1] + ", '" +
							   datasetName + "', '" + massCalFile +
							   "', '" + sizeCalFile + "', " + 
							   params.minHeight + 
							   ", " + params.minArea +
							   ", " +
							   params.minRelArea + ", " +
							   autocal + 
							   ")");
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception creating the dataset entries:");
			e.printStackTrace();
		}
		return returnVals;
	}
	
	/**
	 * Creates an empty collection with no atomic analysis units in it.
	 * @param parent	The location to add this collection under (0 
	 * 					to add at the root).
	 * @param name		What to call this collection in the interface.
	 * @param comment	A comment for this collection
	 * @return			The collectionID of the resulting collection
	 */
	public int createEmptyCollection(int parent, 
									 String name, 
									 String comment,
									 String description)
	{
		int nextID = -1;
		try {
			Statement stmt = con.createStatement();
			
			// Get next CollectionID:
			ResultSet rs = stmt.executeQuery("SELECT MAX(CollectionID)\n" +
										"FROM Collections\n");
			rs.next();
			nextID = rs.getInt(1) + 1;
			
			stmt.executeUpdate("INSERT INTO Collections\n" +
							   "(CollectionID, Name, Comment, Description)\n" +
							   "VALUES (" +
							   Integer.toString(nextID) + 
							   ", '" + name + "', '" + comment + "', '" + 
							   description + "')");
			stmt.executeUpdate("INSERT INTO CollectionRelationships\n" +
							   "(ParentID, ChildID)\n" +
							   "VALUES (" + Integer.toString(parent) +
							   ", " + Integer.toString(nextID) + ")");
			
			
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception creating empty collection:");
			e.printStackTrace();
			return -1;
		}
		return nextID;
	}	
	
	/**
	 * Creates an empty collection with no atomic analysis units in it.
	 * @param parent	The location to add this collection under (0 
	 * 					to add at the root).
	 * @param name		What to call this collection in the interface.
	 * @param comment	A comment for this collection
	 * @return			The collectionID of the resulting collection
	 */
	public int createEmptyCollection(int parent, 
									 String name, 
									 String comment)
	{
		int nextID = -1;
		try {
		    if (con == null)
		        throw new IllegalStateException(
		                "Database connection not open.");
			Statement stmt = con.createStatement();
			
			// Get next CollectionID:
			ResultSet rs = stmt.executeQuery("SELECT MAX(CollectionID)\n" +
										"FROM Collections\n");
			rs.next();
			nextID = rs.getInt(1) + 1;
			stmt.executeUpdate("INSERT INTO Collections\n" +
							   "(CollectionID, Name, Comment)\n" +
							   "VALUES (" +
							   Integer.toString(nextID) + 
							   ", '" + name + "', '" + comment +
							   "')");
			stmt.executeUpdate("INSERT INTO CollectionRelationships\n" +
							   "(ParentID, ChildID)\n" +
							   "VALUES (" + Integer.toString(parent) +
							   ", " + Integer.toString(nextID) + ")");
			
			
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception creating empty collection:");
			e.printStackTrace();
			return -1;
		}
		return nextID;
	}

	/**
	 * Similar to moveCollection, except instead of removing the 
	 * collection and its unique children, the original collection 
	 * remains with original parent and a duplicate with a new id is 
	 * assigned to the new parent.  
	 * @param collectionID The collection id of the collection to move.
	 * @param toParentID The collection id of the new parent.  
	 * @return The collection id of the copy.  
	 */
	public int copyCollection(int collectionID, int toParentID)
	{
		//TODO: Remove items from supercollection if we're pasting 
		//into a subcollection of the current collection, ie if 
		// collectionID == toParentID
		int newID = -1;
		try {
			Statement stmt = con.createStatement();
			
			// Get Collection info:
			ResultSet rs = stmt.executeQuery("SELECT Name, Comment\n" +
										"FROM Collections\n" +
										"WHERE CollectionID = " +
										collectionID);
			rs.next();
			newID = createEmptyCollection(toParentID, 
										  rs.getString("Name"),
										  rs.getString("Comment"));
			String description = getCollectionDescription(collectionID);
			if (description  != null)
				setCollectionDescription(newID, getCollectionDescription(collectionID));

			rs = stmt.executeQuery("SELECT AtomID\n" +
							       "FROM AtomMembership\n" +
								   "WHERE CollectionID = " +
								   collectionID);
			while (rs.next())
			{
				stmt.addBatch("INSERT INTO AtomMembership\n" +
						"(CollectionID, AtomID)\n" +
						"VALUES (" + newID + ", " +
						rs.getInt("AtomID") + 
						")");
			}
			/*stmt.addBatch("INSERT INTO AtomMembership\n" +
					"(CollectionID, AtomID)\n" +
					"VALUES (" + newID + ", " +
					"SELECT AtomID\n" +
				    "FROM AtomMembership\n" +
					"WHERE CollectionID = " +
					collectionID + ")"
					);*/
			stmt.executeBatch();
			
			// Get Children
			rs = stmt.executeQuery("SELECT ChildID\n" +
								   "FROM CollectionRelationships\n" +
								   "WHERE ParentID = " +
								   Integer.toString(collectionID));
			while (rs.next())
			{
				copyCollection(rs.getInt("ChildID"),newID);
			}
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception copying collection: ");
			e.printStackTrace();
			return -1;
		}
		return newID;
	}
	
	
	/**
	 * Create a new collection from an array list of atomIDs which 
	 * have yet to be inserted into the database.  
	 * 
	 * @param parentID	The location of the parent to insert this
	 * 					collection (0 to insert at root level)
	 * @param name		What to call this collection
	 * @param comment	What to leave as the comment
 	 * @param atomType	The type of atoms you are inserting ("ATOFMSParticle" most likely
	 * @param atomList	An array list of atomID's to insert into the 
	 * 					database
	 * @return			The CollectionID of the new collection, -1 for
	 * 					failure.
	 */
	public int createCollectionFromAtoms(int parentID,
										 String name,
										 String comment,
										 String atomType,
										 ArrayList atomList)
	{
		if (atomType.equals("ATOFMSParticle"))
		{
			int collectionID = createEmptyCollection(parentID, 
													 name,
													 comment);
			
			if (!insertAtomicList(atomList,collectionID,atomType))
				return -1;
			return collectionID;
		}
		return -1;
	}

	/**
	 * Inserts a list of AtomicAnalysisUnits to the warehouse.  Intended 
	 * for use on original importation of atoms.
	 * 
	 * @param atomList An ArrayList of AtomicAnalysisUnits which describe
	 * the atoms to add to the warehouse.
	 * @param collectionID The collectionID of the collection to add the
	 * particles to.  
	 * @param atomType A string description of the subclass of atom to 
	 * be inserted.  
	 * @return true on success. 
	 */
	private boolean insertAtomicList(ArrayList atomList, 
									int collectionID, 
									String atomType)
	{
		// first, create entries for the Atoms in the AtomInfo table
		// and the peaklist table
		int[] atomIDs = createAtomInfo(atomList, atomType);	
		if (!createPeaks(atomList, atomIDs, atomType))
			return false;
		// now add atomIDs to the ownership table
		try {
			Statement stmt = con.createStatement();
			for (int i = 0; i < atomIDs.length; i++)
			{
				stmt.addBatch("INSERT INTO AtomMembership\n" +
							  "(CollectionID,AtomID)\n" +
							  "VALUES (" + 
							  Integer.toString(collectionID) + ", " +
							  Integer.toString(atomIDs[i]) + ")");
			}
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception adding particle memberships:");
			System.err.println(e);
			return false;
		}
		return true;
	}
	
	private int[] createAtomInfo(ArrayList atomList, String atomType)
	{
		int idArray[] = null;
			try{
				Statement stmt = con.createStatement();
				
				ResultSet rs = stmt.executeQuery("SELECT MAX (AtomID)\n" +
				                               	 "FROM AtomInfo");
				int nextID = -1;
				if(rs.next())
					nextID = rs.getInt(1) + 1;
				else
					nextID = 0;
				idArray = new int[atomList.size()];
				
				if (atomType.equals("ATOFMSParticle")) {
				for (int i = 0; i < atomList.size(); i++)
				{
					idArray[i] = nextID;
					ATOFMSParticle currentParticle = (ATOFMSParticle) atomList.get(i);
					
					stmt.addBatch("INSERT INTO AtomInfo\n" +
						  	  	  "(AtomID,[Time],LaserPower,Size," +
						  	  	  "OrigFilename)\n" +
								  "VALUES (" + 
								  Integer.toString(nextID) + ", " +
								  "'" + 
								  currentParticle.time + 
								  "', " + 
								  Float.toString((currentParticle.laserPower/(float)1000)) + 
								  ", " + 
								  Float.toString(currentParticle.size) + ", '" +
								  currentParticle.filename + "')");
					nextID++;
				}
				}
				else if (atomType.equals("EnchiladaDataPoint")) {
					for (int i = 0; i < atomList.size(); i++)
					{
						idArray[i] = nextID;
						EnchiladaDataPoint currentParticle = (EnchiladaDataPoint) atomList.get(i);
						
						stmt.addBatch("INSERT INTO AtomInfo\n" +
							  	  	  "(AtomID,[Time],LaserPower,Size,ScatDelay," +
							  	  	  "OrigFilename)\n" +
									  "VALUES (" + 
									  Integer.toString(nextID) + ", '" + 
									  new Date(0)+ "', '0', '0', '0', '" + 
									  currentParticle.dataPointName + "')");
						nextID++;
					}
				}
				stmt.executeBatch();
				stmt.close();
			} catch (SQLException e){
				System.err.println("Error creating items in AtomInfo table:");
				System.err.println(e);
				return null;
			}
			
		return idArray;
	}
	
	private boolean createPeaks(ArrayList atomList, int[] atomIDs, 
			String atomType)
	{
		if (atomIDs.length != atomList.size())
			return false;
		else
		{
			try {
				Statement stmt = con.createStatement();
				AtomicAnalysisUnit particle = null;
				for (int i = 0; i < atomList.size(); i++)
				{
					if (atomType.equals("ATOFMSParticle")) 
						particle = (ATOFMSParticle) atomList.get(i);
					else if (atomType.equals("EnchiladaDataPoint"))
						particle = (EnchiladaDataPoint) atomList.get(i);
					ArrayList<Peak> peakList = particle.getPeakList();
					
					for (int j = 0; j < peakList.size(); j++)
					{
						Peak peak = peakList.get(j);
						stmt.addBatch("INSERT INTO Peaks\n" +
									  "(AtomID, PeakLocation, " +
									  "PeakArea, RelPeakArea, " +
									  "PeakHeight)\n" +
									  "VALUES (" + 
									  Integer.toString(atomIDs[i]) +
									  ", " + 
									  Double.toString(peak.massToCharge) +
									  ", " + 
									  Integer.toString(peak.area) + ", " +
									  Float.toString(peak.relArea) + ", " +
									  Integer.toString(peak.height) + ")");
					}
				}
				stmt.executeBatch();
				stmt.close();
			} catch (SQLException e) {
				System.err.println("Exception inserting the " +
								   "peaklists");
				System.err.println(e);
				return false;
			}
		}
		return true;
	}
	
	public int getNextID() {
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT COUNT (AtomID)" +
					" FROM AtomInfo");
			
			if (rs.next())
				if (rs.getInt(1) == 0)
					return 0;
			
			rs = stmt.executeQuery("SELECT MAX (AtomID)\n" +
			"FROM AtomInfo");
			
			
			int nextID;
			if(rs.next())
				nextID = rs.getInt(1) + 1;
			else
				nextID = 0;
			stmt.close();
			return nextID;
		} catch (SQLException e) {
			System.err.println("Exception finding max atom id.");
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public int insertATOFMSParticle(ATOFMSParticle particle,
										int collectionID,
										int datasetID, int nextID)
	{
		//int nextID = -1;
		try {
			Statement stmt = con.createStatement();
			//System.out.println("Adding batches");
			String timeSubString = particle.time.substring(0, particle.time.length() - 3);
			
			stmt.addBatch("INSERT INTO AtomInfo\n" +
			  	  	  "(AtomID, [Time], LaserPower, Size, ScatDelay, " +
			  	  	  "OrigFilename)\n" +
					  "VALUES (" + 
					  nextID + ", " +
					  "'" + 
					  timeSubString + 
					  "', " + 
					  (particle.laserPower/(float)1000) + 
					  ", " + 
					  particle.size + ", " +
					  particle.scatDelay + ", '" +
					  particle.filename + "')");
			
			stmt.addBatch("INSERT INTO AtomMembership\n" +
						  "(CollectionID, AtomID)\n" +
						  "VALUES (" +
						  Integer.toString(collectionID) + ", " +
						  Integer.toString(nextID) + ")");
			stmt.addBatch("INSERT INTO OrigDataSets\n" +
						  "(OrigDataSetID, AtomID)\n" +
						  "VALUES (" +
						  Integer.toString(datasetID) + ", " + 
						  Integer.toString(nextID) + ")");

			ArrayList<Peak> peakList = particle.getPeakList();

			String tempFilename = tempdir + "\\bulkfile.txt";
			PrintWriter bulkFile = null;
			try {
				bulkFile = new PrintWriter(new FileWriter(tempFilename));
			} catch (IOException e) {
				System.err.println("Trouble creating " + tempFilename);
				e.printStackTrace();
			}

			for (int j = 0; j < peakList.size(); j++)
			{
				Peak peak = peakList.get(j);
				bulkFile.println(nextID + "," + peak.massToCharge + "," +
						peak.area + "," + peak.relArea + "," + peak.height);
			}

			bulkFile.close();
			stmt.addBatch("BULK INSERT Peaks\n" +
					      "FROM '" + tempFilename + "'\n" +
						  "WITH (FIELDTERMINATOR=',')");
			
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception inserting particle " + 
					particle.filename);
			e.printStackTrace();
			
			return -1;
		}
		return nextID;
	}

	/**
	 * Moves a collection and all its children from one parent to 
	 * another.  If the subcollection was the only child of the parent
	 * containing a particular atom, that atom will be removed from 
	 * the parent, if there are other existing subcollections of the 
	 * parent containing particles also belonging to this collection, 
	 * those particles will then exist both in the current collection and
	 * its parent.  <br><br>
	 * 
	 * To avoid removing particles, use copyCollection instead.
	 * @param collectionID The collection id of the collection to move.
	 * @param toParentID The collection id of the new parent.
	 * @return True on success. 
	 */
	public boolean moveCollection(int collectionID, 
								  int toParentID)
	{
		try { 
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT ParentID\n" +
											 "FROM CollectionRelationships\n" +
											 "WHERE ChildID = " + collectionID);
			rs.next();
			int fromParentID = rs.getInt(1);
			
			
			stmt.executeUpdate("UPDATE CollectionRelationships\n" +
							   "SET ParentID = " + 
							   Integer.toString(toParentID) + "\n" +
							   "WHERE ChildID = " +
							   Integer.toString(collectionID));
			
			stmt.close();
		} catch (SQLException e){
			System.err.println("Error moving collection: ");
			System.err.println(e);
			return false;
		}
		return true;
	}


	/**
	 * orphanAndAdopt() essentially deletes a collection and assigns 
	 * the ownership of all its children (collections and atoms) to 
	 * their grandparent collection.  
	 * @param collectionID The ID of the collection to remove. 
	 * @return true on success.
	 */
	public boolean orphanAndAdopt(int collectionID)
	{
		try {
			Statement stmt = con.createStatement();
			// Figure out who the parent of this collection is
			ResultSet rs = stmt.executeQuery("SELECT ParentID\n" +
					"FROM CollectionRelationships\n" + 
					"WHERE ChildID = " + 
					collectionID);
			// If there is no entry in the table for this collectionID,
			// it doesn't exist, so return false
			if(!rs.next())
			{
				return false;
			}
			// parentID is now set to the parent of the current 
			// collection
			int parentID = rs.getInt("ParentID");
			
			if (parentID == 0)
			{
				System.err.println("Cannot perform this operation " +
						"on root level collections.");
				return false;
			}
			
			// Get rid of the current collection in 
			// CollectionRelationships 
			stmt.execute("DELETE FROM CollectionRelationships\n" + 
					"WHERE ChildID = " + 
					Integer.toString(collectionID));
			
			//This creates a temporary table called #TempParticles
			//containing all the atoms of the parentID which now 
			//no longer contains anything from collectionID or its
			//children
			InstancedResultSet irs = getAllAtomsRS(parentID);
			
			// Find the child collections of this collection and 
			// move them to the parent.  
			ArrayList<Integer> subChildren = 
				getImmediateSubCollections(collectionID);
			for (int i = 0; i < subChildren.size(); i++)
			{
				moveCollection(subChildren.get(i).intValue(),
						parentID);
			}
			
			// Find all the Atoms of this collection and move them to 
			// the parent if they don't already exist there
			stmt.executeUpdate("UPDATE AtomMembership\n" +
					"SET CollectionID = " + parentID +"\n" +
					"WHERE CollectionID = " + collectionID +
					"AND AtomID = ANY \n" +
					"(\n" + 
					" SELECT AtomID\n" +
					" FROM AtomMembership\n" + 
					" WHERE AtomMembership.CollectionID = " + 
					collectionID + "\n" +
					" AND AtomMembership.AtomID <> ALL\n" + 
					" (SELECT AtomID\n" +
					"  FROM  #TempParticles" + irs.instance + ")\n" +
			")");
			// Delete the collection now that everything has been 
			// moved
			recursiveDelete(collectionID);
			// remove the table created by getAllAtomsRS()
			stmt.execute("DROP TABLE " + 
			"#TempParticles" + irs.instance);
			// remove the table created in here
			//stmt.execute("DROP TABLE " +
			//"#TempOldCOllection");
			
			stmt.close();
			
		} catch (SQLException e) {
			System.err.println("Error executing orphan and Adopt");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * Deletes a collection and unlike orphanAndAdopt() also recursively
	 * deletes all direct descendents.
	 * 
	 * TODO: This deletes collectionIDs, not DataSetIDs.  Do we need to 
	 * fix this? 
	 * 
	 * My answer would be no.  -Ben
	 * 
	 * @param collectionID The id of the collection to delete
	 * @return true on success. 
	 */
	public boolean recursiveDelete(int collectionID)
	{
		try {
			rDelete(collectionID);
			//System.out.println("Collection has been deleted.");
		} catch (Exception e){
			System.err.println("Exception deleting collection: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void rDelete(int collectionID) throws SQLException
	{
		Statement stmt = con.createStatement();
		//System.out.println("rDelete() CollectionID = " + collectionID);
		ResultSet rs = stmt.executeQuery("SELECT ChildID\n" + 
						  				 "FROM CollectionRelationships\n" + 
										 "WHERE ParentID = " + 
										 Integer.toString(collectionID));
		int child = 0;
		while (rs.next())
		{
			//System.out.println("About to enter recursion");
			rDelete(rs.getInt("ChildID"));
			//System.out.println("Returning from recursion");
		}
		String sCollectionID = Integer.toString(collectionID);
		stmt.execute("DELETE FROM CollectionRelationships\n" + 
					 "WHERE ParentID = " + 
					 sCollectionID + " " +
					 "OR ChildID = " +
					 sCollectionID);
		stmt.execute("DELETE FROM AtomMembership\n" +
					 "WHERE CollectionID = " + sCollectionID);
		stmt.execute("DELETE FROM Collections\n" +
				 "WHERE CollectionID = " + sCollectionID);
		// Also: We could delete all the particles from the particles
		// table IF we want to by now going through the particles 
		// table and choosing every one that does not exist in the 
		// Atom membership table and deleting it.  However, this would
		// remove particles that were referenced in the OrigDataSets 
		// table.  If we don't want this to happen, comment out the 
		// following code, which also removes all references in the 
		// OrigDataSets table:
		//System.out.println(1);
		stmt.execute("DELETE FROM OrigDataSets\n" +
					 "WHERE AtomID IN\n" +
					 "	(\n" +
					 "	SELECT AtomID\n" +
					 "	FROM OrigDataSets\n" +
					 "	WHERE OrigDataSets.AtomID <> ALL\n" +
					 "		(\n" +
					 "		SELECT AtomID\n" +
					 "		FROM AtomMembership\n" +
					 "		)\n" +
					 "	)\n");

		stmt.execute("DELETE FROM Peaks\n" +
				 "WHERE AtomID IN\n" +
				 "	(\n" +
				 "	SELECT AtomID\n" +
				 "	FROM Peaks\n" +
				 "	WHERE Peaks.AtomID <> ALL\n" +
				 "		(\n" +
				 "		SELECT AtomID\n" +
				 "		FROM AtomMembership\n" +
				 "		)\n" +
				 "	)\n");
		
		stmt.execute("DELETE FROM AtomInfo\n" +
				 "WHERE AtomID IN\n" +
				 "	(\n" +
				 "	SELECT AtomID\n" +
				 "	FROM AtomInfo\n" +
				 "	WHERE AtomInfo.AtomID <> ALL\n" +
				 "		(\n" +
				 "		SELECT AtomID\n" +
				 "		FROM AtomMembership\n" +
				 "		)\n" +
				 "	)\n");
		stmt.close();
	}
	
	public ArrayList<Integer> getImmediateSubCollections(int collectionID)
	{
		ArrayList<Integer> subChildren = new ArrayList<Integer>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ChildID\n" +
										  "FROM CollectionRelationships\n" +
										  "WHERE ParentID = " +
										  Integer.toString(collectionID));
			while(rs.next())
			{
				subChildren.add(new Integer(rs.getInt("ChildID")));
			}
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception grabbing subchildren:");
			System.err.println(e);
		}
		return subChildren;
	}

	public ArrayList<Integer> getImmediateSubCollections(
	        ArrayList<Integer> collections)
	{
		ArrayList<Integer> subChildren = new ArrayList<Integer>();

		// Assume that each collectionID will need 10 characters
		// (with comma). Add 500 characters for rest of query. Probably
		// overkill, but faster than regenerating the string every time
		// by appending.
		StringBuilder queryString =
		    new StringBuilder(collections.size()*10+500);
		queryString.append(
		        "SELECT DISTINCT ChildId\n" +
		        "FROM CollectionRelationships\n" +
		        "WHERE ParentID IN ("
		);
		
		for (Integer collection : collections)
		    queryString.append(collection + ",");
		queryString.deleteCharAt(queryString.length()-1);
		
		queryString.append(")");

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(queryString.toString());
			while(rs.next())
			{
				subChildren.add(new Integer(rs.getInt("ChildID")));
			}
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception grabbing subchildren:");
			System.err.println(e);
		}
		return subChildren;
	}
	
	public String getCollectionName(int collectionID) {
		String name = "";
		try {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT Name\n" +
									"FROM Collections\n" +
									"WHERE CollectionID = " +
									collectionID);
		rs.next();
		name = rs.getString("Name");
		} catch (SQLException e) {
			System.err.println("Exception grabbing the collection name:");
			System.err.println(e);
		}
		return name;
	}
	
	public String getCollectionComment(int collectionID) {
		String comment = "";
		try {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT Comment\n" +
									"FROM Collections\n" +
									"WHERE CollectionID = " +
									collectionID);
		rs.next();
		comment = rs.getString("Comment");
		} catch (SQLException e) {
			System.err.println("Exception grabbing the collection comment:");
			System.err.println(e);
		}
		return comment;
	}
	
	public int getCollectionSize(int collection) {
		int returnThis = -1;
		InstancedResultSet irs = getAllAtomsRS(collection);
		
		try {
			ResultSet rs = con.createStatement().executeQuery(
					"SELECT COUNT(AtomID) FROM #TempParticles" + 
					irs.instance);
			rs.next();
			returnThis = rs.getInt(1);
		} catch (SQLException e1) {
			System.err.println("Error selecting the size of " +
					"the table");
			e1.printStackTrace();
		}
		
		try {
			con.createStatement().execute("DROP TABLE " +
					"#TempParticles" +
					irs.instance);
		} catch (SQLException e) {
			System.err.println("Error dropping temporary table" +
			"#TempParticles" + irs.instance);
			e.printStackTrace();
		}
		return returnThis;
	}
	
	public ArrayList<Integer> getAllDescendedAtoms(
			int collectionID)
	{
		InstancedResultSet irs = getAllAtomsRS(collectionID);
		ResultSet rs = irs.rs;
		int thisInstance = irs.instance;
		ArrayList<Integer> results = new ArrayList<Integer>(1000);
		try {
			while(rs.next())
			{
				results.add(new Integer(rs.getInt(1)));
			}
			con.createStatement().execute("DROP TABLE " + 
										  "#TempParticles" +
										  thisInstance);
			} catch (SQLException e) {
				System.err.println("Error retrieving children.");
				e.printStackTrace();
			}
		return results;
	}

	private InstancedResultSet getAllAtomsRS(int collectionID)
	{
		Statement stmt = null;
		try {

		    // Construct a set of all collections that descend from this one,
		    // including this one.
		    ArrayList<Integer> lookUpNext = new ArrayList<Integer>();
		    boolean status = lookUpNext.add(new Integer(collectionID));
		    assert status : "lookUpNext queue full";
		    Set<Integer> descCollections = new HashSet<Integer>();
		    descCollections.add(new Integer(collectionID));
		    
		    // As long as there is at least one collection to lookup, find
		    // all subchildren for all of these collections. Add them to the
		    // set of all collections we have visited and plan to visit
		    // then next time (if we haven't). (This is essentially a breadth
		    // first search on the graph of collection relationships).
		    while (!lookUpNext.isEmpty()) {
		        ArrayList<Integer> subChildren =
		            getImmediateSubCollections(lookUpNext);
		        lookUpNext.clear();
		        for (Integer collection : subChildren)
		            if (!descCollections.contains(collection)) {
		                descCollections.add(collection);
		                lookUpNext.add(collection);
		            }
		    }
		    
		    // Now that we have all the collectionIDs that descend from the one
		    // given, need to query database for all atomIDs that connect to
		    // these collectionIDs (and eliminate dups). 
		    stmt = con.createStatement();
			
			// #TableName = A temporary table visible only to
			// the user who created it, in the session they
			// created it.			
			stmt.execute("IF exists (SELECT * FROM sysobjects WHERE " +
						 "[name] = '#TempParticles" + instance + 
						 "')\n" +
						 "BEGIN\n" +
						 "	DROP TABLE #TempParticles" + instance + 
						 "\n" +
						 "END\n");

			stmt.execute("CREATE TABLE #TempParticles" + instance + 
						 " (AtomID INT PRIMARY KEY CLUSTERED)\n");
			
			// Assume that each collectionID will need 10 characters
			// (with comma). Add 500 characters for rest of query. Probably
			// overkill, but faster than regenerating the string every time
			// by appending.
			StringBuilder queryString =
			    new StringBuilder(descCollections.size()*10+500);
			queryString.append(
			    "INSERT INTO #TempParticles" + instance + " (AtomID)\n" +
			    "(SELECT DISTINCT AtomID \n" +
			    " FROM AtomMembership\n" +
			    " WHERE CollectionID IN ("
			);
		
			for (Integer collection : descCollections)
			    queryString.append(collection + ",");
			queryString.deleteCharAt(queryString.length()-1);
			
		    queryString.append("))");
		    
		    stmt.executeUpdate(queryString.toString());

		} catch (SQLException e) {
			System.err.println("Could not create a temporary table");
			System.err.println("and therefore could not complete ");
			System.err.println("retrieval.");
			e.printStackTrace();
		}

		
		InstancedResultSet returnThis = null;
		try {
			returnThis = new InstancedResultSet(
					stmt.executeQuery(
							"SELECT AtomID\n" +
							"FROM #TempParticles" + instance +
							"\nORDER BY AtomID" ), instance);
		} catch (SQLException e1) {
			System.err.println("Could not retrieve atoms from temporary table. ");
			e1.printStackTrace();
		}
		instance++;
		return returnThis;

	}

	public ArrayList<ATOFMSParticleInfo> getCollectionParticles(int collectionID)
	{
		ArrayList<ATOFMSParticleInfo> particleInfo = 
			new ArrayList<ATOFMSParticleInfo>(1000);
		ATOFMSParticleInfo temp = null;
		try {
			InstancedResultSet irs = getAllAtomsRS(collectionID);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT AtomInfo.AtomID, OrigFilename, Size," +
					" [Time]\n" +
					"FROM AtomInfo, #TempParticles" + irs.instance 
					+"\n" +
					"WHERE AtomInfo.AtomID = #TempParticles" + 
					irs.instance + ".AtomID\n" +
					"ORDER BY #TempParticles" + irs.instance + 
					".AtomID");
			
			DateFormat dFormat = 
				new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			
			while(rs.next())
			{
				temp = new ATOFMSParticleInfo();
				
				temp.setAtomID(rs.getInt(1));
				temp.setFilename(rs.getString(2));
				temp.setSize(rs.getFloat(3));
				temp.setDateString(dFormat.format(
						rs.getTimestamp(4)));
				particleInfo.add(temp);
			}
			stmt.execute("DROP TABLE " + 
			  	"#TempParticles" + irs.instance);
		} catch (SQLException e) {
			System.err.println("Error collecting particle " +
					"information:");
			e.printStackTrace();
		}
		return particleInfo;
	}
	
//	TODO: Leah's making unit tests for the methods above this line; 
//	 Anna's making tests for the methods below this line.
	
	public java.util.Date exportToMSAnalyzeDatabase(
			int collectionID, 
			String newName, 
			String sOdbcConnection) 
	{
		DateFormat dFormat = null;
		Date startTime = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		} catch (ClassNotFoundException e) {
			System.err.println("Error loading ODBC " +
					"bridge driver");
			e.printStackTrace();
			return null;
		}
		try {
			Connection odbcCon = DriverManager.getConnection(
					"jdbc:odbc:" + sOdbcConnection);
			System.out.println("jdbc:odbc:" +sOdbcConnection);
			
			Statement odbcStmt = odbcCon.createStatement();
			Statement stmt = con.createStatement();
			
			ResultSet rs = null;// = stmt.executeQuery(
			
			InstancedResultSet irs = getAllAtomsRS(collectionID);
			
			// Create a table containing the values that will be 
			// exported to the particles table in MS-Analyze
			stmt.execute(
					"IF (OBJECT_ID('#ParticlesToExport') " +
					"IS NOT NULL)\n" +
					"	DROP TABLE #ParticlesToExport\n" +
					"CREATE TABLE #ParticlesToExport (AtomID INT " +
					"PRIMARY KEY, Filename TEXT, [Time] DATETIME, [Size] FLOAT, " +
					"LaserPower FLOAT, NumPeaks INT, TotalPosIntegral INT, " +
					"TotalNegIntegral INT)\n" +
					
					"INSERT INTO #ParticlesToExport\n" +
					"(AtomID,Filename, [Time], [Size], LaserPower)\n" +
					"(\n" +
					"	SELECT AtomInfo.AtomID, OrigFilename, [Time], [Size], LaserPower\n" +
					"	FROM AtomInfo, #TempParticles" + irs.instance 
					+ "\n" +
					"	WHERE AtomInfo.AtomID = #TempParticles" + 
					irs.instance + ".AtomID\n" +
					")\n" +
					
					"UPDATE #ParticlesToExport\n" +
					"SET NumPeaks = \n" +
					"	(SELECT COUNT(AtomID)\n" +
					"		FROM Peaks\n" +
					"			WHERE Peaks.AtomID = #ParticlesToExport.AtomID),\n" +
					"TotalPosIntegral = \n" +
					"	(SELECT SUM (PeakArea)\n" +
					"		FROM Peaks\n" +
					"			WHERE Peaks.AtomID = #ParticlesToExport.AtomID\n" +
					"			AND Peaks.PeakLocation >= 0),\n" +
					"TotalNegIntegral =\n" +
					"	(SELECT SUM (PeakArea)\n" +
					"		FROM Peaks\n" +
					"			WHERE Peaks.AtomID = #ParticlesToExport.AtomID\n" +
					"			AND Peaks.PeakLocation < 0)\n"
			);
			
			// Find the start time of our mock dataset, use this
			// as the timestamp for the dataset since this might
			// potentially be a collection that wasn't originally
			// an MSA dataset and is rather some strange 
			// amalgam of them, or a selection from one.  This 
			// means that if you simply import a dataset and then
			// export it right away, its timestamp will not 
			// necessarily match the same dataset peaklisted in
			// MSA since the timestamp of an MSA dataset is from 
			// the start time listed in the .par file which often
			// is a little earlier, and which most likely 
			// represents the time they switched on the 
			// ATOFMS machine in MS-Control.
			// TODO: Use rGetAllDescended in order to get the real first
			// atom
			
			rs = stmt.executeQuery(
					"SELECT MIN ([Time])\n" +
					"FROM #ParticlesToExport"
					/*"SELECT MIN ([Time])\n" +
					"FROM AtomInfo\n" +
					"WHERE AtomID = ANY\n" +
					"(\n" +
					"	SELECT AtomID\n" +
					"	FROM AtomMembership\n" +
					"	WHERE CollectionID = " + collectionID + "\n" +
					")"*/);
			Date endTime;
			startTime = endTime = null;
			long unixTime;

			//rs.next();
			//System.out.println(rs.getString(0));
			if (rs.next())
			{
				startTime = new Date(rs.getTimestamp(1).getTime());
				//startTime = startTime.substring(0, startTime.length()-2);
				unixTime = startTime.getTime() / 1000;
			}
			else
			{
				unixTime = 0;
				//endTime = "";
			}
			
			// find the end time in the same manner
			rs = stmt.executeQuery(
					"SELECT MAX ([Time])\n" +
					"FROM #ParticlesToExport\n"
					/*"SELECT MAX ([Time])\n" +
					"FROM AtomInfo JOIN AtomMembership\n" +
					"	ON (AtomInfo.AtomID = AtomMembership.AtomID)" +
					"WHERE AtomMembership.CollectionID = " + 
					collectionID + "\n"*/);
			if (rs.next())
			{
				endTime = new Date(rs.getTimestamp(1).getTime());
				//endTime = endTime.substring(0, startTime.length()-2);
			}
			
			
			String comment = "";
			
			// Get the comment for the current collection to use
			// as the comment for the dataset
			rs = stmt.executeQuery(
					"SELECT Comment \n" +
					"FROM Collections\n" +
					"WHERE CollectionID = " + collectionID);
			if (rs.next())
				comment = rs.getString(1);
			
			int hitParticles = 0;
			
			// find out how many particles are in the collection
			// and pretend like that is the number of particles
			// hit in a powercycle.  
			rs = stmt.executeQuery("SELECT COUNT(AtomID) from #ParticlesToExport");
			
			if (rs.next())
				hitParticles = rs.getInt(1);
			
			newName = newName.concat(Long.toString(unixTime));
			
			odbcStmt = odbcCon.createStatement();
			
			// Make an entry for this collection in the datasets
			// table.  Since the particles from this dataset
			// might have been peaklisted separately, enter zeroes
			// for those values, and for the missed particles.
			odbcStmt.executeUpdate(
					"DELETE FROM DataSets\n" +
					"WHERE Name = '" + newName + "'\n");
			System.out.println(
					"INSERT INTO DataSets\n" +
					"VALUES ('" + newName + "', '" + 
					(startTime.getTime() / 1000) + 
					"', '" + (endTime.getTime() / 1000) + 
					"', " + hitParticles + ", 0, 0, " +
					"0, 0, '" + comment + "')");
			dFormat = DateFormat.getDateTimeInstance();
			odbcStmt.executeUpdate(
					"INSERT INTO DataSets\n" +
					"VALUES ('" + newName + "', '" + 
					dFormat.format(startTime) + 
					"', '" + dFormat.format(endTime) + "', " +
					hitParticles + ", 0, 0, " +
					"0, 0, '" + comment + "')");
			

			// get the values for the particles tabel
			//so we can export them to MS Access
			rs = stmt.executeQuery(
					"SELECT * \n" +
					"FROM #ParticlesToExport\n");
			odbcStmt.execute(
					"DELETE FROM Particles\n" +
					"WHERE DataSet = '" + newName + "'\n");
			while(rs.next())
			{
				odbcStmt.addBatch(
						"INSERT INTO Particles\n" +
						"(DataSet, Filename, [Time], Size, " +
						"LaserPower, " +
						"NumPeaks,TotalPosIntegral, " +
						"TotalNegIntegral)\n" +
						"VALUES ('" + newName +"', '" + 
						rs.getString("Filename") +
						"', '" + dFormat.format(new Date(
								rs.getTimestamp("Time").
								getTime())) + 
								"', " + 
						rs.getFloat("Size") + ", " + 
						rs.getFloat("LaserPower") +
						", " + rs.getInt("NumPeaks") + ", " + 
						rs.getInt("TotalPosIntegral") + ", " + 
						rs.getInt("TotalNegIntegral") +
						")");
			}
			odbcStmt.executeBatch();
			stmt.executeUpdate(
					"DROP TABLE #ParticlesToExport");
			stmt.executeUpdate(
					"IF (OBJECT_ID('#PeaksToExport') " +
					"IS NOT NULL)\n" +
					"	DROP TABLE #PeaksToExport\n" +
					"CREATE TABLE #PeaksToExport\n" +
					"(OrigFilename TEXT, " +
					"PeakLocation FLOAT, PeakArea INT, " +
					"RelPeakArea " +
					"FLOAT, PeakHeight INT)\n" +
					"\n" +
					"" +
					"" +
					"INSERT INTO #PeaksToExport\n" +
					"(OrigFilename, PeakLocation, PeakArea, " +
					"RelPeakArea, PeakHeight)\n" +
					"(SELECT OrigFilename, PeakLocation, " +
					"PeakArea, RelPeakArea, PeakHeight\n" +
					"FROM Peaks, #TempParticles" + 
					irs.instance + 
					", AtomInfo\n" +
					"	WHERE (Peaks.AtomID = " +
					"#TempParticles" + 
					irs.instance + ".AtomID)\n" +
					"		AND (Peaks.AtomID = " +
					"AtomInfo.AtomID)" +
					")\n" +
					"DROP TABLE #TempParticles" + 
					irs.instance);
			
			rs = stmt.executeQuery(
					"SELECT OrigFilename, PeakLocation, " +
					"PeakArea, " +
					"RelPeakArea, PeakHeight\n" +
					"FROM #PeaksToExport");
			odbcStmt.executeUpdate(
					"DELETE FROM Peaks\n" +
					"WHERE DataSet = '" + newName + "'");
			while (rs.next())
			{
				odbcStmt.addBatch(
						"INSERT INTO Peaks\n" +
						"(DataSet, Filename, MassToCharge, " +
						"PeakArea, " +
						"RelPeakArea, PeakHeight)" +
						"VALUES\n" +
						"(\n" +
						"	'" + newName + "', '" + 
						rs.getString(1) + "', " +
						rs.getFloat(2) + ", " + rs.getInt(3) + 
						", " +
						rs.getFloat(4) + ", " + rs.getInt(5) +
						")");
			}
			
			odbcStmt.executeBatch();
			stmt.execute("DROP TABLE #PeaksToExport");
			odbcCon.close();
		} catch (SQLException e) {
			System.err.println("SQL error exporting to " +
					"Access database:");
			e.printStackTrace();
			return null;
		}
		return startTime;
	}
	
	
	private class ParticleInfoOnlyCursor 
	implements CollectionCursor {
		protected InstancedResultSet irs;
		protected ResultSet partInfRS = null;
		protected Statement stmt = null;
		/**
		 * Set up a ResultSet for this iterator
		 */
		// TODO: I put the ORDER BY command back in constructor and reset method - anna.
		
		public ParticleInfoOnlyCursor(int collectionID) {
			super();
			try {
				stmt = con.createStatement();
				irs = getAllAtomsRS(collectionID);
				partInfRS = stmt.executeQuery(
						"SELECT AtomInfo.AtomID, " +
						"OrigFilename, ScatDelay, LaserPower, " +
						"[Time]\n" +
						"FROM AtomInfo, #TempParticles" + 
						irs.instance +
						"\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = AtomInfo.AtomID\n" +
						"ORDER BY #TempParticles" + 
						irs.instance + 
						".AtomID");
			} catch (SQLException e) {
				System.err.println("Error initializing a " +
						"resultset " +
						"for that collection:");
				e.printStackTrace();
			}
			
		}
		
		public void reset()
		{		
			try {
				partInfRS.close();
				partInfRS = stmt.executeQuery(
						"SELECT AtomInfo.AtomID, " +
						"OrigFilename, ScatDelay, LaserPower, [Time]\n" +
						"FROM AtomInfo, #TempParticles" + 
						irs.instance + 
						"\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = AtomInfo.AtomID\n" +
						"ORDER BY #TempParticles" + 
						irs.instance + 
						".AtomID");
			} catch (SQLException e) {
				System.err.println("SQL Error resetting " +
						"cursor: ");
				e.printStackTrace();
			}
		}

		public boolean next() {
			try {
				return partInfRS.next();
			} catch (SQLException e) {
				System.err.println("Error checking the " +
						"bounds of " +
						"the ResultSet.");
				e.printStackTrace();
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public ParticleInfo getCurrent() {
			try {
				ParticleInfo particleInfo = 
					new ParticleInfo();
				particleInfo.setParticleInfo(
						new ATOFMSParticleInfo(
						partInfRS.getInt(1),
						partInfRS.getString(2),
						partInfRS.getInt(3),
						partInfRS.getFloat(4), 
						new Date(partInfRS.getTimestamp(5).
								getTime())));
				particleInfo.setID(
						particleInfo.getParticleInfo().
						getAtomID());
				return particleInfo; 
			} catch (SQLException e) {
				System.err.println("Error retrieving the " +
						"next row");
				e.printStackTrace();
				return null;
			}
		}

		public void close() {
			try {
				stmt.close();
				partInfRS.close();
				con.createStatement().executeUpdate(
						"DROP Table #TempParticles" + 
						irs.instance);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		public ParticleInfo get(int i) 
		throws NoSuchMethodException {
			throw new NoSuchMethodException(
					"Not implemented in disk based cursors.");
		}
		
		public BinnedPeakList 
		getPeakListfromAtomID(int atomID) {
			BinnedPeakList peakList = new BinnedPeakList();
			try {
				ResultSet rs = 
					con.createStatement().executeQuery(
							"SELECT PeakLocation,PeakArea\n" +
							"FROM Peaks\n" +
							"WHERE AtomID = " + atomID);
				while(rs.next()) {
					peakList.add(
							rs.getFloat(1),
							rs.getInt(2));
				}
				rs.close();
				return peakList;
			} catch (SQLException e) {
				System.err.println("Error retrieving peak " +
						"list.");
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private class SQLCursor extends ParticleInfoOnlyCursor
	{
		private Statement stmt;
		private String where;
		/**
		 * @param collectionID
		 */
		public SQLCursor(int collectionID, String where) {
			super(collectionID);

			this.where = where;
			InstancedResultSet irs = getAllAtomsRS(
					collectionID);
			try {
				stmt = con.createStatement();
			
				partInfRS = stmt.executeQuery(
						"SELECT AtomInfo.AtomID, " +
						"OrigFilename, ScatDelay, " +
						"LaserPower, " +
						"[Time]\n" +
						"FROM AtomInfo, #TempParticles" + 
						irs.instance + "\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = AtomInfo.AtomID " +
						"AND " + where);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		public void close() {
			try {
				con.createStatement().executeUpdate(
				"DROP Table #TempParticles" + irs.instance);
				super.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		public void reset()
		{
			
			try {
				partInfRS.close();
				partInfRS = stmt.executeQuery(
						"SELECT AtomInfo.AtomID, " +
						"OrigFilename, ScatDelay, " +
						"LaserPower, " +
						"[Time]\n" +
						"FROM AtomInfo, #TempParticles" + 
						irs.instance + "\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = AtomInfo.AtomID " +
						"AND " + where);
			} catch (SQLException e) {
				System.err.println("SQL Error resetting cursor: ");
				e.printStackTrace();
			}
		}
		
	}
	
	private class PeakCursor extends ParticleInfoOnlyCursor
	{	
		protected Statement stmt = null;
		protected ResultSet peakRS = null;
		
		public PeakCursor(int collectionID)
		{
			super (collectionID);
			try {
				stmt = con.createStatement();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		public ParticleInfo getCurrent()
		{
			// This should get overridden in other classes,
			//however, its results from here should be used.
			
			ParticleInfo pInfo = super.getCurrent();
			PeakList pList = new PeakList();
			ArrayList<Peak> aPeakList = new ArrayList<Peak>();
			pList.setAtomID(pInfo.getParticleInfo().getAtomID());
			
			try {
				peakRS = stmt.executeQuery("SELECT PeakHeight, PeakArea, " +
						"RelPeakArea, PeakLocation\n" +
						"FROM Peaks\n" +
						"WHERE AtomID = " + pList.getAtomID());
				while (peakRS.next())
				{
					aPeakList.add(new Peak(peakRS.getInt(1), peakRS.getInt(2), peakRS.getFloat(3),
							peakRS.getFloat(4)));
				}
				pList.setPeakList(aPeakList);
				pInfo.setPeakList(pList);
				peakRS.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return pInfo;
		}
		
		public void close(){
			try {
				peakRS.close();
				super.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class BinnedCursor extends PeakCursor {

		/**
		 * @param collectionID
		 */
		public BinnedCursor(int collectionID) {
			super(collectionID);
		}
		
		public ParticleInfo getCurrent()
		{
			ParticleInfo sPInfo = super.getCurrent();
			
			sPInfo.setBinnedList(bin(sPInfo.getPeakList().getPeakList()));
			return sPInfo;
		}
		
		private BinnedPeakList bin(ArrayList<Peak> peakList)
		{
			BinnedPeakList bPList = new BinnedPeakList();
			
			Peak temp;
			
			for(int i = 0; i < peakList.size(); i++)
			{
				temp = peakList.get(i);
				bPList.add((float)temp.massToCharge, temp.area);
			}
			return bPList;
		}
	}
	

	/**
	 * 
	 * @author ritza
	 *
	 * NOTE:  Randomization cursor info found at 
	 * http://www.sqlteam.com/item.asp?ItemID=217
	 */
	private class RandomizedCursor extends BinnedCursor {
		//private ResultSet rs = null;
		//private InstancedResultSet irs;
		protected Statement stmt = null;
		/**
		 * @param collectionID
		 */
		public RandomizedCursor(int collectionID) {
			super(collectionID);
			//Statement stmt = null;
			try {
				stmt = con.createStatement();
				
				// Drop table if it already exists.
				stmt.execute("IF (OBJECT_ID('#TempRand') " +
						 "IS NOT NULL)\n" +
						 "	DROP TABLE #TempRand\n");
				//These strings constitute the entire SQL query for randomization.
				String createTable = "CREATE TABLE " +
						"#TempRand (AtomID int NOT NULL," +
				"RandNum float NULL)";
				String insertAtoms = "INSERT #TempRand (AtomID) SELECT AtomID " 
					+ "FROM #TempParticles" + irs.instance
					+ " ORDER BY #TempParticles" + irs.instance + ".AtomID";
				String randomize = "DECLARE @rand_holder float\n" +
				"DECLARE Randomizer CURSOR FOR " +
				"SELECT RandNum FROM #TempRand\n" +
				"OPEN Randomizer FETCH NEXT FROM Randomizer " +
				"INTO @rand_holder\n" +
				"WHILE @@Fetch_Status != -1\n" +
				"BEGIN \n" +
				"UPDATE #TempRand SET RandNum = RAND() \n" +
				"WHERE CURRENT OF Randomizer\n" +
				"FETCH NEXT FROM Randomizer \n" +
				"INTO @rand_holder\n" +
				"END\n" +
				"CLOSE Randomizer\n" +
				"DEALLOCATE Randomizer\n";
				String cursorQuery = "SELECT AtomInfo.AtomID, OrigFilename, ScatDelay, LaserPower, [Time]\n" +
				"FROM #TempRand, AtomInfo " +
				"WHERE AtomInfo.AtomID = #TempRand.AtomID\n" +
				"ORDER BY RandNum";
				//System.out.println(createTable);
				stmt.execute(createTable);
				//System.out.println(insertAtoms);
				stmt.execute(insertAtoms);
				//System.out.println(randomize);
				stmt.execute(randomize);
				//System.out.println(cursorQuery);
				partInfRS = stmt.executeQuery(cursorQuery);
				//System.out.println("got result set");
			} catch (SQLException e) {
				System.err.println("Could not randomize atoms.");
				e.printStackTrace();
			}
		}
		public void close() {
			try {
				con.createStatement().executeUpdate(
				"DROP Table #TempRand");
				super.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		public void reset()
		{
			
			try {
				partInfRS.close();
				String cursorQuery = "SELECT AtomInfo.AtomID, OrigFilename, " +
						"ScatDelay, LaserPower, [Time]\n" +
				"FROM #TempRand, AtomInfo " +
				"WHERE AtomInfo.AtomID = #TempRand.AtomID\n" +
				"ORDER BY RandNum";
				partInfRS = stmt.executeQuery(cursorQuery);
			} catch (SQLException e) {
				System.err.println("SQL Error resetting cursor: ");
				e.printStackTrace();
			}
		}
	}


	private class MemoryBinnedCursor extends BinnedCursor {
		InfoWarehouse db;
		boolean firstPass = true;
		int position = -1;
		
		ArrayList<ParticleInfo> storedInfo = null;
		
		public MemoryBinnedCursor(int collectionID) {
			super (collectionID);
			storedInfo = new ArrayList<ParticleInfo>(100);
		}
		
		public void reset()
		{
			if (firstPass) {
				storedInfo.clear();
				super.reset();
			}
			position = -1;
		}
		
		public boolean next()
		{
			position++;
			if (firstPass)
			{
				boolean superNext = super.next();
				if (superNext)
					storedInfo.add(super.getCurrent());
				else
				    firstPass = false;
				return superNext;
			}
			else
				return (position < storedInfo.size());
		}
		
		public ParticleInfo getCurrent()
		{
			return storedInfo.get(position);
		}
		
		public ParticleInfo get(int i)
		{
			if (firstPass)
				if (i < position)
					return storedInfo.get(i);
				else
					return null;
			else
				return storedInfo.get(i);
		}
		
		public BinnedPeakList getPeakListfromAtomID(int atomID) {
			for (ParticleInfo particleInfo : storedInfo) {
				if (particleInfo.getID() == atomID)
					return particleInfo.getBinnedList();
			}
			return new BinnedPeakList();
		}
	}
		
	public CollectionCursor getParticleInfoOnlyCursor(int collectionID)
	{
		return new ParticleInfoOnlyCursor(collectionID);
	}
	
	public CollectionCursor getSQLCursor(int collectionID, 
									     String where)
	{
		return new SQLCursor(collectionID, where);
	}
	
	public CollectionCursor getPeakCursor(int collectionID)
	{
		return new PeakCursor(collectionID);
	}
	
	public CollectionCursor getBinnedCursor(int collectionID)
	{
		return new BinnedCursor(collectionID);
	}
	
	public CollectionCursor getMemoryBinnedCursor(int collectionID)
	{
		return new MemoryBinnedCursor(collectionID);
	}
	
	public CollectionCursor getRandomizedCursor(int collectionID)
	{
		return new RandomizedCursor(collectionID);
	}
	
	public void seedRandom(int seed) {
		try {
			Statement stmt = con.createStatement();
			stmt.execute("SELECT RAND(" + seed + ")\n");
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Error in seeding random number generator.");		
			e.printStackTrace();
		}
	}
	
	/* Used for testing random number seeding */
	public double getNumber() {
	    try {
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery("SELECT RAND()");
	        rs.next();
	        return rs.getDouble(1);
	    } catch (SQLException e) {
	        System.err.println("Error in generating single number.");
	        e.printStackTrace();
	    }
	    return -1;
	}
	
	public boolean moveAtom(int atomID, int fromParentID, int toParentID)
	{
		if (toParentID == 0)
		{
			System.err.println("Cannot move atoms to the root " +
					"collection.");
			return false;
		}

		try {
			Statement stmt = con.createStatement();
			//System.out.println("AtomID: " + atomID + " from: " + 
			//		fromParentID + " to: " + toParentID);
			stmt.executeUpdate(
					"UPDATE AtomMembership\n" +
					"SET CollectionID = " + toParentID + "\n" +
					"WHERE AtomID = " + atomID + " AND CollectionID = " +
					fromParentID);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception updating membership table");
			e.printStackTrace();
		}
		return true;
	}

	public boolean moveAtomBatch(int atomID, int fromParentID, int toParentID)
	{
		if (toParentID == 0)
		{
			System.err.println("Cannot move atoms to the root " +
					"collection.");
			return false;
		}

		try {
			Statement stmt = con.createStatement();
			//System.out.println("AtomID: " + atomID + " from: " + 
			//		fromParentID + " to: " + toParentID);
			stmt.addBatch(
					"UPDATE AtomMembership\n" +
					"SET CollectionID = " + toParentID + "\n" +
					"WHERE AtomID = " + atomID + " AND CollectionID = " +
					fromParentID);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Exception updating membership table");
			e.printStackTrace();
		}
		return true;
	}

	public boolean addAtom(int atomID, int parentID)
	{
		if (parentID == 0)
		{
			System.err.println("Root cannot own any atoms");
			return false;
		}
		
		try {
			con.createStatement().executeUpdate(
					"INSERT INTO AtomMembership \n" +
					"VALUES(" + parentID + ", " + atomID + ")");
		} catch (SQLException e) {
			System.err.println("Exception adding atom to " +
					"AtomMembership table");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// The following set of methods are designed for
	// adding, moving, and deleting atoms in batch.
	
	public void atomBatchInit() {
		try {
			batchStatement = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean addAtomBatch(int atomID, int parentID)
	{
		if (parentID == 0)
		{
			System.err.println("Root cannot own any atoms");
			return false;
		}
		
		try {
			batchStatement.addBatch(
					"INSERT INTO AtomMembership \n" +
					"VALUES(" + parentID + ", " + atomID + ")");
		} catch (SQLException e) {
			System.err.println("Exception adding atom to " +
					"AtomMembership table");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean deleteAtomsBatch(String atomIDs, int collectionID) {
		try {
			batchStatement.addBatch(
					"DELETE FROM AtomMembership \n" +
					"WHERE CollectionID = " + collectionID + "\n" +
					"AND AtomID IN (" + atomIDs + ")");
		} catch (SQLException e) {
			System.err.println("Exception parents from " +
			"parent membership table.");
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	public boolean deleteAtomBatch(int atomID, int collectionID) {
		try {
			batchStatement.addBatch(
					"DELETE FROM AtomMembership \n" +
					"WHERE CollectionID = " + collectionID + "\n" +
					"AND AtomID = " + atomID);
		} catch (SQLException e) {
			System.err.println("Exception adding a batch statement to " +
					"delete atoms from AtomMembership.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void executeBatch() {
		try {
			batchStatement.executeBatch();
			batchStatement.close();
		} catch (SQLException e) {
			System.out.println("Exception executing batch atom adds " +
					"and inserts");
			e.printStackTrace();
		}
	}

	public boolean checkAtomParent(int AtomID, int isMemberOf)
	{
		try {
			ResultSet rs = con.createStatement().executeQuery(
					"Select *\n" +
					"FROM AtomMembership\n" +
					"WHERE AtomID = " + AtomID + 
					" AND CollectionID = " + isMemberOf);
			
			if (rs.next())
			{
				rs.close();
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error checking parentage:");
			e.printStackTrace();
		}
		return false;
	}
	
	// Changed this from private to public.
	public Connection getCon()
	{
		return con;
	}
	
	/**
	 * Replaces characters which would interrupt SQL Server's 
	 * parsing of a string with their escape equivalents
	 * @param s String to modify
	 * @return The same string except in an acceptable string for
	 * SQL Server
	 */
	private String removeReservedCharacters(String s)
	{
		//Replace additional characters as necessary
		s = s.replace("'","''");
		//s = s.replace('"', ' ');
		return s;
	}
	
	public boolean setCollectionDescription(int collectionID,
									String description)
	{
		description = removeReservedCharacters(description);
		try {
			con.createStatement().executeUpdate(
					"UPDATE Collections\n" +
					"SET Description = '" + description + "'\n" +
					"WHERE CollectionID = " + collectionID);
		} catch (SQLException e) {
			System.err.println("Error updating collection " +
					"description:");
			e.printStackTrace();
		}
		return true;
	}
	
	public String getCollectionDescription(int collectionID)
	{
		try {
			ResultSet rs = 
				con.createStatement().executeQuery(
						"SELECT Description\n" +
						"FROM Collections\n" +
						"WHERE CollectionID = " + collectionID);
			rs.next();
			return rs.getString("Description");
		} catch (SQLException e) {
			System.err.println("Error retrieving Collection " +
					"Description.");
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean rebuildDatabase(String dbName) {
		SQLServerDatabase db = null;
		Scanner in = null;
		Connection con = null;

		// Connect to SQL Server independent of a particular database,
		// and drop and add the database.
		// This code works under the assumption that a user called SpASMS has
		// already been created with a password of 'finally'. This user must have
		// already been granted appropriate privileges for adding and dropping
		// databases and tables.
		try {
			db = new SQLServerDatabase();
			db.database = "";
			db.openConnection();
			con = db.getCon();
			Statement stmt = con.createStatement();
			
			// See if database exists. If it does, drop it.
			ResultSet rs = stmt.executeQuery("EXEC sp_helpdb");
			boolean foundDatabase = false;
			while (!foundDatabase && rs.next())
				if (rs.getString(1).equals(dbName))
					foundDatabase = true;	
			if (foundDatabase)
				stmt.executeUpdate("drop database " + dbName);
			
	        stmt.executeUpdate("create database " + dbName);
	        stmt.close();
		} catch (SQLException e) {
			System.err.println("Error in rebuilding SQL Server database.");
			e.printStackTrace();
			return false;
		} finally {
			if (db != null)
				db.closeConnection();
		}

		// Run all the queries in the SQLServerRebuildDatabase.txt file, which
		// inserts all of the necessary tables.
		try {
			db = new SQLServerDatabase();
			db.database = dbName;
			db.openConnection();
			con = db.getCon();

			in = new Scanner(new File("database//SQLServerRebuildDatabase.txt"));
			String query = "";
			StringTokenizer token;
			// loop through license block
			while (in.hasNext()) {
				query = in.nextLine();
				token = new StringTokenizer(query);
				if (token.hasMoreTokens()) {
					String s = token.nextToken();
					if (s.equals("CREATE"))
						break;
				}
			}
			// Update the database according to the stmts.
			con.createStatement().executeUpdate(query);
			
			while (in.hasNext()) {
				query = in.nextLine();
				con.createStatement().executeUpdate(query);
			}
	        
		} catch (IOException e) {
			System.out.println("Error in handling SQLServerDatabaseGenerate.txt.");		
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			System.err.println("Error in adding tables to database.");
			e.printStackTrace();
			return false;
		} finally {
			if (db != null)
				db.closeConnection();
			if (in != null)
				in.close();

		}
		
		return true;
	}
	
	public static boolean dropDatabase(String dbName) {
		SQLServerDatabase db = null;
		Connection con = null;

		// Connect to SQL Server independent of a particular database,
		// and drop and add the database.
		// This code works under the assumption that a user called SpASMS has
		// already been created with a password of 'finally'. This user must have
		// already been granted appropriate privileges for adding and dropping
		// databases and tables.
		try {
			db = new SQLServerDatabase();
			db.database = "";
			db.openConnection();
			con = db.getCon();
			Statement stmt = con.createStatement();
			
			// See if database exists. If it does, drop it.
			ResultSet rs = stmt.executeQuery("EXEC sp_helpdb");
			boolean foundDatabase = false;
			while (!foundDatabase && rs.next())
				if (rs.getString(1).equals(dbName))
					foundDatabase = true;	
			if (foundDatabase)
				stmt.executeUpdate("drop database " + dbName);
			
	        stmt.close();
		} catch (SQLException e) {
			System.err.println("Error in dropping SQL Server database.");
			e.printStackTrace();
			return false;
		} finally {
			if (db != null)
				db.closeConnection();
		}
		return true;
	}		
	
	/* (non-Javadoc)
	 * @see database.InfoWarehouse#getPeaks(int)
	 */
	public ArrayList<Peak> getPeaks(int atomID) 
	{
		ResultSet rs = null;
		try {
			rs = con.createStatement().executeQuery(
				"SELECT * FROM Peaks WHERE AtomID = " +
				atomID);
		} catch (SQLException e) {
			System.err.println("Error selecting peaks");
			e.printStackTrace();
		}
		ArrayList<Peak> returnThis = new ArrayList<Peak>();
		float location = 0;
		int area = 0;
		float relArea = 0;
		int height = 0;
		try {
		while(rs.next())
		{
			location = rs.getFloat(2);
			area = rs.getInt(3);
			relArea = rs.getFloat(4);
			height = rs.getInt(5);
			returnThis.add(new Peak(
					height,
					area,
					relArea,
					location));
		} 
		} catch (SQLException e) {
			System.err.println("Error using the result set");
			e.printStackTrace();
		}
		return returnThis;
	}
	
	// returns the last atomID used.
	public int insertGeneralParticles(ArrayList particles, 
			int collectionID) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		int atomID = getNextID();
		for (int i = 0; i < particles.size(); i++) {
			ids.add(new Integer(atomID));
			atomID++;
		}
		insertAtomicList(particles, collectionID, "EnchiladaDataPoint");
		return atomID-1;
	}
}
