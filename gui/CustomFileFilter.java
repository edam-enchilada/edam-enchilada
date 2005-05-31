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
 * The Original Code is EDAM Enchilada's CustomFileFilter class.
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
 * Created on Jul 19, 2004
 *
 */
package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author andersbe
 *
 */
public class CustomFileFilter extends FileFilter {

	private ArrayList<String> acceptType;

	/**
	 * Construct a filefilter which accepts a given file suffix.
	 * @param aType The suffix to accept (DO NOT include *.).
	 */
	public CustomFileFilter() {
		super();
		acceptType = new ArrayList<String>();
	}

	public void addFileFilter(String aType) {
		acceptType.add(aType);
	}
	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		if(f.isDirectory())
			return true;
		else
		{
			StringTokenizer strTok = new StringTokenizer(f.toString(), ".");
			String fileType = new String("");
			while(strTok.hasMoreTokens()) {
				fileType = new String(strTok.nextToken());
			}
			for (int i = 0; i < acceptType.size(); i++)
				if (fileType.equals(acceptType.get(i)))
					return true;
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		String description = "";
		for (int i = 0; i < acceptType.size(); i++) 
			description += "*." + acceptType.get(i) + " ";
		return description;
	}

}
