package ATOFMS;

public class AMSPeak extends Peak {
	public double height;
	public AMSPeak(double height, double mz) {
		super(height, mz);
		this.height = height;
		// TODO Auto-generated constructor stub
	}
	public String toString()
	{
		String returnThis =
			"Location: " + massToCharge + 
			" Height: " + value;
		return returnThis;
	}
}
