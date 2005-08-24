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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import database.DynamicTable;

public class DataFormatViewer extends JDialog implements ActionListener, ItemListener{
private JPanel cards;
private String[] nameArray;
private JButton okButton;
	
	public DataFormatViewer(Frame owner) {
		super(owner,"Data Format Viewer", true);
		
		setSize(650,300);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel nameAndBox = new JPanel();
		JLabel datatypeName = new JLabel("Datatype: ");
		ArrayList<String> names = MainFrame.db.getKnownDatatypes();
		nameArray = new String[names.size()];
		for (int i = 0; i < names.size(); i++)
			nameArray[i] = names.get(i);
		JComboBox comboBox = new JComboBox(nameArray);
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
		for (int i = 0; i < nameArray.length; i++) {		
			JPanel panel = new JPanel();
			JPanel dsi = new JPanel();
			JPanel ais = new JPanel();
			JPanel aid = new JPanel();
			JPanel center = new JPanel();
			dsi.setLayout(new BoxLayout(dsi, BoxLayout.PAGE_AXIS));
			ais.setLayout(new BoxLayout(ais, BoxLayout.PAGE_AXIS));
			aid.setLayout(new BoxLayout(aid, BoxLayout.PAGE_AXIS));
			
			ArrayList<ArrayList<String>> names = 
				MainFrame.db.getColNamesAndTypes(nameArray[i], DynamicTable.DataSetInfo);
			dsi.add(new JLabel("DataSetInfo Columns:"));
			dsi.add(new JLabel("				"));
			for (int j = 0; j < names.size(); j++)
				dsi.add(new JLabel(names.get(j).get(0) + " :  " + names.get(j).get(1)));
			
			names = MainFrame.db.getColNamesAndTypes(nameArray[i], DynamicTable.AtomInfoDense);
			aid.add(new JLabel("AtomInfoDense Columns:"));
			aid.add(new JLabel("				"));
			for (int j = 0; j < names.size(); j++)
				aid.add(new JLabel(names.get(j).get(0) + " :  " + names.get(j).get(1)));
			center.add(new JLabel("     "));
			center.add(aid);
			center.add(new JLabel("     "));
			
			names = MainFrame.db.getColNamesAndTypes(nameArray[i], DynamicTable.AtomInfoSparse);
			ais.add(new JLabel("AtomInfoSparse Columns:"));
			ais.add(new JLabel("				"));
			for (int j = 0; j < names.size(); j++)
				ais.add(new JLabel(names.get(j).get(0) + " :  " + names.get(j).get(1)));
			
			panel.setLayout(new BorderLayout());
			panel.add(dsi, BorderLayout.LINE_START);
			panel.add(center, BorderLayout.CENTER);
			panel.add(ais, BorderLayout.LINE_END);		
			cards.add(panel, nameArray[i]);
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, (String)evt.getItem());
		
	}

	public void actionPerformed(ActionEvent evt) {
		dispose();		
	}

}
