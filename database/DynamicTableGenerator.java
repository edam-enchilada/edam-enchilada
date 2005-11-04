package database;


import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author ritza
 * @author steinbel
 * 
 * DynamicTableGenerator acts as an importer for .md files, creating appropriate
 * dynamic tables in the database and inserting relevant information into the
 * MetaData table.
 *
 */
public class DynamicTableGenerator extends DefaultHandler {
	
	private String datatype;
	private boolean datasetinfo;
	private boolean atominfodense;
	private boolean atominfosparse;
		//the extra bit of name to be appended to 
		//"AtomInfoSparse" to distinguish one AIS table from another
	private HashMap<Integer, String> sparseNames = new HashMap<Integer,String>(); 
	private int sparseCounter = DynamicTable.AtomInfoSparse.ordinal();
	private String fieldType;
	private int primaryKey;
	private int columnCounter = 0;
	private Connection con;
	private ResultSet rs;
	private Statement stmt;
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws FileNotFoundException 
	{
		if ((systemId != null && systemId.endsWith("meta.dtd"))
				|| (publicId != null && publicId.endsWith("meta.dtd"))) {
			// the current directory is the application install directory.  yay!
			return new InputSource(new FileInputStream("meta.dtd"));
		} else {
			return null;
		}
	}
	
	
	
	/**
	 * Constructor requires a connection to the SQLServer database.
	 * @param connection
	 */
	public DynamicTableGenerator(Connection connection){
		con = connection;
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Make GUI
			System.err.println("SQL Exception creating statement.");
			e.printStackTrace();
		}
		
	}
	/**
	 * Given a .md filename, sets up to parse that xml file and stores the 
	 * information in the MetaData table in the database.  
	 * Helper function for createTables().
	 * 
	 * @param fileName - the xml (.md) file to read
	 */
	private void read(String fileName){
		SAXParserFactory factory = SAXParserFactory.newInstance();
		//validate the XML to make sure it's all nice and legal
		factory.setValidating(true);
		
		DynamicTableGenerator handler = this;
		

		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(fileName, handler);
			
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
	 * Use CDATA as column names in the MetaData table.
	 */
	public void characters(char[] buf, int offset, int len)
	  throws SAXException {
		
		int table;
		if (datasetinfo)
			table = DynamicTable.DataSetInfo.ordinal();
		else if (atominfodense)
			table = DynamicTable.AtomInfoDense.ordinal();
		//the only possible remaining option, since the XML is validated
		else{
			table = sparseCounter;
		}
		String s = new String(buf, offset, len);
		String statement = "INSERT INTO MetaData VALUES ('" + datatype +
			"','[" + s + "]','" + fieldType + "'," + primaryKey + ","
			+ table +"," + columnCounter + ")";
		columnCounter++;
		System.out.println(statement);
		try {
			stmt.addBatch(statement);
		} catch (SQLException e) {
			// TODO make GUI
			System.err.println("SQL Exception inserting values into MetaData.");
			e.printStackTrace();
		}
	}

	 /**
	  * Called each time an element's start tag is encountered, this method
	  * reads the tag and takes appropriate action.
	  */
	 public void startElement(String namespaceURI,
               	String lName, // local name
                String qName, // qualified name
                Attributes attrs)
	 throws SAXException {
	    
		String statement = "";
		String eName = lName; // element name
		if ("".equals(eName)) 
			eName = qName; // namespaceAware = false
		
		//different cases for different elements
		if (eName.equals("metadata"))
			datatype = attrs.getValue(0);
		
		else if (eName.equals("datasetinfo")){
			datasetinfo = true;
			//record DataSetID as the primary key of type INT for the DataSetInfo table
			statement = "INSERT INTO MetaData VALUES ('" + datatype +
				"','[DataSetID]','INT',1," + DynamicTable.DataSetInfo.ordinal()
				+ "," + columnCounter + ")";
			columnCounter++;
			statement += "INSERT INTO MetaData VALUES ('" + datatype +
				"','[DataSet]','VARCHAR(8000)',0," + DynamicTable.DataSetInfo.ordinal()
				+ "," + columnCounter + ")";
		}
		
		else if (eName.equals("atominfodense")){
			atominfodense = true;
			//record AtomID as the primary key of type INT for the AtomInfoDense table
			statement = "INSERT INTO MetaData VALUES ('" + datatype +
				"','[AtomID]','INT',1," + DynamicTable.AtomInfoDense.ordinal()
				+ "," + columnCounter + ")";
			columnCounter++;
		}
		
		else if (eName.equals("atominfosparse")){
			atominfosparse = true;
			//record AtomID as a primary key of type INT for each AtomInfoSparse table
			statement = "INSERT INTO MetaData VALUES ('" + datatype +
				"','[AtomID]','INT',1," + sparseCounter + "," + columnCounter + ")";
			columnCounter++;
			sparseNames.put(sparseCounter, attrs.getValue(0));
		}
		else if (eName.equals("field")){
			fieldType = attrs.getValue("type").toUpperCase();
			if (fieldType.equals("VARCHAR"))
				fieldType = "VARCHAR(8000)";
			if (attrs.getValue("primaryKey").equals("true"))
				primaryKey = 1;
			else primaryKey = 0;
		}
		if (!statement.equals("")){
			System.out.println(statement);
			try {
				stmt.addBatch(statement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.err.println("Error inserting values into MetaData table");
				e.printStackTrace();
			}
		}
	 }
	 
	 /**
	  * This method is called whenever the ending tag of an element is found,
	  * and deals appropriately with such tags.
	  */
	 public void endElement(String namespaceURI,
             String sName, // simple name
             String qName  // qualified name
             )throws SAXException{
		 
		 //at the end of the document, push all the information to the database
		 if (qName.equals("metadata")){
			 try {
					stmt.executeBatch();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }
		 else if (qName.equals("datasetinfo")){
			 datasetinfo = false;
			 columnCounter = 0;
		 }
		 else if (qName.equals("atominfodense")){
			 atominfodense = false;
			 columnCounter = 0;
		 }
		 else if (qName.equals("atominfosparse")){
			 atominfosparse = false;
			 columnCounter = 0;
			 sparseCounter ++;
		 }
		 else if (qName.equals("field")){
			 primaryKey = 0;
			 fieldType = "";
		 }		 
	 }
	 
	 /**
	  * Reads the .md file passed in, stores relevant information in the
	  * MetaData table and creates other appropriate tables.
	  * 
	  * @param file	The .md (xml) file to be read.
	  * @return	The new datatype's name.
	  */
	 public String createTables(String file){
		 		 
		 //read file and put info into MetaData
		 read(file);
		 
		 return createDynamicTables(datatype, false);
		 
	 }
	 
	 public String createDynamicTables(String datatype, boolean fromCompressData) {
		 		 
		 String tableStr;
		 
		 /*
		  * The code for creating these tables is from Anna's original importer.
		  * I only adapted the AtomInfoSparse code to handle multiple AIS tables.
		  * - Leah
		  */
		 //Create DataSetInfo table
		 tableStr = "CREATE TABLE " + datatype + "DataSetInfo (";	
		 try {
			 rs = stmt.executeQuery("SELECT ColumnName, ColumnType, PrimaryKey FROM MetaData " +
					 "WHERE Datatype = '" + datatype + "' AND TableID = " + 
					 DynamicTable.DataSetInfo.ordinal() + "ORDER BY ColumnOrder");
			 String pText = "PRIMARY KEY(";
			 while (rs.next()) {
				 if (rs.getBoolean(3)) {
					 pText += rs.getString(1) + ", ";
				 }
				 tableStr += rs.getString(1) + " " + rs.getString(2) + ", ";
			 }
			 tableStr += pText.substring(0,pText.length()-2) + "))";
			 System.out.println(tableStr);
			 stmt.execute(tableStr);
	
			 //Create AtomInfoDense table
			 tableStr = "CREATE TABLE " + datatype + "AtomInfoDense (";		
			 rs = stmt.executeQuery("SELECT ColumnName, ColumnType, PrimaryKey FROM MetaData " +
					 "WHERE Datatype = '" + datatype + "' AND TableID = " + 
					 DynamicTable.AtomInfoDense.ordinal() + "ORDER BY ColumnOrder");
			 pText = "PRIMARY KEY(";
			 while (rs.next()) {
				 if (rs.getBoolean(3)) {
					 pText += rs.getString(1) + ", ";
				 }
				 tableStr += rs.getString(1) + " " + rs.getString(2) + ", ";
			 }
			 tableStr += pText.substring(0,pText.length()-2) + "))";
			 System.out.println(tableStr);
			 stmt.execute(tableStr);
	
			 // if from CompressData, set variables.
			 if (fromCompressData) {
				 sparseCounter = DynamicTable.AtomInfoSparse.ordinal()+1;
				 sparseNames.clear();
				 sparseNames.put(new Integer(DynamicTable.AtomInfoSparse.ordinal()),"");
			 }
			 
			 //create as many AtomInfoSparse tables as were specified in the .md
			 for (int i=DynamicTable.AtomInfoSparse.ordinal(); i< sparseCounter; i++){
				 tableStr = "CREATE TABLE " + datatype + "AtomInfoSparse" +
				 sparseNames.get((Integer)i) +" (";
				 rs = stmt.executeQuery("SELECT ColumnName, ColumnType, PrimaryKey FROM MetaData " +
						 "WHERE Datatype = '" + datatype + "' AND TableID = " + 
						 i + "ORDER BY ColumnOrder");
				 pText = "PRIMARY KEY (";
				 while (rs.next()) {
					 if (rs.getBoolean(3)) {
						 pText += rs.getString(1) + ", ";
					 }
					 tableStr += rs.getString(1) + " " + rs.getString(2) + ", ";
				 }
				 tableStr += pText.substring(0,pText.length()-2) + "))";
				 System.out.println(tableStr);
				 stmt.execute(tableStr);	
			 }
	
			 rs.close();
	
		 } catch (SQLException e) {
			 System.err.println("SQL Exception creating tables.");
			 // TODO make GUI
			 e.printStackTrace();
		 }
		 
		 return datatype;
				
	 }
	 
	

		
		//tester main
		public static void main(String[] args){
			
			SQLServerDatabase.rebuildDatabase("SpASMSdb");
			SQLServerDatabase db = new SQLServerDatabase();
			db.openConnection();
			Connection connect = db.getCon();
			DynamicTableGenerator echo = new DynamicTableGenerator(connect);
			System.out.println(echo.createTables(args[0]));

			db.closeConnection();

	
		}
	 
}
