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
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

import database.InfoWarehouse;
import database.Database;
import database.VersionChecker;
import database.DynamicTable;
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
	/*
	 * These capabilities work, but only with trivially small databases
	private JMenuItem importXmlDatabaseItem;
	private JMenuItem importXlsDatabaseItem;
	private JMenuItem importCsvDatabaseItem;
	private JMenuItem exportXmlDatabaseItem;
	private JMenuItem exportXlsDatabaseItem;
	private JMenuItem exportCsvDatabaseItem;
	*/
	private JMenuItem emptyCollection;
	private JMenuItem queryItem;
	private JMenuItem compressItem;
	private JMenuItem clusterItem;
	private JMenuItem detectPlumesItem;
	private JMenuItem rebuildItem;
	private JMenuItem exitItem;
	private JMenuItem compactDBItem;
	private JMenuItem backupItem;
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
	private ArrayList<Integer> childrenIDs;
	private String copyCollection;
	private boolean cutBool = false;
	
	private JTable particlesTable = null;
	private Vector<Vector<Object>> data = null;
	
	public static InfoWarehouse db;
	private JComponent infoPanel;
	
	private JTabbedPane collectionViewPanel;
	private JPanel particlePanel;
	private JScrollPane particleTablePane;
	private JScrollPane collInfoPane;
	private JMenuItem visualizeItem;
	private JMenuItem outputItem;
	private JMenuItem aboutItem;
	
	private OutputWindow outputFrame;
	private JTextField searchFileBox;
	private JButton forwardButton;
	private JButton backwardButton;
	
	private int currCollectionSize;
	private int currHigh;
	private int currLow;
	private JLabel currentlyShowing;
	private JButton searchButton;
	private int currCollection;
	
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
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//HACK!!
		//Responsibility lies with:
		//@author shaferia
		//@see http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/UIManager.html
		Font f = new Font("Dialog", Font.PLAIN, 11);
		fixFonts(f);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.gif"));
		
		setSize(800, 600);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		/* @author steinbel
		 * Set ErrorLogger testing boolean to "false" so error dialogs will show.
		 */
		ErrorLogger.testing = false;
		
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
		 * Use a SpringLayout to layout the components that have been added
		 */
		performLayout();
		
		//Display the window.
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
				
			}
		});
		//Various hacks, the fault of:
		//@author shaferia
		// - fix Swing focus bug that causes problems with fast alt-tabbing
		// - apply Enchilada icon to all frames
		addWindowFocusListener(new WindowFocusListener() {
			public void windowLostFocus(WindowEvent event) {
				Window w = event.getOppositeWindow();
				if (w != null) {
					/* Below code commented out for nov2006 release to prevent
					 * output window from appearing on top of main window at 
					 * startup. TEMPORARY hack because this is what fixes the
					 * fast alt-tab bug.  - steinbel 11.8.06
					 */
					//if (event.getWindow() instanceof MainFrame)
						//w.requestFocus();
					/* end commenting by steinbel 11.8.06 */
					if (w instanceof Frame) {
						boolean found = false;
						for (WindowFocusListener listen : w.getWindowFocusListeners()) {
							if (this == listen) {
								found = true;
								break;
							}
						}
						if (!found) {
							if (event.getWindow() != null && event.getWindow() instanceof Frame)
								((Frame) w).setIconImage(((Frame)event.getWindow()).getIconImage());
							w.addWindowFocusListener(this);
						}
					}
				}
			}
			public void windowGainedFocus(WindowEvent e) {
			}
		});
	}
	
	/**
	 * Call when a complete change is performed to the database contents
	 * (upon database rebuild or restore)
	 * @author shaferia
	 */
	public void refreshData() {
		remove(mainSplitPane);
		
		setupSplitPane();
		add(mainSplitPane);
		
		performLayout();
		
		getContentPane().validate();
	}
	
	/**
	 * Layout the components on the frame with a SpringLayout
	 */
	private void performLayout() {
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
	}
	
	private void fixFonts(Font f) {
		UIDefaults defaults = UIManager.getDefaults();
		Object key = null;
        for (java.util.Enumeration keys = UIManager.getDefaults().keys(); 
        	keys.hasMoreElements();
        	key = keys.nextElement())
        {
        	if (key != null && key.toString().endsWith(".font")) {
           		defaults.put(key, f);      		
        	}
        }
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
					"For support, please contact dmusican@carleton.edu.\n" +
					"Software Version nov-2006-4"
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
			if (abig.init()) abig.go(collectionPane);
			// tree update and validate is being done in method go,
			// when 'finished' method runs --- DRM
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
			if(c == null) {
				JOptionPane.showMessageDialog(this, "Please select a collection to delete.",
						"No collection selected", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
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
			if(c == null) {
				JOptionPane.showMessageDialog(this, "Please select a collection to delete.",
						"No collection selected", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
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
			copyID = 
				getSelectedCollection().getCollectionID();
			childrenIDs = getSelectedCollection().getSubCollectionIDs(); 
			copyCollection = getSelectedCollection().getName();
			if (copyID == 0) { //don't allow copying/pasting of root
				JOptionPane.showMessageDialog(this, "Please select a collection to copy.",
						"No collection selected", JOptionPane.WARNING_MESSAGE);
			}
			else {
				cutBool = false;
				pasteItem.setEnabled(true);
			}
		}
		
		else if (source == cutItem)
		{
			copyID = 
				getSelectedCollection().getCollectionID();
			childrenIDs = getSelectedCollection().getSubCollectionIDs(); 
			copyCollection = getSelectedCollection().getName();
			if (copyID == 0) { //don't allow copying/pasting of root
				JOptionPane.showMessageDialog(this, "Please select a collection to cut.",
						"No collection selected", JOptionPane.WARNING_MESSAGE);
			}
			else {
				cutBool = true;
				pasteItem.setEnabled(true);
			}
			
		}
		
		else if (source == pasteItem)
		{
			//if no collection selected, paste into root - @author steinbel

			if (copyID != 
				getSelectedCollection().getCollectionID() && copyID>-1)
			{
				//Ensure that a collection is not being pasted into one of its children
				// @author shaferia, @jane
					
				if (childrenIDs.contains(getSelectedCollection().getCollectionID())) {
					JOptionPane.showMessageDialog(this, "Cannot paste "  +  copyCollection + ": " + " the destination is a subcollection of " + copyCollection + "." );
					return;
				}
				//}
				
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
							" be of the same data type", "Invalid collection", JOptionPane.WARNING_MESSAGE);
			} else
				JOptionPane.showMessageDialog(this, "Cannot copy/paste to the same " +
						"destination as the source.", "Invalid collection", JOptionPane.WARNING_MESSAGE);
		}
		else if (source == queryItem) {
			if (collectionPane.getSelectedCollection() == null)
				JOptionPane.showMessageDialog(this, "Please select a collection to query.", 
						"No collection selected.", JOptionPane.WARNING_MESSAGE);
			else
				new QueryDialog(this, collectionPane, db, getSelectedCollection());
		}
		else if (source == clusterItem) {
			if (collectionPane.getSelectedCollection() == null)
				JOptionPane.showMessageDialog(this, "Please select a collection to cluster.", 
						"No collection selected.", JOptionPane.WARNING_MESSAGE);
			else
				new ClusterDialog(this, collectionPane, db);
		}
		else if (source == visualizeItem) {
			if (getSelectedCollection().getCollectionID() == 0)
				JOptionPane.showMessageDialog(this, "Please select a collection to visualize.", 
						"No collection selected.", JOptionPane.WARNING_MESSAGE);
			else
			try {
				(new chartlib.hist.HistogramsWindow(
					getSelectedCollection().getCollectionID())).setVisible(true);
			} catch (IllegalArgumentException exce) {
				JOptionPane.showMessageDialog(this, "Spectrum Histograms only" +
						" work on ATOFMS collections for now.");
			}
		}
		else if (source == detectPlumesItem){
			if (synchronizedPane.getSelectedCollection() == null)
				JOptionPane.showMessageDialog(this, "Please select a collection which to detect plumes.", 
						"No collection selected.", JOptionPane.WARNING_MESSAGE);
			else
				new DetectPlumesDialog(this,synchronizedPane, db);
		}
		
		else if (source == compressItem) {
			if (collectionPane.getSelectedCollection() == null)
				JOptionPane.showMessageDialog(this, "Please select a collection to compress.", 
						"No collection selected.", JOptionPane.WARNING_MESSAGE);
			else {
				//TODO: Provide a way to set the collection's name
				//or give it better default
				BIRCH b = new BIRCH(collectionPane.getSelectedCollection(),db,"name","comment",DistanceMetric.EUCLIDEAN_SQUARED);
				b.compress();
				collectionPane.updateTree();
			}
		}
		
		else if (source == rebuildItem) {
			if (JOptionPane.showConfirmDialog(this,
			"Are you sure? This will destroy all data in your database.") ==
				JOptionPane.YES_OPTION) {
				//db.rebuildDatabase();
				final JFrame thisref = this;
				final ProgressBarWrapper pbar = 
					new ProgressBarWrapper(thisref, "Rebuilding Database", 100);
				pbar.setIndeterminate(true);
				pbar.setText("Rebuilding Database...");
				
				UIWorker worker = new UIWorker() {
					public Object construct() {
						db.closeConnection();
						try {
							Database.rebuildDatabase("SpASMSdb");
							return true;
						}
						catch (SQLException ex) {
							return false;
						}
					}
					public void finished() {
						super.finished();
						if ((Boolean) get()) {
							//changed by shaferia, 1/8/06
							//no restart on database rebuild
							/*
							JOptionPane.showMessageDialog(thisref,
								"The program will now shut down to reset itself. " +
								"Start it up again to continue.");
							dispose();
							*/
							db.openConnection();
							refreshData();
							pbar.disposeThis();
						}
						else {
							pbar.disposeThis();
							JOptionPane.showMessageDialog(thisref,
								"Could not rebuild the database." +
								"  Close any other programs that may be accessing the database and try again.");
						}
					}
				};
				
				pbar.constructThis();
				worker.start();
			}			
		}
		else if (source == compactDBItem){
			final ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(this, "Compacting Database",100);
			progressBar.constructThis();
			progressBar.setIndeterminate(true);
			progressBar.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			UIWorker worker = new UIWorker() {
				public Object construct() {
					((Database)db).compactDatabase(progressBar);
					return null;
				}
				public void finished() {
					super.finished();
					progressBar.disposeThis();
				}
			};
			worker.start();
		}
		else if (source == backupItem) {
			new BackupDialog(this, db);
		}
		else if (source == exitItem) {
			exit();
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
		/*
		 * @author steinbel
		 */
		else if (source == forwardButton) {
			currLow = currHigh + 1;
			if ( (currHigh + 1000) >= currCollectionSize ){
				currHigh = currCollectionSize;
				forwardButton.setEnabled(false);
			} else
				currHigh += 1000;
			backwardButton.setEnabled(true);
			setTable();
		}
		/*
		 * @author steinbel
		 */
		else if (source == backwardButton) {
			currHigh = currLow - 1;
			if ( (currLow - 1000) <= 1 ){
				currLow = 1;
				backwardButton.setEnabled(false);
			} else
				currLow -= 1000;

			forwardButton.setEnabled(true);
			setTable();	
		}
		/*
		 * @author steinbel
		 */
		else if (source == searchFileBox) {
			//see if filename is valid and set table accordingly
			String searchMe = searchFileBox.getText();
			if (!searchMe.equals(" Enter a filename to search for a particle."))
				searchOn(searchMe);
			else 
				ErrorLogger.displayException(this, "Please enter a filename.");
		}
		/*
		 * @author steinbel
		 * //commented out for fall 06 release - steinbel
		 */
/*		else if (source == searchButton) {
			//see if filename is valid and set table accordingly
			String searchMe = searchFileBox.getText();
			if (!searchMe.equals(" Enter a filename to search for a particle."))
				searchOn(searchMe);
			else 
				ErrorLogger.displayException(this, "Please enter a filename.");
		}
*/		
		ErrorLogger.flushLog(this);
	}
	
	/**
	 * @author steinbel
	 * Searches the db for the filename and sets the particle pane to show that
	 * filename and its surrounding 1000 particles if the particle is in the
	 * database.  (If atom is in a different collection, pops it open.)
	 * @param searchString - the file name desired (must include entire path)
	 */
	private void searchOn(String searchString) {
		
		//find out the atomid for future reference
		int atomID = db.getATOFMSAtomID(searchString);
		//if the atom actually exists
		if (atomID >= 0){
			//if already showing the correct section of the collection
			if ((currLow <= atomID) && (atomID <= currHigh)){
				//highlight particle
				int particleRow;
				if (atomID <= 1001)
					particleRow = atomID - 1;
				else
					particleRow = atomID - 1001;
				particlesTable.changeSelection(particleRow, 0, false, false);
				
			} else if (db.collectionContainsAtom(currCollection, atomID)){
				//switch to correct high and low to show particle
				
			} else { //switch to correct collection
				
				
			}
				
		} //else do nothing - error message is in the InfoWarehouse-level method
		
	}

	public void exit(){
		if (db.isDirty() && JOptionPane.showConfirmDialog(null,
				"One or more Collections has been deleted." +
				"  Would you like to Clean up database? This may take several minutes to"+
				" perform, but will keep Enchilada running efficiently.","Compact Database?",
				JOptionPane.YES_NO_OPTION) ==
					JOptionPane.YES_OPTION) {
			final ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(this, "Compacting Database",100);
			progressBar.constructThis();
			progressBar.setIndeterminate(true);
			UIWorker worker = new UIWorker() {
				public Object construct() {
					((Database)db).compactDatabase(progressBar);
					db.closeConnection();
					return null;
				}
				public void finished() {
					super.finished();
					progressBar.disposeThis();
					dispose();
					
					System.exit(0);
				}
			};
			worker.start();
			
		}else{
			db.closeConnection();
			dispose();
			
			System.exit(0);
		}
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
		
		/*
		 * These capabilities work, but only with trivially small databases
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
		*/
		compactDBItem = new JMenuItem("Compact Database", KeyEvent.VK_C);
		compactDBItem.addActionListener(this);
		
		rebuildItem = new JMenuItem("Rebuild Database", KeyEvent.VK_R);
		rebuildItem.addActionListener(this);
		
		backupItem = new JMenuItem("Backup/Restore...", KeyEvent.VK_B);
		backupItem.addActionListener(this);
		
		exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitItem.addActionListener(this);
		
		fileMenu.add(emptyCollection);
		fileMenu.addSeparator();
		fileMenu.add(importCollectionMenu);
		fileMenu.add(exportCollectionMenu);
		fileMenu.addSeparator();
		/*
		 * These capabilities work, but only with trivially small databases
		fileMenu.add(importDatabaseMenu);
		fileMenu.add(exportDatabaseMenu);
		*/
		fileMenu.add(compactDBItem);
		fileMenu.add(rebuildItem);
		fileMenu.add(backupItem);
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
		pasteItem.setEnabled(false);
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
//		JMenuItem labelItem = new JMenuItem("Label. . .", 
//				KeyEvent.VK_L);
//		JMenuItem classifyItem = new JMenuItem("Classify. . . ", 
//				KeyEvent.VK_F);
		queryItem = new JMenuItem("Query. . . ", KeyEvent.VK_Q);
		queryItem.addActionListener(this);
		compressItem = new JMenuItem("Compress. . . ", KeyEvent.VK_P);
		compressItem.addActionListener(this);
		visualizeItem = new JMenuItem("Visualize. . .", KeyEvent.VK_V);
		visualizeItem.addActionListener(this);
		detectPlumesItem = new JMenuItem("Detect Plumes. . .", KeyEvent.VK_W);
		detectPlumesItem.addActionListener(this);
		
		analysisMenu.add(clusterItem);
//		analysisMenu.add(labelItem);
//		analysisMenu.add(classifyItem);
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
		
		importParsButton = new JButton("Import ATOFMS Data");
		importParsButton.setBorder(new EtchedBorder());
		importParsButton.addActionListener(this);
		
		importEnchiladaDataButton = new JButton("Import Enchilada Data Sets");
		importEnchiladaDataButton.setBorder(new EtchedBorder());
		importEnchiladaDataButton.addActionListener(this);
		
		importAMSDataButton = new JButton("Import AMS Data");
		importAMSDataButton.setBorder(new EtchedBorder());
		importAMSDataButton.addActionListener(this);
		
		importFlatButton = new JButton("Import Time Series");
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
	 * @author steinbel
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
		

		currentlyShowing = new JLabel("Currently showing 0 particles");
		
		forwardButton = new JButton("Next");
		forwardButton.setEnabled(false);
		forwardButton.addActionListener(this);
		
		backwardButton = new JButton("Previous");
		backwardButton.setEnabled(false);
		backwardButton.addActionListener(this);
		
		String initSearch = " Enter a filename to search for a particle.";
		searchFileBox = new JTextField(initSearch);
		searchFileBox.setEnabled(false);
		searchFileBox.addActionListener(this);	
		
		searchButton = new JButton("Search");
		searchButton.setEnabled(false);
		searchButton.addActionListener(this);
		
		particlesTable = new JTable(data, columns);
		
		analyzeParticleButton = new JButton("Analyze Particle");
		analyzeParticleButton.setEnabled(false);
		analyzeParticleButton.addActionListener(this);
		
		JPanel comboPane = new JPanel(new BorderLayout());
		comboPane.add(currentlyShowing, BorderLayout.NORTH);
		comboPane.add(backwardButton, BorderLayout.WEST);
		comboPane.add(forwardButton, BorderLayout.EAST);
		
		JPanel searchPane = new JPanel(new BorderLayout());
		//put next two buttons on another line
//		searchPane.add(searchFileBox, BorderLayout.NORTH);//commented out for fall 06 release - steinbel
//		searchPane.add(searchButton, BorderLayout.SOUTH);
		
		JPanel buttonsPane = new JPanel(new BorderLayout());
		buttonsPane.add(comboPane, BorderLayout.EAST);
		buttonsPane.add(searchPane, BorderLayout.WEST);
		
		particlePanel = new JPanel(new BorderLayout());
		particleTablePane = new JScrollPane(particlesTable);
		
		JPanel partOpsPane = new JPanel(new FlowLayout());
		partOpsPane.add(analyzeParticleButton, BorderLayout.CENTER);
		
		particlePanel.add(buttonsPane, BorderLayout.NORTH);
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
		
		/*
		 * @author steinbel - changed to work with next/prev buttons
		 */ 
		currCollectionSize = db.getCollectionSize(collection.getCollectionID());
		currCollection = collection.getCollectionID();
		
		currLow = 1;
		backwardButton.setEnabled(false);
		
		if (currCollectionSize > 1000) {
			forwardButton.setEnabled(true);
			currHigh = 1000;
		}
		else{
			currHigh = currCollectionSize;
			forwardButton.setEnabled(false);
		}
		
		//allow searching in this collection
//		searchButton.setEnabled(true);	//commented out for fall 06 release - steinbel
//		searchFileBox.setEnabled(true);
		
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
	
	/**
	 * Offers functionality for connecting different databases while maintaining
	 * the connection to "SpASMSdb" in main method, refactored by @author xzhang9
	 */
	public static void main(String[] args) {
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
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// @author xzhang9 
		// Verify that production database exists, and give user opportunity to create if it does not.
		if(!connectDB("SpASMSdb")) return;
		db.openConnection();
		

		//@author steinbel
		VersionChecker vc = new VersionChecker(db);
		try {
			if (! vc.isDatabaseCurrent()) {
				//new error window, but it should have button options, so the
				//existing error framework isn't any good.
				//soooo want new JOptionPane

				Object[] options = {"Rebuild database", "Quit"};
				int action = JOptionPane.showOptionDialog(null, 
						"Your database is an old version and " +
						"incompatible with this version of Enchilada.\n Please" +
						" either rebuild the database (permanently deletes " +
						"your current database\n and all data within it) or " +
						"quit and use an older version of Enchilada.",
						"Warning: Incompatible database",
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[1]);
				if (action == 0){
					db.closeConnection();
					Database.rebuildDatabase("SpASMSdb");
					db.openConnection();
				} else
					System.exit(0);
				
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
	 * Offers functionality for connecting different databases while maintaining
	 * the connection to "SpASMSdb" in main method
	 * @author xzhang9
	 */
	private static boolean connectDB(String dbName) {
		// Verify that database exists, and give user opportunity to create
		// if it does not.
		if (!Database.getDatabase(dbName).isPresent()) {
			if (JOptionPane.showConfirmDialog(null,
					"No database found. Would you like to create one?\n" +
					"Make sure to select yes only if there is no database already present,\n"
					+ "since this will remove any pre-existing Enchilada database.") ==
						JOptionPane.YES_OPTION) {
				try{
					Database.rebuildDatabase(dbName);
				}catch(SQLException s){
					JOptionPane.showMessageDialog(null,
							"Could not rebuild the database." +
							"  Close any other programs that may be accessing the database and try again.");
					return false;
				}
			} else {
				return false; // no database?  we shouldn't do anything at all.
			}
		}
		
		//Open database connection:
		db = Database.getDatabase(dbName);
		return true;
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
	
	/** 
	 * This clears the particle table when a collection is deleted.  
	 */
	public void clearTable() {
		//data.clear();
		data = new Vector<Vector<Object>>(1000);
		Vector<Object> row = new Vector<Object>(6);
		for (int x = 0; x < 6; ++x)
			row.add("");
		data.add(row);
		Vector<Object> columns = new Vector<Object>(1);
		columns.add("");
		
		particlesTable = new JTable(data, columns);
		particlesTable.doLayout();
		particleTablePane.setViewportView(particlesTable);
		collectionViewPanel.setComponentAt(0, particlePanel);
		collectionViewPanel.repaint();
		
//		searchFileBox.setEnabled(false);	//commented out for fall 06 release - steinbel
//		searchButton.setEnabled(false);
		
		analyzeParticleButton.setEnabled(false);
		
		particlesTable.validate();
		analyzeParticleButton.validate();
	}
	
	/**
	 * @author steinbel
	 * This sets the particle table to show 1000 (or fewer) particles at a time.
	 * 
	 * It is called by the setupRightWindow method and by the actionlistener
	 * when the next or previous buttons are clicked.
	 *
	 */
	public void setTable() {
			
			//System.out.println("low " + currLow + " high " + currHigh + " "
			//		+ ((currHigh - currLow)+1));//TESTING
			//clear data in table and repopulate it with appropriate 
			// data.
			currentlyShowing.setText("Currently showing particles " + currLow +
					"-" + currHigh + " of " + currCollectionSize + ".");
			data.clear();
			db.updateParticleTable(getSelectedCollection(),data,currLow,currHigh);
			particlesTable.tableChanged(new TableModelEvent(particlesTable.getModel()));
			particlesTable.doLayout();
	
	}
	
	/**
	 * Updates the collection tree to show the requested collection.
	 * @param cID	The collection to highlight.
	 */
	public void updateCurrentCollection(int cID){
		
		collectionPane.switchCollections(cID);
	}
}