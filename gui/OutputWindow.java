package gui;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import java.io.*;

/**
 * Presents the user with a window that accepts redirected standard output.
 * Offers options to save output to a file and clear output.
 * @author shaferia
 */
public class OutputWindow extends JFrame {
	private JTextArea output;
	private OutputWindow thisref;
	
	private static final boolean RETURN_OUTPUT_ON_CLOSE = false;
	
	public OutputWindow(JFrame mf) {
		super("Output");
		thisref = this;
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		output = new JTextArea();
		output.setBorder(BorderFactory.createLoweredBevelBorder());
		output.setEditable(false);
		output.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane outputScroll = new JScrollPane(output);
		outputScroll.setWheelScrollingEnabled(true);
		cp.add(outputScroll, BorderLayout.CENTER);

		//Add a button offering to save the text of standard output to a file.
		JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveButton = new JButton("Save...");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fileChooser = new FileDialog(thisref, 
	                    "Select a file to write output to:",
	                     FileDialog.SAVE);
				fileChooser.setVisible(true);
				
				//cancel button pressed
				//	there doesn't seem to be a more robust way of detecting this
				if (fileChooser.getFile() == null)
					return;
				
				File f = new File(fileChooser.getDirectory() + fileChooser.getFile());
				if (f.exists()) {
					int overwrite = 
						JOptionPane.showConfirmDialog(thisref, 
								"Would you like to overwrite " + f.getName() + "?",
								"Overwrite?",
								JOptionPane.YES_NO_OPTION);
					if (overwrite == JOptionPane.NO_OPTION)
						return;
					//else continue and overwrite
				}
				
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(f));
					writer.write(thisref.output.getText());
					writer.close();
					System.out.println("Output successfully written to file " + f.getName());
				}
				catch (Exception ex) {
					System.err.println("Couldn't write output to file" + f.getName());
				}
			}
		});
		buttonsPane.add(saveButton);
		
		//Clears the output window
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				thisref.output.setText("");
			}
		});
		buttonsPane.add(clearButton);
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				thisref.setVisible(false);
			}
		});
		buttonsPane.add(closeButton);
		
		cp.add(buttonsPane, BorderLayout.SOUTH);
		
		System.setErr(new PrintStream(new UserOutputter()));
		System.setOut(new PrintStream(new UserOutputter()));
		
		//return output to standard output when window is closed
		if (RETURN_OUTPUT_ON_CLOSE)
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					System.setErr(System.err);
					System.setOut(System.out);
				}
			});
		
		pack();
	}
	
	/**
	 * Performs redirection of output to output TextArea
	 */
	public class UserOutputter extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			output.append((char) b + "");
		}
	}
	
}
