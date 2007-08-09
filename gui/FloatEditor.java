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
 * The Original Code is EDAM Enchilada's CollectionCursor class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Janara Christensen 
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



import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import java.awt.Component;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.border.LineBorder;
import java.awt.*;
/**
 * @author christej
 *
 */
public class FloatEditor extends DefaultCellEditor {
    JFormattedTextField textField;
    NumberFormat floatFormat;
    
    /**
     * set up the textfield and its formatter
     */
    public FloatEditor() {
        super(new JFormattedTextField());
        textField = (JFormattedTextField)getComponent();
        textField.setBorder(new LineBorder(Color.black));
        floatFormat = NumberFormat.getInstance();
        // Set the maximum decimal point precision
        floatFormat.setMaximumIntegerDigits(1);
        floatFormat.setMaximumFractionDigits(10);
        NumberFormatter floatFormatter = new NumberFormatter(floatFormat);
        floatFormatter.setFormat(floatFormat);
        textField.setFormatterFactory(
                new DefaultFormatterFactory(floatFormatter));

    }
    
    /**
     * When a cell is selected, make it empty so when the user types
     * it won't append to what's already there
     */
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {
        textField.setValue(null);
        return textField;
    }
    /**
     * Get the input and try to make it into a float
     */
    public Object getCellEditorValue() {
    	System.out.println("FloatEditor.getCellEditorValue");
    	textField.setBorder(new LineBorder(Color.black));
        Object value = textField.getValue();
        if (value instanceof Float) 
            return value;
        else if (value instanceof Number) 
            return new Float(((Number)value).floatValue()); 
        else {
            try {
                return floatFormat.parseObject(value.toString());
            } 
            catch (ParseException e) {
                return null;
            }
        }
    }
    /**
     * If there was a problem with the input, try commiting it
     * and if that doesn't work, turn the border read and
     * return false (the user will not be able to type 
     * anywhere else until the input is in the correct form)
     */
    public boolean stopCellEditing() {
        if (textField.isEditValid()) {
        	try {
        		textField.commitEdit();
            } 
            catch (java.text.ParseException exc) { }
	    
        } 
        else {
	        textField.setBorder(new LineBorder(Color.red));
        	return false; 
        }
        return super.stopCellEditing();
    }

}