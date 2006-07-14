package chartlib.hist;

import java.awt.Color;


public class HistogramDataset {
	public ChainingHistogram[] hists;
	public int count;
	public Color color;
	public HistogramDataset(int count, ChainingHistogram[] hists, Color color) {
		this.count = count;
		this.hists = hists;
		this.color = color;
	}
}
