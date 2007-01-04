package gui;

/**
 * A GUI to look at what fields a data format has, and also build indexes on
 * particular columns that you plan to run queries on.
 * 
 * @author smitht
 * @author ritza?
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import database.Database;
import database.DynamicTable;
import errorframework.*;

public class DataFormatViewer extends JDialog implements ActionListener, ItemListener{
	private JPanel cards;
	private String[] dataTypes;
	private JButton okButton;
	private Set<String> indexedColumns;
	
	public DataFormatViewer(Frame owner) {
		super(owner,"Data Format Viewer", true);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel nameAndBox = new JPanel();
		nameAndBox.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
		
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.LINE_AXIS));
		bottomPane.add(Box.createHorizontalGlue());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		bottomPane.add(okButton);
		
		setLayout(new BorderLayout());
		JPanel all = new JPanel(new BorderLayout());
		all.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		all.add(nameAndBox, BorderLayout.NORTH);
		all.add(cards, BorderLayout.CENTER);
		all.add(bottomPane, BorderLayout.SOUTH);
		add(all, BorderLayout.CENTER);

		pack();
		
		setVisible(true);
	}

	public void makeCards() {
		boolean grouped = true;
		for (int i = 0; i < dataTypes.length; i++) {		
			JPanel panel = new JPanel();
			JPanel dsi = new JPanel();
			JPanel ais = new JPanel();
			JPanel aid = new JPanel();
			dsi.setLayout(new BoxLayout(dsi, BoxLayout.PAGE_AXIS));
			ais.setLayout(new BoxLayout(ais, BoxLayout.PAGE_AXIS));
			aid.setLayout(new BoxLayout(aid, BoxLayout.PAGE_AXIS));
			
			try {
				indexedColumns = ((Database)MainFrame.db).getIndexedColumns(dataTypes[i]);
			} catch (Exception e) {
				ErrorLogger.writeExceptionToLogAndPrompt("DataFormatViewer","Error finding which columns are indexed!");
			}
			
			ArrayList<ArrayList<String>> namesAndTypes = 
				MainFrame.db.getColNamesAndTypes(dataTypes[i], DynamicTable.DataSetInfo);
			dsi.setBorder(BorderFactory.createTitledBorder("DataSetInfo Columns"));
			for (int j = 0; j < namesAndTypes.size(); j++)
				dsi.add(new JLabel(namesAndTypes.get(j).get(0) + " :  " + namesAndTypes.get(j).get(1)));
			
			namesAndTypes = MainFrame.db.getColNamesAndTypes(dataTypes[i], DynamicTable.AtomInfoDense);
			aid.setBorder(BorderFactory.createTitledBorder("AtomInfoDense Columns"));
			for (int j = 0; j < namesAndTypes.size(); j++) {
				JPanel col = new JPanel();
				col.setLayout(new BoxLayout(col, BoxLayout.X_AXIS));
				col.add(new JLabel(namesAndTypes.get(j).get(0) + " :  " + namesAndTypes.get(j).get(1)));
				col.add(Box.createHorizontalGlue());
				col.add(new CreateIndexButton(dataTypes[i], namesAndTypes.get(j).get(0), 
						! indexedColumns.contains(namesAndTypes.get(j).get(0))));
				aid.add(col);
			}
			
			namesAndTypes = MainFrame.db.getColNamesAndTypes(dataTypes[i], DynamicTable.AtomInfoSparse);
			ais.setBorder(BorderFactory.createTitledBorder("AtomInfoSparse Columns"));
			for (int j = 0; j < namesAndTypes.size(); j++)
				ais.add(new JLabel(namesAndTypes.get(j).get(0) + " :  " + namesAndTypes.get(j).get(1)));

			panel.setLayout(new GridLayout(1, 3));
			panel.add(dsi);
			panel.add(aid);
			panel.add(ais);
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
			this.setFont(this.getFont().deriveFont(this.getFont().getSize2D() * 0.9f));
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
				
				if (n == JOptionPane.YES_OPTION) {
					if (((Database)MainFrame.db).createIndex(dataType, column)) {
						this.setEnabled(false);
						return;
					} else {
						ErrorLogger.writeExceptionToLogAndPrompt("DataFormatViewer","Somehow, we could not create an index!");
					}
				}
			}
		}
	}
	
}
