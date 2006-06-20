package experiments;

import java.io.*;
import java.text.*;

import au.com.bytecode.opencsv.*;

public class CSVDateConverter {
	private CSVReader reader;
	private CSVWriter writer;
	private SimpleDateFormat inForm;
	private SimpleDateFormat outForm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public CSVDateConverter(Reader input, Writer output, SimpleDateFormat inForm) {
		super();
		
		this.inForm = inForm;
		this.reader = new CSVReader(input);
		this.writer = new CSVWriter(output);

	}

	public void close() throws IOException {
		reader.close();
		writer.close();
	}
	
	public void run() throws IOException, ParseException {
		String[] line;
		while ((line = reader.readNext()) != null) {
			line[0] = outForm.format(
					inForm.parse(line[0]));
			writer.writeNext(line);
		}
	}
	
	public static void usage() {
		System.out.println("Usage: CSVDateConverter dateformat");
		System.out.println("send input to std. in, output from std. out.");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		if (args.length != 1) {usage();System.exit(1);}
		CSVDateConverter conv = new CSVDateConverter(
				new InputStreamReader(System.in),
				new OutputStreamWriter(System.out),
				new SimpleDateFormat(args[0]));
		conv.run();
		conv.close();
	}
	
}
