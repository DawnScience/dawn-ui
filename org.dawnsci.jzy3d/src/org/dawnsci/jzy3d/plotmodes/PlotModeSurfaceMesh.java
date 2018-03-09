package org.dawnsci.jzy3d.plotmodes;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceMeshTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public class PlotModeSurfaceMesh extends AbstractJZY3DImagePlotMode {
	
	@Override
	public String getName() {
		return "Surface";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ISurfaceMeshTrace;
	}

	@Override
	protected ITrace createTrace(String name, IPlottingSystem<?> system) {
		return system.createTrace(name,ISurfaceMeshTrace.class);
	}
}