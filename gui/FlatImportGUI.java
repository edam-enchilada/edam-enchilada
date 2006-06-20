package gui;

import java.awt.*;

import dataImporters.*;
import database.SQLServerDatabase;

import errorframework.*;

/**
 * GUI for importing CSV files, using a "task" file.  An example of such a file
 * is in the Importation Files directory of the source tree.
 * 
 * @author smitht
 */

public class FlatImportGUI {
	private FilePicker fp;
	private TSImport importer;
	private Frame parent;
	
	public FlatImportGUI(Frame parent, SQLServerDatabase db) {
		this.parent = parent;
		fp = new FilePicker("Choose .task file", "task", parent);
		if (fp.getFileName() == null) {
			return;
			// should this throw an exception instead?  i think this is ok...
		}
		importer = new TSImport(db, parent);
		
		try {
			doImport();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception importing (generally)");
			ErrorLogger.writeExceptionToLog("FlatImport",e.toString());
		}
	}

	
	
	private void doImport() throws Exception {
		importer.read(fp.getFileName());
	}
}
