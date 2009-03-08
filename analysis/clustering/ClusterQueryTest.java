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
 * The Original Code is EDAM Enchilada's KMeans unit test.
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


package analysis.clustering;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import junit.framework.TestCase;
import database.CreateTestDatabase;
import database.Database;
import database.InfoWarehouse;
import errorframework.NoSubCollectionException;
import ATOFMS.Peak;
import analysis.DistanceMetric;
import analysis.clustering.ClusterQuery;

/*
 * Created August 19, 2008
 * 
 * 
 * This does not test clustering after attemption to cluster around
 * an empty particle. 
 */


public class ClusterQueryTest extends TestCase {
	
	private ClusterQuery qc;
	private InfoWarehouse db;
	String dbName = "testDB";
    Float d = 0.5f;
    int cID = 2;
	
	
	
    /*
     * @see TestCase#setUp()
     */
	protected void setUp() throws Exception {
        super.setUp();
        
        
        new CreateTestDatabase();
		db = Database.getDatabase("TestDB");
		db.openConnection("TestDB");
		
		PrintWriter pw;
		try {
			pw = new PrintWriter("testClust\\q\\par1.txt");
			pw.println(db.getATOFMSFileName(2));
			ArrayList<Peak> peaks = db.getPeaks(db.getAtomDatatype(2), 2);		

			for(int i = 0; i<peaks.size();i++){
				pw.println(peaks.get(i).massToCharge + "," + peaks.get(i).value);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			pw = new PrintWriter("testClust\\q\\par2.txt");
			pw.println(db.getATOFMSFileName(3));
			ArrayList<Peak> peaks = db.getPeaks(db.getAtomDatatype(3), 3);		

			for(int i = 0; i<peaks.size();i++){
				pw.println(peaks.get(i).massToCharge + "," + peaks.get(i).value);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void tearDown() throws Exception
	{
		super.tearDown();
		db.closeConnection();
		System.runFinalization();
		System.gc();
	    Database.dropDatabase(dbName);
		
	}
		
	public void testGoodCluster(){
		ArrayList<String> filenamesGood = new ArrayList<String>();
		
		filenamesGood.add("testClust\\q\\par1.txt");
		filenamesGood.add("testClust\\q\\par2.txt");
		
		qc = new ClusterQuery(
					cID,db, "Cluster Query", "GoodTest", false, filenamesGood,d);
		
		qc.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
		
		System.out.println("setting cursor type");
		qc.setCursorType(Cluster.DISK_BASED);
		
		qc.divide();
	}
	
	public void testNoCluster(){
		ArrayList<String> filenamesNoClusters = new ArrayList<String>();
		
		filenamesNoClusters.add("testClust\\q\\par3.txt");		
		
		qc = new ClusterQuery(
				cID,db, "Cluster Query", "NoTest", false, filenamesNoClusters,d);
		
		qc.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
		
		System.out.println("setting cursor type");
		qc.setCursorType(Cluster.DISK_BASED);
		try{
			qc.divide();
		}catch (NoSubCollectionException sce){
			sce.printStackTrace();
		}
	}
	/*
	public void testEmptyCluster(){
		ArrayList<String> filenamesEmptyClusters = new ArrayList<String>();
		
		filenamesEmptyClusters.add("testClust\\q\\par4.txt");		
		
		try{
		qc = new ClusterQuery(
				cID,db, "Cluster Query", "EmptyTest", false, filenamesEmptyClusters,d);
		
		qc.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
		
		System.out.println("setting cursor type");
		qc.setCursorType(Cluster.DISK_BASED);
		
		qc.divide();
		}catch (AssertionError ae){
			ae.printStackTrace();
		}
	}*/
		
}
	
	


