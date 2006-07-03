package chartlib;

import java.awt.Color;

public class SpectrumPlot extends Chart {
	public SpectrumPlot() {
		super(2, false);

		this.setHasKey(false);
		this.setTitle("Positive and negative peak values");
		this.setTitleX(0,"Positive mass-to-charge ratios");
		this.setTitleY(0,"Area");
		this.setTitleY(1,"Area");
		this.setTitleX(1,"Negative mass-to-charge ratios");
		this.setAxisBounds(0,400, CURRENT_VALUE, CURRENT_VALUE);
		this.setNumTicks(10,10, 1,1);
		this.setBarWidth(3);
		this.setColor(0,Color.red);
		this.setColor(1,Color.blue);
	}
}
