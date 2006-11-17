package gui;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class FloatRenderer extends DefaultTableCellRenderer{
	NumberFormat floatFormat;
	
    public FloatRenderer() {
    	floatFormat = NumberFormat.getInstance();
        // Set the maximum decimal point precision
        floatFormat.setMaximumIntegerDigits(1);
        floatFormat.setMaximumFractionDigits(10);
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
    	Component result=super.getTableCellRendererComponent(table, floatFormat.format(value), isSelected, hasFocus, row, column);
    	return result;
    }	
}
