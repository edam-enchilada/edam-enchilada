package prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author steinbel
 * NOTE: currently filenames are hardcoded in
 */
public class Collector {

	//take in file of ec/bc/whatever data in csv format
	public static void main(String[] args){
		PredictionAggregator pa = new PredictionAggregator();
		String line;
		String result = "";
		try {
			Scanner scan = new Scanner(new File("C:/Documents and Settings/steinbel/workspace/edam-enchilada/prediction/BC.csv"));
			FileWriter out = new FileWriter(new File("C:/Documents and Settings/steinbel/workspace/edam-enchilada/prediction/BC.arff")); 
			
			/*name the attributes - we need 603 to allow for time, mass, and EC/BC
			 *along with 300 negative and 300 positive m/z values
			 */
			out.write("@relation BC \n");
			out.write("@attribute time date \"MM/d/yyyy hh:mm:ss a\" \n");
			out.write("@attribute mass numeric \n");
			out.write("@attribute bc numeric \n");
			
			for(int i=-300; i<=300; i++)
				out.write("@attribute mz" + i + " numeric \n");			
						
			out.write("@data \n");
			
			pa.open();
			//aggregate data and format into .arff format, write to file
			while (scan.hasNextLine()){

				line = scan.nextLine();
				out.write(pa.grab(line));
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
