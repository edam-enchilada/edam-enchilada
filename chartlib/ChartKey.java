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
 * The Original Code is EDAM Enchilada's ChartKey class.
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
 * Created on Feb 1, 2005
 *
 */
package chartlib;

import javax.swing.*;
import java.awt.*;

/**
 * @author sulmanj
 *
 * Manages and displays the key. 
 * 
 */
public class ChartKey extends JPanel 
{
	private int numElements;
	private JLabel[] keyTitles;
	private JPanel[] colorBoxes;
	
	
	public ChartKey()
	{
		numElements = 0;
	}
		
	
	/**
	 * Make a key with a number of sets with default colors and titles.
	 * @param numSets The number of sets.
	 */
	public ChartKey(int numSets)
	{
		numElements = numSets;
		keyTitles = new JLabel[numSets];
		colorBoxes = new JPanel[numSets];
		setupLayout();
	}
	
	/**
	 * Sets the title of a data set.
	 * @param index The index of the data title to change.
	 * @param title The new title.
	 */
	public void setTitle(int index, String title)
	{
		keyTitles[index].setText(title);
		repaint();
	}
	
	/**
	 * Sets the color of a dataset.
	 * @param index The index of the dataset on the key.
	 * @param c The new color.
	 */
	public void setColor(int index, Color c)
	{
		colorBoxes[index].setBackground(c);
		repaint();
	}
	
	/**
	 * Sets up the key with default attributes for the current number of sets.
	 *
	 */
	private void setupLayout()
	{
			//don't draw a key if there are no datasets.
		if(numElements == 0)
			return;
		
		//	layout: a vertical BoxLayout with a 
		// horizontal BoxLayout JPanel for each element.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Key"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		
		JPanel datasetPanel = new JPanel();
		
		for(int count = 0; count < numElements; count++)
		{
			
			datasetPanel = new JPanel();
			datasetPanel.setLayout(new BoxLayout(datasetPanel,BoxLayout.X_AXIS));
			datasetPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			
			//	color label
			colorBoxes[count] = new JPanel();
			colorBoxes[count].setMaximumSize(new Dimension(10,10));
			colorBoxes[count].setMinimumSize(new Dimension(10,10));
			colorBoxes[count].setPreferredSize(new Dimension(10,10));
			//use default color
			colorBoxes[count].setBackground(
					Chart.DATA_COLORS[count % Chart.DATA_COLORS.length]);
			
			datasetPanel.add(colorBoxes[count]);
			//spacer between color and name
			datasetPanel.add(Box.createRigidArea(new Dimension(10,10)));
			
			//titles
			keyTitles[count] = new JLabel("Dataset " + (count + 1));
			keyTitles[count].setFont(new Font("Geneva",Font.PLAIN,10));
			datasetPanel.add(keyTitles[count]);
			
			//finally, add to key
			add(datasetPanel);
			add(Box.createRigidArea(new Dimension(10,10)));
		}
		
		setMaximumSize(getPreferredSize());
	}
}
