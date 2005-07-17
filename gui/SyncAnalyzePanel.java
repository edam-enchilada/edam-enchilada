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
	
	public SyncAnalyzePanel(MainFrame parentFrame, InfoWarehouse db, CollectionTree tree, Collection collection) {
		super(new BorderLayout());
		
		this.parentFrame = parentFrame;
		this.db = db;
		this.collection = collection;
		
		ArrayList<Collection> allCollectionsInTree = tree.getCollectionsInTreeOrderFromRoot(1, collection);
		ArrayList<Integer> collectionIDs = new ArrayList<Integer>();

		for (int i = 0; i < allCollectionsInTree.size(); i++)
			collectionIDs.add(allCollectionsInTree.get(i).getCollectionID());
		
		collectionIDs = db.getCollectionIDsWithAtoms(collectionIDs, false);
		
		String[] collectionNames = new String[collectionIDs.size()];
		
		int index = 0;
		for (int i = 0; i < allCollectionsInTree.size(); i++) {
			if (collectionIDs.contains(allCollectionsInTree.get(i).getCollectionID()))
				collectionNames[index++] = allCollectionsInTree.get(i).getName();
		}
		
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
