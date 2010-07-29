package chartlib.hist;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import chartlib.*;
import externalswing.Useful;

/**
 * A window which contains positive and negative
 * spectrum histograms, along with buttons to control the display.
 * 
 * 
 * @author smitht
 *
 */

public class HistogramsWindow extends JFrame {
	private HistogramsPlot plot;
	ZoomableChart zPlot;
	private JSlider brightnessSlider;
	private int collID;
	
	// default minimum and maximum of the zoom window
	private int defMin = 0, defMax = 300;
	
	private JPanel plotPanel, buttonPanel;
	
	
	
	public HistogramsWindow(int collID) {
		super("Spectrum Histogram");
		
		this.collID = collID;
		
		String datatype = HistogramsPlot.getDB().getCollection(collID)
								.getDatatype();
		if (! datatype.equals("ATOFMS"))
			throw new IllegalArgumentException(
					"Can't handle non-ATOFMS datatypes.");
		
		setLayout(new BorderLayout());
		plotPanel = new JPanel();
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		this.add(plotPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.EAST);
		
		/*
		 * This is really drawn-out, I'm sorry.
		 */
		try {
			setUpPlot();
			// The datamousejkjkdjfkjdkf listens to the plot.  the redirector
			// listens to the datamouseuijdfij.  the brush manager and the
			// zoomablechart both listen to the redirector, which, given
			// some event, sends it to one of those two, depending on what the
			// user has selected.
			DataMouseEventTranslator dtrans = new DataMouseEventTranslator();
			plot.addMouseListener(dtrans);
			plot.addMouseMotionListener(dtrans);
			MouseRedirector mode = new MouseRedirector("Mouse Function");
			dtrans.addMouseListener(mode);
			dtrans.addMouseMotionListener(mode);
			
			mode.addMouseMode("Zoom", zPlot);
			
			final BrushManager brusher = new BrushManager(plot);
			mode.addMouseMode("Brush", brusher);

			buttonPanel.add(mode);
			
			JButton clearBrush = new JButton("Clear selection");
			buttonPanel.add(clearBrush);
			clearBrush.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					brusher.clearSelection();
				}
			});
			
			buttonPanel.add(new HistogramMouseDisplay(plot));
			JButton zdef, zout;
			zdef = new JButton("Zoom Default");
			zdef.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					zPlot.zoomOut();
				}
			});
			buttonPanel.add(zdef);
			
			zout = new JButton("Zoom out");
			zout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					zPlot.zoomOutHalf();
				}
			});
			buttonPanel.add(zout);
			
			/*
			 * Sliders are remarkably difficult to set up prettily.
			 */
			brightnessSlider = new JSlider(0, 50);
			brightnessSlider.setName("brightness");
			brightnessSlider.setMajorTickSpacing(25);
			Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
			labels.put(new Integer(0), new JLabel("Light"));
			labels.put(new Integer(50), new JLabel("Dark"));
			brightnessSlider.setLabelTable(labels);
			brightnessSlider.setPaintLabels(true);
			brightnessSlider.setPaintTicks(true);
			addActionListeners(plot, brightnessSlider);
			brightnessSlider.setValue(25);
			
			buttonPanel.add(brightnessSlider);
		} catch (SQLException e) {
			plotPanel.add(new JTextArea(e.toString()));
		}
		
		buttonPanel.add(Box.createHorizontalStrut(150));
		
//		setPreferredSize(new Dimension(700, 700));
		validate();
		pack();
	}
	
	/**
	 * Traverses the tree of components under the HistogramsPlot, adding any
	 * HistogramsChartAreas to the list of ChangeListeners for the brightness
	 * slider.
	 */
	private void addActionListeners(HistogramsPlot comp, JSlider slider) {
		for (Component c : Useful.findAll(HistogramsChartArea.class, comp)) {
			slider.addChangeListener((ChangeListener) c);
		}
	}

	public static void main(String[] args) throws SQLException {
		HistogramsWindow grr = new HistogramsWindow(2); // some collection.

		grr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		grr.setVisible(true);
	}

	/**
	 * Sets up the HistogramsPlot object and puts it into a ZoomableChart
	 * @throws SQLException
	 */
	protected void setUpPlot() throws SQLException {
		plot = new HistogramsPlot(collID);
		plot.setTitle("Spectrum histogram for collection #" + collID);
		zPlot = new ZoomableChart(plot);

		zPlot.setCScrollMin(defMin);
		zPlot.setCScrollMax(defMax);
		
		plotPanel.add(zPlot);
		
		// this is a little silly: zoomablechart registers itself as a listener
		// of the plot, but we only want it sometimes, so we unregister it
		// and register it on the MouseRedirector, below.
		plot.removeMouseListener(zPlot);
		plot.removeMouseMotionListener(zPlot);
	}
}
