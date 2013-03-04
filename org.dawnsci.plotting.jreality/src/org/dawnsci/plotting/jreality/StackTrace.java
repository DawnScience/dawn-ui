package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.plot.trace.ILineStackTrace;
import org.dawb.common.ui.plot.trace.TraceEvent;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

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
	protected List<AxisValues> createAxisValues() {
		
		final AxisValues xAxis = new AxisValues(getLabel(0), axes!=null?axes.get(0):null);
		final AxisValues yAxis = new AxisValues(getLabel(1), axes!=null?axes.get(1):null);
		final AxisValues zAxis;
		if (getWindow()==null || !(getWindow() instanceof LinearROI)) {
		    zAxis = new AxisValues(getLabel(2), axes!=null?axes.get(2):null);
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

    public void setWindow(ROIBase roi) {
		window=roi;
		if (plotter!=null && this.isActive()) plotter.setStackWindow(window);
	}

}
