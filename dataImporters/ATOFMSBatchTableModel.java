package dataImporters;

/**
 * A table model made to work the same as a ParTableModel, for the purpose of importing many ATOFMS datasets.
 */

import errorframework.ErrorLogger;
import externalswing.SwingWorker;
import gui.FilePicker;
import gui.ProgressBarWrapper;

import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import au.com.bytecode.opencsv.CSVReader;

public class ATOFMSBatchTableModel extends AbstractTableModel implements ParTable {
	private int realRowCount;
	private boolean autocal;
	private List<String[]> importData;
	
	public ATOFMSBatchTableModel(File batchFile) throws IOException {
		super();
		readFile(batchFile);
	}

	/*
	 * Reads all the data from the file into memory, and checks it for a couple
	 * of common problems.
	 */
	private void readFile(File batchFile) throws IOException {
		CSVReader r = new CSVReader(new FileReader(batchFile));
		importData = r.readAll();
		System.out.println("Got a file of " + importData.size() + "x" + importData.get(0).length);
		r.close();
		
		ListIterator<String[]> i = importData.listIterator();
		
		while (i.hasNext()) {
			if (i.next().length != 6) {
				if (!i.hasNext() && i.previous().length == 0) {
					// an empty last line is not worth worrying about.
					i.remove();
					break;
				}	
				throw new IOException(
						"Incorrect CSV format---wrong number of columns " +
						"(line " + i.previousIndex() + ")");
			}
		}
		
		/*
		 *  this is so it's just like the real ParTableModel, which has an
		 *  extra line at the end, filled with nothing interesting.
		 */
		importData.add(new String[] {"", "", "", "", "", ""});
	}
	
	public int getRowCount() {
		return importData.size(); // ParTableModel has an extra row, where data hasn't been entered yet.
	}

	public int getColumnCount() {
		return 8;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= importData.size() - 1) {
			return "";
		}
		
		switch (columnIndex) {
		case 0:
			return rowIndex + 1;
		case 4:
		case 5:
			return Integer.parseInt(importData.get(rowIndex)[columnIndex-1]);
		case 6:
			return Float.parseFloat(importData.get(rowIndex)[columnIndex-1]);		
		case 7:
			return autocal;
		default:
			return (importData.get(rowIndex))[columnIndex-1];
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(400,400);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		FilePicker pick = new FilePicker("Foo", "csv", frame);
		ATOFMSBatchTableModel a;
		try {
			a = new ATOFMSBatchTableModel(
					new File(pick.getFileName()));
		
			JTable tab = new JTable(a);
			frame.add(tab);
			frame.pack();

			// this explodes if anything is wrong, but it's just a test, so oh well.
			final ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(frame, "Importing ATOFMS Datasets", 100);
			progressBar.constructThis();
			final JFrame frameRef = frame;
			final ATOFMSBatchTableModel aRef = a;
			final SwingWorker worker = new SwingWorker(){
				public Object construct(){
					try {
						ATOFMSDataSetImporter dsi = new ATOFMSDataSetImporter(aRef, frameRef, progressBar);
						dsi.collectTableInfo();
					} catch (Exception e) {
						ErrorLogger.writeExceptionToLog("ATOFMSBatchImport",e.toString());
					}
					return null;
				}
			};
			worker.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setAutocal(boolean autocal) {
		this.autocal = autocal;
	}
}
