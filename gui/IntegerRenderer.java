package gui;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class IntegerRenderer extends DefaultTableCellRenderer{
	NumberFormat integerFormat;
	
    public IntegerRenderer() {
    	integerFormat = NumberFormat.getInstance();
        // Set the maximum decimal point precision
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
    	String formatted = integerFormat.format(value);
    	Component result=super.getTableCellRendererComponent(table, formatted, isSelected, hasFocus, row, column);
    	return result;
    }	
}
