package org.dawnsci.dedi.configuration.calculations;

/**
 * A class that represents a closed interval.
 * Provides useful methods for operations with intervals, 
 * such as intersection of two intervals, or determining whether a given value lies within the interval.
 * The empty set is represented by null.
 *
 */
public class NumericRange {
	private double min;
	private double max;
	
	
	/**
	 * Constructs a new closed interval with the given end points. 
	 * Swaps the arguments if the given min is greater than the given max.
	 */
	public NumericRange(double min, double max) {
		super();
		this.min = min;
		this.max = max;
		checkMinMax();
	}


	public double getMin() {
		return min;
	}



	public double getMax() {
		return max;
	}


	/**
	 * Checks whether min is less than or equal to max,
	 * and swaps them if it is not.
	 */
	private void checkMinMax() {
		if (min > max) {
			double t = max;
			max = min;
			min = t;			
		}
	}
	
	
	/**
	 * @return Whether the given value lies in the closed interval.
	 */
	public boolean contains(double value){
		return value >= min && value <= max;
	}
	
	
	/**
	 * @return Whether the given NumericRange is a subset of this range.
	 *         If the given range is null, returns true, as the empty set is a subset of all sets.
	 */
	public boolean contains(NumericRange other){
		if(other == null) return true;
		else return other.getMin() >= this.getMin() && other.getMax() <= this.getMax();
	}
	
	
	/**
	 * @return A new NumericRange that is the intersection of this range with the given range.
	 *         Returns null if the intersection is empty.
	 */
	public NumericRange intersect(NumericRange other){
		if(other == null) return null;
		
		double otherMin = other.getMin();
		double otherMax = other.getMax();
		
		if(otherMin > max || min > otherMax) return null;
		
		return new NumericRange(Math.max(otherMin, min), Math.min(otherMax, max));
	}
	
	
	@Override
	public String toString(){
		return "[" + min + "," + max + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumericRange other = (NumericRange) obj;
		
		return (Double.doubleToLongBits(max) == Double.doubleToLongBits(other.max)) &&
				(Double.doubleToLongBits(min) == Double.doubleToLongBits(other.min));
	}
}
