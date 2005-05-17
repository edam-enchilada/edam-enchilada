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

import database.InfoWarehouse;

import analysis.DistanceMetric;
import analysis.clustering.*;
import analysis.clustering.Cluster;

import java.awt.*;
import java.awt.event.*;

/**
 * @author ritza
 * 
 * The ClusterDialog opens a JDialog Object where the user can choose
 * a clustering algorithm to perform on the selected collection.  The user
 * inputs the parameters for each algorithm.  The user can cluster using only one 
 * algorithm at a time.  When OK is clicked, a new collection is created with the
 * specified name, and the clusters are created as sub-collections of the new 
 * collection.
 * 
 * The window is currently modal - don't know if we want to keep it that way.
 *
 */
public class ClusterDialog extends JDialog implements ItemListener, ActionListener 
{
	
	/* Declared variables */
	private JFrame parent;
	
	private JPanel cards; // the panel that uses CardLayout to change user input.
	private JButton okButton; //Default button
	private JButton cancelButton;
	private JButton advancedButton;
	
	// General clustering fields
	private InfoWarehouse db;
	private JTextField commentField;
	
	// Art2a fields
	private JTextField passesText;
	private JTextField vigText;
	private JTextField learnText;
	
	//KCluster field
	private JTextField kClusterText;
	private JCheckBox refineCentroids;
	
	//Other field
	private JTextField otherText;
	
	private CollectionTree cTree;
	
	// Drop down menu labels:
	final static String ART2A = "Art2a";
	final static String KCLUSTER = "K-Cluster";
	final static String KMEANS = "K-Means / Euclidean Squared";
	final static String KMEDIANS = "K-Medians / City Block";
	final static String SKMEANS = "K-Means / Dot Product";
	final static String OTHER = "Other";
	final static String CITY_BLOCK = "City Block";
	final static String EUCLIDEAN_SQUARED = "Euclidean Squared";
	final static String DOT_PRODUCT = "Dot Product";
	private String dMetric = CITY_BLOCK;
	private boolean refinedCentroids = true;
	private String currentShowing = ART2A;
	private JComboBox clusterDropDown;
	private JComboBox metricDropDown;
	private JComboBox averageClusterDropDown;
	
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
		//setDefaultLookAndFeelDecorated(true);
		setSize(480, 275);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JLabel header = new JLabel("Cluster using: ");
		
		//Create the drop down menu and the dividing line.
		String[] clusterNames = {ART2A, KCLUSTER, OTHER};
		JPanel dropDown = new JPanel();
		clusterDropDown = new JComboBox(clusterNames);
		clusterDropDown.setEditable(false);
		clusterDropDown.addItemListener(this);
		dropDown.add(clusterDropDown);
		
		JPanel headerAndDropDown = new JPanel();
		headerAndDropDown.add(header);
		headerAndDropDown.add(dropDown);
		
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
		refineCentroids = new JCheckBox("Refine Centroids", true);
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
		
		// Add the previous three panels to the cards JPanel using CardLayout.
		cards = new JPanel (new CardLayout());
		cards.add(art2aCard, ART2A);
		cards.add(kClusterCard,KCLUSTER);
		cards.add(otherCard, OTHER);
		
		//CardLayout cLayout = (CardLayout) cards.getLayout();
		
		//Create common info panel:
		JPanel commonInfo = setCommonInfo();
		getRootPane().setDefaultButton(okButton);
		
		// Add all of the components to the main panel.
		add(headerAndDropDown);
		add(dividingLine);
		add(cards);
		add(commonInfo);
		setLayout(new FlowLayout());
		
		//Display the dialogue box.
		setVisible(true);
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
								commentField.getText());
						
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
									commentField.getText(), refinedCentroids);
							
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
									"", commentField.getText(), refinedCentroids);
							
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
			//TODO:  Once AdvancedClusterDialog is in place, uncomment the line.
			//new AdvancedClusterDialog((JDialog)this);
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
			CardLayout cl = (CardLayout)(cards.getLayout());
			String newEvent = (String)evt.getItem();
			cl.show(cards, newEvent);
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
	}
}
