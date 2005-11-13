package gui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class LabelingIon {
	public int ionID;
	public String name;
	public int[] mzVals;
	public double[] ratios;

	private JCheckBox checkBox;
	private String stringRep;

	private boolean isValidIon = true;
	private boolean isUpdatingCheckbox = false;
	
	public LabelingIon(String stringRep) {
		this.stringRep = stringRep;
		try {
			stringRep = stringRep.replaceAll("  ", " ");
			String[] tokens = stringRep.split(" ");
			name = tokens[0];
			
			int numMZVals = (tokens.length - 2) / 2;
			mzVals = new int[numMZVals];
			ratios = new double[numMZVals];

			for (int i = 0; i < numMZVals; i++) {
				mzVals[i] = Integer.parseInt(tokens[i * 2 + 1]);
				ratios[i] = Double.parseDouble(tokens[i * 2 + 1]);
			}
		} catch (NumberFormatException e) {
			System.err.println("Invalid ion string: " + stringRep);
			isValidIon = false;
		}
	}
	
	// Copy constructor... 
	public LabelingIon(LabelingIon ion) {
		stringRep = ion.stringRep;
		ionID = ion.ionID;
		name = ion.name;
		mzVals = ion.mzVals;
		ratios = ion.ratios;
	}
	
	public boolean isValid() { return isValidIon; }
	public boolean isChecked() { return checkBox.isSelected(); }
	public String toString() { return stringRep; }
	
	public void setChecked(boolean checked) { 
		isUpdatingCheckbox = true;
		checkBox.setSelected(checked); 
		isUpdatingCheckbox = false;
	}
	
	public JPanel getCheckboxPanelForIon(final ParticleAnalyzeWindow paw) {
		JPanel ionPanel = new JPanel(new BorderLayout());
		checkBox = new JCheckBox();
		checkBox.setSelected(true);
		ionPanel.add(checkBox, BorderLayout.WEST);
		ionPanel.add(new JLabel(name), BorderLayout.CENTER);
		
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (!isUpdatingCheckbox) {
					paw.doLabeling(true);
				}
			}
		});
		return ionPanel;
	}
}