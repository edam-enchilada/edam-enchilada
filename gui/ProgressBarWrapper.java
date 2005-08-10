package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class ProgressBarWrapper {
	private JDialog waitBarDialog;
	private JProgressBar pBar;
	private JLabel pLabel;
	
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
	
	public void increment(String text) {
		pBar.setValue(curNum++);
		pLabel.setText(text);
		waitBarDialog.validate();
	}
	
	public void constructThis() {
		waitBarDialog.pack();
		waitBarDialog.validate();
		waitBarDialog.setVisible(true);
	}
	
	public void disposeThis() {
		waitBarDialog.setVisible(false);
		waitBarDialog.dispose();
	}
}
