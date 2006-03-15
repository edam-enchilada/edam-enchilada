package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import database.DynamicTable;
import errorframework.*;

public class DataFormatViewer extends JDialog implements ActionListener, ItemListener{
	private JPanel cards;
	private String[] dataTypes;
	private JButton okButton;
	private Set<String> indexedColumns;
	
	public DataFormatViewer(Frame owner) {
		super(owner,"Data Format Viewer", true);
		
		setSize(650,300);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel nameAndBox = new JPanel();
		JLabel datatypeName = new JLabel("Datatype: ");
		ArrayList<String> names = MainFrame.db.getKnownDatatypes();
		dataTypes = new String[names.size()];
		for (int i = 0; i < names.size(); i++)
			dataTypes[i] = names.get(i);
		JComboBox comboBox = new JComboBox(dataTypes);
		comboBox.addItemListener(this);
		nameAndBox.add(datatypeName);
		nameAndBox.add(comboBox);
		
		cards = new JPanel(new CardLayout());
		makeCards();
		
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		add(nameAndBox);
		add(cards);
		add(okButton);
		layout.putConstraint(SpringLayout.WEST, nameAndBox, 10, SpringLayout.WEST, getContentPane());
		layout.putConstraint(SpringLayout.NORTH, nameAndBox, 10, SpringLayout.NORTH, getContentPane());
		layout.putConstraint(SpringLayout.WEST, cards, 20, SpringLayout.WEST, getContentPane());
		layout.putConstraint(SpringLayout.NORTH, cards, 10, SpringLayout.SOUTH, nameAndBox);
		layout.putConstraint(SpringLayout.EAST, okButton, -20, SpringLayout.EAST, getContentPane());
		layout.putConstraint(SpringLayout.SOUTH, okButton, -20, SpringLayout.SOUTH, getContentPane());
		
		
		setVisible(true);
	}

	public void makeCards() {
		boolean grouped = true;
		for (int i = 0; i < dataTypes.length; i++) {		
			JPanel panel = new JPanel();
			JPanel dsi = new JPanel();
			JPanel ais = new JPanel();
			JPanel aid = new JPanel();
			JPanel center = new JPanel();
			dsi.setLayout(new BoxLayout(dsi, BoxLayout.PAGE_AXIS));
			ais.setLayout(new BoxLayout(ais, BoxLayout.PAGE_AXIS));
			aid.setLayout(new BoxLayout(aid, BoxLayout.PAGE_AXIS));
			
			try {
				indexedColumns = MainFrame.db.getIndexedColumns(dataTypes[i]);
			} catch (Exception e) {
				ErrorLogger.writeExceptionToLog("DataFormatViewer","Error finding which columns are indexed!");
			}
			
			ArrayList<ArrayList<String>> namesAndTypes = 
				MainFrame.db.getColNamesAndTypes(dataTypes[i], DynamicTable.DataSetInfo);
			dsi.add(new JLabel("DataSetInfo Columns:"));
			dsi.add(new JLabel("				"));
			for (int j = 0; j < namesAndTypes.size(); j++)
				dsi.add(new JLabel(namesAndTypes.get(j).get(0) + " :  " + namesAndTypes.get(j).get(1)));
			
			namesAndTypes = MainFrame.db.getColNamesAndTypes(dataTypes[i], DynamicTable.AtomInfoDense);
			aid.add(new JLabel("AtomInfoDense Columns:"));
			aid.add(new JLabel("				"));
			for (int j = 0; j < namesAndTypes.size(); j++) {
				Box col = Box.createHorizontalBox();
				
				col.add(new JLabel(namesAndTypes.get(j).get(0) + " :  " + namesAndTypes.get(j).get(1)));
				col.add(new CreateIndexButton(dataTypes[i], namesAndTypes.get(j).get(0), 
						! indexedColumns.contains(namesAndTypes.get(j).get(0))));
				aid.add(col);
			}
			center.add(new JLabel("     "));
			center.add(aid);
			center.add(new JLabel("     "));
			
			namesAndTypes = MainFrame.db.getColNamesAndTypes(dataTypes[i], DynamicTable.AtomInfoSparse);
			ais.add(new JLabel("AtomInfoSparse Columns:"));
			ais.add(new JLabel("				"));
			for (int j = 0; j < namesAndTypes.size(); j++)
				ais.add(new JLabel(namesAndTypes.get(j).get(0) + " :  " + namesAndTypes.get(j).get(1)));
			
			panel.setLayout(new BorderLayout());
			panel.add(dsi, BorderLayout.LINE_START);
			panel.add(center, BorderLayout.CENTER);
			panel.add(ais, BorderLayout.LINE_END);		
			cards.add(panel, dataTypes[i]);
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, (String)evt.getItem());
		
	}

	public void actionPerformed(ActionEvent evt) {
		dispose();		
	}

	
	private class CreateIndexButton extends JButton implements ActionListener {
		private String dataType;
		private String column;
		
		public CreateIndexButton(String dataType, String column, boolean enabled) {
			this.setText("Create Index");
			if (! enabled) {
				this.setEnabled(false);
			} else {
				this.setEnabled(true);
				this.dataType = dataType;
				this.column = column;
				this.setActionCommand("click");
				this.addActionListener(this);
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("click")) {
				int n = JOptionPane.showConfirmDialog(
					    this,
					    "Indexes can speed up particular queries, but they can\n"
					    + "also terribly slow down importing and modifying data.\n"
					    + "Read up a bit on how they work before you make one.\n\n"
					    + "Are you sure you want to make an index?",
					    "Really create index?",
					    JOptionPane.YES_NO_OPTION);
				
				if (n == 0) { // 0 is "yes", surprisingly
					if (MainFrame.db.createIndex(dataType, column)) {
						this.setEnabled(false);
						return;
					} else {
						ErrorLogger.writeExceptionToLog("DataFormatViewer","Somehow, we could not create an index!");
					}
				}
			}
		}
	}
	
}
