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
 * The Original Code is EDAM Enchilada's Collection class.
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
 * Created on Jul 15, 2004
 *
 */
package collection;

import database.*;
import gui.FilePicker;

import java.io.*;
import java.text.*;
import java.util.*;

import atom.ATOFMSAtomFromDB;


/**
 * @author gregc
 * @author andersbe
 *
 */
public class Collection {
	private int collectionID;
	private Collection parentCollection;
	
	private InfoWarehouse db = null;
	private String datatype;
	private AggregationOptions aggregationOptions;

	private String cachedName, cachedComment, cachedDescription;
	private ArrayList<Integer> cachedSubCollectionIDs = null;
	private Set<Integer> cachedCollectionIDSubTree = null;
	private Collection[] cachedSubCollections;
	private int cachedContainsData = -1;
	
	public Collection(String type, int cID, InfoWarehouse database)
	{
		collectionID = cID;
		datatype = type;
		db = database;
	}
	
	public Collection(String name, String type, int cID, InfoWarehouse database)
	{
		cachedName = name;
		collectionID = cID;
		datatype = type;
		db = database;
	}
	
	public void clearCachedChildren() {
		cachedSubCollectionIDs = null;
		cachedCollectionIDSubTree = null;
		cachedSubCollections = null;
	}
	
	public ArrayList<Integer> getSubCollectionIDs()
	{
		if (cachedSubCollectionIDs == null)
			cachedSubCollectionIDs = db.getImmediateSubCollections(this);
		
		return cachedSubCollectionIDs;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Collection) {
			return ((Collection) o).collectionID == collectionID;
		}
		
		return false;
	}
	
	public Collection getChildAt(int index) {
		ArrayList<Integer> subCollectionIDs = getSubCollectionIDs();
		
		if (cachedSubCollections == null)
			cachedSubCollections = new Collection[subCollectionIDs.size()];
		
		if (cachedSubCollections[index] == null) {
			int collectionID = subCollectionIDs.get(index).intValue();
			
			// Make sure children have correct datatype...
			// Should only share from parent nodes that aren't root
			if (datatype.contains("root"))
				cachedSubCollections[index] = db.getCollection(collectionID);
			else
				cachedSubCollections[index] = new Collection(datatype, collectionID, db);
			
			cachedSubCollections[index].setParentCollection(this);
		}
		
		return cachedSubCollections[index];
	}
	
	public Collection getParentCollection() {
		return parentCollection;
	}
	
	public void setParentCollection(Collection c) {
		parentCollection = c;
	}
	
	public int getCollectionID()
	{
		return collectionID;
	}
	
	public String getDatatype() {
		return datatype;
	}
	
	public boolean containsData() {
		if (cachedContainsData == -1)
			cachedContainsData = db.getCollectionIDsWithAtoms(getCollectionIDSubTree(), false).size();
	
		return cachedContainsData > 0;
	}
	
	public Set<Integer> getCollectionIDSubTree() {
		if (cachedCollectionIDSubTree == null) {
			cachedCollectionIDSubTree = db.getAllDescendantCollections(collectionID, true); 
		}
		
		return cachedCollectionIDSubTree;
	}
	
	public String getName() {
		if (cachedName == null)
			cachedName = db.getCollectionName(collectionID);
		
		return cachedName;
	}
	
	public String getComment() {
		if (cachedComment == null)
			cachedComment = db.getCollectionComment(collectionID);
		
		return cachedComment;
	}
	
	public String getDescription() {
		if (cachedDescription == null)
			cachedDescription = db.getCollectionDescription(collectionID);
		
		return cachedDescription;
	}
	
	public String toString()
	{
		return getName().trim();
	}
	
	public ArrayList<Integer> getParticleIDs()
	{
		return db.getAllDescendedAtoms(this);
	}
	
	public void setAggregationOptions(AggregationOptions ao) {
		aggregationOptions = ao;
	}
	
	public AggregationOptions getAggregationOptions() { 
		return aggregationOptions;
	}
	
	//TODO:  Need a progress bar here.
	public boolean exportToPar(javax.swing.JFrame parent)
	{
		String name;
		String parFileName = (new FilePicker("Choose .par file destination",
											 "par", parent)
							  ).getFileName();

		if (parFileName == null) {
			return false;
		} else if (! parFileName.endsWith(".par")) {
			parFileName = parFileName + ".par";
		}
		parFileName = parFileName.replaceAll("'", "");
		// parFileName is an absolute pathname to the par file.
		// the set file goes in the same directory
		File parFile = new File(parFileName);
		File setFile = new File(parFileName.replaceAll("\\.par$", ".set"));
		System.out.println(parFile.getAbsolutePath());
		
		// String name is the basename of the par file---the dataset name.
		// I've done trivial testing---spaces and periods seem to be ok
		// in this name.  I'm sure that single quotes aren't... so I remove
		// them above.
		File noExt = new File(parFileName.replaceAll("\\.par$", ""));
		name = noExt.getName(); // name has no path
		
	
		// the thought: set "name" to the basename of the file they choose.
		java.util.Date date = db.exportToMSAnalyzeDatabase(this, name, "MS-Analyze");
		try {
			DateFormat dFormat = 
				new SimpleDateFormat("MM/dd/yyyy kk:mm:ss");
			PrintWriter out = new PrintWriter(new FileOutputStream(parFile, false));
			out.println("ATOFMS data set parameters");
			out.println(name);
			out.println(dFormat.format(date));
			out.println(getComment());
			out.close();
			
			int particleCount = 1;
			out = new PrintWriter(new FileOutputStream(setFile, false));

			ATOFMSAtomFromDB particle = null;
			// The Atom iterator is essentially a thin wrapper to a 
			// Result set and thus you must call hasNext to get to the
			// next element of the interator as it links to 
			// ResultSet.next()
			CollectionCursor curs = db.getAtomInfoOnlyCursor(this);
			
			NumberFormat nFormat = NumberFormat.getNumberInstance();
			nFormat.setMaximumFractionDigits(6);
			nFormat.setMinimumFractionDigits(6);
			while (curs.next())
			{
				particle = curs.getCurrent().getParticleInfo();

			// the number in the string (65535) is 
			// somewhat meaningless for our purposes, this is
			// the busy 
			// delay according to the MS-Analyze manual.  So I 
			// just put in a dummy value.  I looked at a dataset
			// and it had 65535 for the busy time for every single
			// particle anyway, which leads me to believe it's 
			// actually the max value of a bin in the data (2^16)
			// TODO: Test to make sure size matches the
			// scatter delay MSA produces for the same dataset
				out.println(particleCount + "," + 
						// better would be to copy all the .amz files around
						// and use relative pathnames in a non-abusive way.
//						particle.getFilename().substring(0,1) + 
//						File.separator +
						
						//XXX: hack.  MSA only understands relative pathnames:
						"..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\" +
						// .substring(3) : the part of the path after "c:\".
						// How to make this platform-independent?
						// well, MS-Analyze doesn't work on linux, so there!
						particle.getFilename().substring(3) + 

						// if MS-Analyze is ever smart enough to recognize
						// absolute pathnames, get rid of the stuff above
						// and use this line.
//						particle.getFilename() + 
						", " + particle.getScatDelay() + 
						", 65535, " +
						nFormat.format(particle.getLaserPower()) + 
						", " + 
						dFormat.format(particle.getTimeStamp()));
				particleCount++;
			}
			curs.close();
			out.close();
			System.out.println("Finished exporting to MSA");
		} catch (IOException e) {
			System.err.println("Problem writing .par file: ");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
