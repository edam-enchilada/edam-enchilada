package gui;

// monday, 11:30--12:45 tues 10:30-11 11:30 
import java.awt.*;
import java.io.File;
import javax.swing.*;

import dataImporters.*;
import database.SQLServerDatabase;

public class FlatImportGUI {
	private FilePicker fp;
	private TSConvert conv;
	private EnchiladaDataSetImporter importer;
	
	public FlatImportGUI(Component parent, SQLServerDatabase db) {
		fp = new FilePicker("Choose .task file", "task", parent);
		conv = new TSConvert();
		try {
			importer = new EnchiladaDataSetImporter(db);
			doImport();
			
		} catch (Exception e){
			System.out.println(e.toString());
		}
	}
	
	public void doImport() throws Exception {
		File task = new File(fp.getFileName());
		conv.convert(fp.getFileName());
		System.out.println("I seem to have converted the following files:");
		System.out.println(conv.getOutFiles());
		importer.importFiles(conv.getOutFiles());
	}
}
