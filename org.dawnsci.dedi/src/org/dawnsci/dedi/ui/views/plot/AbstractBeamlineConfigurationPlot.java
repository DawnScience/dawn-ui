package org.dawnsci.dedi.ui.views.plot;

import org.dawnsci.dedi.ui.widgets.plotting.Legend;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Composite;


public abstract class AbstractBeamlineConfigurationPlot {
	protected IPlottingSystem<Composite> system;
	
	public AbstractBeamlineConfigurationPlot(IPlottingSystem<Composite> system) {
		this.system = system;
	}
	
	public abstract void createPlotControls(Composite plotConfigurationPanel, Legend legend);
	
	public abstract void updatePlot();
	
	public abstract void dispose();
}
