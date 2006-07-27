package chartlib.hist;

import java.awt.*;
import java.sql.SQLException;

import javax.swing.*;

import chartlib.*;

import database.SQLServerDatabase;
import externalswing.ProgressTask;

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
	private HistogramsChartArea posHistArea, negHistArea;
	private ZeroHistogramChartArea posZerosArea, negZerosArea;
	
	public HistogramsPlot(final int collID) throws SQLException {
		ProgressTask task = new ProgressTask(null, "Analysing collection", true) {
			public void run() {
				pSetInd(true);
				setStatus("Creating histogram for collection.");
				try {
					final HistogramDataset[] datasets;
					
					datasets = HistogramDataset.analyseCollection(collID, Color.BLACK);
					
					if (terminate)
						return;
					
					// EDT for gui work
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							addDatasets(datasets);
						}
					});
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		task.start();
		// task.start() does not return until the task is complete, since we
		// told the dialog to be modal.
		
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

	private void addDatasets(HistogramDataset[] datasets) {
		if (posHistArea == null) {
			posHistArea = new HistogramsChartArea(datasets[0]);
			posHistArea.setAxisBounds(0, 300, 0, 1);
			this.add(posHistArea);
		} else {
			posHistArea.addDataset(datasets[0]);
		}
		if (posZerosArea == null) {
			posZerosArea = new ZeroHistogramChartArea(datasets[0]);
			posZerosArea.setXAxisBounds(0, 300);
			this.add(posZerosArea);
		} else {
			//zerosArea.addDataset(dataset);  // not implemented yet.
			System.err.println(
			"HistogramsPlot: zerosArea doesn't know about multiple datasets yet.");
		}
		
		
		if (negHistArea == null) {
			negHistArea = new HistogramsChartArea(datasets[1]);
			negHistArea.setAxisBounds(0, 300, 0, 1);
			this.add(negHistArea);
		} else {
			negHistArea.addDataset(datasets[0]);
		}
		if (negZerosArea == null) {
			negZerosArea = new ZeroHistogramChartArea(datasets[1]);
			negZerosArea.setXAxisBounds(0, 300);
			this.add(negZerosArea);
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
