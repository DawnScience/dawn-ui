/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.jreality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.trace.ILineStackTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;

public class StackTrace extends PlotterTrace implements ILineStackTrace {

	
	private Dataset[] stack;

	public StackTrace(JRealityPlotViewer plotter2, String name2) {
		super(plotter2, name2);
	}

	@Override
	public Dataset getData() {
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
			IROI roi = new LinearROI(new double[]{0.0,0.0}, new double[]{stack.length,0.0});
			plotter.updatePlot(createAxisValues(roi), roi, PlottingMode.ONED_THREED, stack);
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}

	}
	
	@Override
	protected List<AxisValues> createAxisValues() {
		return createAxisValues(window);
	}

	/**
	 * Creates the Axes values given a window
	 * @param window
	 * @return axes list
	 */
	protected List<AxisValues> createAxisValues(IROI window) {
		ArrayList<AxisValues> values = new ArrayList<AxisValues>();

		int a = 0;
		int nAxes;
		final IDataset y, z;
		if (axes == null) {
			nAxes = 0;
			values.add(new AxisValues(getLabel(a++), null));
			y = null;
			z = null;
		} else {
			nAxes = axes.size();
			String l = getLabel(a++);
			for (int i = 0; i < (nAxes - 2); i++) {
				values.add(new AxisValues(l, DatasetUtils.convertToDataset(axes.get(i))));
			}
			y = axes.get(nAxes - 2);
			z = axes.get(nAxes - 1);
		}
		values.add(new AxisValues(getLabel(a++), DatasetUtils.convertToDataset(y)));

		final AxisValues zAxis;
		if (z != null) {
			Dataset tz = DatasetUtils.convertToDataset(z);
			if (window instanceof LinearROI && tz.getRank() == 1) {
				final int x1 = window.getIntPoint()[0];
				final int x2 = (int) Math.ceil(((LinearROI) window).getEndPoint()[0]);
				tz = tz.getSliceView(new Slice(x1, x2));
			}
			zAxis = new AxisValues(getLabel(a), tz);
		} else if (window instanceof LinearROI) {
			final int x1 = window.getIntPoint()[0];
			final int x2 = (int) Math.ceil(((LinearROI) window).getEndPoint()[0]);
			final int len = x2 - x1;
			zAxis = new AxisValues(getLabel(a), DatasetFactory.createRange(len, Dataset.INT32));
		} else {
			zAxis = new AxisValues(getLabel(a), null);
		}
		values.add(zAxis);
		return values;
	}

	@Override
	public boolean is3DTrace() {
		return true;
	}

	@Override
	public void setWindow(IROI window) {
		setWindow(window, null);
	}

	@Override
	public IStatus setWindow(IROI roi, IProgressMonitor monitor) {
		window=roi;
		if (plotter!=null && this.isActive()) plotter.setStackWindow(window);
		return Status.OK_STATUS;
	}

	public void dispose() {
		try {
			plotter.removeStackTrace(this);
			super.dispose();
		} catch (Throwable ignored) {
			// It's disposed anyway
		}
	}

	@Override
	public int getRank() {
		return 1;
	}
}
