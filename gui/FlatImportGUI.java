package gui;

// monday, 11:30--12:45 tues 10:30-11 11:30 
import java.awt.*;
import java.io.File;
import javax.swing.*;

import dataImporters.*;
import database.SQLServerDatabase;

import externalswing.*;

public class FlatImportGUI {
	private FilePicker fp;
	private TSConvert conv;
	private EnchiladaDataSetImporter importer;
	private Frame parent;
	
	public FlatImportGUI(Frame parent, SQLServerDatabase db) {
		this.parent = parent;
		fp = new FilePicker("Choose .task file", "task", parent);
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

	
	
	public void doImport() throws Exception {
		File task = new File(fp.getFileName());
		conv.convert(fp.getFileName());
		
		System.out.println("I seem to have converted the following files:");
		System.out.println(conv.getOutFiles());

		importer.importFilesThreaded(conv.getOutFiles());
		
	}
}
