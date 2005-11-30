package externalswing;

import javax.swing.JFrame;

import junit.framework.TestCase;

public class ProgressTaskTest extends TestCase {
	private JFrame frame;
	private int seconds = 5;
	
	@Override
	public void setUp() {
		frame = new JFrame("ProgressTaskTest");
		frame.setVisible(true);
	}
	/*
	 * Test method for 'externalswing.ProgressTask.start()'
	 */
	public void testStart() {
		ProgressTask myTask = new ProgressTask(null, "Progress", true) {
			public void run() {
//				try{Thread.sleep(1000);}catch (InterruptedException e) {}
			}
		};
		
		myTask.start();
		try{Thread.sleep(1000);}catch (InterruptedException e) {}
		
	}

}
