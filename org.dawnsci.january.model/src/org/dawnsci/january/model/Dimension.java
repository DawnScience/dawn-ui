package org.dawnsci.january.model;

import org.eclipse.january.dataset.Slice;

public class Dimension {

	private int dimension;
	private String description = "";
	private String[] axisOptions;
	private String axis = NDimensions.INDICES;
	private int size = -1;
	private Slice slice;
	private int axisFilterIndex = -1;
	private boolean filterAxes = false;


	public Dimension(int dimension, int size) {
		this.dimension = dimension;
		this.size = size;
		slice = new Slice(0, 1, 1);
		if (size != 0) slice.setLength(size);
	}

	public Dimension(Dimension toCopy){
		this.dimension = toCopy.dimension;
		this.description = toCopy.description;
		this.axisOptions = toCopy.axisOptions == null ? null : toCopy.axisOptions.clone();
		this.axis = toCopy.axis;
		this.size = toCopy.size;
		this.slice = toCopy.slice == null ? null : toCopy.slice.clone();
	}

	public Slice getSlice() {
		return slice;
	}

	public void setSlice(Slice slice) {
		this.slice = slice;
		this.slice.setLength(size);
	}

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		if (description == null) description = "";
		this.description = description;
	}

	public String getDimensionWithSize() {
		if (size < 0) return Integer.toString(dimension);
		return Integer.toString(dimension) + " [" + Integer.toString(size) + "]";
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
		
		if (size == 0) {
			slice.setStart(null);
			slice.setStop(null);
			slice.setLength(0);
			return;
		}
		
		if ( slice.getStart() != null && size < slice.getStart()) {
			slice.setStart(size-1);
		}
		
		if (size < slice.getStop()) {
			slice.setStop(size);
		}
		
		slice.setLength(size);
	}


	public String getAxis() {
		return axis;
	}

	public int getDimension() {
		return dimension;
	}


	public void setAxis(String axis) {
		if (axis != null && !axis.isEmpty()) {
			this.axis = axis;
		} else {
			this.axis = NDimensions.INDICES;
		}
	}


	public String[] getAxisOptions() {
		
		if (filterAxes && axisFilterIndex > 0 && axisFilterIndex < axisOptions.length) {
			String[] out = new String[axisFilterIndex];
			System.arraycopy(axisOptions, 0, out, 0, out.length);
			return out;
		}
		
		return axisOptions;
	}
	
	public void setAxisFilterIndex(int index) {
		axisFilterIndex = index;
	}


	public void setAxisOptions(String[] axisOptions) {
		this.axisOptions = axisOptions;
	}
	
	public void setFilterAxes(boolean filter) {
		filterAxes = filter;
	}

}
