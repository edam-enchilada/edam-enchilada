package dataImporters;

import java.awt.Frame;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.ProgressMonitorInputStream;

import externalswing.ProgressTask;


/* Conversion from time-series files to the XML format
   The major method is convert(String task_file), where the task_file specifies
   the conversion task (see upload.task for example). 
 */

public class TSImport{
	private Frame parent;
	
	private List<String> outFileNames;
	
    static String usage = "USAGE: java TSConvert task_file\n"+
                          " E.g., java TSConvert upload.task\n";

    static SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TSImport() {
    	super();
    	outFileNames = new ArrayList<String>();
    }
    
    public void convert(String task_file, final OutputStream outStream) throws Exception{
    	final String tf = task_file;
    	final File task = new File(task_file);
    	final String prefix = task.getParent();
        final BufferedReader in = new BufferedReader(new FileReader(task_file));
        ProgressTask convTask = new ProgressTask(parent, 
        		"Converting CSV files to Enchilada Data", true) {
        	public void run() {
        		pSetInd(true);
        		this.pack();
        		int line_no = 0;
        		String line;
        		printBegin(new PrintStream(outStream));
        		try {
        			// Made it so if a "" is encountered, while loop ends 
        			// (i.e. lines at the end of the 
        			while((line = in.readLine()) != null && (line = line.trim()) != null){
        				//line = line.trim();
        				if(line.charAt(0) == '#') continue;
        				line_no++;
        				setStatus(("CSV "+line_no+": "+line)
        							.substring(0,25)+"...");
        				process(outStream, line.split("\\s*,\\s*"), tf, line_no, prefix);
        			}
        		} catch (Exception e) {
        			System.err.println(e.toString());
        			System.err.println("Exception while converting data!");
        			e.printStackTrace();
        			// TODO: a little more handling of this exception.
        		}
        		printEnd(new PrintStream(outStream));
        	}
        };
        // Since we called ProgressTask as a modal dialog, this call to .start()
        // does not return until the task is completed, but the GUI gets 
        // redrawn as needed anyway.  
        convTask.start();
        outStream.close();
    }

    // args[0]: file name
    // args[1]: time-series column
    // args[2 ...]: value columns
    void process(OutputStream outStream, String[] args, String task_file, int line_no, String prefix)
    throws Exception{
        System.out.println("Processing "+args[0]+" ...");
        if(args.length < 3)
            throw new Exception("Error in "+task_file+" at line "+line_no+": The correct format is FileName, TimeColumn, ValueColumn1, ...\n");
        final BufferedReader in = new BufferedReader(
        		new InputStreamReader(
//        				new ProgressMonitorInputStream(
//        						parent,
//        						"Reading csv file: "+args[0],
        						new FileInputStream(prefix+File.separator+args[0])));
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
        PrintStream out = new PrintStream(outStream);
        for(int i=2; i<values.length; i++){
            output(out,args[i],values[1],values[i]);
        }
    }

    void output(PrintStream out, String name, ArrayList time, ArrayList value){
    	// edsi 246, 260
    	out.print(
           "  <datasetinfo dataSetName=\""+name+"\">\n"+
           "    <field>-1</field> <!-- value should be set to -1 -->\n"+
           "    <field>0</field> <!-- value should be set to 0 -->\n"
        );
        for(int i=0; i<time.size(); i++){
            // edsi 251
        	out.print(
               "    <atominfodense>\n"+
               "      <field>"+time.get(i)+"</field>\n"+
               "      <field>"+value.get(i)+"</field>\n"+
               "    </atominfodense>\n"
            );
        	// edsi 316, 339
        }
        out.print("  </datasetinfo>");
        // edsi 
    }

    void printBegin(PrintStream out){
    	out.print(
    			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
    			"<!DOCTYPE enchiladadata SYSTEM \"temp/enchilada.dtd\">\n"
    	);
    
    	out.print(
                   "<enchiladadata datatype=\"TimeSeries\">\n"
        );
    }

    void printEnd(PrintStream out){
        out.print("</enchiladadata>\n");
    }
    
    public List<String> getOutFiles() {
    	return outFileNames;
    }

	public void setParent(Frame parent) {
		this.parent = parent;
	}
}
