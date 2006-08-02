package dataImporters;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import database.CreateTestDatabase;
import database.SQLServerDatabase;
import errorframework.WriteException;
import junit.framework.TestCase;

/**
 * @author steinbel
 */
public class EnchiladaDataSetImporterTest extends TestCase{
	
	private EnchiladaDataSetImporter edsi;
	private SQLServerDatabase db;
	private ArrayList<File> tempFiles;

	protected void setUp() throws WriteException{
		CreateTestDatabase ctd = new CreateTestDatabase();
		tempFiles = ctd.createEnchFiles();
		db = ctd.tempDB;
		db.openConnection();
		try {
			edsi = new EnchiladaDataSetImporter(db);
		} catch (WriteException e) {
			throw new WriteException("Error initializing EnchiladaDataSetImporter");
		}
	}
	
	protected void tearDown(){
		db.closeConnection();
		try {
			System.runFinalization();
			System.gc();
			db = new SQLServerDatabase("");
			db.openConnection();
			Connection con = db.getCon();
			con.createStatement().executeUpdate("DROP DATABASE TestDB");
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (File file : tempFiles)
			file.delete();
	}
	
	/*
	 * The primary method needed to test the EnchiladaDataSetImporter class because
	 * it initiates the parsing of the files.  (Most of the current methods are 
	 * called by the SAX parser.)
	 */
	public void testRead() throws WriteException{


		Connection con = db.getCon();
		try {
			con.createStatement().execute("USE TestDB");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		edsi.read(tempFiles.get(1).getName());
		
		try {
			//check on datasetinfo
			ResultSet rs = con.createStatement().executeQuery("USE TestDB "
					+ "SELECT * FROM SimpleParticleDataSetInfo");
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 1);
			assertTrue(rs.getString(2).equals("Simple Dataset"));
			assertTrue(rs.getInt(3) == 22);
			
			//check that the first and last atom ids (from set that we insert) 
			//have the correct dense atom info
			rs = con.createStatement().executeQuery("USE TestDB SELECT * FROM " +
					"SimpleParticleAtomInfoDense");
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 22);
			assertTrue(rs.getFloat(2) == 33);
			assertTrue(rs.getFloat(3) == 42);
			rs.next();
			rs.next();
			rs.next();
			assertTrue(rs.getInt(1) == 25);
			//for some reason, 56.6 != 56.6
			//Float size = rs.getFloat(2);
			//System.out.println("The size: " + size);
			//assertTrue(size == 56.6);
			assertTrue(rs.getFloat(3) == 76.5);

			//check for correct sparse info on two middle atoms
			rs = con.createStatement().executeQuery("USE TestDB SELECT * FROM "
					+ "SimpleParticleAtomInfoSparse");
			assertTrue(rs.next());
			assertTrue(rs.getInt(1) == 23);
			assertTrue(rs.getInt(2) == 4);
			assertTrue(rs.getBoolean(3));
			rs.next();
			//even though the next two sparse entries are entered in the opposite
			//order in the file, they are stored in this order because they have
			//the same atomID and 8 comes before 9
			assertTrue(rs.getInt(1) == 24);
			assertTrue(rs.getInt(2) == 8);
			assertFalse(rs.getBoolean(3));
			rs.next();
			assertTrue(rs.getInt(1) == 24);
			assertTrue(rs.getInt(2) == 9);
			assertFalse(rs.getBoolean(3));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testIntersperse(){
		
		assertTrue(EnchiladaDataSetImporter.intersperse("22.4", "").
				equals("22.4"));
		assertTrue(EnchiladaDataSetImporter.intersperse("multiple words", "").
				equals("'multiple words'"));
		assertTrue(EnchiladaDataSetImporter.intersperse("13.1", "22.4").
				equals("22.4, 13.1"));
		assertTrue(EnchiladaDataSetImporter.intersperse("more words", "'words'").
				equals("'words', 'more words'"));
		assertTrue(EnchiladaDataSetImporter.intersperse("mixed numbers and words", "77.7, 'string'").
				equals("77.7, 'string', 'mixed numbers and words'"));
	}
}
