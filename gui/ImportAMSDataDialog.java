package gui;

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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import dataImporters.AMSDataSetImporter;
import dataImporters.ATOFMSDataSetImporter;
import errorframework.*;

public class ImportAMSDataDialog extends JDialog implements ActionListener{

	private JButton okButton;
	private JButton cancelButton;
	private JRadioButton parentButton;
	private JLabel parentLabel;
	private AMSTableModel amsTableModel;
	private JProgressBar progressBar;
	private int dataSetCount;
	private static Window parent = null;
	private boolean importedTogether = false;
	private int parentID = 0; //default parent collection is root
	
	/**
	 * Extends JDialog to form a modal dialogue box for importing 
	 * par files.  
	 * @param owner The parent frame of this dialog box, should be the 
	 * main frame.  This frame will become inactive while ImportPars is
	 * active.  
	 * @throws java.awt.HeadlessException From the constructor of 
	 * JDialog.  
	 */
	public ImportAMSDataDialog(Frame owner) throws HeadlessException {
		// calls the constructor of the superclass (JDialog), sets the title and makes the
		// dialog modal.  
		super(owner, "Import AMS Datasets as Collections", true);
		parent = owner;
		setSize(1000,600);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JTable parTable = getParTable();
		JScrollPane scrollPane = new JScrollPane(parTable);
		
		okButton = new JButton("OK");
		okButton.setMnemonic(KeyEvent.VK_O);
		okButton.addActionListener(this);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this);
		
		//an option to import all the datasets into one parent collection
		parentButton = new JRadioButton(
				"Create a parent collection for all incoming datasets.",
				false);
		parentButton.setMnemonic(KeyEvent.VK_P);
		parentButton.addActionListener(this);
		
		parentLabel = new JLabel();
		
		scrollPane.setPreferredSize(new Dimension(795,500));
		
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Choose Datasets to Convert");
		label.setLabelFor(parTable);
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(scrollPane);
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		
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
	
	private JTable getParTable()
	{
		amsTableModel = new AMSTableModel();
		JTable pTable = new JTable(amsTableModel);		
		TableColumn[] tableColumns = new TableColumn[3];
		for (int i = 0; i < 3; i++)
			tableColumns[i] = pTable.getColumnModel().getColumn(i+1);
		tableColumns[0].setCellEditor(
				new FilePickerEditor("txt","Dataset",this));
		tableColumns[0].setPreferredWidth(250);
		tableColumns[1].setCellEditor(
				new FilePickerEditor("txt","Time Series File",this));
		tableColumns[1].setPreferredWidth(250);
		tableColumns[2].setCellEditor(
				new FilePickerEditor("txt","Mass to Charge File",this));
		tableColumns[2].setPreferredWidth(250);
	
		TableColumn numColumn = pTable.getColumnModel().getColumn(0);
		numColumn.setPreferredWidth(10);
		
		return pTable;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source == okButton) {
				AMSDataSetImporter ams = 
					new AMSDataSetImporter(
							amsTableModel, parent, this);
				// If a .par file or a .cal file is missing, don't start the process.
					try {
							ams.errorCheck();
							ams.collectTableInfo();
						} catch (DisplayException e1) {
							ErrorLogger.displayException(this,e1.toString());
						} catch (WriteException e2) {
							ErrorLogger.writeExceptionToLog("Importing",e2.toString());
						}
						dispose();
		}
		else if (source == parentButton){
			//pop up a "create new collections" dialog box & keep number of new
			//collection
			EmptyCollectionDialog ecd = 
				new EmptyCollectionDialog((JFrame)parent, "AMS", false);
			parentID = ecd.getCollectionID();
			
			if (parentID == -1) {
				parentButton.setSelected(false);
			} else {
				parentLabel.setText("Importing into collection # " + parentID);
				importedTogether = true;
			}
		}
		else if (source == cancelButton)
			dispose();
	}
	
	/**
	 * Method to determine whether the datasets are being imported together into
	 * a single parent collection.
	 * 
	 * @return True if datasets are being imported together.
	 */
	public boolean parentExists(){
		return importedTogether;
	}
	
	/**
	 * Accessor method to obtain the parent collection's ID for the datasets being
	 * imported.
	 * 
	 * @return	parentID
	 */
	public int getParentID(){
		return parentID;
	}
}
