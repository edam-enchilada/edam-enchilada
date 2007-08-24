package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import analysis.DistanceMetric;
import analysis.clustering.Cluster;
import analysis.clustering.KMeans;
import analysis.clustering.ClusterQuery;

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
	private JTextField distance, commentField;
	private ClusterTableModel clusterTableModel;
	private int dataSetCount;
	private static JFrame parent = null;
	private InfoWarehouse db;
	private CollectionTree cTree;

	
	/**
	 * Extends JDialog to form a modal dialogue box for setting
	 * cluster centers.  
	 * @param owner The parent frame of this dialog box, should be the 
	 * main frame.    
	 * @throws java.awt.HeadlessException From the constructor of 
	 * JDialog.  
	 */
	public ClusterQueryDialog(JFrame owner, CollectionTree cTree, InfoWarehouse db) throws HeadlessException {
		super(owner, "Cluster with Chosen Centers", true);
		parent = owner;
		this.db = db;
		this.cTree = cTree;
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
		
		
		JLabel label = new JLabel("Choose Cluster Centers");
		label.setLabelFor(clusterTable);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(scrollPane);
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		JPanel comment = new JPanel();
		JLabel commentLabel = new JLabel("Comment: ");
		commentField = new JTextField(30);
		comment.add(commentLabel);
		comment.add(commentField);
		
		JPanel panel = new JPanel(new BorderLayout());
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		panel.add(label, BorderLayout.NORTH);
		panel.add(listPane, BorderLayout.CENTER);
		panel.add(comment, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		
		JLabel label2 = new JLabel("Maximum distance:");
		distance = new JTextField("0.5", 5);
		distance.setHorizontalAlignment(JTextField.RIGHT);
		buttonPane.add(label2);
		buttonPane.add(distance);
		buttonPane.add(Box.createRigidArea(new Dimension(210,0)));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(okButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10,0)));
		buttonPane.add(cancelButton);
		
		add(panel, BorderLayout.CENTER);
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
		if (source == okButton) {
			System.out.println("okay button");
			float d = (float) 0.0;
			try{
				d = Float.valueOf(distance.getText());
				System.out.println(d);
				if(d>2.0||d<0.0){
					System.out.println("out of range");
					JOptionPane.showMessageDialog(this, "Please enter a number between 0.0 and 2.0 for the distance");
					
				}
				else{
					ArrayList<String> filenames = new ArrayList<String>();
					for(int i = 0; i< clusterTableModel.getRowCount(); i++){
						if(!clusterTableModel.getValueAt(i,1).equals("")){
							filenames.add((String) clusterTableModel.getValueAt(i,1));
						}
					}
					if(filenames.size()>0){
						ClusterQuery qc = new ClusterQuery(
								cTree.getSelectedCollection().
								getCollectionID(),db, 
								"Cluster Query", commentField.getText(), false, filenames,d);
						qc.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
						//TODO:  When should we use disk based and memory based 
						// cursors?
						/*if (db.getCollectionSize(
								cTree.getSelectedCollection().
								getCollectionID()) < 10000) // Was 10000
						{
							System.out.println("setting cursor type");
							qc.setCursorType(Cluster.STORE_ON_FIRST_PASS);
						}
						else*/
					//	{
							System.out.println("setting cursor type");
							qc.setCursorType(Cluster.DISK_BASED);
						//}
	
						qc.divide();
						
						dispose();
					}
					else
						JOptionPane.showMessageDialog(this, "Please select a file to cluster on");
				}
			}
			catch(Exception e1){
				System.out.println("not a number");
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "Please enter a number between 0.0 and 2.0 for the distance");
			}
			
			//set necessary values in the data importer
			//TODO Change the progress bar
		/*	final ProgressBarWrapper progressBar = 
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
			worker.start();*/
			
		}
		else if (source == cancelButton) {
			dispose();
		}
			
	}


}
