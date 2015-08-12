package org.dawnsci.mapping.ui.wizards;

public class Dimension {

	private int dimension;
	private String description;
	private String[] axisOptions;
	private String axis;
	private int size = -1;
	
	
	public Dimension(int dimension) {
		this.dimension = dimension;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDimensionWithSize() {
		if (size < 0) return Integer.toString(dimension);
		return Integer.toString(dimension) + " [" + Integer.toString(size) + "]";
	}
	
	public void setSize(int size) {
		this.size = size;
	}


	public String getAxis() {
		return axis;
	}
	
	public int getDimension() {
		return dimension;
	}


	public void setAxis(String axis) {
		this.axis = axis;
	}


	public String[] getAxisOptions() {
		return axisOptions;
	}


	public void setAxisOptions(String[] axisOptions) {
		this.axisOptions = axisOptions;
	}
	
}
