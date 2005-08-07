package gui;

import collection.AggregationOptions;
import collection.Collection;
import database.InfoWarehouse;
import externalswing.SwingWorker;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

public class AggregateWindow extends JFrame implements ActionListener, ListSelectionListener {
	private MainFrame parentFrame;
	private JButton createSeries, cancel;
	private JTextField descriptionField;
	private TimePanel startTime, endTime, intervalPeriod;
	private JList collectionsList;
	private CollectionListModel collectionListModel;
	private JRadioButton selSeqRadio, timesRadio; 
	
	private InfoWarehouse db;
	private Hashtable<Collection, JPanel> cachedCollectionPanels;
	private Collection[] collections;
	
	private JPanel centerPanel; 

	public AggregateWindow(MainFrame parentFrame, InfoWarehouse db, Collection[] collections) {
		super("Aggregate Collections");
		
		this.parentFrame = parentFrame;
		this.db = db;
		this.collections = collections;
		
		cachedCollectionPanels = new Hashtable<Collection, JPanel>();
		collectionsList = new JList(collectionListModel = new CollectionListModel(collections));
		
		setSize(500, 540);
		setResizable(false);

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
	    group.add(selSeqRadio = new JRadioButton("Selected Sequence"));
	    group.add(timesRadio = new JRadioButton("Times"));
	    selSeqRadio.setSelected(true);
	    
	    JPanel timesPanel = new JPanel(new GridLayout(3, 1, 0, 5));
	    timesPanel.add(startTime = new TimePanel("Start Time:", false));
	    timesPanel.add(endTime = new TimePanel("End Time:", false));
	    timesPanel.add(intervalPeriod = new TimePanel("Interval:", true));
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
		
		if (collection.getDatatype().equals("ATOFMS")) {
			ret = getATOFMSPanel(collection);
		} else {
			ret = new JPanel(); // Blank if unknown collection type...
		}
		
		cachedCollectionPanels.put(collection, ret);
		
		return ret;
	}
	
	private JPanel getATOFMSPanel(Collection collection) {
		final AggregationOptions options = collection.getAggregationOptions();
		final JTextField peakTolerance = new JTextField(4);
		peakTolerance.setText(String.valueOf(options.peakTolerance));
		peakTolerance.setEditable(false);
		peakTolerance.setHorizontalAlignment(JTextField.CENTER);
		
		JSlider slider = new JSlider(0, 50, (int) (options.peakTolerance * 100));
		slider.setPaintLabels(true);
		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		labels.put(new Integer(0), new JLabel("0"));
		labels.put(new Integer(50), new JLabel(".5"));
		slider.setLabelTable(labels);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    JSlider source = (JSlider)e.getSource();
			    double value = source.getValue() / 100.0;
			    options.peakTolerance = value;

			    peakTolerance.setText(String.valueOf(value));
			}
		});

		JPanel tolerancePanel = new JPanel(new BorderLayout(10, 0));
		tolerancePanel.add(slider, BorderLayout.WEST);
		tolerancePanel.add(peakTolerance, BorderLayout.CENTER);
		
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

	    JCheckBox partCount = new JCheckBox();
	    partCount.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				options.produceParticleCountTS = (evt.getStateChange() == ItemEvent.SELECTED);
			}
		});
	    partCount.setSelected(options.produceParticleCountTS);
		
	    JTextField mzValues = new JTextField(100);
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
	    
		JPanel mainPanel = new JPanel();
		
		JPanel partCountPanel = new JPanel(new BorderLayout(10, 0));
		partCountPanel.add(partCount, BorderLayout.WEST);
		partCountPanel.add(new JLabel("<html>Produce particle-count <br>time series</html>"), BorderLayout.CENTER);
		
		JPanel bottomHalf = addComponent(new JPanel(), mainPanel);
		bottomHalf = addComponent(new JLabel("Collection Options for " + collection.getName() + ":"), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JLabel("Peak Tolerance:  "), bottomHalf);
		bottomHalf = addComponent(tolerancePanel, bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JLabel("Combining Method:"), bottomHalf);
		bottomHalf = addComponent(combWithSum, bottomHalf);
		bottomHalf = addComponent(combWithAverage, bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(partCountPanel, bottomHalf);
		bottomHalf = addComponent(new JPanel(), bottomHalf);
		bottomHalf = addComponent(new JLabel("Produce time series"), bottomHalf);
		bottomHalf = addComponent(new JLabel("for m/z values (leave blank for all):"), bottomHalf);
		bottomHalf = addComponent(mzValues, bottomHalf);
		
		return mainPanel;
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
		if (source == createSeries) {
			String newSeriesName = descriptionField.getText().trim().replace("'", "''");			
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
			
			if (baseSequenceOnCollection) {
				Collection selectedCollection = collectionListModel.getCollectionAt(collectionsList.getSelectedIndex());
				timeBasisSQLStr = getTimeBasisSQLString(selectedCollection.getCollectionID());
			} else {
				GregorianCalendar start = startTime.getDate();
				GregorianCalendar end = endTime.getDate();
				GregorianCalendar interval = intervalPeriod.getDate();

				timeBasisSQLStr = getTimeBasisSQLString(start, end, interval);
			}

			int collectionID = createAggregateTimeSeries(newSeriesName, collections, timeBasisSQLStr, baseSequenceOnCollection);
			parentFrame.updateSynchronizedTree(collectionID);
			setVisible(false);
			dispose();
		} else if (source == cancel) {
			setVisible(false);
			dispose();
		}
	}
	
	private int createAggregateTimeSeries(String syncRootName, final Collection[] collections, 
			String timeBasisSQLstring, boolean baseOnCollection) {
		final String timeBasisSetupStr = baseOnCollection ? ""                             : timeBasisSQLstring + "\n ";
		final String timeBasisQueryStr = baseOnCollection ? "(" + timeBasisSQLstring + ")" : "@timeBasis";
		
		final int[][] mzValues = new int[collections.length][];
		int numCollectionsToMake = 0;

		for (int i = 0; i < collections.length; i++) {
			Collection curColl = collections[i];
			if (curColl.getDatatype().equals("ATOFMS")) {
				mzValues[i] = db.getValidMZValuesForCollection(curColl);
				
				if (mzValues[i] != null)
					numCollectionsToMake += mzValues[i].length;

				AggregationOptions options = curColl.getAggregationOptions();
				if (options == null)
					curColl.setAggregationOptions(options = new AggregationOptions());
				
				if (options.produceParticleCountTS)
					numCollectionsToMake++;
			}
		}
		
		final ProgressBarWrapper progress = 
			new ProgressBarWrapper(this, "Aggregating Time Series", numCollectionsToMake);
		
		final int rootCollectionID = db.createEmptyCollection("TimeSeries", 1, syncRootName, "", "");
		
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				for (int i = 0; i < collections.length; i++)
					db.createAggregateTimeSeries(progress, rootCollectionID, collections[i], mzValues[i], timeBasisSetupStr, timeBasisQueryStr);

				progress.disposeThis();
				
				return null;
			}
		};
		worker.start();
		
		progress.constructThis();
		
		return rootCollectionID;
	}
	
	private String getTimeBasisSQLString(int collectionID) {
		String timeSubstr = 
			"   select distinct Time \n" +
			"   from ATOFMSAtomInfoDense AID \n" +
			"   join AtomMembership AM on (AID.AtomID = AM.AtomID) \n" +
			"   where CollectionID = " + collectionID;
		
		return "select T1.Time as BasisTimeStart, T1.Time + min(T2.Time - T1.Time) as BasisTimeEnd \n" +
			   "from (\n" + timeSubstr + "\n) T1 " +
			   "join (\n" + timeSubstr + "\n) T2 on (T1.Time < T2.Time) \n" +
			   "group by T1.Time \n" +
			   "union \n" +
			   "select max(Time) as BasisTimeStart, null as BasisTimeEnd \n" +
			   "from ATOFMSAtomInfoDense AID \n" +
			   "join AtomMembership AM on (AID.AtomID = AM.AtomID) \n" +
			   "where CollectionID = " + collectionID;
	}
	
	private String getTimeBasisSQLString(Calendar start, Calendar end, Calendar interval) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String sql = "declare @timeBasis table ( BasisTimeStart datetime, BasisTimeEnd datetime ) \n";
		java.util.Date startTime, endTime;

		while (start.before(end)) {
			startTime = start.getTime();
			
			//start.add(Calendar.DATE,   interval.get(Calendar.DATE));
			start.add(Calendar.HOUR,   interval.get(Calendar.HOUR));
			start.add(Calendar.MINUTE, interval.get(Calendar.MINUTE));
			start.add(Calendar.SECOND, interval.get(Calendar.SECOND));
			
			if (!start.before(end))
				start = end;
			
			endTime = start.getTime();
			
			sql += "insert @timeBasis (BasisTimeStart, BasisTimeEnd) values ('" + 
						dateFormat.format(startTime) + "', '" + dateFormat.format(endTime) + "') \n";
		}
		
		return sql;
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
		
		public TimePanel(String name, boolean isInterval) {
			setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
			
			JLabel label = new JLabel(name);
			hour   = getComboBox(isInterval ? 0 : 1, 24, false);
			minute = getComboBox(0, 59, true);
			second = getComboBox(0, 59, true);

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
				day    = getComboBox(1, 31, false);
				month  = getComboBox(1, 12, false);
				year   = getComboBox(2000, 2005, false);
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
		
		private JComboBox getComboBox(int start, int end, boolean padZero) {
			String[] ret = new String[end - start + 1];
			for (int i = 0; i < ret.length; i++) {
				int num = start + i;
				String prefix = (padZero && num < 10) ? "0" : "";
				
				ret[i] = prefix + String.valueOf(start + i);
			}
			
			return new JComboBox(ret);
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
