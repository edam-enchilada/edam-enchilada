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
 * The Original Code is EDAM Enchilada's MainFrame class.
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
 * Created on Jul 16, 2004
 *
 */
package gui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.TreePath;

import analysis.dataCompression.BIRCH;
import analysis.DistanceMetric;

import collection.*;

import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import database.*;
import errorframework.ErrorLogger;
import externalswing.SwingWorker;

/**
 * @author ritza
 * TODO:  In all files, check to make sure that when exceptions are
 * thrown/caught etc, that the application makes it back to a 
 * workable state.
 */
public class MainFrame extends JFrame implements ActionListener
{
	public static final int DESCRIPTION = 3;
	private JToolBar buttonPanel;
	private JSplitPane mainSplitPane;
	
	private JButton importEnchiladaDataButton;
	private JButton importAMSDataButton;
	private JButton importParsButton;
	private JButton importFlatButton;
	private JButton exportParsButton;
	private JButton emptyCollButton;
	private JButton analyzeParticleButton;
	private JButton aggregateButton;
	private JButton mapValuesButton;
	private JMenu analysisMenu;
	private JMenuItem loadEnchiladaDataItem;
	private JMenuItem loadAMSDataItem;
	private JMenuItem loadATOFMSItem;
	private JMenuItem batchLoadATOFMSItem;
	private JMenuItem MSAexportItem;
	private JMenuItem importXmlDatabaseItem;
	private JMenuItem importXlsDatabaseItem;
	private JMenuItem importCsvDatabaseItem;
	private JMenuItem exportXmlDatabaseItem;
	private JMenuItem exportXlsDatabaseItem;
	private JMenuItem exportCsvDatabaseItem;
	private JMenuItem emptyCollection;
	private JMenuItem queryItem;
	private JMenuItem compressItem;
	private JMenuItem clusterItem;
	private JMenuItem detectPlumesItem;
	private JMenuItem rebuildItem;
	private JMenuItem exitItem;
	private JMenuItem cutItem;
	private JMenuItem copyItem;
	private JMenuItem pasteItem;
	private JMenuItem deleteAdoptItem;
	private JMenuItem dataFormatItem;
	private JMenuItem recursiveDeleteItem;
	private CollectionTree collectionPane;
	private CollectionTree synchronizedPane;
	private JTextArea descriptionTA;
	private JComboBox chooseParticleSet;
	
	private CollectionTree selectedCollectionTree = null;
	
	private int copyID = -1;
	private boolean cutBool = false;
	
	private JTable particlesTable = null;
	private Vector<Vector<Object>> data = null;
	
	public static SQLServerDatabase db;
	private JComponent infoPanel;
	
	private JTabbedPane collectionViewPanel;
	private JPanel particlePanel;
	private JScrollPane particleTablePane;
	private JScrollPane collInfoPane;
	private JMenuItem visualizeItem;
	private JMenuItem outputItem;
	private JMenuItem aboutItem;
	
	private OutputWindow outputFrame;
	
	/**
	 * Constructor.  Creates and shows the GUI.	 
	 */
	public MainFrame()
	{
		super("Enchilada");
		
		/* "If you are going to set the look and feel, you should do it as the 
		 * very first step in your application. Otherwise you run the risk of 
		 * initializing the Java look and feel regardless of what look and feel 
		 * you've requested. This can happen inadvertently when a static field 
		 * references a Swing class, which causes the look and feel to be 
		 * loaded. If no look and feel has yet been specified, the default Java 
		 * look and feel is loaded."
		 * From http://java.sun.com/docs/books/tutorial/uiswing/misc/plaf.html
		 */
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setSize(800, 600);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/**
		 * Create and add a menu bar to the frame.
		 */
		setupMenuBar();
		/**
		 * Create and add a button bar using JToolBar.
		 */
		setupButtonBar();
		/**
		 * Create the mane panel consisting of a splitpane between the
		 * collections tree and the browsing tabs.
		 */	
		setupSplitPane();
		
		/**
		 * The Spring Layout is a flexible layout that places every panel in the
		 * frame in relation to that panels surrounding it.
		 */
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		
		Container contentPane = getContentPane();
		layout.putConstraint(SpringLayout.NORTH, buttonPanel, 5, 
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, buttonPanel, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, mainSplitPane, 5,
				SpringLayout.SOUTH, buttonPanel);
		layout.putConstraint(SpringLayout.WEST, mainSplitPane, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5,
				SpringLayout.EAST, buttonPanel);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5,
				SpringLayout.EAST, mainSplitPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 5,
				SpringLayout.SOUTH, mainSplitPane);
		
		//Display the window.
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				db.closeConnection();
				
			}
		});
	}
	
	/**
	 * Provides additional UI-specific builtin functionality to SwingWorker.
	 * The only additional necessary thing to do with this class is explicity call super.finished()
	 * 	when using finished()
	 * @author shaferia
	 */
	private abstract class UIWorker extends SwingWorker {
		protected Component[] disable;
		
		@Override
		public void start() {
			start(new Component[0]);
		}
		
		/**
		 * Starts the UIWorker, disabling the given components.
		 * The components will be enabled again when the UIWorker finishes.
		 * @param disable
		 */
		public void start(Component[] disable) {
			this.disable = disable;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			for (Component c : disable)
				c.setEnabled(false);
			
			super.start();
		}
		
		@Override
		public void finished() {
			super.finished();
			for (Component c : disable)
				c.setEnabled(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		
		if (source == aboutItem) {
			JOptionPane.showMessageDialog(this, "EDAM Enchilada\n" +
					"is supported by NSF ITR Grant IIS-0326328.\n" +
					"For support, please contact dmusican@carleton.edu."
//					+"Carleton Contributors:\n" +
//					"Anna Ritz, Ben Anderson, Leah Steinberg,\n" +
//					"Thomas Smith, Deborah Gross, Jamie Olson,\n" +
//					"Janara Christensen, David Musicant, Jon Sulman\n" +
//					"Madison Contributors:\n"
					);
		}
		
		else if (source == outputItem) {
			if (outputFrame == null) {
				outputFrame = new OutputWindow(this);
				outputFrame.setSize(getSize().width / 2, getSize().height / 2);
				outputFrame.setVisible(true);		
			}
			else
				outputFrame.setVisible(true);
		}
		
		if (source == importEnchiladaDataButton ||
				source == loadEnchiladaDataItem) {
			new ImportEnchiladaDataDialog(this);
			collectionPane.updateTree();
			validate();
		}
		
		if (source == batchLoadATOFMSItem) {
			ATOFMSBatchImportGUI abig = new ATOFMSBatchImportGUI(this);
			if (abig.init()) abig.go();
			collectionPane.updateTree();
			validate();
		}
		
		if (source == importParsButton || source == loadATOFMSItem) 
		{
			new ImportParsDialog(this);
			
			collectionPane.updateTree();
			validate();
		}
		
		if (source == importAMSDataButton || source == loadAMSDataItem) {
				new ImportAMSDataDialog(this);
				collectionPane.updateTree();
				validate();
		}
		
		else if (source == importFlatButton) {
			new FlatImportGUI(this, db);
			
			collectionPane.updateTree();
			validate();
		}
		
		else if (source == emptyCollButton || source == emptyCollection) {
			new EmptyCollectionDialog(this);
			collectionPane.updateTree();
			validate();
		}
		
		else if (source == exportParsButton || source == MSAexportItem)
		{
			getSelectedCollection().exportToPar(this);
		}
		
		else if (source == deleteAdoptItem)
		{
			final Collection[] c = getSelectedCollections();
			if(c == null)
				return;
			
			UIWorker worker = new UIWorker() {
				public Object construct() {
					boolean updateRequired = false;
					for (int i = 0; i < c.length; ++i)
						updateRequired |= db.orphanAndAdopt(c[i]);

					return new Boolean(updateRequired);					
				}
				public void finished() {
					super.finished();
					if (get() != null && (Boolean)get()) {
						selectedCollectionTree.updateTree();
						clearTable();
						validate();
					}
				}
			};
			worker.start(new Component[]{selectedCollectionTree});
			
		}
		
		else if (source == recursiveDeleteItem)
		{
			final Collection[] c = getSelectedCollections();
			final CollectionTree collTree = selectedCollectionTree;
			if(c == null)
				return;
			
			UIWorker worker = new UIWorker() {
				public Object construct() {
					boolean updateRequired = false;
					for (int i = 0; i < c.length; ++i)
						updateRequired |= db.recursiveDelete(c[i]);

					return new Boolean(updateRequired);					
				}
				public void finished() {
					super.finished();
					if (get() != null && (Boolean)get()) {
						selectedCollectionTree.updateTree();
						clearTable();
						validate();
					}
				}
			};
			worker.start(new Component[]{selectedCollectionTree});
		}
		
		else if (source == copyItem)
		{
			cutBool = false;
			copyID = 
				getSelectedCollection().getCollectionID();
		}
		
		else if (source == cutItem)
		{
			cutBool = true;
			copyID = 
				getSelectedCollection().getCollectionID();
		}
		
		else if (source == pasteItem)
		{
			//if no collection selected, paste into root
			//@author steinbel

			if (copyID != 
				getSelectedCollection().getCollectionID() || copyID>-1)
			{
				//check if the datatypes are the same
				if(getSelectedCollection().getCollectionID() == 0
						|| db.getCollection(copyID).getDatatype().equals
											(getSelectedCollection().getDatatype())){
					UIWorker worker = new UIWorker() {
						public Object construct() {
							if (cutBool == false)
							{
								db.copyCollection(db.getCollection(copyID), 
									getSelectedCollection());
							}
							else
							{
								db.moveCollection(db.getCollection(copyID), 
									getSelectedCollection());
							}
							return null;
						}
						public void finished() {
							super.finished();
							collectionPane.updateTree();
							validate();
						}
					};
					worker.start(new Component[]{collectionPane});
				}
				else
					JOptionPane.showMessageDialog(this, "Collections within the same folder must" +
							" be of the same data type");
			} else
				System.err.println("Cannot copy/paste to the same " +
				"destination as the source");
		}
		else if (source == queryItem) {new QueryDialog(this, 
				collectionPane, db, getSelectedCollection());}
		
		else if (source == clusterItem) {new ClusterDialog(this, 
				collectionPane, db);}
		else if (source == visualizeItem) {
			(new chartlib.hist.HistogramsWindow(
				getSelectedCollection().getCollectionID())).setVisible(true);
		}
		else if (source == detectPlumesItem){
			new DetectPlumesDialog(this,synchronizedPane, db);
		}
		
		else if (source == compressItem) {
			//TODO: Provide a way to set the collection's name
			//or give it better default
			BIRCH b = new BIRCH(collectionPane.getSelectedCollection(),db,"name","comment",DistanceMetric.EUCLIDEAN_SQUARED);
			b.compress();
			collectionPane.updateTree();
		}
		
		else if (source == exportXmlDatabaseItem||source==exportXlsDatabaseItem||source==exportCsvDatabaseItem) {
			int temp = -1;
			String fileFilter = "";
			if(source == exportXmlDatabaseItem){
				fileFilter = "*.xml";
				temp = 1;
			}
			else if (source == exportXlsDatabaseItem){
				fileFilter = "*.xls";
				temp = 2;
			}
			else{
				fileFilter = "*.csv";
				temp = 3;
			}
			final int fileType = temp;
			FileDialog fileChooser = new FileDialog(this, 
                    "Create a file to store the database:",
                     FileDialog.LOAD);
			fileChooser.setFile(fileFilter);
			fileChooser.setVisible(true);
			final String filename = fileChooser.getDirectory()+fileChooser.getFile();
			System.out.println("File: "+filename);
			ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(this, "Exporting Database",100);
			progressBar.setIndeterminate(true);
			SwingWorker worker = new SwingWorker(){
				public Object construct(){
					try {
						db.exportDatabase(filename,fileType);
					} catch (FileNotFoundException e1) {
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								JOptionPane.showMessageDialog(null,"The file: "+ filename+" could not be created.");
							}
						});
						
					}
					return null;
				}
			};
			
			progressBar.constructThis();
			
		}
		
		else if (source == importXmlDatabaseItem||source==importXlsDatabaseItem||source==importCsvDatabaseItem) {
			int fileType = -1;
			String fileFilter = "";
			if(source == importXmlDatabaseItem){
				fileFilter = "*.xml";
				fileType = 1;
			}
			else if (source == importXlsDatabaseItem){
				fileFilter = "*.xls";
				fileType = 2;
			}
			else{
				fileFilter = "*.csv";
				fileType = 3;
			}
			FileDialog fileChooser = new FileDialog(this, 
                    "Choose a database file to restore:",
                     FileDialog.LOAD);
			fileChooser.setFile(fileFilter);
			fileChooser.setVisible(true);
			String filename = fileChooser.getDirectory()+fileChooser.getFile();
			System.out.println("File: "+filename);
			if (fileChooser.getFile() != null && JOptionPane.showConfirmDialog(this,
			"Are you sure? " +
			"This will destroy all data in your database and restore it from the file:\n"+filename) ==
				JOptionPane.YES_OPTION) {
	/*			try {
			//		db.importDatabase(filename,fileType);
				
				/*JOptionPane.showMessageDialog(this,
						"The program will now shut down to reset itself. " +
				"Start it up again to continue.");*/
	/*			} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(this,"The file: "+ filename+" could not be found.");
				}
	*/			collectionPane.updateTree();
				
			}			
		}
		
		else if (source == rebuildItem) {
			if (JOptionPane.showConfirmDialog(this,
			"Are you sure? This will destroy all data in your database.") ==
				JOptionPane.YES_OPTION) {
				//db.rebuildDatabase();
				db.closeConnection();
				try{
					SQLServerDatabase.rebuildDatabase("SpASMSdb");
					JOptionPane.showMessageDialog(this,
							"The program will now shut down to reset itself. " +
					"Start it up again to continue.");
					dispose();
				}catch(SQLException s){
					JOptionPane.showMessageDialog(this,
							"Could not rebuild the database." +
							"  Close any other programs that may be accessing the database and try again.");
				}
			}			
		}
		
		else if (source == exitItem) {
			db.closeConnection();
			dispose();
		}
		else if(source == analyzeParticleButton) 
		{
			showAnalyzeParticleWindow();
		}
		else if (source == aggregateButton)
		{

			Collection[] selectedCollections = collectionPane.getSelectedCollections();

			if (selectedCollections != null && selectedCollections.length > 0) {
				AggregateWindow aw = new AggregateWindow(this, db, selectedCollections);
				aw.setVisible(true);
			}
		}
		else if (source == mapValuesButton) 
		{
			Collection selectedCollection = synchronizedPane.getSelectedCollection();
			if (selectedCollection != null) { 
				MapValuesWindow bw = new MapValuesWindow(this, db, selectedCollection);
				bw.setVisible(true);
			}
		}
		else if (source == dataFormatItem) {
			new DataFormatViewer(this);
		}
		
		else if (source == chooseParticleSet) {
			setTable();
		}
		
		ErrorLogger.flushLog(this);
	}
	
	public Collection getSelectedCollection() {
		Collection c = collectionPane.getSelectedCollection();
		if (c != null)
			return c;
		else{
			c = synchronizedPane.getSelectedCollection();
			if (c == null)	//if no collection is selected, return the root
				c = db.getCollection(0);
		}
		return c;
			
	}
	
	private Collection[] getSelectedCollections() {
		Collection[] c = collectionPane.getSelectedCollections();
		if (c != null)
			return c;
		else
			return synchronizedPane.getSelectedCollections();
	}
	
	private void showAnalyzeParticleWindow() {
		int[] selectedRows = particlesTable.getSelectedRows();
		
		for (int row : selectedRows) {
			Collection coll = collectionPane.getSelectedCollection();
			ParticleAnalyzeWindow pw = 
				new ParticleAnalyzeWindow(db, particlesTable, row, coll);
			//set this as the owner
			pw.setOwner(this);
			pw.setVisible(true);
		}
	}
	
	/**
	 * setupMenuBar() sets up  File, Edit, Analysis, Collection, 
	 * and Help menus.  All menu options have keyboard shortcuts 
	 * except for the "Delete Selected and All Children" item
	 * in the Collection menu.
	 * 
	 */
	private void setupMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		
		// Add a file menu to the menu bar
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		emptyCollection = new JMenuItem(
				"New empty collection", 
				KeyEvent.VK_N);
		emptyCollection.addActionListener(this);
		
		JMenu importCollectionMenu = new JMenu("Import Collection. . . ");
		loadATOFMSItem = new JMenuItem("from ATOFMS data. . .");
		loadATOFMSItem.addActionListener(this);
		loadEnchiladaDataItem = new JMenuItem("from Enchilada data. . .");
		loadEnchiladaDataItem.addActionListener(this);
		loadAMSDataItem = new JMenuItem("from AMS data. . .");
		loadAMSDataItem.addActionListener(this); 
		batchLoadATOFMSItem = new JMenuItem("from ATOFMS data (with bulk file). . .");
		batchLoadATOFMSItem.addActionListener(this);
		importCollectionMenu.setMnemonic(KeyEvent.VK_I);
		importCollectionMenu.add(loadATOFMSItem);
		importCollectionMenu.add(batchLoadATOFMSItem);
		importCollectionMenu.add(loadEnchiladaDataItem);
		importCollectionMenu.add(loadAMSDataItem);
		
		JMenu exportCollectionMenu = new JMenu("Export Collection. . .");
		MSAexportItem = new JMenuItem("to MS-Analyze. . .");
		MSAexportItem.addActionListener(this);
		exportCollectionMenu.setMnemonic(KeyEvent.VK_E);
		exportCollectionMenu.add(MSAexportItem);
		
		JMenu importDatabaseMenu = new JMenu("Restore Database. . . ");
		importXmlDatabaseItem = new JMenuItem("from XML. . .");
		importXmlDatabaseItem.addActionListener(this);
		importXlsDatabaseItem = new JMenuItem("from Xls. . .");
		importXlsDatabaseItem.addActionListener(this);
		importCsvDatabaseItem = new JMenuItem("from Csv. . .");
		importCsvDatabaseItem.addActionListener(this);
		importDatabaseMenu.add(importXmlDatabaseItem);
		importDatabaseMenu.add(importXlsDatabaseItem);
		importDatabaseMenu.add(importCsvDatabaseItem);
		
		JMenu exportDatabaseMenu = new JMenu("Export Database. . . ");
		exportXmlDatabaseItem = new JMenuItem("to XML. . .");
		exportXmlDatabaseItem.addActionListener(this);
		exportXlsDatabaseItem = new JMenuItem("to Xls. . .");
		exportXlsDatabaseItem.addActionListener(this);
		exportCsvDatabaseItem = new JMenuItem("to Csv. . .");
		exportCsvDatabaseItem.addActionListener(this);
		exportDatabaseMenu.add(exportXmlDatabaseItem);
		exportDatabaseMenu.add(exportXlsDatabaseItem);
		exportDatabaseMenu.add(exportCsvDatabaseItem);
		
		
		rebuildItem = new JMenuItem("Rebuild Database", KeyEvent.VK_R);
		rebuildItem.addActionListener(this);
		
		exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitItem.addActionListener(this);
		
		fileMenu.add(emptyCollection);
		fileMenu.addSeparator();
		fileMenu.add(importCollectionMenu);
		fileMenu.add(exportCollectionMenu);
		fileMenu.addSeparator();
		fileMenu.add(importDatabaseMenu);
		fileMenu.add(exportDatabaseMenu);
		fileMenu.add(rebuildItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		// Add an edit menu to the menu bar.
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		cutItem = new JMenuItem("Cut",KeyEvent.VK_T);
		cutItem.addActionListener(this);
		copyItem = new JMenuItem("Copy",KeyEvent.VK_C);
		copyItem.addActionListener(this);
		pasteItem = new JMenuItem("Paste",KeyEvent.VK_P);
		pasteItem.addActionListener(this);
		JMenuItem selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
		
		menuBar.add(editMenu);
		editMenu.add(cutItem);
		editMenu.add(copyItem);
		editMenu.add(pasteItem);
		editMenu.addSeparator();
		editMenu.add(selectAllItem);
		
		// Add an analysis menu to the menu bar.
		analysisMenu = new JMenu("Analysis");
		analysisMenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(analysisMenu);
		
		clusterItem = new JMenuItem("Cluster. . .", KeyEvent.VK_C);
		clusterItem.addActionListener(this);
		JMenuItem labelItem = new JMenuItem("Label. . .", 
				KeyEvent.VK_L);
		JMenuItem classifyItem = new JMenuItem("Classify. . . ", 
				KeyEvent.VK_F);
		queryItem = new JMenuItem("Query. . . ", KeyEvent.VK_Q);
		queryItem.addActionListener(this);
		compressItem = new JMenuItem("Compress. . . ", KeyEvent.VK_P);
		compressItem.addActionListener(this);
		visualizeItem = new JMenuItem("Visualize. . .", KeyEvent.VK_V);
		visualizeItem.addActionListener(this);
		detectPlumesItem = new JMenuItem("Detect Plumes. . .", KeyEvent.VK_W);
		detectPlumesItem.addActionListener(this);
		
		analysisMenu.add(clusterItem);
		analysisMenu.add(labelItem);
		analysisMenu.add(classifyItem);
		analysisMenu.add(queryItem);
		analysisMenu.add(compressItem);
		analysisMenu.add(visualizeItem);
		//analysisMenu.add(detectPlumesItem);
		
		//Add a collection menu to the menu bar.
		JMenu collectionMenu = new JMenu("Collection");
		collectionMenu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(collectionMenu);
		
		deleteAdoptItem = 
			new JMenuItem("Delete Selected and Adopt Children", 
					KeyEvent.VK_D);
		deleteAdoptItem.addActionListener(this);
		recursiveDeleteItem = 
			new JMenuItem("Delete Selected and All Children");
		recursiveDeleteItem.addActionListener(this);
		
		collectionMenu.add(deleteAdoptItem);
		collectionMenu.add(recursiveDeleteItem);
		
		// add a datatype menu to the menu bar.
		JMenu datatypeMenu = new JMenu("Datatype");
		datatypeMenu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(datatypeMenu);
		dataFormatItem = new JMenuItem("Data Format Viewer", KeyEvent.VK_D);
		dataFormatItem.addActionListener(this);
		
		datatypeMenu.add(dataFormatItem);
		
		//Add a help menu to the menu bar.
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);
		//helpMenu.add(helpItem);
		outputItem = new JMenuItem("Show Output Window", KeyEvent.VK_S);
		outputItem.addActionListener(this);
		helpMenu.add(outputItem);
		helpMenu.addSeparator();
		aboutItem = new JMenuItem("About Enchilada", 
						KeyEvent.VK_A);
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);
		
		setJMenuBar(menuBar);  //Add menu bar to the frame
	}
	
	/**
	 * Creates a button bar - use right now for importing, exporting,
	 * and creating empty collections.
	 *
	 */
	private void setupButtonBar()
	{
		buttonPanel = new JToolBar();
		buttonPanel.setBorder(new EtchedBorder());
		
		importParsButton = new JButton("Import from MS-Analyze");
		importParsButton.setBorder(new EtchedBorder());
		importParsButton.addActionListener(this);
		
		importEnchiladaDataButton = new JButton("Import Enchilada Data Sets");
		importEnchiladaDataButton.setBorder(new EtchedBorder());
		importEnchiladaDataButton.addActionListener(this);
		
		importAMSDataButton = new JButton("Import AMS Data Sets");
		importAMSDataButton.setBorder(new EtchedBorder());
		importAMSDataButton.addActionListener(this);
		
		importFlatButton = new JButton("Import CSV");
		importFlatButton.setBorder(new EtchedBorder());
		importFlatButton.addActionListener(this);
		
		emptyCollButton = new JButton("New Empty Collection");
		emptyCollButton.setBorder(new EtchedBorder());
		emptyCollButton.addActionListener(this);
		
		exportParsButton = new JButton("Export to MS-Analyze");
		exportParsButton.setBorder(new EtchedBorder());
		exportParsButton.addActionListener(this);
		
		buttonPanel.add(emptyCollButton);
		buttonPanel.add(importParsButton);
		buttonPanel.add(importFlatButton);
		buttonPanel.add(importEnchiladaDataButton);
		buttonPanel.add(importAMSDataButton);
		buttonPanel.add(exportParsButton);
		add(buttonPanel);
	}
	
	/**
	 * setupSplitPane() creates and adds a split pane to the frame. The 
	 * left side of the split pane contains a collection and synchronization
	 * trees the right side of the split pane contains a tabbed pane.  
	 * Everything except the spectrum viewer is scrollable.
	 */  
	private void setupSplitPane()
	{
		// Add a JTabbedPane to the split pane.
		collectionViewPanel = new JTabbedPane();
		
		Vector<String> columns = new Vector<String>(1);
		columns.add("");
		
		data = new Vector<Vector<Object>>(1000);
		Vector<Object> row = new Vector<Object>(1);
		row.add("");
		data.add(row);
		
		String[] blank = {"Click on a collection to see information."};
		
		chooseParticleSet = new JComboBox(blank);
		chooseParticleSet.addActionListener(this);
		
		particlesTable = new JTable(data, columns);
		
		analyzeParticleButton = new JButton("Analyze Particle");
		analyzeParticleButton.setEnabled(false);
		analyzeParticleButton.addActionListener(this);
		
		JPanel comboPane = new JPanel(new FlowLayout());
		comboPane.add(chooseParticleSet, BorderLayout.CENTER);
		particlePanel = new JPanel(new BorderLayout());
		particleTablePane = new JScrollPane(particlesTable);
		JPanel partOpsPane = new JPanel(new FlowLayout());
		partOpsPane.add(analyzeParticleButton, BorderLayout.CENTER);
		
		particlePanel.add(comboPane, BorderLayout.NORTH);
		particlePanel.add(particleTablePane, BorderLayout.CENTER);
		particlePanel.add(partOpsPane, BorderLayout.SOUTH);
		
		collectionViewPanel.addTab("Particle List", null, particlePanel,
				null);
		
		//rightPane.addTab("Spectrum Viewer Text", null, panel2, null);
		descriptionTA = new JTextArea("Description here");
		infoPanel = makeTextPanel(descriptionTA);
		collInfoPane = new JScrollPane(infoPanel);
		collectionViewPanel.addTab("Collection Information", 
				null, collInfoPane, null);
		// Create and add the split panes.
		
		// Add a JTree to the split pane.
		JPanel topLeftPanel = new JPanel(new BorderLayout());
		JPanel topLeftButtonPanel = new JPanel(new FlowLayout());
		JPanel bottomLeftPanel = new JPanel(new BorderLayout());
		JPanel bottomLeftButtonPanel = new JPanel(new FlowLayout());
		collectionPane = new CollectionTree(db, this, false);
		synchronizedPane = new CollectionTree(db, this, true);
		topLeftButtonPanel.add(aggregateButton = new JButton("Aggregate Selected"));
		bottomLeftButtonPanel.add(mapValuesButton = new JButton("Map Values"));
		
		//TODO: Remove when Map Values becomes usable
		mapValuesButton.setVisible(false);
		
		topLeftPanel.add(collectionPane, BorderLayout.CENTER);
		topLeftPanel.add(topLeftButtonPanel, BorderLayout.SOUTH);
		bottomLeftPanel.add(synchronizedPane, BorderLayout.CENTER);
		bottomLeftPanel.add(bottomLeftButtonPanel, BorderLayout.SOUTH);
		
		JSplitPane leftPane 
		= new JSplitPane(JSplitPane.VERTICAL_SPLIT, topLeftPanel, bottomLeftPanel);
		leftPane.setMinimumSize(new Dimension(170,64));
		leftPane.setDividerLocation(200);
		
		mainSplitPane = 
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, collectionViewPanel);
		add(mainSplitPane);
		
		aggregateButton.addActionListener(this);
		mapValuesButton.addActionListener(this);
		aggregateButton.setEnabled(false);
		mapValuesButton.setEnabled(false);
	}
	
	public void collectionSelected(CollectionTree colTree, Collection collection) {
		if (selectedCollectionTree != null && colTree != selectedCollectionTree)
			selectedCollectionTree.clearSelection();
		
		selectedCollectionTree = colTree;
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		int dividerLocation = mainSplitPane.getDividerLocation();
		boolean panelChanged = false;
		
		if (colTree == collectionPane) {
			panelChanged = setupRightWindowForCollection(collection);
			aggregateButton.setEnabled(collection.containsData());
			analysisMenu.remove(detectPlumesItem);
			mapValuesButton.setEnabled(false);
		} else if (colTree == synchronizedPane) {
			panelChanged = setupRightWindowForSynchronization(collection);
			mapValuesButton.setEnabled(collection.containsData());
			analysisMenu.add(detectPlumesItem);
			aggregateButton.setEnabled(false);
		}
		
		if (panelChanged) {
			// Bah. Java can't just remember this... need to remind it.
			mainSplitPane.setDividerLocation(dividerLocation);
			
			validate();
		}
		
		editText(MainFrame.DESCRIPTION, collection.getDescription());
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**
	 * Display collection information
	 * @param collection
	 * @return
	 */
	private boolean setupRightWindowForCollection(Collection collection) {
		mainSplitPane.setBottomComponent(collectionViewPanel);
		
		String dataType = collection.getDatatype();
		ArrayList<String> colnames = db.getColNames(dataType, DynamicTable.AtomInfoDense);
		
		// use combo box to display particles in chunks of 1000 particles.
		int numParticles = db.getCollectionSize(collection.getCollectionID());
		int numLines = numParticles/1000;
		if (numParticles%1000 != 0)
			numLines++;
		int low = 1; 
		int high = 1000;
		int counter = 0;
		chooseParticleSet.removeAllItems();
		while (high < numParticles) {
			chooseParticleSet.addItem("Particle Index: " + low + " - " + high);
			counter++;
			low += 1000;
			high += 1000;
		}
		chooseParticleSet.addItem("Particle Index: " + low + " - " + numParticles);
		
		
		Vector<Object> columns = new Vector<Object>(colnames.size());
		for (int i = 0; i < colnames.size(); i++) {
			String temp = colnames.get(i);
			temp = temp.substring(1,temp.length()-1);
			columns.add(temp);
		}
		
		data = new Vector<Vector<Object>>(1000);
		Vector<Object> row = new Vector<Object>(colnames.size());
		for (int i = 0; i < colnames.size(); i++) 
			row.add("");
		
		data.add(row);
		
		particlesTable = new JTable(data, columns);
		particlesTable.setDefaultEditor(Object.class, null);
		
		if (dataType.equals("ATOFMS")) {
			particleTablePane.setViewportView(particlesTable);
			
			particlesTable.setEnabled(true);
			ListSelectionModel lModel = 
				particlesTable.getSelectionModel();
			
			lModel.setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			lModel.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent arg0) {		
					/*	// If collection isn't ATOFMS, don't display anything.
					 if (!db.getAtomDatatype(atomID).equals("ATOFMS"))
					 return;
					 */
					int row = particlesTable.getSelectedRow();
					
					analyzeParticleButton.setEnabled(row != -1);
				}
			});		
			
			particlesTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1)
						showAnalyzeParticleWindow();
				}
			});
			
			collectionViewPanel.setComponentAt(0, particlePanel);
			collectionViewPanel.repaint();
		} else {
			particleTablePane.setViewportView(particlesTable);
			particlesTable.setEnabled(false);
			// TODO Change to just show table (no button)...
			collectionViewPanel.setComponentAt(0, particlePanel);
		}
		
		//call setTable, which populates the table.
		setTable();
		//old code
		//data.clear();
		//data = db.updateParticleTable(collection, data, low, high);

		return true;
	}
	
	
	/**
	 * Display time-series information
	 * @param collection
	 * @return
	 */
	private boolean setupRightWindowForSynchronization(Collection collection) {
		Component rightWindow = mainSplitPane.getBottomComponent();
		
		if (rightWindow instanceof SyncAnalyzePanel) {
			SyncAnalyzePanel sap = (SyncAnalyzePanel) rightWindow;
			
			if (sap.containsCollection(collection)) { 
				sap.selectCollection(collection);
				return false;
			}
		}
		
		mainSplitPane.setBottomComponent(new SyncAnalyzePanel(this, db, synchronizedPane, collection));
		return true;
	}
	
	/**
	 * The makeTextpanel method makes a text panel that can be added to any 
	 * other panel - used for the template.  Once other methods are implemented 
	 * this will be removed.
	 * 
	 * @param text
	 * @return a JComponent object that contains the desired tex
	 * 
	 */
	protected static JComponent makeTextPanel(JTextArea filler) {
		JPanel panel = new JPanel(false);
		filler.setEditable(false);
		filler.getDocument().getLength();
		//filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
	}
	
	public void editText(int panelID, String text)
	{
		if (panelID == DESCRIPTION)
		{
			descriptionTA.setText(text);
			descriptionTA.setCaretPosition(0);
			
			JScrollBar vert = collInfoPane.getVerticalScrollBar();
			int page = vert.getVisibleAmount();
			vert.setUnitIncrement(page / 15);
			vert.setBlockIncrement(page);
			
			/*
			 int docLength = descriptionTA.getDocument().getLength();
			 
			 descriptionTA.replaceRange(text, 0,docLength);*/
		}
	}
	
	public void updateSynchronizedTree(int collectionID) {
		synchronizedPane.updateTree(collectionID);
	}
	
	public void updateAnalyzePanel(Collection c) {
		Component rightWindow = mainSplitPane.getBottomComponent();
		
		if (rightWindow instanceof SyncAnalyzePanel) {
			SyncAnalyzePanel sap = (SyncAnalyzePanel) rightWindow;
			sap.updateModels(c);
		}
	}
	
	public static void main(String[] args) {
		
		// Verify that database exists, and give user opportunity to create
		// if it does not.
		if (!SQLServerDatabase.isPresent("SpASMSdb")) {
			if (JOptionPane.showConfirmDialog(null,
					"No database found. Would you like to create one?\n" +
					"Make sure to select yes only if there is no database already present,\n"
					+ "since this will remove any pre-existing Enchilada database.") ==
						JOptionPane.YES_OPTION) {
				
				try{
					SQLServerDatabase.rebuildDatabase("SpASMSdb");
				}catch(SQLException s){
					JOptionPane.showMessageDialog(null,
							"Could not rebuild the database." +
							"  Close any other programs that may be accessing the database and try again.");
				}
			} else {
				return; // no database?  we shouldn't do anything at all.
			}
		}
		
		//Open database connection:
		db = new SQLServerDatabase("SpASMSdb");
		db.openConnection();
		

		VersionChecker vc = new VersionChecker(db);
		try {
			if (! vc.isDatabaseCurrent()) {
				System.out.println("If we cared yet about database versions, this one would not be current!");
			}
		} catch (Exception e) {
			System.out.println("SQL Exception retrieving version!");
			e.printStackTrace();
		}
		
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new MainFrameRun(args));
	}
	
	/**
	 * Offers simple functionality for parsing command-line arguments while maintaining
	 * earlier deferred construction of MainFrame
	 * @author shaferia
	 */
	private static class MainFrameRun implements Runnable {
		String[] args;
		private MainFrame mf;
		
		/**
		 * Creates MainFrameRun with command-line arguments
		 * @param args
		 * 	-redirectOutput: take output from standard output and redirect to a window.
		 *		If started with this option, will continue to redirect to window even when it is closed.
		 */
		public MainFrameRun(String[] args) {
			this.args = args;
		}
	
		public void run() {
			mf = new MainFrame();
			
			//Check command-line arguments
			for (String s : args) {
				if (s.startsWith("-"))
					s = s.substring(1);
				else
					continue;
				
				try {
					java.lang.reflect.Method m = getClass().getMethod(s, (Class[]) null);
					m.invoke(this, new Object[0]);
				}
				catch (Exception ex) {
					System.out.print("Invalid argument: " + s + " - ");
					System.out.println(ex.getMessage());
				}
			}
		}

		/**
		 * Redirect standard output to a separate JFrame
		 */
		public void redirectOutput() {
			try {
				//This option will be invoked by those not running from inside an IDE -
				//	output will continue to be redirected to a window throughout the session.
				OutputWindow.setReturnOutputOnClose(true);
				
				OutputWindow w = mf.outputFrame = new OutputWindow(mf);
				w.setSize(mf.getSize().width / 2, mf.getSize().height / 2);
			
				//the window will flash in front briefly, but reversing the order of these calls
				//	doesn't properly send the window to the back.
				w.setVisible(true);
				w.toBack();
			}
			catch (Exception ex) {
				System.out.println("Couldn't reassign program output to window");
			}
		}
	}
		
	/**
	 * @return Returns the data.
	 */
	public Vector<Vector<Object>> getData() 
	{
		return data;
	}
	
	/**
	 * @return Returns the particlesTable.
	 */
	public JTable getParticlesTable() {
		return particlesTable;
	}
	
	public JComponent getInfoPanel() {
		return infoPanel;
	}
	
	public void clearOtherTreeSelections(CollectionTree colTree) {
		if (colTree == collectionPane)
			synchronizedPane.clearSelection();
		else if (colTree == synchronizedPane)
			collectionPane.clearSelection();
	}
		
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	/**
	 * Updates with peaklist information for the selected 
	 * atom
	 */
	public void valueChanged(ListSelectionEvent arg0) {
		int row = particlesTable.getSelectedRow();
		
		analyzeParticleButton.setEnabled(row != -1);
	}	
	
	/** This clears the particle table, both at initialization and
	 * at when a collection is deleted.  
	 *
	 */
	public void clearTable() {
		data = new Vector<Vector<Object>>(1000);
		Vector<Object> row = new Vector<Object>(1);
		row.add("");
		data.add(row);
	
		particlesTable.tableChanged(new TableModelEvent(particlesTable.getModel()));
		particlesTable.doLayout();
		
		chooseParticleSet.removeAllItems();
		chooseParticleSet.addItem("Click on a collection to see information.");
		
		analyzeParticleButton.setEnabled(false);
		
		particlesTable.validate();
		analyzeParticleButton.validate();
	}
	
	/**
	 * This sets the particle table according to the particle
	 * set seleced in the combo box above the table.
	 * 
	 * It is called by the setupRightWindow method and by the actionlistener
	 * when the combo box is chosen.
	 *
	 */
	public void setTable() {
		String text = (String)chooseParticleSet.getSelectedItem();
		if (text != null && !text.equals("Click on a collection to see information.")) {
			// extract the two integers.
			Scanner scan = new Scanner(text);
			scan.next();
			scan.next();
			int low = scan.nextInt();
			scan.next();
			int high = scan.nextInt();
			
			//clear data in table and repopulate it with appropriate 
			// data.
			data.clear();
			db.updateParticleTable(getSelectedCollection(),data,low,high);
			particlesTable.tableChanged(new TableModelEvent(particlesTable.getModel()));
			particlesTable.doLayout();
		}
	}
	
	/**
	 * Updates the collection tree to show the requested collection.
	 * @param cID	The collection to highlight.
	 */
	public void updateCurrentCollection(int cID){
		
		collectionPane.switchCollections(cID);
	}
}