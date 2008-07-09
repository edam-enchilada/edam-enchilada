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
 * The Original Code is EDAM Enchilada's ClusterDialog class.
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
package gui;

import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
/**
 * Standard importation table for PALMS data files.
 * One column for .txt and varying row length
 * 
 * @author rzeszotj with credit to other table model authors
 *
 */
public class PALMSTableModel extends AbstractTableModel implements TableModelListener{

	private String[] columnNames;
	private ArrayList<ArrayList<Object>> rowData = new ArrayList<ArrayList<Object>>();
	private ArrayList<Object> newColumn = new ArrayList<Object>();
	private ArrayList<Object> row2 = new ArrayList<Object>();
	public int setCount;
	
	
	public PALMSTableModel() {
		super();
		
			setCount = 0;
			addTableModelListener(this);
			
			columnNames = new String[2]; 
			columnNames[0] = "#";
			columnNames[1] = "PALMS File";
			
			newColumn.add(new Integer(++setCount));
			newColumn.add(new String("PALMS file"));
			rowData.add(newColumn);
			
			row2.add(new Integer(++setCount));
			row2.add(new String(""));
			rowData.add(row2);
		
	}
	
	public String getColumnName(int col)
	{
		return columnNames[col];
	}
	
	public int getRowCount() 
	{
		return rowData.size();
	}
	
	public int getColumnCount()
	{
		return rowData.get(0).size();
	}
	
	public Object getValueAt(int row, int col)
	{
		return rowData.get(row).get(col);
	}
	
	public boolean isCellEditable(int row, int col)
	{
		if (col == 0)
			return false;
		else
			return true;
	}
	
	public void setValueAt(Object value, int row, int col)
	{
		rowData.get(row).set(col,value);
		fireTableCellUpdated(row,col);
	}
	
	
	public Class<?> getColumnClass(int c) 
			{
		return getValueAt(0,c).getClass();
			}
	
	
	public void tableChanged(TableModelEvent e)
	{ 
		if ((e.getLastRow() == rowData.size() - 1) && 
				(e.getType() == TableModelEvent.UPDATE) &&
				e.getColumn() == 1)
		{
			if (!((String)rowData.get(e.getLastRow()).get(1)).equals(""))
			{
				ArrayList<Object> newRow = new ArrayList<Object>(4);
				newRow.add(new Integer(++setCount));
				newRow.add("");
				rowData.add(newRow);
				fireTableRowsInserted(rowData.size()-1,rowData.size()-1);
			}
		}
		if ((e.getLastRow() == rowData.size() - 2) &&
				(e.getType() == TableModelEvent.UPDATE))
		{
			ArrayList<Object> lastRow = (ArrayList<Object>) rowData.get(rowData.size()-1);
			fireTableRowsUpdated(rowData.size()-1,rowData.size()-1);
		}
	
	
	}


}
