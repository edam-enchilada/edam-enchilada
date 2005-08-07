package gui;

import java.awt.FlowLayout;
import javax.swing.*;

public class ProgressBarWrapper {
	private JDialog waitBarDialog;
	private JProgressBar pBar;
	private JLabel pLabel;
	
	private int curNum = 0;
	
	public ProgressBarWrapper(JFrame parentFrame, String title, int numSteps) {
		waitBarDialog = new JDialog(parentFrame, title, true);
		waitBarDialog.setLayout(new FlowLayout());
		pBar = new JProgressBar(0, numSteps);
		pBar.setValue(0);
		pBar.setStringPainted(true);
		pLabel = new JLabel("       Placeholder text.....                                                   ");
		pLabel.setLabelFor(pBar);
		waitBarDialog.add(pBar);
		waitBarDialog.add(pLabel);
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
