package gui;

import collection.Collection;
import database.InfoWarehouse;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class AggregateWindow extends JFrame implements ActionListener {
	private MainFrame parentFrame;
	private JButton createSeries, cancel;
	private JTextField descriptionField, startTime, endTime, interval;
	private JList collectionsList;
	private JRadioButton selSeqRadio, timesRadio; 
	
	private InfoWarehouse db;
	
	public AggregateWindow(MainFrame parentFrame, InfoWarehouse db, Collection[] collections) {
		super("Aggregate Collections");
		
		this.parentFrame = parentFrame;
		this.db = db;
		
		collectionsList = new JList(new CollectionListModel(collections));
		
		setSize(500, 510);
		setResizable(false);

		JPanel mainPanel = new JPanel(new BorderLayout());

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		JPanel topPanel = setupTopPanel();
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(createSeries = new JButton("Create Series"));
		buttonPanel.add(cancel = new JButton("Cancel"));
		
		mainPanel.add(topPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		add(mainPanel);
		
		createSeries.addActionListener(this);
		cancel.addActionListener(this);
	}
	

	private JPanel setupTopPanel() {
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel mainPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		
		mainPanel.add(setupLeftPanel());
		mainPanel.add(setupRightPanel());
		
		JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		descriptionPanel.add(new JLabel("Description:  "));
		descriptionPanel.add(descriptionField = new JTextField(30));
		
		topPanel.add(descriptionPanel, BorderLayout.NORTH);
		topPanel.add(mainPanel, BorderLayout.CENTER);
		
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
	
	private JPanel setupRightPanel() {
		return new JPanel();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		
		if (source == createSeries) {
			setVisible(false);
			dispose();
		} else if (source == cancel) {
			setVisible(false);
			dispose();
		}
	}
	
	public class CollectionListModel extends AbstractListModel {
		Collection[] collections;
		
		public CollectionListModel(Collection[] collections) {
			this.collections = collections;
		}
		
		public int getSize() { return collections.length; }
		public Object getElementAt(int index) {
			return " " + db.getCollectionName(getSelectedID(index)); 
		}
		
		public int getSelectedID(int index) {
			return collections[index].getCollectionID();
		}
	};
}
