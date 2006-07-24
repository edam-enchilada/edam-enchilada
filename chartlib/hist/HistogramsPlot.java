package chartlib.hist;

import java.awt.*;
import java.sql.SQLException;

import javax.swing.*;

import chartlib.*;

import database.SQLServerDatabase;

/**
 * Need to figure out how to do positive and negative spectra.  Maybe
 * put HistogramDataset generation here, and split them up into two datasets,
 * pos/neg.  Then make two of the chart areas.
 * <p>
 * Sure.
 * <p>
 * Then, make a HistogramsButtonPanel or something, 
 * @author smitht
 *
 */

public class HistogramsPlot extends Chart {
	private HistogramsChartArea histArea;
	private ZeroHistogramChartArea zerosArea;
	
	public HistogramsPlot(int collID) throws SQLException {
		addDataset(new HistogramDataset(collID, Color.BLACK));
		
		
		this.add(histArea);
		this.add(zerosArea);
		this.validate();
	}
	
	
	public static SQLServerDatabase getDB() {
		if (gui.MainFrame.db != null) {
			return gui.MainFrame.db;
		} else {
			SQLServerDatabase db = new SQLServerDatabase();
			db.openConnection();
			return db;
		}
	}

	private void addDataset(HistogramDataset dataset) {
		// TODO Auto-generated method stub
		if (histArea == null) {
			histArea = new HistogramsChartArea(dataset);
			histArea.setAxisBounds(0, 400, 0, 1);
		} else {
			histArea.addDataset(dataset);
		}
		
		if (zerosArea == null) {
			zerosArea = new ZeroHistogramChartArea(dataset);
			zerosArea.setXAxisBounds(0, 400);
		} else {
			//zerosArea.addDataset(dataset);  // not implemented yet.
			System.err.println(
			"HistogramsPlot: zerosArea doesn't know about multiple datasets yet.");
		}
	}


	@Override
	protected JPanel createChartPanel() {
		JPanel chartPanel = super.createChartPanel();
		chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
		chartPanel.validate();
		return chartPanel;
	}
	

}
