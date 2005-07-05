package analysis.clustering.o;

import java.util.Collection;
import java.util.Iterator;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class StatSummary {
	private double[] sumsq;
	private double[] sum;
	private int count = 0;
	private int dimensions;

	private static final int MAX_LOCATION = 2500;
	private static int DOUBLE_MAX = MAX_LOCATION * 2;

	/*
	 * Constructors
	 */
	public StatSummary(double[] sum, double[] sumsq, int count) {
		this.sum = sum;
		this.sumsq = sumsq;
		this.count = count;
	}
	
	public StatSummary(Collection<BinnedPeakList> atoms) {
		sumsq = new double[DOUBLE_MAX];
		sum = new double[DOUBLE_MAX];
		addAll(atoms);
	}
	
	
	/*
	 * Methods for Adding More Atoms or Statistics
	 */
	
	public void addAll(Collection<BinnedPeakList> atoms) {
		Iterator<BinnedPeakList> j = atoms.iterator();
			
		while (j.hasNext()) {
			addAtom(j.next());
		}
	}
	
	public void addAtom(BinnedPeakList atom) {
		BinnedPeak p;
		count++;
		for (int i = 0; i < atom.length(); i++) {
			p = atom.getNextLocationAndArea();
			sumsq[p.location + MAX_LOCATION] += p.area;
		}
	}
	
	public void addStats(double[] sum, double[] sumsq, int count) {
		for (int i = 0; i < DOUBLE_MAX; i++) {
			this.sum[i] += sum[i];
			this.sumsq[i] += sumsq[i];
		}
		this.count += count;
	}
	
	public void addStats(StatSummary that) {
		this.addStats(that.sum, that.sumsq, that.count);
	}
	
	public void addPeak(int location, double area) {
		sum[location + MAX_LOCATION] += area;
		sumsq[location + MAX_LOCATION] += Math.pow(area, 2);
		count++;
	}
	
	public void addPeak(BinnedPeak p) {
		addPeak(p.location, p.area);
	}
	
	
	/*
	 * Statistical Methods
	 */
	public double stdDev(int dim) {
		return Math.sqrt((sumsq[dim + MAX_LOCATION] 
		                                - (Math.pow(sum[dim + MAX_LOCATION],2)
		                                		/count)
		                          )/count);
	}
	
	public double mean(int dim) {
		return sum[dim + MAX_LOCATION] / count;
	}
	
	public double sum(int dim) {
		return sum[dim + MAX_LOCATION];
	}
	
	public double sumsq(int dim) {
		return sumsq[dim + MAX_LOCATION];
	}
	
	public int count() {
		return count;
	}
}
