package dataImporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author steinbel
 *
 * FileMaker is a class to create temporary .dtd files for importing .md or .ed
 * files.  It could be adapted to other temporary file purposes (such as rebuilding
 * the database).
 */
public class FileMaker {

	private File directory;
	private File newFile;
	private static final char quote = '"';
	
	/*
	 * take in the name of the path to the location of the .ed or .md file and use that
	 * for making the path to the .dtd file.  have a method to create each of the
	 * files, and one to delete whichever file was just made (pass in the name of
	 * the file to delete).  That way, if different file types are needed later,
	 * just add a method to fill them.  Yes, it might make some duplicate code,
	 * but because each file's contents are hard-coded, there probably wouldn't be
	 * too much overlap, anyways.  Maybe, call the methods "fillMeta", etc.
	 */
	
	/**
	 * Constructor creates the file in a directory called "temp" created in the
	 * pathname given as the location.
	 * 
	 * @param String location 	the pathname of the target directory
	 * @param String fileName	the name of the file to be created, no extension
	 */
	public FileMaker(String location, String fileName){
		
		directory = new File(location + "temp");
		boolean success = directory.mkdir();
		System.out.println(success);
		directory.deleteOnExit();//although we are going to get rid of it
								//manually, this is a safeguard
		
		if (success){
			try {
				newFile = new File(directory, fileName + ".dtd");
				newFile.createNewFile();
				newFile.deleteOnExit();//again, safeguard
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Problem creating temporary .dtd file");
				e.printStackTrace();
			}
		}
		else
			System.err.println("Could not create temporary directory.");
		
	}
	
	/**
	 * Checks whether the file was created properly and can be filled.
	 * 
	 * @return	True if file exists, false if it's null.
	 */
	public boolean fileCreated(){
		if (newFile != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Fills the file with the necessary info to make the "meta.dtd" file.
	 *
	 * @return	True on success.
	 */
	public boolean setMetaContents(){
		
		/*	contents of meta.dtd file:
		 * 
		 * <?xml version="1.0" encoding="utf-8"?>
		 * <!ELEMENT metadata (datasetinfo, atominfodense, atominfosparse+)>
		 * <!-- The metadata element MUST have a datatype associated with it -->
		 * <!ATTLIST metadata datatype CDATA #REQUIRED>
		 * <!ELEMENT datasetinfo (field*)>
		 * <!ELEMENT atominfodense (field*)>
		 * <!ELEMENT atominfosparse (field*)>
		 * <!-- In case more than one AIS table is required, each must have a name/number. -->
		 * <!ATTLIST atominfosparse table CDATA #REQUIRED>
		 * <!ELEMENT field (#PCDATA)>
		 * <!-- Attributes are the type of data in that field, and whether or not it is a primary key - used for the AIS table(s).  -->
		 * <!ATTLIST field 
		 * type CDATA #REQUIRED
		 * primaryKey (true | false) "false"
		 * >
		 */
		String contents = "<?xml version=" + quote + "1.0" + quote+ " " +
				"encoding=" + quote + "utf-8" + quote + "?>\n"
				+ "<!ELEMENT metadata (datasetinfo, atominfodense, atominfosparse+)>\n"
				+ "<!-- The metadata element MUST have a datatype associated with it -->\n"
				+ "<!ATTLIST metadata datatype CDATA #REQUIRED>\n"
				+ "<!ELEMENT datasetinfo (field*)>\n"
				+ "<!ELEMENT atominfodense (field*)>\n"
				+ "<!ELEMENT atominfosparse (field*)>\n"
				+ "<!-- In case more than one AIS tabe is required, each must"
				+ " have a name/number. -->\n"
				+ "<!ATTLIST atominfosparse table CDATA #REQUIRED>\n"
				+ "<!ELEMENT field (#PCDATA)>\n"
				+ "<!-- Attribuets are the type of data in that field, and whether"
				+ " or not it is a primary key - used for the AIS table(s).  -->\n"
				+ "<!ATTLIST field \n"
				+ "type CDATA #REQUIRED\n"
				+ "primaryKey (true | false) " + quote + "false" + quote + "\n"
				+ ">";
		
		return write(contents);
		
	}
	
	/**
	 * Fills the file with the necessary contents to make the "enchilada.dtd" file.
	 * 
	 * @return	True on success.
	 */
	public boolean setEnchiladaContents(){
		
		/* Contents of enchilada.dtd file:
		 * 
		 * <?xml version="1.0" encoding="utf-8"?>
		 * <!ELEMENT enchiladadata (datasetinfo+)>
		 * <!-- The enchiladadata element MUST have a datatype associated with it. -->
		 * <!ATTLIST enchiladadata datatype CDATA #REQUIRED>
		 * <!ELEMENT datasetinfo (field*, atominfodense*)>
		 * <!-- Each dataset MUST have a name (this is used as a primary key). -->
		 * <!ATTLIST datasetinfo dataSetName CDATA #REQUIRED>
		 * <!ELEMENT atominfodense (field*, atominfosparse*)>
		 * <!ELEMENT atominfosparse (field*)>
		 * <!-- The AIS elements MUST specify which AIS table they belong to.  -->
		 * <!ATTLIST atominfosparse table CDATA #REQUIRED>
		 * <!ELEMENT field (#PCDATA)>
		 * 
		 */
		
		String contents = "<?xml version=" + quote + "1.0" + quote
				+ " encoding =" + quote + "utf-8" + quote + "?>\n"
				+ "<!ELEMENT enchiladadata (datasetinfo+)>\n"
				+ "<!-- The enchiladadata element MUST have a datatype associated with it. -->\n"
				+ "<!ATTLIST enchiladadata datatype CDATA #REQUIRED>\n"
				+ "<!ELEMENT datasetinfo (field*, atominfodense*)>\n"
				+ "<!-- Each dataset MUST have a name (this is used as a primary key). -->\n"
				+ "<!ATTLIST datasetinfo dataSetName CDATA #REQUIRED>\n"
				+ "<!ELEMENT atominfodense (field*, atominfosparse*)>\n"
				+ "<!ELEMENT atominfosparse (field*)>\n"
				+ "<!-- The AIS elements MUST specify which AIS table they belong to.  -->\n"
				+ "<!ATTLIST atominfosparse table CDATA #REQUIRED>\n"
				+ "<!ELEMENT field (#PCDATA)>";
				
		
		return write(contents);
		
	}
	
	/**
	 * Helper method to the set*Contents() methods, write(string) does the actual
	 * writing to the new file.
	 * 
	 * @param filler - the string of the file's contents
	 * @return	True on success.
	 */
	private boolean write(String filler) {
		
		boolean succeeded;
		
		try {
			FileWriter fw =  new FileWriter(newFile, true);
			// System.out.println(filler); //debugging
			fw.write(filler);
			fw.close();
			succeeded = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			succeeded = false;
		}
		
		return succeeded;
		
	}
	
	/**
	 * Deletes the temporary file created and then the temporary directory.
	 * (Technically, they are both deleteOnExit, but if they aren't removed
	 * in the correct order, the directory may not get wiped.)
	 * 
	 * @return	True on successful deletion.
	 */
	public boolean deleteTemps(){
		
		boolean deleted = false;
		if (newFile.delete()){
			if (directory.delete())
				deleted = true;
		}
		return deleted;
	}

	//tester main
	public static void main(String[] args){
		
		File test = new File("test.txt");
		
		try {
			test.createNewFile();
			FileWriter writer = new FileWriter(test, true);
			writer.write("this bloody thing ought to work.");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String absPath = test.getAbsolutePath();
		String shortName = test.getName();
		String path = absPath.substring(0, 
				(absPath.length() - shortName.length()) );
		System.out.println(path);
		FileMaker maker = new FileMaker(path, "enchilada");
		System.out.println(maker.setEnchiladaContents());
		System.out.println(maker.deleteTemps());
		test.delete();
		
	}
	
}
