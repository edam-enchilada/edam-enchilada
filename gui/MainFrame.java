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

import collection.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

import database.*;

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
	private JButton importParsButton;
	private JButton exportParsButton;
	private JButton emptyCollButton;
	private JButton analyzeParticleButton;
	private JButton aggregateButton;
	private JButton mapValuesButton;
	private JMenuItem loadEnchiladaDataItem;
	private JMenuItem loadATOFMSItem;
	private JMenuItem MSAexportItem;
	private JMenuItem emptyCollection;
	private JMenuItem queryItem;
	private JMenuItem clusterItem;
	private JMenuItem rebuildItem;
	private JMenuItem exitItem;
	private JMenuItem cutItem;
	private JMenuItem copyItem;
	private JMenuItem pasteItem;
	private JMenuItem deleteAdoptItem;
	private JMenuItem recursiveDeleteItem;
	private CollectionTree collectionPane;
	private CollectionTree synchronizedPane;
	private JTextArea descriptionTA;

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
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (source == importEnchiladaDataButton ||
				source == loadEnchiladaDataItem) {
			new ImportEnchiladaDataDialog(this);
			collectionPane.updateTree();
			validate();
		}
		
		if (source == importParsButton || source == loadATOFMSItem) 
		{
			new ImportParsDialog(this);
			
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
			Collection c = getSelectedCollection();
	        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			db.orphanAndAdopt(c);
	        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	        selectedCollectionTree.updateTree(c.getCollectionID());
			validate();
		}
		
		else if (source == recursiveDeleteItem)
		{
			Collection c = getSelectedCollection();
	        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			db.recursiveDelete(getSelectedCollection());
	        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	        selectedCollectionTree.updateTree(c.getCollectionID());
			validate();
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
			if (copyID != 
				getSelectedCollection().getCollectionID())
			{
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
				collectionPane.updateTree();
				validate();
			}
			else
				System.err.println("Cannot copy/paste to the same " +
						"destination as the source");
		}
		else if (source == queryItem) {new QueryDialog(this, 
											collectionPane, db, getSelectedCollection());}
		
		else if (source == clusterItem) {new ClusterDialog(this, 
				collectionPane, db);}
		
		
		else if (source == rebuildItem) {
			if (JOptionPane.showConfirmDialog(this,
					"Are you sure? This will destroy all data in your database.") ==
				JOptionPane.YES_OPTION) {
				db.closeConnection();
				SQLServerDatabase.rebuildDatabase("SpASMSdb");
				JOptionPane.showMessageDialog(this,
						"The program will now shut down to reset itself. " +
						"Start it up again to continue.");
				dispose();
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
	}
	
	private Collection getSelectedCollection() {
		Collection c = collectionPane.getSelectedCollection();
		if (c != null)
			return c;
		else
			return synchronizedPane.getSelectedCollection();
	}
	
	private void showAnalyzeParticleWindow() {
		int[] selectedRows = particlesTable.getSelectedRows();
		
		for (int row : selectedRows) {
			ParticleAnalyzeWindow pw = new ParticleAnalyzeWindow(db, particlesTable, row);
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
		
		JMenu importMenu = new JMenu("Import Collection. . . ");
		loadATOFMSItem = new JMenuItem("from ATOFMS data. . .");
		loadATOFMSItem.addActionListener(this);
		loadEnchiladaDataItem = new JMenuItem("from Enchilada data. . .");
		loadEnchiladaDataItem.addActionListener(this);
		importMenu.setMnemonic(KeyEvent.VK_I);
		importMenu.add(loadATOFMSItem);
		importMenu.add(loadEnchiladaDataItem);
		
		JMenu exportMenu = new JMenu("Export Collection. . .");
		MSAexportItem = new JMenuItem("to MS-Analyze. . .");
		MSAexportItem.addActionListener(this);
		exportMenu.setMnemonic(KeyEvent.VK_E);
		exportMenu.add(MSAexportItem);
		
		rebuildItem = new JMenuItem("Rebuild Database", KeyEvent.VK_R);
		rebuildItem.addActionListener(this);
		
		exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitItem.addActionListener(this);
		
		fileMenu.add(emptyCollection);
		fileMenu.addSeparator();
		fileMenu.add(importMenu);
		fileMenu.add(exportMenu);
		fileMenu.addSeparator();
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
		JMenu analysisMenu = new JMenu("Analysis");
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
		
		analysisMenu.add(clusterItem);
		analysisMenu.add(labelItem);
		analysisMenu.add(classifyItem);
		analysisMenu.add(queryItem);
		
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
		JMenuItem accessSelected = new JMenuItem("View Selected", 
				KeyEvent.VK_V);
		
		collectionMenu.add(deleteAdoptItem);
		collectionMenu.add(recursiveDeleteItem);
		collectionMenu.addSeparator();
		collectionMenu.add(accessSelected);		
		
		//Add a help menu to the menu bar.
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);
		
		JMenuItem helpItem = new JMenuItem("Help Contents", 
				KeyEvent.VK_H);        
		JMenuItem aboutItem = new JMenuItem("About Enchilada", 
				KeyEvent.VK_A);
		
		helpMenu.add(helpItem);
		helpMenu.addSeparator();
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
		
		emptyCollButton = new JButton("New Empty Collection");
		emptyCollButton.setBorder(new EtchedBorder());
		emptyCollButton.addActionListener(this);
		
		exportParsButton = new JButton("Export to MS-Analyze");
		exportParsButton.setBorder(new EtchedBorder());
		exportParsButton.addActionListener(this);
		
		buttonPanel.add(emptyCollButton);
		buttonPanel.add(importParsButton);
		buttonPanel.add(importEnchiladaDataButton);
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
		columns.add("Click on a collection to see information.");
		
		data = new Vector<Vector<Object>>(1000);
		Vector<Object> row = new Vector<Object>(1);
		row.add("");
		data.add(row);
		
		particlesTable = new JTable(data, columns);
		
		analyzeParticleButton = new JButton("Analyze Particle");
		analyzeParticleButton.setEnabled(false);
		analyzeParticleButton.addActionListener(this);
		
		particlePanel = new JPanel(new BorderLayout());
		particleTablePane = new JScrollPane(particlesTable);
		JPanel partOpsPane = new JPanel(new FlowLayout());
		partOpsPane.add(analyzeParticleButton, BorderLayout.CENTER);
		
		particlePanel.add(particleTablePane, BorderLayout.CENTER);
		particlePanel.add(partOpsPane, BorderLayout.SOUTH);
		
		collectionViewPanel.addTab("Particle List", null, particlePanel,
				null);
		
		//rightPane.addTab("Spectrum Viewer Text", null, panel2, null);
		descriptionTA = new JTextArea("Description here");
		infoPanel = makeTextPanel(descriptionTA);
		JScrollPane collInfoPane = new JScrollPane(infoPanel);
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
			mapValuesButton.setEnabled(false);
		} else if (colTree == synchronizedPane) {
			panelChanged = setupRightWindowForSynchronization(collection);
			mapValuesButton.setEnabled(collection.containsData());
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

	private boolean setupRightWindowForCollection(Collection collection) {
		mainSplitPane.setBottomComponent(collectionViewPanel);
		
        String dataType = collection.getDatatype();
        ArrayList<String> colnames = db.getColNames(dataType, DynamicTable.AtomInfoDense);
        
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
			particlesTable.setEnabled(false);
			
			// Change to just show table (no button)...
			collectionViewPanel.setComponentAt(0, new JScrollPane(particlesTable));
		}
		
		data.clear();
		data = db.updateParticleTable(collection, data);
	    
		particlesTable.tableChanged(new TableModelEvent(particlesTable.getModel()));
        particlesTable.doLayout();
		
		return true;
	}

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

				SQLServerDatabase.rebuildDatabase("SpASMSdb");
			}			
		}
		
		//Open database connection:
		db = new SQLServerDatabase("SpASMSdb");
		db.openConnection();

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainFrame();
			}
		});
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
}
