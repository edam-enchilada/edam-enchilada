package database;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import errorframework.ErrorLogger;

import junit.framework.TestCase;

/**
 * @author steinbel
 *
 */
public class DynamicTableGeneratorTest extends TestCase {
	
	private CreateTestDatabase2 ctd;
	private InfoWarehouse db;
	private Connection con;
	private ArrayList<File> metaFiles;
	private DynamicTableGenerator dtg;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		ctd = new CreateTestDatabase2();
		metaFiles = ctd.createMetaFiles();
		db = Database.getDatabase("TestDB2");
		db.openConnection();
		con = db.getCon();
		dtg  = new DynamicTableGenerator(con);
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		for (File f: metaFiles)
			f.delete();
		
		con.close();
		db.closeConnection();
		super.tearDown();
	}


	/**@author steinbel
	 * Test method for {@link database.DynamicTableGenerator#createTables(java.lang.String)}.
	 * All the parsing and processing is called by createTables() and dependent
	 * on reading in a file.
	 */
	public final void testCreateTables() {
		//first time should work
		dtg.createTables(metaFiles.get(1).getName());
		//check with database for correct insertion
		Statement stmt;
		String query;
		ResultSet rs;
		try {
			//check MetaData to see if info is there
			stmt = con.createStatement();
			query = "USE TestDB2 SELECT COUNT(*) FROM MetaData WHERE "
				+ "Datatype = 'SimpleParticle'";
			rs = stmt.executeQuery(query);
			assertTrue("nothing in the result set", rs.next());
			assertEquals("Not inserted into MetaData properly", rs.getInt(1), 9);
			
			//make sure the tables themselves have been created
			query = "SELECT COUNT(*) FROM SimpleParticleDataSetInfo";
			rs = stmt.executeQuery(query);
			assertTrue("no dataset info table", rs.next());
			
			query = "SELECT COUNT(*) FROM SimpleParticleAtomInfoDense";
			rs = stmt.executeQuery(query);
			assertTrue("no dense info table", rs.next());
			
			query = "SELECT COUNT(*) FROM SimpleParticleAtomInfoSparse";
			rs = stmt.executeQuery(query);
			assertTrue("no dense info table", rs.next());
		
			//avert the pop-up as it would require a response
			ErrorLogger.testing = true;
		
			//test with file that uses dangerous column names.
			dtg.createTables(metaFiles.get(2).getName());
			
			//make sure that none of the dangerous information is in MetaData
			query = "USE TestDB2 SELECT COUNT(*) FROM MetaData WHERE "
				+ "Datatype = 'BadExample'";
			rs = stmt.executeQuery(query);
			assertTrue("Problems getting result set", rs.next());
			assertEquals("BadExample info still in MetaData", rs.getInt(1), 0);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("problem checking db.");
		}
		
	}



}
