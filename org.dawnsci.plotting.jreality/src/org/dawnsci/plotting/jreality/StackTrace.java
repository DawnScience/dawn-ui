package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.api.trace.TraceEvent;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class StackTrace extends PlotterTrace implements ILineStackTrace {

	
	private AbstractDataset[] stack;

	public StackTrace(JRealityPlotViewer plotter2, String name2) {
		super(plotter2, name2);
	}

	@Override
	public AbstractDataset getData() {
		throw new RuntimeException("Please use getStack() instead!");
	}
	
	@Override
	public IDataset[] getStack() {
		return stack;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setData(List<? extends IDataset> axes, IDataset... s) {
		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		
		this.stack = getStack(s);
		this.axes  = (List<IDataset>) axes;
		
		if (isActive()) {
			plotter.updatePlot(createAxisValues(), null, PlottingMode.ONED_THREED, stack);
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}

	}
	
	@Override
	protected List<AxisValues> createAxisValues() {
		
		final AxisValues xAxis = new AxisValues(getLabel(0), axes!=null?(AbstractDataset)axes.get(0):null);
		final AxisValues yAxis = new AxisValues(getLabel(1), axes!=null?(AbstractDataset)axes.get(1):null);
		final AxisValues zAxis;
		if (getWindow()==null || !(getWindow() instanceof LinearROI)) {
		    zAxis = new AxisValues(getLabel(2), axes!=null?(AbstractDataset)axes.get(2):null);
		} else {
			final int x1 = window.getIntPoint()[0];
			final int x2 = (int)Math.round(((LinearROI)window).getEndPoint()[0]);
			final int len = x2-x1;
			zAxis = new AxisValues(getLabel(2), AbstractDataset.arange(len, AbstractDataset.INT32));
		}
		return Arrays.asList(xAxis, yAxis, zAxis);
	}

	@Override
	public boolean is3DTrace() {
		return true;
	}

	@Override
    public void setWindow(IROI roi) {
		window=roi;
		if (plotter!=null && this.isActive()) plotter.setStackWindow(window);
	}
    
	public void dispose() {
		try {
			plotter.removeStackTrace(this);
			super.dispose();
		} catch (Throwable ignored) {
			// It's disposed anyway
		}
	}


}
