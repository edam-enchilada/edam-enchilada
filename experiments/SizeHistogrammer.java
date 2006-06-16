package experiments;
import database.*;
import java.sql.*;
import analysis.clustering.o.*;

public class SizeHistogrammer {
	private static final int collID = 24;
	
	
	public SizeHistogrammer() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		SQLServerDatabase db = new SQLServerDatabase();
		if (!db.openConnection()) throw new RuntimeException();
		
		Connection conn = db.getCon();
		Statement s = conn.createStatement();
		s.execute("SELECT SIZE FROM ATOFMSAtomInfoDense WHERE AtomID IN " +
				"( SELECT AtomID FROM InternalAtomOrder WHERE " +
				"CollectionID = "+collID+" )");
		ResultSet set = s.getResultSet();
		
		HistList histogram = new HistList(0.02f);
		
		while (set.next()) {
			histogram.addPeak(set.getFloat(1));
		}
		
		for (int i = 0; i < histogram.size(); i++) {
			System.out.println(histogram.getIndexMiddle(i)
					+ "\t" + histogram.get(i));
			if (histogram.getIndexMiddle(i) > 2) break;
		}
		
		db.closeConnection();
	}

}
