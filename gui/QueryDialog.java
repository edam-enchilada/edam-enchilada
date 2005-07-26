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
 * The Original Code is EDAM Enchilada's QueryDialog class.
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

import analysis.CollectionDivider;
import analysis.SQLDivider;

import database.InfoWarehouse;

import java.awt.BorderLayout;
import java.awt.event.*;

/**
 * @author ritza
 *
 * QueryDialog opens a dialogue window that allows the user to 
 * query a selected collection.  There are two tabs to this window, a 
 * Basics tab and an Advanced tab.  The Basics query includes the parameters
 * time, size, and count.  The Advanced query allows the user to choose certain 
 * compounds to query, in addition to defining their own SQL query.  There is also 
 * a check box to include the basic query parameters as well - unlike the cluster
 * dialogue box, you can query based on as many parameters as you want.  
 * 
 * I'm thinking that the user enters the name once, and when they tab back and forth
 * that name is remembered, staying the same as they switch between windows.
 * 
 * There is a problem with having two different sets of buttons for the two tabbed panes -
 * might need to fix this later.
 * 
 */
public class QueryDialog extends JDialog 
implements ActionListener, ItemListener
{
	private CollectionTree cTree;
	private InfoWarehouse db;
	
	private JButton okButton; //Default button
	private JButton okButton2;
	private JButton cancelButton;
	private JCheckBox timeButton;
	private JCheckBox sizeButton;
	private JCheckBox countButton;

	private JComboBox fromMonth;
	private JComboBox fromDay;
	private JComboBox fromYear;
	private JComboBox fromHour;
	private JComboBox fromMinute;
	private JComboBox fromSecond;
	private JComboBox fromAMPM;
	
	private JComboBox toMonth;
	private JComboBox toDay;
	private JComboBox toYear;
	private JComboBox toHour;
	private JComboBox toMinute;
	private JComboBox toSecond;
	private JComboBox toAMPM;
	
	private JTextField fromSize;
	private JTextField toSize;
	private JTextField fromCount;
	private JTextField toCount;
	private boolean sizeSelected = false;
	private boolean timeSelected = false;
	private boolean countSelected = false;
	private JTabbedPane tabbedPane;
	private JTextField nameField;
	private JTextField commentField;
	private JPanel commonInfo;
	
	/**
	 * Constructor.  Creates a tabbed pane that is added to
	 * a JDialog Object.  The constructor also shows the GUI.
	 * @param frame - the parent of the JDialog object.
	 */
	public QueryDialog(JFrame frame, CollectionTree cTree,
			InfoWarehouse db) 
	{
		super(frame, "Query", true);
		//Make sure we have nice window decorations.
		setSize(510,450);
		
		this.cTree = cTree;
		this.db = db;
		
		// Create the two panels for the dialogue box.
		JPanel basic = basicQuery();
		JPanel advanced = advancedQuery();

		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Basic",null,basic,null);
		getRootPane().setDefaultButton(okButton);
		//tabbedPane.addTab("Advanced",null,advanced,null);
		tabbedPane.setSize(350,400);
		
		add(tabbedPane); // Add the tabbed pane to the dialogue box.
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		//Display the dialogue box.
		setVisible(true);
	}
	
	/**
	 * basicQuery displays the basic parameters for entering a query - 
	 * time, size, and count.
	 * @return - the JPanel with the basic query information on it.
	 */
	public JPanel basicQuery() 
	{
		JPanel p = new JPanel();
		
		timeButton = new JCheckBox("Time:");
		timeButton.addItemListener(this);
		JLabel timeLabel = new JLabel("mm/dd/yyyy hh:mm:ss AM/PM");
		sizeButton = new JCheckBox("Size:");
		sizeButton.addItemListener(this);
		JLabel sizeLabel = new JLabel("microns");
		countButton = new JCheckBox("Count:");
		countButton.addItemListener(this);
		//Create the user-input parameter panels:
		JPanel timeParamFromPanel = new JPanel();
		JPanel timeParamToPanel = new JPanel();

		//fromTime = new JTextField(10);
		String[] months = { 
				"01", "02", "03", "04", "05", "06","07","08","09",
				"10","11","12"};
		fromMonth = new JComboBox(months);
		toMonth = new JComboBox(months);
		
		String[] days = {
				"01", "02", "03", "04", "05", "06","07","08","09",
				"10","11","12","13","14","15","16","17","18","19",
				"20","21","22","23","24","25","26","27","28","29",
				"30","31"};
		fromDay = new JComboBox(days);
		toDay = new JComboBox(days);
		String[] years = new String[50];
		for (int i = 0; i < 50; i++)
			years[i] = Integer.toString(i+1990);
		fromYear = new JComboBox(years);
		toYear = new JComboBox(years);
		
		fromHour = new JComboBox(months);
		toHour = new JComboBox(months);
		
		String[] minutes = new String[60];
		for (int i = 0; i < 60; i++)
		{
			minutes[i] = Integer.toString(i+1);
			if (i + 1 < 10)
			{
				minutes[i] = "0" + minutes[i];
			}
		}
		fromMinute = new JComboBox(minutes);
		toMinute = new JComboBox(minutes);
		
		fromSecond = new JComboBox(minutes);
		toSecond = new JComboBox(minutes);
		
		String[] AMPM = { "AM", "PM" };
		fromAMPM = new JComboBox(AMPM);
		toAMPM = new JComboBox(AMPM);
		
		//toTime = new JTextField(10);
		
		timeParamFromPanel.add(fromMonth);
		timeParamFromPanel.add(new JLabel("/"));
		timeParamFromPanel.add(fromDay);
		timeParamFromPanel.add(new JLabel("/"));
		timeParamFromPanel.add(fromYear);
		
		timeParamFromPanel.add(new JLabel(" "));
		
		timeParamFromPanel.add(fromHour);
		timeParamFromPanel.add(new JLabel(":"));
		timeParamFromPanel.add(fromMinute);
		timeParamFromPanel.add(new JLabel(":"));
		timeParamFromPanel.add(fromSecond);
		
		timeParamFromPanel.add(new JLabel(" "));
		
		timeParamFromPanel.add(fromAMPM);
		
		
		
		timeParamFromPanel.add(new JLabel(" to "));
		
		timeParamToPanel.add(toMonth, BorderLayout.SOUTH);
		timeParamToPanel.add(new JLabel("/"),BorderLayout.SOUTH);
		timeParamToPanel.add(toDay,BorderLayout.SOUTH);
		timeParamToPanel.add(new JLabel("/"),BorderLayout.SOUTH);
		timeParamToPanel.add(toYear,BorderLayout.SOUTH);
		
		timeParamToPanel.add(new JLabel(" "),BorderLayout.SOUTH);
		
		timeParamToPanel.add(toHour,BorderLayout.SOUTH);
		timeParamToPanel.add(new JLabel(":"),BorderLayout.SOUTH);
		timeParamToPanel.add(toMinute,BorderLayout.SOUTH);
		timeParamToPanel.add(new JLabel(":"),BorderLayout.SOUTH);
		timeParamToPanel.add(toSecond,BorderLayout.SOUTH);
		
		timeParamToPanel.add(new JLabel(" "));
		
		timeParamToPanel.add(toAMPM);
		
		JPanel sizeParamPanel = new JPanel();
		fromSize = new JTextField(10);
		toSize = new JTextField(10);
		
		sizeParamPanel.add(fromSize);
		sizeParamPanel.add(new JLabel(" to "));
		sizeParamPanel.add(toSize);//setParamPanel();
		JPanel countParamPanel = new JPanel();
		//JLabel toLabel = new JLabel("to");
		
		fromCount = new JTextField(10);
		toCount = new JTextField(10);
		
		countParamPanel.add(fromCount);
		countParamPanel.add(new JLabel(" to "));
		countParamPanel.add(toCount);
	
		commonInfo = setCommonInfo();
		// Add all components to JPanel p.
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BorderLayout());
		timeButton.setBorder(
				BorderFactory.createEmptyBorder(0,9,0,0));
		timePanel.add(timeButton,BorderLayout.WEST);
		timePanel.add(timeParamFromPanel, BorderLayout.CENTER);
		timePanel.add(timeParamToPanel, BorderLayout.SOUTH);
		
		JPanel sizePanel = new JPanel();
		sizePanel.add(sizeButton);
		sizePanel.add(sizeParamPanel);
		
		JPanel countPanel = new JPanel();
		countPanel.add(countButton);
		countPanel.add(countParamPanel);
		
		// Add all of the components to the panel.
		p.add(timePanel);
		p.add(timeLabel);
		p.add(sizePanel);
		p.add(sizeLabel);
		p.add(countPanel);
		p.add(commonInfo);
		
		// Use Spring Layout to organize the panel:
		SpringLayout layout = new SpringLayout();
		p.setLayout(layout);
		
		layout.putConstraint(
				SpringLayout.NORTH, timePanel, 15, 
				SpringLayout.NORTH, p);
		layout.putConstraint(
				SpringLayout.NORTH, timeLabel, 0, 
				SpringLayout.SOUTH, timePanel);
		
		layout.putConstraint(
				SpringLayout.WEST, 
				timeLabel, 
				40, 
				SpringLayout.WEST, 
				p);
		
		layout.putConstraint(
				SpringLayout.NORTH, 
				sizePanel, 
				15, 
				SpringLayout.SOUTH, 
				timeLabel);
		
		layout.putConstraint(
				SpringLayout.NORTH, sizeLabel, 0, 
				SpringLayout.SOUTH, 
				sizePanel);
		
		layout.putConstraint(
				SpringLayout.WEST, sizeLabel, 40, 
				SpringLayout.WEST, 
				p);
		
		layout.putConstraint(
				SpringLayout.NORTH, countPanel, 15, 
				SpringLayout.SOUTH, sizeLabel);
		
		layout.putConstraint(
				SpringLayout.NORTH, commonInfo, 30, 
				SpringLayout.SOUTH, countPanel);
		
		return p;
	}
	
	/**
	 * advancedQuery is the second tab in the Dialogue box.  It 
	 * will be used when the chemists wish to use compounds to 
	 * define a query or use SQL commands to define their own 
	 * query.
	 * @return - the JPanel with the advanced query information.
	 */
	public JPanel advancedQuery() 
	{
		JPanel p = new JPanel();
		
		JLabel savedLabel = new JLabel("Open Saved Query: ");
		Object[] list = {"Query A", "Query B", "Query C"};
		JComboBox savedQueries = new JComboBox(list);
		JLabel sqlLabel = new JLabel("User-defined SQL Query:");
		JTextArea sqlTextArea = new JTextArea(7, 30);
		JScrollPane sqlText = new JScrollPane(sqlTextArea);
		JCheckBox saveBox = new JCheckBox("Save query as:");
		JTextField saveField = new JTextField(20);
		
		// Add all components to the panel.
		p.add(savedLabel);
		p.add(savedQueries);
		p.add(sqlLabel);
		p.add(sqlText);
		p.add(saveBox);
		p.add(saveField);
		//p.add(commonInfo);
		
		// Use Spring Layout to organize the panel.
		SpringLayout layout = new SpringLayout();
		p.setLayout(layout);
		
		layout.putConstraint(SpringLayout.NORTH, savedLabel, 15, 
				SpringLayout.NORTH, p);
		layout.putConstraint(SpringLayout.WEST, savedLabel, 15, 
				SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.WEST, savedQueries, 20, 
				SpringLayout.EAST, savedLabel);
		layout.putConstraint(SpringLayout.NORTH, savedQueries, 14, 
				SpringLayout.NORTH, p);
		layout.putConstraint(SpringLayout.NORTH, sqlLabel, 20, 
				SpringLayout.SOUTH, savedLabel);
		layout.putConstraint(SpringLayout.WEST, sqlLabel, 5, 
				SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.NORTH, sqlText, 5, 
				SpringLayout.SOUTH, sqlLabel);
		layout.putConstraint(SpringLayout.WEST, sqlText, 5, 
				SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.NORTH, saveBox, 10, 
				SpringLayout.SOUTH, sqlText);
		layout.putConstraint(SpringLayout.WEST, saveBox, 30, 
				SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.NORTH, saveField, 11, 
				SpringLayout.SOUTH, sqlText);
		layout.putConstraint(SpringLayout.WEST, saveField, 5, 
				SpringLayout.EAST, saveBox);
		//layout.putConstraint(SpringLayout.NORTH, commonInfo, 30, 
		//SpringLayout.SOUTH, saveBox);
		
		return p;
	}
	
	/**
	 * setCommonInfo() lays out the information that the two tabbed panels share;
	 * the name field, the OK button, and the CANCEL button.  This method cuts 
	 * back on redundant programming and makes the two panels look similar.
	 * @return - a JPanel with the text field and the bottons.
	 */
	public JPanel setCommonInfo()
	{
		JPanel commonInfo = new JPanel();
		//Create Name text field;
		JPanel name = new JPanel();
		JLabel nameLabel = new JLabel("Name: ");
		nameField = new JTextField(30);
		name.add(nameLabel);
		name.add(nameField);
		
		JPanel commentPanel = new JPanel();
		JLabel commentLabel = new JLabel("Comment: ");
		commentField = new JTextField(30);
		commentPanel.add(commentLabel);
		commentPanel.add(commentField);
		
		// Create the OK and CANCEL buttons
		JPanel buttons = new JPanel();
		JButton thisOkButton;
		//if (okButton == null)
		//{
			okButton = new JButton("OK");
			thisOkButton = okButton;
		//}
		//else
		//{
		//	okButton2 = new JButton("OK");
		//	thisOkButton = okButton2;
		//}
		thisOkButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(thisOkButton);
		buttons.add(cancelButton);

		
		//Add info to panel and lay out.
		commonInfo.add(name);
		commonInfo.add(commentPanel);
		commonInfo.add(buttons);
		commonInfo.setLayout(new BoxLayout(
				commonInfo, BoxLayout.Y_AXIS));
		
		return commonInfo;
	}
	
	public void itemStateChanged(ItemEvent e) 
	{
		Object source = e.getItemSelectable();
		
		
		
		if (e.getStateChange() == ItemEvent.DESELECTED)
		{
			if (source == timeButton) {
				timeSelected = false;
			} else if (source == sizeButton) {
				sizeSelected = false;
			} else if (source == countButton) {
				countSelected = false;
			}
		}
		else if (e.getStateChange() == ItemEvent.SELECTED)
		{
			if (source == timeButton) {
				timeSelected = true;
			} else if (source == sizeButton) {
				sizeSelected = true;
			} else if (source == countButton) {
				countSelected = true;
			}
		}
	
	}
	
	//TODO: Check user input for correctness
	public void actionPerformed(ActionEvent e) 
	{
		Object source = e.getSource();
		int collectionID = cTree.getSelectedCollection().
		getCollectionID();
		if (tabbedPane.getSelectedIndex() == 0)
		{
			String where = "";
			if (source == okButton || source == okButton2) {
				if (sizeSelected)
				{
					where += "[size] <= " + toSize.getText() 
					+ " AND [size] >= " + fromSize.getText();
				}
				if (timeSelected)
				{
					if (sizeSelected)
						where += " AND";
					where += " [time] <= '" + 
					toMonth.getSelectedItem() + "/" + 
					toDay.getSelectedItem() + "/" +
					toYear.getSelectedItem() + " " +
					toHour.getSelectedItem() + ":" + 
					toMinute.getSelectedItem() + ":" + 
					toSecond.getSelectedItem() + " " +
					toAMPM.getSelectedItem()
					+ "' AND [time] >= '" + 
					fromMonth.getSelectedItem() + "/" + 
					fromDay.getSelectedItem() + "/" +
					fromYear.getSelectedItem() + " " +
					fromHour.getSelectedItem() + ":" + 
					fromMinute.getSelectedItem() + ":" + 
					fromSecond.getSelectedItem() + " " +
					fromAMPM.getSelectedItem() + "'";
				}
				if (countSelected)
				{
					if (sizeSelected || timeSelected)
						where += " AND";
					where += " AtomInfo.AtomID <= " + toCount.getText() +
					" AND AtomInfo.AtomID >= " + fromCount.getText(); 
				}
				System.out.println("Dividing now:");
				System.out.println(where);
				String name = nameField.getText();
				String comment = commentField.getText();
				SQLDivider sqld = new SQLDivider(collectionID, db,
						name, comment, where);
				sqld.setCursorType(CollectionDivider.DISK_BASED);
				sqld.divide();
				cTree.updateTree();
				dispose();
			}			
			else  
			{
				System.out.println("Disposing");
				dispose();
			}
		}
	}


}
