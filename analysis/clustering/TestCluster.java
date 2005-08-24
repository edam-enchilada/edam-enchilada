package analysis.clustering;
/*
 * Created on Aug 19, 2004
 *
 */

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ATOFMS.ParticleInfo;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;

import database.CollectionCursor;
import database.InfoWarehouse;
import database.NonZeroCursor;
import database.SQLServerDatabase;

public class TestCluster extends Cluster{	
		/* Declared Class Variables */
		private boolean refineCentroids; // true to refine centroids, false otherwise.
		protected int k; // number of centroids desired.
		private int numParticles; // number of particles in the collection.
		private Random random;

		private static float error = 0.01f;
		private static int numSamples = 50;
		protected NonZeroCursor curs;
		private int returnThis;
		private JFrame parentContainer;
		private int curInt;
		private double difference;
		private ArrayList<BinnedPeakList> tempArray;
		
		private JDialog errorUpdate;
		private JLabel errorLabel;
		private JFrame container;
		
		/**
		 * Constructor; calls the constructor of the Cluster class.
		 * @param cID - collection ID
		 * @param database - database interface
		 * @param k - number of centroids desired
		 * @param name - collection name
		 * @param comment - comment to insert
		 * @param refineCentroids - true to refine centroids, false otherwise.
		 * 
		 */
		public TestCluster(int cID, InfoWarehouse database, 
				String name, String comment) 
		{
			super(cID, database,name.concat(",blah"),comment);
			collectionID = cID;
			parameterString = name.concat(",blah");	
		}
		
		public int divide() {
			CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<String>();
			set.add("ATOFMSAtomInfoDense.[LaserPower]");
			set.add("ATOFMSAtomInfoDense.[Size]");
			ClusterInformation cInfo = new ClusterInformation(set, "Automatic",null);
			CollectionCursor curs = db.getClusteringCursor(db.getCollection(collectionID), cInfo);
			while (curs.next()) {
				ParticleInfo pInfo = curs.getCurrent();
				System.out.println("AtomID: " + pInfo.getID());
				pInfo.getBinnedList().printPeakList();
			}
			curs.close();
			return 0;
		}
		
		/**
		 * Sets the cursor type; clustering can be done using either by 
		 * disk or by memory.
		 * 
		 * (non-Javadoc)
		 * @see analysis.CollectionDivider#setCursorType(int)
		 */
		public boolean setCursorType(int type) 
		{
			switch (type) {
			case CollectionDivider.DISK_BASED :
				curs = new NonZeroCursor(db.getBinnedCursor(db.getCollection(collectionID)));
			return true;
			case CollectionDivider.STORE_ON_FIRST_PASS : 
			    curs = new NonZeroCursor(db.getMemoryBinnedCursor(db.getCollection(collectionID)));
			return true;
			default :
				return false;
			}
		}
		
		public static void main(String[] args) {
			SQLServerDatabase db = new SQLServerDatabase();
			db.openConnection();
			TestCluster test = new TestCluster(2,db,"name", "comment");
			test.divide();
			db.closeConnection();
		}
}
