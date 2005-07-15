package gui;

import collection.Collection;
import database.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class SyncAnalyzePanel extends JPanel {
	private MainFrame parentFrame;
	private InfoWarehouse db;
	
	private Collection collection;
	private JComboBox firstSeq, secondSeq;
	private JButton exportToCSV;
	
	private Integer[] collectionIDs;
	
	public SyncAnalyzePanel(MainFrame parentFrame, InfoWarehouse db, Collection collection) {
		super(new BorderLayout());
		
		this.parentFrame = parentFrame;
		this.db = db;
		this.collection = collection;
		
		Set<Integer> allCollectionIDs = db.getAllCollectionsInTree(collection.getCollectionID());

		collectionIDs = new Integer[allCollectionIDs.size()];
		collectionIDs = allCollectionIDs.toArray(collectionIDs);
		String[] collectionNames = new String[collectionIDs.length];
		for (int i = 0; i < collectionIDs.length; i++)
			collectionNames[i] = db.getCollectionName(collectionIDs[i]);
		
		JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel sequenceSel = new JPanel(new GridLayout(1, 2, 5, 5));
		JPanel firstSeqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		firstSeqPanel.add(new JLabel("1st Sequence: "));
		firstSeqPanel.add(firstSeq = new JComboBox(collectionNames));

		JPanel secondSeqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		secondSeqPanel.add(new JLabel("2nd Sequence: "));
		secondSeqPanel.add(secondSeq = new JComboBox(collectionNames));
		
		sequenceSel.add(firstSeqPanel);
		sequenceSel.add(secondSeqPanel);
		
		topPanel.add(sequenceSel);
		
		JScrollPane bottomPane = new JScrollPane();
		add(topPanel, BorderLayout.NORTH);
		add(bottomPane, BorderLayout.CENTER);
	}
}
