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
 * The Original Code is EDAM Enchilada's ExceptionDialog class.
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
 * Created on Aug 24, 2004
 *
 */
package gui;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author ritza
 *
 */
public class ExceptionDialog extends JDialog implements ActionListener{
	
	JButton button;
	
	/**
	 * Constructor.
	 * @param frame - parent JFrame
	 * @param message - error message
	 */
	public ExceptionDialog(JDialog frame, String[] message) {
		super(frame, "Error", true);
		
		//Make sure we have nice window decorations.
		setDefaultLookAndFeelDecorated(true);
		setSize(400,400);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JLabel label;
		for (int i=0;i<message.length;i++) {
			label = new JLabel(message[i]);
			panel.add(label);
		}
		button = new JButton("OK");
		button.addActionListener(this);
		panel.add(button);
		
		add(panel); // Add the panel to the dialogue box.
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		//Display the dialogue box.
		pack();
		setVisible(true);
	}
	
	/**
	 * ExceptionDialog without a title, to be used when no parent frame is known.
	 * 
	 * @param message - the message to be displayed.
	 */
	public ExceptionDialog(String[] message){
		super();
//		Make sure we have nice window decorations.
		setDefaultLookAndFeelDecorated(true);
		setSize(400,400);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JLabel label;
		for (int i=0;i<message.length;i++) {
			label = new JLabel(message[i]);
			panel.add(label);
		}
		button = new JButton("OK");
		button.addActionListener(this);
		panel.add(button);
		
		add(panel); // Add the panel to the dialogue box.
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		//Display the dialogue box.
		pack();
		setVisible(true);
	}
	
	public ExceptionDialog(String message){
		new ExceptionDialog(new String[]{message});
	}
	
	/**
	 * ExceptionDialog constructor to handle multiple error messages at the same
	 * time.
	 * 
	 * @param frame - the parent JFrame
	 * @param messages -  The messages you wish to have displayed.  Each String[]
	 * 						is a distinct message.
	 */
	public ExceptionDialog(JDialog frame, ArrayList<String[]> messages){
		super(frame, "Error", true);
		
		setDefaultLookAndFeelDecorated(true);
		setSize(400, 400);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JLabel label;
		// for each separate message, put it on a separate line
		for (String[] newMessage : messages){
			
			label = new JLabel();
			//insert all the info from the message
			String longMessage = "";
			for (int i=0; i<newMessage.length; i++)
				longMessage = longMessage.concat(newMessage[i]);
			
			//System.out.println(longMessage);
			label.setText(longMessage);
			panel.add(label);
			
		}
		button = new JButton("OK");
		button.addActionListener(this);
		panel.add(button);
		
		add(panel); // Add the panel to the dialogue box.
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		//Display the dialogue box.
		pack();
		setVisible(true);
	}
	
	/**
	 * Constructor.
	 * @param frame - parent JFrame.
	 * @param message - error message.
	 */
	public ExceptionDialog(JDialog frame, String message){
		String[] array = {message};
		new ExceptionDialog(frame, array);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == button) 
			dispose();			
	}
}


