package gui;
import java.io.IOException;


public class RunEnchilada {
	public static void main(String[] args) throws IOException, InterruptedException {
		String[] cmdline = {"java", "-Xmx800m", "-jar", "edam-enchilada.jar"};
		
		Process proc = Runtime.getRuntime().exec(cmdline);
		
		proc.waitFor();
			
		System.exit(proc.exitValue());
		
	}
}
