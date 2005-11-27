package externalswing;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.*;

/**
 * ProgressTask - a useful thingie like a SwingWorker, but without facilities
 * to return a value, and with a status text and progress bar in a JDialog box.
 * 
 * Use this if you want something to execute in its own thread with a cute
 * status monitor.
 * 
 * You use this class by creating a subclass with an overridden run() method,
 * then calling .start() on an instance of it.
 * 
 * @author smitht
 *
 */


public abstract class ProgressTask extends JDialog {
	private JProgressBar progressBar;
	private JLabel statusText;
	private Thread task;
	
	/**
	 * These are just the same parameters that a Dialog takes in its
	 * 3-argument version.
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public ProgressTask(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.setLayout(new FlowLayout());
		
		statusText = new JLabel("Initializing...");
		add(statusText);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		add(progressBar);
		
		this.pack();
	}
	
	/**
	 * start() - start the task running, and block until it is finished, if
	 * the dialog is modal.
	 *
	 */
	public void start() {
		/*
		 * Runnable r ends up calling the task to be executed.
		 */
		
		Runnable r = new Runnable() {
			public void run() {
				pSetInd(false);
				setStatus("Running.");
				
				doRun();				
				
		
				// we have to wait until setVisible is called before we dispose().
				// If not, setVisible will happen after, and the dialog will 
				// *never* close!  How cute!
				while (!isVisible()) {
					Thread.yield();
				}
				SwingUtilities.invokeLater(
						new Runnable() {public void run(){dispose();}});
			}
		};
		
		task = new Thread(r);
		task.start();
		
		// setVisible does not return until the dialog is disposed, if the 
		// ProgressTask was constructed as a modal dialog.
		this.setVisible(true);
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
	
	
	
	
	/*
	 * These methods are here because the progressBar and statusText have
	 * to be modified from the EventDispatcher thread.  If they get modified
	 * from the worker thread that's started, race conditions and bad things
	 * happen.
	 */
	protected void pSetInd(boolean state) {
		final boolean s = state;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setIndeterminate(s);
			}
		});	
	}
	
	protected void pSetMax(int maxVal) {
		final int val = maxVal;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setMaximum(val);
			}
		});	
	}
	
	protected void pInc() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progressBar.getValue() + 1);
			}
		});	
	}
	
	protected void pGetVal() {
		progressBar.getValue();
	}
	
	protected void pSetVal(int value) {
		final int val = value;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(val);
			}
		});		
	}
	
	
	protected void setStatus(String text) {
		final String t = text;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusText.setText(t);
				pack();
			}
		});	
	}


}
