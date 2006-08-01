package gui;

import java.awt.Adjustable;
import java.awt.Container;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FileDialog;
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
	private JScrollPane outputScroll;
	private OutputWindow thisref;
	
	private int prevAdjust;
	private int newAdjust;
	private boolean allowAdjust;
	private boolean allowPermAdjust;

	private static boolean RETURN_OUTPUT_ON_CLOSE = true;
	
	/**
	 * Responsible for keeping scrollbar in place when scroll lock is on
	 */
	private AdjustmentListener locker = new AdjustmentListener() {
		public void adjustmentValueChanged(AdjustmentEvent event) {
			Adjustable a = event.getAdjustable();
			newAdjust = a.getValue();
			if (!event.getValueIsAdjusting() && !allowAdjust && !allowPermAdjust) {
				a.setValue(prevAdjust);
			}
			allowAdjust = false;
			prevAdjust = a.getValue();
		}
	};
	
	/**
	 * Allow user input to scrollbar to change its position
	 */
	private MouseAdapter clicker = new MouseAdapter() {
		public void mousePressed(MouseEvent event) {
			allowPermAdjust = true;
		}
		public void mouseReleased(MouseEvent event) {
			allowPermAdjust = false;
		}
		public void mouseClicked(MouseEvent event) {
			allowAdjust = true;
			outputScroll.getVerticalScrollBar().setValue(newAdjust);
		}
	};
	
	private void setScrollLock(boolean state) {
		JScrollBar vscroll = outputScroll.getVerticalScrollBar();
		if (state) {
			vscroll.addAdjustmentListener(locker);
			for (Component c : vscroll.getComponents())
				c.addMouseListener(clicker);
			prevAdjust = vscroll.getValue();
		}
		else {
			for (Component c : vscroll.getComponents())
				c.removeMouseListener(clicker);
			vscroll.removeAdjustmentListener(locker);
		}
	}
	
	/**
	 * Determines whether 
	 * @param behavior true: return output to standard output when this window is closed
	 * 	false: keep output in window
	 */
	public static void setReturnOutputOnClose(boolean behavior) {
		RETURN_OUTPUT_ON_CLOSE = behavior;
	}
	
	public OutputWindow(final JFrame mf) {
		super("Output");
		thisref = this;
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		output = new JTextArea();
		output.setBorder(BorderFactory.createLoweredBevelBorder());
		output.setEditable(false);
		output.setFont(new Font("Monospaced", Font.PLAIN, 12));
		outputScroll = new JScrollPane(output);
		outputScroll.setWheelScrollingEnabled(true);
		cp.add(outputScroll, BorderLayout.CENTER);

		int margin = 3;
		//Add a button offering to save the text of standard output to a file.
		JPanel buttonsPane = new JPanel();
		buttonsPane.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
		buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
		
		final JCheckBox scrollBottom = new JCheckBox("Scroll lock");
		scrollBottom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setScrollLock(scrollBottom.isSelected());
			}
		});
		scrollBottom.setSelected(true);
		setScrollLock(true);
		buttonsPane.add(scrollBottom);
		buttonsPane.add(Box.createHorizontalGlue());
		
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
		buttonsPane.add(Box.createHorizontalStrut(margin));
		
		//Clears the output window
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				thisref.output.setText("");
			}
		});
		buttonsPane.add(clearButton);
		buttonsPane.add(Box.createHorizontalStrut(margin));
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				thisref.setVisible(false);
			}
		});
		buttonsPane.add(closeButton);
		
		cp.add(buttonsPane, BorderLayout.SOUTH);
		
		final PrintStream oldErr = System.err;
		final PrintStream oldOut = System.out;
		System.setErr(new PrintStream(new UserOutputter()));
		System.setOut(new PrintStream(new UserOutputter()));
		
		//return output to standard output when window is closed
		if (RETURN_OUTPUT_ON_CLOSE)
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					System.setErr(oldErr);
					System.setOut(oldOut);
				}
			});
		
		pack();
	}
	
	/**
	 * Performs redirection of output to output TextArea
	 */
	public class UserOutputter extends OutputStream {
		private StringBuffer buf = new StringBuffer();
		
		/**
		 * Create a regularly-flushing buffer for standard output.
		 */
		public UserOutputter() {
			Thread t = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							if (buf.length() > 0) {
								final String toAppend = buf.toString();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										output.append(toAppend);
									}
								});
								buf = new StringBuffer();
							}
							Thread.sleep(100);
						}
						catch (InterruptedException ex) {
							break;
						}
					}
				}
			});
			t.start();
		}
		
		@Override
		public void write(int b) throws IOException {
			buf.append((char) b);
		}
	}
	
}
