/*
 * Created on Dec 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package experiments;

import java.io.File;
import java.util.Date;
import java.io.IOException;
import java.util.zip.ZipException;

import ATOFMS.ATOFMSParticle;
import ATOFMS.ReadSpec;



/**
 * @author ritza
 *
 */
public class ReadExpSpec extends ReadSpec{
	private int[] posdata;
	private int[] negdata;
	
	public ReadExpSpec(String filename, Date d) throws ZipException, IOException {
		super(filename, d);
	}
	
	public ATOFMSParticle createParticle(String name,
			Date timetext,
			float laserpow,
			float digitrate,
			int scatdelay,
			int[] pos,
			int[] neg) {
		posdata = pos;
		negdata = neg;
		
		ATOFMSParticleExp particle = new ATOFMSParticleExp(new File(name).getName(), timetext, laserpow, 
				digitrate, scatdelay, posdata, negdata);
		
		return particle;
	}
}
