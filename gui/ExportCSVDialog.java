/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's EmptyCollectionDialog class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
 * Tom Bigwood tom.bigwood@nevelex.com
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */


/*
 * Created on March 8, 2009
 */
package gui;

import javax.swing.*;
import javax.swing.table.TableModel;

import collection.Collection;

import dataExporters.CSVDataSetExporter;
import dataExporters.MSAnalyzeDataSetExporter;
import database.Database;
import database.InfoWarehouse;
import errorframework.DisplayException;
import errorframework.ErrorLogger;
import externalswing.SwingWorker;

import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author jtbigwoo
 */
public class ExportCSVDialog extends JDialog implements ActionListener 
{
	public static String EXPORT_FILE_EXTENSION = "csv";
	
	private JButton okButton;
	private JButton cancelButton;
	private JTextField maxMZField;
	private JTextField csvFileField;
	private JButton csvDotDotDot;
	private InfoWarehouse db;
	private JFrame parent = null;
	private Collection collection = null;
	private ArrayList<Integer> atomIds = null;
	
	/**
	 * Called when you want to export a particular particle or whole collection of particles
	 * @param parent
	 * @param db
	 * @param c
	 */
	public ExportCSVDialog(JFrame parent, JTable dt, InfoWarehouse db, Collection c) {
		super (parent,"Export to CSV file", true);
		this.db = db;
		this.parent = parent;
		this.collection = c;
		this.atomIds = getSelectedAtomIds(dt);
		dt.getModel();
		setSize(450,150);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JLabel csvFileLabel = new JLabel("." + EXPORT_FILE_EXTENSION + " File: ");
		csvFileField = new JTextField(25);
		csvDotDotDot = new JButton("...");
		csvDotDotDot.addActionListener(this);
		
		JLabel maxMZLabel = new JLabel("Higest m/z value to export: ");
		maxMZField = new JTextField(25);
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		JPanel mainPanel = new JPanel();
		SpringLayout layout = new SpringLayout();
	    mainPanel.setLayout(layout);	
		
	    mainPanel.add(csvFileLabel);
	    mainPanel.add(csvFileField);
	    mainPanel.add(csvDotDotDot);
	    mainPanel.add(maxMZLabel);
	    mainPanel.add(maxMZField);
	    mainPanel.add(buttonPanel);
	    
		layout.putConstraint(SpringLayout.WEST, csvFileLabel,
                10, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, csvFileLabel,
                15, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, csvFileField,
                170, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, csvFileField,
                10, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, csvDotDotDot,
                375, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, csvDotDotDot,
                10, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.WEST, maxMZLabel,
                10, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, maxMZLabel,
                15, SpringLayout.SOUTH, csvFileField);
		layout.putConstraint(SpringLayout.WEST, maxMZField,
                170, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, maxMZField,
                10, SpringLayout.SOUTH, csvFileField);
		layout.putConstraint(SpringLayout.WEST, buttonPanel,
                160, SpringLayout.WEST, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, buttonPanel,
                10, SpringLayout.SOUTH, maxMZLabel);
		
		add(mainPanel);
		
		setVisible(true);	
	}
	
	public void actionPerformed(ActionEvent e) {
		int maxMZValue;
		Object source = e.getSource();
		if (source == csvDotDotDot) {
			csvFileField.setText((new FileDialogPicker("Choose ." + EXPORT_FILE_EXTENSION + " file destination",
					 EXPORT_FILE_EXTENSION, this)).getFileName());
		}
		else if (source == okButton) {
			try {
				maxMZValue = Integer.parseInt(maxMZField.getText());
			}
			catch (NumberFormatException nfe) {
				maxMZValue = -1;
			}
			if(!csvFileField.getText().equals("") && !csvFileField.getText().equals("*.csv")) {
				if (maxMZValue > 0) {
					final Database dbRef = (Database)db;
					
					final ProgressBarWrapper progressBar = 
						new ProgressBarWrapper(parent, MSAnalyzeDataSetExporter.TITLE, 100);
					final CSVDataSetExporter cse = 
							new CSVDataSetExporter(
									this, dbRef,progressBar);
					
					progressBar.constructThis();
					final String csvFileName = csvFileField.getText().equals("") ? null : csvFileField.getText();
					final int mzValue = maxMZValue;
					
					final SwingWorker worker = new SwingWorker(){
						public Object construct() {
							if (atomIds != null && atomIds.size() > 0)
							{
								cse.exportToCSV(atomIds, csvFileName, mzValue);
							}
							else
							{
								// send the whole collection if we haven't selected a particular particle
								cse.exportToCSV(collection, csvFileName, mzValue);
							}
							return null;
						}
						public void finished() {
							progressBar.disposeThis();
							ErrorLogger.flushLog(parent);
							parent.validate();
						}
					};
					worker.start();
					dispose();
				}
				else
					JOptionPane.showMessageDialog(this, "Highest m/z value to export must be a number greater than zero.");
			}
			//If they didn't enter a name, force them to enter one
			else
				JOptionPane.showMessageDialog(this, "Please enter an export file name.");
		}			
		else  
			dispose();
	}
	private ArrayList<Integer> getSelectedAtomIds(JTable particleTable)
	{
		ArrayList<Integer> returnList = null;
		TableModel particleModel;
		Object value;
		if (particleTable.getSelectedRows() != null && particleTable.getSelectedRows().length != 0) {
			particleModel = particleTable.getModel();
			returnList = new ArrayList<Integer>(particleTable.getSelectedRows().length);
			for (int rowIndex : particleTable.getSelectedRows()) {
				returnList.add((Integer) particleTable.getValueAt(rowIndex, 0));
			}
		}
		return returnList;
	}

}
