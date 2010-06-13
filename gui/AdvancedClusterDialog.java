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
 * The Original Code is EDAM Enchilada's AdvancedClusterDialog class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
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
 * Created on Dec 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import analysis.clustering.Cluster;
import analysis.clustering.ClusterK;
import java.awt.*;
/**
 *
 * 
 *TODO:  Problems entering text, getting the buttons to work, and closing the window.
 *
 * @author ritza
 * @author jtbigwoo
 */
public class AdvancedClusterDialog extends JDialog implements ActionListener {
	private JDialog parent;
	
	private float origError;
	private int origSampleNum;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton restoreDefault;
	
	private JTextField errorField;
	private JTextField numSamplesField;
	private JTextField powerField;
	private JTextField smallestNormalizedPeakField;
	
	private JTextField randomSeedField;
	
	public AdvancedClusterDialog(JDialog frame) {
		super(frame,"Advanced Cluster Options", true);
		parent = frame;
		setSize(350,240);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JLabel kHeader = new JLabel("K-Cluster:");
		
		JPanel k = new JPanel();
		JLabel kError = new JLabel("error:");
		String initialError = Float.toString(ClusterK.getError());
		errorField = new JTextField(initialError, 5);
		errorField.setEditable(true);
		JLabel kSampNum = new JLabel("# of subsamples:");
		String initialNumSamples = Integer.toString(ClusterK.getNumSamples());
		numSamplesField = new JTextField(initialNumSamples, 5);
		numSamplesField.setEditable(true);
		k.add(kError);
		k.add(errorField);
		k.add(kSampNum);
		k.add(numSamplesField);
		
		/*A section for setting the preprocessing power for peaks.*/
		JLabel preProcess = new JLabel("Preprocessing parameters:");		
		JPanel p = new JPanel();
		JLabel peakPower = new JLabel("peak power:");
		String initialPower = Double.toString(0.5);//get the current power
		powerField = new JTextField(initialPower, 5);
		p.add(peakPower);
		p.add(powerField);
		JPanel p2 = new JPanel();
		JLabel smallestPeakLabel = new JLabel("Smallest normalized peak to include:");
		String initialSmallestPeak = Double.toString(0.0001);//get the current power
		smallestNormalizedPeakField = new JTextField(initialSmallestPeak, 5);
		p2.add(smallestPeakLabel);
		p2.add(smallestNormalizedPeakField);
		JPanel p3 = new JPanel();
		JLabel randomSeedLabel = new JLabel("Integer seed number:");
		randomSeedField = new JTextField(Integer.toString(ClusterK.DEFAULT_RANDOM), 20);
		p3.add(randomSeedLabel);
		p3.add(randomSeedField);
		
		/*Add the buttons to the bottom of the dialog*/
		JPanel buttons = new JPanel();
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		restoreDefault = new JButton("Restore Default");
		restoreDefault.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);
		buttons.add(restoreDefault);
		
		add(kHeader);
		add(k);
		add(preProcess);
		add(p);
		add(p3);
		add(p2);
		add(buttons);
		setLayout(new FlowLayout());
		
		getRootPane().setDefaultButton(okButton);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == okButton) {
			try {
			String text = errorField.getText();
			ClusterK.setError(Float.parseFloat(text));
			text = numSamplesField.getText();
			ClusterK.setNumSamples(Integer.parseInt(text));
			text = powerField.getText();
			Cluster.setPower(Double.parseDouble(text));
			text = smallestNormalizedPeakField.getText();
			Cluster.setSmallestNormalizedPeak(Float.parseFloat(text));
			text = randomSeedField.getText();
			ClusterK.setRandomSeed(Integer.parseInt(text));
			} catch (Exception exception) {
				JOptionPane.showMessageDialog(parent,
						"Error with parameters.\n" +
						"Make sure there are no empty entries,\n" +
						"the 'error' field contains a real number,\n" +
						"the '# of subsamples' field contains an integer,\n" +
						" and the 'peak power' field contains a decimal number.",
						"Exception",
						JOptionPane.ERROR_MESSAGE);
			}
			dispose();
		}
		if (source == cancelButton) {
			dispose();
		}
		if(source == restoreDefault) {
			errorField.setText("0.01");
			numSamplesField.setText("10");
			powerField.setText("0.5");
			randomSeedField.setText(Integer.toString(ClusterK.DEFAULT_RANDOM));
		}
	}
}
