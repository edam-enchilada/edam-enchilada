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
 * The Original Code is EDAM Enchilada's ParTableModel class.
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
 *
 */
package gui;

import java.util.ArrayList;
import javax.swing.event.*;

import javax.swing.table.AbstractTableModel;

/**
 * @author andersbe
 *
 */
public class ParTableModel extends AbstractTableModel implements TableModelListener
{
	private String[] columnNames;
	private ArrayList<ArrayList<Object>> rowData = new ArrayList<ArrayList<Object>>();
	private ArrayList<Object> newColumn = new ArrayList<Object>();
	private ArrayList<Object> row2 = new ArrayList<Object>();
	int setCount;

	
	public ParTableModel(int numCols) {
		super();
		if (numCols == 8) {
			columnNames = new String[8]; 
			columnNames[0] = "#";
	columnNames[1] = "*.par";
	columnNames[2] = "mass cal"; 
	columnNames[3] = "size cal";
	columnNames[4] = "Min. Height"; 
	columnNames[5] = "Min. Area";
	columnNames[6] = "Min. Rel. Area";
	columnNames[7] = "Autocal";
			setCount = 0;
		addTableModelListener(this);
		newColumn.add(new Integer(++setCount));
		newColumn.add(new String(".par file"));
		newColumn.add(new String(".cal file"));
		newColumn.add(new String(".noz file"));
		newColumn.add(new Integer(0));
		newColumn.add(new Integer(0));
		newColumn.add(new Float(0.0));
		newColumn.add(new Boolean(true));
		rowData.add(newColumn);

		row2.add(new Integer(++setCount));
		row2.add(new String(""));
		row2.add(new String(""));
		row2.add(new String(""));
		row2.add(new Integer(0));
		row2.add(new Integer(0));
		row2.add(new Float(0.0));
		row2.add(new Boolean(true));
		rowData.add(row2);
		}
		else if (numCols == 2){
			setCount = 0;
			columnNames = new String[2];
			columnNames[0] = "#";
			columnNames[1] = "file";
			addTableModelListener(this);
			newColumn.add(new Integer(++setCount));
			newColumn.add(new String(".edsf or .esmf file"));
			rowData.add(newColumn);
			row2.add(new Integer(++setCount));
			row2.add(new String(""));
			rowData.add(newColumn);	
		}
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
	{ if (columnNames.length == 8) {
		if ((e.getLastRow() == rowData.size() - 1) && 
			(e.getType() == TableModelEvent.UPDATE) &&
			e.getColumn() == 1)
		{
			if (!((String)rowData.get(e.getLastRow()).get(1)).equals(""))
			{
				ArrayList<Object> newRow = new ArrayList<Object>(8);
				newRow.add(new Integer(++setCount));
				newRow.add(new String(""));
				// You don't need to use new statements here to create new
				// objects since Strings, Doubles and Booleans are immutable.
				// When a value in the table is changed, you are in fact 
				// creating a new instance of a String, Double or Boolean
				// and setting a single reference to point to the new instance
				// while all other references still point to the old instance. 
				newRow.add(((String) rowData.get(rowData.size()-1).get(2)));
				newRow.add(((String) rowData.get(rowData.size()-1).get(3)));
				newRow.add((Integer) rowData.get(rowData.size()-1).get(4));
				newRow.add((Integer) rowData.get(rowData.size()-1).get(5));
				newRow.add((Float) rowData.get(rowData.size()-1).get(6));
				newRow.add((Boolean) rowData.get(rowData.size()-1).get(7));
				rowData.add(newRow);
				fireTableRowsInserted(rowData.size()-1,rowData.size()-1);
			}
		}
		if ((e.getLastRow() == rowData.size() - 2) &&
			(e.getType() == TableModelEvent.UPDATE))
		{
			ArrayList<Object> lastRow = (ArrayList<Object>) rowData.get(rowData.size()-1);
			lastRow.set(2, (String) rowData.get(rowData.size()-2).get(2));
			lastRow.set(3, (String) rowData.get(rowData.size()-2).get(3));
			lastRow.set(4, (Integer) rowData.get(rowData.size()-2).get(4));
			lastRow.set(5, (Integer) rowData.get(rowData.size()-2).get(5));
			lastRow.set(6, (Float) rowData.get(rowData.size()-2).get(6));
			lastRow.set(7, (Boolean) rowData.get(rowData.size()-2).get(7));
			fireTableRowsUpdated(rowData.size()-1,rowData.size()-1);
		}
	}
	else if (columnNames.length == 2) {
		if ((e.getLastRow() == rowData.size() - 1) && 
				(e.getType() == TableModelEvent.UPDATE) &&
				e.getColumn() == 1)
			{
				if (!((String)rowData.get(e.getLastRow()).get(1)).equals(""))
				{
					ArrayList<Object> newRow = new ArrayList<Object>(8);
					newRow.add(new Integer(++setCount));
					newRow.add(new String(""));
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


}
