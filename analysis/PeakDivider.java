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
 * The Original Code is EDAM Enchilada's PeakDivider class.
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
 * Created on Dec 1, 2004
 */
package analysis;

import database.InfoWarehouse;

/**
 * @author andersbe
 */
public class PeakDivider extends CollectionDivider {
	private BinnedPeakList peaks;
	
	/**
	 * PeakDivider will create a subcollection of the input collecion
	 * (cID) which contains only those particles which have peaks at
	 * all of the locations in mustContain.  Area is not checked.
	 * 
	 * @param cID 			The collection to divide
	 * @param database		The InfoWarehouse to use
	 * @param name			The name for the subcollection
	 * @param comment		A comment regarding the subcollection
	 * @param mustContain	A list of peak locations that the 
	 * 						Resulting collection must contain
	 */
	public PeakDivider(
			int cID, 
			InfoWarehouse database, 
			String name, 
			String comment,
			BinnedPeakList mustContain) {
		super(cID, database, name, comment);
		peaks = mustContain;
	}

	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#setCursorType(int)
	 */
	public boolean setCursorType(int type) {
		switch (type) {
		case CollectionDivider.DISK_BASED :
			curs = db.getBinnedCursor(collectionID);
			return true;
		case CollectionDivider.STORE_ON_FIRST_PASS : 
			return false;
		default :
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#divide()
	 */
	public int divide() {
		BinnedPeakList thisParticleList;
		ParticleInfo thisParticle;
		boolean match = true;
		while (curs.next())
		{
			match = true;
			thisParticle = curs.getCurrent();
			thisParticleList = thisParticle.getBinnedList();
			int temp = thisParticle.getParticleInfo().getAtomID();
			for (int i = 0; i < peaks.length(); i++)
			{
				peaks.resetPosition();
				if (thisParticleList.getAreaAt(
						peaks.getNextLocationAndArea().location) 
						== 0)
					match = false;
			}
			if (match)
				putInHostSubCollection(temp);
		}
		return newHostID;
	}

	/*public static void main (String[] args)
	{
		InfoWarehouse db = new SQLServerDatabase();
		db.openConnection();
		
		BinnedPeakList bPL = new BinnedPeakList();
		bPL.add(30.1f,0.6f);
		PeakDivider pD = new PeakDivider(
				1, db, "only 3", "A Comment", bPL);
		pD.setCursorType(DISK_BASED);
		pD.divide();
		
		db.closeConnection();
	}*/
}
