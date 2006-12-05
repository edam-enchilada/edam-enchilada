package prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

/**
 * @author steinbel
 * NOTE: currently filenames are hardcoded in
 */
public class Collector {

	//take in file of ec/bc/whatever data in csv format
	public static void main(String[] args){
		Date start = new Date();
		PredictionAggregator pa = new PredictionAggregator();
		String line;
		String result = "";
		StringBuilder builder = new StringBuilder();
		try {
			Scanner scan = new Scanner(new File("C:/Documents and Settings/steinbel/workspace/edam-enchilada/prediction/small.csv"));
			FileWriter out = new FileWriter(new File("C:/Documents and Settings/steinbel/workspace/edam-enchilada/prediction/small.arff")); 
			
			pa.open();
			//aggregate data and format into .arff format, write to file
			while (scan.hasNextLine()){

				line = scan.nextLine();
				builder.append(pa.grab(line));
			}
			pa.close();
			Date end1 = new Date();
			System.out.println("start " + start.toString() + " end1 " + end1.toString());
			System.out.println("time taken: " + (end1.getTime() - start.getTime()));
			builder.insert(0, pa.assembleAttributes());
			out.write(builder.toString());
			out.close();
			scan.close();
			Date end = new Date();
			System.out.println("time for printing indices:");
			System.out.println("start " + end1.toString() + " end " + end.toString());
			System.out.println("time taken: " + (end.getTime() - end1.getTime()));
			System.out.println("total time:");
			System.out.println("start " + start.toString() + " end " + end.toString());
			System.out.println("time taken: " + (end.getTime() - start.getTime()));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
