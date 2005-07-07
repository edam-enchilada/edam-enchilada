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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import atom.ATOFMSAtomFromDB;

import ATOFMS.ATOFMSParticle;


/**
 * @author andersbe
 *
 */
public class Collection {
	private int collectionID;
	private String name, comment;
	private InfoWarehouse db = null;
	private String datatype;
	private String namingField;
	
	public Collection (String type, int cID, InfoWarehouse database)
	{
		collectionID = cID;
		datatype = type;
		db = database;
		name = db.getCollectionName(collectionID);
		comment = db.getCollectionComment(collectionID);
	}
	
	public ArrayList<Integer> getSubCollectionIDs()
	{
		ArrayList<Integer> subCollections = db.getImmediateSubCollections(this);
		return subCollections;
	}
	
	public int getCollectionID()
	{
		return collectionID;
	}
	
	public String getDatatype() {
		return datatype;
	}
	
	public String toString()
	{
		return db.getCollectionName(collectionID).trim();
	}
	
	public ArrayList<Integer> getParticleIDs()
	{
		return db.getAllDescendedAtoms(this);
	}
	
	public ArrayList<ATOFMSParticle> getParticles()
	{
		ArrayList<Integer> atomIDs = db.getAllDescendedAtoms(this);
		
		
		return null;
	}
	
	//TODO:  Need a progress bar here.
	public boolean exportToPar()
	{
		File parFile = new File(name + ".par");
		File setFile = new File(name + ".set");
		System.out.println(parFile.getAbsolutePath());
		java.util.Date date = db.exportToMSAnalyzeDatabase(this, name, "MS-Analyze");
		try {
			DateFormat dFormat = 
				new SimpleDateFormat("MM/dd/yyyy kk:mm:ss");
			PrintWriter out = new PrintWriter(new FileOutputStream(parFile, false));
			out.println("ATOFMS data set parameters");
			out.println(name);
			out.println(dFormat.format(date));
			out.println(comment);
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
				out.println(particleCount + ", " + 
						particle.getFilename().substring(0,1) + 
						"\\" +
						particle.getFilename() + 
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
