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
 * The Original Code is EDAM Enchilada's TestCursor class.
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
 * Created on Nov 29, 2004
 */
package testing;


import java.util.ArrayList;

import ATOFMS.ParticleInfo;
import analysis.BinnedPeakList;
import atom.ATOFMSAtomFromDB;
import database.CollectionCursor;


/**
 * @author andersbe
 */
public class TestCursor implements CollectionCursor {

	private ArrayList<ParticleInfo> particles;
	private int index = -1;
	/**
	 * 
	 */
	public TestCursor() {
		super();
		particles = new ArrayList<ParticleInfo>();
		
		ParticleInfo tempPI = new ParticleInfo();
		ATOFMSAtomFromDB tempAPI = new ATOFMSAtomFromDB();
		tempAPI.setAtomID(1);
		tempPI.setParticleInfo(tempAPI);
		BinnedPeakList tempBPL = new BinnedPeakList();
		tempBPL.add(10,1.0f);
		tempPI.setBinnedList(tempBPL);
		particles.add(tempPI);
		
		tempPI = new ParticleInfo();
		tempAPI = new ATOFMSAtomFromDB();
		tempAPI.setAtomID(2);
		tempPI.setParticleInfo(tempAPI);
		tempBPL = new BinnedPeakList();
		tempBPL.add(20,1.0f);
		tempPI.setBinnedList(tempBPL);
		particles.add(tempPI);
		
		tempPI = new ParticleInfo();
		tempAPI = new ATOFMSAtomFromDB();
		tempAPI.setAtomID(3);
		tempPI.setParticleInfo(tempAPI);
		tempBPL = new BinnedPeakList();
		tempBPL.add(30,1.0f);
		tempPI.setBinnedList(tempBPL);
		particles.add(tempPI);
	}

	/* (non-Javadoc)
	 * @see database.CollectionCursor#next()
	 */
	public boolean next() {
		index++;
		if (index < particles.size())
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see database.CollectionCursor#getCurrent()
	 */
	public ParticleInfo getCurrent() {
		if (index < particles.size())
			return particles.get(index);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see database.CollectionCursor#close()
	 */
	public void close() {
	}

	/* (non-Javadoc)
	 * @see database.CollectionCursor#reset()
	 */
	public void reset() {
		index = -1;
	}

	/* (non-Javadoc)
	 * @see database.CollectionCursor#get(int)
	 */
	public ParticleInfo get(int i) throws NoSuchMethodException {
		return particles.get(i);
	}
	
	public BinnedPeakList getPeakListfromAtomID(int id) {
		return null;
	}
}
