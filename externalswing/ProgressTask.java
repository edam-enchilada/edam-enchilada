package externalswing;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.*;
//forget earlier today, say i started at 12:30

public abstract class ProgressTask extends JDialog {
	protected JProgressBar progressBar;
	protected JLabel statusText;
	private Thread task;
	
	public ProgressTask(Frame owner, String title) {
		super(owner, title, true);
		this.setLayout(new FlowLayout());
		statusText = new JLabel("Initializing...");
		add(statusText);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		add(progressBar);
		
		this.pack();
		System.out.println("End of PT constructor");
	}
	
	public void start() {
		System.out.println("About to start() the task");
		Runnable r = new Runnable() {
			public void run() {
				progressBar.setIndeterminate(false);
				statusText.setText("Running.");
				doRun();
			}
		};
		task = new Thread(r);
		task.start();
	}
	
	private void doRun() {
		// this is dumb, but i don't know how to refer to the ProgressTask's
		// run() from within the Runnable's run().
		this.run();
	}
	
	/**
	 * Override this method to code the task you'll have executed.
	 * 
	 * To update the user on what's happening, use the object variables
	 * progressBar and statusText.
	 * 
	 * You will need to set the bounds and, obviously, the current value of 
	 * the progressBar.
	 *
	 */
	public abstract void run();
}
