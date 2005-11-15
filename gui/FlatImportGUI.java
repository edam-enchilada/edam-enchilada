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
	private Component parent;
	
	public FlatImportGUI(Component parent, SQLServerDatabase db) {
		this.parent = parent;
		fp = new FilePicker("Choose .task file", "task", parent);
		conv = new TSConvert();
		conv.setParent(parent);
		try {
			importer = new EnchiladaDataSetImporter(db);
			importer.setParent(parent);
			doImport();
			
		} catch (Exception e){
			System.out.println(e.toString());
		}
	}
	
	public void doImport() throws Exception {
		File task = new File(fp.getFileName());
		Runnable convRunner = new Runnable() {
			public void run() {
				try {
				conv.convert(fp.getFileName());
				} catch (Exception e) {
					// XXX do something intelligent with this exception
					System.out.println(e.getMessage());
				}
			}
		};
		Thread convThread = new Thread(convRunner);
		convThread.run();
		convThread.join();
		
		System.out.println("I seem to have converted the following files:");
		System.out.println(conv.getOutFiles());

		importer.importFiles(conv.getOutFiles());
	}
}
