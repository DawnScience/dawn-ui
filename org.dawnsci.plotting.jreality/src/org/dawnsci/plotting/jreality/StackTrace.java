package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.plot.trace.IStackTrace;
import org.dawb.common.ui.plot.trace.TraceEvent;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class StackTrace extends PlotterTrace implements IStackTrace {

	
	private AbstractDataset[] stack;

	public StackTrace(JRealityPlotViewer plotter2, String name2) {
		super(plotter2, name2);
	}

	@Override
	public AbstractDataset getData() {
		throw new RuntimeException("Please use getStack() instead!");
	}
	
	@Override
	public AbstractDataset[] getStack() {
		return stack;
	}

	@Override
	public void setData(List<AbstractDataset> axes, AbstractDataset... stack) {
		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		
		this.stack = stack;
		this.axes  = axes;
		
		if (isActive()) {
			plotter.updatePlot(createAxisValues(), null, PlottingMode.ONED_THREED, stack);
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}

	}
	@Override
	public boolean is3DTrace() {
		return true;
	}

	
}
