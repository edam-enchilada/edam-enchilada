package gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.*;

import dataImporters.EnchiladaDataSetImporter;
import database.DynamicTable;
import database.SQLServerDatabase;

public class ClusteringSpecifications extends JDialog implements ActionListener, ItemListener{

	private JButton okButton;
	private JButton setButton;
	private JButton resetButton;
	private JButton cancelButton;
	private JPanel clusteringInfo;
	private String[] nameArray;
	private JComboBox comboBox;
	private JPanel cards;
	
	public ClusteringSpecifications(Frame owner) {
		super(owner, "Clustering Specifications", true);
		setSize(350,400);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JLabel datatypeName = new JLabel("Datatype: ");
		ArrayList<String> names = MainFrame.db.getKnownDatatypes();
		nameArray = new String[names.size()];
		for (int i = 0; i < names.size(); i++)
			nameArray[i] = names.get(i);
		comboBox = new JComboBox(nameArray);
		comboBox.addItemListener(this);

		makeCards();

		okButton = new JButton("OK");
		setButton = new JButton("Set");
		resetButton = new JButton("Reset");
		cancelButton = new JButton("Cancel");
		
		add(datatypeName);
		add(comboBox);
		add(new JLabel("                                  " +
				"                                    "));
		add(cards);
		add(new JLabel("                                  " +
		"                                                            "));
		add(okButton);
		add(cancelButton);
		
		setLayout(new FlowLayout());
		setVisible(true);
	}
	
	public void makeCards() {
		cards = new JPanel(new CardLayout());
		boolean grouped = true;
		for (int i = 0; i < nameArray.length; i++) {
			ArrayList<JRadioButton> buttons = getColumnNames(nameArray[i]);
			JPanel panel = new JPanel();
			JPanel keyPanel = new JPanel();
			JPanel valuePanel = new JPanel();
			JPanel weightPanel = new JPanel();
			
			keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.PAGE_AXIS));		
			keyPanel.add(new JLabel("Choose one unique key"));
			ButtonGroup keyGroup = new ButtonGroup();
			for (int j = 0; j < buttons.size(); j++) {
				keyGroup.add(buttons.get(j));
				keyPanel.add(buttons.get(j));
			}
			JRadioButton auto = new JRadioButton("Automatic");
			keyGroup.add(auto);
			keyPanel.add(auto);
			
			panel.add(keyPanel);
			panel.add(valuePanel);
			panel.add(weightPanel);
			cards.add(panel, nameArray[i]);
		}
	}
		
	public ArrayList<JRadioButton> getColumnNames(String datatype) {
		ArrayList<JRadioButton> buttonsToCheck = new ArrayList<JRadioButton>();
		// set up dense info
		ArrayList<ArrayList<String>> namesAndTypes = 
			MainFrame.db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoDense);
		for (int i = 0; i < namesAndTypes.size(); i++) {
			if ((namesAndTypes.get(i).get(1).equals("INT") || 
					namesAndTypes.get(i).get(1).equals("REAL")) && 
					!namesAndTypes.get(i).get(0).equals("AtomID")) 
			buttonsToCheck.add(new JRadioButton(namesAndTypes.get(i).get(0) + 
					": " + namesAndTypes.get(i).get(1) + "  (dense)"));
		}
		
		// set up sparseInfo
		namesAndTypes = MainFrame.db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoSparse);
		for (int i = 0; i < namesAndTypes.size(); i++) {
			if ((namesAndTypes.get(i).get(1).equals("INT") || 
					namesAndTypes.get(i).get(1).equals("REAL")) && 
					!namesAndTypes.get(i).get(0).equals("AtomID"))
			buttonsToCheck.add(new JRadioButton(namesAndTypes.get(i).get(0) + 
					": " + namesAndTypes.get(i).get(1) + "  (sparse)"));
		}
		return buttonsToCheck;
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == okButton) {
			dispose();
		}
		else if (source == cancelButton)
			dispose();
		else if (source == setButton)
			dispose();
		else if (source == resetButton)
			dispose();
	}


	public void itemStateChanged(ItemEvent evt) {
	    CardLayout cl = (CardLayout)(cards.getLayout());
	    cl.show(cards, (String)evt.getItem());
	}

}
