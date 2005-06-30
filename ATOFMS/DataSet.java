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
 * The Original Code is EDAM Enchilada's Dataset class.
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
package ATOFMS;

/**
 * @author andersbe
 *
 * needed to export collections to the MS-Analyze database, and 
 * to import DataSets as collections into the SpASMS database.  
 * 
 */
public class DataSet {
	
	/**
	 * These variables are necessary to export the dataset to
	 * the MS-Analyze database.
	 */ 
	private String name;
	private ATOFMSParticle[] particles;
	private PeakParams params;
	private CalInfo calInfo;
	
	/**
	 * Constructor.
	 * @param n - name of the dataset
	 * @param p - array of particles in the dataset
	 * @param pp - peak list parameters for the dataset
	 * @param c - calibration information for the dataset
	 */
	public DataSet (String n, ATOFMSParticle[] p, PeakParams pp, CalInfo c){
		name = n;
		particles = p;
		params = pp;
		calInfo = c;
	}
	
	/**
	 * @return dataset name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return array of particles in dataset
	 */
	public ATOFMSParticle[] getParticles(){
		return particles;
	}
	
	/**
	 * @return PeakParams object for dataset
	 */
	public PeakParams getParams(){
		return params;
	}
	
	/**
	 * @return CalInfo object for dataset
	 */
	public CalInfo getCalInfo(){
		return calInfo;
	}
	
}
