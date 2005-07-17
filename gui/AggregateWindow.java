package gui;

import collection.AggregationOptions;
import collection.Collection;
import database.InfoWarehouse;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.*;

public class AggregateWindow extends JFrame implements ActionListener, ListSelectionListener {
	private MainFrame parentFrame;
	private JButton createSeries, cancel;
	private JTextField descriptionField, startTime, endTime, interval;
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
		
		setSize(500, 510);
		setResizable(false);

		JPanel mainPanel = new JPanel(new BorderLayout());

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		JPanel topPanel = setupTopPanel(collections);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(createSeries = new JButton("Create Series"));
		buttonPanel.add(cancel = new JButton("Cancel"));
		
		mainPanel.add(topPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
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
		SpringLayout leftPanelLayout = new SpringLayout();
		JPanel leftPanel = new JPanel(leftPanelLayout);
		
		JPanel collectionsPanel = new JPanel(new BorderLayout());
		JScrollPane collections = new JScrollPane(collectionsList);
		collectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		collections.setPreferredSize(new Dimension(230, 170));
		
		collectionsPanel.add(new JLabel("Collections:"), BorderLayout.NORTH);
		collectionsPanel.add(collections);
		
		JLabel timeBasis = new JLabel("Time Basis:");
	    ButtonGroup group = new ButtonGroup();
	    group.add(selSeqRadio = new JRadioButton("Selected Sequence"));
	    group.add(timesRadio = new JRadioButton("Times"));
	    selSeqRadio.setSelected(true);
	    
	    JPanel timesPanel = new JPanel(new GridLayout(3, 2, 0, 5));
	    timesPanel.add(new JLabel("Start Time:"));
	    timesPanel.add(startTime = new JTextField(8));
	    timesPanel.add(new JLabel("End Time:"));
	    timesPanel.add(endTime = new JTextField(8));
	    timesPanel.add(new JLabel("Interval:"));
	    timesPanel.add(interval = new JTextField(8));
	    
		leftPanel.add(collectionsPanel);
		leftPanel.add(timeBasis);
		leftPanel.add(selSeqRadio);
		leftPanel.add(timesRadio);
		leftPanel.add(timesPanel);

		leftPanelLayout.putConstraint(SpringLayout.NORTH, collectionsPanel, 10, 
				SpringLayout.NORTH, leftPanel);
		leftPanelLayout.putConstraint(SpringLayout.NORTH, timeBasis, 40,
				SpringLayout.SOUTH, collectionsPanel);
		leftPanelLayout.putConstraint(SpringLayout.NORTH, selSeqRadio, 0,
				SpringLayout.SOUTH, timeBasis);
		leftPanelLayout.putConstraint(SpringLayout.NORTH, timesRadio, 0,
				SpringLayout.SOUTH, selSeqRadio);
		leftPanelLayout.putConstraint(SpringLayout.NORTH, timesPanel, 5,
				SpringLayout.SOUTH, timesRadio);
		leftPanelLayout.putConstraint(SpringLayout.WEST, timesPanel, 15,
				SpringLayout.WEST, timesRadio);
				
		return leftPanel;
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
				System.out.println(evt.getStateChange() == ItemEvent.SELECTED);
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
		bottomHalf = addComponent(new JLabel("Collection Options for " + collection.getName()), bottomHalf);
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
		bottomHalf = addComponent(new JLabel("for m/z values:"), bottomHalf);
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
			String newSeriesName = descriptionField.getText().trim();
			
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
				timeBasisSQLStr = db.getTimeBasisSQLString(selectedCollection.getCollectionID());
			} else {
				GregorianCalendar start = new GregorianCalendar();
				GregorianCalendar end = new GregorianCalendar();
				GregorianCalendar interval = new GregorianCalendar();
				timeBasisSQLStr = db.getTimeBasisSQLString(start, end, interval);
			}
				
			int collectionID = db.createAggregateTimeSeries(newSeriesName, collections, timeBasisSQLStr, baseSequenceOnCollection);
			parentFrame.updateSynchronizedTree(collectionID);
			setVisible(false);
			dispose();
		} else if (source == cancel) {
			setVisible(false);
			dispose();
		}
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
}
