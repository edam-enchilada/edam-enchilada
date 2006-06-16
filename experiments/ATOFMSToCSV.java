package experiments;

import java.sql.*;

import database.SQLServerDatabase;

public class ATOFMSToCSV {

	public ATOFMSToCSV() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		SQLServerDatabase db = new SQLServerDatabase();
		if (!db.openConnection()) throw new RuntimeException();
		
		db.getBinnedCursor(db.getCollection(3));
	}

}
