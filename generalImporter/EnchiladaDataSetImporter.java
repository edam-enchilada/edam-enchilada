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
 * The Original Code is EDAM Enchilada's EnchiladaDataSetImporter class.
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

package generalImporter;

import gui.MainFrame;
import gui.ParTableModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import atom.EnchiladaDataPoint;

import database.Datatype;
import database.SQLServerDatabase;

/**
 * @author ritza
 */
public class EnchiladaDataSetImporter {
	
	private File file;
	private SQLServerDatabase db;
	private Scanner scan;
	private int collectionID;
	private ArrayList<EnchiladaDataPoint> particles;

	public EnchiladaDataSetImporter(ParTableModel t) {
		ArrayList<String> fileNames = collectTableInfo(t);
		db = MainFrame.db;
		particles = new ArrayList<EnchiladaDataPoint>();
		for (int i = 0; i < fileNames.size(); i++) {
		file = new File(fileNames.get(i));
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createEmptyCollection();
		
		String ext = file.getName().substring(file.getName().length()-5);
		// get datatype and create table if needed.
		ext = ext.substring(ext.lastIndexOf("."));
		Datatype type = new Datatype(ext);
		if (!type.tableExists()) 
			type.createTable();
		
		
		if (ext.equals(".edsf"))
			singleFileProcessor();
		else if(ext.equals(".edmf"))
			multiFileProcessor();
		else System.out.println("wrong extension");
		db.insertGeneralParticles(particles, collectionID);
		}
	}
	
	public ArrayList<String> collectTableInfo(ParTableModel table) {
		int rowCount = table.getRowCount()-1;
		ArrayList<String> files = new ArrayList<String>();
		String name = "";
		for (int i=0;i<rowCount;i++) {
			try {
				files.add((String)table.getValueAt(i,1));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return files;
	}
	
	public void createEmptyCollection() {
		String name = "", comment = "", peek;
		while(scan.hasNext()) {
			peek = scan.nextLine();
			if (peek.equals("^^^^^^^^"))
				break;
			else name += peek + " "; 
		}
		while(scan.hasNext()) {
			peek = scan.nextLine();
			if (peek.equals("^^^^^^^^"))
				break;
			else comment += peek + " "; 
		}
		collectionID = db.createEmptyCollection(0, name, comment);	
		// scan through ids
		peek = "";
		while (!peek.equals("^^^^^^^^")) {
			peek = scan.next();
		}
	}
	
	public void singleFileProcessor() {
			while (scan.hasNext()) {
				scan = insertParticle(scan);
			}
		}
	
	public void multiFileProcessor() {
		while (scan.hasNext()) {
			String str = scan.nextLine();
			if (str.equals("^^^^^^^^"))
				break;
			File nextFile = new File(str);
			try {
				Scanner s = new Scanner(nextFile);
				insertParticle(s);
			} catch (FileNotFoundException e) {
				System.out.println("list of particle files not" +
						" properly formatted.");
				e.printStackTrace();
			}
		}
	}
	
	public Scanner insertParticle(Scanner scan) {
		int index = 0, clusteredF = 0, f1 = 0;
		float f2 = 0;
		String partName = "";
		while (scan.hasNext()) {
			String str = scan.nextLine();
			if (str.equals("^^^^^^^^"))
				break;
			else 
				partName += str;
		}
		EnchiladaDataPoint part = new EnchiladaDataPoint(partName);
		while (scan.hasNext()) {
			String str = scan.nextLine();
			if (str.equals("^^^^^^^^"))
				break;
			StringTokenizer token = new StringTokenizer(str);
			if (token.hasMoreTokens()) 
				index = Integer.parseInt(token.nextToken());
			if (token.hasMoreTokens()) 
				clusteredF = Integer.parseInt(token.nextToken());
			if (token.hasMoreTokens()) 
				f1 = Integer.parseInt(token.nextToken());
			if (token.hasMoreTokens()) 
				f2 = Float.parseFloat(token.nextToken());
			part.addPeak(f1, clusteredF, f2, (double)index);
		}
		particles.add(part);
		return scan;
	}
}
