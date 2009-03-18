

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("All unit tests that I could find");
		//$JUnit-BEGIN$
		suite.addTestSuite(analysis.BinnedPeakListTest.class);
		suite.addTestSuite(analysis.MedianFinderTest.class);
		suite.addTestSuite(analysis.NormalizableTest.class);
		suite.addTestSuite(analysis.NormalizerTest.class);
		suite.addTestSuite(analysis.clustering.Art2ATest.class);
		suite.addTestSuite(analysis.clustering.ClusterTest.class);
		suite.addTestSuite(analysis.clustering.ClusterQueryTest.class);
		suite.addTestSuite(analysis.clustering.KMeansTest.class);
		suite.addTestSuite(analysis.clustering.KMediansTest.class);
		suite.addTestSuite(analysis.dataCompression.CFNodeTest.class);
		suite.addTestSuite(analysis.dataCompression.CFTreeTest.class);
		suite.addTestSuite(analysis.dataCompression.ClusterFeatureTest.class);
		suite.addTestSuite(ATOFMS.CalInfoTest.class);
		suite.addTestSuite(chartlib.hist.HistogramDatasetTest.class);
		suite.addTestSuite(database.DatabaseTest.class);
		suite.addTestSuite(database.DynamicTableGeneratorTest.class);
		suite.addTestSuite(database.TSBulkInserterTest.class);
		suite.addTestSuite(dataImporters.AMSDataSetImporterTest.class);
		suite.addTestSuite(dataImporters.ATOFMSDataSetImporterTest.class);
		suite.addTestSuite(dataImporters.EnchiladaDataSetImporterTest.class);
		suite.addTestSuite(dataImporters.PALMSDataSetImporterTest.class);
		suite.addTestSuite(dataImporters.SPASSDataSetImporterTest.class);
		suite.addTestSuite(dataImporters.TSImportTest.class);
		suite.addTestSuite(externalswing.ProgressTaskTest.class);
		suite.addTestSuite(gui.AggregatorTest.class);
		suite.addTestSuite(dataExporters.MSAnalyzeDataSetExporterTest.class);
		suite.addTestSuite(dataExporters.CSVDataSetExporterTest.class);
		//$JUnit-END$
		return suite;
	}

}
