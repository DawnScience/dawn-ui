/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IMulti2DTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.ui.PlatformUI;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;




/**
 * A class for holding multi image trace data.
 * 
 * We may need to abstract some parts to a general 3D trace as more options are supported.
 * 
 * 
 */
public class Multi2DTrace extends Image3DTrace implements IMulti2DTrace{

	//private static Logger logger = LoggerFactory.getLogger(Multi2DTrace.class);
	private IDataset[] data;

	public Multi2DTrace(JRealityPlotViewer plotter, String name) {
		super(plotter, name);
		plotType = PlottingMode.MULTI2D;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setData(List<? extends IDataset> axes, IDataset... s) {
		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
		imageServiceBean.setImage(s[0]);
		
		if (service==null) service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
		if (rescaleHistogram) {
			final float[] fa = service.getFastStatistics(imageServiceBean);
			setMin(fa[0]);
			setMax(fa[1]);
		}

		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		
		this.data = s;
		this.axes  = (List<IDataset>) axes;
		
		if (isActive()) {
			plotter.updatePlot(createAxisValues(), null, plotType, (Dataset[])data);
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}
	}

	@Override
	public boolean is3DTrace() {
		return true;
	}

	public void dispose() {
		try {
			plotter.removeMulti2DTrace(this);
			super.dispose();
		} catch (Throwable ignored) {
			// It's disposed anyway
		}
	}

	@Override
	public IDataset[] getMulti2D() {
		return data;
	}

	/**
	 * Not in use!
	 * Use getMulti2D() method instead
	 */
	@Override
	public IDataset getData() {
		// TODO Auto-generated method stub
		return null;
	}

}
