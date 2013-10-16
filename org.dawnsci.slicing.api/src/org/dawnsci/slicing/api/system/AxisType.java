package org.dawnsci.slicing.api.system;

public enum AxisType {

	NONE(null,         -Integer.MAX_VALUE), 
	X("X",              0), 
	Y("Y",              1), 
	Y_MANY("Y (Many)",  1),  // Not actually used as a legal type, just for labels.
	Z("Z",              2), 
	SLICE("(Slice)",   -1), 
	RANGE("(Range)",   -1);
	
	private final String  label;
	private final int     index;

	AxisType(String label, int index) {
		this.label = label;
		this.index = index;
	}

	public String getLabel() {
		return label;
	}

	public boolean hasValue() {
		return index<0;
	}

	public int getIndex() {
		return index;
	}
	
	public String toString() {
		return getLabel();
	}
}
