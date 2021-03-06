package org.dawnsci.mapping.ui.datamodel;

public interface IMapPlotController {
	
	PlottableMapObject getTopMap();
	
	PlottableMapObject getTopMap(double x, double y);
	
	void plotData(final double x, final double y, boolean hold);
	
	void setTransparency(PlottableMapObject map);
	
	void bringToFront(PlottableMapObject map);
	
	void sendToBack(PlottableMapObject map);
	
	public HighAspectImageDisplay getHighAspectImageDisplayMode();

	public void setHighAspectImageDisplayMode(HighAspectImageDisplay highAspectImageDisplayMode);
	
	public boolean initPlots();

}
