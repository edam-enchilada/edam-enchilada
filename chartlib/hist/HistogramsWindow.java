package chartlib.hist;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import chartlib.*;

/**
 * A window which will (but does not yet) contain positive and negative
 * spectrum histograms, along with buttons to control the display.
 * 
 * 
 * @author smitht
 *
 */

public class HistogramsWindow extends JFrame implements ActionListener {
	private HistogramsPlot plot;
	ZoomableChart zPlot;
	
	// default minimum and maximum of the zoom window
	private int defMin = 0, defMax = 300;
	
	private JPanel plotPanel, buttonPanel;
	
	
	
	public HistogramsWindow(int collID) {
		super("I'm watching you!");
		
		setLayout(new BorderLayout());
		plotPanel = new JPanel();
		buttonPanel = new JPanel();
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
		} catch (SQLException e) {
			plotPanel.add(new JTextArea(e.toString()));
		}
		
		buttonPanel.setPreferredSize(new Dimension(150, 500));
		plotPanel.setPreferredSize(new Dimension(500, 500));
		
		validate();
		pack();
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
