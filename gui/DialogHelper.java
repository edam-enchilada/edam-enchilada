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
 * The Original Code is EDAM Enchilada's ClusterDialog class.
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
 * Tom Bigwood tom.bigwood@nevelex.com
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
 * Created on May 26, 2010
 */
package gui;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class with code commonly used by dialog boxes.
 * 
 * @author jtbigwoo
 *
 */
public class DialogHelper {

	/**
	 * Parses a String of numbers, hyphens, and dashes to get out integer values.
	 * It works like this: input: 8-10, output: 8,9,10; input: 1-2,12, output: 1,2,12;
	 * Only works on positive numbers
	 * @param rangeString
	 * @return
	 * @throws NumberFormatException throws an exception if we don't have the right format
	 */
	public static Set<Integer>getRangeValuesFromString(String rangeString) throws NumberFormatException {
		//using a set because it guarantees uniqueness
		SortedSet<Integer> rangeValues = new TreeSet<Integer>();
//		try {
//			rangeValues.add(Integer.parseInt(rangeString));
//		}
//		catch (NumberFormatException exception) {
		// strip spaces
		String kText = rangeString.replaceAll(" ", "");
		// match to patterns like "12", "12-20", "12,13,15", "12-20,22-30", "12-20,22"
		Pattern p = Pattern.compile("[0-9]+(\\-[0-9]+)*(,[0-9]+(\\-[0-9]+)*)*");
		Matcher m = p.matcher(kText);
		if (m.matches()) {
			String[] rangeStrings = kText.split(",");
			for (String range : rangeStrings) {
				int start = Integer.parseInt(range.split("-")[0]);
				int end = range.split("-").length == 2 ? Integer.parseInt(range.split("-")[1]) : start; 
				if (start > end) {
					throw new NumberFormatException("Invalid number range. " + start + " is greater than " + end);
				}
				for (int i = start; i <= end; i++) {
					rangeValues.add(i);
				}
			}
		}
		else {
			throw new NumberFormatException("Invalid number range input.");
		}
//		}
		return rangeValues;
		

	}
}
