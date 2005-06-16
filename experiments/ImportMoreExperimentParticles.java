/*
 * Created on Dec 9, 2004
 *
 */
package experiments;

import java.util.*;

import database.SQLServerDatabase;
import msanalyze.*;
import atom.*;

/**
 * @author ritza
 *
 */
public class ImportMoreExperimentParticles {
	private static int amplitude;
	private ArrayList<ATOFMSParticle> particles;
	private SQLServerDatabase db;
	
	public ImportMoreExperimentParticles() {
		particles = new ArrayList<ATOFMSParticle>();
		ArrayList<Integer>  indices = new ArrayList<Integer>(60000);
		
		//Open database connection:
		db = new SQLServerDatabase("localhost","1433","SpASMSdb");
		db.openConnection();
		
		
		ATOFMSParticle.currPeakParams = new PeakParams(30,30,0.01f);
		try {
			ATOFMSParticle.currCalInfo = 
				new CalInfo("Particles for Clustering\\040215a_33.cal", true);
		} catch(Exception exception) {
			System.out.println("error loading cal info.");
			exception.printStackTrace();
		}
		
		int[] id = new int[2];
		id = db.createEmptyCollectionAndDataset(0,"Amp: "+ amplitude,"7 different particles",
				"Particles for Clustering\\040215a_33.cal",".noz file", 
				ATOFMSParticle.currCalInfo,
				ATOFMSParticle.currPeakParams);
		
		// Use the indices array to duplicate the number of each type of particle.
		// Choose a random number of duplications.  
		for (int p1 = 0; p1 < 21000; p1++) 
			indices.add(new Integer(1));
		for (int p2 = 0; p2 < 6000; p2++) 
			indices.add(new Integer(2));
		for (int p3 = 0; p3 < 3000; p3++) 
			indices.add(new Integer(3));
		for (int p4 = 0; p4 < 12000; p4++) 
			indices.add(new Integer(4));
		for (int p5 = 0; p5 < 1500; p5++) 
			indices.add(new Integer(5));
		for (int p6 = 0; p6 < 13500; p6++) 
			indices.add(new Integer(6));
		for (int p7 = 0; p7 < 3000; p7++) 
			indices.add(new Integer(7));
		
		// randomize the particles in the array.
		// Use a random object with a seed to ensure
		// that they randomize the same way every time.
		indices.trimToSize();
		Random rnd = new Random(23713);
		Collections.shuffle(indices,rnd);		
		
		/**
		 * Indices to particles:
		 * 
		 * 1	a-020801071636-00055.amz	
		 * 2	a-040215084636-00033.amz
		 * 3	b-040215093256-00061.amz	
		 * 4	b-040215093918-00150.amz	
		 * 5	h-041120141836-00007.amz	
		 * 6	i-040808153000-00112.amz	
		 * 7	i-040808160921-00310.amz
		 */
		
		try {
			// insert the particles into the database, peaklisting them every time.
			int newAtomID = db.getNextID();
			int particleNumber;
			ReadExpSpec readSpec;
			String file;
			int marker = 100;
			for (int i = 0; i < indices.size(); i++) {
				if (i/marker == i/(double)marker)
					System.out.println("Inserting particle " + i);
				particleNumber = indices.get(i).intValue();
				switch (particleNumber) {
				case 1:
					file = "a-020801071636-00055.amz";
					break;
				case 2:
					file = "a-040215084636-00033.amz";
					break;
				case 3:
					file = "b-040215093256-00061.amz";
					break;
				case 4:
					file = "b-040215093918-00150.amz";
					break;
				case 5:
					file = "h-041120141836-00007.amz";
					break;
				case 6:
					file = "i-040808153000-00112.amz";
					break;
				default:
					file = "i-040808160921-00310.amz";
				}
				readSpec = 	new ReadExpSpec("Particles for Clustering\\" + file); 
				db.insertATOFMSParticle(readSpec.getParticle(),id[0],id[1],newAtomID++);
			}
		}catch (Exception exception) {
			System.out.println("Caught exception");
			exception.printStackTrace();
		}
	}
	
	public static int getAmplitude() {
		return amplitude;
	}
	
	public static void main(String[] args) {
		amplitude = Integer.parseInt(args[0]);
		ImportMoreExperimentParticles imp = 
			new ImportMoreExperimentParticles();
		System.out.println("done.");
	}
}
