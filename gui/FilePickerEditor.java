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
 * The Original Code is EDAM Enchilada's FilePickerEditor class.
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
 * Created on Jul 19, 2004
 */
package gui;

import javax.swing.AbstractCellEditor;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.event.*;
import java.awt.Color;
import java.util.ArrayList;




/**
 * @author andersbe
 *
 */
public class FilePickerEditor extends AbstractCellEditor 
							  implements TableCellEditor, ActionListener
{
	private String filename;
	private String oldFilename;
	private JFileChooser fileChooser;
	private Component parent;
	private JButton button;

	
	protected static final String EDIT = "edit";
	/**
	 * 
	 * @param filetype
	 * @param title
	 */
	public FilePickerEditor(ArrayList<String> filetypes, 
							String title, 
							Component pDialog) 
	{
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		CustomFileFilter customFilter = new CustomFileFilter();
		for (int i = 0; i < filetypes.size(); i++)
			customFilter.addFileFilter(filetypes.get(i));
		
		fileChooser.setFileFilter(customFilter);

		button = new JButton(oldFilename);
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);
		button.setBackground(Color.WHITE);
		
		parent = pDialog;
	}
	
	public FilePickerEditor(String filetype, 
			String title, 
			Component pDialog) 
{
fileChooser = new JFileChooser();
fileChooser.setDialogTitle(title);
fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
CustomFileFilter customFilter = new CustomFileFilter();
customFilter.addFileFilter(filetype);
fileChooser.setFileFilter(customFilter);

button = new JButton(oldFilename);
button.setActionCommand(EDIT);
button.addActionListener(this);
button.setBorderPainted(false);
button.setBackground(Color.WHITE);

parent = pDialog;
}
	
	public void actionPerformed(ActionEvent e) 
	{
		int returnVal = JFileChooser.CANCEL_OPTION;
		if (EDIT.equals(e.getActionCommand()))
		{
			//The user has clicked on the cell, so bring up the dialog
			returnVal = fileChooser.showOpenDialog(parent);
			fileChooser.setVisible(true);
			
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				filename = fileChooser.getSelectedFile().getAbsolutePath();
			}
			else
				filename = oldFilename;
			
			fireEditingStopped();
		}

	}
	
	public Object getCellEditorValue()
	{
		return filename;
	}
	
	public Component getTableCellEditorComponent(JTable table,
												 Object value,
												 boolean isSelected,
												 int row,
												 int column)
	{
		oldFilename = (String) value;
		return button;
	}

}
