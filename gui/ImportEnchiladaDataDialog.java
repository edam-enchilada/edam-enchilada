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
 * The Original Code is EDAM Enchilada's ImportEnchiladaDataDialog class.
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
 * Jonathan Sulman sulmanj@carleton.edu
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
 * Created on May 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gui;

import dataImporters.EnchiladaDataSetImporter;
import errorframework.*;
import database.DynamicTableGenerator;
import database.SQLServerDatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableColumn;


/**
 * @author ritza
 * @author steinbel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ImportEnchiladaDataDialog extends JDialog implements ActionListener {
	
		private JButton okButton;
		private JButton cancelButton;
		private JButton dataTypeButton;
		private JRadioButton parentButton;
		private JLabel parentLabel;
		private EnchiladaDataTableModel eTableModel;
		private int dataSetCount;
		private static Window parent = null;
		private boolean exceptions = false;
		private SQLServerDatabase db = MainFrame.db;
		private JTextArea typelist;
		private boolean importedTogether = false;
		private int parentID = -1;
		
		/**
		 * Extends JDialog to form a modal dialogue box for importing 
		 * par files.  
		 * @param owner The parent frame of this dialog box, should be the 
		 * main frame.  This frame will become inactive while ImportPars is
		 * active.  
		 * @throws java.awt.HeadlessException From the constructor of 
		 * JDialog.  
		 */
		public ImportEnchiladaDataDialog(Frame owner) throws HeadlessException {
			// calls the constructor of the superclass (JDialog), sets the title and makes the
			// dialog modal.  
			super(owner, "Import Enchilada Data Sets as Collections", true);
			parent = owner;
			setSize(500,600);
			
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			

			JTable edTable = getEnchiladaDataTable();
			
			JScrollPane scrollPane = new JScrollPane(edTable);
			
			parentButton = new JRadioButton();
			parentButton.setMnemonic(KeyEvent.VK_P);
			parentButton.addActionListener(this);
			
			okButton = new JButton("OK");
			okButton.setMnemonic(KeyEvent.VK_O);
			okButton.addActionListener(this);
			
			cancelButton = new JButton("Cancel");
			cancelButton.setMnemonic(KeyEvent.VK_C);
			cancelButton.addActionListener(this);
			
			scrollPane.setPreferredSize(new Dimension(300, 200));
			
			JPanel listPane = new JPanel();
			listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
			JLabel label = new JLabel("<html>Choose Enchilada Data files to import.");
			label.setLabelFor(edTable);
			
			listPane.add(label);
			listPane.add(Box.createRigidArea(new Dimension(0,5)));
			listPane.add(scrollPane);
			listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			listPane.add(Box.createRigidArea(new Dimension(0, 25)));
			
			//create a new textarea (typelist) for a list of the datatypes
			typelist = new JTextArea(50, 70);
			
			//populate it with the names of the known datatypes
			ArrayList<String> typeNames = new ArrayList<String>();			
			typeNames = db.getKnownDatatypes();
			
			for (String type : typeNames)
				typelist.append(type + "\n");
			
			typelist.setEditable(false);
			
			//create a label for the typelist
			JLabel listLabel = new JLabel("Known datatypes");
			
			JLabel newTypeLabel = new JLabel();
			newTypeLabel.setText("<html>To import data"
					+ " of an unlisted type, import a metadata file for the" 
					+ " new datatype.</html>");
			newTypeLabel.setMaximumSize(new Dimension(150, 60));
			
			//button to pop up the FilePickerEditor
			dataTypeButton = new JButton("Choose .md file");
			dataTypeButton.setMnemonic(KeyEvent.VK_H);
			dataTypeButton.addActionListener(this);
			
			//contain all the datatype information in one place
			JPanel metaDataPane = new JPanel();
			metaDataPane.setLayout(new BoxLayout(metaDataPane, BoxLayout.X_AXIS));
			
			JPanel typePane = new JPanel();
			typePane.setLayout(new BoxLayout(typePane, BoxLayout.Y_AXIS));
			typePane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			typePane.add(listLabel);
			typePane.add(Box.createRigidArea(new Dimension(0,5)));
			typePane.add(typelist);
			
			JPanel choosePane = new JPanel();
			choosePane.setLayout(new BoxLayout(choosePane, BoxLayout.Y_AXIS));
			choosePane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			choosePane.add(newTypeLabel);
			choosePane.add(Box.createRigidArea(new Dimension(0,5)));
			choosePane.add(dataTypeButton);
			
			metaDataPane.add(typePane);
			metaDataPane.add(Box.createRigidArea(new Dimension(30, 0)));
			metaDataPane.add(choosePane);
			
			listPane.add(metaDataPane);
			
			
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
			
			//import into a parent collection goes here
			parentLabel = new JLabel("Create a parent collection for all"
					+ " incoming datasets.");
			buttonPane.add(parentButton);
			buttonPane.add(parentLabel);
			
			buttonPane.add(Box.createHorizontalGlue());
			buttonPane.add(okButton);
			buttonPane.add(Box.createRigidArea(new Dimension(10,0)));
			buttonPane.add(cancelButton);
			
			add(listPane, BorderLayout.CENTER);
			add(buttonPane, BorderLayout.SOUTH);
			
			setVisible(true);	
		}
		
	
		private JTable getEnchiladaDataTable(){
			
			eTableModel = new EnchiladaDataTableModel();
			JTable eTable = new JTable(eTableModel);
			
			TableColumn numColumn = eTable.getColumnModel().getColumn(0);
			numColumn.setPreferredWidth(10);
			TableColumn list = eTable.getColumnModel().getColumn(1);
			list.setCellEditor(new FilePickerEditor("ed", "Import", this));
			list.setPreferredWidth(250);
			
			return eTable;
			
		}
		
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			int cID = -1;
			if (source == okButton) {
				EnchiladaDataSetImporter importer;
				try {
					importer = new EnchiladaDataSetImporter(db);
					ArrayList<String> files = importer.collectTableInfo(eTableModel);
					cID = importer.importFiles(files);
				} catch (WriteException e1) {
					ErrorLogger.writeExceptionToLog("EnchiladaImporting",e1.getMessage());
				} catch (DisplayException e1) {
					ErrorLogger.displayException(this,e1.getMessage());
				}
				db.updateAncestors(db.getCollection(cID));
				dispose();
			}
			else if (source == cancelButton)
				dispose();
			
			else if (source == dataTypeButton){
						
				FilePicker fp = new FilePicker("Import", "md", this);
				String fileName = fp.getFileName();
				
				if (fileName != null){
					File file;
					Scanner scan;
					String typeName = "";
					
					try {
						file = new File(fileName);
						scan = new Scanner(file);
						//find the datatype information.  eeww.
						boolean found = false;
						while (!found){
							String next = scan.next();
							if (next.contains("datatype")){
								int marker = next.indexOf("=");
								//increment & decrement to get around quotes
								next = next.substring(marker+2, next.length()-2);
								System.out.println(next);
								typeName = next;
								found = true;
							}
						}
						
						
						if (!db.containsDatatype(typeName)){						
							Connection con = db.getCon();
							DynamicTableGenerator newType =
								new DynamicTableGenerator(con);
							
							typeName = newType.createTables(fileName);
							//TODO: check for format, SQL errors in creation for GUI?
							typelist.append(typeName + "\n");
						}
						
						//if the scanner couldn't find anything, the file's not
						//in the right format
					} catch (NoSuchElementException e1){
						ErrorLogger.writeExceptionToLog("EnchiladaImporting","Please check .md file " + fileName + 
								" for correct format.");
						
					} catch (FileNotFoundException e1) {
						ErrorLogger.writeExceptionToLog("EnchiladaImporting","Problems creating new datatype from " + fileName);
						//System.err.println("Problems creating new datatype.");
						//e1.printStackTrace();
					}					
				}
			}
			
			else if (source == parentButton){
				//pop up a "create new collections" dialog box & keep number of new
				//collection
				EmptyCollectionDialog ecd = 
					new EmptyCollectionDialog((JFrame)parent, "ATOFMS", true);
				parentID = ecd.getCollectionID();
				
				if (parentID == -1) {
					parentButton.setSelected(false);
				} else {
					parentLabel.setText("Importing into collection # " + parentID);
					importedTogether = true;
				}
			}
		}
}
