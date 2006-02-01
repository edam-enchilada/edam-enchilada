package dataImporters;
/**
 * @author Lei Chen
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class ATOFMSBatchLoader {
	private ArrayList<String> stringSplit(String s, String sp) {
		String ss = s;
		ArrayList<String> result = new ArrayList<String>();
		
		int pos = ss.indexOf(sp);
		while (pos != -1) {
			result.add(ss.substring(0, pos));
			
			ss = ss.substring(pos + sp.length(), ss.length());
			
			pos = ss.indexOf(sp);
		}
		
		result.add(ss);
		
		return result;
	}
	
	public ArrayList<ArrayList<Object>> createList(String dir,
			String calFileName) throws IOException {
		
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		
		FileReader fr = new FileReader(calFileName);
		BufferedReader br = new BufferedReader(fr);
		
		String line = br.readLine();
		Hashtable<String, ArrayList<String>> calTable = 
			new Hashtable<String, ArrayList<String>>();
		while (line != null) {
			
			ArrayList<String> elements = stringSplit(line, ",");
			
			String dataset = elements.get(0);
			String subset = elements.get(1);
			
			calTable.put(dataset + "_" + subset, elements);
		
			line = br.readLine();
		}	

		String dataDirName = dir + "\\data";
		// System.out.println(dataDirName);
		
		File dataDir = new File(dir + "\\data");
		// System.out.println(dataDirName);
		
		
		int count = 0;
		File [] datasets = dataDir.listFiles();
		for (int i=0; i < datasets.length; i++) {
			System.out.println(datasets[i].getName());
			
			int height = 10;
			int area = 10;
			Float relArea = new Float(0.0001);
			boolean autoCal = true;

			File dataset = datasets[i];
			
			File [] groups = datasets[i].listFiles();
						
			for (int j=0; j < groups.length; j++) {
				File group = groups[j];
				
				String key = dataset.getName() + "_" + group.getName();
				System.out.println(key);
				ArrayList<String> entry = calTable.get(key);
				if (entry == null)
					continue;
				
				String sizeCalFn = dir + "\\Calibrations\\" + entry.get(2) + ".noz";
				String massCalFn = dir + "\\Calibrations\\" + entry.get(3) + ".cal";
				
				System.out.println(group.getAbsolutePath());
				
				File [] spectra = group.listFiles();
				
				for (int k=0; k < spectra.length; k++) {
					String filename = spectra[k].getName();
					if (filename.indexOf(".par") != -1) {
						
						ArrayList<Object> row = new ArrayList<Object>(7);
						
						row.add(spectra[k].getAbsolutePath());
						row.add(massCalFn);
						row.add(sizeCalFn);
						row.add(new Integer(height));
						row.add(new Integer(area));
						row.add(relArea);
						row.add(new Boolean(autoCal));
						
						for (int p=0; p < row.size(); p++) {
							System.out.println(row.get(p));
						}
						
						result.add(row);
						
						/*
						System.out.println("-------------");
						System.out.println(spectra[k].getAbsolutePath());
						System.out.println(result.size());
						*/
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ATOFMSBatchLoader loader = new ATOFMSBatchLoader();
		loader.createList("C:\\ESL_RAW", "C:\\ESL_RAW\\Calibrations\\size_cal_info.csv");
	}
}
