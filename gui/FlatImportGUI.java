package gui;

import java.awt.*;
import java.util.concurrent.*;
import java.io.*;

import javax.swing.*;

import dataImporters.*;
import database.SQLServerDatabase;

import externalswing.*;



/*
 * Actually, a LinkedBlockingQueue is perfect.
 * 
 * OK, so:  to do.
 * in TSConvert, add support within the ProgressTask to stick things on the
 * queue instead of just adding them to the array.
 * 
 * here, create the fileQueue and pass it to each thing
 * 
 * think about how closing one dialog will influence the other.  bleah.
 * maybe override dispose in the implementations
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
			new ExceptionDialog(e);
		}
	}

	
	
	private void doImport() throws Exception {
		importer.read(fp.getFileName());
	}
}
