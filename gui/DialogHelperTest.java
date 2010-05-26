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

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

public class DialogHelperTest extends TestCase {

	public DialogHelperTest(String s) {
		super(s);
	}
	
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {

	}

	/**
	 * Tests single number strings, both positive and negative
	 */
	public void testGetRangeValuesFromStringSingleNumber() {
		Set<Integer> returnValues;
		boolean exceptionHappened = false;
		
		returnValues = DialogHelper.getRangeValuesFromString("1");
		assertEquals(1, returnValues.size());
		assertEquals(1, returnValues.iterator().next().intValue());
	
		returnValues = DialogHelper.getRangeValuesFromString(" 13 ");
		assertEquals(1, returnValues.size());
		assertEquals(13, returnValues.iterator().next().intValue());

		returnValues = DialogHelper.getRangeValuesFromString("00013");
		assertEquals(1, returnValues.size());
		assertEquals(13, returnValues.iterator().next().intValue());

		try {
			returnValues = DialogHelper.getRangeValuesFromString("-12 ");
		}
		catch (NumberFormatException nfe) {
			assertEquals("Invalid number range input.", nfe.getMessage());
			exceptionHappened = true;
		}
		if (!exceptionHappened) {
			fail("Failed to generate exception for negative int value");
		}
	}

	/**
	 * Tests number ranges
	 */
	public void testGetRangeValuesFromStringRanges() {
		Set<Integer> returnValues;
		Iterator<Integer> returnIterator;
		boolean exceptionHappened = false;
		
		returnValues = DialogHelper.getRangeValuesFromString("15 - 17");
		assertEquals(3, returnValues.size());
		returnIterator = returnValues.iterator();
		assertEquals(15, returnIterator.next().intValue());
		assertEquals(16, returnIterator.next().intValue());
		assertEquals(17, returnIterator.next().intValue());
		
		returnValues = DialogHelper.getRangeValuesFromString("15-15");
		assertEquals(1, returnValues.size());
		assertEquals(15, returnValues.iterator().next().intValue());

		try {
			returnValues = DialogHelper.getRangeValuesFromString("17 - 15 ");
		}
		catch (NumberFormatException nfe) {
			assertEquals("Invalid number range. 17 is greater than 15", nfe.getMessage());
			exceptionHappened = true;
		}
		if (!exceptionHappened) {
			fail("Failed to generate exception for backwards range");
		}
	}

	/**
	 * Tests number lists
	 */
	public void testGetRangeValuesFromStringLists() {
		Set<Integer> returnValues;
		Iterator<Integer> returnIterator;
		boolean exceptionHappened = false;
		
		returnValues = DialogHelper.getRangeValuesFromString("12,14, 17");
		assertEquals(3, returnValues.size());
		returnIterator = returnValues.iterator();
		assertEquals(12, returnIterator.next().intValue());
		assertEquals(14, returnIterator.next().intValue());
		assertEquals(17, returnIterator.next().intValue());
		
		returnValues = DialogHelper.getRangeValuesFromString(" 154,154");
		assertEquals(1, returnValues.size());
		assertEquals(154, returnValues.iterator().next().intValue());

		try {
			returnValues = DialogHelper.getRangeValuesFromString("12,14,-17");
		}
		catch (NumberFormatException nfe) {
			assertEquals("Invalid number range input.", nfe.getMessage());
			exceptionHappened = true;
		}
		if (!exceptionHappened) {
			fail("Failed to generate exception for negative numbers in a list");
		}
	}

	/**
	 * Tests number lists and ranges mixed together
	 */
	public void testGetRangeValuesFromStringListsRanges() {
		Set<Integer> returnValues;
		Iterator<Integer> returnIterator;
		boolean exceptionHappened = false;
		
		returnValues = DialogHelper.getRangeValuesFromString("12  ,  14 - 17");
		assertEquals(5, returnValues.size());
		returnIterator = returnValues.iterator();
		assertEquals(12, returnIterator.next().intValue());
		assertEquals(14, returnIterator.next().intValue());
		assertEquals(15, returnIterator.next().intValue());
		assertEquals(16, returnIterator.next().intValue());
		assertEquals(17, returnIterator.next().intValue());
		
		returnValues = DialogHelper.getRangeValuesFromString("125-126,14-17");
		assertEquals(6, returnValues.size());
		returnIterator = returnValues.iterator();
		assertEquals(14, returnIterator.next().intValue());
		assertEquals(15, returnIterator.next().intValue());
		assertEquals(16, returnIterator.next().intValue());
		assertEquals(17, returnIterator.next().intValue());
		assertEquals(125, returnIterator.next().intValue());
		assertEquals(126, returnIterator.next().intValue());
		
		returnValues = DialogHelper.getRangeValuesFromString("154-154,154");
		assertEquals(1, returnValues.size());
		assertEquals(154, returnValues.iterator().next().intValue());

		try {
			returnValues = DialogHelper.getRangeValuesFromString("12-14,-17");
		}
		catch (NumberFormatException nfe) {
			assertEquals("Invalid number range input.", nfe.getMessage());
			exceptionHappened = true;
		}
		if (!exceptionHappened) {
			fail("Failed to generate exception for negative numbers in a list");
		}
	}

	public void tearDown() {
		
	}
}
