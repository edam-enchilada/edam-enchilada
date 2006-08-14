package experiments;
import ATOFMS.ParticleInfo;
import analysis.BinnedPeakList;
import collection.Collection;
import database.CollectionCursor;
import database.InfoWarehouse;
import database.SQLServerDatabase;
import java.sql.*;

/**
 * Benchmark demonstrating that BPLOnlyCursor is many times faster (15?) than
 * older types.
 * 
 * @author smitht
 *
 */

public class PeakListCursorExperiment {
	public static void main(String[] args) throws SQLException {
		int count = 0;
		long newStart, newEnd, oldStart, oldEnd;
		InfoWarehouse db = chartlib.hist.HistogramsPlot.getDB();
		
		SQLServerDatabase.BPLOnlyCursor newtype 
			= db.getBPLOnlyCursor(db.getCollection(24));
		
		newStart = System.currentTimeMillis();
		Tuple<Integer, BinnedPeakList> t;
		
		while (newtype.hasNext()) {
			t = newtype.next();
		}
		
		newtype.close();
		newEnd = System.currentTimeMillis();
		System.out.println();
		System.out.println("New type completed in " + ( newEnd - newStart ) + 
				" milliseconds.");

		oldStart = System.currentTimeMillis();
		
		collection.Collection coll = db.getCollection(24);
		CollectionCursor particleCursor = db.getBinnedCursor(coll);
		
		while (particleCursor.next()) {
			ParticleInfo pInfo = particleCursor.getCurrent();
			BinnedPeakList bpl = pInfo.getBinnedList();
		}
		
		particleCursor.close();
		
		oldEnd = System.currentTimeMillis();
		System.out.println();
		System.out.println("Old type completed in " + ( oldEnd - oldStart ) + 
		" milliseconds.");


		
		
		
		
		
	}
}
