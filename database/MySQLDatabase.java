package database;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import database.Database.BatchExecuter;
import database.Database.BatchInserter;
import database.Database.BulkInserter;
import database.Database.Inserter;
import database.SQLServerDatabase.StringBatchExecuter;

public class MySQLDatabase extends Database {
	public MySQLDatabase() {
		url = "localhost";
		port = "3306";
		database = "SpASMSdb";
		loadConfiguration("MySQL");
	}
	
	public MySQLDatabase(String dbName) {
		this();
		database = dbName;
	}
	
	public boolean isPresent() {
		return isPresentImpl("SHOW DATABASES");
	}
	
	public boolean openConnection() {
		return openConnectionImpl(
				"com.mysql.jdbc.Driver",
				"jdbc:mysql://" + url + ":" + port + "/" + database,
				"root",
				"sa-account-password");
	}
	
	public DateFormat getDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	protected BatchExecuter getBatchExecuter(Statement stmt) {
		return new BatchBatchExecuter(stmt);
	}
	
	protected class BatchBatchExecuter extends BatchExecuter {
		public BatchBatchExecuter(Statement stmt) {
			super(stmt);
		}
		
		public void append(String sql) throws SQLException {
			stmt.addBatch(sql);
		}

		public void execute() throws SQLException {
			stmt.executeBatch();
		}
	}
	

	protected Inserter getBulkInserter(BatchExecuter stmt, String table) {
		return new BulkInserter(stmt, table) {
			protected String getBatchSQL() {
				return "LOAD DATA INFILE '" + tempFilename.replaceAll("\\\\", "\\\\\\\\") 
					+ "' INTO TABLE " + table + 
					" FIELDS TERMINATED BY ','" +
					" ENCLOSED BY '\\''" + 
					" ESCAPED BY '\\\\'";
			}
		};
	}
	
	public void notdone() {
		StackTraceElement sti = null;
		try {
			throw new Exception();
		}
		catch (Exception ex) {
			sti = ex.getStackTrace()[1];
		}
		
		String message = "Not done: ";
		message += sti.getClassName() + "." + sti.getMethodName();
		message += "(" + sti.getFileName() + ":" + sti.getLineNumber() + ")";
		System.err.println(message);
	}

	
}
