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

import collection.Collection;

import analysis.CollectionDivider;
import analysis.SQLDivider;

import database.DynamicTable;
import database.InfoWarehouse;
import externalswing.SwingWorker;

import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

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
	private JFrame parent;
	private CollectionTree cTree;
	private InfoWarehouse db;
	
	private JButton okButton; //Default button
	private JButton cancelButton;
	
	private JPanel timePanel;
	private JPanel sizePanel;
	private JPanel countPanel;
	private JCheckBox timeButton;
	private JCheckBox sizeButton;
	private JCheckBox countButton;
	
	private JRadioButton naDateRadio,eurDateRadio;
	private Calendar epoch = Calendar.getInstance();
	private TimePanel fromTime;
	private TimePanel toTime;

	
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
	
	private Collection collection;
	
	/**
	 * Constructor.  Creates a tabbed pane that is added to
	 * a JDialog Object.  The constructor also shows the GUI.
	 * @param frame - the parent of the JDialog object.
	 */
	public QueryDialog(JFrame frame, CollectionTree cTree,
			InfoWarehouse db, Collection collection) 
	{
		super(frame, "Query", false);
		
		Container cont = getContentPane();
		cont.setLayout(new BorderLayout());
		
		this.parent = frame;
		this.cTree = cTree;
		this.db = db;
		this.collection = collection;
		
		// Create the two panels for the dialogue box.
		JPanel basic = basicQuery();
		JPanel advanced = advancedQuery();
		commonInfo = setCommonInfo();		
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Basic",null,basic,null);
		getRootPane().setDefaultButton(okButton);
		//Advanced tab hidden for now - uncomment to enable
		//@author shaferia 1/12/07
		//tabbedPane.addTab("Advanced",null,advanced,null);
		
		cont.add(tabbedPane, BorderLayout.CENTER); // Add the tabbed pane to the dialogue box.
		cont.add(commonInfo, BorderLayout.SOUTH);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		//AMS particles have no size information
		if (collection.getDatatype().equals("AMS")) {
			sizeButton.setEnabled(false);
			setEnabledChildren(sizePanel, false);
		}
		
		pack();
		
		setResizable(false);
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
		JPanel opts = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		opts.setLayout(layout);
		
		timeButton = new JCheckBox("Time:");
		timeButton.addItemListener(this);
		
	    timePanel = new JPanel(new GridLayout(3, 1, 0, 5));
		Calendar epoch = Calendar.getInstance();
		
		ButtonGroup dateGroup = new ButtonGroup();
		dateGroup.add(naDateRadio = new JRadioButton("MM/DD/YYYY or MM-DD-YYYY"));
		dateGroup.add(eurDateRadio = new JRadioButton("DD/MM/YYYY or DD.MM.YYYY"));
		naDateRadio.setSelected(true);
		naDateRadio.addActionListener(this);
		eurDateRadio.addActionListener(this);
		
		timePanel.add(fromTime = new TimePanel("Start Time:",epoch,false,naDateRadio,eurDateRadio),0);
		timePanel.add(toTime = new TimePanel("End Time:",epoch,false,naDateRadio,eurDateRadio),1);
		
		JPanel explPanel = new JPanel(new GridLayout(1,2,3,3));
		explPanel.add(naDateRadio,0);
		explPanel.add(eurDateRadio,1);
		timePanel.add(explPanel);
		
		
		sizeButton = new JCheckBox("Size:");
		sizeButton.addItemListener(this);
		
		sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel sizeLabel = new JLabel("microns");
		fromSize = new JTextField(10);
		toSize = new JTextField(10);
		sizePanel.add(fromSize);
		sizePanel.add(new JLabel(" to "));
		sizePanel.add(toSize);//setParamPanel();
		sizePanel.add(sizeLabel);
		
		
		countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		countButton = new JCheckBox("Count:");
		countButton.addItemListener(this);
		fromCount = new JTextField(10);
		toCount = new JTextField(10);
		countPanel.add(fromCount);
		countPanel.add(new JLabel(" to "));
		countPanel.add(toCount);
		
		c.anchor = GridBagConstraints.WEST;
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		layout.setConstraints(timeButton, c);
		layout.setConstraints(sizeButton, c);
		layout.setConstraints(countButton, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(timePanel, c);
		layout.setConstraints(sizePanel, c);
		layout.setConstraints(countPanel, c);
		
		setEnabledChildren(timePanel, false);
		setEnabledChildren(sizePanel, false);
		setEnabledChildren(countPanel, false);
		
		opts.add(timeButton);
		opts.add(timePanel);
		opts.add(sizeButton);
		opts.add(sizePanel);
		opts.add(countButton);
		opts.add(countPanel);
		
		opts.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 18));
		
		return opts;
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
		p.setLayout(new BorderLayout());

		JTextArea sqlTextArea = new JTextArea(7, 30);
		JPanel leftPanel = new JPanel(new BorderLayout());
		JLabel sqlLabel = new JLabel("User-defined SQL Query:");
		leftPanel.add(sqlLabel, BorderLayout.NORTH);
		JScrollPane sqlText = new JScrollPane(sqlTextArea);
		leftPanel.add(sqlText, BorderLayout.CENTER);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		p.add(leftPanel, BorderLayout.CENTER);
		
		JPanel saveArea = new JPanel();
		BoxLayout layout = new BoxLayout(saveArea, BoxLayout.Y_AXIS);
		saveArea.setLayout(layout);
		
		JLabel savedLabel = new JLabel("Open Saved Query: ");
		Object[] list = {"Query A", "Query B", "Query C"};
		JComboBox savedQueries = new JComboBox(list);
		JButton execOpen = new JButton("Open");
		
		JLabel saveNewLabel = new JLabel("Save query as:");
		JTextField saveField = new JTextField(20);
		JButton execSave = new JButton("Save");
		
		saveArea.add(savedLabel);
		saveArea.add(savedQueries);
		saveArea.add(execOpen);

		saveArea.add(Box.createVerticalStrut(10));
		JSeparator divider = new JSeparator(JSeparator.HORIZONTAL);
		divider.setBorder(BorderFactory.createRaisedBevelBorder());
		saveArea.add(divider);
		saveArea.add(Box.createVerticalStrut(10));
		
		saveArea.add(saveNewLabel);
		saveArea.add(saveField);
		saveArea.add(execSave);
		
		// Add all components to the panel.
		p.add(saveArea, BorderLayout.EAST);
		p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 18));
		
		//p.add(commonInfo);
		
		// Use Spring Layout to organize the panel.
		/*
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
		*/
		
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
		JPanel namePanel = new JPanel();
		JLabel nameLabel = new JLabel("Name: ");
		nameField = new JTextField(30);
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		
		JPanel commentPanel = new JPanel();
		JLabel commentLabel = new JLabel("Comment: ");
		commentField = new JTextField(30);
		commentPanel.add(commentLabel);
		commentPanel.add(commentField);
		
		// Create the OK and CANCEL buttons
		JPanel buttons = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);

		//no more divider
		//JSeparator divider = new JSeparator(JSeparator.HORIZONTAL);
		//divider.setBorder(BorderFactory.createRaisedBevelBorder());
		
		//Add info to panel and lay out.
		//commonInfo.add(Box.createVerticalStrut(15));
		//commonInfo.add(divider);
		//commonInfo.add(Box.createVerticalStrut(15));
		commonInfo.add(namePanel);
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
				setEnabledChildren(timePanel, false);
			} else if (source == sizeButton) {
				sizeSelected = false;
				setEnabledChildren(sizePanel, false);
			} else if (source == countButton) {
				countSelected = false;
				setEnabledChildren(countPanel, false);
			}
		}
		else if (e.getStateChange() == ItemEvent.SELECTED)
		{
			if (source == timeButton) {
				timeSelected = true;
				setEnabledChildren(timePanel, true);
			} else if (source == sizeButton) {
				sizeSelected = true;
				setEnabledChildren(sizePanel, true);
			} else if (source == countButton) {
				countSelected = true;
				setEnabledChildren(countPanel, true);
			}
		}
	
	}
	
	/**
	 * Sets the enabled state of c and all its children to b
	 * @param c the component to modify
	 * @param b if true, make enabled.
	 */
	public void setEnabledChildren(Component c, boolean b) {
		c.setEnabled(b);
		
		if (c instanceof Container)
			for (Component subc : ((Container)c).getComponents())
				setEnabledChildren(subc, b);
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
			if (source == okButton) {
				//TODO: once "Advanced" tab is implemented, add criteria here
				if (!(sizeSelected || timeSelected || countSelected)) {
					JOptionPane.showMessageDialog(
							this,
							"Please select query criteria!",
							"Query error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (sizeSelected)
				{
					where += "[size] <= " + toSize.getText() 
					+ " AND [size] >= " + fromSize.getText();
				}
				if (timeSelected)
				{
					if(fromTime.isBad())
					{
						JOptionPane.showMessageDialog(
			    				this, 
			    				"Please enter a valid time for \"Start Time\".\n"+
			    				"Invalid times will appear in red.", 
			    				"Invalid Time String", 
			    				JOptionPane.ERROR_MESSAGE);
			    		return;
					}
					else if(toTime.isBad())
					{
						JOptionPane.showMessageDialog(
			    				this, 
			    				"Please enter a valid time for \"End Time\".\n"+
			    				"Invalid times will appear in red.", 
			    				"Invalid Time String", 
			    				JOptionPane.ERROR_MESSAGE);
			    		return;
					}
					else if(fromTime.getDate().after(toTime.getDate()))
					{
						JOptionPane.showMessageDialog(
			    				this, 
			    				"Start time must be before end time.", 
			    				"Invalid Time Strings", 
			    				JOptionPane.ERROR_MESSAGE);
			    		return;
					}
					if (sizeSelected)
						where += " AND";
					where += " [time] <= '" + 
					toTime.getTimeString()
					+ "' AND [time] >= '" + 
					fromTime.getTimeString() + "'";
				}
				if (countSelected)
				{	
					
					int from = Integer.parseInt(fromCount.getText());
					int to = Integer.parseInt(toCount.getText());
					if (sizeSelected || timeSelected)
						where += " AND";
					String densename = db.getDynamicTableName(DynamicTable.AtomInfoDense, collection.getDatatype());
					where += " rownum <= " + to +
					" AND rownum >= " + from; 
				}
				System.out.println("Dividing now:");
				System.out.println(where);
				String name = nameField.getText();
				String comment = commentField.getText();
				
				//Run the query outside the EDT
				final SQLDivider sqld = new SQLDivider(collectionID, db,
						name, comment, where);
				final ProgressBarWrapper pbar = 
					new ProgressBarWrapper(parent, "Executing Query", 100);
				pbar.setIndeterminate(true);
				pbar.constructThis();
				pbar.setText("Executing Query...");
				
				SwingWorker sw = new SwingWorker() {
					public Object construct() {
						sqld.setCursorType(CollectionDivider.DISK_BASED);
						sqld.divide();
						return null;
					}
					
					public void finished() {
						cTree.updateTree();
						pbar.disposeThis();
						dispose();
					}
				};
				sw.start();
			}			
			else if (source == naDateRadio)
			{
				Calendar start = fromTime.getDate();
				Calendar end = toTime.getDate();
				final QueryDialog thisRef = this;
				timePanel.remove(0);
			    timePanel.add(fromTime = new TimePanel("Start Time:", start,
			    		                               false,naDateRadio,eurDateRadio),0);
			    timePanel.remove(1);
			    timePanel.add(toTime = new TimePanel("End Time:", end,
			    		                             false,naDateRadio,eurDateRadio),1);
			    thisRef.pack();
			    thisRef.repaint();
			}
			else if (source == eurDateRadio)
			{
				Calendar start = fromTime.getDate();
				Calendar end = toTime.getDate();
				final QueryDialog thisRef = this;
				timePanel.remove(0);
			    timePanel.add(fromTime = new TimePanel("Start Time:", start,
			    		                               false,naDateRadio,eurDateRadio),0);
			    timePanel.remove(1);
			    timePanel.add(toTime = new TimePanel("End Time:", end,
			    		                             false,naDateRadio,eurDateRadio),1);
			    thisRef.pack();
			    thisRef.repaint();
			}
			else  
			{
				System.out.println("Disposing");
				dispose();
			}
		}
	}
	

}
