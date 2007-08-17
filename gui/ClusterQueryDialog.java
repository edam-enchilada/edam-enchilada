package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import dataImporters.AMSDataSetImporter;
import database.InfoWarehouse;
import errorframework.*;
import externalswing.SwingWorker;


/**
 * @author turetske
 *
 * ClusterQueryDialog opens a dialogue window that allows the user to 
 * cluster a selected collection using cluster centers that the user imputs.
 * There is an expanding list that the user can inputs a file into as well as
 * an input box for a distance parameter.
 *
 */


public class ClusterQueryDialog extends JDialog implements ActionListener{
	
	private JButton okButton;
	private JButton cancelButton;
	private ClusterTableModel clusterTableModel;
	private int dataSetCount;
	private static JFrame parent = null;
	private InfoWarehouse db;
	
	/**
	 * Extends JDialog to form a modal dialogue box for setting
	 * cluster centers.  
	 * @param owner The parent frame of this dialog box, should be the 
	 * main frame.    
	 * @throws java.awt.HeadlessException From the constructor of 
	 * JDialog.  
	 */
	public ClusterQueryDialog(JFrame owner, InfoWarehouse db) throws HeadlessException {
		super(owner, "Cluster with Chosen Centers", true);
		parent = owner;
		this.db = db;
		setSize(500,400);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JTable clusterTable = getClusterTable();
		JScrollPane scrollPane = new JScrollPane(clusterTable);
		
		okButton = new JButton("OK");
		okButton.setMnemonic(KeyEvent.VK_O);
		okButton.addActionListener(this);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this);
		
		scrollPane.setPreferredSize(new Dimension(400,300));
		
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel("Choose Cluster Centers");
		label.setLabelFor(clusterTable);
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(scrollPane);
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
	
	private JTable getClusterTable()
	{
		clusterTableModel = new ClusterTableModel();
		JTable pTable = new JTable(clusterTableModel);		
		TableColumn[] tableColumns = new TableColumn[1];
		for (int i = 0; i < 1; i++)
			tableColumns[i] = pTable.getColumnModel().getColumn(i+1);
		tableColumns[0].setCellEditor(
				new FileDialogPickerEditor("txt","Center",this));
		tableColumns[0].setPreferredWidth(395);
		
		TableColumn numColumn = pTable.getColumnModel().getColumn(0);
		numColumn.setPreferredWidth(5);
		return pTable;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		/*if (source == okButton) {
			//set necessary values in the data importer
			//TODO Change the progress bar
			final ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(parent, AMSDataSetImporter.TITLE, 100);
			//TODO Find out the paraments to clusterQuery
			final clusterQuery cluster = 
					new clusterQuery(clusterTableModel, parent, db, progressBar);
			
			//Create the progress bar and spin off a thread to do the work in
			//Database transactions are not currently used.
			progressBar.constructThis();
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					try {
						clusterQuery.collectTableInfo();
					}
					catch (DisplayException e1) {
						ErrorLogger.displayException(progressBar,e1.toString());
					}
					catch (WriteException e2) {
						ErrorLogger.displayException(progressBar,e2.toString());
					}
					return null;
				}
				public void finished() {
					progressBar.disposeThis();
					dispose();
				}
			};
			worker.start();
		}
		else */if (source == cancelButton) {
			dispose();
		}
			
	}


}
