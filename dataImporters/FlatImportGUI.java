package dataImporters;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class FlatImportGUI {
	
	
	private class ModeDialog extends JDialog implements ActionListener {
		JButton taskButton;
		JButton csvButton;
		JLabel titleLabel;
		
		public ModeDialog(Frame parent) {
			super(parent);
			
			taskButton = new JButton("...from .task file");
			taskButton.setActionCommand("task");
			taskButton.addActionListener(this);
			
			csvButton = new JButton("...by choosing .csv file(s)");
			csvButton.setActionCommand("csv");
			csvButton.setEnabled(false);
			csvButton.addActionListener(this);
			
			titleLabel = new JLabel("Import Time Series");
			
			this.add(titleLabel, BorderLayout.NORTH);
			this.add(csvButton, BorderLayout.CENTER);
			this.add(taskButton, BorderLayout.SOUTH);
		
			this.pack();
			this.setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("task")) {
				// so, how do i close this window and open the next one?
				//9-10:45
			}
			
		}
	}
}
