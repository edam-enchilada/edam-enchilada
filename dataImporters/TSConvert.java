package dataImporters;

import java.awt.Component;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.ProgressMonitorInputStream;

/* Conversion from time-series files to the XML format
   The major method is convert(String task_file), where the task_file specifies
   the conversion task (see upload.task for example). 
 */

public class TSConvert{
	private Component parent;
	
	private List<String> outFileNames;
	
    static String usage = "USAGE: java TSConvert task_file\n"+
                          " E.g., java TSConvert upload.task\n";

    static SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TSConvert() {
    	super();
    	outFileNames = new ArrayList<String>();
    }
    
    public static void main(String[] argv){
        if(argv.length < 1){
            System.out.println(usage+"Please specify the task file!");
            return;
        }
        try{
            TSConvert converter = new TSConvert();
            converter.convert(argv[0]);
        }catch(Exception e){
            System.out.print(e.getMessage()+"\n");
        }
    }
    
    public void convert(String task_file) throws Exception{
    	File task = new File(task_file);
    	String prefix = task.getParent();
        BufferedReader in = new BufferedReader(new FileReader(task_file));
        int line_no = 0;
        String line;
        while((line = in.readLine()) != null){
            line_no++;
            line = line.trim();
            if(line.charAt(0) == '#') continue;
            process(line.split("\\s*,\\s*"), task_file, line_no, prefix);
        }

    }

    // args[0]: file name
    // args[1]: time-series column
    // args[2 ...]: value columns
    void process(String[] args, String task_file, int line_no, String prefix)
    throws Exception{
        System.out.println("Processing "+args[0]+" ...");
        if(args.length < 3)
            throw new Exception("Error in "+task_file+" at line "+line_no+": The correct format is FileName, TimeColumn, ValueColumn1, ...\n");
        final BufferedReader in = new BufferedReader(
        		new InputStreamReader(
        				new ProgressMonitorInputStream(
        						parent,
        						"Reading csv file: "+args[0],
        						new FileInputStream(prefix+File.separator+args[0]))));
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
//        Thread readThr = new Thread() {
//		    public void run() {
//		    	String line;
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
//		    	return;
//		    }
//        };
//        readThr.start();
//        // this sucks, yes.
//        readThr.join();
        String outFName = prefix+File.separator+args[0]+".ed";
        outFileNames.add(outFName);
        PrintStream out = new PrintStream(outFName);
        printBegin(out);
        for(int i=2; i<values.length; i++){
            output(out,args[i],values[1],values[i]);
        }
        printEnd(out);
        out.close();
    }

    void output(PrintStream out, String name, ArrayList time, ArrayList value){
        out.print(
           "  <datasetinfo dataSetName=\""+name+"\">\n"+
           "    <field>-1</field> <!-- value should be set to -1 -->\n"+
           "    <field>0</field> <!-- value should be set to 0 -->\n"
        );
        for(int i=0; i<time.size(); i++){
            out.print(
               "    <atominfodense>\n"+
               "      <field>"+time.get(i)+"</field>\n"+
               "      <field>"+value.get(i)+"</field>\n"+
               "    </atominfodense>\n"
            );
        }
        out.print("  </datasetinfo>");
    }

    void printBegin(PrintStream out){
        out.print(
                  "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
                  "<!DOCTYPE enchiladadata SYSTEM \"temp/enchilada.dtd\">\n"+
                  "<enchiladadata datatype=\"TimeSeries\">\n"
        );
    }

    void printEnd(PrintStream out){
        out.print("</enchiladadata>\n");
    }
    
    public List<String> getOutFiles() {
    	return outFileNames;
    }

	public void setParent(Component parent) {
		this.parent = parent;
	}
}
