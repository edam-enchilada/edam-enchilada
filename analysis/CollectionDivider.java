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
 * The Original Code is EDAM Enchilada's CollectionDivider
 * class.
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
 * Created on Aug 19, 2004
 *
 * Ben Anderson
 */
package analysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import database.CollectionCursor;
import database.InfoWarehouse;

/**
 * @author andersbe
 *
 */
public abstract class CollectionDivider {
	
	private String atomIDsToDelete;
	
	/**
	 * Use a disk based cursor/retrieve each row from the database
	 * every time you want to access it.
	 */
	public static final int DISK_BASED = 0;

	/**
	 * Store each row in memory on the first pass and in 
	 * subsequent passes.  
	 * TODO: CURRENTLY UNTESTED!!!
	 */
	public static final int STORE_ON_FIRST_PASS = 1;

	/**
	 * The id of the collection you are dividing
	 */
	protected int collectionID;

	/**
	 * A pointer to an active InfoWarehouse
	 */
	protected InfoWarehouse db;

	/**
	 * The id of the new collection which all subdivisions will
	 * be children of
	 */
	protected int newHostID;

	/**
	 * The current number of active subcollections
	 */
	protected int numSubCollections;

	/**
	 * an array list of the IDs of each sub collection 
	 * (each collection can be refered to it by its location
	 * in the array list.
	 */
	protected ArrayList<Integer> subCollectionIDs;

	/**
	 * The CollectionCursor used to access the atoms of the 
	 * collection you are dividing.  Initialize this to one of the
	 * implementations using a get method from InfoWarehouse
	 */
	protected CollectionCursor curs = null;

	/**
	 * Construct a CollectionDivider.
	 * @param cID		The id of the collection to be divided
	 * @param database	The open InfoWarehouse to write to
	 * @param name		A name for the new host collection
	 * @param comment	A comment for the collection
	 */
	public CollectionDivider(int cID, InfoWarehouse database, String name, String comment)
	{
	    if (database == null)
	        throw new IllegalArgumentException(
	                "Parameter 'database' should not be null");
	    
		collectionID = cID;
		
		db = database;
		
		subCollectionIDs = new ArrayList<Integer>();
		
		newHostID = db.createEmptyCollection(collectionID, name, comment);
		
		numSubCollections = 0;
	}

	/**
	 * Implement this method so it returns true on supported types
	 * and false on unsupported.  This method must be called 
	 * before calling divide.  If the type is supported, 
	 * initialize curs using one of the InfoWarehouse getCursor
	 * methods
	 * 
	 * @param type	DISK_BASED or STORE_ON_FIRST_PASS, or others
	 * @return true if the cursor type is supported, false 
	 * otherwise
	 */
	abstract public boolean setCursorType(int type);

	/**
	 * Creates a new subcollection and returns an int by which 
	 * the subcollection can be referred to when putting particles
	 * into it.  
	 * 
	 * @return the handle by which the subcollection should be 
	 * referred to.
	 */
	protected int createSubCollection()
	{
		int subCollectionID = 0;
		numSubCollections++;
		subCollectionID = db.createEmptyCollection(newHostID,
				Integer.toString(numSubCollections), 
				Integer.toString(numSubCollections));
		assert (subCollectionID != -1) : "Error creating empty subcollection";
		subCollectionIDs.add(new Integer(subCollectionID));
		return numSubCollections;
	}

	/**
	 * Creates an empty sub Collection with a name and comments.
	 * @param name
	 * @param comments
	 * @return - sub collection number
	 */
	protected int createSubCollection(
			String name, 
			String comments)
	{
		int subCollectionID = 0;
		numSubCollections++;
		subCollectionID = db.createEmptyCollection(newHostID,
				name, 
				comments);
		assert (subCollectionID != -1) :"Error creating empty subcollection: " 
			+ name + "Comments: " + comments;
		subCollectionIDs.add(new Integer(subCollectionID));
		return numSubCollections;
	}
	
	/**
	 * Takes an atom and puts it in the identified subcollection.
	 * You should take care to make sure the atom exists in the
	 * collection you're dividing before using this collection
	 * because if it doesn't exist, this function will not stop
	 * you from moving it.  
	 * 
	 * @param atomID The id of the atom you wish to move
	 * @param target The handle (returned from createSubCollection)
	 * 			of the collection you want to put the atom into.
	 * 			This handle should be the count (from 1) of the 
	 * 			order in which you added the collection.  (First 
	 * 			collection = 1, second = 2 third = 3 etc.
	 * @return
	 */
	protected boolean putInSubCollection(int atomID, int target)
	{		
		if (db.checkAtomParent(atomID,collectionID))
			return db.moveAtom(atomID, collectionID, subCollectionIDs.get(target-1).intValue());
		else
			return db.addAtom(atomID,subCollectionIDs.get(target-1).intValue());
	}
	
	/**
	 * Initializes putInSubcollectionBatch.
	 */
	protected void putInSubCollectionBatchInit() {
		atomIDsToDelete = "";
		db.atomBatchInit();
	}

	/**
	 * adds atoms to a batch that is eventually executed.
	 * @param atomID
	 * @param target
	 * @return - true if atom can be added.
	 */
	protected boolean putInSubCollectionBatch(int atomID, int target)
	{
		atomIDsToDelete = atomIDsToDelete + atomID + ",";
		return db.addAtomBatch(atomID,
				subCollectionIDs.get(target-1).intValue());
	}
	
	/**
	 * Executes putInSubCollectionBatch
	 */
	protected void putInSubCollectionBatchExecute()
	{
		//System.out.println("About to execute INSERTs.");
		//System.out.println((new Date()).toString());
		db.executeBatch();
		//System.out.println("Done with INSERTS, about to do DELETE");
		//System.out.println((new Date()).toString());
		db.atomBatchInit();
		
		if (atomIDsToDelete.length() > 0 &&
				atomIDsToDelete.length() < 2000) {
			atomIDsToDelete = atomIDsToDelete.substring(0,atomIDsToDelete.length()-1);
			db.deleteAtomsBatch(atomIDsToDelete,collectionID);
		} else if (atomIDsToDelete.length() > 0 &&
				atomIDsToDelete.length() >= 2000) {
			Scanner atomIDs = new Scanner(atomIDsToDelete).useDelimiter(",");
			while (atomIDs.hasNext()) {
				db.deleteAtomBatch(atomIDs.nextInt(), collectionID);
			}
		}
		db.executeBatch();
		//System.out.println("Done with DELETEs.");
		//System.out.println((new Date()).toString());
	}

	/**
	 * Moves atomID from parent into the main subcollection.  Use
	 * this in dividers that don't divide into multiple subcollections.
	 * @param atomID
	 * @return
	 */
	protected boolean putInHostSubCollection(int atomID)
	{
		if (db.checkAtomParent(atomID, collectionID))
			return db.moveAtom(atomID, collectionID, newHostID);
		else
			return db.addAtom(atomID, newHostID);
	}

	/**
	 * This is where the actual method of dividing the collection
	 * should take place.  This is the last method that should
	 * be called of your CollectionDivider.  Should return the 
	 * id of the host collection of the subcollections.  
	 */
	abstract public int divide();
}
