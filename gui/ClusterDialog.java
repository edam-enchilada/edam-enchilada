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
import javax.swing.border.Border;

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
 * @author ritza
 * 
 * The ClusterDialog opens a JDialog Object where the user can choose
 * a clustering algorithm to perform on the selected collection.  The user
 * inputs the parameters for each algorithm.  The user can cluster using only one 
 * algorithm at a time.  When OK is clicked, a new collection is created with the
 * specified name, and the clusters are created as sub-collections of the new 
 * collection.
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
	private JComboBox clusterDropDown, metricDropDown, averageClusterDropDown, infoTypeDropdown, denseKeyBox, sparseKeyBox;
	private ArrayList<JRadioButton> denseButtons, sparseButtons, weightButtons;

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
		//Set window settings.
		setSize(550, 750);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		int fontSize = 16;
		JLabel step1 = new JLabel(" 1.  Select Clustering Specifications ");
		step1.setFont(new Font(step1.getFont().getName(), step1.getFont().getStyle(), fontSize));
		step1.setBorder(BorderFactory.createEtchedBorder());
		JLabel step2 = new JLabel(" 2.  Choose Appropriate Clustering Algorithm ");
		step2.setFont(new Font(step2.getFont().getName(), step2.getFont().getStyle(), fontSize));
		step2.setBorder(BorderFactory.createEtchedBorder());
		JLabel step3 = new JLabel(" 3.  Begin Clustering ");
		step3.setFont(new Font(step3.getFont().getName(), step3.getFont().getStyle(), fontSize));
		step3.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel clusterAlgorithms = setClusteringAlgorithms();
		JPanel clusterSpecs = setClusteringSpecifications();
		
		//Create common info panel:
		JPanel commonInfo = setCommonInfo();
		getRootPane().setDefaultButton(okButton);
		
		add(step1);
		add(clusterSpecs);
		add(step2);
		add(clusterAlgorithms);
		add(step3);
		add(commonInfo);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		//Display the dialogue box.
		setVisible(true);
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

		JPanel headerAndDropDown = new JPanel();
		headerAndDropDown.add(header);
		headerAndDropDown.add(dropDown);
		headerAndDropDown.add(normalizer);
		
		JLabel dividingLine = 
			new JLabel("------------------------------------------" +
			"---------------------------------------------------");
		
		//Create the art2a panel that will show when "Art2a" is selected.
		JPanel parameters = new JPanel();
		parameters.setSize(400,200);
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
		parameters.setSize(400,200);
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
		mainPanel.setPreferredSize(new Dimension(500,160));
		mainPanel.add(headerAndDropDown);
		mainPanel.add(dividingLine);
		mainPanel.add(algorithmCards);
		mainPanel.setLayout(new FlowLayout());
		return mainPanel;
	}
	
	/**
	 * creates the panel that displays the clustering specifications
	 * @return JPanel
	 */
	public JPanel setClusteringSpecifications() {
		// Set button arraylists
		denseButtons = getColumnNames(cTree.getSelectedCollection().getDatatype(), DynamicTable.AtomInfoDense);
		sparseButtons = getColumnNames(cTree.getSelectedCollection().getDatatype(),DynamicTable.AtomInfoSparse);
		weightButtons = getColumnNames(cTree.getSelectedCollection().getDatatype(),DynamicTable.AtomInfoDense);
		weightButtons.add(0, new JRadioButton("None"));

		// Set dropdown for dense and sparse information
		String[] infoNames = {init, dense, sparse};
		infoTypeDropdown = new JComboBox(infoNames);
		infoTypeDropdown.addItemListener(this);
		
		// Set dense panel
		JPanel densePanel = new JPanel();
		SpringLayout denseLayout = new SpringLayout();
		densePanel.setLayout(denseLayout);	
		densePanel.setPreferredSize(new Dimension(250,300));
		String[] denseNames = {denseKey};
		denseKeyBox = new JComboBox(denseNames);
		densePanel.add(denseKeyBox);
		JLabel denseChoose = new JLabel("Choose one or more values below:");
		densePanel.add(denseChoose);
		denseLayout.putConstraint(SpringLayout.WEST, denseKeyBox, 10, SpringLayout.WEST, densePanel);
		denseLayout.putConstraint(SpringLayout.NORTH, denseKeyBox, 10, SpringLayout.NORTH, densePanel);
		denseLayout.putConstraint(SpringLayout.WEST, denseChoose, 20, SpringLayout.WEST, densePanel);
		denseLayout.putConstraint(SpringLayout.NORTH, denseChoose, 10, SpringLayout.SOUTH, denseKeyBox);
		JScrollPane denseButtonPane = getButtonPane(denseButtons, false);
		densePanel.add(denseButtonPane);
		denseLayout.putConstraint(SpringLayout.WEST, denseButtonPane, 10, SpringLayout.WEST, densePanel);
		denseLayout.putConstraint(SpringLayout.NORTH, denseButtonPane, 20, SpringLayout.SOUTH, denseChoose);

		// set sparse panel
		JPanel sparsePanel = new JPanel();
		SpringLayout sparseLayout = new SpringLayout();
		sparsePanel.setLayout(sparseLayout);	
		sparsePanel.setPreferredSize(new Dimension(250,300));
		String[] boxNames = new String[sparseButtons.size()];
		for (int j = 0; j < sparseButtons.size(); j++)
			boxNames[j] = "Key = " + sparseButtons.get(j).getText();
		sparseKeyBox = new JComboBox(boxNames);
		sparsePanel.add(sparseKeyBox);
		JLabel sparseChoose = new JLabel("Choose one value below:");
		sparsePanel.add(sparseChoose);
		sparseLayout.putConstraint(SpringLayout.WEST, sparseKeyBox, 10, SpringLayout.WEST, sparsePanel);
		sparseLayout.putConstraint(SpringLayout.NORTH, sparseKeyBox, 10, SpringLayout.NORTH, sparsePanel);
		sparseLayout.putConstraint(SpringLayout.WEST, sparseChoose, 20, SpringLayout.WEST, sparsePanel);
		sparseLayout.putConstraint(SpringLayout.NORTH, sparseChoose, 10, SpringLayout.SOUTH, sparseKeyBox);
		JScrollPane sparseButtonPane = getButtonPane(sparseButtons, true);
		sparsePanel.add(sparseButtonPane);
		sparseLayout.putConstraint(SpringLayout.WEST, sparseButtonPane, 10, SpringLayout.WEST, sparsePanel);
		sparseLayout.putConstraint(SpringLayout.NORTH, sparseButtonPane, 20, SpringLayout.SOUTH, sparseChoose);
		
		// set weight panel
		JPanel weightPanel = new JPanel();
		SpringLayout weightLayout = new SpringLayout();
		weightPanel.setLayout(weightLayout);	
		weightPanel.setPreferredSize(new Dimension(210,300));
		JLabel weightChoose = new JLabel("Choose a weight:");
		weightPanel.add(weightChoose);
		weightLayout.putConstraint(SpringLayout.WEST, weightChoose, 0, SpringLayout.WEST, weightPanel);
		weightLayout.putConstraint(SpringLayout.NORTH, weightChoose, 47, SpringLayout.NORTH, weightPanel);
		JScrollPane weightButtonPane = getButtonPane(weightButtons, true);
		weightButtons.get(0).setSelected(true);
		weightPanel.add(weightButtonPane);
		weightLayout.putConstraint(SpringLayout.WEST, weightButtonPane, 0, SpringLayout.WEST, weightPanel);
		weightLayout.putConstraint(SpringLayout.NORTH, weightButtonPane, 20, SpringLayout.SOUTH, weightChoose);
		
		// Add dense and sparse panels to cards
		specificationCards = new JPanel(new CardLayout());
		specificationCards.add(new JPanel(), init);
		specificationCards.add(densePanel, dense);
		specificationCards.add(sparsePanel, sparse);
		
		// add cards to final panel
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.setPreferredSize(new Dimension(500, 340));
		panel.add(new JLabel("Choose Type of Particle Information to Cluster on: "));
		panel.add(infoTypeDropdown);
		panel.add(specificationCards);
		panel.add(weightPanel);
	
		return panel;
		
	}
	
	/**
	 * gets the list of radio buttons in a scrollable pane; can be grouped
	 * or ungrouped.
	 * 
	 * @param buttons - arraylist of buttons
	 * @param grouped - true if grouped, false otherwise
	 * @return JScrollPane
	 */
	public JScrollPane getButtonPane(ArrayList<JRadioButton> buttons, boolean grouped) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		ButtonGroup group = new ButtonGroup();
		if (grouped) 
			for (int i = 0; i < buttons.size(); i++) 
				group.add(buttons.get(i));
		for (int i = 0; i < buttons.size(); i++) 
			pane.add(buttons.get(i));
		JScrollPane scrollPane = new JScrollPane(pane);
		scrollPane.setPreferredSize(new Dimension(200, 200));
		return scrollPane;	
	}
	
	public ArrayList<JRadioButton> getColumnNames(String datatype, DynamicTable table) {
		ArrayList<JRadioButton> buttonsToCheck = new ArrayList<JRadioButton>();
		ArrayList<ArrayList<String>> namesAndTypes = 
			MainFrame.db.getColNamesAndTypes(datatype, table);
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
		commonInfo.setPreferredSize(new Dimension(500, 100));
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
			String infoType = (String)infoTypeDropdown.getSelectedItem();
			ArrayList<String> list = new ArrayList<String>();
			String key = null, weight = null;
			boolean auto = false;
			boolean norm = normalizer.isSelected();
			String denseTableName = db.getDynamicTableName(DynamicTable.AtomInfoDense, 
					cTree.getSelectedCollection().getDatatype());
			String sparseTableName = db.getDynamicTableName(DynamicTable.AtomInfoSparse, 
					cTree.getSelectedCollection().getDatatype());
			Scanner scan;
			for (int i = 0; i < weightButtons.size(); i++)
				if (weightButtons.get(i).isSelected()) {
					scan = new Scanner(weightButtons.get(i).getText());
					weight = scan.next();
					if (weight.equals("None"))
						weight = null;
					else weight = denseTableName + "." + weight;
					break;
				}
			if (infoType.equals(dense)) {
				for (int i = 0; i < denseButtons.size(); i++)
					if (denseButtons.get(i).isSelected()) {
						scan = new Scanner(denseButtons.get(i).getText());
						list.add(denseTableName + "." + scan.next());
					}
				key = (String)denseKeyBox.getSelectedItem();
				if (key.equals(denseKey))
					auto = true;
				else {
					scan = new Scanner(key);
					scan.next();
					scan.next();
					key = scan.next();
					auto = false;
				}
			}
			else if (infoType.equals(sparse)) {
				for (int i = 0; i <sparseButtons.size(); i++)
					if (sparseButtons.get(i).isSelected()) {
						scan = new Scanner(sparseButtons.get(i).getText());
						list.add(sparseTableName + "." + scan.next());
						break;
					}
				scan = new Scanner((String)sparseKeyBox.getSelectedItem());
				scan.next();
				scan.next();
				key = scan.next();
				// TODO: iffy here, prone to bugs.
				auto = false;
			}
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
