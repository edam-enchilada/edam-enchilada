package chartlib.hist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

public class BrushManager extends MouseInputAdapter {
	public static final int SELECTION_INCREASED = 1;
	
	DataMouseEventTranslator trans;
	ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	
	ArrayList<BrushSelection> selected = new ArrayList<BrushSelection>();
	
	public BrushManager(HistogramsPlot plot) {
		trans = new DataMouseEventTranslator(plot);
		trans.addMouseMotionListener(this);
		trans.addMouseListener(this);
	}

	private void notifyListeners() {
		for (ActionListener l : listeners) {
			l.actionPerformed(
					new ActionEvent(this, SELECTION_INCREASED, "selection"));
		}
	}
	
	public void mousePressed(MouseEvent me) {
		DataMouseEvent e = cast(me);
		
		
		
	}
	
	private DataMouseEvent cast(MouseEvent e) {
		if (!(e instanceof DataMouseEvent))
			throw new IllegalArgumentException("You made BrushManager a listener" +
					"of things it shouldn't listen to!  How seditious!");
		return (DataMouseEvent) e;
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseDragged(MouseEvent e) {
		
	}
	
	
	public void mouseClicked(MouseEvent me) {
		DataMouseEvent e = cast(me);
		Point2D dataP = e.getPoint2D();
		selected.add(new BrushSelection((int)dataP.getX(), (float)dataP.getY()));
	}
	
	public void clearSelection() {
		selected.clear();
	}
}
