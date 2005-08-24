package analysis.clustering;

import java.util.Set;

public class ClusterInformation {
	// names have 'ATOFMSAtomInfoDense.' , etc. before it. - AR
	public Set<String> valueColumns;
	public String keyColumn;
	public String weightColumn;
	public boolean automatic;
	
	public ClusterInformation(Set<String> v, String k, String w) {
		valueColumns = v;
		keyColumn = k;
		weightColumn = w;
		if (keyColumn.equals("Automatic"))
			automatic = true;
		else 
			automatic = false;
	}
}
