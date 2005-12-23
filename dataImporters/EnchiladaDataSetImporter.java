package dataImporters;

import gui.EnchiladaDataTableModel;


import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

import javax.swing.ProgressMonitorInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import database.SQLServerDatabase;
import externalswing.ProgressTask;

/**
 * 
 * @author steinbel
 *
 * Handles importation of .ed files.
 */

public class EnchiladaDataSetImporter extends DefaultHandler {
	
	private String data = "";
	private SQLServerDatabase db;
	private Connection con;
	private Statement stmt;
	private String datatype;
	private boolean inDataSetInfo = false;
	private boolean inAtomInfoDense = false;
	private boolean inAtomInfoSparse = false;
	private String dataSetName;
	private String sparseName;
	private int atomID;
	private String DSIparams;
	private String AIDparams;
	private int collectionID;
	private int datasetID;
	private TreeMap<String, ArrayList<String>> AISinfo;
	private static final String quote = "'";
	private Frame parent;
	
	public EnchiladaDataSetImporter(SQLServerDatabase sqlsdb){
		
		db = sqlsdb;
		con = db.getCon();
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.err.println("SQL problems creating statement.");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Collect the filenames from the gui table, check each one to make sure it
	 * has the correct extension (.ed).
	 * 
	 * @param table	The EnchiladaDataTableModel to get names from.
	 * @return ArrayList<String>	The filenames contained in the table.
	 */
	public ArrayList<String> collectTableInfo(EnchiladaDataTableModel table) {
		
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
		}
		return tableInfo;
	}
	
	/**
	 * Takes in a list of filenames and imports each file in turn.
	 * 
	 * @param fileNames
	 * @return the collectionID
	 */
	public int importFiles(List<String> fileNames){
				
		for (String eachFile : fileNames)
			read(eachFile);
		return collectionID;
		
	}
	
	/**
	 * Imports a list of filenames like importFiles, but with a cute GUI
	 * and makes sure not keep the GUI from redrawing (if you're doing that
	 * yourself, use importFiles()).
	 * 
	 * @param fileNames
	 * @return the collectionID
	 */
	public int importFilesThreaded(List<String> fileNames) {
		final List<String> fNames = fileNames;
		ProgressTask task = new ProgressTask(parent, "Importing Enchilada Data",true)
		{
			public void run() {
				pSetMax(fNames.size());
				pSetVal(0);
				for (String eachFile : fNames) {
					setStatus("Importing "+eachFile);
					read(eachFile);
					pInc();
				}
			}
		};
		task.start();
		return collectionID;
	}
	
	public void importStreamThreaded(final InputStream in) {
		ProgressTask task = new ProgressTask(parent, "Importing Enchilada Data",true)
		{
			public void run() {
				pSetInd(true);
				setStatus("Importing...");
				read(in);
			}
		};
		task.start();
	}

	/**
	 * Given a .ed filename, sets up to parse that xml file and stores the 
	 * information in the relevant tables in the database.  
	 * Helper function for importFiles().
	 * 
	 * @param fileName - the xml (.ed) file to read
	 */
	public void read(String fileName) {
		try {
			read(new BufferedInputStream(
//					new ProgressMonitorInputStream(
//							parent,
//							"Reading data from " + fileName,
							new FileInputStream(fileName)));			
		} catch (IOException e) {
			e.printStackTrace();
			// TODO gui.
		}
	}
	
	public void read(InputStream inStream) {
		SAXParserFactory factory = SAXParserFactory.newInstance();

		//validate the XML to make sure it's all nice and legal
		factory.setValidating(true);
		
		EnchiladaDataSetImporter handler = this;
		
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(inStream, handler);
			
		} catch (ParserConfigurationException e) {
			// TODO make GUI
			System.err.println("Parser Configuration error.");
			e.printStackTrace();
		}
		/*
		 * below code is from the Java tutorial on XML:
		 * @see http://java.sun.com/xml/jaxp/dist/1.1/docs/tutorial/sax/6_val.html
		 */
		catch (SAXParseException spe){
			//error generated by the parser
			System.out.println("\n** Parsing error, line " +
					spe.getLineNumber() + ", uri " +
					spe.getSystemId());
			System.out.println("    " + spe.getMessage());
			
			//Unpack the delivered exception to get the exception it contains
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();
		}
		catch (SAXException e) {
			// TODO make GUI
			System.err.println("SAX exception.  Incorrect file format.");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO make GUI
			System.err.println("IO Exception.");
			e.printStackTrace();
		}
	
	}


	/**
	 * Called each time a starting element in encountered, this method sets
	 * boolean tags for which tables the information belongs to and what they are
	 * named.
	 */
	public void startElement(String namespaceURI,
			String lName, // local name
			String qName, // qualified name
			Attributes attrs)
	throws SAXException {
		
		String eName = lName; // element name
		if ("".equals(eName)) 
			eName = qName; // namespaceAware = false
		
		//System.out.println(eName);//debugging
		
		//handle different tags
		if (eName.equals("enchiladadata")){
			datatype = attrs.getValue(0);
			//System.out.println(datatype);//debugging
		}
		else if (eName.equals("datasetinfo")){
			inDataSetInfo = true;
			dataSetName = attrs.getValue(0);
			DSIparams="";
		}
		else if (eName.equals("atominfodense")){
			inAtomInfoDense = true;
			atomID = db.getNextID();
			AIDparams="";
			
			//set up to receive sparse info
			AISinfo = new TreeMap<String, ArrayList<String>>();
			
			//if this is the first particle, create a new dataset & collection
			if (!DSIparams.equals("")){
				String comment = ""; //what do we really want here?
				int[] collectionInfo = db.createEmptyCollectionAndDataset(
						datatype,
						0,
						dataSetName,
						comment,
						DSIparams);
				collectionID = collectionInfo[0];
				datasetID = collectionInfo[1];
				DSIparams = ""; //reset this
			}
		}
		else if (eName.equals("atominfosparse")){
			inAtomInfoSparse = true;
			sparseName = "AtomInfoSparse" + attrs.getValue(0);
			
			//check if this flavor of AIS has an entry in AISinfo yet,
			//create one if necessary
			if (!AISinfo.containsKey(sparseName))
				AISinfo.put(sparseName, new ArrayList<String>());
			
			//initialize AISparams string
			AISinfo.get(sparseName).add("");
		}
		
	}
	
	/**
	 * This method resets flags and names when exiting different elements.
	 */
	public void endElement(String namespaceURI,
			String sName, // simple name
			String qName  // qualified name
	)throws SAXException{
		
		/*
		 * First, grab all the CDATA that has been assembled in characters().
		 * (Probably a more elegant way to do this . . . )
		 * Because the parser checks to make sure the elements are in order,
		 * we can check in reverse-heirarchical order to reduce if statements.
		 */
		if (qName.equals("field")){
			//if it's a sparse info field, add it to the last sparse entry for this
			//sparse table
			if (inAtomInfoSparse){
				ArrayList<String> list = AISinfo.get(sparseName);
				String AISparams = list.get(list.size()-1);
				AISparams = intersperse(data, AISparams);
				//replace old with new
				list.remove(list.size()-1);
				list.add(AISparams);
				//System.out.println("AISparams: " + AISparams);//debugging
			}
			else if (inAtomInfoDense){
				
				AIDparams = intersperse(data, AIDparams);
				// System.out.println("AIDparams: " + AIDparams);//debugging
			}
			else if (inDataSetInfo){
				DSIparams = intersperse(data, DSIparams);
				// System.out.println("DSIparams: " + DSIparams);//debugging
			} 
			data = "";
		}
		
		/*at the end of the document, push all the information to the database*/
		if (qName.equals("enchiladadata")){
			try {
				stmt.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (qName.equals("datasetinfo")){
			inDataSetInfo = false;
			dataSetName = null;
		}
		else if (qName.equals("atominfodense")){
			inAtomInfoDense = false;
			db.insertParticle(AIDparams, AISinfo, db.getCollection(collectionID), 
					collectionID, atomID);
		}
		else if (qName.equals("atominfosparse")){
			inAtomInfoSparse = false;
			sparseName = null;
		}
	}
	
	/**
	 * Handles the incoming data by inserting it into the correct tables in the
	 * database according to the boolean flags set by the element tags.
	 */
	public void characters(char[] buf, int offset, int len)
	throws SAXException{
		
		
		String temp = new String(buf, offset, len);
		data = data.concat(temp);
		//System.out.println(data);//debugging
		
	}
	
	/**
	 * Creates a comma-separated string (with all string surrounded by 
	 * single quotes) from an existing string and an addition.
	 * 
	 * @param add		The string to add onto the end of params.
	 * @param params	The existing string.
	 * @return	The comma-separated string.
	 */
	public String intersperse(String add, String params){
		
		//separate out the numbers from the real men!
		try{
			Float number = new Float(add);
			
			if (params.equals(""))
				params = add;
			else
				params = params + "," + add;
			
		}
		//if not a number, surround in single quotes, or it's empty so it's NULL
		catch (NumberFormatException e){
			
			if (add.equals("")) {
			
				if (params.equals(""))
					params = "NULL";
				else
					params = params + ",NULL";
						
			} else {
				
				if (params.equals(""))
					params = quote + add + quote;
				else
					params = params + "," + quote + add + quote;
			}	
		}
		
		return params;
		
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
	throws FileNotFoundException
	{
		if ((systemId != null && systemId.endsWith("enchilada.dtd"))
				|| (publicId != null && publicId.endsWith("enchilada.dtd"))) {
			// XXX: make this sensitive to where Enchilada is installed.
			return new InputSource(new FileInputStream(
				"importation files\\enchilada.dtd"));
		} else {
			return null;
		}
	}

	public void setParent(Frame parent) {
		this.parent = parent;
	}
}
