package chartlib.hist;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class HistogramsWindow extends JFrame implements ActionListener {
	private HistogramsPlot plot;
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
			plotPanel.add(plot);

			buttonPanel.add(new HistogramMouseDisplay(plot));
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
