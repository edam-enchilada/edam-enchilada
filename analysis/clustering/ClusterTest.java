package analysis.clustering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.dbunit.*;
import org.dbunit.database.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.dataset.excel.*;


import database.SQLServerDatabase;

/**
 * Still working on this one . . . having lots of trouble with importing
 * from Excel.
 * 
 * @author steinbel
 *
 */

public class ClusterTest extends DatabaseTestCase{

	private SQLServerDatabase db;
	private IDataSet loadedDataSet;

	public void testCheckDataLoaded() throws Exception
	{
		// IDataSet createdDataSet = getConnection().createDataSet(
		//		 new String[]{"CenterAtoms"});
		// assertNotNull(createdDataSet);
		     
		                                                              
		assertNotNull(loadedDataSet);
		XlsDataSet.write(loadedDataSet, new FileOutputStream("testExcel.xls"));
		//  int rowCount = loadedDataSet.getTable("MANUFACTURER").getRowCount();
		//  assertEquals(2, rowCount);
	}
	
	@Override
	protected IDatabaseConnection getConnection() throws Exception {
		db = new SQLServerDatabase("TestDB2");
		db.openConnection();
		IDatabaseConnection con = new DatabaseConnection(db.getCon());
		return con;
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		//File file = new File("C:/Documents And Settings/steinbel/workspace/Copy of edam-enchilada/testing/smallTest.xls");
		InputStream in =  this.getClass().getClassLoader().getResourceAsStream("testing/small.xml");
		loadedDataSet = new FlatXmlDataSet(in);
		
		return loadedDataSet;
	}

}
