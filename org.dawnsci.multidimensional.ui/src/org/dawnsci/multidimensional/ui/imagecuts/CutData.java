package org.dawnsci.multidimensional.ui.imagecuts;

public class CutData {

	String label;
	Double value;
	int index;
	double delta;
	CutType type;

	public CutData(String label, int index, double delta, Double value, CutType type) {
		this.label = label;
		this.index = index;
		this.delta = delta;
		this.value = value == null ? index : value;
		this.type = type;
	}

	public double getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public double getDelta() {
		return delta;
	}

	public CutType getType() {
		return type;
	}

	public enum CutType {
		X, Y, ADDITIONAL;
	}

}
