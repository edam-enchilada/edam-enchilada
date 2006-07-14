package chartlib.hist;

import gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;

import collection.Collection;
import chartlib.*;
import database.*;
import ATOFMS.ParticleInfo;
import analysis.*;


public class HistogramPlot extends Chart {
	PeakHistogramChartArea chartArea = new PeakHistogramChartArea();

	public static void main(String[] args) {
		JFrame grr = new JFrame("woopdy doo");
		grr.setLayout(new BorderLayout());
		
		HistogramPlot p = new HistogramPlot(24);
		
		grr.getContentPane().add(p,BorderLayout.CENTER);
		grr.validate();
		grr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		grr.setPreferredSize(new Dimension(400, 400));
		grr.pack();
		grr.setVisible(true);
	}
}
