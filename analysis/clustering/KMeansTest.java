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

import java.sql.Connection;
import java.sql.DriverManager;

import analysis.BinnedPeakList;
import analysis.DistanceMetric;


import database.CreateTestDatabase;
import database.SQLServerDatabase;
import junit.framework.TestCase;
/*
 * Created on Dec 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author dmusican
 *
 */
public class KMeansTest extends TestCase {

    private KMeans kmeans;
    private SQLServerDatabase db;
    String dbName = "TestDB";
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
		try {
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load current driver.");
			
		} // end catch
		
		Connection con = null;
		
		try {
			con = DriverManager.getConnection("jdbc:microsoft:sqlserver://localhost:1433;TestDB;SelectMethod=cursor;","SpASMS","finally");
		} catch (Exception e) {
			System.err.println("Failed to establish a connection to SQL Server");
			System.err.println(e);
		}
		
		// TODO: commented this out. AR
		//SQLServerDatabase.rebuildDatabase("TestDB");
		new CreateTestDatabase();
		db = new SQLServerDatabase("localhost","1433","TestDB");
		db.openConnection();
		
        int cID = 1;
        int k = 5;
        String name = "Test clustering";
        String comment = "Test comment";
        boolean refine = true;
        kmeans = new KMeans(cID,db,k,name,comment,refine);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
		db.closeConnection();
		System.runFinalization();
		System.gc();
	    SQLServerDatabase.dropDatabase(dbName);
        
    }

    public void testGetDistance() {
        BinnedPeakList list1 = new BinnedPeakList();
        BinnedPeakList list2 = new BinnedPeakList();
        list1.add(1,0.1f);
        list1.add(2,0.2f);
        list2.add(1,0.3f);
        list2.add(3,0.3f);

        kmeans.setDistanceMetric(DistanceMetric.CITY_BLOCK);
        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.CITY_BLOCK)*100)/100. == 0.7);
        kmeans.setDistanceMetric(DistanceMetric.EUCLIDEAN_SQUARED);
        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.EUCLIDEAN_SQUARED)*100)/100.
                == 0.17);
        kmeans.setDistanceMetric(DistanceMetric.DOT_PRODUCT);
        assertTrue(Math.round(list1.getDistance(list2,DistanceMetric.DOT_PRODUCT)*100)/100.
                == 0.97);
    }

}
