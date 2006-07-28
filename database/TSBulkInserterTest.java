package database;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Note that I'm not clever enough to check if the particles we put in
 * actually get there intact.  Oops.  Sounds like something for DbUnit.
 * 
 * @author smitht
 */

public class TSBulkInserterTest extends TestCase {
	private TSBulkInserter ins;
	
	SQLServerDatabase db;
	
	
	public void setUp() throws Exception {
		db = new SQLServerDatabase();
		db.openConnection();
		ins = new TSBulkInserter(db);
	}

	/*
	 * Test method for 'database.TSBulkInserter.addPoint(Date, Float)'
	 */
	public void testAddOnePoint() {
		TreeMap<Date, Float> data = new TreeMap<Date, Float>();
		data.put(new Date(), new Float(3));
		insertAndTest(data);
	}
	
	public void testAdd500Points() {
		TreeMap<Date, Float> data = new TreeMap<Date, Float>();
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(System.currentTimeMillis());
		for (int i = 0; i < 500; i++) {
			data.put(c.getTime(), new Float(i));
			
			c.add(Calendar.SECOND, 30);
		}
		insertAndTest(data);
	}
	
	private void insertAndTest(Map<Date,Float> data) {
		ins.startDataset("test coll");
		
		Iterator<Entry<Date, Float>> i = data.entrySet().iterator();

		try {
			while (i.hasNext()) {
				Entry<Date,Float> e = i.next();
				ins.addPoint(e.getKey(), e.getValue());
			}
			ins.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

}
