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
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.sql.*;

import ATOFMS.ParticleInfo;
import ATOFMS.Peak;
import analysis.BinnedPeakList;
import analysis.clustering.PeakList;
import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;

import gui.*;

import java.io.*;
import java.util.Scanner;

import collection.Collection;



/**
 * @author andersbe
 *
 */
public class SQLServerDatabase implements InfoWarehouse
{
	/* Class Variables */
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
		
		File f = new File("config.ini");
		try {
			Scanner scan = new Scanner(f);
			while (scan.hasNext()) {
				String tag = scan.next();
				String val = scan.next();
				if (scan.hasNext())
					scan.nextLine();
				
				if (tag.equalsIgnoreCase("db_url:")) { url = val; }
				else if (tag.equalsIgnoreCase("db_port:")) { port = val; }
			}
			scan.close();
		} catch (FileNotFoundException e) { 
			// Don't worry if the file doesn't exist... 
			// just go on with the default values 
		}
	}
	
	public SQLServerDatabase(String dbName) {
		this();
		
		database = dbName;
	}

	/**
	 * Determine if the database is actually present (returns true if it is).
	 */
	public static boolean isPresent(String dbName) {

		boolean foundDatabase = false;
		try {
			SQLServerDatabase db = new SQLServerDatabase("");
			db.openConnection();
			Connection con = db.getCon();
			Statement stmt = con.createStatement();
			
			// See if database exists. If it does, drop it.
			ResultSet rs = stmt.executeQuery("EXEC sp_helpdb");
			while (!foundDatabase && rs.next())
				if (rs.getString(1).equals(dbName))
					foundDatabase = true;
		} catch (SQLException e) {
			new ExceptionDialog(new String[] {"Error in testing if ", dbName,
					" is present."});
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
			new ExceptionDialog("Failed to load current driver.");
			System.err.println("Failed to load current driver.");
			return false;
		} // end catch
		con = null;
		try {
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + url + ":" + port + ";DatabaseName=" + database + ";SelectMethod=cursor;","SpASMS","finally");
		} catch (Exception e) {
			new ExceptionDialog("Failed to establish a connection to SQL Server.");
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
				new ExceptionDialog("Could not close the connection to SQL Server.");
				System.err.println("Could not close the connection: ");
				System.err.println(e);
				return false;
			}
			return true;
		}
		else
			return false;
	}
	
	/* Create Empty Collections */
	
	/**
	 * Creates an empty collection with no atomic analysis units in it.
	 * @param parent	The location to add this collection under (0 
	 * 					to add at the root).
	 * @param name		What to call this collection in the interface.
	 * @param datatype collection's datatype
	 * @param comment	A comment for this collection
	 * @return			The collectionID of the resulting collection
	 */
	public int createEmptyCollection( String datatype,
			int parent, 
			String name, 
			String comment,
			String description)
	{
		
		if (description.length() == 0)
			description = "Name: " + name + " Comment: " + comment;
		
		int nextID = -1;
		try {
			Statement stmt = con.createStatement();
			
			// Get next CollectionID:
			ResultSet rs = stmt.executeQuery("SELECT MAX(CollectionID)\n" +
										"FROM Collections\n");
			rs.next();
			nextID = rs.getInt(1) + 1;
			
			stmt.executeUpdate("INSERT INTO Collections\n" +
							   "(CollectionID, Name, Comment, Description, Datatype)\n" +
							   "VALUES (" +
							   Integer.toString(nextID) + 
							   ", '" + name + "', '" 
							   + comment + "', '" + 
							   description + "', '" + datatype + "')");
			stmt.executeUpdate("INSERT INTO CollectionRelationships\n" +
							   "(ParentID, ChildID)\n" +
							   "VALUES (" + Integer.toString(parent) +
							   ", " + Integer.toString(nextID) + ")");
			
			
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception creating empty collection.");
			System.err.println("Exception creating empty collection:");
			e.printStackTrace();
			return -1;
		}
		return nextID;
	}	
	
	/**
	 * createEmptyCollectionAndDataset is used for the initial 
	 * importation of data.  It creates an empty collection
	 * which can then be filled using insertATOFMSParticle, using the 
	 * return values as parameters.  
	 * @param parent The ID of the parent to insert this collection at
	 * (0 for root)
	 * @param datatype
	 * @param datasetName The name of the dataset, 
	 * @param comment The comment from the dataset
	 * @param params - string of parameters for query
	 * @return int[0] = collectionID, int[1] = datasetID
	 */
	public int[] createEmptyCollectionAndDataset(String datatype, int parent,  
			String datasetName, String comment, String params)
	{
		int[] returnVals = new int[2];
		
		// What do we want to put as the description?
		returnVals[0] = createEmptyCollection(datatype, parent, datasetName, comment, datasetName + ": " + comment);
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT MAX (DataSetID)\n" +
											 "FROM " + getDynamicTableName(DynamicTable.DataSetInfo,datatype));

			if (rs.next())
				returnVals[1] = rs.getInt(1)+1;
			else
				returnVals[1] = 0;
			
			stmt.executeUpdate("INSERT INTO " + getDynamicTableName(DynamicTable.DataSetInfo,datatype) + " VALUES(" + 
							   returnVals[1] + ", " + params + ")");
			
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception creating the new dataset.");
			System.err.println("Exception creating the dataset entries:");
			e.printStackTrace();
		}
		return returnVals;
	}
	
	/**
	 * Create a new collection from an array list of atomIDs which 
	 * have yet to be inserted into the database.  Not used as far as
	 * I can tell.
	 * 
	 * @param parentID	The location of the parent to insert this
	 * 					collection (0 to insert at root level)
	 * @param name		What to call this collection
	 * @param datatype  collection's datatype
	 * @param comment	What to leave as the comment
 	 * @param atomType	The type of atoms you are inserting ("ATOFMSParticle" most likely
	 * @param atomList	An array list of atomID's to insert into the 
	 * 					database
	 * @return			The CollectionID of the new collection, -1 for
	 * 					failure.
	 *//*
	public int createCollectionFromAtoms( String datatype,
			int parentID,
			String name,
			String comment,
			ArrayList<String> atomList)
	{
		int collectionID = createEmptyCollection(datatype,
				parentID, 
													 name,
													 comment,"");
			Collection collection = getCollection(collectionID);
			if (!insertAtomicList(datatype, atomList,collection))
				return -1;
			return collectionID;
	}*/
	
	/* Copy and Move Collections */
	
	/**
	 * Similar to moveCollection, except instead of removing the 
	 * collection and its unique children, the original collection 
	 * remains with original parent and a duplicate with a new id is 
	 * assigned to the new parent.  
	 * @param collectionID The collection id of the collection to move.
	 * @param toParentID The collection id of the new parent.  
	 * @return The collection id of the copy.  
	 */
	public int copyCollection(Collection collection, Collection toCollection)
	{
		int newID = -1;
		try {
			Statement stmt = con.createStatement();
			
			// Get Collection info:
			ResultSet rs = stmt.executeQuery("SELECT Name, Comment, Description\n" +
										"FROM Collections\n" +
										"WHERE CollectionID = " +
										collection.getCollectionID());
			rs.next();
			newID = createEmptyCollection(collection.getDatatype(),
					toCollection.getCollectionID(), 
					rs.getString("Name"),
					rs.getString("Comment"),rs.getString("Description"));
			Collection newCollection = getCollection(newID);
			String description = getCollectionDescription(collection.getCollectionID());
			if (description  != null)
				setCollectionDescription(newCollection, getCollectionDescription(collection.getCollectionID()));

			rs = stmt.executeQuery("SELECT AtomID\n" +
							       "FROM AtomMembership\n" +
								   "WHERE CollectionID = " +
								   collection.getCollectionID());
			while (rs.next())
			{
				stmt.addBatch("INSERT INTO AtomMembership\n" +
						"(CollectionID, AtomID)\n" +
						"VALUES (" + newID + ", " +
						rs.getInt("AtomID") + 
						")");
			}
			stmt.executeBatch();
			
			// Get Children
			rs = stmt.executeQuery("SELECT ChildID\n" +
								   "FROM CollectionRelationships\n" +
								   "WHERE ParentID = " +
								   Integer.toString(collection.getCollectionID()));
			while (rs.next())
			{
				copyCollection(newCollection,getCollection(rs.getInt("ChildID")));
			}
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception copying collection.");
			System.err.println("Exception copying collection: ");
			e.printStackTrace();
			return -1;
		}
		return newID;
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
	public boolean moveCollection(Collection collection, 
								  Collection toCollection)
	{
		try { 
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT ParentID\n" +
											 "FROM CollectionRelationships\n" +
											 "WHERE ChildID = " + collection.getCollectionID());
			rs.next();
			int fromParentID = rs.getInt(1);
			
			
			stmt.executeUpdate("UPDATE CollectionRelationships\n" +
							   "SET ParentID = " + 
							   Integer.toString(toCollection.getCollectionID()) + "\n" +
							   "WHERE ChildID = " +
							   Integer.toString(collection.getCollectionID()));
			
			stmt.close();
		} catch (SQLException e){
			new ExceptionDialog("SQL Exception moving the collection.");
			System.err.println("Error moving collection: ");
			System.err.println(e);
			return false;
		}
		return true;
	}

	/* Insert Atoms */
	
	/**
	 * Inserts a list of AtomicAnalysisUnits to the warehouse.  Intended 
	 * for use on original importation of atoms.
	 * 
	 * @param datatype collection's datatype
	 * @param atomList An ArrayList of AtomicAnalysisUnits which describe
	 * the atoms to add to the warehouse.
	 * @param collectionID The collectionID of the collection to add the
	 * particles to.  
	 * 
	 * @return true on success. 
	 */
	private boolean insertAtomicList(String datatype, 
									ArrayList<String> atomList, 
									Collection collection)
	{
		// first, create entries for the Atoms in the AtomInfo table
		// and the peaklist table
		int[] atomIDs = createAtomInfo(datatype, atomList);	
		if (!createSparseData(datatype, atomList, atomIDs))
			return false;
		// now add atomIDs to the ownership table
		try {
			Statement stmt = con.createStatement();
			for (int i = 0; i < atomIDs.length; i++)
			{
				stmt.addBatch("INSERT INTO AtomMembership\n" +
							  "(CollectionID,AtomID)\n" +
							  "VALUES (" + 
							  Integer.toString(collection.getCollectionID()) + ", " +
							  Integer.toString(atomIDs[i]) + ")");
			}
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception adding particles, please check the incoming data for correct format.");
			System.err.println("Exception adding particle memberships:");
			System.err.println(e);
			return false;
		}
		return true;
	}
	
	/**
	 * createAtomInfo takes an arraylist of atoms and inserts them into
	 * the AtomInfoDense table for the given datatype.
	 * @param datatype
	 * @param atomList
	 * @return array of IDs.
	 */
	private int[] createAtomInfo(String datatype, ArrayList<String> atomList)
	{
		int idArray[] = null;
			try{
				Statement stmt = con.createStatement();
				
				ResultSet rs = stmt.executeQuery("SELECT MAX (AtomID)\n" +
				                               	 "FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,datatype));
				int nextID = -1;
				if(rs.next())
					nextID = rs.getInt(1) + 1;
				else
					nextID = 0;
				
				idArray = new int[atomList.size()];
							
				for (int i = 0; i < atomList.size(); i++)
				{
					idArray[i] = nextID;
					String currentParticle = atomList.get(i);
					
					stmt.addBatch("INSERT INTO " + getDynamicTableName(DynamicTable.AtomInfoDense,datatype) + 
							" VALUES (" + nextID + ", " +
								  currentParticle + ")");
					nextID++;
				}
				stmt.executeBatch();
				stmt.close();
			} catch (SQLException e){
				new ExceptionDialog("SQL Exception creating items in AtomInfoDense table.  Please check incoming data for correct format.");
				System.err.println("Error creating items in AtomInfo table:");
				System.err.println(e);
				return null;
			}
			
		return idArray;
	}
	
	/**
	 * createSparseData takes an arrayList of atoms and their atomIDs and 
	 * inserts each atom's sparse data into the AtomInfoSparse table for 
	 * the given datatype.
	 * @param datatype
	 * @param atomList
	 * @param atomIDs
	 * @return true if successful.
	 */
	private boolean createSparseData(String datatype, 
			ArrayList<String> atomList, int[] atomIDs)
	{
		if (atomIDs.length != atomList.size())
			return false;
		else
		{
			try {
				Statement stmt = con.createStatement();
				String particle;
				for (int i = 0; i < atomList.size(); i++)
				{
					particle = atomList.get(i);
						stmt.addBatch("INSERT INTO " + getDynamicTableName(DynamicTable.AtomInfoSparse,datatype) + " VALUES (" + 
									 particle + ")");
				}
				stmt.executeBatch();
				stmt.close();
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception inserting into AtomInfoSparse.  Please check the data for correct format.");
				System.err.println("Exception inserting the " +
								   "peaklists");
				System.err.println(e);
				return false;
			}
		}
		return true;
	}

	/**
	 * insertParticle takes a string of dense info, a string of sparse info, 
	 * the collection, the datasetID and the nextID and inserts the info 
	 * into the dynamic tables based on the collection's datatype.
	 * @param dense - string of dense info
	 * @param sparse - string of sparse info
	 * @param collection - current collection
	 * @param datasetID - current datasetID
	 * @param nextID - next ID
	 * @return nextID if successful
	 */
	public int insertParticle(String dense, ArrayList<String> sparse,
										Collection collection,
										int datasetID, int nextID)
	{
		try {
			Statement stmt = con.createStatement();
			//System.out.println("Adding batches");
			
			stmt.addBatch("INSERT INTO " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + " VALUES (" + 
					nextID + ", " + dense + ")");
			stmt.addBatch("INSERT INTO AtomMembership\n" +
						  "(CollectionID, AtomID)\n" +
						  "VALUES (" +
						  collection.getCollectionID() + ", " +
						  nextID + ")");
			stmt.addBatch("INSERT INTO DataSetMembers\n" +
						  "(OrigDataSetID, AtomID)\n" +
						  "VALUES (" +
						  datasetID + ", " + 
						  nextID + ")");

			String tableName = getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype());

			// Only bulk insert if client and server are on the same machine...
			if (url.equals("localhost")) {
				String tempFilename = tempdir + File.separator + "bulkfile.txt";
				PrintWriter bulkFile = null;
				try {
					bulkFile = new PrintWriter(new FileWriter(tempFilename));
				} catch (IOException e) {
					System.err.println("Trouble creating " + tempFilename);
					e.printStackTrace();
				}

				for (int j = 0; j < sparse.size(); j++)
					bulkFile.println(nextID + "," + sparse.get(j));
	
				bulkFile.close();
				stmt.addBatch("BULK INSERT " + tableName + "\n" +
						      "FROM '" + tempFilename + "'\n" +
							  "WITH (FIELDTERMINATOR=',')");
			} else {
				for (int j = 0; j < sparse.size(); j++)
					stmt.addBatch("INSERT INTO " + tableName +  
							      " VALUES (" + nextID + "," + sparse.get(j) + ")");
			}
			
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception inserting atom.  Please check incoming data for correct format.");
			System.err.println("Exception inserting particle.");
			e.printStackTrace();
			
			return -1;
		}
		return nextID;
	}
	
	/**
	 * Inserts particles.  Not used yet, but it was here.  
	 * @return the last atomID used.
	 *//*
	public int insertGeneralParticles(String datatype, ArrayList<String> particles, 
			Collection collection) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		int atomID = getNextID();
		for (int i = 0; i < particles.size(); i++) {
			ids.add(new Integer(atomID));
			atomID++;
		}
		insertAtomicList(datatype, particles, collection);
		return atomID-1;
	}*/

	/**
	 * adds an atom to a collection.
	 * @return true if successful
	 */
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
			new ExceptionDialog("SQL Exception adding atom to AtomMembership.");
			System.err.println("Exception adding atom to " +
					"AtomMembership table");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * adds an atom to the batch statement
	 * @return true if successful.
	 */
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
			new ExceptionDialog("SQL Exception adding atom to AtomMembership.");
			System.err.println("Exception adding atom to " +
					"AtomMembership table");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/* Delete Atoms */
	
	/**
	 * orphanAndAdopt() essentially deletes a collection and assigns 
	 * the ownership of all its children (collections and atoms) to 
	 * their grandparent collection.  
	 * @param collectionID The ID of the collection to remove. 
	 * @return true on success.
	 */
	public boolean orphanAndAdopt(Collection collection)
	{
		try {
			Statement stmt = con.createStatement();
			// Figure out who the parent of this collection is
			ResultSet rs = stmt.executeQuery("SELECT ParentID\n" +
					"FROM CollectionRelationships\n" + 
					"WHERE ChildID = " + 
					collection.getCollectionID());
			// If there is no entry in the table for this collectionID,
			// it doesn't exist, so return false
			if(!rs.next())
				return false;
			
			// parentID is now set to the parent of the current 
			// collection
			int parentID = rs.getInt("ParentID");
			
			if (parentID == 0)
			{
				new ExceptionDialog("Cannot perform this operation on root level collections.");
				System.err.println("Cannot perform this operation " +
						"on root level collections.");
				return false;
			}
			// Get rid of the current collection in 
			// CollectionRelationships 
			stmt.execute("DELETE FROM CollectionRelationships\n" + 
					"WHERE ChildID = " + 
					Integer.toString(collection.getCollectionID()));
			
			//This creates a temporary table called #TempParticles
			//containing all the atoms of the parentID which now 
			//no longer contains anything from collectionID or its
			//children
			Collection parentCollection = getCollection(parentID);
			InstancedResultSet irs = getAllAtomsRS(parentCollection);
			
			// Find the child collections of this collection and 
			// move them to the parent.  
			ArrayList<Integer> subChildren = 
				getImmediateSubCollections(collection);
			for (int i = 0; i < subChildren.size(); i++)
			{
				moveCollection(getCollection(subChildren.get(i).intValue()), 
						parentCollection);
			}
			
			// Find all the Atoms of this collection and move them to 
			// the parent if they don't already exist there
			stmt.executeUpdate("UPDATE AtomMembership\n" +
					"SET CollectionID = " + parentID +"\n" +
					"WHERE CollectionID = " + collection.getCollectionID() +
					"AND AtomID = ANY \n" +
					"(\n" + 
					" SELECT AtomID\n" +
					" FROM AtomMembership\n" + 
					" WHERE AtomMembership.CollectionID = " + 
					collection.getCollectionID() + "\n" +
					" AND AtomMembership.AtomID <> ALL\n" + 
					" (SELECT AtomID\n" +
					"  FROM  #TempParticles" + irs.instance + ")\n" +
			")");
			// Delete the collection now that everything has been 
			// moved
			recursiveDelete(collection);
			// remove the table created by getAllAtomsRS()
			stmt.execute("DROP TABLE " + 
			"#TempParticles" + irs.instance);
			// remove the table created in here
			//stmt.execute("DROP TABLE " +
			//"#TempOldCOllection");
			
			stmt.close();
			
		} catch (SQLException e) {
			new ExceptionDialog("Error executing Orphan and Adopt.");
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
	 * @param collectionID The id of the collection to delete
	 * @return true on success. 
	 */
	public boolean recursiveDelete(Collection collection)
	{
		System.out.println(collection.getCollectionID());
		try {
			rDelete(collection, collection.getDatatype());
			//System.out.println("Collection has been deleted.");
		} catch (Exception e){
			new ExceptionDialog("Exception deleting collection.");
			System.err.println("Exception deleting collection: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Actual recursion for deletion, called by recursiveDelete method above.
	 * @param collection
	 * @throws SQLException
	 */
	private void rDelete(Collection collection, String datatype) throws SQLException
	{
		Statement stmt = con.createStatement();
		//System.out.println("rDelete() CollectionID = " + collectionID);
		ResultSet rs = stmt.executeQuery("SELECT ChildID\n" + 
						  				 "FROM CollectionRelationships\n" + 
										 "WHERE ParentID = " + 
										 Integer.toString(collection.getCollectionID()));
		int child = 0;
		while (rs.next())
		{
			//System.out.println("About to enter recursion");
			Collection childCollection = getCollection(rs.getInt("ChildID"));
			rDelete(childCollection, datatype);
			//System.out.println("Returning from recursion");
		}
		String sCollectionID = Integer.toString(collection.getCollectionID());
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
		// remove particles that were referenced in the DataSetMembers 
		// table.  If we don't want this to happen, comment out the 
		// following code, which also removes all references in the 
		// DataSetMembers table:
		//System.out.println(1);
		stmt.execute("DELETE FROM DataSetMembers\n" +
					 "WHERE AtomID IN\n" +
					 "	(\n" +
					 "	SELECT AtomID\n" +
					 "	FROM DataSetMembers\n" +
					 "	WHERE DataSetMembers.AtomID <> ALL\n" +
					 "		(\n" +
					 "		SELECT AtomID\n" +
					 "		FROM AtomMembership\n" +
					 "		)\n" +
					 "	)\n");

		// it is ok to call atominfo tables here because datatype is
		// set from recursiveDelete() above.
		stmt.execute("DELETE FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,datatype) + "\n" +
				 "WHERE AtomID IN\n" +
				 "	(\n" +
				 "	SELECT AtomID\n" +
				 "	FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,datatype) + "\n" +
				 "	WHERE " + getDynamicTableName(DynamicTable.AtomInfoSparse,datatype) + ".AtomID <> ALL\n" +
				 "		(\n" +
				 "		SELECT AtomID\n" +
				 "		FROM AtomMembership\n" +
				 "		)\n" +
				 "	)\n");
		
		stmt.execute("DELETE FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,datatype) +
				 " \n WHERE AtomID IN\n" +
				 "	(\n" +
				 "	SELECT AtomID\n" +
				 "	FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,datatype) +
				 " \n WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,datatype) + ".AtomID <> ALL\n" +
				 "		(\n" +
				 "		SELECT AtomID\n" +
				 "		FROM AtomMembership\n" +
				 "		)\n" +
				 "	)\n");
		stmt.close();
	}
	
	/**
	 * Get the immediate subcollections for the given collection.
	 * @param collection
	 * @return arrayList of atomIDs of subchildren.
	 */
	public ArrayList<Integer> getImmediateSubCollections(Collection collection)
	{
		ArrayList<Integer> subChildren = new ArrayList<Integer>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ChildID\n" +
										  "FROM CollectionRelationships\n" +
										  "WHERE ParentID = " +
										  Integer.toString(collection.getCollectionID()));
			while(rs.next())
			{
				subChildren.add(new Integer(rs.getInt("ChildID")));
			}
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception grabbing subchildren.");
			System.err.println("Exception grabbing subchildren:");
			System.err.println(e);
		}
		return subChildren;
	}

	/**
	 * puts an atom-delete call in the atom batch for each atomID in string.
	 * @return true if successful. 
	 */
	public boolean deleteAtomsBatch(String atomIDs, Collection collection) {
		try {
			batchStatement.addBatch(
					"DELETE FROM AtomMembership \n" +
					"WHERE CollectionID = " + collection.getCollectionID() + "\n" +
					"AND AtomID IN (" + atomIDs + ")");
		} catch (SQLException e) {
			new ExceptionDialog(new String[] {"SQL Exception deleting atoms.", 
					atomIDs});
			System.err.println("Exception parents from " +
			"parent membership table.");
			e.printStackTrace();
			return false;
		}
		return true;
		
	}

	/**
	 * puts an atom-delete call in the atom batch
	 * @return true if successful.
	 */	
	public boolean deleteAtomBatch(int atomID, Collection collection) {
		try {
			batchStatement.addBatch(
					"DELETE FROM AtomMembership \n" +
					"WHERE CollectionID = " + collection.getCollectionID() + "\n" +
					"AND AtomID = " + atomID);
		} catch (SQLException e) {
			new ExceptionDialog(new String[]{"SQL Exception deleting atom ",
					Integer.toString(atomID)});
			System.err.println("Exception adding a batch statement to " +
					"delete atoms from AtomMembership.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/* Move Atoms */
	
	/**
	 * moves an atom from one collection to another.
	 * @return true if successful
	 */
	public boolean moveAtom(int atomID, int fromParentID, int toParentID)
	{
		if (toParentID == 0)
		{
			new ExceptionDialog("Cannot move atoms to the root collection.");
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
			new ExceptionDialog("SQL Exception updating AtomMembership table.");
			System.err.println("Exception updating membership table");
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * adds a move-atom call to a batch statement.
	 * @return true if successful
	 */
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
			new ExceptionDialog("SQL Exception updating AtomMembership table.");
			System.err.println("Exception updating membership table");
			e.printStackTrace();
		}
		return true;
	}

	/* Atom Batch Init and Execute */
	
	/**
	 * initializes atom batches for moving atoms and adding atoms.
	 */
	public void atomBatchInit() {
		try {
			batchStatement = con.createStatement();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception occurred.");
			e.printStackTrace();
		}
	}

	/**
	 * Executes the current batch
	 */
	public void executeBatch() {
		try {
			batchStatement.executeBatch();
			batchStatement.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception executing batch atom adds and inserts.");
			System.out.println("Exception executing batch atom adds " +
					"and inserts");
			e.printStackTrace();
		}
	}

	/* Get functions for collections and table names */
	
	/**
	 * Gets immediate subcollections for a given collection
	 * @param collections
	 * @return arraylist of atomIDs
	 */
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
		        "SELECT DISTINCT ChildID\n" +
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
			new ExceptionDialog("SQL Exception grabbing subchildren.");
			System.err.println("Exception grabbing subchildren:");
			System.err.println(e);
		}
		return subChildren;
	}
	
	/**
	 * returns a collection given a collectionID.
	 */
	public Collection getCollection(int collectionID) {
		boolean isPresent = false;
		String datatype = "";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT CollectionID FROM Collections");
			while (rs.next()) {
				if (rs.getInt(1) == collectionID) {
					isPresent = true;
					break;
				}
			}
			
			if (isPresent) {
			rs = stmt.executeQuery("SELECT Datatype FROM Collections WHERE CollectionID = " + collectionID);
			rs.next();
			datatype = rs.getString(1);
			}
			else {
				new ExceptionDialog(new String[]{"Error retrieving collection for collectionID ",
						Integer.toString(collectionID)});
				System.err.println("collectionID not created yet!!");
			}
		
		} catch (SQLException e) {
			new ExceptionDialog(new String[]{"SQL Exception retrieving collection for collectionID ",
					Integer.toString(collectionID)});
			System.err.println("error creating collection");
			e.printStackTrace();
		}
		return new Collection(datatype,collectionID,this);
	}
	
	/**
	 * gets the collection name.
	 */
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
			new ExceptionDialog(new String[]{"Error retrieving the collection name for collectionID ",
					Integer.toString(collectionID)});
			System.err.println("Exception grabbing the collection name:");
			System.err.println(e);
		}
		return name;
	}
	
	/**
	 * gets the collection comment.
	 */
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
			new ExceptionDialog(new String[]{"Error retrieving the collection comment for collectionID ",
					Integer.toString(collectionID)});
			System.err.println("Exception grabbing the collection comment:");
			System.err.println(e);
		}
		return comment;
	}
	
	/**
	 * gets the collection description for the given collectionID
	 */
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
			new ExceptionDialog(new String[]{"Error retrieving the collection description for collectionID ",
					Integer.toString(collectionID)});
			System.err.println("Error retrieving Collection " +
					"Description.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * gets the collection size
	 */
	public int getCollectionSize(int collectionID) {
		int returnThis = -1;
		InstancedResultSet irs = getAllAtomsRS(getCollection(collectionID));
		
		try {
			ResultSet rs = con.createStatement().executeQuery(
					"SELECT COUNT(AtomID) FROM #TempParticles" + 
					irs.instance);
			rs.next();
			returnThis = rs.getInt(1);
		} catch (SQLException e1) {
			new ExceptionDialog(new String[]{"Error retrieving the collection size for collectionID ",
					Integer.toString(collectionID)});
			System.err.println("Error selecting the size of " +
					"the table");
			e1.printStackTrace();
		}
		
		try {
			con.createStatement().execute("DROP TABLE " +
					"#TempParticles" +
					irs.instance);
		} catch (SQLException e) {
			new ExceptionDialog(new String[]{"Error retrieving the collection size for collectionID ",
					Integer.toString(collectionID)});
			System.err.println("Error dropping temporary table" +
			"#TempParticles" + irs.instance);
			e.printStackTrace();
		}
		return returnThis;
	}
	
	/**
	 * gets all the atoms underneath the given collection.  Calls
	 * getAlDescendedAtomsRS, which uses recursion.  
	 */
	public ArrayList<Integer> getAllDescendedAtoms(
			Collection collection)
	{
		InstancedResultSet irs = getAllAtomsRS(collection);
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
				new ExceptionDialog("SQL Exception retrieving children of the collection.");
				System.err.println("Error retrieving children.");
				e.printStackTrace();
			}
		return results;
	}
	
	/**
	 * recursion for getting all the atoms.
	 * @param collection
	 * @return - resultset
	 */
	public InstancedResultSet getAllAtomsRS(Collection collection)
	{
		Statement stmt = null;
		try {

		    // Construct a set of all collections that descend from this one,
		    // including this one.
		    ArrayList<Integer> lookUpNext = new ArrayList<Integer>();
		    boolean status = lookUpNext.add(new Integer(collection.getCollectionID()));
		    assert status : "lookUpNext queue full";
		    Set<Integer> descCollections = new HashSet<Integer>();
		    descCollections.add(new Integer(collection.getCollectionID()));
		    
		    // As long as there is at least one collection to lookup, find
		    // all subchildren for all of these collections. Add them to the
		    // set of all collections we have visited and plan to visit
		    // then next time (if we haven't). (This is essentially a breadth
		    // first search on the graph of collection relationships).
		    while (!lookUpNext.isEmpty()) {
		        ArrayList<Integer> subChildren =
		            getImmediateSubCollections(lookUpNext);
		        lookUpNext.clear();
		        for (Integer col : subChildren)
		            if (!descCollections.contains(col)) {
		                descCollections.add(col);
		                lookUpNext.add(col);
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
		
			for (Integer col : descCollections)
			    queryString.append(col + ",");
			queryString.deleteCharAt(queryString.length()-1);
			
		    queryString.append("))");
		    
		    stmt.executeUpdate(queryString.toString());

		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception retrieving children of the collection.");
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
							"\n ORDER BY AtomID" ), instance);
		} catch (SQLException e1) {
			new ExceptionDialog("SQL Exception retrieving children of the collection.");
			System.err.println("Could not retrieve atoms from temporary table. ");
			e1.printStackTrace();
		}
		instance++;
		return returnThis;

	}

	/**
	 * gets an arraylist of ATOFMS Particles for the given collection.
	 * Unique to ATOFMS data - not used anymore except for unit tests.  
	 *
	 */
	public ArrayList<GeneralAtomFromDB> getCollectionParticles(Collection collection)
	{
		ArrayList<GeneralAtomFromDB> particleInfo = 
			new ArrayList<GeneralAtomFromDB>(1000);
		try {
			InstancedResultSet irs = getAllAtomsRS(collection);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID " +
					"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) +
					", #TempParticles" + irs.instance 
					+"\n" +
					"WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #TempParticles" + 
					irs.instance + ".AtomID\n" +
					"ORDER BY #TempParticles" + irs.instance + 
					".AtomID");
			
			DateFormat dFormat = 
				new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			
			while(rs.next())
			{
				particleInfo.add(new GeneralAtomFromDB(rs.getInt(1)));
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
	
	/**
	 * update particle table returns a vector<vector<Object>> for the gui's 
	 * particles table.  All items are taken from AtomInfoDense, and all 
	 * items are strings except for the atomID, which is used to produce 
	 * graphs.
	 */
	public Vector<Vector<Object>> updateParticleTable(Collection collection, Vector<Vector<Object>> particleInfo) {
		particleInfo.clear();
		int numberColumns = getColNames(collection.getDatatype(),DynamicTable.AtomInfoDense).size();
		// This isn't a registered datatype... oops
		if (numberColumns == 0)
			return null;
		
		try {
			InstancedResultSet irs = getAllAtomsRS(collection);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".* " +
					"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) +
					", #TempParticles" + irs.instance 
					+"\n" +
					"WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #TempParticles" + 
					irs.instance + ".AtomID\n" +
					"ORDER BY #TempParticles" + irs.instance + 
					".AtomID");
			
			while(rs.next())
			{
				Vector<Object> vtemp = new Vector<Object>(numberColumns);
				vtemp.add(rs.getInt(1)); // Integer for atomID
				for (int i = 2; i <= numberColumns; i++) 
					vtemp.add(rs.getString(i));
				particleInfo.add(vtemp);
			}
			stmt.execute("DROP TABLE " + 
			  	"#TempParticles" + irs.instance);
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception collecting particle information.");
			System.err.println("Error collecting particle " +
					"information:");
			e.printStackTrace();
		}
		return particleInfo;
	}
	
	/**
	 * gets the dynamic table name according to the datatype and the table
	 * type.
	 * @param table
	 * @param datatype
	 * @return table name.
	 */
	public String getDynamicTableName(DynamicTable table, String datatype) {
		assert (!datatype.equals("root")) : "root isn't a datatype.";
		
		if (table == DynamicTable.DataSetInfo) 
			return datatype + "DataSetInfo";
		if (table == DynamicTable.AtomInfoDense)
			return datatype + "AtomInfoDense";
		if (table == DynamicTable.AtomInfoSparse)
			return datatype + "AtomInfoSparse";
		else return null;
	}
	
	/**
	 * Gets the datatype of a given atom.  
	 * @param atomID
	 * @return
	 */
	public String getAtomDatatype(int atomID) {
		String datatype = "";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Collections.Datatype " +
					"FROM Collections,AtomMembership WHERE " +
					"AtomMembership.AtomID = " + atomID + " AND " +
					"Collections.CollectionID = " +
			"AtomMembership.CollectionID");	
			rs.next();
			datatype = rs.getString(1);
			} catch (SQLException e) {
				new ExceptionDialog(new String[] {"SQL Exception getting the datatype for atom ",
						Integer.toString(atomID)});
				System.err.println("error getting atom's datatype");
				e.printStackTrace();
			}
			return datatype;
	}
	
	/* Set functions for collections */
	
	/**
	 * Changes the collection description
	 * @return true if successful
	 */
	public boolean setCollectionDescription(Collection collection,
									String description)
	{
		description = removeReservedCharacters(description);
		try {
			con.createStatement().executeUpdate(
					"UPDATE Collections\n" +
					"SET Description = '" + description + "'\n" +
					"WHERE CollectionID = " + collection.getCollectionID());
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception updating collection description.");
			System.err.println("Error updating collection " +
					"description:");
			e.printStackTrace();
		}
		return true;
	}
	
	
	/* Misc */
	
	/**
	 * getNextID returns the next possible ID for an atom.
	 * @return ID
	 */
	public int getNextID() {
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT MAX (AtomID) FROM AtomMembership");
			
			int nextID;
			if(rs.next())
				nextID = rs.getInt(1) + 1;
			else
				nextID = 0;
			stmt.close();
			return nextID;
			
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception finding the maximum atomID.");
			System.err.println("Exception finding max atom id.");
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * exports a collection to the MSAnalyze database by making up 
	 * the necessary data to import (.par file, etc).
	 * @return date associated with the mock dataset.
	 */
	public java.util.Date exportToMSAnalyzeDatabase(
			Collection collection, 
			String newName, 
			String sOdbcConnection) 
	{
		
		assert (collection.getDatatype().equals("ATOFMS")) :
			"trying to export the wrong datatype for MSAnalyze: " + collection.getDatatype();		
		DateFormat dFormat = null;
		Date startTime = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		} catch (ClassNotFoundException e) {
			new ExceptionDialog("Error loading ODBC bridge driver.");
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
			
			InstancedResultSet irs = getAllAtomsRS(collection);
			
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
					"	SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID, OrigFilename, [Time], [Size], LaserPower\n" +
					"	FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ", #TempParticles" + irs.instance 
					+ "\n" +
					"	WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #TempParticles" + 
					irs.instance + ".AtomID\n" +
					")\n" +
					
					"UPDATE #ParticlesToExport\n" +
					"SET NumPeaks = \n" +
					"	(SELECT COUNT(AtomID)\n" +
					"		FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + "\n" +
					"			WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #ParticlesToExport.AtomID),\n" +
					"TotalPosIntegral = \n" +
					"	(SELECT SUM (PeakArea)\n" +
					"		FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + "\n" +
					"			WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #ParticlesToExport.AtomID\n" +
					"			AND " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".PeakLocation >= 0),\n" +
					"TotalNegIntegral =\n" +
					"	(SELECT SUM (PeakArea)\n" +
					"		FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + "\n" +
					"			WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #ParticlesToExport.AtomID\n" +
					"			AND " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".PeakLocation < 0)\n"
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
					"FROM #ParticlesToExport");
			Date endTime;
			startTime = endTime = null;
			long unixTime;

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
					"FROM #ParticlesToExport\n");
			if (rs.next())
			{
				endTime = new Date(rs.getTimestamp(1).getTime());
				//endTime = endTime.substring(0, startTime.length()-2);
			}
			
			
			String comment = " ";
			
			// Get the comment for the current collection to use
			// as the comment for the dataset
			rs = stmt.executeQuery(
					"SELECT Comment \n" +
					"FROM Collections\n" +
					"WHERE CollectionID = " + collection.getCollectionID());
			if (rs.next())
				comment = rs.getString(1);
			if (comment.length() == 0)
				comment = "Imported from Edam-Enchilada";
			
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
					"FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ", #TempParticles" + 
					irs.instance + 
					", " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + "\n" +
					"	WHERE (" + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".AtomID = " +
					"#TempParticles" + 
					irs.instance + ".AtomID)\n" +
					"		AND (" + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".AtomID = " +
					getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID)" +
					")\n" +
					"DROP TABLE #TempParticles" + 
					irs.instance);
			
			rs = stmt.executeQuery(
					"SELECT OrigFilename, PeakLocation, " +
					"PeakArea, " +
					"RelPeakArea, PeakHeight\n" +
					"FROM #PeaksToExport");
			odbcStmt.executeUpdate(
					"DELETE FROM Peaks \n" +
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
			new ExceptionDialog("SQL Exception exporting to MSAccess database.");
			System.err.println("SQL error exporting to " +
					"Access database:");
			e.printStackTrace();
			return null;
		}
		return startTime;
	}

	/**
	 * Checks to see if the atom id is a member of the collectionID.
	 * @return true if atom is a member of the collection.
	 */
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
			new ExceptionDialog("SQL Exception checking atom's parentage.");
			System.err.println("Error checking parentage:");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * returns the connection.  Used to be a private or protected 
	 * method (don't know when we last changed it).
	 * @return connection
	 */
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
	
	/**
	 * rebuilds the database; sets the static tables.
	 * @param dbName
	 * @return true if successful
	 */
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
			new ExceptionDialog("Error rebuilding SQL Server database.");
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
			new ExceptionDialog("Error rebuilding SQL Server database.");
			System.out.println("Error in handling SQLServerDatabaseGenerate.txt.");		
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			new ExceptionDialog("Error rebuilding SQL Server database.");
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
	
	/**
	 * drops the given database.
	 * @param dbName
	 * @return
	 */
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
			new ExceptionDialog("Error dropping SQL Server database.");
			System.err.println("Error in dropping SQL Server database.");
			e.printStackTrace();
			return false;
		} finally {
			if (db != null)
				db.closeConnection();
		}
		return true;
	}		
	
	
	/** (non-Javadoc)
	 * @see database.InfoWarehouse#getPeaks(int)
	 * 
	 * gets an arraylist of peaks given a datatype and atomID.  
	 * ATOFMS-specific.
	 */
	public ArrayList<Peak> getPeaks(String datatype, int atomID) 
	{
		ResultSet rs = null;
		try {
			rs = con.createStatement().executeQuery(
				"SELECT * FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,datatype) + " WHERE AtomID = " +
				atomID);
		} catch (SQLException e) {
			System.err.println("Error selecting peaks");
			e.printStackTrace();
		}
		ArrayList<Peak> returnThis = new ArrayList<Peak>();
		float location = 0, relArea = 0;
		int area = 0, height = 0;
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
			new ExceptionDialog("SQL Exception retrieving peaks.");
			System.err.println("Error using the result set");
			e.printStackTrace();
		}
		return returnThis;
	}
	
	/* Cursor classes */

	/**
	 * AtomInfoOnly cursor.  Returns atom info.
	 */
	private class AtomInfoOnlyCursor 
	implements CollectionCursor {
		protected InstancedResultSet irs;
		protected ResultSet partInfRS = null;
		protected Statement stmt = null;
		Collection collection;
		
		public AtomInfoOnlyCursor(Collection col) {
			super();
			
			assert(col.getDatatype().equals("ATOFMS")) : "Wrong datatype for cursor.";
			
			collection = col;
			
			try {
				stmt = con.createStatement();
				irs = getAllAtomsRS(collection);
				partInfRS = stmt.executeQuery(
						"SELECT ATOFMSAtomInfoDense.AtomID, " +
						"OrigFilename, ScatDelay, LaserPower, " +
						"[Time]\n" +
						"FROM ATOFMSAtomInfoDense, #TempParticles" + 
						irs.instance +
						"\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = ATOFMSAtomInfoDense.AtomID\n" +
						"ORDER BY #TempParticles" + 
						irs.instance + 
						".AtomID");
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
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
						"SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID, " +
						"OrigFilename, ScatDelay, LaserPower, [Time]\n" +
						"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ", #TempParticles" + 
						irs.instance + 
						"\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID\n" +
						"ORDER BY #TempParticles" + 
						irs.instance + 
						".AtomID");
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				System.err.println("SQL Error resetting " +
						"cursor: ");
				e.printStackTrace();
			}
		}

		public boolean next() {
			try {
				return partInfRS.next();
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
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
						new ATOFMSAtomFromDB(
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
				new ExceptionDialog("SQL Exception retrieving data.");
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
				new ExceptionDialog("SQL Exception retrieving data.");
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
							"FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + "\n" +
							"WHERE AtomID = " + atomID);
				while(rs.next()) {
					peakList.add(
							rs.getFloat(1),
							rs.getInt(2));
				}
				rs.close();
				return peakList;
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				System.err.println("Error retrieving peak " +
						"list.");
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * SQL Cursor.  Returns atom info with a given "where" clause.
	 */
	private class SQLCursor extends AtomInfoOnlyCursor
	{
		private Statement stmt;
		private String where;
		private Collection collection;
		SQLServerDatabase db;
		/**
		 * @param collectionID
		 */
		public SQLCursor(Collection col, String where, SQLServerDatabase db) {
			super(col);
			collection = col;
			this.where = where;
			this.db = db;
			InstancedResultSet irs = db.getAllAtomsRS(collection);
			try {
				stmt = con.createStatement();
			
				partInfRS = stmt.executeQuery(
						"SELECT ATOFMSAtomInfoDense.AtomID, " +
						"OrigFilename, ScatDelay, " +
						"LaserPower, " +
						"[Time]\n" +
						"FROM ATOFMSAtomInfoDense, #TempParticles" + irs.instance + "\n" +
						"WHERE #TempParticles" + 
						irs.instance + ".AtomID = ATOFMSAtomInfoDense.AtomID " +
						"AND " + where);
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				e.printStackTrace();
			}
		}
		
		public void close() {
			try {
				con.createStatement().executeUpdate(
				"DROP Table #TempParticles" + irs.instance);
				super.close();
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				e.printStackTrace();
			}
		}
		public void reset()
		{
			
			try {
				partInfRS.close();
				partInfRS = stmt.executeQuery(
						"SELECT " + db.getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID, " +
						"OrigFilename, ScatDelay, " +
						"LaserPower, " +
						"[Time]\n" +
						"FROM " + db.getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ", #TempParticles" + 
						irs.instance + "\n" +
						"WHERE #TempParticles" + 
						irs.instance + 
						".AtomID = " + db.getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID " +
						"AND " + where);
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				System.err.println("SQL Error resetting cursor: ");
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Peak Cursor.  Returns peak info for a given atom.
	 */
	private class PeakCursor extends AtomInfoOnlyCursor
	{	
		protected Statement stmt = null;
		protected ResultSet peakRS = null;
		private Collection collection;
		
		public PeakCursor(Collection col)
		{
			super (col);
			collection = col;
			try {
				stmt = con.createStatement();

			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
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
						"FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + "\n" +
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
				new ExceptionDialog("SQL Exception retrieving data.");
				e.printStackTrace();
			}
			
			return pInfo;
		}
		
		public void close(){
			try {
				peakRS.close();
				super.close();
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Binned Cursor.  Returns binned peak info for a given atom.
	 */
	private class BinnedCursor extends PeakCursor {

		/**
		 * @param collectionID
		 */
		public BinnedCursor(Collection collection) {
			super(collection);
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
	 * Randomized Cursor.  Returns randomized atom info.
	 *
	 * NOTE:  Randomization cursor info found at 
	 * http://www.sqlteam.com/item.asp?ItemID=217
	 */
	private class RandomizedCursor extends BinnedCursor {
		protected Statement stmt = null;
		private Collection collection;
		/**
		 * @param collectionID
		 */
		public RandomizedCursor(Collection col) {
			super(col);
			collection = col;
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
				String cursorQuery = "SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID, OrigFilename, ScatDelay, LaserPower, [Time]\n" +
				"FROM #TempRand, " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + " " +
				"WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #TempRand.AtomID\n" +
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
				new ExceptionDialog("SQL Exception randomizing data.");
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
				new ExceptionDialog("SQL Exception retrieving data.");
				e.printStackTrace();
			}
		}
		public void reset()
		{
			
			try {
				partInfRS.close();
				String cursorQuery = "SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID, OrigFilename, " +
						"ScatDelay, LaserPower, [Time]\n" +
				"FROM #TempRand, " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + " " +
				"WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #TempRand.AtomID\n" +
				"ORDER BY RandNum";
				partInfRS = stmt.executeQuery(cursorQuery);
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving data.");
				System.err.println("SQL Error resetting cursor: ");
				e.printStackTrace();
			}
		}
	}


	/**
	 * Memory Binned Cursor.  Returns binned peak info for a given atom,
	 * info kept in memory.
	 */
	private class MemoryBinnedCursor extends BinnedCursor {
		InfoWarehouse db;
		boolean firstPass = true;
		int position = -1;
		
		ArrayList<ParticleInfo> storedInfo = null;
		
		public MemoryBinnedCursor(Collection collection) {
			super (collection);
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

	/**
	 * get method for atomInfoOnlyCursor.
	 */
	public CollectionCursor getAtomInfoOnlyCursor(Collection collection)
	{
		return new AtomInfoOnlyCursor(collection);
	}
	
	/**
	 * get method for SQLCursor.
	 */
	public CollectionCursor getSQLCursor(Collection collection, 
									     String where)
	{
		return new SQLCursor(collection, where, this);
	}
	
	/**
	 * get method for peakCursor.
	 */
	public CollectionCursor getPeakCursor(Collection collection)
	{
		return new PeakCursor(collection);
	}
	
	/**
	 * get method for BinnedCursor.
	 */
	public CollectionCursor getBinnedCursor(Collection collection)
	{
		return new BinnedCursor(collection);
	}
	
	/**
	 * get method for MemoryBinnedCursor.
	 */
	public CollectionCursor getMemoryBinnedCursor(Collection collection)
	{
		return new MemoryBinnedCursor(collection);
	}
	
	/**
	 * get method for randomizedCursor.
	 */
	public CollectionCursor getRandomizedCursor(Collection collection)
	{
		return new RandomizedCursor(collection);
	}
	
	/**
	 * Seeds the random number generator.
	 */
	public void seedRandom(int seed) {
		try {
			Statement stmt = con.createStatement();
			stmt.execute("SELECT RAND(" + seed + ")\n");
			stmt.close();
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception retrieving data.");
			System.err.println("Error in seeding random number generator.");		
			e.printStackTrace();
		}
	}
	
	/**
	 *  Used for testing random number seeding 
	 */
	public double getNumber() {
	    try {
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery("SELECT RAND()");
	        rs.next();
	        return rs.getDouble(1);
	    } catch (SQLException e) {
			new ExceptionDialog("SQL Exception retrieving data.");
	        System.err.println("Error in generating single number.");
	        e.printStackTrace();
	    }
	    return -1;
	}

	/**
	 * getColNamesAndTypes returns an arraylist of strings of the column names for the given table
	 * and datatype.  Not used yet, but may be useful in the future.  
	 * @param datatype
	 * @param table - dynamic table you want
	 * @return arraylist of column names and their types.
	 */
	public ArrayList<ArrayList<String>> getColNamesAndTypes(String datatype, DynamicTable table) {
		ArrayList<ArrayList<String>> colNames = new ArrayList<ArrayList<String>>();
		ArrayList<String> temp;	
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ColumnName, ColumnType FROM MetaData " +
					"WHERE Datatype = '" + datatype + "' " +
			"AND TableID = " + table.ordinal() + " ORDER BY ColumnOrder");
			
			while (rs.next()) {
				temp = new ArrayList<String>();
				temp.add(rs.getString(1));
				temp.add(rs.getString(2));
				colNames.add(temp);
			}
			
			} catch (SQLException e) {
				new ExceptionDialog("SQL Exception retrieving column names.");
				System.err.println("Error retrieving column names");
				e.printStackTrace();
			}
		return colNames;
	}

	/**
	 * getColNamesAndTypes returns an arraylist of strings of the column names for the given table
	 * and datatype.  Not used yet, but may be useful in the future.  
	 * @param datatype
	 * @param table - dynamic table you want
	 * @return arraylist of column names.
	 */
	public ArrayList<String> getColNames(String datatype, DynamicTable table) {
		ArrayList<String> colNames = new ArrayList<String>();
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ColumnName FROM MetaData " +
					"WHERE Datatype = '" + datatype + "' " +
			"AND TableID = " + table.ordinal() + " ORDER BY ColumnOrder");
			
			while (rs.next()) 
				colNames.add(rs.getString(1));
			
			
			} catch (SQLException e) {
			System.err.println("Error retrieving column names");
			e.printStackTrace();
			}
		return colNames;
	}
	
	public int saveMap(String name, Vector<int[]> mapRanges) {
		int valueMapID = -1;
		
		try{
			Statement stmt = con.createStatement();		
			ResultSet rs 
				= stmt.executeQuery("SET NOCOUNT ON " +
						"INSERT ValueMaps (Name) Values('" + name.replace("'", "''") + "') " +
						"SELECT @@identity " +
						"SET NOCOUNT OFF");
			rs.next();
			valueMapID = rs.getInt(1);
			rs.close();
			
			for (int i = 0; i < mapRanges.size(); i++) {
				int[] range = mapRanges.get(i);
			
				stmt.execute(
						"INSERT ValueMapRanges (ValueMapID, Value, Low, High) " +
						"VALUES ("+valueMapID+","+range[0]+","+range[1]+","+range[2]+")");
			}
		} catch (SQLException e) {
			new ExceptionDialog("SQL Exception inserting new value map range.");
			System.err.println("Error inserting new value map range");
			e.printStackTrace();
		}
		return valueMapID;
	}
	
	public Hashtable<Integer, String> getValueMaps() {
		Hashtable<Integer, String> valueMaps = new Hashtable<Integer, String>();
		
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ValueMapID, Name from ValueMaps");
			
			while (rs.next())
				valueMaps.put(rs.getInt("ValueMapID"), rs.getString("Name"));
			rs.close();
		}
		catch (SQLException e){
			new ExceptionDialog("SQL exception retrieving value vaps");
			System.err.println("Error getting value maps from database.");
			e.printStackTrace();
		}
		
		return valueMaps;
	}
	
	public Vector<int[]> getValueMapRanges() {
		Vector<int[]> valueMapRanges = new Vector<int[]>();
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs 
				= stmt.executeQuery("SELECT ValueMapID, Value, Low, High " +
									"FROM ValueMapRanges " +
									"ORDER BY ValueMapID, Low ");
			
			while (rs.next())
				valueMapRanges.add(new int[] {rs.getInt("ValueMapID"), rs.getInt("Value"), rs.getInt("Low"), rs.getInt("High") });
			rs.close();
		}
		catch (SQLException e){
			new ExceptionDialog("SQL exception retrieving value map ranges");
			System.err.println("Error getting value map ranges from database.");
			e.printStackTrace();
		}
		
		return valueMapRanges;
	}
	
	public int applyMap(String mapName, Vector<int[]> map, Collection collection) {
		int oldCollectionID = collection.getCollectionID();
		String colName = this.getCollectionName(oldCollectionID);
		String dataType = collection.getDatatype();
		
		int newCollectionID = createEmptyCollection(dataType, oldCollectionID, colName + " - " + mapName, "", "");
		String tableName = getDynamicTableName(DynamicTable.AtomInfoDense, dataType);
		
		int nextAtomID = getNextID();
		String mapStatement = "CASE";
		for (int i = 0; i < map.size(); i++) {
			int[] curMap = map.get(i);
			mapStatement += " WHEN T.Value >= " + curMap[1] + " AND T.Value < " + curMap[2] + " THEN " + curMap[0];
		}
		mapStatement += " ELSE NULL END";
		
		String query = 
			"DECLARE @atoms TABLE ( " +
			"   NewAtomID int IDENTITY(" + nextAtomID + ",1), " +
			"   OldAtomID int " +
			") " +
			
			" insert @atoms (OldAtomID) " +
			" select AtomID from AtomMembership where CollectionID = " + oldCollectionID +
			
			" insert AtomMembership (CollectionID, AtomID) " +
			" select " + newCollectionID + ", NewAtomID" +
			" from @atoms A" +

			" insert " + tableName + " (AtomID, Time, Value) " +
			" select A.NewAtomID, T.Time, " + mapStatement + " as Value" +
			" from " + tableName + " T " +
		    " join @atoms A on (A.OldAtomID = T.AtomID)";

		System.out.println(query);
		
		try {
			Statement stmt = con.createStatement();
			stmt.execute(query);
		} catch (SQLException e) {
			new ExceptionDialog("SQL exception creating new mapped collection");
			System.err.println("Error creating new mapped collection.");
			e.printStackTrace();
		}
		
		return newCollectionID;
	}
	/**
	 * Get all the datatypes currently entered in the database.
	 * 
	 * @return ArrayList<String> of the known datatypes.
	 */
	public ArrayList<String> getKnownDatatypes(){
		ArrayList<String> knownTypes = new ArrayList<String>();
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Datatype FROM MetaData");
			String currentType = "";
			while (rs.next()){
				if (! rs.getString(1).equals(currentType)){
					currentType = rs.getString(1);
					knownTypes.add(currentType);
				}
			}
		}
		catch (SQLException e){
			new ExceptionDialog("SQL exception retrieving known datatypes.");
			System.err.println("Error getting the known datatypes.");
			e.printStackTrace();
		}
		
		return knownTypes;
	}
	
	/**
	 * Determine whether the database already contains a datatype.
	 * 
	 * @param type	The name of the datatype you seek.
	 * @return	True if the datatype is in the database, false otherwise.
	 */
	public boolean containsDatatype(String type){
		
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Datatype FROM MetaData"
					+" WHERE Datatype = '" + type +"'");
			
			if (rs.next()){
				return true;}
			else
				return false;
			
		}
		catch (SQLException e){
			new ExceptionDialog(new String[]{"SQL Exception checking for the existence of datatype ",
					type});
			System.err.println("problems checking datatype from SQLServer.");
			return false;
		}
		
	}

}

