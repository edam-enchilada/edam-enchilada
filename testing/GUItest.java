package testing;

import java.awt.Component;

import gui.*;
import database.*;
import javax.swing.*;

import org.uispec4j.*;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.interception.*;

/* For the Abbot testing framework
 * 	http://abbot.sourceforge.net/doc/overview.shtml
import junit.extensions.abbot.*;
import abbot.finder.Matcher;
import abbot.finder.MultiMatcher;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.*;
import abbot.tester.*; 

make GUItest extends ComponentTestFixture 
*/

/**
 * Use UISpec4J to run a few example automated UI unit tests
 * @author shaferia
 */
public class GUItest extends UISpecTestCase {
	static { UISpec4J.init(); }
	
	private InfoWarehouse db;
	private MainFrame mf;
	private Window mainframe;
	
	private boolean databaseRebuilt = false;
	
	/**
	 * Recreate MainFrame and reconnect to the database for each test
	 */
	protected void setUp() throws Exception {
		if (!databaseRebuilt) {
			Database.rebuildDatabase("TestDB");
			databaseRebuilt = true;
		}
		
		db = Database.getDatabase("TestDB");
		MainFrame.db = db;
		db.openConnection("TestDB");
		WindowInterceptor.run(new Trigger() {
			public void run() {
				mf = new MainFrame();
				mf.setVisible(true);
			}
		});
		mainframe = new Window(mf);
	}
	
	protected void tearDown() throws Exception {
		mf.dispose();
		db.closeConnection();
	}
	
	/**
	 * Test importing a dataset (testRow/b) as a single collection and
	 * as tripled into a parent collection.
	 */
	/*public void testImportData() throws Throwable {
		//locations of files to import - these will be entered in the GUI
		final String parFile = "testRow/b/b.par";
		final String calFile = "testRow/b/cal.cal";
		
		//opens the import dialog
		Trigger openImport = new Trigger() {
			public void run() throws Exception {
				MenuItem menu = mainframe.getMenuBar().
				getMenu("File").getSubMenu("Import Collection. . .").
				getSubMenu("From ATOFMS data. . .");
				menu.click();
			}
		};
		
		//import a single collection
		WindowInterceptor.init(openImport).process(new WindowHandler() {
			public Trigger process(Window w) throws Exception {
				JTable table = (JTable) ((JScrollPane)
				w.findSwingComponent(new ComponentMatcher() {
					public boolean matches(Component c) {
						return c instanceof JScrollPane;
					}
				})).getViewport().getView();
				
				int targetRow = 0;
				table.setValueAt(parFile, targetRow, 1);
				table.setValueAt(calFile, targetRow, 2);
				table.setValueAt(10, targetRow, 4);
				table.setValueAt(10, targetRow, 5);
				table.setValueAt(0.01f, targetRow, 6);
				
				System.out.println("Now importing test data...");
				Trigger t = w.getButton("OK").triggerClick();
				System.out.println("...done importing test data");
				
				return t;
			}
		}).run();
		
		//import multiple collections (3 identical ones) into a parent collection
		WindowInterceptor.init(openImport).process(new WindowHandler() {
			public Trigger process(Window w) throws Exception {
				JTable table = (JTable) ((JScrollPane)
				w.findSwingComponent(new ComponentMatcher() {
					public boolean matches(Component c) {
						return c instanceof JScrollPane;
					}
				})).getViewport().getView();
				
				for (int targetRow = 0; targetRow < 3; ++targetRow) {
					table.setValueAt(parFile, targetRow, 1);
					table.setValueAt(calFile, targetRow, 2);
					table.setValueAt(10, targetRow, 4);
					table.setValueAt(10, targetRow, 5);
					table.setValueAt(0.01f, targetRow, 6);
				}
				
				Trigger parentOpener = 
					w.getCheckBox("Create a parent collection for all incoming datasets.")
					.triggerClick();
				
				//set the parent collection's properties
				WindowInterceptor.init(parentOpener)
				.process(new WindowHandler() {
					//handle the parent collection dialog
					public Trigger process(Window w) throws Exception {
						UIComponent[] comps = w.getUIComponents(new ComponentMatcher() {
							public boolean matches(Component c) {
								return (c instanceof JTextField);
							}		
						});
						((TextBox)comps[0]).setText("parent");
						((TextBox)comps[1]).setText("parent comment");
						return w.getButton("OK").triggerClick();
					}
				}).run();
				
				return w.getButton("OK").triggerClick();
			}
		}).run();
	}
	
	/**
	 * Test retrieving a version number off the About dialog
	 */
	public void testGetVersion() throws Throwable {
		WindowInterceptor.init(new Trigger() {
			//get the "About Enchilada" button
			public void run() throws Exception {
				MenuItem menu = mainframe.getMenuBar().
				getMenu("Help").getSubMenu("About Enchilada");
				menu.click();
			}
		}).process(new WindowHandler() {
			public Trigger process(Window w) throws Exception {
				//find the dialog's JLabels
				UIComponent[] comps = w.getUIComponents(new ComponentMatcher() {
					public boolean matches(Component c) {
						return c instanceof JLabel;
					}
				});
				
				//Find the software version if it exists
				String vers = "Software Version";
				boolean versfound = false;
				for (UIComponent c : comps) {
					String text = ((JLabel) c.getAwtComponent()).getText();
					if (text != null && text.indexOf(vers) > -1) {
						String s = text.substring(text.indexOf(vers) + vers.length() + 1);
						System.out.println("version: " + s);
						if (s.length() > 1)
							versfound = true;
						System.out.println("here");
					}
				}
				assertTrue(versfound);
				return w.getButton("OK").triggerClick();
			}	
		}).run();
	}
	
	/* Code for the Abbot unit testing framework: 
    public void testGetVersion() throws Throwable {
    	//navigate through the menus
    	Component c = getFinder().find(new JMenuItemMatcher("Help"));
    	JMenuItemTester jmit = new JMenuItemTester();
    	jmit.actionClick(c);
    	c = getFinder().find(new JMenuItemMatcher("Help|About Enchilada"));
    	jmit.actionClick(c);
    	
    	//find a the JLabels on the form with "Software Version"
    	final String vers = "Software Version";
    	Container dialog = (Container) getFinder().find(new WindowMatcher("Message"));
    	c = getFinder().find(dialog, new Matcher() {
			public boolean matches(Component c) {
				return (c instanceof JLabel) &&
						((JLabel)c).getText() != null && 
					 	((JLabel)c).getText().indexOf(vers) > -1;
			}
    	});
    	String text = ((JLabel)c).getText();
    	String version = text.substring(text.indexOf(vers) + vers.length() + 1);
    	assertTrue(version.length() > 0);
    	System.out.println("Version: " + version);
    	
    	//Find and click the OK button
    	JButtonTester jbt = new JButtonTester();
    	c = getFinder().find(dialog, new abbot.finder.matchers.ClassMatcher(JButton.class, true));
    	jbt.actionClick(c);
    	
    	//make the frame stick around so we can see it - not necessary for testing
    	while (true) {
    		synchronized (mf) {
    			mf.wait();
    		}
    	}
    }
    
 
    public GUItest(String name) { super(name); }

	*/
}