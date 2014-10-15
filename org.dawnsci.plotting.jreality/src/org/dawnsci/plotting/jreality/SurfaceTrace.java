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

import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.dawnsci.plotting.util.PlottingUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.ui.PlatformUI;

/**
 * A class for holding surface trace data.
 * 
 * We may need to abstract some parts to a general 3D trace as more options are supported.
 * 
 * @author Matthew Gerring
 *
 */
public class SurfaceTrace extends Image3DTrace implements ISurfaceTrace{

	//private static Logger logger = LoggerFactory.getLogger(SurfaceTrace.class);
	private Dataset        data;

	public SurfaceTrace(JRealityPlotViewer plotter, String name) {
		super(plotter, name);
		plotType = PlottingMode.SURF2D;
	}

	@Override
	public IStatus setWindow(IROI window, IProgressMonitor monitor) {
		return setWindow(window, false, monitor);
	}

	/**
	 * Also ignores data windows outside the data size.
	 * @param window
	 * @param updateClipping
	 */
	@Override
	public IStatus setWindow(IROI window, boolean updateClipping, IProgressMonitor monitor) {
		// if a surface roi, we make sure there are no negative values
		if (window instanceof SurfacePlotROI) {
			int stXPt = (int) window.getPointX();
			int stYPt = (int) window.getPointY();
			stXPt = stXPt < 0 ? 0 : stXPt;
			stYPt = stYPt < 0 ? 0 : stYPt;
			((SurfacePlotROI)window).setPoint(stXPt, stYPt);
		}
		this.window = window;
		if (plotter!=null && this.isActive()) {
			return plotter.setSurfaceWindow(this.window, updateClipping, monitor);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IROI getWindow() {
		return window;
	}
//
//	private int[] normalize(int[] point, int maxX, int maxY) {
//		if (point[0]<0) point[0]=0;
//		if (point[0]>=maxX) point[0]=maxX-1;
//		
//		if (point[1]<0) point[1]=0;
//		if (point[1]>=maxY) point[1]=maxY-1;
//		return point;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public void setData(final IDataset data, List<? extends IDataset> axes) {
		if (window == null)
			window = SurfaceTrace.createSurfacePlotROI(data);

		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
		imageServiceBean.setImage(data);
		
		if (service==null) service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
		if (rescaleHistogram) {
			final double[] fa = service.getFastStatistics(imageServiceBean);
			setMin(fa[0]);
			setMax(fa[1]);
		}

		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		
		this.data = (Dataset)data;
		this.axes = (List<IDataset>) axes;
		if (isActive()) {
			plotter.updatePlot(createAxisValues(), plotter.getWindow(getWindow()), plotType, this.data);
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}
	}

	/**
	 * Creates a SurfacePlotROI 1/4 the size of the dataset given as a parameter
	 */
	public static SurfacePlotROI createSurfacePlotROI(IDataset data) {
		// Apply some downsampling to the surfacePlotROI
		int width = data.getShape()[1]/2;
		int height = data.getShape()[0]/2;
		int binShape = 1, samplingMode = 0;
		binShape = PlottingUtils.getBinShape(width, height, false);
		if (binShape != 1) {
			// DownsampleMode.MEAN = 2
			samplingMode = 2; 
		}
		SurfacePlotROI window = new SurfacePlotROI(0, 0, width, height, samplingMode, samplingMode, 0, 0);
		window.setLengths(new double[]{Double.valueOf(width), Double.valueOf(height)});
		window.setXBinShape(binShape);
		window.setYBinShape(binShape);
		window.setLowerClipping(0);
		window.setUpperClipping(Integer.MAX_VALUE);
		return window;
	}

	@Override
	public boolean is3DTrace() {
		return true;
	}

	@Override
	public void dispose() {
		try {
			plotter.removeSurfaceTrace(this);
			super.dispose();
		} catch (Throwable ignored) {
			// It's disposed anyway
		}
	}

	@Override
	public IDataset getData() throws RuntimeException {
		return data;
	}

}
