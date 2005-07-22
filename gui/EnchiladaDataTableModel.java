package gui;

import java.util.ArrayList;

import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

/**
 * EnchiladaDataTableModel collects and holds information from the user for
 * importing a set of Enchilada Data.  (Ideally, we create an abstract
 * ImportTableModel class, with the getSuchAndSuch() methods in there, then have
 * both EnchiladaDataTableModel and ParTableModel extend that class, so all that
 * would initially differ would be their constructors, and maybe how they handle
 * events.)
 * @author steinbel
 * 6.23.05
 */

public class EnchiladaDataTableModel extends AbstractTableModel
	implements TableModelListener{

	private String[] columnNames = new String[2];
	private ArrayList<ArrayList<Object>> rowData = 
		new ArrayList<ArrayList<Object>>();
	private ArrayList<Object> newColumn = new ArrayList<Object>();
	private ArrayList<Object> row2 = new ArrayList<Object>();
	public int setCount;
	
	/**
	 * Creates an EnchiladaDataTableModel of two columns and one initial row.
	 *
	 */
	public EnchiladaDataTableModel(){
		
			addTableModelListener(this);
			columnNames[0] = "#";
			columnNames[1] = "file";
			newColumn.add(new Integer(++setCount));
			newColumn.add(".ed file");
			rowData.add(newColumn);
			
	}
	
	public int getRowCount() {
		
		return rowData.size();
	
	}

	public int getColumnCount() {
		
		return rowData.get(0).size();
	
	}

	public Object getValueAt(int row, int col){
		
		return rowData.get(row).get(col);
	
	}
	
	//User can't change the number of files being imported (it is auto-generated)
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
	
	public Class<?> getColumnClass(int c) {

		return getValueAt(0,c).getClass();
	
	}

	public void tableChanged(TableModelEvent e) {
		
		//If the user has changed the last row, and the filename isn't blank,
		//create a new row at the end of the table
		if ( (e.getLastRow() == rowData.size() - 1) && 
			(e.getType() == TableModelEvent.UPDATE) ){
			
			//if valid input has been entered in the last row, add a new row
			if ( !((String)rowData.get(e.getLastRow()).get(1)).equals("") && 
					!((String)rowData.get(e.getLastRow()).get(1)).equals(".ed file")){
				
				ArrayList<Object> newRow = new ArrayList<Object>(2);
				newRow.add(new Integer(++setCount));
				newRow.add(new String(""));
				rowData.add(newRow);
				fireTableRowsInserted(rowData.size()-1,rowData.size()-1);
				
			}
		}

		if ((e.getLastRow() == rowData.size() - 2) &&
		 (e.getType() == TableModelEvent.UPDATE) ){
			
			ArrayList<Object> lastRow = 
				(ArrayList<Object>) rowData.get(rowData.size()-1);
			fireTableRowsUpdated(rowData.size()-1,rowData.size()-1);
		
		}
			
	}
	
}