package org.dawnsci.mapping.ui.datamodel;

public interface IMapPlotController {
	
	PlottableMapObject getTopMap();
	
	PlottableMapObject getTopMap(double x, double y);
	
	void plotData(final double x, final double y, boolean hold);
	
	void updatePlot();
	
	void setTransparency(PlottableMapObject map);

}
