package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;

public interface IPlotModeColored extends IPlotMode {

	public void setMinMax(Number[] minMax);
	
	public void setAxesRange(double[] range);
}
