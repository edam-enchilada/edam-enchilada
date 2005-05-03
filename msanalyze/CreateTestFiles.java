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
	 * The Original Code is EDAM Enchilada's CreatTestFiles debugging class.
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
	 * Created on Aug 25, 2004
	 *
	 */
	package msanalyze;

	import java.io.*;

	/**
	 * @author ritza
	 *
	 */
	public class CreateTestFiles {
		
		private File parFile;
		private File massFile;
		private File sizeFile;
		
		public CreateTestFiles() {
			createParFile();
			createMassFile();
			createSizeFile();
		}
		
		public void createParFile() {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter("Test.par"));
				writer.println("ATOFMS data set parameters");
				writer.println("Z");
				writer.println("99/99/9999 99:99:99");
				writer.println("this is a test");
				writer.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void createMassFile() {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter("Test.cal"));
				for (int i=0;i<2;i++) {
				writer.println("0.999");
				writer.println("-0.999");
				}
				for (int i=0;i<8;i++)
				writer.println("9999,9.999");
				
				writer.close();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void createSizeFile() {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter("Test.noz"));
				writer.println("[ATOFMS Particle Size Calibration]");
				writer.println("Comment=this is a test");
				writer.println("C1=0.999");
				writer.println("C2=0.999");
				writer.println("C3=0.999");
				writer.println("C4=0.999");
				writer.println("this is a test");
				writer.close();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public File getParFile() {
			return parFile;
		}
		
		public File getMassFile() {
			return massFile;
		}
		
		public File getSizeFile() {
			return sizeFile;
		}
	}
