package dataImporters;

import gui.EnchiladaDataTableModel;
import gui.MainFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import database.SQLServerDatabase;

/**
 * Class to import data from EnchiladaData format (.ed) files chosen by the
 * user in the gui table (EnchiladaDataTableModel).
 * 
 * @author steinbel
 *	6.30.05
 */

public class EnchiladaDataSetImporter{
	
	private boolean collectionContinues;
	private int collectionID;
	private int datasetID;
	private File file;
	private Scanner scan;
	private String datatype;
	private String lastFile;
	private String nextFile;
	private SQLServerDatabase db;
	private int atomID;
	private String atomInfoDense;
	private ArrayList<String> atomInfoSparse;
	private StringTokenizer tokenizer;
	
	/**
	 * Constructor sets initial variable values, calls methods to collect files'
	 * names from the table passed in and import the data from those files to 
	 * the database.
	 * 
	 * @param eTable	The EnchiladadataTableModel holding the filenames. 
	 */
	public EnchiladaDataSetImporter(EnchiladaDataTableModel eTable){
		
		collectionContinues = false;
		scan = null;
		datatype = "";
		nextFile = "";
		db = MainFrame.db;
		atomID = -99;
		
		ArrayList<String> fileNames = collectTableInfo(eTable);
		
		for (int i=0; i<fileNames.size(); i++){
			file = new File(fileNames.get(i));
			//System.out.println(fileNames.get(i));//debugging
			
			try {
				scan = new Scanner(file);
				String firstLine = scan.nextLine();
				
				if (firstLine.equalsIgnoreCase("BEGIN")){
					datatype = scan.nextLine();
					
					if (db.containsDatatype(datatype))
						importData();
					else 
						System.err.println("Database does not contains datatype "
								+ datatype);
					//TODO change into gui warning
				}
				
				//there shouldn't be anything except "BEGIN" unless the previous
				//file tagged for it
				else if (collectionContinues){
					
					//test to see if the filename of this file is same as nextFile
					//and the name of the last file is the file that is pointed to
					if (! nextFile.equals(file.getName())
					 	|| ! lastFile.equals(firstLine))
						//TODO: turn into a GUI warning
						System.err.println("Incorrect file order.");
					
					//test to see if the first line of this file is the same as
					//the last filename
					else if (! datatype.equals(scan.nextLine()))
						//TODO: change into gui
						System.err.println("Datatypes in continuing files should match");
				
					else 
						importData();
					
				}
				
			} catch (FileNotFoundException e) {
				//TODO change into gui warning
				System.err.println("File " + fileNames.get(i) + " not found.");
				e.printStackTrace();
			}
			
		}
		
	}

	/**
	 * Collect the filenames from the gui table, check each one to make sure it
	 * has the correct extension (.ed).
	 * 
	 * @param table	The EnchiladaDataTableModel to get names from.
	 * @return ArrayList<String>	The filenames contained in the table.
	 */
	private ArrayList<String> collectTableInfo(EnchiladaDataTableModel table) {
		
		ArrayList<String> tableInfo = new ArrayList<String>();
		int rowCount = table.getRowCount()-1;
		String name = "";
		String ext = "";
		
		for (int i=0;i<rowCount;i++) {
			
			name = (String)table.getValueAt(i, 1);
			ext = name.substring(name.length()-3, name.length());
			
			if (ext.equals(".ed"))
				tableInfo.add((String)table.getValueAt(i,1));
			else
				System.err.println("Incorrect file extension for file " + name);
				//TODO change into gui warning
			
		}
		return tableInfo;
	}
	
	/**
	 * Reads in the current file held by the class variable 'file,' creates
	 * new collections as necessary, inserts atom information into the database.
	 *
	 */
	private void importData() {
		
		String first;
		String restOfLine;
		String readyToInsert;
		
		while (scan.hasNext()){
			
			//for the case where the line starts with '*' (new dataset)
			first = scan.next();
			//System.out.println("first is " + first);
			if (first.length() == 1){
				
				//if there's already an atom's worth of info waiting, insert it
				if (atomID >= 0){
					db.insertParticle(atomInfoDense, atomInfoSparse, 
							db.getCollection(collectionID), datasetID,
							db.getNextID());
					//System.out.println("Current collectionID, datasetID and atomID "
							//+ collectionID + " " + datasetID + " " + atomID);
					atomID = -77;
				}
				
				restOfLine = scan.nextLine();
				//System.out.println(restOfLine);
				int[] IDs = createEmptyCollection(restOfLine);
				collectionID = IDs[0];
				datasetID = IDs[1];
				
			}
			//for ** case (AtomInfoDense)
			else if (first.length() == 2){
				
				//if there's already an atom's worth of info waiting, insert it
				if (atomID >= 0){
					db.insertParticle(atomInfoDense, atomInfoSparse, 
							db.getCollection(collectionID), datasetID,
							db.getNextID());
					//System.out.println("Current collectionID, datasetID and atomID "
							//+ collectionID + " " + datasetID + " " + atomID);
				}
				
				restOfLine = chop(scan.nextLine());
				//System.out.println(restOfLine);				
				//store the rest of the line's info in atomInfoDense
				atomInfoDense = restOfLine;
				//get an atomID to use til scan hits another atominfodense line
				atomID = db.getNextID();
				//reset atominfosparse
				atomInfoSparse = new ArrayList<String>();
				
			}
			//for *** case (AtomInfoSparse)
			else if (first.length() == 3){
				
				restOfLine = chop(scan.nextLine());
				//System.out.println(restOfLine);
					
				//if this line comes before any ** lines, atomInfoSparse won't
				//be instantiated yet, so we catch the nullpointer and tell
				//the user about the corrupt data file
				try{
					//insert the rest of the line's info into atomInfoSparse
					atomInfoSparse.add(restOfLine);
				}
				catch (NullPointerException e){
					//TODO: change into a GUI warning
					System.err.println("Corrupted .ed file, sparse data listed "
							+ "before dense data in " + file.getName());
				}
				
			}
			//for ^^^^^^^^ case (line before end of file)
			else if (first.length() == 8){
				
				//check if dataset continues
				String last = scan.next();
				//System.out.println(last);
				if (! last.equalsIgnoreCase("END")){
					
					nextFile = last;
					lastFile = file.getName();
					collectionContinues = true;
					
				}
				else{
					
					collectionContinues = false;
					//push the last atom's info into the database
					if (atomID >= 0)
						db.insertParticle(atomInfoDense, 
								atomInfoSparse,
								db.getCollection(collectionID), 
								datasetID, 
								atomID);
					collectionID = -1;
					datasetID = -1;
					atomID = -11;
				}
					
			}
			//if file format is incorrect, insert any remaining atoms and set
			//IDs to negative values, so the scanner skips until it hits a new
			//dataset
			else{
				//TODO: change into gui?  how do we handle corrupted files?
				System.err.println("Incorrect .ed file format for file " 
						+ file.getName());
				collectionContinues = false;
				if (atomID >= 0)
					db.insertParticle(atomInfoDense, 
							atomInfoSparse, 
							db.getCollection(collectionID), 
							datasetID, 
							atomID);
				collectionID = -1;
				datasetID = -1;
				atomID = -11;
			}
			
		}
		
		scan.close();
		
	}

	/**
	 * Creates a new empty collection and a new dataset in the database.
	 * 
	 * @param line	The line of the .ed file containing information about
	 * 				the new dataset.
	 * @return	int[] collectionInfo where collectionInfo[0] is the collectionID
	 * 								 and collectionInfo[1] is the datasetID
	 */
	private int[] createEmptyCollection(String line) {
		
		int[] collectionInfo;
		tokenizer = new StringTokenizer(line);
		String datasetName = tokenizer.nextToken();
		String data = line.substring(datasetName.length() + 1, line.length());
		data = chop(data);
		//System.out.println(data); //debugging
		String comment = ""; //what do we want to put in here??
		collectionInfo = db.createEmptyCollectionAndDataset(datatype, 
				0, datasetName, comment, data);
		
		return collectionInfo;
		
	}
	
	/**
	 * A helper method to turn space-delimited strings into comma-delimited 
	 * strings suitable for feeding to SQLServerDatabase.insertParticle();
	 * 
	 * @param whole	The string to be chopped up.
	 * @return	String	The initial string with commas instead of spaces between
	 * 					values.
	 */
	private String chop(String whole){
		
		String pieces = "";
		tokenizer = new StringTokenizer(whole);
		
		while (tokenizer.hasMoreTokens()){
			
			if (tokenizer.countTokens() > 1)
				pieces = pieces + tokenizer.nextToken() + ",";
			else
				pieces = pieces + tokenizer.nextToken();
				
		}
		
		return pieces;
		
	}
}