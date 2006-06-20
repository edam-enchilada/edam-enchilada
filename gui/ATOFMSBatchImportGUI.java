package gui;

import java.awt.*;
import javax.swing.*;

import java.io.*;

import dataImporters.ATOFMSBatchTableModel;
import dataImporters.ATOFMSDataSetImporter;
import errorframework.ErrorLogger;

/**
 * A GUI for importing several ATOFMS collections at once, using a CSV file that
 * looks just like the Import from MS-Analyze GUI.
 * An example of such a file is in the importation files directory of the source
 * tree.
 * 
 * @author smitht
 *
 */

public class ATOFMSBatchImportGUI {
	private Window parent;
	private ATOFMSBatchTableModel tab;
	private int parentID;
	
	public ATOFMSBatchImportGUI(Window parent) {
		super();
		this.parent = parent;
	}
	
	/**
	 * init() - ask the user various questions about importing, and prepare to import.
	 * 
	 * if (import.init()) import.go();
	 * 
	 * @return true if a dataset can now be imported, false otherwise.
	 */
	public boolean init() {
		try {
			FilePicker fpick = new FilePicker("Choose a dataset list to import",
					"csv", parent);
			if (fpick.getFileName() == null) {
				// they chose to cancel.
				return false;
			}
			
			// load the csv file
			tab = new ATOFMSBatchTableModel(
					new File(fpick.getFileName()));
			
			// ask whether to use autocal
			int selection = JOptionPane.showConfirmDialog(parent,
					"Use the autocalibrator on these data sets?",
					"Autocalibrate peaks?",
					JOptionPane.YES_NO_OPTION);
			if (selection == JOptionPane.YES_OPTION)
				tab.setAutocal(true);
			else
				tab.setAutocal(false);
			
			selection = JOptionPane.showConfirmDialog(parent,
					"Import all datasets into one parent collection?",
					"Import into parent?",
					JOptionPane.YES_NO_OPTION);
			if (selection == JOptionPane.YES_OPTION) {
				EmptyCollectionDialog ecd = 
					new EmptyCollectionDialog((JFrame)parent, "ATOFMS", false);
				parentID = ecd.getCollectionID();
				if (parentID == -1) {
					return false;
				}
			} else {
				parentID = 0;
			}
			
			
			return true;
		} catch (IOException ex) {
			ErrorLogger.writeExceptionToLog("ATOFMSBatchImport",ex.toString());
		}
		return false;
	}
	
	/**
	 * Actually try to import the dataset.  Doesn't tell the caller whether
	 * it worked or not, but it does tell the user.  This seems to be the usual
	 * thing to do, don't know if it's right.  Whee!
	 * 
	 * Can take a while...
	 */
	public void go() {
		try {
			ATOFMSDataSetImporter dsi = new ATOFMSDataSetImporter(tab, parent);
			dsi.setParentID(parentID);
			dsi.checkNullRows();
			dsi.collectTableInfo();
		} catch (Exception e) {
			ErrorLogger.writeExceptionToLog("ATOFMSBatchImport",e.toString());
		}
	}
}
