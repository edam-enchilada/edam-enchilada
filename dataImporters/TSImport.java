package dataImporters;

import java.awt.Frame;
import java.io.*;
import java.sql.SQLException;
import java.text.*;
import java.util.*;

import javax.swing.ProgressMonitorInputStream;

import collection.Collection;

import database.SQLServerDatabase;
import database.TSBulkInserter;

import errorframework.DisplayException;
import errorframework.ErrorLogger;
import errorframework.WriteException;
import externalswing.ProgressTask;

/**
 * TSImport.java - Import a list of CSV files to the database.
 * @author smitht
 */

/*
 * Complete list of needed SQLServerDatabase methods:
 * getNextID - get new AtomID
 * createEmptyCollectionAndDataset
 * insertParticle
 * getCollection? what's that?
 */

public class TSImport{
	private boolean failed = false;
	
	private SQLServerDatabase db;
	
	private Frame parent;
	
	private ProgressTask convTask;
	
	public static final String dfString = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat dateformatter = new SimpleDateFormat(dfString);

    public TSImport(SQLServerDatabase db, Frame parent) {
    	super();
    	this.parent = parent;
    	this.db = db;
	}
	
    public boolean read(String task_file) throws DisplayException, WriteException {
    	final String tf = task_file;
    	if (! tf.endsWith("task")) {
    		// They haven't given us a task file!
    		throw new DisplayException("Currently, to import any CSV" +
    				" file, a .task file must be used." +
    				"  See 'importation files\\demo.task' in the installation " +
    				"directory.");
    	}
    	
    	final File task = new File(task_file);
    	final String prefix = task.getParent();
    	final BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(task_file));
		} catch (FileNotFoundException e1) {
			throw new WriteException(task_file+" is not found.  Please check the file name");
		}
    	convTask = new ProgressTask(parent, 
    			"Importing CSV Files", true) {
    		public void run() {  			
    			pSetInd(true);
    			this.pack();
    			int line_no = 0;
    			String line;
    			try {
    				// Made it so if a "" is encountered, while loop ends 
    				// (i.e. lines at the end of the 
    				while((line = in.readLine()) != null){
    					line = line.trim();
    					if (line.equals("")) continue;
    					if(line.charAt(0) == '#') continue;
    					line_no++;
    					setStatus(("CSV "+line_no+": "+line+"                          ")  // 26 spaces
    							.substring(0,25)+"...");
    					process(line.split("\\s*,\\s*"), tf, line_no, prefix);
    				}
    			} catch (InterruptedException e) {
    				failed = true;
    			} catch (Exception e) {
    				failed = true;
    				System.err.println(e.toString());
    				System.err.println("Exception while converting data!");
    				e.printStackTrace();
    				ErrorLogger.writeExceptionToLog("TSImport","Exception while converting data: " +e.toString());
    			}
    		}
    	};
    	// Since we called ProgressTask as a modal dialog, this call to .start()
    	// does not return until the task is completed, but the GUI gets 
    	// redrawn as needed anyway.  
    	convTask.start();
    	return ! failed;
    }

    // args[0]: file name
    // args[1]: time-series column
    // args[2 ...]: value columns
    private void process(String[] args, String task_file, int line_no, String prefix)
    throws Exception{
        System.out.println("Processing "+args[0]+" ...");
		
		if (convTask.terminate) throw new InterruptedException("Inter");
		
        if(args.length < 3)
            throw new Exception("Error in "+task_file+" at line "+line_no+": The correct format is FileName, TimeColumn, ValueColumn1, ...\n");
        final BufferedReader in = new BufferedReader(
        		new FileReader(prefix+File.separator+args[0]));
        String line = in.readLine();
        if(line == null || line.trim().equals(""))
            throw new Exception("Error in "+args[0]+" at line 1: The first line should be the list of column names\n");
        String[] column = line.split("\\s*,\\s*");
        final int[] colIndex = new int[args.length];
        for(int i=1; i<args.length; i++){
            boolean found = false;
            for(int j=0; j<column.length; j++){
                if(args[i].equals(column[j])){
                    colIndex[i] = j; found = true; break;
                }
            }
            if(!found) throw new Exception("Error in "+args[0]+" at line 1: Cannot find column name "+args[i]+", which is defined in "+task_file+" at line "+line_no+"\n");
        }
        final ArrayList[] values = new ArrayList[args.length];
        for(int i=1; i<values.length; i++)
        	values[i] = new ArrayList<String>(1000);
        try {
        	while((line = in.readLine()) != null){
        		if(line.trim().equals("")) continue;
        		String[] v = line.split("\\s*,\\s*");
        		for(int i=1; i<values.length; i++)
        			values[i].add(v[colIndex[i]]);
        	}
        } catch (IOException i) {
        	System.out.println(i.getMessage());
        }
        for(int i=2; i<values.length; i++){
        	if (convTask.terminate) throw new InterruptedException("dialog closed, probably");
            putDataset(args[i],values[1],values[i]);
        }
    }

    private void putDataset(String name, ArrayList<String> time, ArrayList<String> value)
    throws SQLException, UnsupportedFormatException, InterruptedException
    {
    	System.out.println("Putting a dataset: " +name);
    	TSBulkInserter ins = new TSBulkInserter(db);
    	ins.startDataset(name);
    	
    	TreeMap<String, ArrayList<String>> noSparseTables = new TreeMap<String, ArrayList<String>>(); 
    	
    	try {
    		for(int i=0; i<time.size(); i++) {
    			if (convTask.terminate) throw new InterruptedException("Time for the task to terminate!");
    			
    			ins.addPoint(dateformatter.parse(time.get(i)), 
    					Float.parseFloat(value.get(i)));
    		}
    	} catch (ParseException e) {
    		throw new UnsupportedFormatException(e.toString() + 
    				"  Expecting " +dfString);
    	}
    	ins.commit();
    }

    public static void main(String[] args) {
    	SQLServerDatabase db = new SQLServerDatabase("SpASMSdb");
    	db.openConnection();
    	
    	TSImport t = new TSImport(db, null);
    	
    	ArrayList<String> times = new ArrayList<String>();
    	times.add("2005-07-04 13:00:00");
    	
    	ArrayList<String> values = new ArrayList<String>();
    	values.add("37");
    	
    	try {
    		t.putDataset("WOOT", times, values);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }
 
	public class UnsupportedFormatException extends IOException {
		public UnsupportedFormatException(String message) {
			super(message);
		}
	}

    
}
