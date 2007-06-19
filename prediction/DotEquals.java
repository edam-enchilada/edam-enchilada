package prediction;

import java.io.*;
import java.util.*;

public class DotEquals {

	public static void main(String[] args) throws IOException
	{
		String path = (new File(".")).getCanonicalPath();
		Scanner infile1 = new Scanner(new File(path + "/prediction/stlouis.arff"));
		Scanner infile2 = new Scanner(new File(path + "/prediction/stl.arff"));
		
		while (infile1.hasNext())
		{
			if (!infile1.nextLine().equals(infile2.nextLine()))
			{	
				System.out.println("BROKEN!!");
			}	
		}
		System.out.println("YAY");
	}
}