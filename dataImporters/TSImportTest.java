package dataImporters;

import junit.framework.TestCase;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import database.SQLServerDatabase;

public class TSImportTest extends TestCase {
	private String taskFile;
	private SQLServerDatabase db;
	private static final int NUM_PARTICLES = 5000;
	private File tsFile;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		
		new database.CreateTestDatabase();
		db = new SQLServerDatabase("TestDB");
		if (! db.openConnection()) {
			throw new Exception("Couldn't open DB con");
		}
		
		tsFile = makeTestFile();
		taskFile = makeTaskFile(tsFile);
	}

	private File makeTestFile() throws IOException {
		File f = File.createTempFile("tsFile",".csv");
		PrintWriter ts = new PrintWriter(f);
		
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat dForm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ts.println("Time,Val");
		
		for (int i = 0; i < NUM_PARTICLES; i++) {
			ts.println(dForm.format(c.getTime())+","+i);
			c.add(Calendar.SECOND, 30);
		}
		
		ts.close();
		
		return f;
	}
	
	private String makeTaskFile(File tsFile) throws IOException {
		File taskF = File.createTempFile("taskFile",".task");
		PrintWriter task = new PrintWriter(taskF);
		
		task.println(tsFile.getName() +",Time,Val");
		task.close();
		return taskF.getPath();
	}
	
	/*
	 * Test method for 'dataImporters.TSImport.read(String)'
	 */
	public void testRead() {
		TSImport imp = new TSImport(db, null);
		
		System.out.println("Inserting " + NUM_PARTICLES + " particles.");
		System.out.println("Started at " + new Date());
		try {
			if (! imp.readTaskFile(taskFile)) {
				fail();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		System.out.println("Ended at   " + new Date());
	
		
		// note that we don't test whether the data got there ok, whether it's in order, or anything like that.  oops.
	}
	
	public void tearDown() {
		tsFile.delete();
		new File(taskFile).delete();
	}
}
