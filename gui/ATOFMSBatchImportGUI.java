package gui;

import java.awt.*;
import javax.swing.*;
import java.io.*;

import dataImporters.ATOFMSBatchTableModel;
import dataImporters.ATOFMSDataSetImporter;

public class ATOFMSBatchImportGUI {
	private Window parent;
	private ATOFMSBatchTableModel tab;
	
	public ATOFMSBatchImportGUI(Window parent) {
		super();
		this.parent = parent;
	}
	
	
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
			
			return true;
		} catch (IOException ex) {
			new ExceptionDialog(ex.toString());
		}
		return false;
	}
	
	public void go() {
		try {
			ATOFMSDataSetImporter dsi = new ATOFMSDataSetImporter(tab, parent);
			dsi.checkNullRows();
			dsi.collectTableInfo();
		} catch (Exception e) {
			new ExceptionDialog(e.toString());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
