package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class ProgressBarWrapper {
	private JDialog waitBarDialog;
	private final JProgressBar pBar;
	private final JLabel pLabel;
	
	private int curNum = 0;
	
	public ProgressBarWrapper(JFrame parentFrame, String title, int numSteps) {
		waitBarDialog = new JDialog(parentFrame, title, true);
		waitBarDialog.setLayout(new BorderLayout());
		pBar = new JProgressBar(0, numSteps);
		pBar.setValue(0);
		pBar.setStringPainted(true);
		pBar.setBorder(new EmptyBorder(5, 5, 5, 5));
		pLabel = new JLabel("");
		pLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pLabel.setLabelFor(pBar);
		pBar.setBorder(new EmptyBorder(5, 5, 5, 5));
		waitBarDialog.add(pBar, BorderLayout.NORTH);
		waitBarDialog.add(pLabel, BorderLayout.CENTER);
		waitBarDialog.setPreferredSize(new Dimension(500, 100));
	}
	
	public void increment(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pBar.setValue(curNum++);
				pLabel.setText(text);
				waitBarDialog.validate();
			}
		});
	}
	
	public void constructThis() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				waitBarDialog.pack();
				waitBarDialog.validate();
				waitBarDialog.setVisible(true);
			}
		});
	}
	
	public void disposeThis() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				waitBarDialog.setVisible(false);
				waitBarDialog.dispose();
			}
		});
	}

	public void setIndeterminate(final boolean indeterminate) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (indeterminate) {
					pBar.setString("");
				} else {
					pBar.setString(null);
				}
				pBar.setIndeterminate(indeterminate);
			}
		});
	}

}
