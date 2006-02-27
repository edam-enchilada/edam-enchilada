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
		
		// see the overridden dispose() method.
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
				/*
				 * Wait until the window has appeared, so we can be sure that
				 * we close it later on.
				 */
				while (true) {
					if (isVisible()) {
						/*
						 * The window is visible, so now let's start running
						 * our task.
						 */
						setStatus("Running.");
						
						// accomplish our task!!
						doRun();
						
						break;
					} else if (Thread.currentThread().isInterrupted()) {
						/*
						 * interrupted() implies that either the window has been
						 * closed (see overridden dispose()), or the thread
						 * has been interrupted for some other reason.  Either
						 * way, we don't do the task, and we do make sure the
						 * window is gone.
						 */
						break;
					}
					Thread.yield();
				}
		
				// GUI commands should get run by the EventDispatcher thread.
				SwingUtilities.invokeLater(
						new Runnable() {public void run(){dispose();}});
			}
		};
		
		task = new Thread(r);
		task.start();
		System.out.println("The worker thread: " + task);
		
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
	 * See the dispose() method for important information about premature
	 * termination of the task.
	 *
	 */
	public abstract void run();
	
	/**
	 * dispose() - close the dialog box and stop the task that is running.
	 * 
	 * This overridden dispose() method calls interrupt() on the thread that
	 * contains the running task.  See Thread.interrupt().  If the thread is
	 * waiting for something, like I/O, it will get an exception thrown, which
	 * should stop it.  If not, i.e. it is doing computations, it will only get
	 * its interrupted() status set.  Therefore, it would be a good idea to
	 * check whether this is the case periodically during heavy computations in
	 * your threaded job, using the static method Thread.interrupted().
	 */
	public void dispose() {
		task.interrupt();
		System.out.println("dispose called");
		super.dispose();
	}	
	
	

	/*
	 * These methods are here because the progressBar and statusText have
	 * to be modified from the EventDispatcher thread.  If they get modified
	 * from the worker thread that's started, race conditions and bad things
	 * happen.  So they simply access the progress bar asynchronously, and
	 * return immediately.
	 * 
	 * Feel free to add more if you feel like you need to.
	 */
	/**
	 * Set the Indeterminate-ness of the jprogressbar.
	 */
	protected void pSetInd(boolean state) {
		final boolean s = state;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setIndeterminate(s);
			}
		});	
	}
	
	/**
	 * Set the maximum value of the progress bar.
	 * @param maxVal
	 */
	protected void pSetMax(int maxVal) {
		final int val = maxVal;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setMaximum(val);
			}
		});	
	}
	
	/**
	 * Increment the value of the progress bar by one.
	 */
	protected void pInc() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progressBar.getValue() + 1);
			}
		});	
	}
	
	/*
	 * I'm guessing that it's ok to do this reading operation synchronously.
	 * 
	 * Also, it would be a bother to get a result from one thread to another.
	 */
	/**
	 * Get the current value of the progress bar.
	 */
	protected void pGetVal() {
		progressBar.getValue();
	}
	
	/**
	 * Set the current value of the progress bar.  You should set the maximum
	 * first.
	 * @param value
	 */
	protected void pSetVal(int value) {
		final int val = value;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(val);
			}
		});		
	}
	
	/**
	 * Set the status text that is displayed in the dialog box, near the
	 * progress bar.
	 * @param text
	 */
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
