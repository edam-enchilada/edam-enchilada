package gui;

import chartlib.*;
import collection.Collection;
import database.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
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
	
	private JComboBox[] conditionSeq, conditionComp, conditionType,	conditionSeq2;

	private SyncCollectionModel[] conditionModel, conditionModel2;
	private JTextField[] conditionValue;
	private JComboBox[] booleanOps;
	private ArrayList<Hashtable<Date,Double>> data;
	private Dataset[] datasets;
	private Dataset[] scatterplotData;

	private int numConditions = 2;

	private JPanel topPanel;
	private ZoomableChart zchart;
	private JButton zoomOutButton;
	private double xMin, xMax;

	private static String[] comparators = { "", " <", " >", " <=", " >=", " =", " <>" };
	private static String[] comptypes = { " VAL: ", " SEQ: " };
	private static String[] booleans = { " AND", " OR" };

	public SyncAnalyzePanel(MainFrame parentFrame, InfoWarehouse db,
			CollectionTree tree, Collection collectionToBaseOn) { 
		
		super(new BorderLayout());

		this.parentFrame = parentFrame;
		this.db = db;
		this.tree = tree;

		firstCollectionModel = new SyncCollectionModel(collectionToBaseOn);
		secondCollectionModel = new SyncCollectionModel(firstCollectionModel);

		topPanel = new JPanel(new BorderLayout());
		JPanel seqAndZoom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		seqAndZoom.add(new JLabel("1st Sequence: "));
		seqAndZoom.add(firstSeq = new JComboBox(firstCollectionModel));
		seqAndZoom.add(new JLabel("              2nd Sequence: "));
		seqAndZoom.add(secondSeq = new JComboBox(secondCollectionModel));
		
		zoomOutButton = new JButton("Zoom Out");
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zchart.zoom(xMin, xMax);
			}
		});
		
		seqAndZoom.add(zoomOutButton);

		JButton exportToCSV, refresh;
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
				exportDataToCSV();
			}
		});

		JPanel bottomHalf = new JPanel(new BorderLayout());
		bottomHalf.add(buildConditionPanels(firstCollectionModel), BorderLayout.WEST);
		bottomHalf.add(buttonPanel, BorderLayout.CENTER);

		topPanel.add(seqAndZoom, BorderLayout.NORTH);
		topPanel.add(bottomHalf, BorderLayout.CENTER);

		bottomPane = new JScrollPane();
		setupBottomPane();
		bottomPane.getVerticalScrollBar().setUnitIncrement(10);

		add(topPanel, BorderLayout.NORTH);
		add(bottomPane, BorderLayout.CENTER);

		validate();
	}

	private JPanel buildConditionPanels(SyncCollectionModel basisModel) {
		conditionSeq = new JComboBox[numConditions];
		conditionComp = new JComboBox[numConditions];
		conditionType = new JComboBox[numConditions];
		conditionSeq2 = new JComboBox[numConditions];
		conditionModel = new SyncCollectionModel[numConditions];
		conditionModel2 = new SyncCollectionModel[numConditions];
		conditionValue = new JTextField[numConditions];
		booleanOps = new JComboBox[numConditions - 1];

		final JPanel conditionsPanel = new JPanel(new GridLayout(numConditions, 1, 5, 5));

		for (int i = 0; i < numConditions; i++) {
			conditionModel[i] = new SyncCollectionModel(basisModel);
			conditionModel2[i] = new SyncCollectionModel(basisModel);

			final JPanel thisCondPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JComboBox condType = new JComboBox(comptypes);
			final JComboBox condSeq2 = new JComboBox(conditionModel2[i]);
			final JTextField condVal = new JTextField(10);

			conditionSeq2[i] = condSeq2;

			if (i > 0) {
				thisCondPanel.add(booleanOps[i - 1] = new JComboBox(booleans));
			} else {
				JPanel p = new JPanel();
				p.setPreferredSize(new JComboBox(booleans).getPreferredSize());
				thisCondPanel.add(p);
			}

			thisCondPanel.add(new JLabel(" Condition " + (i + 1) + ": "));
			thisCondPanel
					.add(conditionSeq[i] = new JComboBox(conditionModel[i]));
			thisCondPanel.add(conditionComp[i] = new JComboBox(comparators));
			thisCondPanel.add(conditionType[i] = condType);
			thisCondPanel.add(conditionValue[i] = condVal);

			condType.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					thisCondPanel.remove(5);
					if (condType.getSelectedIndex() == 0)
						thisCondPanel.add(condVal);
					else
						thisCondPanel.add(condSeq2);

					topPanel.validate();
					topPanel.repaint();
				}
			});

			conditionSeq[i].setPreferredSize(new Dimension((int) conditionSeq[i].getPreferredSize().getWidth(), 20));
			conditionComp[i].setPreferredSize(new Dimension((int) conditionComp[i].getPreferredSize().getWidth(), 20));
			conditionsPanel.add(thisCondPanel);
		}

		return conditionsPanel;
	}

	public void setupBottomPane() {
		JPanel panePanel = new JPanel(new BorderLayout());
		bottomPane.setViewportView(panePanel);

		Collection seq1 = (Collection) firstSeq.getSelectedItem();
		Collection seq2 = (Collection) secondSeq.getSelectedItem();

		ArrayList<String> conditionStrs = new ArrayList<String>();
		ArrayList<Collection> condCollections = new ArrayList<Collection>();

		if (seq1 != null) {
			for (int i = 0; i < numConditions; i++) {
				Collection condColl = (Collection) conditionSeq[i].getSelectedItem();
				
				boolean compareAgainstValue = (conditionType[i].getSelectedIndex() == 0);
				
				String condVal = conditionValue[i].getText().trim();
				Collection compareColl = (Collection) conditionSeq2[i].getSelectedItem();
				int curIndex = condCollections.size();

				if (condColl != null && conditionComp[i].getSelectedIndex() > 0
						&& (compareAgainstValue && !condVal.equals("") || !compareAgainstValue && compareColl != null)) {
					String condStr = "";

					if (i > 0 && curIndex > 0)
						condStr = booleanOps[i - 1].getSelectedItem() + " ";

					condStr += "C" + curIndex + ".Value "
							+ conditionComp[i].getSelectedItem() + " ";

					condCollections.add(condColl);
					
					if (compareAgainstValue)
						condStr += condVal;
					else {
						condStr += "C" + (++curIndex) + ".Value ";
						condCollections.add(compareColl);
					}
					
					conditionStrs.add(condStr);
				}
			}
			
			data = new ArrayList<Hashtable<Date,Double>>();
			
			data.add(db.getConditionalTSCollectionData(seq1, condCollections, conditionStrs));
			if(seq2!=null)
				data.add(db.getConditionalTSCollectionData(seq2, condCollections, conditionStrs));
		}

		if (data.size() > 0 && data.get(0).size() > 0) {
			int numSequences = (seq2 != null ? 2 : 1);
			
			datasets = new Dataset[numSequences];
			for (int i = 0; i < numSequences; i++)
				datasets[i] = new Dataset();
			scatterplotData = new Dataset[numSequences];
			for(int i=0;i<numSequences;i++){
				scatterplotData[i] = new Dataset();
			}
			
			ArrayList<ArrayList<Date>> dateSet = new ArrayList<ArrayList<Date>>();
			for(int i=0;i<data.size();i++){
				dateSet.add(new ArrayList<Date>(data.get(i).keySet()));
				Collections.sort(dateSet.get(i));
			}

			// This casting longs to doubles could come back to bite
			// me in the ass... but it's the only way to shove both
			// dates and regular data into the x-axis of a datapoint...
			double lastTimePoint = 0;
			double maxValue[] = new double[numSequences];
			double startTime = (double) dateSet.get(0).get(0).getTime();
			for(int i=0;i<numSequences;i++){
				if(startTime > (double) dateSet.get(i).get(0).getTime())
					startTime = (double) dateSet.get(i).get(0).getTime();
				for (Date d : dateSet.get(i)) {
					lastTimePoint = (double) d.getTime();
					double value = data.get(i).get(d);
					
					if (value > maxValue[i])
						maxValue[i] = value;
					
					datasets[i].add(new DataPoint(lastTimePoint, value));
					
				}
			}
			for (Date d : dateSet.get(0)) {
				boolean plottable = true;
				for (int i = 1; i < numSequences && plottable; i++) {
					plottable = (data.get(i).get(d) != null);
				}
				if (plottable) {
					for (int i = 0; i < numSequences; i++) {
						lastTimePoint = (double) d.getTime();
						double value = data.get(i).get(d);
						scatterplotData[i].add(new DataPoint(lastTimePoint, value));
					}
				}
			}
			xMin = startTime - 1000;
			xMax = lastTimePoint + 1000;
			
			for (int i = 0; i < numSequences; i++) {
				if (maxValue[i] <= 0)
					maxValue[i] = 10;
			}

			// sets up chart
			TimeSeriesPlot chart = new TimeSeriesPlot(datasets);
			chart.setTitle("<html><b>Time Series Comparison</b></html>");

			for (int i = 0; i < numSequences; i++) {
				chart.setAxisBounds(i, xMin, xMax, 0, maxValue[i]);
			}
			chart.repaint();

			zchart = new ZoomableChart(chart);
			zchart.setFocusable(true);
			zchart.setCScrollMin(xMin);
			zchart.setCScrollMax(xMax);
			
			// Set up comparison charts
			JPanel bottomPanel = addComponent(zchart, panePanel);

			int dataSetIndex = numSequences;
			
			/*for (int i = 0; dataSetIndex < numSequences; i++) {
				Collection condColl = (Collection) conditionSeq[i].getSelectedItem();
				Collection compareColl = (Collection) conditionSeq2[i].getSelectedItem();
				
				boolean compareAgainstValue = (conditionType[i].getSelectedIndex() == 0);
				
				double trueMax = maxValue[dataSetIndex];
				Chart compChart;
				if (compareAgainstValue) 
					compChart = new Chart(1, false);
				else {
					compChart = new Chart(2, true);
					trueMax = Math.max(trueMax, maxValue[dataSetIndex + 1]);
				}

				compChart.setHasKey(false);
				compChart.setTitle("<html><b>Condition Series "	+ (i + 1) + "</b></html>");
				compChart.setTitleX(0, "Time");
				compChart.setTitleY(0, "Condition Series " + (i + 1) + " Value");
				compChart.setColor(0, Color.green);
				compChart.setAxisBounds(0, xMin, xMax, 0, trueMax);
				compChart.setDataset(0, datasets[dataSetIndex]);
				compChart.setDataDisplayType((datasets[dataSetIndex++].size() == 1), true);
				compChart.setNumTicks(10, 10, 1, 1);
				compChart.setBarWidth(3);
				compChart.drawXAxisAsDateTime(0);
				
				if (!compareAgainstValue) {
					compChart.setTitleY(1, "Condition Series " + (i + 1) + " Comparison Value");
					compChart.setColor(1, Color.magenta);
					compChart.setAxisBounds(1, xMin, xMax, 0, trueMax);
					compChart.setDataset(1, datasets[dataSetIndex++]);
				}

				compChart.setPreferredSize(new Dimension(400, 400));
				compChart.setBorder(new EmptyBorder(15, 0, 0, 0));

				bottomPanel = addComponent(compChart, bottomPanel);
			}*/

			if (numSequences > 1) {
				Chart scatterChart = new ScatterPlot(scatterplotData[0], scatterplotData[1]);
				scatterChart.setTitle("<html><b>Time Series Scatter Plot -- R^2: %10.5f</b></html>");
				scatterChart.setAxisBounds(0, xMin,	xMax, 0, maxValue[0]);
				scatterChart.setAxisBounds(1, xMin,	xMax, 0, maxValue[1]);
				
				bottomPanel = addComponent(scatterChart, bottomPanel);
			}
		} else {
			JPanel textPanel = new JPanel(new FlowLayout());
			textPanel.add(new JLabel("No data matches query"));
			panePanel.add(textPanel, BorderLayout.CENTER);
		}
	}

	private void exportDataToCSV() {
		// Rebuild data, and show new graph before exporting...
		setupBottomPane();
		ArrayList<Date> dateSet;
		//dateSet = new ArrayList<Date>(data.keySet());
		dateSet = db.getCollectionDates((Collection) firstSeq.getSelectedItem(),
				(Collection) secondSeq.getSelectedItem());
		Collections.sort(dateSet);

		try {
			JFileChooser fc = new JFileChooser("Choose Output File");
			int result = fc.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				SimpleDateFormat dformat = new SimpleDateFormat(
						"M/d/yyyy hh:mm:ss a");
				File f = fc.getSelectedFile();
				PrintWriter fWriter = new PrintWriter(f);

				Collection seq1 = (Collection) firstSeq.getSelectedItem();
				Collection seq2 = (Collection) secondSeq.getSelectedItem();
				int numCollections = 1;
				if (seq2 != null){
					fWriter.println("R^2: "	+ datasets[0].getCorrelationStats(datasets[1]).r2);
					numCollections++;
				}
				String condString = "";
				for (int i = 0; i < numConditions; i++) {
					Collection condColl = (Collection) conditionSeq[i].getSelectedItem();

					boolean compareAgainstValue = (conditionType[i].getSelectedIndex() == 0);
					
					String condVal = conditionValue[i].getText().trim();
					Collection compareColl = (Collection) conditionSeq2[i].getSelectedItem();

					if (condColl != null && conditionComp[i].getSelectedIndex() > 0
							&& (compareAgainstValue && !condVal.equals("") || !compareAgainstValue && compareColl != null)) {
					
						if (condString.length() > 0)
							condString += booleanOps[i - 1].getSelectedItem()
									+ " ";

						condString += condColl + " "
								+ conditionComp[i].getSelectedItem() + " ";
						
						if (compareAgainstValue)
							condString += condVal;
						else
							condString += compareColl;
					}
				}
				
				if (condString.length() == 0)
					condString = "None";
				else
					condString = "\"" + condString + "\"";

				fWriter.println("Condition applied: " + condString);

				String line1 = "";
				String line2 = "Date";
				if (seq1 != null) {
					line1 += ",Sequence 1";
					line2 += ",\"" + seq1.getName().replace("\"", "\"\"") + "\"";
				}
				
				if (seq2 != null) {
					line1 += ",Sequence 2";
					line2 += ",\"" + seq2.getName().replace("\"", "\"\"") + "\"";
				}
				
				int index = 1;
				for (int i = 0; i < numConditions; i++) {
					Collection condColl = (Collection) conditionSeq[i].getSelectedItem();
					if (condColl != null) {
						line1 += ",Condition " + index;
						line2 += ",\"" + condColl.getName().replace("\"", "\"\"") + "\"";
						
						Collection compareColl = (Collection) conditionSeq2[i].getSelectedItem();
						if (compareColl != null) {
							line1 += ",Condition " + index + " Comparison";
							line2 += ",\"" + compareColl.getName().replace("\"", "\"\"") + "\"";
						}
						
						index++;
					}
				}
				fWriter.println(line1);
				fWriter.println(line2);
				for (Date d : dateSet) {
					fWriter.print(dformat.format(d));
					
					for(Hashtable<Date,Double> table : data){
						Double value = table.get(d);
						String stringVal = "";
						if(value != null) stringVal = ""+value;
						fWriter.print("," + stringVal);
					}
					fWriter.println();
				}
				fWriter.close();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error! File not found!");
			e.printStackTrace();
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
			ArrayList<Collection> allCollectionsInTree = tree
					.getCollectionsInTreeOrderFromRoot(1, collectionToBaseOn);
			ArrayList<Integer> collectionIDs = new ArrayList<Integer>();

			for (int i = 0; i < allCollectionsInTree.size(); i++)
				collectionIDs
						.add(allCollectionsInTree.get(i).getCollectionID());

			collectionIDs = db.getCollectionIDsWithAtoms(collectionIDs);

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

		public Object getSelectedItem() {
			return selectedItem;
		}

		public void setSelectedItem(Object item) {
			selectedItem = (Collection) item;
		}

		public int getSize() {
			return collections.length;
		}

		public Object getElementAt(int index) {
			return collections[index];
		}

		public void addListDataListener(ListDataListener l) {
		}

		public void removeListDataListener(ListDataListener l) {
		}
	}
}
