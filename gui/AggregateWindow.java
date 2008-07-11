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

package gui;

import collection.AggregationOptions;
import collection.Collection;
import database.*;
import externalswing.SwingWorker;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

/**
 * This class is a gui object for aggregating data. It automatically adjusts options depending on
 * data type and automatically parses dates of varying format. 
 * 
 */
public class AggregateWindow extends JFrame implements ActionListener, ListSelectionListener {
	private MainFrame parentFrame;
	private JButton createSeries, cancel, calculateTimes;
	private JTextField descriptionField;
	private TimePanel startTime, endTime, intervalPeriod;
	private JList collectionsList;
	private CollectionListModel collectionListModel;
	private JRadioButton selSeqRadio, timesRadio; 
	private JRadioButton eurDateRadio, naDateRadio, iso_1DateRadio, iso_2DateRadio;
	private JComboBox matchingCombo;
	
	private InfoWarehouse db;
	private Hashtable<Collection, JPanel> cachedCollectionPanels;
	private Collection[] collections;
	
	private JPanel centerPanel,timesPanel, dateRadioPanel;

	public AggregateWindow(MainFrame parentFrame, InfoWarehouse db, Collection[] collections) {
		super("Aggregate Collections");
		
		this.parentFrame = parentFrame;
		this.db = db;
		this.collections = collections;
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
				
			}
		});
		
		cachedCollectionPanels = new Hashtable<Collection, JPanel>();
		collectionsList = new JList(collectionListModel = new CollectionListModel(collections));
		
		setPreferredSize(new Dimension(500, 625));
		setSize(500,625);
		setResizable(true);

		JPanel mainPanel = new JPanel(new BorderLayout());

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		JPanel topPanel = setupTopPanel(collections);
		JPanel timePanel = setupTimePanel();
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(createSeries = new JButton("Create Series"));
		buttonPanel.add(cancel = new JButton("Cancel"));
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(timePanel, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		mainPanel.add(topPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		add(mainPanel);
		
		createSeries.addActionListener(this);
		cancel.addActionListener(this);
		collectionsList.addListSelectionListener(this);
		
		collectionsList.setSelectedIndex(0);
		
	}
	

	private JPanel setupTopPanel(Collection[] collections) {
		JPanel topPanel = new JPanel(new BorderLayout());
		centerPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		
		centerPanel.add(setupLeftPanel(), 0);
		centerPanel.add(getPanelForCollection(collections[0]), 1);
		
		JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		descriptionPanel.add(new JLabel("Description:  "));
		descriptionPanel.add(descriptionField = new JTextField(30));
		
		topPanel.add(descriptionPanel, BorderLayout.NORTH);
		topPanel.add(centerPanel, BorderLayout.CENTER);
		
		return topPanel;
	}
	
	private JPanel setupLeftPanel() {
		JPanel leftPanel = new JPanel(new BorderLayout());
		JScrollPane collections = new JScrollPane(collectionsList);
		collectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		collections.setPreferredSize(new Dimension(230, 190));
		
		leftPanel.add(new JLabel("Collections:"), BorderLayout.NORTH);
		leftPanel.add(collections);
		
		return leftPanel;
	}
	
	private JPanel setupTimePanel() {
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new EmptyBorder(10, 5, 5, 0));

		JLabel timeBasis = new JLabel("Time Basis:");
	    ButtonGroup group_1 = new ButtonGroup();
	    group_1.add(selSeqRadio = new JRadioButton("Match to:"));
	    group_1.add(timesRadio = new JRadioButton("Times"));
	    timesRadio.setSelected(true);
	    
	    ButtonGroup group_2 = new ButtonGroup();
	    group_2.add(naDateRadio = new JRadioButton("MM/DD/YYYY or MM-DD-YYYY"));
	    group_2.add(eurDateRadio = new JRadioButton("DD/MM/YYYY or DD.MM.YYYY"));
	    group_2.add(iso_1DateRadio = new JRadioButton("YYYY-MM-DD"));
	    group_2.add(iso_2DateRadio = new JRadioButton("YYYY-DD-MM"));
	    naDateRadio.addActionListener(this);
	    eurDateRadio.addActionListener(this);
	    iso_1DateRadio.addActionListener(this);
	    iso_2DateRadio.addActionListener(this);
	    naDateRadio.setSelected(true);
	    
	    JPanel matchingPanel = new JPanel();
	    matchingCombo = new JComboBox(collections);
		matchingCombo .setEditable(false);
		//matchingPanel.add(selSeqRadio);
		//matchingPanel.add(matchingCombo);
	    
	    Calendar startDate = new GregorianCalendar(), endDate = new GregorianCalendar();
	    Calendar interval = new GregorianCalendar(0, 0, 0, 0, 0, 0);
	    // add things with indices so that you can remove them later!
	    timesPanel = new JPanel(new GridLayout(4, 1, 0, 5));
	    timesPanel.add(calculateTimes=new JButton("Calculate Time Interval"),0);
	    calculateTimes.addActionListener(this);
	    timesPanel.add(startTime = new TimePanel("Start Time:", startDate, false),1);
	    timesPanel.add(endTime = new TimePanel("End Time:", endDate, false),2);
	    timesPanel.add(intervalPeriod = new TimePanel("Interval:", interval, true),3);
	    timesPanel.setBorder(new EmptyBorder(0, 25, 0, 0));
	    
	    dateRadioPanel = new JPanel(new GridLayout(2,2,3,3));
	    dateRadioPanel.add(naDateRadio,0);
	    dateRadioPanel.add(eurDateRadio,1);
	    dateRadioPanel.add(iso_1DateRadio,2);
	    dateRadioPanel.add(iso_2DateRadio,3);
	    dateRadioPanel.setBorder(new EmptyBorder(0, 25, 0, 0));
	    
		JPanel bottomHalf = addComponent(timeBasis, bottomPanel);
		//bottomHalf = addComponent(matchingPanel, bottomHalf);
		bottomHalf = addComponent(selSeqRadio, bottomHalf);
		bottomHalf = addComponent(matchingCombo, bottomHalf);
		
		bottomHalf = addComponent(timesRadio, bottomHalf);
		bottomHalf = addComponent(timesPanel, bottomHalf);
		bottomHalf = addComponent(dateRadioPanel, bottomHalf);
		
	    return bottomPanel;
	}
	private JPanel getPanelForCollection(Collection collection) {
		if (cachedCollectionPanels.containsKey(collection))
			return cachedCollectionPanels.get(collection);
		
		JPanel ret;
		
		AggregationOptions options = collection.getAggregationOptions();
		if (options == null)
			collection.setAggregationOptions(options = new AggregationOptions());
		
		if (collection.getDatatype().equals("ATOFMS") || 
				collection.getDatatype().equals("AMS")) {
			ret = getATOFMSPanel(collection);
		} else if (collection.getDatatype().equals("TimeSeries")) {
			ret = getTimeSeriesPanel(collection);
		} else {
			ret = new JPanel(); // Blank if unknown collection type...
		}
		
		cachedCollectionPanels.put(collection, ret);
		
		return ret;
	}
	
	private JPanel getATOFMSPanel(Collection collection) {
		final AggregationOptions options = collection.getAggregationOptions();
		
	    JCheckBox partCount = new JCheckBox();
	    partCount.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				options.produceParticleCountTS = (evt.getStateChange() == ItemEvent.SELECTED);
			}
		});
	    partCount.setSelected(options.produceParticleCountTS);
		
	    // right now it's going to do all or just those you select.  it might
	    // also be clever to do those you select or the complement of those you
	    // select.  But I'm not going to do that right now.  --Thomas
	    
	    final JTextField mzValues = new JTextField(100);
	    try {
			options.setMZValues(options.mzString);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(
					mzValues, 
					"Error: Invalid input (" + options.mzString + ")\n" +
							"Make sure your input is of the form: \"-10 to -4, 5 to 6, 100 to 400\"\n" +
							"and that all values are within the range of -600 to 600", 
					"Invalid Input", 
					JOptionPane.ERROR_MESSAGE); 
			mzValues.setText(options.mzString);
		}
		mzValues.setText(options.mzString);
	    mzValues.addFocusListener(new FocusListener() {
	    	private String savedText;
	    	
	    	public void focusGained(FocusEvent evt) {
	    		savedText = ((JTextField) evt.getSource()).getText();
	    	}
	    	
	    	public void focusLost(FocusEvent evt) {
	    		JTextField mzValues = ((JTextField) evt.getSource());
	    		String newText = mzValues.getText();
	    		try {
	    			options.setMZValues(newText);
	    		} catch (NumberFormatException e) {
	    			JOptionPane.showMessageDialog(
	    					mzValues, 
	    					"Error: Invalid input (" + newText + ")\n" +
	    							"Make sure your input is of the form: \"-10 to -4, 5 to 6, 100 to 400\"\n" +
	    							"and that all values are within the range of -600 to 600", 
	    					"Invalid Input", 
	    					JOptionPane.ERROR_MESSAGE); 
	    			mzValues.setText(savedText);
	    		}
	    	}
	    });
	    
	    ButtonGroup bg = getValueCombiningButtons(options);
	    Enumeration<AbstractButton> combiningButtons = bg.getElements();
	    
		JPanel mainPanel = new JPanel();
		
		JPanel partCountPanel = new JPanel(new BorderLayout(10, 0));
		partCountPanel.add(partCount, BorderLayout.WEST);
		partCountPanel.add(new JLabel("<html>Produce particle-count <br>time series</html>"), BorderLayout.CENTER);
		
		final JCheckBox noMZValues = new JCheckBox("Don't aggregate M/Z values", false);
		noMZValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.setMZValues(noMZValues.isSelected() ? "" : mzValues.getText());
				if (noMZValues.isSelected())
					options.allMZValues = false;
				mzValues.setEnabled(! noMZValues.isSelected());
			}
		});
		
		JPanel bottomHalf = addComponent(new JPanel(), mainPanel);
		bottomHalf = addComponent(new JLabel("Collection Options for " + collection.getName() + ":"), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JLabel("Combining Method:"), bottomHalf);
		
		while (combiningButtons.hasMoreElements())
			bottomHalf = addComponent(combiningButtons.nextElement(), bottomHalf);
		
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(partCountPanel, bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(noMZValues, bottomHalf);
		bottomHalf = addComponent(new JLabel("Produce time series"), bottomHalf);
		bottomHalf = addComponent(new JLabel("for m/z values (leave blank for all):"), bottomHalf);
		bottomHalf = addComponent(mzValues, bottomHalf);
		
		return mainPanel;
	}
	
	private JPanel getTimeSeriesPanel(Collection collection) {
		final AggregationOptions options = collection.getAggregationOptions();
		
	    JCheckBox isContinuousData = new JCheckBox();
	    isContinuousData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				options.treatDataAsContinuous = (evt.getStateChange() == ItemEvent.SELECTED);
			}
		});
	    isContinuousData.setSelected(options.treatDataAsContinuous);
	    
	    ButtonGroup bg = getValueCombiningButtons(options);
	    Enumeration<AbstractButton> combiningButtons = bg.getElements();
	    
		JPanel continuousDataPanel = new JPanel(new BorderLayout(10, 0));
		continuousDataPanel.add(isContinuousData, BorderLayout.WEST);
		continuousDataPanel.add(new JLabel("<html>Treat Data as Continuous</html>"), BorderLayout.CENTER);
		
		JPanel mainPanel = new JPanel();
		JPanel bottomHalf = addComponent(new JPanel(), mainPanel);
		bottomHalf = addComponent(new JLabel("Collection Options for " + collection.getName() + ":"), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(continuousDataPanel, bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JLabel("Combining Method:"), bottomHalf);
		
		while (combiningButtons.hasMoreElements())
			bottomHalf = addComponent(combiningButtons.nextElement(), bottomHalf);
		
		return mainPanel;
	}

	private ButtonGroup getValueCombiningButtons(final AggregationOptions options) {
		JRadioButton combWithSum = new JRadioButton("Sum");
		JRadioButton combWithAverage = new JRadioButton("Average");
		
		combWithSum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JRadioButton) e.getSource()).isSelected())
					options.combMethod = AggregationOptions.CombiningMethod.SUM;
			}
		});
		
		combWithAverage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JRadioButton) e.getSource()).isSelected())
					options.combMethod = AggregationOptions.CombiningMethod.AVERAGE;
			}
		});
		
	    ButtonGroup group = new ButtonGroup();
	    group.add(combWithSum);
	    group.add(combWithAverage);
	    combWithSum.setSelected(options.combMethod == AggregationOptions.CombiningMethod.SUM);
	    combWithAverage.setSelected(options.combMethod == AggregationOptions.CombiningMethod.AVERAGE);

	    return group;
	}
	
	private JPanel addComponent(JComponent newComponent, JPanel parent) {
		JPanel bottomHalf = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.add(newComponent, BorderLayout.NORTH);
		parent.add(bottomHalf, BorderLayout.CENTER);
		return bottomHalf;
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source == calculateTimes){
			final Calendar startDate = new GregorianCalendar(), endDate = new GregorianCalendar();
		    final ProgressBarWrapper progressBar = 
				new ProgressBarWrapper(this, "Calculating Time Interval", collections.length);
			progressBar.constructThis();
			progressBar.setIndeterminate(true);
			final AggregateWindow thisRef = this;
			final SwingWorker aggWorker = new SwingWorker() {
				private int collectionID;
				public Object construct() {
					db.getMaxMinDateInCollections(collections, startDate, endDate);
					return new Integer(0);
				}
				public void finished() {
					timesPanel.remove(1);
				    timesPanel.add(startTime = new TimePanel("Start Time:", startDate, false),1);
				    timesPanel.remove(2);
				    timesPanel.add(endTime = new TimePanel("End Time:", endDate, false),2);
				    thisRef.pack();
				    thisRef.repaint();
					progressBar.disposeThis();
				}
			};
			aggWorker.start();
		    
		    
		}else if (source == createSeries) {
			final long timingStart = new Date().getTime();
			final String newSeriesName = descriptionField.getText().trim().replace("'", "''");			
			if (newSeriesName.equals("")) {
    			JOptionPane.showMessageDialog(
    					this, 
    					"Please fill out description field before aggregating.", 
    					"No Description", 
    					JOptionPane.ERROR_MESSAGE);
    			
    			return;
			}
			
			String timeBasisSQLStr = null;
			boolean baseSequenceOnCollection = selSeqRadio.isSelected(); 
			
			final Aggregator aggregator;
			if (baseSequenceOnCollection) {
				Collection selectedCollection = collections[matchingCombo.getSelectedIndex()];
				aggregator = new Aggregator(this, db, selectedCollection);
				System.out.println("selected Collection: "+selectedCollection.getName()+
						"\tID: "+ selectedCollection.getCollectionID());
			} else {
				//Check the time scales for correct formatting
				if(startTime.isBad())
				{
					JOptionPane.showMessageDialog(
		    				this, 
		    				"Please enter a valid time for \"Start Time\".\n"+
		    				"Valid times will appear in green.", 
		    				"Invalid Time String", 
		    				JOptionPane.ERROR_MESSAGE);
		    		return;
				}
				else if(endTime.isBad())
				{
					JOptionPane.showMessageDialog(
		    				this, 
		    				"Please enter a valid time for \"End Time\".\n"+
		    				"Valid times will appear in green.", 
		    				"Invalid Time String", 
		    				JOptionPane.ERROR_MESSAGE);
		    		return;
				}
				else if(intervalPeriod.isBad())
				{
					JOptionPane.showMessageDialog(
		    				this, 
		    				"Please enter a valid time for \"Interval\".\n"+
		    				"Valid times will appear in green.", 
		    				"Invalid Time String", 
		    				JOptionPane.ERROR_MESSAGE);
		    		return;
				}
				Calendar start = startTime.getDate();
				Calendar end = endTime.getDate();
				Calendar interval = intervalPeriod.getDate();
				if (end.before(start)) {
					JOptionPane.showMessageDialog(
	    					this, 
	    					"Start time must come before end time...", 
	    					"Invalid times used for aggregation basis", 
	    					JOptionPane.ERROR_MESSAGE);
	    			
	    			return;
				} else if (interval.get(Calendar.HOUR_OF_DAY) == 0 &&
							interval.get(Calendar.MINUTE) == 0 &&
							interval.get(Calendar.SECOND) == 0) {
					JOptionPane.showMessageDialog(
	    					this, 
	    					"Time interval cannot be zero...", 
	    					"Invalid times used for aggregation basis", 
	    					JOptionPane.ERROR_MESSAGE);
	    			
	    			return;
				}
				aggregator = new Aggregator(this, db, start, end, interval);
				System.out.println("start: "+start.getTimeInMillis()+"\nend: "+end.getTimeInMillis()+
						"\ninterval: "+interval.getTimeInMillis());
			}
			final ProgressBarWrapper initProgressBar =
				aggregator.createAggregateTimeSeriesPrepare(collections);
			System.out.print("collections: ");
			for(Collection collection : collections){
				System.out.print(collection.getCollectionID()+", ");
			}
			System.out.println();
			
			final SwingWorker aggWorker = new SwingWorker() {
				private int collectionID;
				public Object construct() {
					db.beginTransaction();
					try{
					collectionID = aggregator.createAggregateTimeSeries(
							newSeriesName,collections,initProgressBar,
							parentFrame);
					db.commitTransaction();
					} catch(InterruptedException e){
						db.rollbackTransaction();
						return null;
					} catch(AggregationException f){
						final String name = f.collection.getName();
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								JOptionPane.showMessageDialog(null,
										"The start and stop dates which you selected resulted" +
										" in the collection: "+name+" having " +
										"no data points to aggregate.  Either remove the collection or try" +
										"different dates.");
							}
							
						});
						db.rollbackTransaction();
						return null;
					}
					return new Integer(collectionID);
				}
				public void finished() {
					initProgressBar.disposeThis();
					setVisible(false);
					long timingEnd = new Date().getTime();
					System.out.println("Aggregation Time: " + (timingEnd-timingStart));
					// for all collections, set default Aggregation Options.
					for (int i = 0; i < collections.length; i++) {
						AggregationOptions ao = new AggregationOptions();
						ao.setDefaultOptions();
						collections[i].setAggregationOptions(ao);
						//collections[i].getAggregationOptions().setDefaultOptions();
					}
					
					dispose();		
				}
			};
			aggWorker.start();

		} else if (source == cancel) {
			cancel();
		}
		//When the radio buttons are clicked, adjust the text in each box to conform to the new format
		//It's faster just to delete them and make new ones using the correct standard
		else if (source == naDateRadio)
		{
			Calendar start = startTime.getDate();
			Calendar end = endTime.getDate();
			final AggregateWindow thisRef = this;
			timesPanel.remove(1);
		    timesPanel.add(startTime = new TimePanel("Start Time:", start, false),1);
		    timesPanel.remove(2);
		    timesPanel.add(endTime = new TimePanel("End Time:", end, false),2);
		    thisRef.pack();
		    thisRef.repaint();
		}
		else if (source == eurDateRadio)
		{
			Calendar start = startTime.getDate();
			Calendar end = endTime.getDate();
			final AggregateWindow thisRef = this;
			timesPanel.remove(1);
		    timesPanel.add(startTime = new TimePanel("Start Time:", start, false),1);
		    timesPanel.remove(2);
		    timesPanel.add(endTime = new TimePanel("End Time:", end, false),2);
		    thisRef.pack();
		    thisRef.repaint();
		}	
		else if (source == iso_1DateRadio)
		{
			Calendar start = startTime.getDate();
			Calendar end = endTime.getDate();
			final AggregateWindow thisRef = this;
			timesPanel.remove(1);
		    timesPanel.add(startTime = new TimePanel("Start Time:", start, false),1);
		    timesPanel.remove(2);
		    timesPanel.add(endTime = new TimePanel("End Time:", end, false),2);
		    thisRef.pack();
		    thisRef.repaint();
		}
		else if (source == iso_2DateRadio)
		{
			Calendar start = startTime.getDate();
			Calendar end = endTime.getDate();
			final AggregateWindow thisRef = this;
			timesPanel.remove(1);
		    timesPanel.add(startTime = new TimePanel("Start Time:", start, false),1);
		    timesPanel.remove(2);
		    timesPanel.add(endTime = new TimePanel("End Time:", end, false),2);
		    thisRef.pack();
		    thisRef.repaint();
		}
	}
	
	public void cancel(){
//		 for all collections, set default Aggregation Options.
		for (int i = 0; i < collections.length; i++) {
			if(collections[i]==null)
				System.out.println("collections["+i+"] is null");
			if(collections[i].getAggregationOptions()==null)
				System.out.println("collections["+i+"].getAggregationOptions() is null");
			if(collections[i].getAggregationOptions()!=null)
				collections[i].getAggregationOptions().setDefaultOptions();
		}
		setVisible(false);
		dispose();
	}
	public void valueChanged(ListSelectionEvent e) {
		int index = collectionsList.getSelectedIndex();
		
		if (!e.getValueIsAdjusting() && index > -1) {
			Collection collection = collectionListModel.getCollectionAt(index);

			centerPanel.remove(1);
			centerPanel.add(getPanelForCollection(collection), 1);
			centerPanel.validate();
			centerPanel.repaint();
		}
	}
	
	public class CollectionListModel extends AbstractListModel {
		Collection[] collections;
		
		public CollectionListModel(Collection[] collections) {
			this.collections = collections;
		}
		
		public int getSize() { return collections.length; }
		public Object getElementAt(int index) {
			return " " + getCollectionAt(index).getName(); 
		}
		
		public Collection getCollectionAt(int index) {
			return collections[index];
		}
	};
	
	/**
	 * This is the updated TimePanel class. The old class used pulldowns for date and time selection, 
	 * which was not very easy.
	 * Instead, it now uses a JTextField that updates on every change within it. The parsing style 
	 * is configurable through radio buttons, and the TimePanel adopts whatever style is currently selected
	 * upon creation.
	 * 
	 * @author rzeszotj
	 *
	 */
	public class TimePanel extends JPanel implements DocumentListener {
		private boolean isInterval;
		
		private SimpleDateFormat NADate_1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		private SimpleDateFormat NADate_2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		private SimpleDateFormat EDate_1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		private SimpleDateFormat EDate_2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		private SimpleDateFormat ISO_1Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		private SimpleDateFormat ISO_2Date = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
		private SimpleDateFormat Hours_1 = new SimpleDateFormat("HH:mm:ss");
		
		private Date currentTime;
		private JTextField timeField;
		private ParsePosition p = new ParsePosition(0);
		
		private final Color ERROR = new Color(255,128,128);
		private final Color GOOD = new Color(70,255,85);
		
		public TimePanel(String name, Calendar init, boolean interval) {
			setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
			isInterval = interval;
			
			JLabel label = new JLabel(name);
			if (isInterval)
			{
				timeField = new JTextField(getTimeString(init),9);
			}
			else
			{
				timeField = new JTextField(getTimeString(init),19);
			}
			
			timeField.getDocument().addDocumentListener(this);
	        
			label.setPreferredSize(new Dimension(70, 20));
			
			add(label);
			add(timeField);
			updateDate();
		}
		
		/**
		 * Formats a calendar into a string using whatever style is selected
		 * @param c The calendar to format
		 */
	    private String getTimeString(Calendar c)
	    {
	    	String s = "";
	    	Date cur = c.getTime();
	    	if(isInterval)
	    		s = Hours_1.format(cur);
			else if(naDateRadio.isSelected())
			{
				s = NADate_1.format(cur);
			}
			else if(eurDateRadio.isSelected())
			{
				s = EDate_1.format(cur);
			}
			else if(iso_1DateRadio.isSelected())
			{
				s = ISO_1Date.format(cur);
			}
			else if(iso_2DateRadio.isSelected())
			{
				s = ISO_2Date.format(cur);
			}
	    	return s;
	    }
	    
	    /**
		 * Updates currentTime with text in timeField, changing background
		 */
		private void updateDate()
		{
			//Get text from the text box, if blank, show error and stop
			String cur = null;
			try
			{
				cur = timeField.getText();
			}
			catch(NullPointerException e)
			{
				timeField.setBackground(ERROR);
				currentTime = null;
				return;
			}
			//Make sure we have a string with something in it
			p.setIndex(0);
			if (cur == null)
			{
				timeField.setBackground(ERROR);
				return;
			}
			
			//Parse the string depending on what is selected
			if(isInterval)
			{
				currentTime = Hours_1.parse(cur,p);
			}
			else if(naDateRadio.isSelected())
			{
				currentTime = NADate_1.parse(cur,p);
				if (currentTime == null)
				{
					p.setIndex(0);
					currentTime = NADate_2.parse(cur,p);
				}
			}
			else if(eurDateRadio.isSelected())
			{
				currentTime = EDate_1.parse(cur,p);
				if (currentTime == null)
				{
					p.setIndex(0);
					currentTime = EDate_2.parse(cur,p);
				}
			}
			else if(iso_1DateRadio.isSelected())
			{
				currentTime = ISO_1Date.parse(cur,p);
			}
			else if(iso_2DateRadio.isSelected())
			{
				currentTime = ISO_2Date.parse(cur,p);
			}
			else
				timeField.setBackground(ERROR);
			
			//If we parsed correctly, color it GOOD, otherwise color an ERROR
			if(currentTime == null)
			{
				timeField.setBackground(ERROR);
			}
			else
			{
				timeField.setBackground(GOOD);
			}
		}
		
		/**
		 * Returns true is an unparsable entry is present
		 */
		public boolean isBad()
		{
			if (currentTime == null)
				return true;
			else
				return false;
		}
		
		/**
		 * Returns a calendar object based on the string in textField
		 */
		public GregorianCalendar getDate() {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(currentTime);
			return c;
		}
		
		public void actionPerformed(ActionEvent e) {
			timesRadio.setSelected(true);
		}
		public void insertUpdate(DocumentEvent ev) {
	        updateDate();
	    }
	    
	    public void removeUpdate(DocumentEvent ev) {
	        updateDate();
	    }
	    
	    public void changedUpdate(DocumentEvent ev) {
	    	
	    }
	}
}