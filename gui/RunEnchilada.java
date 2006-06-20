package gui;
import java.io.IOException;

/**
 * I was hoping to get rid of the ugly DOS box that accompanies our program.  But
 * it doesn't seem to work to use this.  The GUI gets unresponsive.
 * @author smitht
 *
 */

public class RunEnchilada {
	public static void main(String[] args) throws IOException, InterruptedException {
		String[] cmdline = {"java", "-Xmx800m", "-jar", "edam-enchilada.jar"};
		
		Process proc = Runtime.getRuntime().exec(cmdline);
		
		proc.waitFor();
			
		System.exit(proc.exitValue());
		
	}
}
