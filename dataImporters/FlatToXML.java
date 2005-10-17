package dataImporters;

import gui.FilePicker;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

/*
 * OK, ideas.
 * 
 * Display the first few lines of the file, have them enter the field
 * separator, figure out if it's right.  Once the FS is there, add a place
 * to enter all the field names.  Popup or beep or sumpin if they put spaces
 * in the field names (we can't support that, right?).
 * 
 * Also need field types!!!
 * 
 * Special handling of the date field.
 * 
 * Hmmm, the important thing right now is to get really simple things working.
 * 
 * ohhhh, the datasetinfo.  huh.  actually all we need to know is the name.
 * the name can default ot the filename.
 * 
 * Conditions to check for:
 * The first line is labels for the fields or something weird?
 * Is this thingie going to support quoted strings?  They're hardcore.
 * Different numbers of fields in some lines?
 */


public class FlatToXML extends JFrame {
	File inFile;
	// XXX: the OK Button thing is not ready to work yet.  One
	// thing can be done and set the OK button enabled, while the
	// other is still undone and would want it disabled.  bleh.
	
	// maybe OK should always be enabled and there'll just be a popup?
	// but that's silly.  blah.
	JButton OKButton;
	
	public FlatToXML(File inFile) {
		// setup gui stuff.
		
		this.inFile = inFile;
		
		JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		
		OKButton = new JButton("OK");
		OKButton.setEnabled(false);
		
		
		root.add(new DateWidget("10/4/05", OKButton));

		root.add(new FieldOrderWidget("this,that", OKButton));
		
		
		JPanel controlBox = new JPanel();
		controlBox.setLayout(new BoxLayout(controlBox, BoxLayout.X_AXIS));
			controlBox.add(OKButton);
			controlBox.add(new JButton("Cancel"));
		root.add(controlBox);
		
		
		
		
		
		
		
		
		
		
		this.add(root);
		this.pack();
		this.setVisible(true);

		
		//doConversion();
	}
	
	
	private File fileOpenDialog() {
		FilePicker fp = new FilePicker("Choose a file to convert", 
				"csv", this);
		if (fp == null) {
			return null;
		}
		return new File(fp.getFileName());
	}
	
	
	
	
	public static void main(String[] args) {
		FlatToXML supah = new FlatToXML(new File("asdf"));
	}
	

	private class FieldOrderWidget extends JPanel implements ActionListener {
		public FieldOrderWidget(String exampleLine, JButton doneYet) {
			
		}
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class DateWidget extends JPanel implements ActionListener {
		String exampleString;
		
		JTextField formatField;
//		JTextField yearField;
//		JTextField monthField;
//		JTextField dayField;
		JButton parseButton;
		JTextField parseOutputField;
//		JCheckBox appendDate;
		
		JButton doneYet; // we enable this button when a date can be parsed.
		
		
		public DateWidget(String parseTestString, JButton doneYet) {
			this.exampleString = parseTestString;
			this.doneYet = doneYet;
			
			// Set up teh GUUUUIIII
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.setBorder(BorderFactory.createEtchedBorder());
			
			// formatBox: contains the input string to the DateFormat thingie.
			JPanel formatBox = new JPanel();
			formatBox.setBorder(BorderFactory.createTitledBorder("Date Format"));
			formatBox.setLayout(new BoxLayout(formatBox, BoxLayout.X_AXIS));
			JPanel fBLabels = new JPanel();
				fBLabels.setLayout(new GridLayout(2,0));
				fBLabels.add(new JLabel("Parse this:"));
				fBLabels.add(new JLabel("With this template:"));
			formatBox.add(fBLabels);
			JPanel fBFields = new JPanel();
				fBFields.setLayout(new GridLayout(2,0));
				JTextField exampleField = new JTextField(exampleString);
				exampleField.setEditable(false);
				fBFields.add(exampleField);
				formatField = new JTextField("y-M-d k:m:s");
				formatField.addActionListener(this);
				fBFields.add(formatField);
			formatBox.add(fBFields);
			this.add(formatBox);
			
//			// justTimeBox: contains a year, month, day thing to append
//			// to the thing if it's just time.
//			JPanel justTimeBox = new JPanel();
//			justTimeBox.setLayout(new BoxLayout(justTimeBox, BoxLayout.Y_AXIS));
//				appendDate = new JCheckbox("Field in file is just time");
//			justTimeBox.add()
			
			// tryParseBox: contains "try parsing!" button and output field.
			JPanel tryParseBox = new JPanel();
			tryParseBox.setBorder(BorderFactory.createEtchedBorder());
			tryParseBox.setLayout(new BoxLayout(tryParseBox, BoxLayout.X_AXIS));
				parseButton = new JButton("Try Parsing");
				parseButton.addActionListener(this);
				tryParseBox.add(parseButton);
				parseOutputField = new JTextField("(no output yet)");
				parseOutputField.setColumns(25);
				parseOutputField.setEditable(false);
				tryParseBox.add(parseOutputField);
			this.add(tryParseBox);
			
			
			
		}

		private String parse() {
			// If we were clever,
			// we could get the position where the error occurred back to the
			// GUI or something.
			//ParsePosition pos = new ParsePosition(0);
			Date parsed;
			
			try {
				SimpleDateFormat df = 
					new SimpleDateFormat(formatField.getText());
				parsed = df.parse(exampleString);
			} catch (ParseException ex) {
				return null;
			}
			
			if (parsed == null) {
				return null;
			} else {
				SimpleDateFormat odf = new SimpleDateFormat(
						"MMMMM d, yyyy 'at' h:mm a 'and' " +
						"s.S 'seconds'");
				return odf.format(parsed);
			}
		}
		
		public void actionPerformed(ActionEvent ev) {
			if (ev.getSource().equals(parseButton)
					||
					ev.getSource().equals(formatField))
			{
				String parsed = parse();
				if (parsed != null) {
					doneYet.setEnabled(true);
					parseOutputField.setText(parsed);
				} else {
					doneYet.setEnabled(false);
					parseOutputField.setText("Parsing failed.");
				}
			}
			
		}
		
		
	}

}

