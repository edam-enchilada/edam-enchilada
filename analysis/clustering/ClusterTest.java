package analysis.clustering;

import java.util.ArrayList;

import analysis.BinnedPeakList;
import database.InfoWarehouse;
import junit.framework.TestCase;


/**
 * Just added a basic test for generateCentroidArrays.
 * 
 * @author jtbigwoo
 *
 */
// TODO: Add a test or two for createCenterAtoms
public class ClusterTest extends TestCase{

	private InfoWarehouse db;

	
	public void testGenerateCentroidArrays()
	{
		ArrayList<float[]> resultList;
		// just try it with two centroids
		BinnedPeakList peakList = new BinnedPeakList();
		peakList.add(12, 57.0f);
		peakList.add(-12, 57.5f);
		Centroid center = new Centroid(peakList, 1);
		ArrayList<Centroid> list = new ArrayList<Centroid>();
		list.add(center);
		peakList = new BinnedPeakList();
		peakList.add(13, .58f);
		peakList.add(26, .42f);
		center = new Centroid(peakList, 1);
		list.add(center);
		resultList = Cluster.generateCentroidArrays(list, Cluster.ARRAYOFFSET);
		float[] resultArray = resultList.get(0);
		assertEquals(57.5f, resultArray[-12 + Cluster.ARRAYOFFSET]);
		assertEquals(57.0f, resultArray[12 + Cluster.ARRAYOFFSET]);
		resultArray = resultList.get(1);
		assertEquals(0f, resultArray[-13 + Cluster.ARRAYOFFSET]);
		assertEquals(.58f, resultArray[13 + Cluster.ARRAYOFFSET]);
		assertEquals(.42f, resultArray[26 + Cluster.ARRAYOFFSET]);
	}
}
