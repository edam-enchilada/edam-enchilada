package chartlib.hist;

/**
 * MouseRedirector - Pass along mouse events to one of multiple destinations.
 * <p>
 * This is a GUI object for making multiple modes of mouse interaction.  Add this
 * as a mouselistener and/or mousemotionlistener to the place of your choice.
 * The user
 * gets a radio button group with the labels that you specify via addMouseMode,
 * and when a particular one is selected, all mouse events that this receives
 * will be routed to the appropriate destination.    
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;

public class MouseRedirector extends JPanel implements MouseInputListener {
	/* If more actions need to be added, like temporarily enabling or disabling
	    a particular destination for mouse events, it might make sense to have
	    another map, or change this map, to be from label to destination, rather
	    than from button to destionation. */
	private Map<ButtonModel, MouseInputListener> destination;
	private ButtonGroup bGroup = new ButtonGroup();
	
	/**
	 * Create a new MouseRedirector, with no modes at all, and the given title.
	 * <p>
	 * You may want to set the layout, by default it's the default layout
	 * of a JPanel.
	 * @param title
	 */
	public MouseRedirector(String title) {
		super();
		if (title != null) {
			add(new JLabel(title));
		}
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		destination = new HashMap<ButtonModel, MouseInputListener>();
	}
	
	/**
	 * Add a button that, when selected, will route mouse events to the
	 * given destination.
	 * @param label the label of the button
	 * @param dest the destination of the mouse events
	 */
	public void addMouseMode(String label, MouseInputListener dest) {
		JRadioButton newButton = new JRadioButton(label);
		if (destination.isEmpty())
			newButton.setSelected(true); // the first button is selected.
		destination.put(newButton.getModel(), dest);
		bGroup.add(newButton);
		add(newButton);
	}
	
	/**
	 * Register the correct listener, deliver the event, then unregister it.
	 */
	@Override
	protected void processMouseEvent(MouseEvent e) {
		MouseListener l = destination.get(bGroup.getSelection());
		addMouseListener(l);
		super.processMouseEvent(e);
		removeMouseListener(l);
	}

	/**
	 * Register the correct listener, deliver the event, then unregister it.
	 */
	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		MouseMotionListener l = destination.get(bGroup.getSelection());
		addMouseMotionListener(l);
		super.processMouseMotionEvent(e);
		removeMouseMotionListener(l);
	}

	/*
	 * Below here: boring repetetive methods, and a main method for testing.
	 */
	
	public void mouseClicked(MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseEntered(MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		processMouseEvent(e);
	}

	public void mousePressed(MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseDragged(MouseEvent e) {
		processMouseMotionEvent(e);
	}

	public void mouseMoved(MouseEvent e) {
		processMouseMotionEvent(e);
	}
	
	/*
	 * Just for testing.  Creates a little dialog box that will let you see
	 * how it works.
	 */
	public static void main(String[] args) {
		JPanel btn = new JPanel();
		btn.setPreferredSize(new Dimension(40, 40));
		MouseRedirector sub = new MouseRedirector("Destination?");
		btn.addMouseListener(sub);
		btn.addMouseMotionListener(sub);
		btn.setBackground(Color.RED);
		sub.addMouseMode("Foo", new MouseInputAdapter() {
			public void mouseMoved(MouseEvent e) {
				System.out.println("FOOOOO");
			}
		});
		
		sub.addMouseMode("bar", new MouseInputAdapter() {
			public void mouseMoved(MouseEvent e) {
				System.out.println("BARRRR");
			}
		});
		
		JFrame fr = new JFrame("subverter");
		JPanel pan = new JPanel();
		
		pan.add(btn);
		pan.add(sub);
		fr.add(pan);
		fr.pack();
		fr.setVisible(true);
		fr.setDefaultCloseOperation(fr.EXIT_ON_CLOSE);
	}
}
