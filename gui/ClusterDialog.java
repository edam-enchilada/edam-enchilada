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
 * The Original Code is EDAM Enchilada's ClusterDialog class.
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
 * Created on Jul 19, 2004
 */
package gui;

import javax.swing.*;
import javax.swing.border.*;

import database.DynamicTable;
import database.InfoWarehouse;

import analysis.DistanceMetric;
import analysis.clustering.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 
 * The ClusterDialog opens a JDialog Object where the user can choose
 * a clustering algorithm to perform on the selected collection.  The user
 * inputs the parameters for each algorithm.  The user can cluster using only one 
 * algorithm at a time.  When OK is clicked, a new collection is created with the
 * specified name, and the clusters are created as sub-collections of the new 
 * collection.
 * 
 * 
 * @author ritza
 */
public class ClusterDialog extends JDialog implements ItemListener, ActionListener 
{
	
	/* Declared variables */
	private CollectionTree cTree;
	private InfoWarehouse db;
	
	private JFrame parent;
	private JPanel algorithmCards, specificationCards, clusteringInfo; 
	private JButton okButton, cancelButton, advancedButton;
	private JTextField commentField, passesText, vigText, learnText, kClusterText, otherText;
	private JCheckBox refineCentroids, normalizer;
	private JComboBox clusterDropDown, metricDropDown, averageClusterDropDown, infoTypeDropdown;
	private JLabel denseKeyLabel,sparseKeyLabel;
	private ArrayList<JRadioButton> sparseButtons;
	private ArrayList<JCheckBox> denseButtons; 

	// dropdown options
	final static String ART2A = "Art2a";
	final static String KCLUSTER = "K-Cluster";
	final static String KMEANS = "K-Means / Euclidean Squared";
	final static String KMEDIANS = "K-Medians / City Block";
	final static String SKMEANS = "K-Means / Dot Product";
	final static String OTHER = "Other";
	final static String CITY_BLOCK = "City Block";
	final static String EUCLIDEAN_SQUARED = "Euclidean Squared";
	final static String DOT_PRODUCT = "Dot Product";
	final static String init = " ";
	final static String dense = "Dense Particle Information";
	final static String sparse = "Sparse Particle Information";
	final static String denseKey = " Key = Automatic (1, 2, 3, etc) ";
	private static ArrayList<String> sparseKey;
	
	private boolean refinedCentroids = false;
	private String dMetric = CITY_BLOCK;
	private String currentShowing = ART2A;
	private String[] nameArray;

	/**
	 * Constructor.  Creates and shows the dialogue box.
	 * 
	 * @param frame - the parent JFrame of the JDialog object.
	 */
	public ClusterDialog(JFrame frame, CollectionTree cTree, 
			InfoWarehouse db) {
		super(frame,"Cluster",true);
		parent = frame;
		this.cTree = cTree;
		this.db = db;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel rootPanel = new JPanel();
		
		JPanel clusterAlgorithms = setClusteringAlgorithms();
		JPanel clusterSpecs = setClusteringSpecifications();
		
		//Create common info panel:
		JPanel commonInfo = setCommonInfo();
		getRootPane().setDefaultButton(okButton);
		
		//Changed to "Information" - benzaids
		clusterSpecs.setBorder(getSectionBorder("Information"));
		rootPanel.add(clusterSpecs);
		
		//Changed from step 2 to step 1 - benzaids
		clusterAlgorithms.setBorder(getSectionBorder("1. Choose Appropriate Clustering Algorithm"));
		rootPanel.add(clusterAlgorithms);

		//Changed from step 3 to step 2 - benzaids
		commonInfo.setBorder(getSectionBorder("2. Begin Clustering"));
		rootPanel.add(commonInfo);
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
		
		add(rootPanel, BorderLayout.CENTER);
		//rootPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		pack();
		
		//Display the dialogue box.
		setVisible(true);
	}
	
	private Border getSectionBorder(String title) {
		TitledBorder border = BorderFactory.createTitledBorder(title);
		Font font = border.getTitleFont();
		border.setTitleFont(new Font(font.getName(), font.getStyle(), 16));
		Border superBorder = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10), border);
		return superBorder;
	}
	
	/**
	 * creates panel that displays clustering algorithms.
	 * @return JPanel
	 */
	public JPanel setClusteringAlgorithms() {
		JLabel header = new JLabel("Cluster using: ");
		
		//Create the drop down menu and the dividing line.
		String[] clusterNames = {ART2A, KCLUSTER, OTHER};
		JPanel dropDown = new JPanel();
		clusterDropDown = new JComboBox(clusterNames);
		clusterDropDown.setEditable(false);
		clusterDropDown.addItemListener(this);
		dropDown.add(clusterDropDown);
		normalizer = new JCheckBox("Normalize data");
		normalizer.setSelected(true);

		JPanel headerAndDropDown = new JPanel();
		headerAndDropDown.add(header);
		headerAndDropDown.add(dropDown);
		headerAndDropDown.add(normalizer);
		
		JSeparator divider = new JSeparator(JSeparator.HORIZONTAL);
		divider.setBorder(BorderFactory.createRaisedBevelBorder());
		
		//Create the art2a panel that will show when "Art2a" is selected.
		JPanel parameters = new JPanel();
		parameters.setLayout(new FlowLayout());
		JLabel vigLabel = new JLabel("Vigilance:");
		JLabel learnLabel = new JLabel("Learning Rate:");
		JLabel passesLabel = new JLabel("Max # of Passes: ");
		passesText = new JTextField(5);
		vigText = new JTextField(5);
		learnText = new JTextField(5);
		parameters.add(vigLabel);
		parameters.add(vigText);
		parameters.add(learnLabel);
		parameters.add(learnText);
		parameters.add(passesLabel);
		parameters.add(passesText);
		
		JLabel distMetricLabel = new JLabel("Choose Distance Metric: ");
		JPanel art2aDropDown = new JPanel();
		String[] metricNames = {CITY_BLOCK, EUCLIDEAN_SQUARED, DOT_PRODUCT};
		metricDropDown = new JComboBox(metricNames);
		metricDropDown.setEditable(false);
		metricDropDown.addItemListener(this);
		art2aDropDown.add(distMetricLabel);
		art2aDropDown.add(metricDropDown);
		
		JPanel art2aCard = new JPanel();
		art2aCard.add(art2aDropDown);
		art2aCard.add(parameters);
		art2aCard.setLayout(
				new BoxLayout(art2aCard, BoxLayout.PAGE_AXIS));
		
		//Create the kCluster panel that will show when "K-Cluster" is selected.
		parameters = new JPanel();
		JLabel kLabel = new JLabel("Number of Clusters:");
		kClusterText = new JTextField(5);
		refineCentroids = new JCheckBox("Refine Centroids");
		refineCentroids.addItemListener(this);
		parameters.add(kLabel);
		parameters.add(kClusterText);
		parameters.add(refineCentroids);
		
		JLabel kClusterLabel = new JLabel("Choose algorithm: ");
		JPanel kClusterDropDown = new JPanel();
		String[] averagingNames = {KMEANS,KMEDIANS,SKMEANS};
		averageClusterDropDown = new JComboBox(averagingNames);
		averageClusterDropDown.setEditable(false);
		averageClusterDropDown.addItemListener(this);
		kClusterDropDown.add(kClusterLabel);
		kClusterDropDown.add(averageClusterDropDown);
		
		JPanel kClusterCard = new JPanel();
		kClusterCard.add(kClusterDropDown);
		kClusterCard.add(parameters);
		kClusterCard.setLayout(
				new BoxLayout(kClusterCard, BoxLayout.PAGE_AXIS));
		
		//Create the other panel that will show when "Other" is selected.
		JPanel otherCard = new JPanel();
		JLabel otherLabel = new JLabel("Parameters:");
		otherText = new JTextField(10); 
		otherCard.add(otherLabel);
		otherCard.add(otherText);
		
		// Add the previous three panels to the algorithmCards JPanel using CardLayout.
		algorithmCards = new JPanel (new CardLayout());
		algorithmCards.add(art2aCard, ART2A);
		algorithmCards.add(kClusterCard,KCLUSTER);
		algorithmCards.add(otherCard, OTHER);
				
	
		
		// Add all of the components to the main panel.
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.add(headerAndDropDown);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(divider);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(algorithmCards);
		return mainPanel;
	}
	
	/**
	 * creates the panel that displays the clustering specifications
	 * @return JPanel
	 */
	public JPanel setClusteringSpecifications() {
		// Set button arraylists
		denseButtons = getDenseColumnNames(cTree.getSelectedCollection().getDatatype());
		sparseButtons = getSparseColumnNames(cTree.getSelectedCollection().getDatatype());

		// Set dropdown for dense and sparse information
		String[] infoNames = {init, dense, sparse};
		infoTypeDropdown = new JComboBox(infoNames);
		infoTypeDropdown.addItemListener(this);
		
		// Set dense panel
		JPanel densePanel = new JPanel();
		densePanel.setLayout(new BoxLayout(densePanel, BoxLayout.PAGE_AXIS));	
		denseKeyLabel = new JLabel(denseKey);
		densePanel.add(denseKeyLabel);
		JLabel denseChoose = new JLabel("Choose one or more values below:");
		densePanel.add(denseChoose);
		JScrollPane denseButtonPane = getDenseButtonPane(denseButtons);
		densePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
		densePanel.add(denseButtonPane);

		// set sparse panel
		JPanel sparsePanel = new JPanel();
		sparsePanel.setLayout(new BoxLayout(sparsePanel, BoxLayout.PAGE_AXIS));
		sparseKey = db.getPrimaryKey(cTree.getSelectedCollection().getDatatype(),DynamicTable.AtomInfoSparse);
		assert (sparseKey.size() == 1) : "More than one sparse key!";
		sparseKeyLabel = new JLabel("key = " + sparseKey.get(0));
		sparsePanel.add(sparseKeyLabel);
		JLabel sparseChoose = new JLabel("Choose one value below:");
		sparsePanel.add(sparseChoose);
		JScrollPane sparseButtonPane = getSparseButtonPane(sparseButtons);
		sparsePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
		sparsePanel.add(sparseButtonPane);
		
		// Add dense and sparse panels to cards
		specificationCards = new JPanel(new CardLayout());
		specificationCards.add(new JPanel(), init);
		specificationCards.add(densePanel, dense);
		specificationCards.add(sparsePanel, sparse);
		
		// add cards to final panel
		JPanel panel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel();
//***We now only allow clustering on PeakArea - benzaids
//***To revert to original, remove all comments with ***
/***
//		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

		
//////////////////// HACK until bug is fixed - steinbel - then cut below and
		//uncomment line above setting layout to BoxLayout
		topPanel.setLayout(new BorderLayout());
		topPanel.add(new JLabel("NOTE: Clustering on anything other than peak area \n"
				+" renders the cluster centers meaningless.  This is a known bug we are working to fix."),
				BorderLayout.NORTH);
//////////////////// end HACK
***/
		
		//***topPanel.add(new JLabel("Choose Type of Particle Information to Cluster on: "), BorderLayout.WEST);
		//***topPanel.add(infoTypeDropdown, BorderLayout.CENTER);
		//***panel.add(specificationCards, BorderLayout.CENTER);
		
		topPanel.add(new JLabel("Clustering will be done on Peak Area."));
		panel.add(topPanel, BorderLayout.NORTH);
		
		return panel;
		
	}
	
	/**
	 * gets the list of dense check boxes
	 * 
	 * @param buttons - arraylist of buttons
	 * @return JScrollPane
	 */
	public JScrollPane getDenseButtonPane(ArrayList<JCheckBox> buttons) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		for (int i = 0; i < buttons.size(); i++) 
			pane.add(buttons.get(i));
		JScrollPane scrollPane = new JScrollPane(pane);
		return scrollPane;	
	}
	
	/**
	 * gets the list of grouped sparse radio buttons in a scrollable pane.
	 * 
	 * @param buttons - arraylist of buttons
	 * @return JScrollPane
	 */
	public JScrollPane getSparseButtonPane(ArrayList<JRadioButton> buttons) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		ButtonGroup group = new ButtonGroup(); 
		for (int i = 0; i < buttons.size(); i++) {
			group.add(buttons.get(i));
			pane.add(buttons.get(i));
		}
		JScrollPane scrollPane = new JScrollPane(pane);
		return scrollPane;	
	}
	
	public ArrayList<JCheckBox> getDenseColumnNames(String datatype) {
		ArrayList<JCheckBox> buttonsToCheck = new ArrayList<JCheckBox>();
		ArrayList<ArrayList<String>> namesAndTypes = 
			MainFrame.db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoDense);
		for (int i = 0; i < namesAndTypes.size(); i++) {
			if ((namesAndTypes.get(i).get(1).equals("INT") || 
					namesAndTypes.get(i).get(1).equals("REAL")) && 
					!namesAndTypes.get(i).get(0).equals("AtomID")) 
			buttonsToCheck.add(new JCheckBox(namesAndTypes.get(i).get(0) + 
					" : " + namesAndTypes.get(i).get(1)));
		}
		return buttonsToCheck;
	}
	
	public ArrayList<JRadioButton> getSparseColumnNames(String datatype) {
		ArrayList<JRadioButton> buttonsToCheck = new ArrayList<JRadioButton>();
		ArrayList<ArrayList<String>> namesAndTypes = 
			MainFrame.db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoSparse);
		for (int i = 0; i < namesAndTypes.size(); i++) {
			if ((namesAndTypes.get(i).get(1).equals("INT") || 
					namesAndTypes.get(i).get(1).equals("REAL")) && 
					!namesAndTypes.get(i).get(0).equals("AtomID")) 
			buttonsToCheck.add(new JRadioButton(namesAndTypes.get(i).get(0) + 
					" : " + namesAndTypes.get(i).get(1)));
		}
		return buttonsToCheck;
	}

	
	/**
	 * setCommonInfo() lays out the information that the two tabbed panels share;
	 * the name field, the OK button, the Advanced button, and the CANCEL button.  
	 * This method cuts back on redundant programming and makes the two panels look similar.
	 * @return - a JPanel with the text field and the bottons.
	 */
	public JPanel setCommonInfo(){
		JPanel commonInfo = new JPanel();
		//Create Name text field;
		JPanel comment = new JPanel();
		JLabel commentLabel = new JLabel("Comment: ");
		commentField = new JTextField(30);
		comment.add(commentLabel);
		comment.add(commentField);
		
		// Create the OK, Advanced and CANCEL buttons
		JPanel buttons = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		advancedButton = new JButton("Advanced...");
		advancedButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);
		buttons.add(advancedButton);
		
		//Add info to panel and lay out.
		commonInfo.add(comment);
		commonInfo.add(buttons);
		commonInfo.setLayout(new BoxLayout(commonInfo, 
				BoxLayout.Y_AXIS));
		
		return commonInfo;
	}	
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		DistanceMetric dMetInt = DistanceMetric.CITY_BLOCK;
		if (dMetric.equals(CITY_BLOCK) || 
				dMetric.equals(KMEDIANS))
		{
			dMetInt = DistanceMetric.CITY_BLOCK;
		}
		else if (dMetric.equals(EUCLIDEAN_SQUARED) || 
				dMetric.equals(KMEANS))
		{
			dMetInt = DistanceMetric.EUCLIDEAN_SQUARED;
		}
		else if (dMetric.equals(DOT_PRODUCT) || 
				dMetric.equals(SKMEANS))
		{
			dMetInt = DistanceMetric.DOT_PRODUCT;
		}
		if (source == okButton) {
			// TODO: error check here to make sure something is selected.
			// TODO: make this more graceful.
			// Get clustering specifications and create ClusterInformation object.
			
			//We now only use sparse - benzaids
			//String infoType = (String)infoTypeDropdown.getSelectedItem();
			String infoType = sparse;
			
			ArrayList<String> list = new ArrayList<String>();
			String key = null, weight = null;
			boolean auto = false;
			boolean norm = normalizer.isSelected();
			String denseTableName = db.getDynamicTableName(DynamicTable.AtomInfoDense, 
					cTree.getSelectedCollection().getDatatype());
			String sparseTableName = db.getDynamicTableName(DynamicTable.AtomInfoSparse, 
					cTree.getSelectedCollection().getDatatype());
			
			/*WE ONLY USE PEAKAREA NOW - benzaids
			Scanner scan;
			if (infoType.equals(dense)) {
				for (int i = 0; i < denseButtons.size(); i++)
					if (denseButtons.get(i).isSelected()) {
						scan = new Scanner(denseButtons.get(i).getText());
						list.add(denseTableName + "." + scan.next());
					}
				key = denseKey;
				auto = true;
			}
			else if (infoType.equals(sparse)) {
				for (int i = 0; i <sparseButtons.size(); i++)
					if (sparseButtons.get(i).isSelected()) {
						scan = new Scanner(sparseButtons.get(i).getText());
						list.add(sparseTableName + "." + scan.next());
						break;
					}
				key = sparseKey.get(0);
			}
			*/
			
			list.add(sparseTableName + ".PeakArea");//We only use PeakArea now (i = 1 in loop above) - benzaids
			key = sparseKey.get(0);//added this line outside of if statement - benzaids
			ClusterInformation cInfo = new ClusterInformation(list, key, weight, auto, norm);
			
			// Call the appropriate algorithm.
			if (currentShowing == ART2A)
			{
				try {
					System.out.println("Collection ID: " +
							cTree.getSelectedCollection().
							getCollectionID());
					
					// Get information from the dialogue box:
					float vig = Float.parseFloat(vigText.getText());
					float learn = Float.parseFloat(learnText.getText());
					int passes = Integer.parseInt(passesText.getText());
					
					// Check to make sure that these are valid params:
					if (vig < 0 || vig > 2 || learn < 0 || 
							learn > 1	|| passes <= 0) {
						JOptionPane.showMessageDialog(parent,
								"Error with parameters.\n" +
								"Appropriate values are:\n" +
								"0 <= vigilance <= 2\n" +
								"0 <= learning rate <= 1\n" +
								"number of passes > 0",
								"Number Format Exception",
								JOptionPane.ERROR_MESSAGE);
					}
					else {
						Art2A art2a = new Art2A(
								cTree.getSelectedCollection().
								getCollectionID(),db, 
								vig, learn, passes, dMetInt, 
								commentField.getText(), cInfo);
						
						art2a.setDistanceMetric(dMetInt);
						//TODO:  When should we use disk based and memory based 
						// cursors?
						if (db.getCollectionSize(
								cTree.getSelectedCollection().
								getCollectionID()) < 10000)
						{
							art2a.setCursorType(Cluster.STORE_ON_FIRST_PASS);
						}
						else
						{
							art2a.setCursorType(Cluster.DISK_BASED);
						}
						art2a.divide();
					}
					
				} catch (NullPointerException exception) {
					exception.printStackTrace();
					JOptionPane.showMessageDialog(parent,
							"Make sure you have selected a collection.",
							"Null Pointer Exception",
							JOptionPane.ERROR_MESSAGE);
				}
				catch (NumberFormatException exception) {
					exception.printStackTrace();
					JOptionPane.showMessageDialog(parent,
							"Make sure you have entered parameters.",
							"Number Format Exception",
							JOptionPane.ERROR_MESSAGE);
				}
				
				cTree.updateTree();
				dispose();
			}
			if (currentShowing == KCLUSTER)
			{
				
				try {
					System.out.println("Collection ID: " +
							cTree.getSelectedCollection().
							getCollectionID());
					
					// Get information from dialogue box:
					int k = Integer.parseInt(kClusterText.getText());
					
					// Check to make sure it's valid:
					if (k <= 0) {
						JOptionPane.showMessageDialog(parent,
								"Error with parameters.\n" +
								"Appropriate values are: k > 0",
								"Number Format Exception",
								JOptionPane.ERROR_MESSAGE);
					}
					else {
						if (dMetInt == DistanceMetric.CITY_BLOCK) {
							KMedians kMedians = new KMedians(
									cTree.getSelectedCollection().
									getCollectionID(),db, k, "", 
									commentField.getText(), refinedCentroids, cInfo);
							kMedians.addInfo(cInfo);
							kMedians.setDistanceMetric(dMetInt);
							if (db.getCollectionSize(
									cTree.getSelectedCollection().
									getCollectionID()) < 10000)
							{
								kMedians.setCursorType(Cluster.STORE_ON_FIRST_PASS);
							}
							else
							{
								kMedians.setCursorType(Cluster.DISK_BASED);
							}

							kMedians.divide();
							dispose();
						}
						else if (dMetInt == DistanceMetric.EUCLIDEAN_SQUARED ||
						         dMetInt == DistanceMetric.DOT_PRODUCT) {
							KMeans kMeans = new KMeans(
									cTree.getSelectedCollection().
									getCollectionID(),db, 
									Integer.parseInt(kClusterText.getText()), 
									"", commentField.getText(), refinedCentroids, cInfo);
							kMeans.addInfo(cInfo);
							kMeans.setDistanceMetric(dMetInt);
							//TODO:  When should we use disk based and memory based 
							// cursors?
							if (db.getCollectionSize(
									cTree.getSelectedCollection().
									getCollectionID()) < 10000)
							{
								kMeans.setCursorType(Cluster.STORE_ON_FIRST_PASS);
							}
							else
							{
								kMeans.setCursorType(Cluster.DISK_BASED);
							}

							kMeans.divide();
							dispose();
						}
					}
				} catch (NullPointerException exception) {
					exception.printStackTrace();
					JOptionPane.showMessageDialog(parent,
							"Make sure you have selected a collection.",
							"Null Pointer Exception",
							JOptionPane.ERROR_MESSAGE);
				}
				catch (NumberFormatException exception) {
					exception.printStackTrace();
					JOptionPane.showMessageDialog(parent,
							"Make sure you have entered parameters.",
							"Number Format Exception",
							JOptionPane.ERROR_MESSAGE);
				}
				cTree.updateTree();
			}
			
			if (currentShowing == OTHER) 
			{
				JOptionPane.showMessageDialog(parent,
						"This is not an algorithm.\n" +
						"Please choose another one.",
						"Not Implemented Yet",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		if (source == advancedButton) {
			new AdvancedClusterDialog((JDialog)this);
		}
		else  
			dispose();
	}
	
	/**
	 * itemStateChanged(ItemEvent evt) needs to be defined, as the 
	 * ClusterDialog implements ItemListener.
	 */
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() == clusterDropDown)
		{
			CardLayout cl = (CardLayout)(algorithmCards.getLayout());
			String newEvent = (String)evt.getItem();
			cl.show(algorithmCards, newEvent);
			if (newEvent.equals(KCLUSTER))
				dMetric = KMEANS;
			if (newEvent.equals(ART2A))
				dMetric = CITY_BLOCK;
			currentShowing = newEvent;
		}
		else if (evt.getSource() == metricDropDown)
		{
			dMetric = (String)evt.getItem();
		}
		else if (evt.getSource() == averageClusterDropDown)
		{
			dMetric = (String)evt.getItem();
		}
		else if (evt.getSource() == refineCentroids)
		{
			refinedCentroids = !refinedCentroids;
		}
		else if (evt.getSource() == infoTypeDropdown) {
			CardLayout cl = (CardLayout)(specificationCards.getLayout());
			String newEvent = (String)evt.getItem();
			if (newEvent.equals(init) || newEvent.equals(dense)) {
				for (int i = 0; i < sparseButtons.size(); i++)
					sparseButtons.get(i).setSelected(false);
			}
			else if (newEvent.equals(init) || newEvent.equals(sparse)) {
				for (int i = 0; i < denseButtons.size(); i++)
					denseButtons.get(i).setSelected(false);
			}
			cl.show(specificationCards, newEvent);
		}
	}
}
