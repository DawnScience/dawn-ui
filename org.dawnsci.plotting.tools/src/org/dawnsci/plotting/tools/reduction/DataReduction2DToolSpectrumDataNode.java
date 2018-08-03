package org.dawnsci.plotting.tools.reduction;

import org.eclipse.dawnsci.plotting.api.trace.ITrace;

class DataReduction2DToolSpectrumDataNode implements Comparable<DataReduction2DToolSpectrumDataNode>{
	private final int index;
	private final String name;
	private final double[] axisValues;
	private ITrace trace;
	
	public DataReduction2DToolSpectrumDataNode(int index, double... axisValues) {
		this.index = index;
		this.axisValues = axisValues;
		this.name = "Spectrum " + index;
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return Integer.toString(index);
	}

	public ITrace getTrace() {
		return trace;
	}

	public void setTrace(ITrace trace) {
		this.trace = trace;
	}
	
	public void clearTrace() {
		setTrace(null);
	}
	
	public String getName() {
		return name;
	}
	
	public double[] getAxisValues() {
		return axisValues;
	}

	@Override
	public int compareTo(DataReduction2DToolSpectrumDataNode o) {
		return this.index - o.index;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return this.index == ((DataReduction2DToolSpectrumDataNode) obj).index;
	}
}
