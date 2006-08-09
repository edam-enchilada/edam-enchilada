package chartlib.hist;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

public class BrushManager extends MouseInputAdapter {
	Point dragStart = null;
	
	ArrayList<BrushSelection> selected = new ArrayList<BrushSelection>();
	
	public BrushManager() {
		
	}
	
	public void listenTo() {}

	public void mousePressed(MouseEvent me) {
//		DataMouseEvent e = cast(me);
//		
//		
		
	}
	
	private DataMouseEvent cast(MouseEvent e) {
		if (!(e instanceof DataMouseEvent))
			throw new IllegalArgumentException("You made BrushManager a listener" +
					"of things it shouldn't listen to!  How seditious!");
		return (DataMouseEvent) e;
	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		
	}
	
	
	public void mouseClicked(MouseEvent me) {
		System.out.print(".");
		DataMouseEvent e = cast(me);
		int chartNum = e.getChartNumber(); // next: implement this.
		Point2D dataP = e.getPoint2D();
		selected.add(new BrushSelection(chartNum, 
				(int)dataP.getX(), (float)dataP.getY()));
		selectionChanged();
	}
	
	private void selectionChanged() {
		System.out.println("Sel changed.");
	}

	public void clearSelection() {
		selected.clear();
	}

	public ArrayList<BrushSelection> getSelected() {
		return selected;
	}
}
