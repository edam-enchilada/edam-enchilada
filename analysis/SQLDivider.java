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
 * The Original Code is EDAM Enchilada's SQLDivider class.
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
 * Created on Oct 19, 2004
 */
package analysis;

import database.InfoWarehouse;

/**
 * SQLDivider implements the abstract methods of CollectionDivider.
 * It results in a single subcollection of the collection you are 
 * dividing which contains only those particles from the super 
 * collection which meet the WHERE criteria.  Make sure the clause is
 * valid because it will not be checked by SpASMS.  The collection 
 * description is generated from the WHERE clause as well.  If you 
 * get errors, you may have to remove reserved characters from it 
 * before setting the Description using the private method 
 * removeReservedCharacters() if the string contains any characters
 * that will interrupt SQL Server's reading it as a string.  
 * 
 * @author andersbe
 */
public class SQLDivider extends CollectionDivider {
	private String select, where;
	/**
	 * Create an SQLDivider which will divide based on your WHERE
	 * clause (don't include the WHERE). 
	 * @param collectionID
	 * @param database
	 * @param name
	 * @param comment
	 * @param where What you want after the WHERE
	 */
	public SQLDivider(int collectionID, InfoWarehouse database,
					  String name, String comment, String where)
	{
		super(collectionID, database, name, comment);
		this.where = where;
	}

	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#setCursorType(int)
	 */
	public boolean setCursorType(int type) {
		if (type == DISK_BASED)
		{
			curs = db.getSQLCursor(collectionID, where);
			return true;
		}
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see analysis.CollectionDivider#divide()
	 */
	public int divide() {
		while (curs.next())
		{
			int temp = curs.getCurrent().getParticleInfo().getAtomID();
			putInHostSubCollection(temp);
		}
		db.setCollectionDescription(newHostID, "Divided on:\n" +
				where);
		return newHostID;
	}
}
