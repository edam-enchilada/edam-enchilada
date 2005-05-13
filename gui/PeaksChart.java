/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's PeaksChart class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Jonathan Sulman sulmanj@carleton.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */


/*
 * Created on Mar 7, 2005
 */
package gui;

import javax.swing.*;

import msanalyze.CalInfo;
import msanalyze.ReadSpec;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import chartlib.Chart;
import chartlib.DataPoint;
import chartlib.Dataset;
import chartlib.ZoomableChart;
import database.SQLServerDatabase;
import atom.ATOFMSParticle;
import atom.Peak;

import java.awt.*;
import java.awt.event.*;

/**
 * @author sulmanj
 *
 * A chart specialized for this SpASMS.
 * Deals directly with Peak data.
 * Contains a JTable that displays the text of the peak data displayed in the
 * chart.
 */
public class PeaksChart extends JPanel implements MouseMotionListener, ActionListener {
	private Chart chart;
	private ZoomableChart zchart;
	private JTable table; 
	private JRadioButton peakButton, specButton;
	private javax.swing.table.AbstractTableModel datamodel;
	private ArrayList<atom.Peak> peaks;
	private ArrayList<Peak> posPeaks;
	private ArrayList<Peak> negPeaks;
	private Dataset posSpecDS, negSpecDS;
	private int atomID;
	private String atomFile;
	
	private boolean spectrumLoaded = false;
	private static final int SPECTRUM_RESOLUTION = 1;
	
	/**
	 * Makes a new panel containing a zoomable chart and a table of values.
	 * Both begin empty.
	 * @param chart
	 */
	public PeaksChart() {
		peaks = new ArrayList<Peak>();
		atomFile = null;
		
		setLayout(new BorderLayout());

		//		sets up chart
		chart = new chartlib.Chart(2);
		chart.setHasKey(false);
		chart.setTitle("Positive and negative peak values");
		chart.setTitleX(0,"Positive mass-to-charge ratios");
		chart.setTitleY(0,"Height");
		chart.setTitleY(1,"Height");
		chart.setTitleX(1,"Negative mass-to-charge ratios");
		chart.setAxisBounds(0,400, Chart.CURRENT_VALUE, Chart.CURRENT_VALUE);
		chart.setNumTicks(10,10, 1,1);
		chart.setBarWidth(3);
		chart.setColor(Color.red);
		
		zchart = new ZoomableChart(chart);
		zchart.addMouseMotionListener(this);
		zchart.setFocusable(true);
		add(zchart, BorderLayout.CENTER);
		
		//sets up table
		datamodel = new PeaksTableModel();
		table = new JTable(datamodel);
		table.setFocusable(false);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.setPreferredScrollableViewportSize(new Dimension(300, 200));
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new GridLayout(2,1));
		ButtonGroup bg = new ButtonGroup();
		
		peakButton = new JRadioButton("Peaks");
		peakButton.setActionCommand("peaks");
		peakButton.addActionListener(this);
		specButton = new JRadioButton("Spectrum");
		specButton.setActionCommand("spectrum");
		specButton.addActionListener(this);
		bg.add(peakButton);
		bg.add(specButton);
		bg.setSelected(peakButton.getModel(),true);
		buttonPanel.add(peakButton);
		buttonPanel.add(specButton);
		
		rightPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(rightPanel, BorderLayout.EAST);
	}

	/**
	 * Sets the chart to display a new set of peaks.
	 * @param newPeaks The new peaks to display.
	 * @param title atomID The id number of the atom the peaks are from.
	 * @param filename The path of the file in which the data resides.  Null if no file
	 * is found or desired.  This file is used for importing the full spectrum.
	 */
	public void setPeaks(ArrayList<Peak> newPeaks, int atomID, String filename)
	{
		this.atomID = atomID;
		this.atomFile = filename;
		spectrumLoaded = false;
		peaks = newPeaks;
		posPeaks = new ArrayList<Peak>();
		negPeaks = new ArrayList<Peak>();
		
		//loads peaks
		for (Peak p : peaks)
		{
			if(p.massToCharge > 0){
				posPeaks.add(p);
			}
			else{
				negPeaks.add(p);
			}
		}
		
		//sets up chart to detect mouse hits on peaks
		double[] xCoords = new double[posPeaks.size()];
		for(int i = 0; i<xCoords.length; i++)
		{
			xCoords[i] = posPeaks.get(i).massToCharge;
		}
		chart.setHitDetectCoords(0,xCoords);
		
		xCoords = new double[negPeaks.size()];
		for(int i = 0; i<xCoords.length; i++)
		{
			xCoords[i] = -negPeaks.get(i).massToCharge;
		}
		chart.setHitDetectCoords(1,xCoords);
		
		
		if(peakButton.isSelected())
			displayPeaks();
		else if(specButton.isSelected())
			displaySpectrum();
		
		//chart.setDataDisplayType(true, false);
		
		chart.packData(false, true); //updates the Y axis scale.
		chart.setTitle("Particle " + atomID);
		datamodel.fireTableDataChanged();
		
		unZoom();
	}
	
	/**
	 * Returns the chart to its original zoom.
	 *
	 */
	public void unZoom()
	{
		chart.setAxisBounds(0,400, Chart.CURRENT_VALUE, Chart.CURRENT_VALUE);
		//chart.setTicks(20,Chart.CURRENT_VALUE,1,1);
		chart.packData(false, true);
	}
	
	
	
		
	/**
	 * Checks if the mouse is near a peak.  If so, highlights it
	 * in the table.
	 */
	public void mouseMoved(MouseEvent e)
	{
		if(peaks.isEmpty()) return;
		
		
		Point mousePoint = e.getPoint();  //the mouse location
		Peak peak;
		int chartIndex = chart.getChartIndexAt(mousePoint); //the chart the mouse is point at
		if(chartIndex == -1) return; //mouse not pointing at a chart -> don't do anything
		
		//java.awt.geom.Point2D.Double dataPoint = chart.getDataValueForPoint(chartIndex, mousePoint);
		Double dp = chart.getBarForPoint(chartIndex, mousePoint);
		int multiplier, adder = 0;
		
		table.clearSelection();
		
		if(dp != null)
		{
			
			//System.out.println("Point detecting");
			ArrayList<Peak> peaks;
			if(chartIndex == 0)  // in positive peaks chart
			{
				peaks = posPeaks;
				multiplier = 1;
				adder = negPeaks.size(); //positive peaks are after neg peaks in table
			}
			else //in negative peaks chart
			{
				peaks = negPeaks;
				multiplier = -1; //peaks have negative values, so mult by -1
			}
			
			//checks each peak for a match
			for(int count = 0; count < peaks.size(); count++)
			{
				peak = peaks.get(count);
				
				if(peak.massToCharge == multiplier * dp)
					table.addRowSelectionInterval(count + adder, count + adder);
				
			}
		}
	}
	public void mouseDragged(MouseEvent e){}
	
	public void actionPerformed(ActionEvent e)
	{
		if(peaks == null)
		{
			return;
		}
		String command  = e.getActionCommand();
		if(command.equals( "peaks" ))
			displayPeaks();
		else if(command.equals( "spectrum" ))
			displaySpectrum();
	}
	
	/**
	 * Sets the chart to display peaks.
	 */
	public void displayPeaks()
	{
		Dataset negDS = new Dataset(), posDS = new Dataset();
		for(Peak p : posPeaks)
		{
			posDS.add(
					new DataPoint(p.massToCharge, p.height));
		}
		for(Peak p : negPeaks)
		{
			negDS.add(
					new DataPoint(-p.massToCharge, p.height));
		}

		
		chart.setDataset(0,posDS);
		chart.setDataset(1,negDS);
		
		chart.setDataDisplayType(true, false);
	}
	
	public void displaySpectrum()
	{
		if(!spectrumLoaded)
		try{
			getSpectrum();
		}
		catch (Exception e)
		{
			System.err.println("Error loading spectrum");
		}

		chart.setDataset(1,negSpecDS);
		chart.setDataset(0,posSpecDS);
		chart.packData(false, true);
		chart.setDataDisplayType(false, true);
	}
	
	/**
	 * Fetches the spectrum from the data file
	 */
	private void getSpectrum()
	{
		ResultSet rs;
		int origDataSetID=0;
		String massCalFile;
		boolean autocal;
		ATOFMSParticle particle;
		
		/*
		 * Procedure:
		 * (1) Use atomID to get OrigDataSet.
		 * (2) Use OrigDataSet to get Calibration data
		 * (3) Create CalInfo object
		 * (4) create ATOFMS particle
		 */
		
		//Get OrigDataSet from database
		
		SQLServerDatabase db = MainFrame.db;
		Connection con = db.getCon();
		
		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT OrigDataSetID\n" +
										"FROM OrigDataSets\n" +
										"WHERE AtomID = " +
										atomID);
			rs.next();
			origDataSetID = rs.getInt("OrigDataSetID");
			} catch (SQLException e) {
				//System.err.println("Exception getting OrigDataSetID");
				JOptionPane.showMessageDialog(null,
						"An error occured retrieving the dataset ID from the database.",
						"Spectrum Display Error",JOptionPane.ERROR_MESSAGE);
				peakButton.setSelected(true);
				return;
			}	
			
			
		//Get Calibration data
		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT *\n" +
					"FROM PeakCalibrationData\n" +
					"WHERE DataSetID = " +
					origDataSetID);
			rs.next();
			massCalFile = rs.getString("MassCalFile");
			autocal = rs.getBoolean(6);
			} catch (SQLException e) {
				//System.err.println("Exception getting calibration data");
				JOptionPane.showMessageDialog(null,
						"An error occured while retrieving the calibration data from the database.",
						"Spectrum Display Error", JOptionPane.ERROR_MESSAGE);
				peakButton.setSelected(true);
				return;
			}
		
			//create CalInfo object
		try{
			ATOFMSParticle.currCalInfo = new CalInfo(massCalFile, autocal);
		} catch (java.io.IOException e)
		{
			//System.err.println("Exception opening calibration file");
			JOptionPane.showMessageDialog(null,
					"An error occurred while opening the calibration file.\n"
					+ e.toString(),
					"Spectrum Display Error", JOptionPane.ERROR_MESSAGE);
			peakButton.setSelected(true);
			return;
		}
		
		//read spectrum
		try {
			particle = new ReadSpec(atomFile).getParticle();
		} catch (Exception e)
		{
			//System.err.println("Exception opening atom file");
			JOptionPane.showMessageDialog(null,
					"An error occurred while opening the atom file.\n"
					+ e.toString(),
					"Spectrum Display Error", JOptionPane.ERROR_MESSAGE);
			peakButton.setSelected(true);
			return;
		}
		
		//	create dataset
		DataPoint[] posDP = particle.getPosSpectrum();
		DataPoint[] negDP = particle.getNegSpectrum();
		
		posSpecDS = new Dataset();
		negSpecDS = new Dataset();
		//get rid of some points, for efficiency
		for(int i=0; i < posDP.length; i+=SPECTRUM_RESOLUTION)
		{
			posSpecDS.add(posDP[i]);
		}
		for(int i=0; i < negDP.length; i+=SPECTRUM_RESOLUTION)
		{
			negSpecDS.add(negDP[i]);
		}
		
	}
	
	
	
	/**
	 * Handles the data for the table.
	 * @author sulmanj
	 */
	private class PeaksTableModel extends javax.swing.table.AbstractTableModel
	{
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 3618133463824348212L;

		public int getColumnCount() {
			
			return 4;
		}

		public int getRowCount() {
			
			return peaks.size();
		}

		public Object getValueAt(int row, int column) {
			Peak peak;
			if(row < negPeaks.size())
				peak = negPeaks.get(row);
			else
				peak = posPeaks.get(row - negPeaks.size());
			switch(column)
			{
			// Putting these in wrapper classes, hope this
			// helps 
			// -Ben
			case 0: return new Double(peak.massToCharge);
			case 1: return new Integer(peak.height);
			case 2: return new Integer(peak.area);
			case 3: return new Float(peak.relArea);
			default: return null;
			}
		}
		
		public String getColumnName(int column)
		{
			switch(column)
			{
			case 0: return "Location";
			case 1: return "Height";
			case 2: return "Area";
			case 3: return "Relative Area";
			default: return "";
			}
		}
	}
}
