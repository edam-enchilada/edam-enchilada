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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import msanalyze.DataSetImporter;

/**
 * @author ritza
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ImportEnchiladaDataDialog extends JDialog implements ActionListener {
	
		private JButton okButton;
		private JButton cancelButton;
		private ParTableModel pTableModel;
		private int dataSetCount;
		private static Window parent = null;
		private boolean exceptions = false;
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
			super(owner, "Import MS-Analyze *.pars as Collections", true);
			parent = owner;
			setSize(1000,600);
			
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			//JTable parTable = getParTable();
			
			//JScrollPane scrollPane = new JScrollPane(parTable);
			
			okButton = new JButton("OK");
			okButton.setMnemonic(KeyEvent.VK_O);
			okButton.addActionListener(this);
			
			cancelButton = new JButton("Cancel");
			cancelButton.setMnemonic(KeyEvent.VK_C);
			cancelButton.addActionListener(this);
			
		//	scrollPane.setPreferredSize(new Dimension(200, 500));
			
			JPanel listPane = new JPanel();
			listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
			JLabel label = new JLabel("Choose Datasets to Convert");
			//label.setLabelFor(parTable);
			listPane.add(label);
			listPane.add(Box.createRigidArea(new Dimension(0,5)));
			//listPane.add(scrollPane);
			listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
			
			buttonPane.add(Box.createHorizontalGlue());
			buttonPane.add(okButton);
			buttonPane.add(Box.createRigidArea(new Dimension(10,0)));
			buttonPane.add(cancelButton);
			
			add(listPane, BorderLayout.CENTER);
			add(buttonPane, BorderLayout.SOUTH);
			
			setVisible(true);	
		}
		/*
		public ImportParsDialog() {
			// dummy constructor for testing purposes.
		}
		
		private JTable getParTable()
		{
			pTableModel = new ParTableModel();
			JTable pTable = new JTable(pTableModel);		
			TableColumn[] tableColumns = new TableColumn[7];
			for (int i = 0; i < 7; i++)
				tableColumns[i] = pTable.getColumnModel().getColumn(i+1);
			
			tableColumns[0].setCellEditor(
					new FileDialogPickerEditor("par","Import",this));
			tableColumns[0].setPreferredWidth(250);
			tableColumns[1].setCellEditor(
					new FileDialogPickerEditor("cal","Mass Cal file",this));
			tableColumns[1].setPreferredWidth(250);
			tableColumns[2].setCellEditor(
					new FileDialogPickerEditor("noz","Size Cal file",this));
			tableColumns[2].setPreferredWidth(250);
		
			for (int i=3;i<7;i++)
				tableColumns[i].setCellEditor(pTable.getDefaultEditor(Double.class));
			
			TableColumn numColumn = pTable.getColumnModel().getColumn(0);
			numColumn.setPreferredWidth(10);
			
			return pTable;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			if (source == okButton) {
					DataSetImporter dsi = 
						new DataSetImporter(
								pTableModel, parent, this);
					// If a .par file or a .cal file is missing, don't start the process.
					if (!dsi.nullRows()) {
						dsi.collectTableInfo();
						if (!exceptions)
							dispose();
					}
			}
			else if (source == cancelButton)
				dispose();
		}
		
		public void displayException(String[] message) {
			exceptions = true;
			new ExceptionDialog(this,message);
		}}
*/
		
		public void actionPerformed(ActionEvent e){}

}
