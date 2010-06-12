//removed by jtbigwoo 5/30/2010

//package dataImporters;
//
//import gui.EnchiladaDataTableModel;
//import gui.ImportEnchiladaDataDialog;
//
//
//import java.awt.Frame;
//import java.io.BufferedInputStream;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import org.xml.sax.Attributes;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import org.xml.sax.SAXParseException;
//import org.xml.sax.helpers.DefaultHandler;
//
//import analysis.clustering.Cluster;
//
//import database.InfoWarehouse;
//import errorframework.*;
//import externalswing.ProgressTask;
//
///**
// * 
// * @author steinbel
// *
// * Handles importation of .ed files.
// */
//
//public class EnchiladaDataSetImporter extends DefaultHandler {
//	
//	private String data = "";
//	private InfoWarehouse db;
//	private Connection con;
//	private Statement stmt;
//	private String datatype;
//	private boolean inDataSetInfo = false;
//	private boolean inAtomInfoDense = false;
//	private boolean inAtomInfoSparse = false;
//	private String dataSetName;
//	private String sparseName;
//	private int atomID;
//	private String DSIparams;
//	private String AIDparams;
//	private String AISparams;
//	private int collectionID;
//	private int datasetID;
//	private ArrayList<String> AISinfo;
//	private Frame parent;
//	private ImportEnchiladaDataDialog ench;
//	
//	public EnchiladaDataSetImporter(InfoWarehouse sqlsdb) throws WriteException{
//		
//		db = sqlsdb;
//		con = db.getCon();
//		try {
//			stmt = con.createStatement();
//		} catch (SQLException e1) {
//			throw new WriteException("SQL problems creating Enchilada statement.");
//		}
//		
//	}
//	
//	/**
//	 * Collect the filenames from the gui table, check each one to make sure it
//	 * has the correct extension (.ed).
//	 * 
//	 * @param table	The EnchiladaDataTableModel to get names from.
//	 * @return ArrayList<String>	The filenames contained in the table.
//	 */
//	public ArrayList<String> collectTableInfo(EnchiladaDataTableModel table) 
//		throws DisplayException{
//		
//		ArrayList<String> tableInfo = new ArrayList<String>();
//		int rowCount = table.getRowCount()-1;
//		String name = "";
//		String ext = "";
//		
//		for (int i=0;i<rowCount;i++) {
//			
//			name = (String)table.getValueAt(i, 1);
//			ext = name.substring(name.length()-3, name.length());
//			
//			if (ext.equals(".ed"))
//				tableInfo.add((String)table.getValueAt(i,1));
//			else
//				throw new DisplayException("Incorrect file extension for file " + name);
//		}
//		return tableInfo;
//	}
//	
//	/**
//	 * Takes in a list of filenames and imports each file in turn.
//	 * 
//	 * @param fileNames
//	 * @return the collectionID
//	 */
//	public int importFiles(List<String> fileNames) throws WriteException{
//				
//		for (String eachFile : fileNames)
//			read(eachFile);
//		return collectionID;
//		
//	}
//	
//	/**
//	 * Imports a list of filenames like importFiles, but with a cute GUI
//	 * and makes sure not keep the GUI from redrawing (if you're doing that
//	 * yourself, use importFiles()).
//	 * 
//	 * @param fileNames
//	 * @return the collectionID
//	 */
//	public int importFilesThreaded(List<String> fileNames) {
//		final List<String> fNames = fileNames;
//		ProgressTask task = new ProgressTask(parent, "Importing Enchilada Data",true)
//		{
//			public void run() {
//				pSetMax(fNames.size());
//				pSetVal(0);
//				for (String eachFile : fNames) {
//					setStatus("Importing "+eachFile);
//					if (terminate) { return; }
//			try {
//				read(eachFile);
//			} catch (WriteException e) {
//				ErrorLogger.writeExceptionToLogAndPrompt("EnchiladaImporting",e.getMessage());
//			}
//								pInc();
//							}
//						}
//		};
//		task.start();
//		return collectionID;
//	}
//	
//	public void importStreamThreaded(final InputStream in) throws WriteException{
//		ProgressTask task = new ProgressTask(parent, "Importing Enchilada Data",true)
//		{
//			public void run() {
//				pSetInd(true);
//				if (terminate) {return;}
//				setStatus("Importing...");
//				try {
//					read(in);
//				} catch (WriteException e) {
//					ErrorLogger.writeExceptionToLogAndPrompt("EnchiladaImporting",e.getMessage());
//				}
//			}
//		};
//		task.start();
//	}
//
//	/**
//	 * Given a .ed filename, sets up to parse that xml file and stores the 
//	 * information in the relevant tables in the database.  
//	 * Helper function for importFiles().
//	 * 
//	 * @param fileName - the xml (.ed) file to read
//	 */
//	public void read(String fileName) throws WriteException{
//		try {
//			read(new BufferedInputStream(
////					new ProgressMonitorInputStream(
////							parent,
////							"Reading data from " + fileName,
//							new FileInputStream(fileName)));			
//		} catch (IOException e) {
//			e.printStackTrace();
//			// TODO gui.
//		}
//	}
//	
//	public void read(InputStream inStream) throws WriteException{
//		SAXParserFactory factory = SAXParserFactory.newInstance();
//
//		//validate the XML to make sure it's all nice and legal
//		factory.setValidating(true);
//		
//		EnchiladaDataSetImporter handler = this;
//		
//		try {
//			SAXParser parser = factory.newSAXParser();
//			parser.parse(inStream, handler);
//			
//		} catch (ParserConfigurationException e) {
//			throw new WriteException("Parser Configuration error for input stream "
//					+inStream.toString());
//		}
//		/*
//		 * below code is from the Java tutorial on XML:
//		 * @see http://java.sun.com/xml/jaxp/dist/1.1/docs/tutorial/sax/6_val.html
//		 */
//		catch (SAXParseException spe){
//			//error generated by the parser
//			
//			
//			//Unpack the delivered exception to get the exception it contains
//			Exception x = spe;
//			if (spe.getException() != null)
//				x = spe.getException();
//			x.printStackTrace();
//			throw new WriteException("\n** Parsing error, line " +
//					spe.getLineNumber() + ", uri " +
//					spe.getSystemId()+","+spe.getMessage());
//		}
//		catch (SAXException e) {
//			// TODO make GUI
//			throw new WriteException("SAX exception.  Incorrect file format.");
//		} catch (IOException e) {
//			throw new WriteException("IO Exception dealing with the parser.");
//		}
//	
//	}
//
//
//	/**
//	 * Called each time a starting element in encountered, this method sets
//	 * boolean tags for which tables the information belongs to and what they are
//	 * named.
//	 */
//	public void startElement(String namespaceURI,
//			String lName, // local name
//			String qName, // qualified name
//			Attributes attrs)
//	throws SAXException {
//		
//		String eName = lName; // element name
//		if ("".equals(eName)) 
//			eName = qName; // namespaceAware = false
//		
//		//System.out.println(eName);//debugging
//		
//		//handle different tags
//		if (eName.equals("enchiladadata")){
//			datatype = attrs.getValue(0);
//			//System.out.println(datatype);//debugging
//		}
//		else if (eName.equals("datasetinfo")){
//			inDataSetInfo = true;
//			dataSetName = attrs.getValue(0);
//			DSIparams="";
//		}
//		else if (eName.equals("atominfodense")){
//			inAtomInfoDense = true;
//			atomID = db.getNextID();
//			AIDparams="";
//			
//			//set up to receive sparse info
//			AISinfo = new ArrayList<String>();
//			
//			//if this is the first particle, create a new dataset & collection
//			if (!DSIparams.equals("")){
//				String comment = ""; //what do we really want here?
//				int[] collectionInfo = db.createEmptyCollectionAndDataset(
//						datatype,
//						0,
//						dataSetName,
//						comment,
//						DSIparams);
//				collectionID = collectionInfo[0];
//				datasetID = collectionInfo[1];
//				DSIparams = ""; //reset this
//			}
//		}
//		else if (eName.equals("atominfosparse")){
//			inAtomInfoSparse = true;
//			AISparams = "";
//			AISinfo.add(AISparams);
//		}
//		
//	}
//	
//	/**
//	 * This method resets flags and names when exiting different elements.
//	 */
//	public void endElement(String namespaceURI,
//			String sName, // simple name
//			String qName  // qualified name
//	)throws SAXException{
//		
//		/*
//		 * First, grab all the CDATA that has been assembled in characters().
//		 * (Probably a more elegant way to do this . . . )
//		 * Because the parser checks to make sure the elements are in order,
//		 * we can check in reverse-heirarchical order to reduce if statements.
//		 */
//		if (qName.equals("field")){
//			//if it's a sparse info field, add it to the last sparse entry
//			if (inAtomInfoSparse){
//				AISparams = AISinfo.get(AISinfo.size()-1);	
//				AISparams = Cluster.intersperse(data, AISparams);
//				//replace old with new
//				AISinfo.remove(AISinfo.size()-1);
//				AISinfo.add(AISparams);
//				//System.out.println("AISparams: " + AISparams);//debugging
//			}
//			else if (inAtomInfoDense){
//				
//				AIDparams = Cluster.intersperse(data, AIDparams);
//				// System.out.println("AIDparams: " + AIDparams);//debugging
//			}
//			else if (inDataSetInfo){
//				DSIparams = Cluster.intersperse(data, DSIparams);
//				// System.out.println("DSIparams: " + DSIparams);//debugging
//			} 
//			data = "";
//		}
//		
//		/*at the end of the document, push all the information to the database*/
//		if (qName.equals("enchiladadata")){
//			try {
//				stmt.executeBatch();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else if (qName.equals("datasetinfo")){
//			inDataSetInfo = false;
//			dataSetName = null;
//		}
//		else if (qName.equals("atominfodense")){
//			inAtomInfoDense = false;
//			db.insertParticle(AIDparams, AISinfo, db.getCollection(collectionID), 
//					datasetID, atomID);
//		}
//		else if (qName.equals("atominfosparse")){
//			inAtomInfoSparse = false;
//		}
//	}
//	
//	/**
//	 * Handles the incoming data by inserting it into the correct tables in the
//	 * database according to the boolean flags set by the element tags.
//	 */
//	public void characters(char[] buf, int offset, int len)
//	throws SAXException{
//		
//		
//		String temp = new String(buf, offset, len);
//		data = data.concat(temp);
//		//System.out.println(data);//debugging
//		
//	}
//	
//	@Override
//	public InputSource resolveEntity(String publicId, String systemId)
//	throws FileNotFoundException
//	{
//		if ((systemId != null && systemId.endsWith("enchilada.dtd"))
//				|| (publicId != null && publicId.endsWith("enchilada.dtd"))) {
//			// XXX: make this sensitive to where Enchilada is installed.
//			return new InputSource(new FileInputStream(
//				"importation files\\enchilada.dtd"));
//		} else {
//			return null;
//		}
//	}
//
//	public void setParent(Frame parent) {
//		this.parent = parent;
//	}
//}
