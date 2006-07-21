package chartlib.hist;

import chartlib.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;

/**
 * 
 * @author smitht
 *
 */

public class HistogramMouseDisplay extends JPanel implements
		MouseMotionListener {
	private HistogramsPlot chart;
	private JLabel xLabel, xValue, yLabel, yValue, zLabel, zValue;

	public HistogramMouseDisplay(HistogramsPlot chart) {
		super();
		this.chart = chart;
		chart.addMouseMotionListener(this);
		
		this.setLayout(new GridLayout(2,2));
		
		xLabel = new JLabel("m/z:");
		xValue = new JLabel("        ");
		
		yLabel = new JLabel("Rel. Area:");
		yValue = new JLabel("        ");
		
		zLabel = new JLabel("Count:");
		zValue = new JLabel("        ");
		
		JPanel temp;
		temp = new JPanel();
		temp.add(xLabel);
		temp.add(xValue);
		this.add(temp);
		
		temp = new JPanel();
		temp.add(yLabel);
		temp.add(yValue);
		this.add(temp);
		
		temp = new JPanel();
		temp.add(zLabel);
		temp.add(zValue);
		this.add(temp);
		
		this.setBorder(new BevelBorder(BevelBorder.RAISED));
		
		this.validate();
		this.setMinimumSize(this.getSize());
	}
	
	// don't care.
	public void mouseDragged(MouseEvent e) {}

	public void mouseMoved(MouseEvent e) {
		assert(e.getSource().equals(chart));  
		// actually, might be the zoomable chart.
		// do clicks go through the looking glass?
		
		Point p = e.getPoint();
		
		Component source = chart.findComponentAt(p);
		if (! (source instanceof HistogramsChartArea)) 
			return;
		
		HistogramsChartArea ca = (HistogramsChartArea) source;
		
		if (ca.isInDataArea(p)) {
			
			Point2D.Double dataPoint = ca.getDataValueForPoint(p);
			
			
			xValue.setText("" + format(dataPoint.getX()));
			yValue.setText("" + format(dataPoint.getY()));
			zValue.setText("" + ca.getCountAt((int) dataPoint.getX(), (float) dataPoint.getY()));
		}
	}
	
	private String format(double num) {
		return "" + ((Math.floor (num * 100.0)) / 100.0);
	}
}
