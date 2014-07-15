package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.plotting.api.trace.IScatter3DTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class Scatter3DTrace extends PlotterTrace implements IScatter3DTrace {

	
	private AbstractDataset scatter;

	public Scatter3DTrace(JRealityPlotViewer plotter2, String name2) {
		super(plotter2, name2);
	}

	@Override
	public AbstractDataset getData() {
		return scatter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setData(IDataset data, List<? extends IDataset> axes) {
		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		this.scatter = (AbstractDataset) data;
		this.axes  = (List<IDataset>) axes;
		
		if (isActive()) {
			plotter.updatePlot(createAxisValues(), null, PlottingMode.SCATTER3D, scatter);
			
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
		if (window instanceof LinearROI) {
			final int x1 = window.getIntPoint()[0];
			final int x2 = (int)Math.round(((LinearROI) window).getEndPoint()[0]);
			final int len = x2-x1;
			zAxis = new AxisValues(getLabel(2), AbstractDataset.arange(len, AbstractDataset.INT32));
		} else {
			zAxis = new AxisValues(getLabel(2), axes!=null?(AbstractDataset)axes.get(2):null);
		}
		return Arrays.asList(xAxis, yAxis, zAxis);
	}

	@Override
	public boolean is3DTrace() {
		return true;
	}

	@Override
	public IStatus setWindow(IROI roi, IProgressMonitor monitor) {
		window=roi;
		if (plotter!=null && this.isActive()) plotter.setStackWindow(window);
		return Status.OK_STATUS;
	}

	public void dispose() {
		try {
			plotter.removeScatter3DTrace(this);
			super.dispose();
		} catch (Throwable ignored) {
			// It's disposed anyway
		}
	}

	@Override
	public int getRank() {
		return 3;
	}
}
