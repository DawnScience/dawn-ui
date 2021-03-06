package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.january.model.NDimensions;

public class PlottableObject {

	private IPlotMode mode;
	private NDimensions nDimensions;
	
	public PlottableObject(IPlotMode mode, NDimensions nDimensions) {
		this.mode = mode;
		this.nDimensions = nDimensions;
	}
	
	public NDimensions getNDimensions() {
		return nDimensions;
	}
	
	public IPlotMode getPlotMode(){
		return mode;
	}
}
