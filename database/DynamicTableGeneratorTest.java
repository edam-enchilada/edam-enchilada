package database;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * @author steinbel
 *
 */
public class DynamicTableGeneratorTest extends TestCase {
	
	private CreateTestDatabase2 ctd;
	private SQLServerDatabase db;
	private Connection con;
	private ArrayList<File> metaFiles;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		ctd = new CreateTestDatabase2();
		metaFiles = ctd.createMetaFiles();
		db = new SQLServerDatabase("TestDB2");
		db.openConnection();
		con = db.getCon();
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		con.close();
		db.closeConnection();
		super.tearDown();
	}

	/**
	 * Test method for {@link database.DynamicTableGenerator#characters(char[], int, int)}.
	 */
	public final void testCharacters() {
	}

	/**
	 * Test method for {@link database.DynamicTableGenerator#createTables(java.lang.String)}.
	 */
	public final void testCreateTables() {
		// TODO
	}

	/**
	 * Test method for {@link database.DynamicTableGenerator#createDynamicTables(java.lang.String)}.
	 */
	public final void testCreateDynamicTables() {
		// TODO
	}

}
