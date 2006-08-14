package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import errorframework.ErrorLogger;
import gui.ProgressBarWrapper;

/**
 * Encapsulates InfoWarehouse functionality for a relational database
 * @author andersbe, shaferia
 */
public abstract class Database implements InfoWarehouse {
	protected Connection con;
	protected String url;
	protected String port;
	protected String database;
	
	protected String tempdir = System.getenv("TEMP");
	
	protected boolean isDirty = false;
	public boolean isDirty(){
		return isDirty;
	}
	
	/**
	 * Construct an instance of either SQLServerDatabase or MySQLDatabase
	 * @param dbname the name of the database to use (SpASMSdb, TestDB, etc)
	 * @return an InfoWarehouse backed by a relational database
	 */
	public static InfoWarehouse getDatabase(String dbName) {
		return new SQLServerDatabase(dbName);
	}
	
	/**
	 * Construct an instance of either SQLServerDatabase or MySQLDatabase
	 * @return an InfoWarehouse backed by a relational database
	 */
	public static InfoWarehouse getDatabase() {
		return new SQLServerDatabase();
	}
	
	/**
	 * Load information from config.ini for the database with the given name
	 * @param dbname the name of the database (MSDE, MySQL) to use
	 */
	protected void loadConfiguration(String dbname) {
		File f = new File("config.ini");
		try {
			Scanner scan = new Scanner(f);
			while (scan.hasNext()) {
				String tag = scan.next();
				String val = scan.next();
				if (scan.hasNext())
					scan.nextLine();
				
				if (tag.equalsIgnoreCase("db_url:")) { url = val; }
				else if (tag.equalsIgnoreCase(dbname + "_db_port:")) { port = val; }
			}
			scan.close();
		} catch (FileNotFoundException e) { 
			// Don't worry if the file doesn't exist... 
			// just go on with the default values 
		}
	}
	
	/**
	 * Find if the database is present
	 * @param command the SQL to get a list of databases
	 * @return true if present
	 */
	protected boolean isPresentImpl(String command) {
		boolean foundDatabase = false;
		String testdb = database;
		try {
			database = "";
			openConnection();
			Connection con = getCon();
			Statement stmt = con.createStatement();
			
			// See if database exists.
			ResultSet rs = stmt.executeQuery(command);
			while (!foundDatabase && rs.next())
				if (rs.getString(1).equals(testdb))
					foundDatabase = true;
			stmt.close();
		} catch (SQLException e) {
			ErrorLogger.displayException(null,"Error in testing if "+testdb+" is present.");
		}
		database = testdb;
		return foundDatabase;
	}
	
	/**
	 * Retrieve the {@link java.sql.Connection Connection} for this database
	 */
	public Connection getCon() {
		return con;
	}
	
	/**
	 * Open a connection to the database
	 * @param driver the driver name to use
	 * @param connectionstr the connection string to be used with DriverManager.getConnection
	 * @param user username
	 * @param pass password
	 * @return true on success
	 */
	protected boolean openConnectionImpl(String driver, String connectionstr, String user, String pass) {
		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			ErrorLogger.writeExceptionToLog("Database","Failed to load current driver for database " + database);
			System.err.println("Failed to load current driver.");
			return false;
		} // end catch
		con = null;
		try {
			con = DriverManager.getConnection(connectionstr, user, pass);
			con.setAutoCommit(true);
		} catch (Exception e) {
			ErrorLogger.writeExceptionToLog("Database","Failed to establish a connection to " + database);
			System.err.println("Failed to establish a connection to database");
			System.err.println(e);
		}
		return true;
	}

	public boolean closeConnection()
	{
		if (con != null)
		{
			try {
				con.close();
			} catch (Exception e) {
				ErrorLogger.writeExceptionToLog("Database","Could not close the connection to " + database);
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
	 * rebuilds the database; sets the static tables.
	 * @param dbName
	 * @return true if successful
	 */
	public static boolean rebuildDatabase(String dbName) throws SQLException{
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
			throw new SQLException();
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
	 * Drops the given database.
	 * @param dbName the database to drop
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
	
	/**
	 * Create an index on some part of AtomInfoDense for a datatype.  Possibly
	 * useful if you're going to be doing a *whole lot* of queries on a
	 * particular field or set of fields.  For syntax in the fieldSpec, look
	 * at an SQL reference.  If it's just one field, just put the name of the
	 * column there.
	 * 
	 * @author smitht
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
	 * 
	 * @author smitht
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
	 * Returns the adjacent atom for the collection, according to InternalAtomOrder.
	 * @param collection	The ID of the collection under scrutiny.
	 * @param currentID		The current atom's ID.
	 * @param position		1 for next atomID, -1 for previous atomID.
	 * @return	index[0] The ID of the adjacent atom in the collection.
	 * 			index[1] The position of the adjacent atom in the collection.
	 */
	public int[] getAdjacentAtomInCollection(int collection, int currentID, int position){
		int nextID = -99;
		int pos = -77;
		String query = "SELECT AtomID, OrderNumber FROM InternalAtomOrder " +  
		"WHERE (CollectionID = " + collection + ") AND (OrderNumber = " +
		                   "(SELECT OrderNumber FROM InternalAtomOrder " +
		                    "WHERE CollectionID = " + collection +
		                    " AND AtomID = " + currentID + ") + " + position + ")";
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			nextID = rs.getInt(1);
			pos = rs.getInt(2);
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new int[]{nextID, pos};
	}
	
	/**
	 * Updates InternalAtomOrder
	 * @param atomID
	 * @param toParentID
	 */	
	public void addSingleInternalAtomToTable(int atomID, int toParentID) {
		//update InternalAtomOrder; have to iterate through all
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
				while (atomID > rs.getInt(1) && rs.next()) {
					order++;
					// jump to spot in db where atomID fits.
				}

				if (rs.getRow() == 0)
					//if we're at the end of the collection,
					stmt.addBatch("INSERT INTO InternalAtomOrder VALUES ("
							+atomID+","+toParentID+","+(order + 1)+")");
				else if (atomID != rs.getInt(1)) {
					stmt.addBatch("INSERT INTO InternalAtomOrder VALUES ("
							+atomID+","+toParentID+","+order+")");
					
					do {
						order++;
						stmt.addBatch("UPDATE InternalAtomOrder SET OrderNumber = " + order + 
								" WHERE AtomID = "+rs.getInt(1) + 
								" AND CollectionID = " +toParentID);
					}
					while (rs.next());
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
	 * Deletes a collection and unlike orphanAndAdopt() also recursively
	 * deletes all direct descendents.
	 * This method merely selects the collections to be deleted and stores them in #deleteTemp
	 * 
	 * @param collectionID The id of the collection to delete
	 * @return true on success. 
	 */
	public boolean compactDatabase(ProgressBarWrapper progressBar)
	{
		System.out.println("Compacting database and removing unaccessible atoms..");
		try {
			Statement stmt = con.createStatement();
			Statement typesStmt = con.createStatement();
			StringBuilder sql = new StringBuilder();
			sql.append("IF object_id('tempdb..#collections') IS NOT NULL\n"+
					"BEGIN\n" +
					"DROP TABLE #collections;\n"+
					"END\n");
			sql.append("CREATE TABLE #collections (CollectionID int, \n PRIMARY KEY([CollectionID]));\n");
			// Update the InternalAtomOrder table:  Assumes that subcollections
			// are already updated for the parentCollection.
			// clear InternalAtomOrder table of the deleted collection and all subcollections.
			
			sql.append("insert #collections (CollectionID) \n" +
					"	SELECT DISTINCT CollectionID\n" +
					"	FROM AtomMembership\n" +
					"	WHERE AtomMembership.CollectionID NOT IN " +
					"		(SELECT CollectionID\n" +
					"		FROM Collections\n" +
					"		);\n");
			
			
			sql.append("DELETE AtomMembership\nFROM AtomMembership\n"
					+ "INNER JOIN #collections\n ON " +
							"(#collections.CollectionID = AtomMembership.CollectionID);\n");
			sql.append("DELETE FROM InternalAtomOrder\n"
					+ "WHERE CollectionID IN (SELECT * FROM #collections);\n");
			
			ResultSet types = typesStmt.executeQuery("SELECT DISTINCT Datatype FROM MetaData");
			
			while(types.next()){
				String datatype = types.getString(1);
				String sparseTableName = getDynamicTableName(DynamicTable.AtomInfoSparse,datatype);
				String denseTableName = getDynamicTableName(DynamicTable.AtomInfoDense,datatype);
				
				sql.append("IF object_id('tempdb..#atoms') IS NOT NULL \n"+
						"BEGIN\n" +
						"DROP TABLE  #atoms;\n"+
						"END\n");

				sql.append("CREATE TABLE #atoms (AtomID int, \n PRIMARY KEY([AtomID]));\n");
				sql.append("insert #atoms (AtomID) \n" +
						"	SELECT AtomID\n" +
						"	FROM "+denseTableName+"\n" +
						"	WHERE AtomID NOT IN " +
						"		(SELECT AtomID\n" +
						"		FROM AtomMembership\n" +
						"		);\n");
				
				// Also: We could delete all the particles from the particles
				// table IF we want to by now going through the particles 
				// table and choosing every one that does not exist in the 
				// Atom membership table and deleting it.  However, this would
				// remove particles that were referenced in the DataSetMembers 
				// table.  If we don't want this to happen, comment out the 
				// following code, which also removes all references in the 
				// DataSetMembers table:
				//System.out.println(1);
				sql.append("DELETE FROM DataSetMembers\n" +
				"WHERE AtomID IN (SELECT * FROM #atoms);\n");
				// it is ok to call atominfo tables here because datatype is
				// set from recursiveDelete() above.
				// note: Sparse table may not necessarily exist. So check first.
				sql.append("IF EXISTS (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '" + sparseTableName + "')" +
						"DELETE "+sparseTableName+" FROM " + sparseTableName + "\n" +
						"INNER JOIN #atoms ON (#atoms.AtomID = "+sparseTableName+".AtomID);\n");
				sql.append("DELETE "+denseTableName+" FROM " + denseTableName + "\n" +
						"INNER JOIN #atoms ON (#atoms.AtomID = "+denseTableName+".AtomID);\n");
				
				sql.append("DROP TABLE #atoms;\n");
				//This separation is necessary!!
				// SQL Server parser is stupid and if you create, delete, and recreate a temporary table
				// the parser thinks you're doing something wrong and will die.
				if(progressBar.wasTerminated()){
					sql = new StringBuilder();
					sql.append("DROP TABLE #collections;\n");
					stmt.execute(sql.toString());
					stmt.close();
					return false;
				}
				System.out.println(sql.toString());
				stmt.execute(sql.toString());
				sql = new StringBuilder();
				
			}
			
			sql.append("DROP TABLE #collections;\n");
			System.out.println(sql.toString());
			stmt.execute(sql.toString());
			isDirty = false;
			stmt.close();
			//updateAncestors(0);
		} catch (Exception e){
			ErrorLogger.writeExceptionToLog("SQLServer","Exception deleting collection.");
			System.err.println("Exception deleting collection: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

}
