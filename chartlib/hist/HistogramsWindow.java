package chartlib.hist;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import java.util.*;
import java.util.List;

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

public class HistogramsWindow extends JFrame implements ActionListener {
	private HistogramsPlot plot;
	ZoomableChart zPlot;
	private JSlider brightnessSlider;
	
	// default minimum and maximum of the zoom window
	private int defMin = 0, defMax = 300;
	
	private JPanel plotPanel, buttonPanel;
	
	
	
	public HistogramsWindow(int collID) {
		super("I'm watching you!");
		
		setLayout(new BorderLayout());
		plotPanel = new JPanel();
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		this.add(plotPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.EAST);

		try {
			plot = new HistogramsPlot(collID);
			zPlot = new ZoomableChart(plot);

			zPlot.setCScrollMin(defMin);
			zPlot.setCScrollMax(defMax);
			
			plotPanel.add(zPlot);
			
			buttonPanel.add(new HistogramMouseDisplay(plot));
			JButton zdef, zout;
			zdef = new JButton("Zoom Default");
			zdef.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					zPlot.zoom(defMin, defMax);
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
			
			brightnessSlider = new JSlider(0, 50);
			brightnessSlider.setName("brightness");
			brightnessSlider.setMajorTickSpacing(25);
			Hashtable labels = new Hashtable();
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
//		plotPanel.setPreferredSize(new Dimension(500, 500));
		
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


	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws SQLException {
		HistogramsWindow grr = new HistogramsWindow(2);

		grr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		grr.setVisible(true);
	}
}
