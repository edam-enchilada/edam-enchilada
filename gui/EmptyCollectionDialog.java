/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's EmptyCollectionDialog class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */


/*
 * Created on Aug 1, 2004
 */
package gui;

import javax.swing.*;

import database.SQLServerDatabase;

import java.awt.event.*;

/**
 * @author ritza
 */
public class EmptyCollectionDialog extends JDialog implements ActionListener 
{
	private JButton okButton;
	private JButton cancelButton;
	private JTextField nameField;
	private JTextField commentField;
	private JTextField datatypeField;
	private int collectionID;
	
	public EmptyCollectionDialog (JFrame parent) {
		super (parent,"Empty Collection", true);
		setDefaultLookAndFeelDecorated(true);
		setSize(400,200);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel namePanel = new JPanel();
		JLabel nameLabel = new JLabel("Name: ");
		nameField = new JTextField(25);
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		
		JPanel commentPanel = new JPanel();
		JLabel commentLabel = new JLabel("Comment: ");
		commentField = new JTextField(25);
		commentPanel.add(commentLabel);
		commentPanel.add(commentField);
		
		JPanel datatypePanel = new JPanel();
		JLabel datatypeLabel = new JLabel("Datatype: ");
		datatypeField = new JTextField(25);
		datatypePanel.add(datatypeLabel);
		datatypePanel.add(datatypeField);
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		JPanel mainPanel = new JPanel();
		mainPanel.add(namePanel);
		mainPanel.add(commentPanel);
		mainPanel.add(datatypePanel);
		mainPanel.add(buttonPanel);
		
		add(mainPanel);
		
		setVisible(true);	
	}
	
	/**
	 * Accessor method.
	 * 
	 * @return	collectionID for new empty collection
	 */
	public int getCollectionID(){
		
		return collectionID;
		
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == okButton) {
			SQLServerDatabase db = new SQLServerDatabase("localhost","1433","SpASMSdb");
			db.openConnection();
			collectionID = db.createEmptyCollection(datatypeField.getText(), 0,nameField.getText(),commentField.getText(),"");
			db.closeConnection();
			System.out.println("Empty Collection ID: " + collectionID);
			dispose();
		}			
		else  
			dispose();
}

}
