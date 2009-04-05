/**
 * FileDialogPicker uses a FileDialog and sets it appropriately for use choosing files
 * within Enchilada, but is not tied to being a CellEditor in tables, unlike 
 * FilePickerEditor and FileDialogPickerEditor.
 * @author turetske
 * 6.13.07
 */

package gui;

import javax.swing.JButton;
import java.awt.*;


public class FileDialogPicker{

	private String fileName;
	private String oldFilename;
	private String fileFilter;
	private FileDialog fileChooser;
	private JButton button;
	
	/**
	 * 
	 * @param title	The title for the dialog box.
	 * @param ext The extension for the file filter (without the '.').
	 * @param pDialog The owner frame for the dialog
	 * 
	 */
	
	public FileDialogPicker(String title, String ext, Frame pDialog){
		fileChooser = new FileDialog(pDialog,title,FileDialog.LOAD);
		fileFilter = "*." + ext;
		
		button = new JButton(oldFilename);
		button.setBorderPainted(false);
		button.setBackground(Color.WHITE);
		fileChooser.setFile(fileFilter);
		fileChooser.setVisible(true);
		
		String returnVal = null;
		returnVal = fileChooser.getFile();
		
		if(returnVal != null)
		{
			fileName = fileChooser.getDirectory() + returnVal;
		}
		else
			fileName = oldFilename;
		
	}
	
	/**
	 * 
	 * @param title	The title for the dialog box.
	 * @param ext The extension for the file filter (without the '.').
	 * @param pDialog The owner dialog for this dialog
	 * 
	 */
	public FileDialogPicker(String title, String ext, Dialog pDialog){
		fileChooser = new FileDialog(pDialog,title,FileDialog.LOAD);
		fileFilter = "*." + ext;
		
		button = new JButton(oldFilename);
		button.setBorderPainted(false);
		button.setBackground(Color.WHITE);
		fileChooser.setFile(fileFilter);
		fileChooser.setVisible(true);
		
		String returnVal = null;
		returnVal = fileChooser.getFile();
		
		if(returnVal != null)
		{
			fileName = fileChooser.getDirectory() + returnVal;
		}
		else
			fileName = oldFilename;
		
	}
	/**
	 * 
	 * @param title	The title for the dialog box.
	 * @param ext The extension for the file filter (without the '.').
	 * @param pDialog The owner dialog for this dialog
	 * @param load true if load is the desired option, false if save is the desired option
	 */
	public FileDialogPicker(String title, String ext, Frame frame, Boolean load){
		if(load)
			fileChooser = new FileDialog(frame,title,FileDialog.LOAD);
		else
			fileChooser = new FileDialog(frame,title,FileDialog.SAVE);
		fileFilter = "*." + ext;
		
		button = new JButton(oldFilename);
		button.setBorderPainted(false);
		button.setBackground(Color.WHITE);
		fileChooser.setFile(fileFilter);
		fileChooser.setVisible(true);
		
		String returnVal = null;
		returnVal = fileChooser.getFile();
		
		if(returnVal != null)
		{
			fileName = fileChooser.getDirectory() + returnVal;
		}
		else
			fileName = oldFilename;
		
	}

	/**
	 * 
	 * @param title	The title for the dialog box.
	 * @param origFileName The file name for the filter.  It could be *.extension.
	 * @param pDialog The owner dialog for this dialog
	 * @param load true if load is the desired option, false if save is the desired option
	 */
	public FileDialogPicker(String title, String origFileName, Dialog pDialog, Boolean load){
		if(load)
			fileChooser = new FileDialog(pDialog,title,FileDialog.LOAD);
		else
			fileChooser = new FileDialog(pDialog,title,FileDialog.SAVE);
		fileFilter = origFileName;
		
		button = new JButton(oldFilename);
		button.setBorderPainted(false);
		button.setBackground(Color.WHITE);
		fileChooser.setFile(fileFilter);
		fileChooser.setVisible(true);
		
		String returnVal = null;
		returnVal = fileChooser.getFile();
		
		if(returnVal != null)
		{
			fileName = fileChooser.getDirectory() + returnVal;
		}
		else
			fileName = oldFilename;
		
	}

	public String getFileName(){
		return fileName;
	}
}
