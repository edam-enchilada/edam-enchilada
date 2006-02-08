package dataImporters;

public interface ParTable {
	public int getRowCount();
	public int getColumnCount();
	public Object getValueAt(int row, int column);
}
