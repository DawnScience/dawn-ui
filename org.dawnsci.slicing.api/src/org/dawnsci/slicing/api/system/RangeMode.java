package org.dawnsci.slicing.api.system;

/**
 * Mode for slicing, whether the slicing allows multiple ranges
 * to be defined or not.
 * 
 * @author fcp94556
 *
 */
public enum RangeMode {

	NO_RANGES, SINGLE_RANGE, MULTI_RANGE;
	
	public boolean isRange() {
		return this==SINGLE_RANGE || this==MULTI_RANGE;
	}
}
