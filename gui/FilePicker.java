package gui;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JFileChooser;

/**
 * FilePicker uses a FileChooser and sets it appropriately for use choosing files
 * within Enchilada, but is not tied to being a CellEditor in tables, unlike 
 * FilePickerEditor and FileDialogPickerEditor.
 * @author steinbel
 * 6.27.05
 * 
 * FilePicker is now no longer used and has been replaced with FileDialogPicker
 * 6.13.07
 */

public class FilePicker{

	private JFileChooser fc;
	private CustomFileFilter customff;
	private String fileName;
	private int returnValue;
	
	/**
	 * 
	 * @param title	The title for the dialog box.
	 * @param ext The extension for the file filter (without the '.').
	 * @param parent The parent component for the dialog.
	 */
	public FilePicker(String title, String ext, Component parent){
		
		fc = new JFileChooser();
		fc.setDialogTitle(title);
		customff = new CustomFileFilter();
		customff.addFileFilter(ext);
		fc.setFileFilter(customff);
		returnValue = fc.showOpenDialog(parent);
		
		if (returnValue == JFileChooser.APPROVE_OPTION)
			fileName = fc.getSelectedFile().getAbsolutePath();
		else
			fileName = null;
		
	}
	
	/**
	 * 
	 * @param title	The title for the dialog box.
	 * @param exts	A list of the possible extensions for the file filter
	 * 					(without the '.').
	 * @param parent	The parent component for the dialog.
	 */
	public FilePicker(String title, ArrayList<String> exts, Component parent){
		
		fc = new JFileChooser();
		fc.setDialogTitle(title);
		customff = new CustomFileFilter();
		
		for (String extension : exts)
			customff.addFileFilter(extension);
		
		fc.setFileFilter(customff);
		returnValue = fc.showOpenDialog(parent);
		
		if (returnValue == JFileChooser.APPROVE_OPTION)
			fileName = fc.getSelectedFile().getAbsolutePath();
		else
			fileName = null;
		
	}
	
	public String getFileName(){
		return fileName;
	}
	
}