package org.dawnsci.jzy3d.plotmodes;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IWaterfallTrace;

public class PlotModeWaterfall extends AbstractJZY3DImagePlotMode {

	
	@Override
	public String getName() {
		return "Waterfall";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IWaterfallTrace;
	}
	
	@Override
	protected ITrace createTrace(String name, IPlottingSystem<?> system) {
		return system.createTrace(name,IWaterfallTrace.class);
	}
}