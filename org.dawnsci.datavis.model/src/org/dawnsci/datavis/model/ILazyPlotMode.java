package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public interface ILazyPlotMode extends IPlotMode {

	public void displayData(ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception;
	
}
