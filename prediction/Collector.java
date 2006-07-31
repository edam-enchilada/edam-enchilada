/**
 * Collect the ATOFMS data together with the BC or EC data and export to Weka.
 */
package prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author steinbel
 * NOTE: Still working with Perl script for postprocessing.  Also, currently
 * hardcoded for BC. 7.31.06
 */
public class Collector {

	//take in file of ec/bc/whatever data in arff format
	public static void main(String[] args){
		PredictionAggregator grab = new PredictionAggregator();
		String line;
		String result = "";
		try {
			Scanner scan = new Scanner(new File("C:/Documents and Settings/steinbel/My Documents/WorkSpace/edam-enchilada/prediction/small.csv"));
			FileWriter out = new FileWriter(new File("C:/Documents and Settings/steinbel/My Documents/WorkSpace/edam-enchilada/prediction/BC_plus.csv"));
			grab.open();
			while (scan.hasNextLine()){

				line = scan.nextLine();
				out.write(grab.grab(line));
				//out.append(result);
				//System.out.println(result);//debugging
			}
			grab.close();
			out.close();
			scan.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//for all data rows
		//extract the next time
	
		//pass to grabber
	
		//returned: row w/avg. ATOFMS for that hour tacked onto the whatever data
	
		//write out all this to some file
	
}
