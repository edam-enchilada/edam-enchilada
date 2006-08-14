package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import errorframework.ErrorLogger;

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
}
