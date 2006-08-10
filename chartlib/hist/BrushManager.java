package chartlib.hist;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public class BrushManager implements MouseInputListener {
	BrushSelection dragStart = null;
	
	ArrayList<BrushSelection> selected = new ArrayList<BrushSelection>();
	
	HistogramsPlot plot;
	
	public BrushManager(HistogramsPlot plot) {
		this.plot = plot;
	}
	
	public void mousePressed(MouseEvent me) {
		DataMouseEvent e = cast(me);
		int chartNum = e.getChartNumber();
		if (chartNum == -1 || !e.isInDataArea()) return;
		Point2D dataP = e.getPoint2D();
		dragStart = new BrushSelection(chartNum, (int) dataP.getX(), 
				(float) dataP.getY());
	}
	
	private DataMouseEvent cast(MouseEvent e) {
		if (!(e instanceof DataMouseEvent))
			throw new IllegalArgumentException("You made BrushManager a listener" +
					"of things it shouldn't listen to!  How seditious!");
		return (DataMouseEvent) e;
	}

	public void mouseReleased(MouseEvent me) {
		if (dragStart == null) return;
		DataMouseEvent e = cast(me);
		int chartNum = e.getChartNumber();
		if (chartNum == -1 || !e.isInDataArea() || chartNum != dragStart.spectrum) {
			dragStart = null;
			return;
		}
		Point2D dataP = e.getPoint2D();
		float[] minmax = new float[] {(float) dataP.getY(), dragStart.min };
		Arrays.sort(minmax);
		selected.add(new BrushSelection(chartNum, dragStart.mz, minmax[0], minmax[1]));
		selectionChanged();
	}

	public void mouseDragged(MouseEvent e) {
		
	}
	
	public void mouseClicked(MouseEvent me) {
		System.out.print(".");
		DataMouseEvent e = cast(me);
		int chartNum = e.getChartNumber();
		if (chartNum == -1 || !e.isInDataArea()) return;
		Point2D dataP = e.getPoint2D();
		selected.add(new BrushSelection(chartNum, 
				(int)dataP.getX(), (float)dataP.getY()));
		selectionChanged();
	}
	
	private void selectionChanged() {
		plot.setBrushSelection(selected);
	}

	public void clearSelection() {
		selected.clear();
		selectionChanged();
	}

	public ArrayList<BrushSelection> getSelected() {
		return selected;
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {
		dragStart = null;
	}

}
