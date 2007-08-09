package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.*;

public class ProgressBarWrapper extends JDialog{
	private final JProgressBar pBar;
	private final JLabel pLabel;
	private volatile boolean wasTerminated;
	private int curNum = 0;
	
	public ProgressBarWrapper(JFrame parentFrame, String title, int numSteps) {
		super(parentFrame, title, true);
		wasTerminated = false;
		pBar = new JProgressBar(0, numSteps);
		pBar.setValue(0);
		pBar.setStringPainted(true);
		pBar.setBorder(new EmptyBorder(5, 5, 5, 5));
		pLabel = new JLabel("");
		pLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pLabel.setLabelFor(pBar);
		pBar.setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		add(pBar, BorderLayout.NORTH);
		add(pLabel, BorderLayout.CENTER);
		setPreferredSize(new Dimension(500, 100));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0) {
				wasTerminated = true;
			}
		});
		
	}
	
	public void increment(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pBar.setValue(curNum++);
				pLabel.setText(text);
				validate();
			}
		});
	}
	
	public void constructThis() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pack();
				validate();
				setVisible(true);
			}
		});
	}
	
	public void disposeThis() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(false);
				dispose();
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

	public boolean wasTerminated() {
		return wasTerminated;
	}
	public void setMaximum(final int n){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pBar.setMaximum(n);
				validate();
			}
		});
	}
	public void reset(){
		curNum = 0;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pBar.setValue(0);
				pLabel.setText("");validate();
			}
		});
	}
	public void setValue(final int n){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pBar.setValue(n);
				validate();
			}
		});
	}
	public void setText(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pLabel.setText(text);
				validate();
			}
		});
	}
}
