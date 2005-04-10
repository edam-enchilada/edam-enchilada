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
import java.util.ArrayList;

import chartlib.Chart;
import chartlib.DataPoint;
import chartlib.Dataset;
import chartlib.ZoomableChart;
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
public class PeaksChart extends JPanel implements MouseMotionListener {
	private Chart chart;
	private ZoomableChart zchart;
	private JTable table; 
	private javax.swing.table.AbstractTableModel datamodel;
	private ArrayList<atom.Peak> peaks;
	private ArrayList<Peak> posPeaks;
	private ArrayList<Peak> negPeaks;
	
	/**
	 * Makes a new panel containing a zoomable chart and a table of values.
	 * Both begin empty.
	 * @param chart
	 */
	public PeaksChart() {
		peaks = new ArrayList<Peak>();
		
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
		//table.setCellSelectionEnabled(false);
		add(new JScrollPane(table), BorderLayout.EAST);
	}

	/**
	 * Sets the chart to display a new set of peaks.
	 * @param newPeaks The new peaks to display.
	 * @param title The title of the peak set or particle.
	 */
	public void setPeaks(ArrayList<Peak> newPeaks, String title)
	{
		peaks = newPeaks;
		posPeaks = new ArrayList<Peak>();
		negPeaks = new ArrayList<Peak>();
		chartlib.Dataset chartPos = new Dataset();
		chartlib.Dataset chartNeg = new Dataset();
		for (Peak p : peaks)
		{
			if(p.massToCharge > 0){
				chartPos.add(
						new DataPoint(p.massToCharge, p.height));
				posPeaks.add(p);
			}
			else{
				chartNeg.add(
						new DataPoint(- p.massToCharge, p.height));
				negPeaks.add(p);
			}
		}
		chart.setDataset(0,chartPos);
		chart.setDataset(1,chartNeg);
		chart.packData(false, true); //updates the Y axis scale.
		chart.setTitle("Peaks for Particle " + title);
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
		DataPoint dp = chart.getDataPointForPoint(chartIndex, mousePoint);
		int multiplier, adder = 0;
		
		table.clearSelection();
		
		if(dp != null)
		{
			
			
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
				
				if(peak.massToCharge == multiplier * dp.x)
					table.addRowSelectionInterval(count + adder, count + adder);
				
			}
		}
	}
	public void mouseDragged(MouseEvent e){}
	
	
	
	
	/**
	 * Handles the data for the table.
	 * @author sulmanj
	 */
	private class PeaksTableModel extends javax.swing.table.AbstractTableModel
	{
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
