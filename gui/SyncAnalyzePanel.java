package gui;

import ATOFMS.Peak;
import chartlib.*;
import collection.Collection;
import database.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

public class SyncAnalyzePanel extends JPanel {
	private MainFrame parentFrame;
	private CollectionTree tree;
	private InfoWarehouse db;
	
	private JScrollPane bottomPane;
	
	private JComboBox firstSeq, secondSeq;
	private SyncCollectionModel firstCollectionModel, secondCollectionModel;
	
	private JComboBox[] conditionSeq, conditionComp;
	private SyncCollectionModel[] conditionModel;
	private JTextField[] conditionValue;
	private JComboBox[] booleanOps;
	
	private int numConditions = 2;
	
	private JButton exportToCSV, refresh;
	
	private static String[] comparators = { "", " <", " >", " <=", " >=", " =", " <>" };
	private static String[] booleans = { " AND", " OR" };
	
	public SyncAnalyzePanel(MainFrame parentFrame, InfoWarehouse db, CollectionTree tree, Collection collectionToBaseOn) {
		super(new BorderLayout());
		
		this.parentFrame = parentFrame;
		this.db = db;
		this.tree = tree;
		
		firstCollectionModel = new SyncCollectionModel(collectionToBaseOn);
		secondCollectionModel = new SyncCollectionModel(firstCollectionModel);

		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel sequenceSel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sequenceSel.add(new JLabel("1st Sequence: "));
		sequenceSel.add(firstSeq = new JComboBox(firstCollectionModel));
		sequenceSel.add(new JLabel("              2nd Sequence: "));
		sequenceSel.add(secondSeq = new JComboBox(secondCollectionModel));
		
		Box buttonPanel = new Box(BoxLayout.Y_AXIS);
		JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel exportToCSVPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		refreshPanel.add(refresh = new JButton("Refresh"));
		exportToCSVPanel.add(exportToCSV = new JButton("Export to CSV"));
		
		buttonPanel.add(refreshPanel);
		buttonPanel.add(exportToCSVPanel);

		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setupBottomPane();
			}
		});
		
		exportToCSV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setupBottomPane();
			}
		});
		
		JPanel bottomHalf = new JPanel(new BorderLayout());
		bottomHalf.add(buildConditionPanels(firstCollectionModel), BorderLayout.WEST);
		bottomHalf.add(buttonPanel, BorderLayout.CENTER);
		
		topPanel.add(sequenceSel, BorderLayout.NORTH);
		topPanel.add(bottomHalf, BorderLayout.CENTER);
		
		bottomPane = new JScrollPane();
		setupBottomPane();
		
		add(topPanel, BorderLayout.NORTH);
		add(bottomPane, BorderLayout.CENTER);
		
		validate();
	}
	
	private JPanel buildConditionPanels(SyncCollectionModel basisModel) {
		conditionSeq = new JComboBox[numConditions];
		conditionComp = new JComboBox[numConditions];
		conditionModel = new SyncCollectionModel[numConditions];
		conditionValue = new JTextField[numConditions];
		booleanOps = new JComboBox[numConditions - 1];
		
		JPanel conditionsPanel = new JPanel(new GridLayout(numConditions, 1, 5, 5));
		
		for (int i = 0; i < numConditions; i++) {
			JPanel thisCondPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			conditionModel[i] = new SyncCollectionModel(basisModel);
			
			if (i > 0) {
				thisCondPanel.add(booleanOps[i - 1] = new JComboBox(booleans));
			} else {
				JPanel p = new JPanel();
				p.setPreferredSize(new JComboBox(booleans).getPreferredSize());
				thisCondPanel.add(p);
			}
				
			thisCondPanel.add(new JLabel(" Condition " + (i + 1) + ": "));
			thisCondPanel.add(conditionSeq[i] = new JComboBox(conditionModel[i]));
			thisCondPanel.add(conditionComp[i] = new JComboBox(comparators));
			thisCondPanel.add(conditionValue[i] = new JTextField(10));

			conditionSeq[i].setPreferredSize(new Dimension((int) conditionSeq[i].getPreferredSize().getWidth(), 20));
			conditionComp[i].setPreferredSize(new Dimension((int) conditionComp[i].getPreferredSize().getWidth(), 20));
			conditionsPanel.add(thisCondPanel);
		}
		
		return conditionsPanel;
	}

	
	public void setupBottomPane() {		
		JPanel panePanel = new JPanel(new BorderLayout());
		bottomPane.setViewportView(panePanel);
		
		Hashtable<Date, double[]> data = null;
		Collection seq1 = (Collection) firstSeq.getSelectedItem();
		Collection seq2 = (Collection) secondSeq.getSelectedItem();

		ArrayList<String> conditionStrs = new ArrayList<String>();
		ArrayList<Collection> condCollections = new ArrayList<Collection>();
		
		if (seq1 != null) {
			for (int i = 0; i < numConditions; i++) {
				Collection condColl = (Collection) conditionSeq[i].getSelectedItem();
				String condVal = conditionValue[i].getText().trim();
				int curIndex = conditionStrs.size();
				
				if (condColl != null && conditionComp[i].getSelectedIndex() > 0 && !condVal.equals("")) {
					String condStr = "";
					
					if (i > 0 && curIndex > 0)
						condStr = booleanOps[i - 1].getSelectedItem() + " ";
					
					condStr += "C" + curIndex + ".Value " + conditionComp[i].getSelectedItem() + " " + condVal;
					
					conditionStrs.add(condStr);
					condCollections.add(condColl);
				}
			}
			
			data = db.getConditionalTSCollectionData(seq1, seq2, condCollections, conditionStrs);
		}
		
		if (data != null && data.size() > 0) {
			int numSequences = (seq2 != null ? 2 : 1);
			int numSets = numSequences + condCollections.size();
			
			Dataset[] datasets = new Dataset[numSets];
			for (int i = 0; i < numSets; i++)
				datasets[i] = new Dataset();
			
			Set<Date> keySet = data.keySet();
			Date[] dateSet = new Date[keySet.size()];
			keySet.toArray(dateSet);
			Arrays.sort(dateSet);

			double secondsFromStart = 0;
			double maxValue[] = new double[numSets];
			long startTime = dateSet[0].getTime();
			for (Date d : dateSet)
			{
				secondsFromStart = (d.getTime() - startTime) / 1000.0;
				double[] values = data.get(d);

				for (int i = 0; i < numSets; i++) {
					double value = values[i];
					
					if (value > maxValue[i])
						maxValue[i] = value;
					
					if (value != -99)
						datasets[i].add(new DataPoint(secondsFromStart, value));
				}
			}

			for (int i = 0; i < numSets; i++) {
				if (maxValue[i] <= 0)
					maxValue[i] = 10;
			}

			// sets up chart
			Chart chart = new Chart(numSequences, true);
			chart.setHasKey(false);
			chart.setTitle("<html><b>Time Series Comparison</b></html>");
			
			for (int i = 0; i < numSequences; i++) {
				chart.setTitleX(i, "Time (in seconds) from start of data");
				chart.setTitleY(i, "Sequence " + (i + 1) + " Value");
				chart.setColor(i, i == 0 ? Color.red : Color.blue);
				chart.setAxisBounds(i, -10, secondsFromStart + 10, 0, maxValue[i]);
				chart.setDataset(i, datasets[i]);
				chart.setDataDisplayType((datasets[i].size() == 1), true);
			}

			chart.setNumTicks(10,10, 1,1);
			chart.setBarWidth(3);
			chart.setPreferredSize(new Dimension(400, 400));
			
			chart.repaint();

			JPanel bottomPanel = addComponent(chart, panePanel);
			
			for (int i = numSequences; i < numSets; i++) {
				// sets up chart
				Chart compChart = new Chart(1, false);
				compChart.setHasKey(false);
				compChart.setTitle("<html><b>Condition Series " + (i - numSequences + 1) + "</b></html>");
				compChart.setTitleX(0, "Time (in seconds) from start of data");
				compChart.setTitleY(0, "Condition Series " + (i - numSequences + 1) + " Value");
				compChart.setColor(0, Color.green);
				compChart.setAxisBounds(0, -10, secondsFromStart + 10, 0, maxValue[i]);
				compChart.setDataset(0, datasets[i]);
				compChart.setDataDisplayType((datasets[i].size() == 1), true);		
				compChart.setNumTicks(10,10, 1,1);
				compChart.setBarWidth(3);

				compChart.setPreferredSize(new Dimension(400, 400));
				compChart.setBorder(new EmptyBorder(15, 0, 0, 0));
				
				bottomPanel = addComponent(compChart, bottomPanel);
			}
		} else {
			JPanel textPanel = new JPanel(new FlowLayout());
			textPanel.add(new JLabel("No data matches query"));
			panePanel.add(textPanel, BorderLayout.CENTER);
		}
	}
	
	private JPanel addComponent(JComponent newComponent, JPanel parent) {
		JPanel bottomHalf = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.add(newComponent, BorderLayout.NORTH);
		parent.add(bottomHalf, BorderLayout.CENTER);
		return bottomHalf;
	}
	
	public void updateModels(Collection collection) {
		firstCollectionModel.setupModelFromCollection(collection);
		secondCollectionModel.setupModelFromOtherModel(firstCollectionModel);
		
		for (int i = 0; i < numConditions; i++)
			conditionModel[i].setupModelFromOtherModel(firstCollectionModel);
	}
	
	public boolean containsCollection(Collection c) {
		return firstCollectionModel.getMatchingItem(c) != null;
	}
	
	public void selectCollection(Collection c) {
		firstSeq.setSelectedItem(firstCollectionModel.getMatchingItem(c));
		secondSeq.setSelectedIndex(0);

		for (int i = 0; i < numConditions; i++) {
			conditionSeq[i].setSelectedIndex(0);
			conditionComp[i].setSelectedIndex(0);
			conditionValue[i].setText("");
		}
		
		repaint();

		setupBottomPane();
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
