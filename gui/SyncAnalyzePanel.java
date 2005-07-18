package gui;

import collection.Collection;
import database.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class SyncAnalyzePanel extends JPanel {
	private MainFrame parentFrame;
	private CollectionTree tree;
	private InfoWarehouse db;
	
	private JComboBox firstSeq, secondSeq;
	private SyncCollectionModel firstCollectionModel, secondCollectionModel;
	
	private JButton exportToCSV;
	
	public SyncAnalyzePanel(MainFrame parentFrame, InfoWarehouse db, CollectionTree tree, Collection collectionToBaseOn) {
		super(new BorderLayout());
		
		this.parentFrame = parentFrame;
		this.db = db;
		this.tree = tree;
		
		firstCollectionModel = new SyncCollectionModel(collectionToBaseOn);
		secondCollectionModel = new SyncCollectionModel(firstCollectionModel);

		JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel sequenceSel = new JPanel(new GridLayout(1, 2, 5, 5));
		JPanel firstSeqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		firstSeqPanel.add(new JLabel("1st Sequence: "));
		firstSeqPanel.add(firstSeq = new JComboBox(firstCollectionModel));

		JPanel secondSeqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		secondSeqPanel.add(new JLabel("2nd Sequence: "));
		secondSeqPanel.add(secondSeq = new JComboBox(secondCollectionModel));
		
		sequenceSel.add(firstSeqPanel);
		sequenceSel.add(secondSeqPanel);
		
		topPanel.add(sequenceSel);
		
		JScrollPane bottomPane = new JScrollPane();
		add(topPanel, BorderLayout.NORTH);
		add(bottomPane, BorderLayout.CENTER);
	}
	
	public void updateModels(Collection collection) {
		firstCollectionModel.setupModelFromCollection(collection);
		secondCollectionModel.setupModelFromOtherModel(firstCollectionModel);
	}
	
	public boolean containsCollection(Collection c) {
		return firstCollectionModel.getMatchingItem(c) != null;
	}
	
	public void selectCollection(Collection c) {
		firstSeq.setSelectedItem(firstCollectionModel.getMatchingItem(c));
		firstSeq.repaint();
	}

	private class SyncCollectionModel implements ComboBoxModel {
		private Collection[] collections;
		private Collection selectedItem = null;
	
		public SyncCollectionModel(Collection collectionToBaseOn) {
			setupModelFromCollection(collectionToBaseOn);
		}

		public SyncCollectionModel(SyncCollectionModel otherModel) {
			setupModelFromOtherModel(otherModel);
		}
		
		public void setupModelFromCollection(Collection collectionToBaseOn) {
			ArrayList<Collection> allCollectionsInTree = tree.getCollectionsInTreeOrderFromRoot(1, collectionToBaseOn);
			ArrayList<Integer> collectionIDs = new ArrayList<Integer>();

			for (int i = 0; i < allCollectionsInTree.size(); i++)
				collectionIDs.add(allCollectionsInTree.get(i).getCollectionID());
			
			collectionIDs = db.getCollectionIDsWithAtoms(collectionIDs, false);
						
			collections = new Collection[collectionIDs.size()];
			
			int index = 0;
			// Make sure that if all else fails, the first item is selected...
			boolean selectNext = true;
			
			for (int i = 0; i < allCollectionsInTree.size(); i++) {
				Collection curCollection = allCollectionsInTree.get(i);
				
				if (collectionToBaseOn.equals(curCollection))
					selectNext = true;
				
				if (collectionIDs.contains(curCollection.getCollectionID())) {
					if (selectNext) {
						selectedItem = curCollection;
						selectNext = false;
					}
						
					collections[index++] = curCollection;
				}
			}
		}
		
		public void setupModelFromOtherModel(SyncCollectionModel otherModel) {
			collections = new Collection[otherModel.collections.length + 1];
			
			// Make blank entry at i = 0
			for (int i = 0; i < otherModel.collections.length; i++)
				collections[i + 1] = otherModel.collections[i];
		}


		// This helps to ensure that only the originally 
		// constructed items are thrown into this collection
		public Collection getMatchingItem(Collection c) {
			for (int i = 0; i < collections.length; i++)
				if (c.equals(collections[i]))
					return collections[i];
			
			return null;
		}
		
		public Object getSelectedItem() { return selectedItem; }
        public void setSelectedItem(Object item) { selectedItem = (Collection) item; }
        public int getSize() { return collections.length; }
        public Object getElementAt(int index) { return collections[index]; }
        public void addListDataListener(ListDataListener l) {}
        public void removeListDataListener(ListDataListener l) {}
	}
}
