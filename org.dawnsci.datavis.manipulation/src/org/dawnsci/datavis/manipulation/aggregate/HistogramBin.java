package org.dawnsci.datavis.manipulation.aggregate;

import org.apache.commons.math3.fraction.BigFraction;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Maths;

class HistogramBin {
	private String name;
	private double start;
	private double stop;
	private double delta;
	private int steps;

	public HistogramBin(String name, double start, double delta, int steps) {
		this.name = name;
		this.start = start;
		this.delta = delta;
		this.steps = steps;
		calcStop();
	}

	public HistogramBin(HistogramBin other) {
		this.name = other.name;
		this.start = other.start;
		this.delta = other.delta;
		this.steps = other.steps;
		this.stop = other.stop;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return start
	 */
	public double getStart() {
		return start;
	}

	/**
	 * @return stop
	 */
	public double getStop() {
		return stop;
	}

	/**
	 * @return delta (or bin width)
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * @return number of steps or bins
	 */
	public int getSteps() {
		return steps;
	}

	/**
	 * Set start (will change stop value)
	 * @param start
	 */
	public void setStart(double start) {
		this.start = start;
		calcStop();
	}

	/**
	 * Set delta (will change stop value)
	 * @param delta (or bin width)
	 */
	public void setDelta(double delta) {
		if (delta <= 0) {
			throw new IllegalArgumentException("Delta must be greater than zero");
		}
		this.delta = delta;
		calcStop();
	}

	/**
	 * Set steps (will change stop value)
	 * @param steps
	 */
	public void setSteps(int steps) {
		if (steps <= 0) {
			throw new IllegalArgumentException("Steps must be one or more");
		}
		this.steps = steps;
		calcStop();
	}

	private void calcStop() {
		stop = start + steps*delta;
	}

	@Override
	protected HistogramBin clone() {
		return new HistogramBin(this);
	}

	/**
	 * Create bin edges for histogramming
	 * @param offsetByHalf if true then offset edges by half-width (if you want to round to lower edge)
	 * @return bin edges
	 */
	public Dataset createBinEdges(boolean offsetByHalf) {
		DoubleDataset b = DatasetFactory.createRange(steps);
		b.imultiply(delta).iadd(offsetByHalf ? start - 0.5*delta : start);
		b.setName(name);
		return b;
	}

	@Override
	public String toString() {
		return String.format("%s %g:%g:%g (%d)", name, start, stop, delta, steps);
	}

	private static final double MAX_ERROR = 1e-5;
	private static final int MAX_ITER = 1000;
	private static final double MAX_DIFF = 1e-3;

	/**
	 * Quantize axis values
	 * @param a
	 * @return start, stop (exclusive), step
	 */
	public static HistogramBin calculateQuantization(Dataset a) {
		int scale = Math.max((int) Math.log10(Math.abs(a.min().doubleValue())),
				(int) Math.log10(Math.abs(a.max().doubleValue())));

		// N(N-1)/2 difference values
		Dataset fa = Maths.multiply(a.flatten(), Math.pow(10, -scale));
		int n = fa.getSize();
		DoubleDataset diffs = DatasetFactory.zeros(n * (n - 1) /2);
		int k = 0;
		double delta = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; i++) {
			double ai = fa.getDouble(i);
			for (int j = i + 1; j < n; j++) {
				double v = Math.abs(ai - fa.getDouble(j));
				if (v > MAX_ERROR && delta > v) { // positive min
					delta = v;
				}
				diffs.set(v, k++);
			}
		}

		if (Double.isInfinite(delta)) {
			delta = 0;
		}

		BigFraction frac = new BigFraction(delta, MAX_ERROR, MAX_ITER);
		System.err.printf("%s: %g => %g\n", a.getName(), delta, frac.doubleValue());

		// check all diffs are multiples of frac
		delta = frac.doubleValue();
		int steps = 1;
		boolean anyFraction = false;
		if (delta > 0) {
			double f = 1./delta;
			IndexIterator it = diffs.getIterator();
			double[] buffer = diffs.getData();
			double max = Double.NEGATIVE_INFINITY;
			while (it.hasNext()) {
				double v = f * buffer[it.index];
				double rv = Math.round(v);
				if (rv > max) {
					max = rv;
				}
				double d = v - rv;
				if (Math.abs(d) > MAX_DIFF) {
					System.err.printf("Too far off rounding %g by %g\n", v, d);
					anyFraction = true;
				}
			}
			if (anyFraction) {
				System.err.printf("Fractional steps of %g exist\n", delta);
			}
			steps += (int) max;
		}

		delta *= Math.pow(10,  scale);
		frac = new BigFraction(a.min().doubleValue(), MAX_ERROR, MAX_ITER);
		double start = frac.doubleValue();
		frac = new BigFraction(a.max().doubleValue(), MAX_ERROR, MAX_ITER);
		double stop = frac.doubleValue();
		int maxSteps = 2;
		while (maxSteps < n) {
			maxSteps *= 2;
		}
		if (anyFraction || steps > maxSteps) {
			steps = maxSteps;
			delta = (stop - start)/steps;
		}
		double end = start + steps*delta;
		if (end < stop) {
			System.err.printf("Calculated stop is too large: %g < %g\n", end, stop);
		}

		return new HistogramBin(a.getName(), start, delta, steps);
	}

}