package org.dawnsci.datavis.model;

public interface IPlotController {

	IPlotMode getCurrentMode();

	void switchPlotMode(IPlotMode ob);

	PlottableObject getPlottableObject();

	void addPlotModeListener(PlotModeChangeEventListener plotModeListener);

	void removePlotModeListener(PlotModeChangeEventListener plotModeListener);

	void init();
	
	public IPlotDataModifier[] getCurrentPlotModifiers();
	
	public void enablePlotModifier(IPlotDataModifier modifier);
	
	public IPlotDataModifier getEnabledPlotModifier();
	
	public void dispose();

	void setColorProvider(ITraceColourProvider colorProvider);

	ITraceColourProvider getColorProvider();
	
	void forceReplot();

}
