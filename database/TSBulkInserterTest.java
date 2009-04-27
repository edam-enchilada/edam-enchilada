package database;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	
	InfoWarehouse db;
	
	
	protected void setUp() throws Exception {
		new CreateTestDatabase();
		db = Database.getDatabase("TestDB");
		db.openConnection("TestDB");
		ins = new TSBulkInserter(db);
	}

	protected void tearDown() throws Exception {
		db.closeConnection();
	}
	
	/*
	 * Test method for 'database.TSBulkInserter.addPoint(Date, Float)'
	 */
	public void testAddOnePoint() {
		TreeMap<Date, Float> data = new TreeMap<Date, Float>();
		Date initialDate = new Date();
		data.put(initialDate, new Float(0));
		insertAndTest(data, initialDate);
	}
	
	public void testAdd500Points() {
		TreeMap<Date, Float> data = new TreeMap<Date, Float>();
		Date initialDate = new Date();
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(initialDate.getTime());
		for (int i = 0; i < 500; i++) {
			data.put(c.getTime(), new Float(i));
			
			c.add(Calendar.SECOND, 30);
		}
		insertAndTest(data, initialDate);
	}

	// test to see if we still get out of memory exceptions on big collections
	public void testAdd30000Points() {
		TreeMap<Date, Float> data = new TreeMap<Date, Float>();
		Date initialDate = new Date();
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(initialDate.getTime());
		for (int i = 0; i < 30000; i++) {
			data.put(c.getTime(), new Float(i));
			
			c.add(Calendar.SECOND, 30);
		}
		insertAndTest(data, initialDate);
	}
	
	private void insertAndTest(Map<Date,Float> data, Date initialDate) {
		ins.startDataset("test coll");
		
		Iterator<Entry<Date, Float>> i = data.entrySet().iterator();

		try {
			// run it
			while (i.hasNext()) {
				Entry<Date,Float> e = i.next();
				ins.addPoint(e.getKey(), e.getValue());
			}
			ins.commit();

			// test it
			Connection con = db.getCon();
			PreparedStatement ps = con.prepareStatement("select atomid, time, value from timeseriesatominfodense where atomid = ?");
			ResultSet rs;
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(initialDate);
			cal.set(Calendar.MILLISECOND, 0);
			initialDate.setTime(cal.getTimeInMillis());

			ps.setInt(1, 22);
			rs = ps.executeQuery();
			assertTrue(rs.next());
			assertEquals(initialDate.getTime(), rs.getTimestamp("time").getTime());
			assertEquals(0f, rs.getFloat("value"));
			if (data.size() > 1)
			{
				// check the middle one
				int half = data.size() / 2;
				cal.add(Calendar.SECOND, 30 * half);
				initialDate.setTime(cal.getTimeInMillis());

				ps.setInt(1, 22 + half);
				rs = ps.executeQuery();
				assertTrue(rs.next());
				assertEquals(initialDate.getTime(), rs.getTimestamp("time").getTime());
				assertEquals(0f + half, rs.getFloat("value"));
				
				// check the last one
				cal.add(Calendar.SECOND, 30 * (data.size() - half - 1));
				initialDate.setTime(cal.getTimeInMillis());

				ps.setInt(1, 22 + data.size() - 1);
				rs = ps.executeQuery();
				assertTrue(rs.next());
				assertEquals(initialDate.getTime(), rs.getTimestamp("time").getTime());
				assertEquals(0f + data.size() - 1, rs.getFloat("value"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}

		return;
		// starts at 22, date, 0
		
	}

}
