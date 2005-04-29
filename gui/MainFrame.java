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

import atom.Peak;
//import msanalyze.DataSetImporter;
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
public class MainFrame extends JFrame implements ActionListener, 
	WindowListener, ListSelectionListener, KeyListener
{
	public static final int DESCRIPTION = 3;
	private JToolBar buttonPanel;
	private JToolBar specButtonPanel;
	private JSplitPane splitPane;
	
	private JButton importParsButton;
	private JButton exportParsButton;
	private JButton emptyCollButton;
	private JButton specPrevButton;
	private JButton specNextButton;
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
	private JMenuItem nextParticleItem;
	private JMenuItem prevParticleItem;
	private JMenuItem unzoomItem;
	private CollectionTree leftPane;
	private JTextArea descriptionTA;

	private int copyID = -1;
	private boolean cutBool = false;

	private JTable particlesTable = null;
	private Vector<Vector<Object>> data = null;
	
	public static SQLServerDatabase db;
	private JComponent infoPanel;
	private JTextArea peaksText;
	private PeaksChart peaksChart;
	private int lastAtomID;
	
	/**
	 * Constructor.  Creates and shows the GUI.	 
	 */
	public MainFrame()
	{
		super("SpASMS");

        setDefaultLookAndFeelDecorated(true);
        
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
		layout.putConstraint(SpringLayout.NORTH, splitPane, 5,
				SpringLayout.SOUTH, buttonPanel);
		layout.putConstraint(SpringLayout.WEST, splitPane, 5,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5,
				SpringLayout.EAST, buttonPanel);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5,
				SpringLayout.EAST, splitPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 5,
				SpringLayout.SOUTH, splitPane);
	
		//Display the window.
		setVisible(true);
		
	
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (source == importParsButton || source == loadATOFMSItem) 
		{
			new ImportParsDialog(this);
			
			leftPane.updateTree();
			validate();
		}

		
		else if (source == emptyCollButton || source == emptyCollection) {
			new EmptyCollectionDialog(this);
			leftPane.updateTree();
			validate();
		}
		
		else if (source == exportParsButton || source == MSAexportItem)
		{
			leftPane.getSelectedCollection().exportToPar();
		}
		
		else if (source == deleteAdoptItem)
		{
	        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			db.orphanAndAdopt(leftPane.getSelectedCollection().getCollectionID());
	        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			leftPane.updateTree();
			validate();
		}
		
		else if (source == recursiveDeleteItem)
		{
	        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			db.recursiveDelete(leftPane.getSelectedCollection().getCollectionID());
	        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			leftPane.updateTree();
			validate();
		}
		
		else if (source == copyItem)
		{
			cutBool = false;
			copyID = 
				leftPane.getSelectedCollection().getCollectionID();
		}
		
		else if (source == cutItem)
		{
			cutBool = true;
			copyID = 
				leftPane.getSelectedCollection().getCollectionID();
		}
		
		else if (source == pasteItem)
		{
			if (copyID != 
				leftPane.getSelectedCollection().getCollectionID())
			{
				if (cutBool == false)
				{
					db.copyCollection(copyID, 
							leftPane.getSelectedCollection().
							getCollectionID());
				}
				else
				{
					db.moveCollection(copyID,
							leftPane.getSelectedCollection().
							getCollectionID());
					
				}
				leftPane.updateTree();
				validate();
			}
			else
				System.err.println("Cannot copy/paste to the same " +
						"destination as the source");
		}
		else if (source == queryItem) {new QueryDialog(this, 
											leftPane, db);}
		
		else if (source == clusterItem) {new ClusterDialog(this, 
				leftPane, db);}
		
		
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
		
		else if(source == nextParticleItem)
		{
			particlesTable.changeSelection(particlesTable.getSelectedRow() - 1,
					particlesTable.getSelectedColumn(),
					false,
					false);
		}
		
		else if(source == prevParticleItem)
		{
			particlesTable.changeSelection(particlesTable.getSelectedRow() + 1,
					particlesTable.getSelectedColumn(),
					false,
					false);
		}
		
		else if(source == unzoomItem)
		{
			peaksChart.unZoom();
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
		importMenu.setMnemonic(KeyEvent.VK_I);
		importMenu.add(loadATOFMSItem);
		
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
		
		//Add a graph menu to menu bar
		JMenu graphMenu = new JMenu("Graph");
		graphMenu.setMnemonic(KeyEvent.VK_G);
		menuBar.add(graphMenu);
		
		nextParticleItem = new JMenuItem("Next Particle",
				KeyEvent.VK_UP);
		nextParticleItem.addActionListener(this);
		prevParticleItem = new JMenuItem("Previous Particle",
				KeyEvent.VK_DOWN);
		prevParticleItem.addActionListener(this);
		unzoomItem = new JMenuItem("Unzoom Graph", KeyEvent.VK_Z);
		unzoomItem.addActionListener(this);
		
		graphMenu.add(nextParticleItem);
		graphMenu.add(prevParticleItem);
		graphMenu.addSeparator();
		graphMenu.add(unzoomItem);
		
		
		//Add a help menu to the menu bar.
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);
		
		JMenuItem helpItem = new JMenuItem("Help Contents", 
				KeyEvent.VK_H);        
		JMenuItem aboutItem = new JMenuItem("About SpASMS", 
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
		
		emptyCollButton = new JButton("New Empty Collection");
		emptyCollButton.setBorder(new EtchedBorder());
		emptyCollButton.addActionListener(this);
		
		exportParsButton = new JButton("Export to MS-Analyze");
		exportParsButton.setBorder(new EtchedBorder());
		exportParsButton.addActionListener(this);
		
		buttonPanel.add(emptyCollButton);
		buttonPanel.add(importParsButton);
		buttonPanel.add(exportParsButton);
		add(buttonPanel);
	}
	
	/**
	 * setupSplitPane() creates and adds a split pane to the frame. The 
	 * left side of the split pane contains a tree; the right
	 * side of the split pane contains a tabbed pane.  Everything 
	 * except the spectrum viewer is scrollable.
	 */  
	private void setupSplitPane()
	{  
		// Add a JTree to the split pane.
		leftPane = new CollectionTree(db, this);
		leftPane.setMinimumSize(new Dimension(128,64));
		
		// Add a JTabbedPane to the split pane.
		JTabbedPane rightPane = new JTabbedPane();
		
		//Add a dummy table to the Particle Pane.
		Vector<String> columns = new Vector<String>(4);
		columns.add("Particle ID");
		columns.add("Filename");
		columns.add("Size");
		columns.add("Time");
		data = new Vector<Vector<Object>>(1000);
		Vector<Object> row = new Vector<Object>(4);
		row.add(new Integer(0));
		row.add("");
		row.add(new Integer(0));
		row.add("");
		data.add(row);
		
		particlesTable = new JTable(/*new AtomTableModel(db,0)*/data,columns);
		particlesTable.setDefaultEditor(Object.class, null);
		ListSelectionModel lModel = 
			particlesTable.getSelectionModel();
		lModel.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		lModel.addListSelectionListener(this);

		//TODO:  Instead of using the default table model,
		// implement a subclass of AbstractTableModel that goes
		// directly to the database to get rows/columns

		JScrollPane particlePane = 
			new JScrollPane(particlesTable);
		
		peaksText = new JTextArea("Peaks:");
		peaksText.setEditable(false);
		peaksText.setPreferredSize(new Dimension(150, 450));
		
		/*JScrollPane peaksPane = new JScrollPane(peaksText);
		
		JPanel partInfoPane = new JPanel();
		SpringLayout partInfoLayout = new SpringLayout();
		partInfoPane.setLayout(partInfoLayout);
		partInfoPane.add(particlePane);
		partInfoPane.add(peaksPane);
		//partInfoLayout.putConstraint(
		//		SpringLayout.NORTH,	particlePane, 0,
		//		SpringLayout.NORTH, partInfoPane);
		//partInfoLayout.putConstraint(
		//		SpringLayout.SOUTH,	particlePane, 0,
		//		SpringLayout.SOUTH, partInfoPane);
		//partInfoLayout.putConstraint(
		//		SpringLayout.WEST, particlePane, 0,
		//		SpringLayout.EAST, partInfoPane);
		partInfoLayout.putConstraint(
				SpringLayout.WEST, peaksPane, 5, 
				SpringLayout.EAST, particlePane);
	//	partInfoLayout.putConstraint(
		//		SpringLayout.EAST, peaksText,0,
		//		SpringLayout.WEST, partInfoPane);
		//partInfoLayout.putConstraint(
		//		SpringLayout.SOUTH, peaksText,0,
		//		SpringLayout.SOUTH, partInfoPane);
		//partInfoLayout.putConstraint(
		//		SpringLayout.NORTH, peaksText, 0,
		//		SpringLayout.NORTH, partInfoPane);*/
		rightPane.addTab(
				"Particle List", 
				null, particlePane, null);
		
		//text version of peak data
		JComponent panel2 = makeTextPanel(
				peaksText);
		
		
		//graphic version of peak data
		JComponent panel2a = new JPanel(new GridLayout(1,1));
		peaksChart = new PeaksChart();

		
		//sets up a keystroke so that arrow keys will change the selected
		//particle.
		panel2a.addKeyListener(this);
		panel2a.add(peaksChart);
		
		rightPane.addTab("Spectrum Viewer Text", null, panel2, null);
		//rightPane.addTab("Spectrum Viewer Graph", null, panel2a, null);
		rightPane.addTab("Zoomable Graph", null, panel2a, null);
		descriptionTA = new JTextArea("Description here");
		infoPanel = makeTextPanel(descriptionTA);
		JScrollPane collectionPane = new JScrollPane(infoPanel);
		rightPane.addTab("Collection Information", 
				null, collectionPane, null);
		
		/*JComponent panel4 = makeTextPanel(
				new JTextArea("Subcollection information here."));
		JScrollPane subcollectionPane = new JScrollPane(panel4);
		rightPane.addTab("Subcollections", null, subcollectionPane, null);
		*/
		// Create and add the split pane.
		splitPane = 
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
		add(splitPane);
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
	
	/* WindowListener Interface Implementation */
	public void windowClosing(WindowEvent e) {
       db.closeConnection();
    }
	 
	public void windowOpening(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowGainedFocus(WindowEvent e) {}
    public void windowLostFocus(WindowEvent e) {}
    public void windowStateChanged(WindowEvent e) {}
	
	public static void main(String[] args) {

		// Verify that database exists, and give user opportunity to create
		// if it does not.
		if (!SQLServerDatabase.isPresent("localhost","1433","SpASMSdb")) {

			if (JOptionPane.showConfirmDialog(null,
					"No database found. Would you like to create one?\n" +
					"Make sure to select yes only if there is no database already present,\n"
					+ "since this will remove any pre-existing SpASMS database.") ==
						JOptionPane.YES_OPTION) {

				SQLServerDatabase.rebuildDatabase("SpASMSdb");
			}			
		}
		
		//Open database connection:
		db = new SQLServerDatabase("localhost","1433","SpASMSdb");
		db.openConnection();

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame mFrame = new MainFrame();
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
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	/**
	 * Updates with peaklist information for the selected 
	 * atom
	 */
	public void valueChanged(ListSelectionEvent arg0) {
		int row = particlesTable.getSelectedRow();
		String peakString = null;
		if (row != -1)
		{
			int atomID = ((Integer) 
					particlesTable.getValueAt(row, 0))
					.intValue();
			String filename = (String)particlesTable.getValueAt(row,1);
			
			if (atomID != lastAtomID && atomID >= 0)
			{
				System.out.println("AtomID = " + atomID);
				ArrayList<Peak> peaks = db.getPeaks(atomID);
				peakString = "Peaks:\n";

				for (Peak p : peaks)
				{
					peakString += 
						"\t" + p.toString() + "\n";
					

				}
				System.out.println(peakString);
				peaksText.setText(peakString);
				peaksChart.setPeaks(peaks, atomID, filename);
			}
			
			lastAtomID = atomID;
		}
	}
	
	/**
	 * When an arrow key is pressed, moves to
	 * the next particle.
	 */
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		int curRow = particlesTable.getSelectedRow();
		int curColumn = particlesTable.getSelectedColumn();

		if((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_DOWN)
				&& curRow < particlesTable.getRowCount() - 1){
			
			particlesTable.changeSelection(particlesTable.getSelectedRow() + 1,
					particlesTable.getSelectedColumn(),
					false,
					false);
		}
		
		else if((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_UP)
				&& curRow > 0){
			particlesTable.changeSelection(particlesTable.getSelectedRow() - 1,
					particlesTable.getSelectedColumn(),
					false,
					false);
		}
		
		//Z unzooms the chart.
		else if(key == KeyEvent.VK_Z)
		{
			peaksChart.unZoom();
		}
		
		
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
}
