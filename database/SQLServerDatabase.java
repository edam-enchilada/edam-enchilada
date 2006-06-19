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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.sql.*;

import ATOFMS.ParticleInfo;
import ATOFMS.Peak;
import analysis.*;
import analysis.clustering.ClusterInformation;
import analysis.clustering.PeakList;
import analysis.dataCompression.CFNode;
import analysis.dataCompression.CFTree;
import collection.*;
import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;

import gui.*;

import java.io.*;
import java.util.Scanner;

import errorframework.ErrorLogger;

/* 
 * Maybe a good way to refactor this file is to separate out methods that
 * are used by importers from those used by clustering code, and so on.
 * It might work well, or it might not...
 */

/**
 * @author andersbe
 *
 */
public class SQLServerDatabase implements InfoWarehouse
{
	/* Class Variables */
	protected Connection con;
	private String url;
	private String port;
	private String database;
	private static int instance = 0;
	private String tempdir = System.getenv("TEMP");
	
	// for batch stuff
	private Statement batchStatement;
	private ArrayList<Integer> alteredCollections;
	
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
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.displayException(null,"Error in testing if "+dbName+" is present.");
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
			Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
		} catch (Exception e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Failed to load current driver for database.");
			System.err.println("Failed to load current driver.");
			return false;
		} // end catch
		con = null;
		try {
			con = DriverManager.getConnection("jdbc:jtds:sqlserver://" + url + ":" + port + ";DatabaseName=" + database + ";SelectMethod=cursor;","SpASMS","finally");
		} catch (Exception e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Failed to establish a connection to SQL Server.");
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
				ErrorLogger.writeExceptionToLog("SQLServer","Could not close the connection to SQL Server.");
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
	 * @param parent	The key to add this collection under (0 
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
					", '" + removeReservedCharacters(name) + "', '" 
					+ removeReservedCharacters(comment) + "', '" + 
					removeReservedCharacters(description) + "', '" + datatype + "')");
			stmt.executeUpdate("INSERT INTO CollectionRelationships\n" +
					"(ParentID, ChildID)\n" +
					"VALUES (" + Integer.toString(parent) +
					", " + Integer.toString(nextID) + ")");
			
			
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception creating empty collection.");
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
	 * 
	 * Don't include the name of the dataset in the list of params -
	 * it will be added by the method.
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
			
			//Changed back to use datasetName separately from params to fix
			//importation from MSAnalyze.  TODO: We should discuss.  ~Leah
			String statement = "INSERT INTO " + getDynamicTableName(DynamicTable.DataSetInfo,datatype) + " VALUES(" + 
			returnVals[1] + ",'" + datasetName + "',"+ params + ")";
			if (statement.charAt(statement.length()-2) == ',')
				statement = statement.substring(0,statement.length()-2)+")";
			System.out.println(statement); //debugging
			stmt.execute(statement);	
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception creating the new dataset.");
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
	 * @param parentID	The key of the parent to insert this
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
			boolean next = rs.next();
			assert (next) : "Error copying collection information";
			newID = createEmptyCollection(collection.getDatatype(),
					toCollection.getCollectionID(), 
					rs.getString(1), rs.getString(2),rs.getString(3));
			Collection newCollection = getCollection(newID);
			String description = getCollectionDescription(collection.getCollectionID());
			if (description  != null)
				setCollectionDescription(newCollection, getCollectionDescription(collection.getCollectionID()));
			
			rs = stmt.executeQuery("SELECT AtomID\n" +
					"FROM AtomMembership\n" +
					"WHERE CollectionID = " +
					collection.getCollectionID());
			System.out.println("Copying: " + collection.getCollectionID());
			System.out.println("new CollectionID: " + newID);
			while (rs.next())
			{
				stmt.addBatch("INSERT INTO AtomMembership\n" +
						"(CollectionID, AtomID)\n" +
						"VALUES (" + newID + ", " +
						rs.getInt("AtomID") + 
				")");
			}
			stmt.executeBatch();
			rs.close();
			// Get Children
			ArrayList<Integer> children = getImmediateSubCollections(collection);
			for (int i = 0; i < children.size(); i++) {
				copyCollection(getCollection(children.get(i)), newCollection);			
			}
			
			//updates the internal atom order for each child collection.  
			// this can be optimized.
			updateInternalAtomOrder(newCollection);
			
			stmt.close();
			
			// update new collection's ancestors.
			updateAncestors(newCollection.getParentCollection());
			return newID;
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception copying collection "+collection.getName());
			System.err.println("Exception copying collection: ");
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Moves a collection and all its children from one parent to 
	 * another.  If the subcollection was the only child of the parent
	 * containing a particular atom, that atom will be removed from 
	 * the parent, if there are other existing subcollections of the 
	 * parent containing particles also belonging to this collection, 
	 * those particles will then exist both in the current collection and
	 * its parent.
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
			int col = collection.getCollectionID();
			int toCol = toCollection.getCollectionID();
			
			Statement stmt = con.createStatement();
			stmt.executeUpdate("UPDATE CollectionRelationships\n" +
					"SET ParentID = " + toCol + "\n" +
					"WHERE ChildID = " + col);
			
			// update InternalAtomOrder table.
			// update toCollection from collection to leaves
			updateInternalAtomOrder(toCollection);
			// update collection and toCollection's parent up to root.
			updateAncestors(collection);
			updateAncestors(toCollection.getParentCollection());
			stmt.close();
		} catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception moving the collection "+collection.getName());
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception adding particles, please check the incoming data for correct format.");
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception creating items in AtomInfoDense table.  Please check incoming data for correct format.");
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
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception inserting into AtomInfoSparse.  Please check the data for correct format.");
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
		//System.out.println("next AtomID: "+nextID);
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
				//System.out.println("server is localhost");
				String tempFilename = tempdir + File.separator + "bulkfile.txt";
				PrintWriter bulkFile = null;
				try {
					bulkFile = new PrintWriter(new FileWriter(tempFilename));
				} catch (IOException e) {
					System.err.println("Trouble creating " + tempFilename);
					e.printStackTrace();
					// XXX: do something else here
				}
				
				for (int j = 0; j < sparse.size(); j++) {
					bulkFile.println(nextID + "," + sparse.get(j));
					//System.out.println(nextID + "," + sparse.get(j));
				}
				
				bulkFile.close();
				String statement = "BULK INSERT " + tableName + "\n" +
						"FROM '" + tempFilename + "'\n" +
				"WITH (FIELDTERMINATOR=',')";
				stmt.addBatch(statement);
				//System.out.println("Statement: "+statement);
			} else {
				for (int j = 0; j < sparse.size(); j++)
					stmt.addBatch("INSERT INTO " + tableName +  
							" VALUES (" + nextID + "," + sparse.get(j) + ")");
				
			}
			//System.out.println(tableName);
			
			stmt.executeBatch();
			
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception inserting atom.  Please check incoming data for correct format.");
			System.err.println("Exception inserting particle.");
			e.printStackTrace();
			
			return -1;
		}
		return nextID;
	}
	
	//TODO : Although this means we can import multiple AtomInfoSparse tables,
	//		the rest of the program can't handle them.
	/**
	 * A method to insert particles in the database that allows for multiple
	 * AtomInfoSparse tables.
	 * 
	 * @param dense	The dense info for this particle, in a comma-separated string.
	 * @param sparseTables	The sparse info, each sparse table has its own entry 
	 * 						in the map, key of its tablename.  Each ArrayList
	 * 						represents one SparseInfo entry, with the data contained
	 * 						in a comma-separated string.
	 * @param collection The collection into which the particle is imported.
	 * @param datasetID	 The dataset into which the particle is imported.
	 * @param nextID The atomID for the particle being imported.
	 * @return	the successfully inserted particle's ID (-1 on failure).
	 */
	public int insertParticle(String dense, 
			TreeMap<String, ArrayList<String>> sparseTables,
			Collection collection,
			int datasetID, int nextID){
		
		//System.out.println("Inserting new particle:");
		String insert = "";
		
		try {
			Statement stmt = con.createStatement();
			//System.out.println("Adding batches");
			
			insert = "INSERT INTO " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + " VALUES (" + 
			nextID + ", " + dense + ")";
			//System.out.println(insert); //debugging
			stmt.addBatch(insert);
			insert = "INSERT INTO AtomMembership" +
			"(CollectionID, AtomID)" +
			"VALUES (" +
			collection.getCollectionID() + ", " +
			nextID + ")";
			//System.out.println(insert); //debugging
			stmt.addBatch(insert);
			insert = "INSERT INTO DataSetMembers" +
			"(OrigDataSetID, AtomID)" +
			" VALUES (" +
			datasetID + ", " + 
			nextID + ")";
			//System.out.println(insert); //debugging
			stmt.addBatch(insert);
			stmt.executeBatch();
			
			// Only bulk insert if client and server are on the same machine...
			if (url.equals("localhost")) {
				String tempFilename = tempdir + File.separator + "bulkfile.txt";
				PrintWriter bulkFile = null;
				try {
					bulkFile = new PrintWriter(new FileWriter(tempFilename));
				} catch (IOException e) {
					System.err.println("Trouble creating " + tempFilename);
					e.printStackTrace();
					// XXX: do something else here
				}
				
				
				while (!sparseTables.isEmpty()){
					//the table name is the string the arraylists are mapped by
					String tableName = collection.getDatatype() + sparseTables.firstKey();
					
					ArrayList<String> sparse = sparseTables.get(sparseTables.firstKey());
					//insert all the strings in the arraylist
					String printer;
					System.out.println("Printing to bulk file:");
					for (int j = 0; j < sparse.size(); j++){
						printer = nextID + "," + sparse.get(j);
						System.out.println(printer);//debugging
						bulkFile.println(printer);
					}
					//remove that mapping
					sparseTables.remove(sparseTables.firstKey());
					
					bulkFile.close();
					stmt.addBatch("BULK INSERT " + tableName + "\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH (FIELDTERMINATOR=',')");
				}
				
				bulkFile.close();
				
				//endif	
			} else {
				
				//for each of the arraylists of strings in the map
				String string;
				while (!sparseTables.isEmpty()){
					
					//the table name is the string the arraylists are mapped by
					//preceeded by the datatype name
					String tableName = collection.getDatatype() + 
					sparseTables.firstKey();
					
					ArrayList<String> sparse = 
						sparseTables.get(sparseTables.firstKey());
					//insert all the strings in the arraylist
					for (int j = 0; j < sparse.size(); j++){
						string = "INSERT INTO " + tableName + 
						" VALUES (" + nextID + "," + sparse.get(j) + ")";
						System.out.println(string);	//debugging
						stmt.addBatch(string);
					}
					
					//remove that mapping
					sparseTables.remove(sparseTables.firstKey());
				}
				
			}
			
			stmt.executeBatch();			
		stmt.close();
			
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception inserting atom.  Please check incoming data for correct format.");
			System.err.println("value of insert:" + insert);
			System.err.println("Exception inserting particle.");
			e.printStackTrace();
			
			return -1;
		}
		// update internal atom table.
		addSingleInternalAtomToTable(nextID, collection.getCollectionID());
	
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception adding atom "+atomID+"to AtomMembership.");
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception adding atom "+atomID+" to AtomMembership.");
			System.err.println("Exception adding atom to " +
			"AtomMembership table");
			e.printStackTrace();
			return false;
		}
		if (!alteredCollections.contains(new Integer(parentID)))
			alteredCollections.add(new Integer(parentID));
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
			// parentID is now set to the parent of the current 
			// collection
			int parentID = getParentCollectionID(collection.getCollectionID());
			if (parentID == -1)
				return false;
			else if (parentID < 2)
			{
				ErrorLogger.writeExceptionToLog("SQLServer","Cannot perform the Orphan And Adopt operation on root level collections.");
				System.err.println("Cannot perform this operation " +
				"on root level collections.");
				return false;
			}
			
			Statement stmt = con.createStatement();
			
			// Get rid of the current collection in 
			// CollectionRelationships 
			stmt.execute("DELETE FROM CollectionRelationships\n" + 
					"WHERE ChildID = " + 
					Integer.toString(collection.getCollectionID()));
			
			//This gets all the original atoms that belong to the parentCollection;
			Collection parentCollection = getCollection(parentID);
			//ResultSet rs = getAllAtomsRS(parentCollection);
			
			// Find the child collections of this collection and 
			// move them to the parent.  
			ArrayList<Integer> subChildren = getImmediateSubCollections(collection);
			for (int i = 0; i < subChildren.size(); i++)
			{
				moveCollection(getCollection(subChildren.get(i).intValue()), 
						parentCollection);
			}
			
			//this query updates the AtomMembership database so that all the collectionIDs are set to
			//parentID when the CollectionID is the child's CollectionID and when AtomID has
			//the child's CollectionID but not the parent's CollectionID
			//so if we have
			// {(2,100), (2, 101), (2, 103), (5, 99), (5, 100), (5, 101)}
			//where 2 is the parent and 5 is the child and the first number denotes the CollectionID
			//and the second number denotes the AtomID
			//we want it to change all the 5s to 2s except when the corresponding AtomID is already
			//in the set of 2s.  So we want to change (5, 99) but not (5, 100) and (5, 101).
			
			String query = "UPDATE AtomMembership SET CollectionID = " + 
			parentID + " WHERE CollectionID = " + collection.getCollectionID()+ 
			" and AtomID in (select AtomID from AtomMembership where CollectionID = " + collection.getCollectionID() + 
			" and AtomID not in (select AtomID from AtomMembership where CollectionID = " + parentID + "))";

			stmt.executeUpdate(query);
			
			// Delete the collection now that everything has been 
			// moved.  Updates the InternalAtomOrder table as well.
			recursiveDelete(collection);
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error executing Orphan and Adopt.");
			System.err.println("Error executing orphan and Adopt");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Deletes a collection and unlike orphanAndAdopt() also recursively
	 * deletes all direct descendents.
	 * This method merely selects the collections to be deleted and stores them in #deleteTemp
	 * 
	 * @param collectionID The id of the collection to delete
	 * @return true on success. 
	 */
	public boolean recursiveDelete(Collection collection)
	{
		Collection parent = collection.getParentCollection();
		System.out.println("Deleting " + collection.getCollectionID());
		try {
			Statement stmt = con.createStatement();
			StringBuilder sql = new StringBuilder();
			sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#deleteCollections')\n"+
					"BEGIN\n"+
					"DROP TABLE #deleteAtoms\n"+
					"END;\n");
					sql.append("CREATE TABLE #deleteCollections (CollectionID int);\n");
			// Update the InternalAtomOrder table:  Assumes that subcollections
			// are already updated for the parentCollection.
			// clear InternalAtomOrder table of the deleted collection and all subcollections.
			Set<Integer> descendents =  getAllDescendantCollections(collection.getCollectionID(), false);
			Iterator allsubcollections = descendents.iterator();
			if (url.equals("localhost")) {
				String tempFilename = tempdir + File.separator + "bulkfile.txt";
				PrintWriter bulkFile = null;
				try {
					bulkFile = new PrintWriter(new FileWriter(tempFilename));
				} catch (IOException e) {
					System.err.println("Trouble creating " + tempFilename);
					e.printStackTrace();
				}
				bulkFile.println(collection.getCollectionID());
				while(allsubcollections.hasNext()){
					bulkFile.println(allsubcollections.next());
				}
				bulkFile.close();
				sql.append("BULK INSERT #deleteCollections\n" +
						"FROM '" + tempFilename + "'\n" +
				"WITH (FIELDTERMINATOR=',');\n");
			} else {
				while(allsubcollections.hasNext()){
					sql.append("INSERT INTO #deleteCollections VALUES("+allsubcollections.next()+");\n");
				}
			}
			stmt.execute(sql.toString());
			rDelete(collection, collection.getDatatype());
			stmt.execute("DROP TABLE #deleteCollections;\n");
			stmt.close();
			updateAncestors(parent);
		} catch (Exception e){
			ErrorLogger.writeExceptionToLog("SQLServer","Exception deleting collection.");
			System.err.println("Exception deleting collection: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Actual deletion of the collections and atoms.
	 * Updates the ancestors of the InternalAtomORder table on the deleted
	 * collection's ancestors. 
	 * @param collection
	 * @throws SQLException
	 */
	private void rDelete(Collection collection, String datatype) throws SQLException
	{
		Statement stmt = con.createStatement();
		StringBuilder sql = new StringBuilder();
		
		sql.append("DELETE FROM InternalAtomOrder\n"+
				"WHERE CollectionID IN (SELECT * FROM #deleteCollections);\n");	
		sql.append("DELETE FROM CollectionRelationships\n" + 
				"WHERE ParentID IN (SELECT * FROM #deleteCollections)\n" +
				"OR ChildID IN (SELECT * FROM #deleteCollections);\n" );
		sql.append("DELETE FROM AtomMembership\n" +
				"WHERE CollectionID IN (SELECT * FROM #deleteCollections);\n" );
		sql.append("DELETE FROM Collections\n" +
				"WHERE CollectionID IN (SELECT * FROM #deleteCollections);\n" );
		
		sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#deleteAtoms')\n"+
				"BEGIN\n"+
				"DROP TABLE #deleteAtoms\n"+
				"END;\n");
		sql.append("CREATE TABLE #deleteAtoms (AtomID int);\n");
		sql.append("insert #deleteAtoms (AtomID) \n" +
				"	SELECT AtomID\n" +
				"	FROM DataSetMembers\n" +
				"	WHERE DataSetMembers.AtomID <> ALL\n" +
				"		(\n" +
				"		SELECT AtomID\n" +
				"		FROM AtomMembership\n" +
				"		);\n");

//		 Also: We could delete all the particles from the particles
		// table IF we want to by now going through the particles 
		// table and choosing every one that does not exist in the 
		// Atom membership table and deleting it.  However, this would
		// remove particles that were referenced in the DataSetMembers 
		// table.  If we don't want this to happen, comment out the 
		// following code, which also removes all references in the 
		// DataSetMembers table:
		//System.out.println(1);
		sql.append("DELETE FROM DataSetMembers\n" +
				"WHERE AtomID IN (SELECT * FROM #deleteAtoms);\n");
		
		String sparseTableName = getDynamicTableName(DynamicTable.AtomInfoSparse,datatype);
		String denseTableName = getDynamicTableName(DynamicTable.AtomInfoDense,datatype);
		
		// it is ok to call atominfo tables here because datatype is
		// set from recursiveDelete() above.
		// note: Sparse table may not necessarily exist. So check first.
		sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '" + sparseTableName + "')" +
				"DELETE FROM " + sparseTableName + "\n" +
				"WHERE AtomID IN (SELECT * FROM #deleteAtoms);\n");
		
		sql.append("DELETE FROM " + denseTableName + "\n" +
				"	WHERE AtomID IN (SELECT * FROM #deleteAtoms);\n");
		sql.append("DROP TABLE #deleteAtoms;\n");
		//System.out.println("Statement: "+sql.toString());
		stmt.execute(sql.toString());
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception grabbing subchildren in GetImmediateSubCollections.");
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception deleting atoms "+atomIDs);
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception deleting atom "+atomID);
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
			ErrorLogger.writeExceptionToLog("SQLServer","Cannot move atoms to the root collection.");
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
			stmt.execute("DELETE FROM InternalAtomOrder WHERE AtomID = " + 
					atomID + " AND CollectionID = " + fromParentID);
			addSingleInternalAtomToTable(atomID, toParentID);
			updateAncestors(getCollection(fromParentID));
			updateAncestors(getCollection(toParentID));
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception updating AtomMembership table.");
			System.err.println("Exception updating membership table");
			e.printStackTrace();
		}
		return true;
	}
	
	public void addSingleInternalAtomToTable(int atomID, int toParentID) {
//		update InternalAtomOrder; have to iterate through all
		// atoms sequentially in order to insert it. 
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT AtomID FROM" +
					" InternalAtomOrder WHERE CollectionID = "+toParentID + " ORDER BY AtomID");
			int order = 1;
			boolean looking = true;
			if (!rs.next())
				stmt.addBatch("INSERT INTO InternalAtomOrder VALUES ("+atomID+","+toParentID+",1)");
			else {
				while (atomID < rs.getInt(1) && rs.next()) {
					order++;
					// jump to spot in db where atomID fits.
				}
				if (atomID != rs.getInt(1)) {
					stmt.addBatch("INSERT INTO InternalAtomOrder VALUES ("+atomID+","+toParentID+","+order+")");
					while(rs.next()) {
						order++;
						stmt.addBatch("UPDATE InternalAtomOrder SET OrderNumber = " + order + " WHERE AtomID = "+rs.getInt(1));
					}
				}
			}
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * adds a move-atom call to a batch statement.
	 * @return true if successful
	 * 
	 * NOT USED AS OF 12/05
	 
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
	   */
	
	/* Atom Batch Init and Execute */
	
	/**
	 * initializes atom batches for moving atoms and adding atoms.
	 */
	public void atomBatchInit() {
		try {
			batchStatement = con.createStatement();
			alteredCollections = new ArrayList<Integer>();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception occurred initializing AtomBatch functionality.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes the current batch
	 */
	public void atomBatchExecute() {
		try {
			batchStatement.executeBatch();
			for (int i = 0; i < alteredCollections.size(); i++)
				updateInternalAtomOrder(getCollection(alteredCollections.get(i)));
			for (int i = 0; i < alteredCollections.size(); i++) 
				updateAncestors(getCollection(alteredCollections.get(i)).getParentCollection());
			batchStatement.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception executing batch atom adds and inserts.");
			System.out.println("Exception executing batch atom adds " +
			"and inserts");
			e.printStackTrace();
		}
	}
	
	/* Get functions for collections and table names */
	
	/**
	 * Gets immediate subcollections for a given collection
	 * @param collections
	 * @return arraylist of collectionIDs
	 */
	public ArrayList<Integer> getImmediateSubCollections(
			ArrayList<Integer> collections)
			{
		ArrayList<Integer> subChildren = new ArrayList<Integer>();
		
		String query = 
			"SELECT DISTINCT ChildID\n" +
			"FROM CollectionRelationships\n" +
			"WHERE ParentID IN (" + join(collections, ",") + ")";
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				subChildren.add(new Integer(rs.getInt("ChildID")));
			}
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception grabbing subchildren in GetImmediateSubCollections.");
			System.err.println("Exception grabbing subchildren:");
			System.err.println(e);
		}
		return subChildren;
			}
	
	/**
	 * returns a collection given a collectionID.
	 */
	public Collection getCollection(int collectionID) {
		Collection collection;
		boolean isPresent = false;
		String datatype = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
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
				ErrorLogger.writeExceptionToLog("SQLServer","Error retrieving collection for collectionID "+collectionID);
				System.err.println("collectionID not created yet!!");
				return null;
			}
			stmt.close();
			
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving collection for collectionID "+collectionID);
			System.err.println("error creating collection");
			e.printStackTrace();
			return null;
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
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error retrieving the collection name for collectionID "+collectionID);
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
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error retrieving the collection comment for collectionID "+collectionID);
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
		String descrip = "";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT Description\n" +
					"FROM Collections\n" +
					"WHERE CollectionID = " + collectionID);
			rs.next();
			descrip = rs.getString("Description");
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error retrieving the collection description for collectionID "+collectionID);
			System.err.println("Error retrieving Collection " +
			"Description.");
			e.printStackTrace();
		}
		return descrip;
	}
	
	/**
	 * gets the collection size
	 */
	public int getCollectionSize(int collectionID) {
		int returnThis = -1;
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT COUNT(AtomID) FROM InternalAtomOrder WHERE CollectionID = " + collectionID);
			boolean test = rs.next();
			assert (test): "error getting atomID count.";
			returnThis = rs.getInt(1);
			stmt.close();
		} catch (SQLException e1) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error retrieving the collection size for collectionID "+collectionID);
			System.err.println("Error selecting the size of " +
			"the table");
			e1.printStackTrace();
		}
		return returnThis;
	}
	
	// returns an arraylist of non-empty collection ids from the original collection of collectionIDs.
	// If you want to include children, then include them in the Set that is passed.
	public ArrayList<Integer> getCollectionIDsWithAtoms(java.util.Collection<Integer> collectionIDs) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		if (collectionIDs.size() > 0) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT DISTINCT CollectionID FROM AtomMembership WHERE CollectionID in (" + join(collectionIDs, ",") + ")");
				
				while (rs.next())
					ret.add(rs.getInt("CollectionID"));
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving collections with atoms.");
				System.err.println("Error retrieving collections with atoms.");
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public static String join(java.util.Collection collection, String delimiter) {
		// Blecch... java should be able to do this itself...
		
		StringBuffer sb = new StringBuffer();
		boolean firstElement = true;
		for (Object o : collection) {
			if (!firstElement)
				sb.append(",");
			sb.append(o);
			firstElement = false;
		}
		
		return sb.toString();
	}
	
	/**
	 * gets all the atoms underneath the given collection.
	 */
	public ArrayList<Integer> getAllDescendedAtoms(Collection collection) {
		ArrayList<Integer> results = new ArrayList<Integer>(1000);
		try {
			ResultSet rs = getAllAtomsRS(collection);
			while(rs.next())
				results.add(new Integer(rs.getInt(1)));
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving children of the collection.");
			System.err.println("Error retrieving children.");
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Gets the parent collection ID using a simple query.
	 */
	public int getParentCollectionID(int collectionID) {
		int parentID = -1;
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT ParentID\n" +
					"FROM CollectionRelationships\n" + 
					"WHERE ChildID = " + collectionID);
			
			// If there is no entry in the table for this collectionID,
			// it doesn't exist, so return false
			if(rs.next())
				parentID = rs.getInt("ParentID");
			
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving parentID of the collection.");
			System.err.println("Error retrieving parentID of the collection.");
			e.printStackTrace();
		}
		
		return parentID;
	}
	
	/**
	 * Returns all collectionIDs beneath the given collection, optionally including it.
	 */
	public Set<Integer> getAllDescendantCollections(int collectionID, boolean includeTopLevel) {
		
		// Construct a set of all collections that descend from this one,
		// including this one.
		ArrayList<Integer> lookUpNext = new ArrayList<Integer>();
		boolean status = lookUpNext.add(new Integer(collectionID));
		assert status : "lookUpNext queue full";
		
		Set<Integer> descCollections = new HashSet<Integer>();
		if (includeTopLevel)
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
			for (Integer col : subChildren)
				if (!descCollections.contains(col)) {
					descCollections.add(col);
					lookUpNext.add(col);
				}
		}
		
		return descCollections;
	}
	
	/**
	 * This method has CHANGED as of 11/15/05.
	 * It used to recurse through all the collection's subcollections
	 * and create a temporary table of all the atoms.  Now, it only needs
	 * a simple query from the InternalAtomOrder table. Note that the  
	 * InternalAtomOrderTable must be completely updated for this to work.
	 * 
	 * It also used to return an InstancedResultSet, which is not just
	 * a plain ResultSet.
	 * @param collection
	 * @return - resultset
	 */
	public ResultSet getAllAtomsRS(Collection collection)
	{
		ResultSet returnThis = null;
		try {
			Statement stmt = con.createStatement();
			returnThis = stmt.executeQuery("SELECT AtomID " +
					"FROM InternalAtomOrder WHERE CollectionID = " + 
					collection.getCollectionID() + " ORDER BY AtomID");
			//NOTE: Atoms are already ordered by AtomID, and this might be
			// redundant.  If needed, you can take this out to optimize and 
			// only order when needed in each method. - AR
			//stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving children of the collection.");
			e.printStackTrace();
		}
		return returnThis;
	}
	
	/**
	 * gets an arraylist of ATOFMS Particles for the given collection.
	 * Unique to ATOFMS data - not used anymore except for unit tests.  
	 *
	 *DEPRECIATED 12/05 - AR
	 *
	 public ArrayList<GeneralAtomFromDB> getCollectionParticles(Collection collection)
	 {
	 ArrayList<GeneralAtomFromDB> particleInfo = 
	 new ArrayList<GeneralAtomFromDB>(1000);
	 try {
	 ResultSet rs = getAllAtomsRS(collection);
	 DateFormat dFormat = 
	 new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	 while(rs.next()) {
	 System.out.println(rs.getInt(1));
	 particleInfo.add(new GeneralAtomFromDB(rs.getInt(1),this));
	 }
	 } catch (SQLException e) {
	 System.err.println("Error collecting particle " +
	 "information:");
	 e.printStackTrace();
	 }
	 return particleInfo;
	 }
	 */
	
	/**
	 * update particle table returns a vector<vector<Object>> for the gui's 
	 * particles table.  All items are taken from AtomInfoDense, and all 
	 * items are strings except for the atomID, which is used to produce 
	 * graphs.
	 * 
	 * This will only return 1000 particles at a time.
	 */
	public Vector<Vector<Object>> updateParticleTable(Collection collection, Vector<Vector<Object>> particleInfo, int lowIndex, int highIndex) {
		assert (highIndex - lowIndex < 1000) : "trying to collect over 1000 particles at a time!";
		particleInfo.clear();
		int numberColumns = getColNames(collection.getDatatype(),DynamicTable.AtomInfoDense).size();
		// This isn't a registered datatype... oops
		if (numberColumns == 0)
			return null;
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".* " +
					"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) +
					", InternalAtomOrder\n" +
					"WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = InternalAtomOrder.AtomID\n" +
					"AND InternalAtomOrder.CollectionID = " + collection.getCollectionID() +
					"AND InternalAtomOrder.OrderNumber\n" +
					"BETWEEN " + lowIndex + " AND " + highIndex + "\n" +
			"ORDER BY InternalAtomOrder.OrderNumber");
			
			while(rs.next())
			{
				Vector<Object> vtemp = new Vector<Object>(numberColumns);
				vtemp.add(rs.getInt(1)); // Integer for atomID
				for (int i = 2; i <= numberColumns; i++) 
					vtemp.add(rs.getString(i));
				particleInfo.add(vtemp);
			}
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception collecting particle information.");
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
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception getting the datatype for atom "+atomID);
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
			Statement stmt = con.createStatement();
			stmt.executeUpdate(
					"UPDATE Collections\n" +
					"SET Description = '" + description + "'\n" +
					"WHERE CollectionID = " + collection.getCollectionID());
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception updating collection description.");
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception finding the maximum atomID.");
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
		
		if (! collection.getDatatype().equals("ATOFMS")) {
			throw new RuntimeException(
					"trying to export the wrong datatype for MSAnalyze: " 
					+ collection.getDatatype());
		}
		DateFormat dFormat = null;
		Date startTime = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		} catch (ClassNotFoundException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error loading ODBC bridge driver.");
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
					"	FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ", InternalAtomOrder" +
					"	WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + 
					".AtomID = InternalAtomOrder.AtomID AND InternalAtomOrder.CollectionID = " + collection.getCollectionID() + ")\n" +
					
					"UPDATE #ParticlesToExport\n" +
					"SET NumPeaks = \n" +
					"	(SELECT COUNT(AtomID)\n" +
					"		FROM " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + "\n" +
					"			WHERE " + getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID = #ParticlesToExport.AtomID),\n" +
					"TotalPosIntegral = \n" +
					"	(SELECT SUM (PeakArea)\n" +
					"		FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + "\n" +
					"			WHERE " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".AtomID = #ParticlesToExport.AtomID\n" +
					"			AND " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".PeakLocation >= 0),\n" +
					"TotalNegIntegral =\n" +
					"	(SELECT SUM (PeakArea)\n" +
					"		FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + "\n" +
					"			WHERE " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".AtomID = #ParticlesToExport.AtomID\n" +
					"			AND " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".PeakLocation < 0)\n"
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
			
			ResultSet rs = stmt.executeQuery(
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
			
			
			// get the values for the particles table
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
						(new File(rs.getString("Filename"))).getName() +
						"', '" + 
						dFormat.format(new Date(
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
					"\n" +
					"\n" +
					"INSERT INTO #PeaksToExport\n" +
					"(OrigFilename, PeakLocation, PeakArea, " +
					"RelPeakArea, PeakHeight)\n" +
					"(SELECT OrigFilename, PeakLocation, " +
					"PeakArea, RelPeakArea, PeakHeight\n" +
					"FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ", InternalAtomOrder, " 
					+ getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + "\n" +
					"	WHERE (" + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".AtomID = InternalAtomOrder.AtomID)\n" +
					"   AND (InternalAtomOrder.CollectionID = " + collection.getCollectionID() + ")" +
					"	AND (" + getDynamicTableName(DynamicTable.AtomInfoSparse,collection.getDatatype()) + ".AtomID = " +
					getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype()) + ".AtomID)" +
			")\n");
			
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
						(new File(rs.getString(1))).getName() + "', " +
						rs.getFloat(2) + ", " + rs.getInt(3) + 
						", " +
						rs.getFloat(4) + ", " + rs.getInt(5) +
				")");
			}
			
			odbcStmt.executeBatch();
			stmt.execute("DROP TABLE #PeaksToExport");
			odbcCon.close();
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception exporting to MSAccess database.");
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
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"Select *\n" +
					"FROM AtomMembership\n" +
					"WHERE AtomID = " + AtomID + 
					" AND CollectionID = " + isMemberOf);
			
			if (rs.next())
			{
				rs.close();
				return true;
			}
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception checking atom's parentage.");
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
			ErrorLogger.writeExceptionToLog("SQLServer","Error rebuilding SQL Server database.");
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
			in = new Scanner(new File("SQLServerRebuildDatabase.txt"));
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
				//System.out.println(query);
				con.createStatement().executeUpdate(query);
			}
			
		} catch (IOException e) {
			System.err.println("IOException occurred when rebuilding the database.");
			return true;
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","Error rebuilding SQL Server database.");
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
			ErrorLogger.writeExceptionToLog("SQLServer","Error dropping SQL Server database.");
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
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(
					"SELECT * FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse,datatype) + " WHERE AtomID = " +
					atomID);
			//stmt.close();
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
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving peaks.");
			System.err.println("Error using the result set");
			e.printStackTrace();
		}
		return returnThis;
	}
	
	/* Cursor classes */
	private class ClusteringCursor implements CollectionCursor {
		protected InstancedResultSet irs;
		protected ResultSet rs;
		protected Statement stmt = null;
		private Collection collection;
		private ClusterInformation cInfo;
		private String datatype;
		
		public ClusteringCursor(Collection collection, ClusterInformation cInfo) {
			super();
			this.collection = collection;
			datatype = collection.getDatatype();
			this.cInfo = cInfo;
			rs = getAllAtomsRS(collection);
		}
		
		public boolean next() {
			try {
				return rs.next();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a clustering cursor.");
				System.err.println("Error checking the " +
						"bounds of " +
				"the ResultSet.");
				e.printStackTrace();
				return false;
			}
		}
		
		public ParticleInfo getCurrent() {
			
			ParticleInfo particleInfo = new ParticleInfo();
			try {
				particleInfo.setID(rs.getInt(1));
				particleInfo.setBinnedList(getPeakListfromAtomID(rs.getInt(1)));
			}catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a clustering cursor.");
				System.err.println("Error retrieving the " +
				"next row");
				e.printStackTrace();
				return null;
			}
			return particleInfo; 
		}
		
		public void close() {
			try {
				rs.close();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a clustering cursor.");
				e.printStackTrace();
			}
		}
		
		public void reset() {
			try {
				rs.close();
				rs = getAllAtomsRS(collection);
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a clustering cursor.");
				System.err.println("Error resetting a " +
						"resultset " +
				"for that collection:");
				e.printStackTrace();
			}	
		}
		
		public ParticleInfo get(int i) throws NoSuchMethodException {
			throw new NoSuchMethodException("Not implemented in disk based cursors.");
		}
		
		public BinnedPeakList getPeakListfromAtomID(int id) {
			BinnedPeakList peakList;
			if (cInfo.normalize)
				peakList = new BinnedPeakList(new Normalizer());
			else
				peakList = new BinnedPeakList(new DummyNormalizer());
			try {
				ResultSet listRS;
				Statement stmt2 = con.createStatement();
				if (cInfo.automatic) {
					listRS = stmt2.executeQuery("SELECT " + join(cInfo.valueColumns, ",") +
							" FROM " + getDynamicTableName(DynamicTable.AtomInfoDense, datatype) +
							" WHERE AtomID = " + id);
					
					listRS.next();
					for (int i = 1; i <= cInfo.valueColumns.size(); i++) {
						//TODO: this is a hack; fix.
						try {
							peakList.addNoChecks(i, listRS.getFloat(i));
						} catch (SQLException e) {
							peakList.addNoChecks(i, listRS.getInt(i));
						}
					}
				}
				else {
					listRS = stmt2.executeQuery("SELECT " + 
							cInfo.keyColumn + ", " + cInfo.valueColumns.iterator().next() +  
							" FROM " + getDynamicTableName(DynamicTable.AtomInfoSparse, datatype) + 
							" WHERE AtomID = " + id);
					while (listRS.next()) 
						peakList.add(listRS.getFloat(1), listRS.getFloat(2));
				} 
				stmt2.close();
				listRS.close();
			}catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a clustering cursor.");
				System.err.println("Error retrieving the " +
				"next row");
				e.printStackTrace();
				return null;
			}
			return peakList;		
		}
		
		public boolean isNormalized() {
			return cInfo.normalize;
		}
	}
	
	/**
	 * Memory Clustering Cursor.  Returns binned peak info for a given atom,
	 * info kept in memory.
	 */
	private class MemoryClusteringCursor extends ClusteringCursor {
		InfoWarehouse db;
		boolean firstPass = true;
		int position = -1;
		
		ArrayList<ParticleInfo> storedInfo = null;
		
		public MemoryClusteringCursor(Collection collection, ClusterInformation cInfo) {
			super (collection, cInfo);
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
			return super.getPeakListfromAtomID(atomID);
		}
	}
	
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
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				partInfRS = stmt.executeQuery("SELECT "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID, OrigFilename, ScatDelay," +
						" LaserPower, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+", InternalAtomOrder WHERE" +
						" InternalAtomOrder.CollectionID = "+collection.getCollectionID() +
						" AND "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID = InternalAtomOrder.AtomID");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void reset()
		{		
			try {
				partInfRS.close();
				partInfRS = stmt.executeQuery("SELECT "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID, OrigFilename, ScatDelay," +
						" LaserPower, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+", InternalAtomOrder WHERE" +
						" InternalAtomOrder.CollectionID = "+collection.getCollectionID() +
						" AND "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID = InternalAtomOrder.AtomID");
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a AtomInfoOnly cursor.");
				System.err.println("SQL Error resetting " +
				"cursor: ");
				e.printStackTrace();
			}
		}
		
		public boolean next() {
			try {
				return partInfRS.next();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a AtomInfoOnly cursor.");
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
				ParticleInfo particleInfo = new ParticleInfo();
				particleInfo.setParticleInfo(
						new ATOFMSAtomFromDB(
								partInfRS.getInt(1),
								partInfRS.getString(2),
								partInfRS.getInt(3),
								partInfRS.getFloat(4), 
								new Date(partInfRS.getTimestamp(5).
										getTime())));
				particleInfo.setID(particleInfo.getParticleInfo().getAtomID());
				return particleInfo; 
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a AtomInfoOnly cursor.");
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
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a AtomInfoOnly cursor.");
				e.printStackTrace();
			}
		}
		
		public ParticleInfo get(int i) 
		throws NoSuchMethodException {
			throw new NoSuchMethodException("Not implemented in disk based cursors.");
		}
		
		public BinnedPeakList getPeakListfromAtomID(int atomID) {
			BinnedPeakList peakList = new BinnedPeakList(new Normalizer());
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
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a AtomInfoOnly cursor.");
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
			try {
				stmt = con.createStatement();
				partInfRS = stmt.executeQuery("SELECT "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID, OrigFilename, ScatDelay," +
						" LaserPower, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+", InternalAtomOrder WHERE" +
						" InternalAtomOrder.CollectionID = "+collection.getCollectionID() +
						" AND "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID = InternalAtomOrder.AtomID" +
						" AND " + where);
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a SQL cursor.");
				e.printStackTrace();
			}
		}
		
		public void close() {
			try {
				stmt.close();
				super.close();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a SQL cursor.");
				e.printStackTrace();
			}
		}
		public void reset()
		{
			
			try {
				partInfRS.close();
				partInfRS = stmt.executeQuery("SELECT "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID, OrigFilename, ScatDelay," +
						" LaserPower, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+", InternalAtomOrder WHERE" +
						" InternalAtomOrder.CollectionID = "+collection.getCollectionID() +
						" AND "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID = InternalAtomOrder.AtomID" +
						" AND " + where);
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a SQL cursor.");
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
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a Peak cursor.");
				e.printStackTrace();
			}
		}
		
		public ParticleInfo getCurrent()
		{
			// This should get overridden in other classes,
			//however, its results from here should be used.
			
			ParticleInfo pInfo = new ParticleInfo();
			PeakList pList = new PeakList();
			ArrayList<Peak> aPeakList = new ArrayList<Peak>();
			try {
				pList.setAtomID(partInfRS.getInt(1));
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
				pInfo.setID(pList.getAtomID());
				peakRS.close();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a Peak cursor.");
				e.printStackTrace();
			}
			
			return pInfo;
		}
		
		public void close(){
			try {
				peakRS.close();
				super.close();
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a Peak cursor.");
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
			BinnedPeakList bPList = new BinnedPeakList(new Normalizer());
			
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
	 * 
	 * Updated 12/05 at http://www.sqlteam.com/item.asp?ItemID=217
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
				String createTable = "CREATE TABLE #TempRand (AtomID int NOT NULL,RandNum float NULL)";
				String insertAtoms = "INSERT #TempRand (AtomID) SELECT AtomID " 
					+ "FROM InternalAtomOrder WHERE CollectionID = " + col.getCollectionID()+ " ORDER BY AtomID";
				String randomize = "DECLARE Randomizer CURSOR" +
				" FOR SELECT RandNum FROM #TempRand" +
				" OPEN Randomizer"+
				" FETCH NEXT FROM Randomizer" +
				" WHILE @@Fetch_Status != -1" +
				" BEGIN UPDATE #TempRand SET RandNum = rand()" +
				" WHERE CURRENT OF Randomizer"+
				" FETCH NEXT FROM Randomizer" +
				" END"+
				" CLOSE Randomizer"+
				" DEALLOCATE Randomizer";
				String cursorQuery = "SELECT "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID, OrigFilename, ScatDelay," +
				" LaserPower, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+", #TempRand WHERE " +
				getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID = #TempRand.AtomID"+
				" ORDER BY RandNum";
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
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a Randomized cursor.");
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
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a Randomized cursor.");
				e.printStackTrace();
			}
		}
		public void reset()
		{
			
			try {
				partInfRS.close();
				String cursorQuery = "SELECT "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID, OrigFilename, ScatDelay," +
				" LaserPower, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+", #TempRand WHERE " +
				getDynamicTableName(DynamicTable.AtomInfoDense,collection.getDatatype())+".AtomID = #TempRand.AtomID"+
				" ORDER BY RandNum";
				partInfRS = stmt.executeQuery(cursorQuery);
			} catch (SQLException e) {
				ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving data through a Randomized cursor.");
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
			return new BinnedPeakList(new Normalizer());
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
	 * get method for ClusteringCursor.
	 */
	public CollectionCursor getClusteringCursor(Collection collection, ClusterInformation cInfo)
	{
		return new ClusteringCursor(collection, cInfo);
	}
	
	/**
	 * Seeds the random number generator.
	 */
	public void seedRandom(int seed) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeQuery("SELECT RAND(" + seed + ")\n");
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception seeding the random number generator.");
			System.err.println("Error in seeding random number generator.");		
			e.printStackTrace();
		}
	}
	
	/**
	 *  Used for testing random number seeding 
	 */
	public double getNumber() {
		double num = -1;
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT RAND()");
			rs.next();
			num = rs.getDouble(1);
			System.out.println(num);
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception testing the random number seeding.");
			System.err.println("Error in generating single number.");
			e.printStackTrace();
		}
		return num;
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
				temp.add(rs.getString(1).substring(1,rs.getString(1).length()-1));
				temp.add(rs.getString(2));
				colNames.add(temp);
			}
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception retrieving column names.");
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
			
			stmt.close();
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
					"INSERT ValueMaps (Name) Values('" + removeReservedCharacters(name) + "') " +
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
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception inserting new value map range.");
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
			stmt.close();
		}
		catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving value maps");
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
			stmt.close();
		}
		catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving value map ranges");
			System.err.println("Error getting value map ranges from database.");
			e.printStackTrace();
		}
		
		return valueMapRanges;
	}
	
	public int applyMap(String mapName, Vector<int[]> map, Collection collection) {
		int oldCollectionID = collection.getCollectionID();
		String dataType = collection.getDatatype();
		
		int newCollectionID = createEmptyCollection(dataType, oldCollectionID, mapName, "", "");
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
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception creating new mapped collection");
			System.err.println("Error creating new mapped collection.");
			e.printStackTrace();
		}
		
		return newCollectionID;
	}
	
	/** 
	 * Gets the maximum and minimum dates of the atoms in all collections 
	 * about to be aggregated.
	 */
	public void getMaxMinDateInCollections(Collection[] collections, Calendar minDate, Calendar maxDate) {
		String cIDs = "";
		ArrayList<String> infoDenseNames = new ArrayList<String>();
		for (int i = 0; i < collections.length;i++) {
			cIDs += collections[i].getCollectionID();
			if (i != collections.length-1)
				cIDs += ",";
			String infoDenseStr = collections[i].getDatatype()+"AtomInfoDense";
			if (!infoDenseNames.contains(infoDenseStr)) {
				//System.out.println(infoDenseStr);
				infoDenseNames.add(infoDenseStr);
			}
		}
		assert (infoDenseNames.size() > 0):"no datatypes defined.";
		
		try{
			StringBuilder sqlStr = new StringBuilder();
			sqlStr.append("CREATE INDEX iao_index ON InternalAtomOrder (CollectionID);\n"+
		"SELECT MAX(Time) as MaxTime, MIN(Time) as MinTime\nFROM(\n");
			sqlStr.append("SELECT AtomID, Time FROM "+ infoDenseNames.get(0)+"\n");
			for (int i = 1; i < infoDenseNames.size(); i++)
				sqlStr.append("UNION SELECT AtomID, Time FROM " + infoDenseNames.get(i)+"\n");
			sqlStr.append(") AID, InternalAtomOrder IAO\n"+
					"WHERE IAO.CollectionID in ("+cIDs+")\n" +
							"AND AID.AtomID = IAO.AtomID;\n");
			sqlStr.append("DROP INDEX InternalAtomOrder.iao_index;\n");
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sqlStr.toString());
			if (rs.next()) {
				Timestamp minT = rs.getTimestamp("MinTime");
				if (!rs.wasNull())
					minDate.setTime(minT);
				
				Timestamp maxT = rs.getTimestamp("MaxTime");
				if (!rs.wasNull())
					maxDate.setTime(maxT);
			}
			
			rs.close();
			stmt.close();
		} catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving max time for collections.");
			System.err.println("SQL exception retrieving max time for collections");
			e.printStackTrace();
		}
	}
	
	/**
	 * Create an index on some part of AtomInfoDense for a datatype.  Possibly
	 * useful if you're going to be doing a *whole lot* of queries on a
	 * particular field or set of fields.  For syntax in the fieldSpec, look
	 * at an SQL reference.  If it's just one field, just put the name of the
	 * column there.
	 * 
	 * @return true if the index was successfully created, false otherwise.
	 */
	public boolean createIndex(String dataType, String fieldSpec) {
		String table = getDynamicTableName(DynamicTable.AtomInfoDense, dataType);
		
		String indexName = "index_" + fieldSpec.replaceAll("[^A-Za-z0-9]","");
		String s = "CREATE INDEX " + indexName + " ON " + table 
			+ " (" + fieldSpec + ")";
		
		try {
			Statement stmt = con.createStatement();
			boolean ret = stmt.execute(s);
			stmt.close();
			return true;
		} catch (SQLException e) {
			System.err.println(e.toString());
			return false;
		}
	}
	
	/**
	 * Returns a list of indexed columns in an AtomInfoDense table.
	 */
	public Set<String> getIndexedColumns(String dataType) throws SQLException {
		Set<String> indexed = new HashSet<String>();
		
		String table = this.getDynamicTableName(DynamicTable.AtomInfoDense, dataType);
		
		Statement stmt = con.createStatement();
		ResultSet r = stmt.executeQuery("EXEC sp_helpindex " + table);
		
		String[] tmp;
		while (r.next()) {
			tmp = r.getString("index_keys").split(", ");
			for (int i = 0; i < tmp.length; i++) {
				indexed.add(tmp[i]);
			}
		}
		
		r.close();
		stmt.close();
		
		return indexed;
	}
	
	/**
	 * Creates a table of all the appropriate atomIDs and the binned
	 * times.  If the collection is the one that the aggregation is based on,
	 * the table is just a copy of the original one.
	 * 
	 * NOTE:  ALSO HAS A LIST OF ALL ATOM IDS IN COLLECTION!
	 */
	public void createTempAggregateBasis(Collection c, Collection basis) {
		// grabbing the times for all subcollectionIDs
		try {
			StringBuilder tempTable = new StringBuilder();
			Statement stmt = con.createStatement();
			tempTable.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'TEMPTimeBins')\n"+
					"DROP TABLE TEMPTimeBins;\n");
			tempTable.append("CREATE TABLE TEMPTimeBins (AtomID INT, BinnedTime datetime, PRIMARY KEY (AtomID));\n");
			// if aggregation is based on this collection, copy table
			if (c.getCollectionID() == basis.getCollectionID()) {	
				System.out.println("copying table...");
				tempTable.append("INSERT TEMPTimeBins (AtomID, BinnedTime)\n"+
						"SELECT AID.AtomID, [Time] FROM "+getDynamicTableName(DynamicTable.AtomInfoDense, c.getDatatype())+" AID,\n"+
						"InternalAtomOrder IAO \n"+
						"WHERE IAO.AtomID = AID.AtomID\n" +
						"AND CollectionID = "+c.getCollectionID()+"\n"+
				"ORDER BY Time;\n");
			}
			// else, perform a join merge on the two collections.
			else {
				Statement stmt1 = con.createStatement();
				Statement stmt2 = con.createStatement();
				// get distinct times from basis collection
				ResultSet basisRS = stmt1.executeQuery("SELECT DISTINCT [Time] \n" +
						"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense, basis.getDatatype()) + " AID,\n" +
						"InternalAtomOrder IAO \n"+
						"WHERE IAO.AtomID = AID.AtomID\n" +
						"AND CollectionID = "+basis.getCollectionID()+"\n"+
				"ORDER BY Time;\n");
				// get all times from collection to bin.
				ResultSet collectionRS = stmt2.executeQuery("SELECT AID.AtomID, [Time] \n" +
						"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense, c.getDatatype()) + " AID,\n" +
						"InternalAtomOrder IAO \n"+
						"WHERE IAO.AtomID = AID.AtomID\n" +
						"AND CollectionID = "+c.getCollectionID()+"\n"+
				"ORDER BY Time;\n");
				
				// initialize first values:
				Timestamp nextBin = null;
				boolean test = basisRS.next();
				assert (test) : "no basis times for collection!";
				Timestamp currentBin = basisRS.getTimestamp(1);
				collectionRS.next();
				int atomID = collectionRS.getInt(1);
				Timestamp collectionTime = collectionRS.getTimestamp(2);
				boolean next = true;
				
				// We skip the times before the first bin.
				while (collectionTime.compareTo(currentBin) < 0) {
					next = collectionRS.next();
					if (!next)
						break;
					else { 
						atomID = collectionRS.getInt(1);
						collectionTime = collectionRS.getTimestamp(2);
					}
				}
				//	while the next time bin is legal...
				
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
					while (next && basisRS.next()) { 
						nextBin = basisRS.getTimestamp(1);
						// while collectionTime is within bin, insert it in table.
						while (collectionTime.compareTo(nextBin) < 0) {
							bulkFile.println(atomID+","+currentBin.toString());
							next = collectionRS.next();
							if (!next)
								break;
							else { 
								atomID = collectionRS.getInt(1);
								collectionTime = collectionRS.getTimestamp(2);
							}	
						}
						currentBin = nextBin;
					}
					bulkFile.close();
					tempTable.append("BULK INSERT TEMPTimeBins\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH (FIELDTERMINATOR=',');\n");
				} else {
					int counter = 0;
					while (next && basisRS.next()) { 
						nextBin = basisRS.getTimestamp(1);
						// while collectionTime is within bin, insert it in table.
						while (collectionTime.compareTo(nextBin) < 0) {
							tempTable.append("INSERT INTO TEMPTimeBins VALUES ("+atomID+",'"+currentBin+"');\n");
							counter++;
							next = collectionRS.next();
							if (!next)
								break;
							else { 
								atomID = collectionRS.getInt(1);
								collectionTime = collectionRS.getTimestamp(2);
							}	
							if (counter > 500) {
								stmt.execute(tempTable.toString());
								counter = 0;
								tempTable = new StringBuilder();
							}
						}
						currentBin = nextBin;
					}
				}		
//				We skip the times after the last bin.
				stmt1.close();
				stmt2.close();
			}
			stmt.execute(tempTable.toString());
			stmt.close();	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Assign each Atom in c to a 'bin' and  store this information into the TEMPTimeBins table
	 * This method uses the start, end and interval parameters and 
	 * builds the SQL statement and then creates the TEMPTimeBins Table;
	 * 
	 * NOTE:  ALSO HAS A LIST OF ALL ATOM IDS IN COLLECTION!
	 */
	public void createTempAggregateBasis(Collection c, Calendar start, Calendar end, Calendar interval) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar increment = (Calendar) start.clone();	
		int counter = 0;
		try {
			StringBuilder tempTable = new StringBuilder();
			Statement stmt = con.createStatement();
			Statement stmt1 = con.createStatement();
			tempTable.append("CREATE TABLE TEMPTimeBins (AtomID INT, BinnedTime datetime, PRIMARY KEY (AtomID));\n");
			counter++;
			// get all times from collection to bin.
			ResultSet collectionRS = stmt1.executeQuery("SELECT AID.AtomID, [Time] \n" +
					"FROM " + getDynamicTableName(DynamicTable.AtomInfoDense, c.getDatatype()) + " AID,\n" +
					"InternalAtomOrder IAO \n"+
					"WHERE IAO.AtomID = AID.AtomID\n" +
					"AND CollectionID = "+c.getCollectionID()+"\n"+
				"ORDER BY Time;\n");
			
			// initialize first values:
			collectionRS.next();
			int atomID = collectionRS.getInt(1);
			Timestamp collectionTime = collectionRS.getTimestamp(2);
			Date basisTime = increment.getTime();
			Date nextTime = null;
			boolean next = true; 
			
			// if there are times before the first bin, skip them.
			while (basisTime.compareTo((Date)collectionTime)> 0) {
				next = collectionRS.next();
				if (!next)
					break;
				else {
					atomID = collectionRS.getInt(1);
					collectionTime = collectionRS.getTimestamp(2);
				}
			}
//			while the next time bin is legal...
//			 Only bulk insert if client and server are on the same machine...
			if (url.equals("localhost")) {
				String tempFilename = tempdir + File.separator + "bulkfile.txt";
				PrintWriter bulkFile = null;
				try {
					bulkFile = new PrintWriter(new FileWriter(tempFilename));
				} catch (IOException e) {
					System.err.println("Trouble creating " + tempFilename);
					e.printStackTrace();
				}
				while (next) {
					increment.add(Calendar.HOUR,   interval.get(Calendar.HOUR));
					increment.add(Calendar.MINUTE, interval.get(Calendar.MINUTE));
					increment.add(Calendar.SECOND, interval.get(Calendar.SECOND));
					nextTime = increment.getTime();
					while (next && nextTime.compareTo((Date)collectionTime)> 0) {
							bulkFile.println(+atomID+","+dateFormat.format(basisTime));
							next = collectionRS.next();
						if (!next)
							break;
						atomID = collectionRS.getInt(1);
						collectionTime = collectionRS.getTimestamp(2);	
					}	
					if (nextTime.compareTo(end.getTime()) > 0)
						next = false;
					else 
						basisTime = nextTime;
				}	
				bulkFile.close();
				tempTable.append("BULK INSERT TEMPTimeBins\n" +
						"FROM '" + tempFilename + "'\n" +
				"WITH (FIELDTERMINATOR=',');\n");
			} else {
				while (next) {
					increment.add(Calendar.HOUR,   interval.get(Calendar.HOUR));
					increment.add(Calendar.MINUTE, interval.get(Calendar.MINUTE));
					increment.add(Calendar.SECOND, interval.get(Calendar.SECOND));
					nextTime = increment.getTime();
					while (next && nextTime.compareTo((Date)collectionTime)> 0) {
							tempTable.append("INSERT INTO TEMPTimeBins VALUES ("+atomID+",'"+dateFormat.format(basisTime)+"');\n");
							counter++;
							next = collectionRS.next();
						if (!next)
							break;
						atomID = collectionRS.getInt(1);
						collectionTime = collectionRS.getTimestamp(2);	
						if (counter > 1000) {
							stmt.execute(tempTable.toString());
							counter = 0;
							tempTable = new StringBuilder();
						}
					}	
					if (nextTime.compareTo(end.getTime()) > 0)
						next = false;
					else 
						basisTime = nextTime;
				}	
			}	
			
			// if there are still more times, skip them.

			stmt.execute(tempTable.toString());
			stmt1.close();
			stmt.close();	
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception creating aggregate basis temp table");
			System.err.println("SQL exception creating aggregate basis temp table");
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes the most recent temp aggregate basis table.
	 */
	public void deleteTempAggregateBasis() {
		try {
			Statement stmt = con.createStatement();
			stmt.execute("DROP TABLE TEMPTimeBins;\n");
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception deleting aggregate basis temp table");
			System.err.println("SQL exception deleting aggregate basis temp table");
			e.printStackTrace();
		}
	}
	
	/**
	 * This creates the empty collections and the queries and sends it to fillAtomsFromMemory 
	 * method.  This only handles ATOFMS and TIME SERIES data, which will need to
	 * be changed.
	 * 
	 * @return the list of CIDs that need to be updated in InternalAtomORder.
	 */
	public void createAggregateTimeSeries(ProgressBarWrapper progressBar, int rootCollectionID, Collection curColl, int[] mzValues) {
		int collectionID = curColl.getCollectionID();
		String collectionName = curColl.getName();
		AggregationOptions options = curColl.getAggregationOptions();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
		Statement stmt = con.createStatement();
		StringBuilder sql = new StringBuilder();
		// Create and Populate TEMPatoms table with appropriate information.
		/* IF DATATYPE IS ATOFMS */
		if (curColl.getDatatype().equals("ATOFMS")) {	
			if (mzValues == null) {
				ErrorLogger.writeExceptionToLog("SQLServer","Error! Collection: " + collectionName + " doesn't have any peak data to aggregate!");
				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate!");
				System.err.println("Collections need to overlap times in order to be aggregated.");
				return;
			} 
			
			int newCollectionID = createEmptyCollection("TimeSeries", rootCollectionID, collectionName, "", "");
			if (mzValues.length == 0) {
				// do nothing!  allow emptiness.
//				ErrorLogger.writeExceptionToLog("SQLServer","Note: Collection: " + collectionName + " doesn't have any peak data to aggregate!");
//				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate!");
//				System.err.println("Collections need to overlap times in order to be aggregated.");
			} else {
				//create and insert MZ Values into temporary TEMPmz table.
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'TEMPmz')\n"+
				"DROP TABLE TEMPmz;\n");
				sql.append("CREATE TABLE TEMPmz (Value INT);\n");
				// Only bulk insert if client and server are on the same machine...
				if (url.equals("localhost")) {
					String tempFilename = tempdir + File.separator + "MZbulkfile.txt";
					PrintWriter bulkFile = null;
					try {
						bulkFile = new PrintWriter(new FileWriter(tempFilename));
					} catch (IOException e) {
						System.err.println("Trouble creating " + tempFilename);
						e.printStackTrace();
					}
					for (int i = 0; i < mzValues.length; i++){
						bulkFile.println(mzValues[i]);
					}
					bulkFile.close();
					sql.append("BULK INSERT TEMPmz\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH (FIELDTERMINATOR=',');\n");
				} else {
					for (int i = 0; i < mzValues.length; i++){
						sql.append("INSERT INTO TEMPmz VALUES("+mzValues[i]+");\n");
					}
				}	
				stmt.execute(sql.toString());
				sql = new StringBuilder();
				//	create TEMPatoms table
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'TEMPatoms')\n"+
				"DROP TABLE TEMPatoms;\n");
				sql.append("CREATE TABLE TEMPatoms (" +
						"NewAtomID int, \n" +
				" Time DateTime, \n" +
				" MZLocation int, \n " +
				" Value real, \n" +
				" PRIMARY KEY(NewAtomID) );\n");
				//This commented-out code does all of the joins in SQL.  This works, but is extremely slow
				// when there are a lot of m/z values to aggregate.  Because of this, the 
				// JOIN TEMPmz MZ on (abs(AIS.PeakLocation - MZ.Value) < "+options.peakTolerance+")
				// join is done in java below, using special information about the data
				
				// went back to Greg's JOIN methodology, but retained TEMPmz table, which speeds it up.
				// collects the sum of the Height/Area over all atoms at a given Time and for a specific m/z 
				/*sql.append("insert TEMPatoms (Time, MZLocation, Value) \n" +
						"SELECT BinnedTime, MZ.Value AS Location,"+options.getGroupMethodStr()+"(PeakHeight) AS Value \n"+
						"FROM TEMPTimeBins TB\n" +
						"JOIN ATOFMSAtomInfoSparse AIS on (TB.AtomID = AIS.AtomID)\n"+
						"JOIN TEMPmz MZ on (abs(AIS.PeakLocation - MZ.Value) < "+options.peakTolerance+")\n"+
						"GROUP BY BinnedTime,MZ.Value\n"+
						"ORDER BY Location, BinnedTime;\n");
				*/
				Statement chargesStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet charges = chargesStmt.executeQuery("SELECT * FROM TEMPmz ORDER BY Value;\n");
				
				Statement peaksStmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet peaks = peaksStmt.executeQuery(
						"SELECT BinnedTime, AIS.PeakLocation AS MZLocation, "
							+ options.getGroupMethodStr() + "(PeakArea) AS PeakArea\n"
						+ "FROM TEMPTimeBins TB\n"
						+ "JOIN ATOFMSAtomInfoSparse AIS on (TB.AtomID = AIS.AtomID)\n"
						+ "GROUP BY BinnedTime,AIS.PeakLocation\n"
						+ "ORDER BY AIS.PeakLocation, BinnedTime;\n");
				
				//The following is effectively
				//JOIN TEMPmz MZ on (abs(AIS.PeakLocation - MZ.Value) < "+options.peakTolerance+")
				// using special information about the data
				int nextID = getNextID();
				if (url.equals("localhost")) {
					String tempFilename = tempdir + File.separator + "Atomsbulkfile.txt";
					PrintWriter bulkFile = null;
					try {
						bulkFile = new PrintWriter(new FileWriter(tempFilename));
					} catch (IOException e) {
						System.err.println("Trouble creating " + tempFilename);
						e.printStackTrace();
					}
					if (charges.next()) {
						boolean quit = false;
						int mzBin = charges.getInt(1);
						while (!quit && peaks.next()) {
							String dateTime = peaks.getString(1);
							double peakLocation = peaks.getDouble(2);
							double value = peaks.getDouble(3);
							double error = mzBin - peakLocation;
							//If the current peak is past the MZ number, skip ahead
							if(Math.abs(error) > options.peakTolerance){
								if(error < 0){
									while(error < 0 && Math.abs(error) > options.peakTolerance) {
										if(!charges.next()) break;
										mzBin = charges.getInt(1);
										error = mzBin - peakLocation;
									}
								}
							}
							//otherwise, add the element it's good
							if (Math.abs(error) <= options.peakTolerance) {
								try{
								bulkFile.println(""+ nextID + "," + 
										formatter.format(parser.parse(dateTime)) + "," + mzBin + "," + value);
								nextID++;
								}catch(ParseException e){
									System.err.println("Problem Inserting atoms");
									System.exit(1);
								}
							}
						}
					}
					bulkFile.close();
					sql.append("BULK INSERT TEMPatoms\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH " +
					"	(FIELDTERMINATOR=','," +
					"	 ROWTERMINATOR='\n');\n");
					
					
				}else{
					if (charges.next()) {
						boolean quit = false;
						double mzBin = charges.getDouble(1);
						while (!quit && peaks.next()) {
							String dateTime = peaks.getString(1);
							double peakLocation = peaks.getDouble(2);
							double value = peaks.getDouble(3);
							double error = mzBin - peakLocation;
							//If the current peak is past the MZ number, skip ahead
							if(Math.abs(error) > options.peakTolerance){
								if(error < 0){
									while(error < 0 && Math.abs(error) > options.peakTolerance) {
										if(!charges.next()) break;
										mzBin = charges.getDouble(1);
										error = mzBin - peakLocation;
									}
								}
							}
							//otherwise, add the element it's good
							if (Math.abs(error) <= options.peakTolerance) {
								try{
								sql.append("INSERT INTO TEMPatoms VALUES("+ nextID + ",'" + formatter.format(parser.parse(dateTime)) + "'," + mzBin + "," + value + ");\n");
								nextID++;
								}catch(ParseException e){
									System.err.println("Problem Inserting atoms");
									System.exit(1);
								}
							}
						}
					}
				}
				
				/*stmt.execute(sql.toString());
				sql = new StringBuilder();
				
				//insert 0's for non-existent peaks
				ResultSet missingPeaks = stmt.executeQuery(
						"SELECT TB.BinnedTime, MZ.Value\n"+
						"FROM TEMPTimeBins TB, TEMPmz MZ\n"
						+ "WHERE NOT EXISTS (SELECT * FROM TEMPatoms ATOM " +
								"WHERE ATOM.TIME=TB.BinnedTime AND ATOM.MZLocation=MZ.Value);\n");
				
				nextID = getNextID();
				
				if (url.equals("localhost")) {
					String tempFilename = tempdir + File.separator + "Missingbulkfile.txt";
					//System.out.println("Temp File: "+tempFilename);
					PrintWriter bulkFile = null;
					try {
						bulkFile = new PrintWriter(new FileWriter(tempFilename));
					} catch (IOException e) {
						System.err.println("Trouble creating " + tempFilename);
						e.printStackTrace();
					}
					while(missingPeaks.next()){
						try{
							bulkFile.println(nextID+","+formatter.format(parser.parse(missingPeaks.getString(1)))+","+missingPeaks.getString(2)+",0");
							nextID = getNextID();
						}catch(ParseException e){
							System.err.println("Problem Inserting atoms");
							System.exit(1);
						}
					}
					bulkFile.close();
					sql.append("BULK INSERT TEMPatoms\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH " +
					"	(FIELDTERMINATOR=','," +
					"	 ROWTERMINATOR='\n');\n");
					
				}else{
					try{
						sql.append("INSERT INTO TEMPatoms VALUES("+ nextID + ",'" + formatter.format(parser.parse(missingPeaks.getString(1))) + ","+missingPeaks.getString(2)+",0);\n");
						nextID = getNextID();
					}catch(ParseException e){
						System.err.println("Problem Inserting atoms");
						System.exit(1);
					}
				}
				stmt.execute(sql.toString());
				sql = new StringBuilder();
				*/
				
				
				// build 2 child collections - one for particle counts time-series,
				// one for M/Z values time-series.
				int mzRootCollectionID = createEmptyCollection("TimeSeries", newCollectionID, "M/Z", "", "");
				int mzPeakLoc, mzCollectionID;
				// for each mz value specified, make a new child collection and populate it.
				for (int j = 0; j < mzValues.length; j++) {	
					mzPeakLoc = mzValues[j];
					mzCollectionID = createEmptyCollection("TimeSeries", mzRootCollectionID, mzPeakLoc + "", "", "");
					progressBar.increment("  " + collectionName + ", M/Z: " + mzPeakLoc);
					sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
							"select " + mzCollectionID + ", NewAtomID from TEMPatoms WHERE MZLocation = "+mzPeakLoc+"\n" +
							"ORDER BY NewAtomID;\n");
					sql.append("insert TimeSeriesAtomInfoDense (AtomID, Time, Value) \n" +
							"select NewAtomID, Time, Value from TEMPatoms WHERE MZLocation = "+mzPeakLoc+
					"ORDER BY NewAtomID;\n");
				}
				sql.append("DROP TABLE TEMPmz;\n");
				sql.append("DROP TABLE TEMPatoms;\n");
				progressBar.increment("  Executing M/Z Queries...");
					// if the particle count is selected, produce that time series as well.
					// NOTE:  QUERY HAS CHANGED DRASTICALLY SINCE GREG'S IMPLEMENTATION!!!
					// it now tracks number of particles instead of sum of m/z particles.	
				//System.out.println("Statement: "+sql.toString());
				stmt.execute(sql.toString());
			}
			sql = new StringBuilder();
			if (options.produceParticleCountTS) {
				int combinedCollectionID = createEmptyCollection("TimeSeries", newCollectionID, "Particle Counts", "", "");
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'TEMPatomCount')\n"+
				"DROP TABLE TEMPatomCount;\n");
				sql.append("CREATE TABLE TEMPatomCount (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
						" Time DateTime, \n MZLocation int, \n Value real)\n" +
						"insert TEMPatomCount (Time, Value) \n" +
						"SELECT BinnedTime, COUNT(AtomID) AS IDCount FROM TEMPTimeBins TB\n"+
						"GROUP BY BinnedTime\n"+
				"ORDER BY BinnedTime;\n");
				
				sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
						"select " + combinedCollectionID + ", NewAtomID from TEMPatomCount;\n");
				sql.append("insert " + getDynamicTableName(DynamicTable.AtomInfoDense, "TimeSeries") + " (AtomID, Time, Value) \n" +
				"select NewAtomID, Time, Value from TEMPatomCount;\n");
				sql.append("DROP TABLE TEMPatomCount;");
				
				progressBar.increment("  " + collectionName + ", Particle Counts");
				//System.out.println("Statement: "+sql.toString());
				stmt.execute(sql.toString());
			}
			
			/* IF DATATYPE IS TIME SERIES */
		} else if (curColl.getDatatype().equals("TimeSeries")) {
			sql.append("CREATE TABLE TEMPatoms (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
			" Time DateTime, \n Value real);\n");
			sql.append("insert TEMPatoms (Time, Value) \n" +
					"select BinnedTime, " + options.getGroupMethodStr() + "(AID.Value) AS Value \n" +
					"from TEMPTimeBins TB \n" +
					"join TimeSeriesAtomInfoDense AID on (TB.AtomID = AID.AtomID) \n"+
					"group by BinnedTime \n" +
			"order by BinnedTime;\n");
			
			int newCollectionID = createEmptyCollection("TimeSeries", rootCollectionID, collectionName, "", "");
			sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
					"select " + newCollectionID + ", NewAtomID from TEMPatoms;\n");
			
			sql.append("insert TimeSeriesAtomInfoDense (AtomID, Time, Value) \n" +
			"select NewAtomID, Time, Value from TEMPatoms;\n");
			sql.append("DROP TABLE TEMPatoms;\n");
			progressBar.increment("  " + collectionName);
			stmt.execute(sql.toString());
		}
		/* IF DATATYPE IS AMS */
		else if (curColl.getDatatype().equals("AMS")) {	
			if (mzValues == null) {
				ErrorLogger.writeExceptionToLog("SQLServer","Collection: " + collectionName + " doesn't have any peak data to aggregate");
				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate");
				System.err.println("Collections need to overlap times in order to be aggregated.");
				return;
			} else if (mzValues.length == 0) {
				ErrorLogger.writeExceptionToLog("SQLServer","Collection: " + collectionName + " doesn't have any peak data to aggregate");
				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate");
				System.err.println("Collections need to overlap times in order to be aggregated.");
			} else {
				//create and insert MZ Values into temporary TEMPmz table.
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'TEMPmz')\n"+
				"DROP TABLE TEMPmz;\n");
				sql.append("CREATE TABLE TEMPmz (Value INT);\n");
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
					for (int i = 0; i < mzValues.length; i++){
						bulkFile.println(mzValues[i]);
					}
					bulkFile.close();
					sql.append("BULK INSERT TEMPmz\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH (FIELDTERMINATOR=',');\n");
				} else {
					for (int i = 0; i < mzValues.length; i++){
						sql.append("INSERT INTO TEMPmz VALUES("+mzValues[i]+");\n");
					}
				}	
				//	create TEMPatoms table
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'TEMPatoms')\n"+
				"DROP TABLE TEMPatoms;\n");
				sql.append("CREATE TABLE TEMPatoms (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
				" Time DateTime, \n MZLocation int, \n Value real);\n");
				// went back to Greg's JOIN methodology, but retained TEMPmz table, which speeds it up.
				sql.append("insert TEMPatoms (Time, MZLocation, Value) \n" +
						"SELECT BinnedTime, MZ.Value AS Location,"+options.getGroupMethodStr()+"(PeakHeight) AS PeakHeight \n"+
						"FROM TEMPTimeBins TB\n" +
						"JOIN AMSAtomInfoSparse AIS on (TB.AtomID = AIS.AtomID)\n"+
						"JOIN TEMPmz MZ on (abs(AIS.PeakLocation - MZ.Value) < "+options.peakTolerance+")\n"+
						"GROUP BY BinnedTime,MZ.Value\n"+
						"ORDER BY Location, BinnedTime;\n");

				// build 2 child collections - one for time series, one for M/Z values.
				int newCollectionID = createEmptyCollection("TimeSeries", rootCollectionID, collectionName, "", "");
				int mzRootCollectionID = createEmptyCollection("TimeSeries", newCollectionID, "M/Z", "", "");
				int mzPeakLoc, mzCollectionID;
				// for each mz value specified, make a new child collection and populate it.
				for (int j = 0; j < mzValues.length; j++) {	
					mzPeakLoc = mzValues[j];
					mzCollectionID = createEmptyCollection("TimeSeries", mzRootCollectionID, mzPeakLoc + "", "", "");
					progressBar.increment("  " + collectionName + ", M/Z: " + mzPeakLoc);
					sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
							"select " + mzCollectionID + ", NewAtomID from TEMPatoms WHERE MZLocation = "+mzPeakLoc+"\n" +
							"ORDER BY NewAtomID;\n");
					sql.append("insert TimeSeriesAtomInfoDense (AtomID, Time, Value) \n" +
							"select NewAtomID, Time, Value from TEMPatoms WHERE MZLocation = "+mzPeakLoc+
					"ORDER BY NewAtomID;\n");
				}
				sql.append("DROP TABLE TEMPmz;\n");
				sql.append("DROP TABLE TEMPatoms;\n");
				progressBar.increment("  Executing M/Z Queries...");	
				stmt.execute(sql.toString());
			}
		}
		//stmt.execute(sql.toString());
		stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception aggregating collection: " + collectionName);
			System.err.println("SQL exception aggregating collection: " + collectionName);
			e.printStackTrace();
		}
	}
	
	/**
	 * This creates the empty collections and the queries and sends it to fillAtomsFromMemory 
	 * method.  This only handles ATOFMS and TIME SERIES data, which will need to
	 * be changed.
	 * 
	 * @return the list of CIDs that need to be updated in InternalAtomORder.
	 */
	/*public void createAggregateTimeSeries(ProgressBarWrapper progressBar, int rootCollectionID, Collection curColl, int[] mzValues) {
		int collectionID = curColl.getCollectionID();
		String collectionName = curColl.getName();
		AggregationOptions options = curColl.getAggregationOptions();
		try {
		Statement stmt = con.createStatement();
		StringBuilder sql = new StringBuilder();
		// Create and Populate #atoms table with appropriate information.
		 IF DATATYPE IS ATOFMS 
		if (curColl.getDatatype().equals("ATOFMS")) {	
			if (mzValues == null) {
				ErrorLogger.writeExceptionToLog("SQLServer","Error! Collection: " + collectionName + " doesn't have any peak data to aggregate!");
				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate!");
				System.err.println("Collections need to overlap times in order to be aggregated.");
				return;
			} 
			
			int newCollectionID = createEmptyCollection("TimeSeries", rootCollectionID, collectionName, "", "");
			if (mzValues.length == 0) {
				// do nothing!  allow emptiness.
//				ErrorLogger.writeExceptionToLog("SQLServer","Note: Collection: " + collectionName + " doesn't have any peak data to aggregate!");
//				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate!");
//				System.err.println("Collections need to overlap times in order to be aggregated.");
			} else {
				//create and insert MZ Values into temporary #mz table.
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#mz')\n"+
				"DROP TABLE #mz;\n");
				sql.append("CREATE TABLE #mz (Value INT);\n");
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
					for (int i = 0; i < mzValues.length; i++){
						bulkFile.println(mzValues[i]);
					}
					bulkFile.close();
					sql.append("BULK INSERT #mz\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH (FIELDTERMINATOR=',');\n");
				} else {
					for (int i = 0; i < mzValues.length; i++){
						sql.append("INSERT INTO #mz VALUES("+mzValues[i]+");\n");
					}
				}	
				//	create #atoms table
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#atoms')\n"+
				"DROP TABLE #atoms;\n");
				sql.append("CREATE TABLE #atoms (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
				" Time DateTime, \n MZLocation int, \n Value real);\n");
				// went back to Greg's JOIN methodology, but retained #mz table, which speeds it up.
				// collects the sum of the Height/Area over all atoms at a given Time and for a specific m/z 
				sql.append("insert #atoms (Time, MZLocation, Value) \n" +
						"SELECT BinnedTime, MZ.Value AS Location,"+options.getGroupMethodStr()+"(PeakHeight) AS PeakHeight \n"+
						"FROM #TimeBins TB\n" +
						"JOIN ATOFMSAtomInfoSparse AIS on (TB.AtomID = AIS.AtomID)\n"+
						"JOIN #mz MZ on (abs(AIS.PeakLocation - MZ.Value) < "+options.peakTolerance+")\n"+
						"GROUP BY BinnedTime,MZ.Value\n"+
						"ORDER BY Location, BinnedTime;\n");

				// build 2 child collections - one for particle counts time-series,
				// one for M/Z values time-series.
				int mzRootCollectionID = createEmptyCollection("TimeSeries", newCollectionID, "M/Z", "", "");
				int mzPeakLoc, mzCollectionID;
				// for each mz value specified, make a new child collection and populate it.
				for (int j = 0; j < mzValues.length; j++) {	
					mzPeakLoc = mzValues[j];
					mzCollectionID = createEmptyCollection("TimeSeries", mzRootCollectionID, mzPeakLoc + "", "", "");
					progressBar.increment("  " + collectionName + ", M/Z: " + mzPeakLoc);
					sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
							"select " + mzCollectionID + ", NewAtomID from #atoms WHERE MZLocation = "+mzPeakLoc+"\n" +
							"ORDER BY NewAtomID;\n");
					sql.append("insert TimeSeriesAtomInfoDense (AtomID, Time, Value) \n" +
							"select NewAtomID, Time, Value from #atoms WHERE MZLocation = "+mzPeakLoc+
					"ORDER BY NewAtomID;\n");
				}
				sql.append("DROP TABLE #mz;\n");
				sql.append("DROP TABLE #atoms;\n");
				progressBar.increment("  Executing M/Z Queries...");
					// if the particle count is selected, produce that time series as well.
					// NOTE:  QUERY HAS CHANGED DRASTICALLY SINCE GREG'S IMPLEMENTATION!!!
					// it now tracks number of particles instead of sum of m/z particles.	
				stmt.execute(sql.toString());
			}
			sql = new StringBuilder();
			if (options.produceParticleCountTS) {
				int combinedCollectionID = createEmptyCollection("TimeSeries", newCollectionID, "Particle Counts", "", "");
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#atomCount')\n"+
				"DROP TABLE #atomCount;\n");
				sql.append("CREATE TABLE #atomCount (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
						" Time DateTime, \n MZLocation int, \n Value real)\n" +
						"insert #atomCount (Time, Value) \n" +
						"SELECT BinnedTime, COUNT(AtomID) AS IDCount FROM #TimeBins TB\n"+
						"GROUP BY BinnedTime\n"+
				"ORDER BY BinnedTime;\n");
				
				sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
						"select " + combinedCollectionID + ", NewAtomID from #atomCount;\n");
				sql.append("insert " + getDynamicTableName(DynamicTable.AtomInfoDense, "TimeSeries") + " (AtomID, Time, Value) \n" +
				"select NewAtomID, Time, Value from #atomCount;\n");
				sql.append("DROP TABLE #atomCount;");
				
				progressBar.increment("  " + collectionName + ", Particle Counts");
				stmt.execute(sql.toString());
			}
			
			 IF DATATYPE IS TIME SERIES 
		} else if (curColl.getDatatype().equals("TimeSeries")) {
			sql.append("CREATE TABLE #atoms (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
			" Time DateTime, \n Value real);\n");
			sql.append("insert #atoms (Time, Value) \n" +
					"select BinnedTime, " + options.getGroupMethodStr() + "(AID.Value) AS Value \n" +
					"from #TimeBins TB \n" +
					"join TimeSeriesAtomInfoDense AID on (TB.AtomID = AID.AtomID) \n"+
					"group by BinnedTime \n" +
			"order by BinnedTime;\n");
			
			int newCollectionID = createEmptyCollection("TimeSeries", rootCollectionID, collectionName, "", "");
			sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
					"select " + newCollectionID + ", NewAtomID from #atoms;\n");
			
			sql.append("insert TimeSeriesAtomInfoDense (AtomID, Time, Value) \n" +
			"select NewAtomID, Time, Value from #atoms;\n");
			sql.append("DROP TABLE #atoms;\n");
			progressBar.increment("  " + collectionName);
			stmt.execute(sql.toString());
		}
		 IF DATATYPE IS AMS 
		else if (curColl.getDatatype().equals("AMS")) {	
			if (mzValues == null) {
				ErrorLogger.writeExceptionToLog("SQLServer","Collection: " + collectionName + " doesn't have any peak data to aggregate");
				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate");
				System.err.println("Collections need to overlap times in order to be aggregated.");
				return;
			} else if (mzValues.length == 0) {
				ErrorLogger.writeExceptionToLog("SQLServer","Collection: " + collectionName + " doesn't have any peak data to aggregate");
				System.err.println("Collection: " + collectionID + "  doesn't have any peak data to aggregate");
				System.err.println("Collections need to overlap times in order to be aggregated.");
			} else {
				//create and insert MZ Values into temporary #mz table.
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#mz')\n"+
				"DROP TABLE #mz;\n");
				sql.append("CREATE TABLE #mz (Value INT);\n");
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
					for (int i = 0; i < mzValues.length; i++){
						bulkFile.println(mzValues[i]);
					}
					bulkFile.close();
					sql.append("BULK INSERT #mz\n" +
							"FROM '" + tempFilename + "'\n" +
					"WITH (FIELDTERMINATOR=',');\n");
				} else {
					for (int i = 0; i < mzValues.length; i++){
						sql.append("INSERT INTO #mz VALUES("+mzValues[i]+");\n");
					}
				}	
				//	create #atoms table
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#atoms')\n"+
				"DROP TABLE #atoms;\n");
				sql.append("CREATE TABLE #atoms (NewAtomID int IDENTITY("+getNextID()+", 1), \n" +
				" Time DateTime, \n MZLocation int, \n Value real);\n");
				// went back to Greg's JOIN methodology, but retained #mz table, which speeds it up.
				sql.append("insert #atoms (Time, MZLocation, Value) \n" +
						"SELECT BinnedTime, MZ.Value AS Location,"+options.getGroupMethodStr()+"(PeakHeight) AS PeakHeight \n"+
						"FROM #TimeBins TB\n" +
						"JOIN AMSAtomInfoSparse AIS on (TB.AtomID = AIS.AtomID)\n"+
						"JOIN #mz MZ on (abs(AIS.PeakLocation - MZ.Value) < "+options.peakTolerance+")\n"+
						"GROUP BY BinnedTime,MZ.Value\n"+
						"ORDER BY Location, BinnedTime;\n");

				// build 2 child collections - one for time series, one for M/Z values.
				int newCollectionID = createEmptyCollection("TimeSeries", rootCollectionID, collectionName, "", "");
				int mzRootCollectionID = createEmptyCollection("TimeSeries", newCollectionID, "M/Z", "", "");
				int mzPeakLoc, mzCollectionID;
				// for each mz value specified, make a new child collection and populate it.
				for (int j = 0; j < mzValues.length; j++) {	
					mzPeakLoc = mzValues[j];
					mzCollectionID = createEmptyCollection("TimeSeries", mzRootCollectionID, mzPeakLoc + "", "", "");
					progressBar.increment("  " + collectionName + ", M/Z: " + mzPeakLoc);
					sql.append("insert AtomMembership (CollectionID, AtomID) \n" +
							"select " + mzCollectionID + ", NewAtomID from #atoms WHERE MZLocation = "+mzPeakLoc+"\n" +
							"ORDER BY NewAtomID;\n");
					sql.append("insert TimeSeriesAtomInfoDense (AtomID, Time, Value) \n" +
							"select NewAtomID, Time, Value from #atoms WHERE MZLocation = "+mzPeakLoc+
					"ORDER BY NewAtomID;\n");
				}
				sql.append("DROP TABLE #mz;\n");
				sql.append("DROP TABLE #atoms;\n");
				progressBar.increment("  Executing M/Z Queries...");	
				stmt.execute(sql.toString());
			}
		}
		//stmt.execute(sql.toString());
		stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception aggregating collection: " + collectionName);
			System.err.println("SQL exception aggregating collection: " + collectionName);
			e.printStackTrace();
		}
	}
	
	*/
	public int[] getValidSelectedMZValuesForCollection(Collection collection, Date startDate, Date endDate) {
		Set<Integer> collectionIDs = collection.getCollectionIDSubTree();
		AggregationOptions options = collection.getAggregationOptions();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = null;
			ArrayList<Integer> peakLocs = new ArrayList<Integer>();
			StringBuilder sql = new StringBuilder();
			
//			if we want to get all mz values:
			if (options.allMZValues) {
				rs = stmt.executeQuery("select distinct cast(round (PeakLocation,0) as int) as RoundedPeakLocation " +
						"from "+
						getDynamicTableName(DynamicTable.AtomInfoSparse, collection.getDatatype())+
						" AIS, InternalAtomOrder IAO, "+
						getDynamicTableName(DynamicTable.AtomInfoDense, collection.getDatatype())+
						" AID \n"+
						"WHERE IAO.CollectionID = "+collection.getCollectionID()+"\n"+
						"AND IAO.AtomID = AIS.AtomID \n" +
						"AND IAO.AtomID = AID.AtomID \n" +
						"AND abs(PeakLocation-(round(PeakLocation,0))) < " + options.peakTolerance+"\n"+
						"AND AID.Time >= '"+dateFormat.format(startDate)+"'\n"+
						"AND AID.Time <= '"+dateFormat.format(endDate)+"'\n"+
				"ORDER BY RoundedPeakLocation;\n");
				while (rs.next()){
					peakLocs.add(rs.getInt("RoundedPeakLocation"));
				}
				rs.close();

			// if there's a list of mz values:
			} else if (options.mzValues != null && options.mzValues.size() > 0)
			{
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '#mz')\n"+
						"DROP TABLE #mz;\n");
				sql.append("CREATE TABLE #mz (Value INT);\n");
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
					for (int i = 0; i < options.mzValues.size(); i++){
						bulkFile.println(options.mzValues.get(i));
					}
					bulkFile.close();
					sql.append("BULK INSERT #mz\n" +
							"FROM '" + tempFilename + "'\n" +
							"WITH (FIELDTERMINATOR=',');\n");
				} else {
					for (int i = 0; i < options.mzValues.size(); i++) {
						sql.append("INSERT INTO #mz VALUES ("+options.mzValues.get(i)+");\n");
					}
				}	
				stmt.execute(sql.toString());
				/* If Datatype is ATOFMS */
				rs = stmt.executeQuery("select distinct MZ.Value as RoundedPeakLocation " +
						"from "+
						getDynamicTableName(DynamicTable.AtomInfoSparse, collection.getDatatype())+
						" AIS, InternalAtomOrder IAO, #mz MZ, "+
						getDynamicTableName(DynamicTable.AtomInfoDense, collection.getDatatype())+
						" AID \n"+
						"WHERE IAO.CollectionID = "+collection.getCollectionID()+"\n"+
						"AND IAO.AtomID = AIS.AtomID \n" +
						"AND IAO.AtomID = AID.AtomID \n"+
						"AND abs(PeakLocation - MZ.Value) < " + options.peakTolerance+"\n"+
						"AND AID.Time >= '"+dateFormat.format(startDate)+"'\n"+
						"AND AID.Time <= '"+dateFormat.format(endDate)+"'\n"+
				"ORDER BY MZ.Value;\n");
				while (rs.next()){
					peakLocs.add(rs.getInt("RoundedPeakLocation"));
				}
				stmt.execute("DROP TABLE #mz;\n");
				rs.close();
			} 

			stmt.close();
			
			int[] ret = new int[peakLocs.size()];
			int i = 0;
			for (int peakLoc : peakLocs)
				ret[i++] = peakLoc;
			
			return ret;
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception creating finding M/Z values within collection");
			System.err.println("Error creating finding M/Z values within collection.");
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public Hashtable<java.util.Date, double[]> getConditionalTSCollectionData(Collection seq1, Collection seq2, 
			ArrayList<Collection> conditionalSeqs, ArrayList<String> conditionStrs) {
		
		ArrayList<String> columnsToReturn = new ArrayList<String>();
		columnsToReturn.add("Ts1Value");
		
		String atomSelStr = "select CollectionID, Time, Value from " +
		getDynamicTableName(DynamicTable.AtomInfoDense, "TimeSeries") + 
		" D join AtomMembership M on (D.AtomID = M.AtomID)";
		
		String selectStr = "select T1.Time as Time";
		String tableJoinStr = "from (" + atomSelStr + ") T1 \n";
		String collCondStr = "where T1.CollectionID = " + seq1.getCollectionID() + " \n";
		String condStr = ", %s as %s";
		
		if (seq2 != null) {
			tableJoinStr += "join (" + atomSelStr + ") T2 on (T2.Time = T1.Time) \n";
			collCondStr += "and T2.CollectionID = " + seq2.getCollectionID() + " \n";
			columnsToReturn.add("Ts2Value");
		}
		
		if (conditionStrs.size() > 0) {
			condStr = ", case when (";
			
			for (int i = 0; i < conditionStrs.size(); i++)
				condStr += conditionStrs.get(i);
			
			condStr += ") then %s else -999 end as %s";
			
			for (int i = 0; i < conditionalSeqs.size(); i++) {
				tableJoinStr += "join (" + atomSelStr + ") C" + i + " on (C" + i + ".Time = T1.Time) \n";
				selectStr += ", C" + i + ".Value as C" + i + "Value";
				collCondStr += "and C" + i + ".CollectionID = " + conditionalSeqs.get(i).getCollectionID() + " \n";
				columnsToReturn.add("C" + i + "Value");
			}
		}
		
		selectStr += String.format(condStr, "T1.Value", "Ts1Value");
		
		if (seq2 != null)
			selectStr += String.format(condStr, "T2.Value", "Ts2Value");
		
		String sqlStr = selectStr + " \n" + tableJoinStr + collCondStr;
		Hashtable<java.util.Date, double[]> retData = new Hashtable<java.util.Date, double[]>();
		
		try{
			Statement stmt = con.createStatement();
			//System.out.println(sqlStr);
			ResultSet rs = stmt.executeQuery(sqlStr);
			
			while (rs.next()) {
				double[] retValues = new double[columnsToReturn.size()];
				for (int i = 0; i < retValues.length; i++)
					retValues[i] = rs.getDouble(columnsToReturn.get(i));
				if (rs.getTimestamp("Time") != null)
					retData.put(rs.getTimestamp("Time"), retValues);	
			}
			stmt.close();
			rs.close();
		} catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving time series data.");
			System.err.println("Error retrieving time series data.");
			e.printStackTrace();
		}
		
		return retData;
	}
	
	public void syncWithIonsInDB(ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		String sqlStr =
			"SET NOCOUNT ON \n" + 
			"DECLARE @sigs TABLE ( \n" +
			"    Name varchar(8000), \n" +
			"    IsPositive bit \n" +
			")";
		
		for (LabelingIon ion : posIons)
			sqlStr += "insert @sigs values ('" + ion.name + "', 1) \n";
		
		for (LabelingIon ion : negIons)
			sqlStr += "insert @sigs values ('" + ion.name + "', 0) \n";
		
		// Add any new ions from file into database
		sqlStr += 
			"insert IonSignature (Name, IsPositive) \n" +
			"select S.Name, S.IsPositive \n" +
			"from @sigs S \n" +
			"left outer join IonSignature IONS on (S.Name = IONS.Name and S.IsPositive = IONS.IsPositive) \n" +
			"where IONS.IonID IS NULL \n\n" +
			
			// And then get all IonIDs in signature file back for later use...
			"select IONS.IonID, IONS.Name, IONS.IsPositive \n" +
			"from @sigs S \n" +
			"join IonSignature IONS on (S.Name = IONS.Name and S.IsPositive = IONS.IsPositive) \n" + 
			
			"SET NOCOUNT OFF";
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sqlStr);
			
			while (rs.next()) {
				ArrayList<LabelingIon> arrToLookThrough = rs.getBoolean("IsPositive") ? posIons : negIons;
				String ionName = rs.getString("Name");
				for (LabelingIon ion : arrToLookThrough) {
					if (ion.name.equals(ionName))
						ion.ionID = rs.getInt("IonID");
				}
			}
			
			rs.close();
			stmt.close();
		} catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving Ion data.");
			System.err.println("Error retrieving Ion data.");
			e.printStackTrace();
		}
	}
	
	public void saveAtomRemovedIons(int atomID, ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		String sqlStr =
			"DECLARE @removedIons TABLE ( IonID int ) \n";
		
		for (LabelingIon ion : posIons)
			if (!ion.isChecked())
				sqlStr += "insert @removedIons values (" + ion.ionID + ") \n";
		
		for (LabelingIon ion : negIons)
			if (!ion.isChecked())
				sqlStr += "insert @removedIons values (" + ion.ionID + ") \n";
		
		sqlStr += "delete from AtomIonSignaturesRemoved where AtomID = " + atomID + "\n\n";
		sqlStr += 
			"insert AtomIonSignaturesRemoved (AtomID, IonID) \n" +
			"select " + atomID + ", IonID from @removedIons";
		
		try {
			Statement stmt = con.createStatement();
			stmt.execute(sqlStr);
			stmt.close();
		} catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception saving removed Ion data.");
			System.err.println("Error saving removed Ion data.");
			e.printStackTrace();
		}	
	}
	
	public void buildAtomRemovedIons(int atomID, ArrayList<LabelingIon> posIons, ArrayList<LabelingIon> negIons) {
		Set<Integer> removedIDs = new HashSet<Integer>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select IonID from AtomIonSignaturesRemoved where AtomID = " + atomID);
			
			while (rs.next())
				removedIDs.add(rs.getInt("IonID"));
			
			rs.close();
			stmt.close();
		} catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving removed Ion data.");
			System.err.println("Error retrieving removed Ion data.");
			e.printStackTrace();
		}
		
		for (LabelingIon ion : posIons)
			ion.setChecked(!removedIDs.contains(ion.ionID));
		
		for (LabelingIon ion : negIons)
			ion.setChecked(!removedIDs.contains(ion.ionID));
		
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
			stmt.close();
		}
		catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL exception retrieving known datatypes.");
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
				stmt.close();
				return true;
			}
			else {
				stmt.close();
				return false;
			}
		}
		catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception checking for the existence of datatype "+type);
			System.err.println("problems checking datatype from SQLServer.");
			return false;
		}
		
	}
	
	/** 
	 * Gets first atom for top-leve particles in collection.
	 */
	public int getFirstAtomInCollection(Collection collection) {
		int atom = -1;
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MIN(AtomID) FROM AtomMembership WHERE CollectionID = " + collection.getCollectionID());
			
			if (rs.next())
				atom = rs.getInt(1);
			stmt.close();
		}
		catch (SQLException e){
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception getting first atom in collection");
			System.err.println("problems checking datatype from SQLServer.");
		}
		return atom;
	}
	
	/**
	 * If the new, compressed datatype doesn't exist, add it.
	 */
	public void addCompressedDatatype(String newDatatype, String oldDatatype) {
		System.out.println();
		// Copies only relevant columns into table
		try {
			if (!containsDatatype(newDatatype)) {
				assert(containsDatatype(oldDatatype));
				// insert metadata info.
				Statement stmt = con.createStatement();
				
				// insert the dataset info.  
				System.out.println("inserting DataSet info...");
				stmt.addBatch("INSERT INTO MetaData VALUES ('" + newDatatype + "','[DataSet]','VARCHAR(8000)',0,0,2)");
				stmt.addBatch("INSERT INTO MetaData VALUES ('" + newDatatype + "','[DataSetID]','INT',1,0,1)");
				stmt.executeBatch();
				// the following statement takes all the reals and ints from 
				// atominfo dense and sparse and orders them by column number.
				ResultSet rs = stmt.executeQuery("SELECT * FROM MetaData WHERE " +
						"Datatype = '" + oldDatatype + "' AND TableID != 0 AND " +
						"(ColumnType = 'INT' OR ColumnType = 'REAL') ORDER BY " +
				"TableID, ColumnOrder");
				int columnOrder = 1;
				boolean firstloop = true;
				while (rs.next()) {
					if (rs.getInt(5) == 2 && firstloop) {
						columnOrder = 1;
						firstloop = false;
					}

					//ALL COMRESSED VALUES ARE SET TO REAL!!
					/*String update = "INSERT INTO MetaData VALUES ('" +
					newDatatype + "','" + rs.getString(2) + "','" + 
					rs.getString(3) + "'," + rs.getInt(4) + "," +
					rs.getInt(5) + "," + columnOrder + ")";*/

					String update = "INSERT INTO MetaData VALUES ('" +
					newDatatype + "','" + rs.getString(2) + "','REAL'," + rs.getInt(4) + "," +
					rs.getInt(5) + "," + columnOrder + ")";
					System.out.println(update);
					stmt.addBatch(update);
					columnOrder++;
				}
				stmt.executeBatch();
				rs = stmt.executeQuery("SELECT MAX(ColumnOrder) FROM MetaData " +
						"WHERE Datatype = '" + newDatatype + "' AND TableID = 1");
				assert(rs.next());
				int nextColumnOrder = rs.getInt(1) + 1;
				String numParts = "INSERT INTO MetaData VALUES ('" + 
				newDatatype + "', '[NumParticles]', 'REAL',0," + 
				DynamicTable.AtomInfoDense.ordinal() + "," + nextColumnOrder +")";
				System.out.println(numParts);
				stmt.execute(numParts);
				rs.close();
				stmt.close();
				// create dynamic tables, skipping over the xml format.
				DynamicTableGenerator generator = new DynamicTableGenerator(con);
				String newName = generator.createDynamicTables(newDatatype,true);
				assert(newName.equals(newDatatype));
			}
		}
		catch(SQLException e) {
			System.err.println("error creating compressed tables");
			e.printStackTrace();
		}
	}
	
	public CollectionCursor getMemoryClusteringCursor(Collection collection, ClusterInformation cInfo) {
		return new MemoryClusteringCursor(collection, cInfo);
	}
	
	public ArrayList<String> getPrimaryKey(String datatype, DynamicTable table) {
		ArrayList<String> strings = new ArrayList<String>();	
		Statement stmt;
		try {
			stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT ColumnName FROM MetaData " +
					"WHERE PrimaryKey = 1 AND Datatype = '" + datatype + 
					"' AND TableID = " + table.ordinal());
			while (rs.next()) {
				if (!rs.getString(1).equals("[AtomID]") && !rs.getString(1).equals("[DatasetID]")) 
					strings.add(rs.getString(1));
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		
		return strings;
	}
	
	/**
	 * internalAtomOrder updated by updating the collection itself,
	 * recursing through subcollections.  This ONLY updates the specified
	 * collection, and it works by traversing down to the leaves of the tree.
	 * It is a recursive algortihm, and will be used mostly for cut/copy/paste
	 * functionality.  Importing collections don't need this, since they have no
	 * children.
	 * 
	 * Implements the bulk insert method from Greg Cipriano, since the client
	 * and the server have to be on the same machine.
	 * 
	 * @param collection
	 */
	public void updateInternalAtomOrder(Collection collection) {
		//System.out.println("updating InternalAtomOrder for collection " + collection.getCollectionID());
		int cID = collection.getCollectionID();
		if (cID == 0 || cID == 1) 
			return;
		try {
			Statement stmt = con.createStatement();
			stmt.execute("DELETE FROM InternalAtomOrder WHERE CollectionID = " + cID);
			
			//get all the AtomIDs from AtomMembership if the corresponding CollectionID was
			//the parent's or one of the children's.  We want the union of these so that there
			//are no overlaps.
			String query = "SELECT AtomID FROM AtomMembership WHERE CollectionID = " + cID;
			Iterator<Integer> subCollections = getAllDescendantCollections(cID,false).iterator();
			while (subCollections.hasNext())
				query += " union (SELECT AtomID FROM AtomMembership WHERE CollectionID = " + subCollections.next() + ")";
			query += " ORDER BY AtomID";
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);
			int order = 1;
			
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
				while (rs.next()) {
					//System.out.println(rs.getInt(1) + "," + cID + "," + order);
					bulkFile.println(rs.getInt(1) + "," + cID + "," + order);
					order++;
				}
				bulkFile.close();
				stmt.addBatch("BULK INSERT InternalAtomOrder\n" +
						"FROM '" + tempFilename + "'\n" +
				"WITH (FIELDTERMINATOR=',')");
			} else {
				while (rs.next()) {			
					stmt.addBatch("INSERT INTO InternalAtomOrder VALUES("+rs.getInt(1)+","+cID+","+order+")");
					order++;
				}
			}		
			//System.out.println("inserted " + (order-1) + " atoms into cID " + 4);
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.writeExceptionToLog("SQLServer","SQL Exception inserting atom.  Please check incoming data for correct format.");
			System.err.println("Exception inserting particle.");
			e.printStackTrace();
		}
	}
	
	/**
	 * updates a particular collection and its ancestors.  It does this
	 * by using the InternalAtomOrder table - it assumes that everything
	 * EXCEPT the collection and its parents is accurate in the 
	 * InternalAtomOrder table.
	 * 
	 * WAY THIS WORKS:
	 * This is a recursive algorithm.  It takes a collection, updates it, and
	 * then calls the method on the collection's parent, moving up the tree until
	 * it reaches the ROOT (0 or 1).  For each collection , it does the following:
	 * 1. Deletes all the rows in InternalAtomOrder with the collection's ID.
	 * 2. gets the union of the following two queries:
	 * 	 a) Gets the atomIDS of the collection from AtomMembership
	 *   b) Gets the atomIDs of the collection's children from InternalAtomOrder
	 * 3. Inserts this result set into InternalAtomOrder table
	 * 
	 * @param collection
	 */
	public void updateAncestors(Collection collection) {
		// if you try to update a null collection or one of the root collections,
		// return.
		if (collection == null || 
				collection.getCollectionID() == 0 || 
				collection.getCollectionID() == 1) 
			return;
		int cID = collection.getCollectionID();
		//System.out.println("updating InternalAtomOrder Table for: " + cID);
		try {
			Statement stmt = con.createStatement();
			
			// Repopulate InternalAtomOrder table.
			//System.out.println("DELETE FROM InternalAtomOrder WHERE CollectionID = " + cID);
			stmt.execute("DELETE FROM InternalAtomOrder WHERE CollectionID = " + cID);
			String query = "SELECT DISTINCT AtomID FROM AtomMembership " +
				"WHERE CollectionID = "+cID;
			ArrayList<Integer> children = collection.getSubCollectionIDs();
			if (children.size() != 0) {
				query += " UNION SELECT AtomID FROM InternalAtomOrder WHERE CollectionID = " + children.get(0);
				for (int i = 1; i < children.size(); i++) 
					query += " OR CollectionID = " + children.get(i);
				
			}
			query += " ORDER BY AtomID";
			ResultSet rs = stmt.executeQuery(query);
			int order = 1;
			Statement st2 = con.createStatement();
			
			while (rs.next()) {
				//System.out.println("inserting...");
				st2.addBatch("INSERT INTO InternalAtomOrder VALUES ("+
						rs.getInt(1) + ","+cID+","+order+")");
				order++;
				if (order % 1000 == 0) st2.executeBatch();
			}
			st2.executeBatch();
			st2.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// maybe make this conditional?  how?  temp table and replace it?
		updateAncestors(collection.getParentCollection());
	}
	
	
	/**
	 * This method, used by VersionChecker, returns the version string that the
	 * database contains, which hopefully corresponds to the structure of the
	 * database.
	 * @return the version string, or "No database version" if the database is from before version strings.
	 * @throws SQLException
	 */
	public String getDatabaseVersion() throws SQLException {
		String version;
		Statement stmt = con.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(
					"SELECT Value FROM DBInfo WHERE Name = 'Version'");
			if (! rs.next()) {
				throw new Exception("Inconsistent DB State?");
				// no version in DB (though this shouldn't happen?)
			} else {
				version = rs.getString(1);
			}
		} catch (SQLException e) {
			version = "No database version";
		} catch (Exception e) {
			throw new SQLException("Can't understand what state the DB is in. (has version field but no value)");
		}
		return version;
	}

	public String aggregateColumn(DynamicTable table, String string, ArrayList<Integer> curIDs, String oldDatatype) {
		double sum=0;
		try {
			Statement stmt = con.createStatement();
		String query = "SELECT SUM("+string+") FROM "+
			getDynamicTableName(table,oldDatatype)+" WHERE AtomID IN (";
		for (int i = 0; i < curIDs.size(); i++) {
			query+=curIDs.get(i);
			if (i!=curIDs.size()-1)
				query+=",";
		}
		query+=");";
		//System.out.println(query);
		ResultSet rs = stmt.executeQuery(query);
		assert(rs.next());
		sum=rs.getDouble(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ""+sum;
	}
	
}

