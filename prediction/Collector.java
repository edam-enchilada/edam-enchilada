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
 * NOTE: currently hardcoded for BC. 7.31.06
 */
public class Collector {

	//take in file of ec/bc/whatever data in arff format
	public static void main(String[] args){
		PredictionAggregator pa = new PredictionAggregator();
		String line;
		String result = "";
		try {
			Scanner scan = new Scanner(new File("C:/Documents and Settings/steinbel/My Documents/WorkSpace/edam-enchilada/prediction/BC.csv"));
			FileWriter out = new FileWriter(new File("C:/Documents and Settings/steinbel/My Documents/WorkSpace/edam-enchilada/prediction/BC.arff"));
			// write out the .arff header 
			
			/*name the attributes - we need 603 to allow for time, mass, and EC/BC
			 *along with 300 negative and 300 positive mz values
			 */
			out.write("@relation BC \n");
			out.write("@attribute time date \"MM/d/yyyy hh:mm:ss a\" \n");
			out.write("@attribute mass numeric \n");
			out.write("@attribute bc numeric \n");
			
			for(int i=1; i<601; i++)
				out.write("@attribute mz" + i + " numeric \n");
			
			out.write("@data \n");
			
			pa.open();
			while (scan.hasNextLine()){

				line = scan.nextLine();
				out.write(pa.grab(line));
				//out.append(result);
				//System.out.println(result);//debugging
			}
			pa.close();
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
}
