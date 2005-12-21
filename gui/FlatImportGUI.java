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
	private TSConvert conv;
	private EnchiladaDataSetImporter importer;
	private Frame parent;
	
	public FlatImportGUI(Frame parent, SQLServerDatabase db) {
		this.parent = parent;
		fp = new FilePicker("Choose .task file", "task", parent);
		if (fp.getFileName() == null) {
			return;
			// should this throw an exception instead?  i think this is ok...
		}
		conv = new TSConvert();
		conv.setParent(parent);
		
		importer = new EnchiladaDataSetImporter(db);
		importer.setParent(parent);
		
		try {
			doImport();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception importing (generally)");
			// TODO: more handling of this exception.
		}
	}

	
	
	private void doImport() throws Exception {		
		PipedInputStream pinput = new PipedInputStream();
		final PipedOutputStream poutput = new PipedOutputStream(pinput);
	
		// uncomment these, and comment out the pipe versions, if you need
		// to see what's getting passed between the converter and importer.
//		FileOutputStream poutput = new FileOutputStream(new File("temp.xml"));
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					conv.convert(fp.getFileName(), poutput);
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: exception handling.  as usual.
				}
			}
		});

		
//		InputStream pinput = new FileInputStream(new File("temp.xml"));
		importer.importStreamThreaded(new BufferedInputStream(pinput));
	}
}
