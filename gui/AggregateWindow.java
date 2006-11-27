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

public class AggregateWindow extends JFrame implements ActionListener, ListSelectionListener {
	private MainFrame parentFrame;
	private JButton createSeries, cancel, calculateTimes;
	private JTextField descriptionField;
	private TimePanel startTime, endTime, intervalPeriod;
	private JList collectionsList;
	private CollectionListModel collectionListModel;
	private JRadioButton selSeqRadio, timesRadio; 
	
	private InfoWarehouse db;
	private Hashtable<Collection, JPanel> cachedCollectionPanels;
	private Collection[] collections;
	
	private JPanel centerPanel,timesPanel; 

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
		
		setPreferredSize(new Dimension(500, 580));
		setSize(500,580);
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
	    ButtonGroup group = new ButtonGroup();
	    group.add(selSeqRadio = new JRadioButton("Selected Collection"));
	    group.add(timesRadio = new JRadioButton("Times"));
	    selSeqRadio.setSelected(true);
	    
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
	   
		JPanel bottomHalf = addComponent(timeBasis, bottomPanel);
		bottomHalf = addComponent(selSeqRadio, bottomHalf);
		bottomHalf = addComponent(timesRadio, bottomHalf);
		bottomHalf = addComponent(timesPanel, bottomHalf);
		
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
				Collection selectedCollection = collectionListModel.getCollectionAt(collectionsList.getSelectedIndex());
				aggregator = new Aggregator(this, db, selectedCollection);
				System.out.println("selected Collection: "+selectedCollection.getCollectionID());
			} else {
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
										" in the collection '"+name+"' having " +
										"no data points to aggregate.  Either remove the collection or try " +
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
	
	public class TimePanel extends JPanel implements ActionListener {
		private JComboBox day, month, year, hour, minute, second;
		private boolean isInterval;
		
		public TimePanel(String name, Calendar initDate, boolean interval) {
			setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
			isInterval = interval;
			
			JLabel label = new JLabel(name);
			hour   = getComboBox(initDate.get(Calendar.HOUR_OF_DAY), 0, isInterval ? 24 : 23, false);
			minute = getComboBox(initDate.get(Calendar.MINUTE), 0, 59, true);
			second = getComboBox(initDate.get(Calendar.SECOND), 0, 59, true);

			label.setPreferredSize(new Dimension(70, 20));
			hour.setPreferredSize(new Dimension(40, 20));
			minute.setPreferredSize(new Dimension(40, 20));
			second.setPreferredSize(new Dimension(40, 20));
			
			add(label);
			
			add(hour);
			add(new JLabel(":"));
			add(minute);
			add(new JLabel(":"));
			add(second);
			
			if (!isInterval) {
				month  = getComboBox(initDate.get(Calendar.MONTH) + 1, 1, 12, false);
				day    = getComboBox(initDate.get(Calendar.DAY_OF_MONTH), 1, 31, false);

				// Obtain current year and set upperbound to be 3 years beyond
				DateFormat df = new SimpleDateFormat("yyyy");
		        Date date = new Date();
		        int currentYear = Integer.parseInt(df.format(date));
				year   = getComboBox(initDate.get(Calendar.YEAR), 2000, currentYear+3, false);
				day.setPreferredSize(new Dimension(40, 20));
				month.setPreferredSize(new Dimension(40, 20));
				year.setPreferredSize(new Dimension(60, 20));
				
				add(new JLabel("   "));
				
				add(month);
				add(new JLabel("\\"));
				add(day);
				add(new JLabel("\\"));
				add(year);
				
				day.addActionListener(this);
				month.addActionListener(this);
				year.addActionListener(this);
			}
			
			hour.addActionListener(this);
			minute.addActionListener(this);
			second.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			timesRadio.setSelected(true);
		}
		
		private JComboBox getComboBox(int initVal, int start, int end, boolean padZero) {
			String[] ret = new String[end - start + 1];
			for (int i = 0; i < ret.length; i++) {
				int num = start + i;
				String prefix = (padZero && num < 10) ? "0" : "";
				
				ret[i] = prefix + String.valueOf(start + i);
			}
			
			JComboBox retBox = new JComboBox(ret);
			retBox.setSelectedIndex(initVal - start);
			return retBox;
		}
		
		private int getIntVal(JComboBox box) {
			if (box != null)
				return Integer.parseInt((String) box.getSelectedItem());
			else
				return 0;
		}
		
		public GregorianCalendar getDate() {
			return new GregorianCalendar(
					getIntVal(year), getIntVal(month) - 1, getIntVal(day),
					getIntVal(hour), getIntVal(minute),	getIntVal(second));
		}
	}
}