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
public class PreClusterDialog extends ClusterDialog 
{
	
	/**
	 * Constructor.  Creates and shows the dialogue box.
	 * 
	 * @param frame - the parent JDialog of the JDialog object.
	 */
	public PreClusterDialog(JFrame frame, JDialog parent, CollectionTree cTree, 
			InfoWarehouse db) {
		super(frame,cTree,db);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel rootPanel = new JPanel();
		
		String[] clusterNames = {ART2A, KCLUSTER};
		JPanel clusterAlgorithms = setClusteringAlgorithms(clusterNames);
		
		pack();
		
		//Display the dialogue box.
		setVisible(true);
	}
	
//	public void actionPerformed(ActionEvent e) {
//		Object source = e.getSource();
//		DistanceMetric dMetInt = DistanceMetric.CITY_BLOCK;
//		if (dMetric.equals(CITY_BLOCK) || 
//				dMetric.equals(KMEDIANS))
//		{
//			dMetInt = DistanceMetric.CITY_BLOCK;
//		}
//		else if (dMetric.equals(EUCLIDEAN_SQUARED) || 
//				dMetric.equals(KMEANS) || dMetric.equals(HIER_WARDS))
//		{
//			dMetInt = DistanceMetric.EUCLIDEAN_SQUARED;
//		}
//		else if (dMetric.equals(DOT_PRODUCT) || 
//				dMetric.equals(SKMEANS))
//		{
//			dMetInt = DistanceMetric.DOT_PRODUCT;
//		}
//		if (source == okButton) {
//			// TODO: error check here to make sure something is selected.
//			// TODO: make this more graceful.
//			// Get clustering specifications and create ClusterInformation object.
//			
//			//We now only use sparse - benzaids
//			//String infoType = (String)infoTypeDropdown.getSelectedItem();
//			String infoType = sparse;
//			
//			ArrayList<String> list = new ArrayList<String>();
//			String key = null, weight = null;
//			boolean auto = false;
//			boolean norm = normalizer.isSelected();
//			String denseTableName = db.getDynamicTableName(DynamicTable.AtomInfoDense, 
//					cTree.getSelectedCollection().getDatatype());
//			String sparseTableName = db.getDynamicTableName(DynamicTable.AtomInfoSparse, 
//					cTree.getSelectedCollection().getDatatype());
//			
//			/*WE ONLY USE PEAKAREA NOW - benzaids
//			Scanner scan;
//			if (infoType.equals(dense)) {
//				for (int i = 0; i < denseButtons.size(); i++)
//					if (denseButtons.get(i).isSelected()) {
//						scan = new Scanner(denseButtons.get(i).getText());
//						list.add(denseTableName + "." + scan.next());
//					}
//				key = denseKey;
//				auto = true;
//			}
//			else if (infoType.equals(sparse)) {
//				for (int i = 0; i <sparseButtons.size(); i++)
//					if (sparseButtons.get(i).isSelected()) {
//						scan = new Scanner(sparseButtons.get(i).getText());
//						list.add(sparseTableName + "." + scan.next());
//						break;
//					}
//				key = sparseKey.get(0);
//			}
//			*/
//			
//			list.add(sparseTableName + ".PeakArea");//We only use PeakArea now (i = 1 in loop above) - benzaids
//			key = sparseKey.get(0);//added this line outside of if statement - benzaids
//			ClusterInformation cInfo = new ClusterInformation(list, key, weight, auto, norm);
//			
//			// Call the appropriate algorithm.
//			if (currentShowing == ART2A)
//			{
//				try {
//					System.out.println("Collection ID: " +
//							cTree.getSelectedCollection().
//							getCollectionID());
//					
//					// Get information from the dialogue box:
//					float vig = Float.parseFloat(vigText.getText());
//					float learn = Float.parseFloat(learnText.getText());
//					int passes = Integer.parseInt(passesText.getText());
//					
//					// Check to make sure that these are valid params:
//					if (vig < 0 || vig > 2 || learn < 0 || 
//							learn > 1	|| passes <= 0) {
//						JOptionPane.showMessageDialog(parent,
//								"Error with parameters.\n" +
//								"Appropriate values are:\n" +
//								"0 <= vigilance <= 2\n" +
//								"0 <= learning rate <= 1\n" +
//								"number of passes > 0",
//								"Number Format Exception",
//								JOptionPane.ERROR_MESSAGE);
//					}
//					else {
//						Art2A art2a = new Art2A(
//								cTree.getSelectedCollection().
//								getCollectionID(),db, 
//								vig, learn, passes, dMetInt, 
//								commentField.getText(), cInfo);
//						
//						art2a.setDistanceMetric(dMetInt);
//						//TODO:  When should we use disk based and memory based 
//						// cursors?
//						if (db.getCollectionSize(
//								cTree.getSelectedCollection().
//								getCollectionID()) < 10000)
//						{
//							art2a.setCursorType(Cluster.STORE_ON_FIRST_PASS);
//						}
//						else
//						{
//							art2a.setCursorType(Cluster.DISK_BASED);
//						}
//						art2a.divide();
//					}
//					
//				} catch (NullPointerException exception) {
//					exception.printStackTrace();
//					JOptionPane.showMessageDialog(parent,
//							"Make sure you have selected a collection.",
//							"Null Pointer Exception",
//							JOptionPane.ERROR_MESSAGE);
//				}
//				catch (NumberFormatException exception) {
//					exception.printStackTrace();
//					JOptionPane.showMessageDialog(parent,
//							"Make sure you have entered parameters.",
//							"Number Format Exception",
//							JOptionPane.ERROR_MESSAGE);
//				}
//				
//				cTree.updateTree();
//				dispose();
//			}
//			if (currentShowing == KCLUSTER)
//			{
//				
//				try {
//					System.out.println("Collection ID: " +
//							cTree.getSelectedCollection().
//							getCollectionID());
//					
//					// Get information from dialogue box:
//					int k = Integer.parseInt(kClusterText.getText());
//					
//					// Check to make sure it's valid:
//					if (k <= 0) {
//						JOptionPane.showMessageDialog(parent,
//								"Error with parameters.\n" +
//								"Appropriate values are: k > 0",
//								"Number Format Exception",
//								JOptionPane.ERROR_MESSAGE);
//					}
//					else {
//						if (dMetInt == DistanceMetric.CITY_BLOCK) {
//							KMedians kMedians = new KMedians(
//									cTree.getSelectedCollection().
//									getCollectionID(),db, k, "", 
//									commentField.getText(), refinedCentroids, cInfo);
//							kMedians.addInfo(cInfo);
//							kMedians.setDistanceMetric(dMetInt);
//							if (db.getCollectionSize(
//									cTree.getSelectedCollection().
//									getCollectionID()) < 0) // WAS 10000
//							{
//								kMedians.setCursorType(Cluster.STORE_ON_FIRST_PASS);
//							}
//							else
//							{
//								kMedians.setCursorType(Cluster.DISK_BASED);
//							}
//
//							kMedians.divide();
//							dispose();
//						}
//						else if (dMetInt == DistanceMetric.EUCLIDEAN_SQUARED ||
//						         dMetInt == DistanceMetric.DOT_PRODUCT) {
//							KMeans kMeans = new KMeans(
//									cTree.getSelectedCollection().
//									getCollectionID(),db, 
//									Integer.parseInt(kClusterText.getText()), 
//									"", commentField.getText(), refinedCentroids, cInfo);
//							kMeans.addInfo(cInfo);
//							kMeans.setDistanceMetric(dMetInt);
//							//TODO:  When should we use disk based and memory based 
//							// cursors?
//							if (db.getCollectionSize(
//									cTree.getSelectedCollection().
//									getCollectionID()) < 10000) // Was 10000
//							{
//								kMeans.setCursorType(Cluster.STORE_ON_FIRST_PASS);
//							}
//							else
//							{
//								kMeans.setCursorType(Cluster.DISK_BASED);
//							}
//
//							kMeans.divide();
//							dispose();
//						}
//					}
//				} catch (NullPointerException exception) {
//					exception.printStackTrace();
//					JOptionPane.showMessageDialog(parent,
//							"Make sure you have selected a collection.",
//							"Null Pointer Exception",
//							JOptionPane.ERROR_MESSAGE);
//				}
//				catch (NumberFormatException exception) {
//					exception.printStackTrace();
//					JOptionPane.showMessageDialog(parent,
//							"Make sure you have entered parameters.",
//							"Number Format Exception",
//							JOptionPane.ERROR_MESSAGE);
//				}
//				cTree.updateTree();
//			}
//			if (currentShowing == HIERARCHICAL)
//			{
//				
//				try {
//					System.out.println("Collection ID: " +
//							cTree.getSelectedCollection().
//							getCollectionID());
//					
//					// Get information from dialogue box:
//					int k = Integer.parseInt(hClusterText.getText());
//					
//					// Check to make sure it's valid:
//					if (k <= 0) {
//						JOptionPane.showMessageDialog(parent,
//								"Error with parameters.\n" +
//								"Appropriate values are: k > 0",
//								"Number Format Exception",
//								JOptionPane.ERROR_MESSAGE);
//					}
//					else {
//						if (dMetInt == DistanceMetric.EUCLIDEAN_SQUARED ||
//						         dMetInt == DistanceMetric.DOT_PRODUCT) {
//							ClusterHierarchical hCluster = new ClusterHierarchical(
//									cTree.getSelectedCollection().
//									getCollectionID(),db, 
//									Integer.parseInt(hClusterText.getText()), 
//									"", commentField.getText(), cInfo);
//							hCluster.addInfo(cInfo);
//							hCluster.setDistanceMetric(dMetInt);
//							//TODO:  When should we use disk based and memory based 
//							// cursors?
//							if (db.getCollectionSize(
//									cTree.getSelectedCollection().
//									getCollectionID()) < 10000) // Was 10000
//							{
//								hCluster.setCursorType(Cluster.STORE_ON_FIRST_PASS);
//							}
//							else
//							{
//								hCluster.setCursorType(Cluster.DISK_BASED);
//							}
//
//							hCluster.divide();
//							dispose();
//						}
//					}
//				} catch (NullPointerException exception) {
//					exception.printStackTrace();
//					JOptionPane.showMessageDialog(parent,
//							"Make sure you have selected a collection.",
//							"Null Pointer Exception",
//							JOptionPane.ERROR_MESSAGE);
//				}
//				catch (NumberFormatException exception) {
//					exception.printStackTrace();
//					JOptionPane.showMessageDialog(parent,
//							"Make sure you have entered parameters.",
//							"Number Format Exception",
//							JOptionPane.ERROR_MESSAGE);
//				}
//				cTree.updateTree();
//			}
//			if (currentShowing == OTHER) 
//			{
//				JOptionPane.showMessageDialog(parent,
//						"This is not an algorithm.\n" +
//						"Please choose another one.",
//						"Not Implemented Yet",
//						JOptionPane.ERROR_MESSAGE);
//			}
//		}
//		if (source == preClusterCheckBox) {
//			preClusterSettings.setEnabled(preClusterCheckBox.isSelected());
//		}
//		else if (source == preClusterSettings) {
//			new PreClusterSettings(this);
//		}
//		else if (source == advancedButton) {
//			new AdvancedClusterDialog((JDialog)this);
//		}
//		else  
//			dispose();
//		
//		db.clearCache();
//	}
//	
//	/**
//	 * itemStateChanged(ItemEvent evt) needs to be defined, as the 
//	 * ClusterDialog implements ItemListener.
//	 */
//	public void itemStateChanged(ItemEvent evt) {
//		if (evt.getSource() == clusterDropDown)
//		{
//			CardLayout cl = (CardLayout)(algorithmCards.getLayout());
//			String newEvent = (String)evt.getItem();
//			cl.show(algorithmCards, newEvent);
//			if (newEvent.equals(KCLUSTER))
//				dMetric = KMEANS;
//			if (newEvent.equals(ART2A))
//				dMetric = CITY_BLOCK;
//			if (newEvent.equals(HIERARCHICAL))
//				dMetric = HIER_WARDS;
//			currentShowing = newEvent;
//		}
//		else if (evt.getSource() == metricDropDown)
//		{
//			dMetric = (String)evt.getItem();
//		}
//		else if (evt.getSource() == averageClusterDropDown)
//		{
//			dMetric = (String)evt.getItem();
//		}
//		else if (evt.getSource() == refineCentroids)
//		{
//			refinedCentroids = !refinedCentroids;
//		}
//		else if (evt.getSource() == infoTypeDropdown) {
//			CardLayout cl = (CardLayout)(specificationCards.getLayout());
//			String newEvent = (String)evt.getItem();
//			if (newEvent.equals(init) || newEvent.equals(dense)) {
//				for (int i = 0; i < sparseButtons.size(); i++)
//					sparseButtons.get(i).setSelected(false);
//			}
//			else if (newEvent.equals(init) || newEvent.equals(sparse)) {
//				for (int i = 0; i < denseButtons.size(); i++)
//					denseButtons.get(i).setSelected(false);
//			}
//			cl.show(specificationCards, newEvent);
//		}
//	}
}
