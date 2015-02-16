/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

import org.dawnsci.plotting.tools.utils.ToolUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoomTool extends ProfileTool {
	
	private static Logger logger = LoggerFactory.getLogger(ZoomTool.class);

	@Override
	protected void configurePlottingSystem(IPlottingSystem plotter) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected String getRegionName() {
		return "Zoom";
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.BOX||type==RegionType.PERIMETERBOX;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	@Override
	protected ITrace createProfile(final IImageTrace  image, 
			                     IRegion      region,
			                     IROI         rbs, 
			                     boolean      tryUpdate, 
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
		
		try {
		    createZoom(image, region, rbs, tryUpdate, isDrag, monitor);
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger.trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
		}
        return profilePlottingSystem.getTraces().iterator().next();
	}
	

	protected Dataset createZoom(final IImageTrace  image, 
					            IRegion      region,
					            IROI         rbs, 
					            boolean      tryUpdate, 
					            boolean      isDrag,
					            IProgressMonitor monitor) {

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return null;
		if (!region.isVisible()) return null;

		if (monitor.isCanceled()) return null;

		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;

		Dataset slice = null;
		getClass();
		Dataset im    = (Dataset)image.getData();
		slice = (Dataset) ToolUtils.getClippedSlice(im, bounds);
		slice.setName(region.getName());
		// Calculate axes to have real values not size
		Dataset yLabels = null;
		Dataset xLabels = null;
		if (image.getAxes()!=null && image.getAxes().size() > 0) {
			Dataset xl = (Dataset)image.getAxes().get(0);
			if (xl!=null) xLabels = ZoomTool.getLabelsFromLabels(xl, bounds, 0);
			Dataset yl = (Dataset)image.getAxes().get(1);
			if (yl!=null) yLabels = ZoomTool.getLabelsFromLabels(yl, bounds, 1);
		}

		if (yLabels==null) yLabels = IntegerDataset.createRange(bounds.getPoint()[1], bounds.getEndPoint()[1], yInc);
		if (xLabels==null) xLabels = IntegerDataset.createRange(bounds.getPoint()[0], bounds.getEndPoint()[0], xInc);

		final IImageTrace zoom_trace = (IImageTrace)profilePlottingSystem.updatePlot2D(slice, Arrays.asList(new IDataset[]{xLabels, yLabels}), monitor);
		registerTraces(region, Arrays.asList(new ITrace[]{zoom_trace}));
		Display.getDefault().syncExec(new Runnable()  {
			public void run() {
				zoom_trace.setPaletteData(image.getPaletteData());
			}
		});

		return slice;

	}


	static Dataset getLabelsFromLabels(Dataset xl, RectangularROI bounds, int axisIndex) {
		try {
			int fromIndex = (int)bounds.getPoint()[axisIndex];
			int toIndex   = (int)bounds.getEndPoint()[axisIndex];
			int step      = toIndex>fromIndex ? 1 : -1;
			final Dataset slice = xl.getSlice(new int[]{fromIndex}, new int[]{toIndex}, new int[]{step});
			return slice;
		} catch (Exception ne) {
			return null;
		}
	}
	
	@Override
	public DataReductionInfo export(DataReductionSlice drslice) throws Exception {

		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			
			final RectangularROI bounds = (RectangularROI)region.getROI();
			if (bounds==null)        continue;
			if (!region.isVisible()) continue;

			final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
			final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
			
			final Dataset slice = ((Dataset)drslice.getData()).getSlice(new int[] { (int) bounds.getPoint()[1],   (int) bounds.getPoint()[0]    },
											                       new int[] { (int) bounds.getEndPoint()[1],(int) bounds.getEndPoint()[0] },
											                       new int[] {yInc, xInc});
			slice.setName(region.getName().replace(' ','_'));
			
			drslice.appendData(slice);
		}
        return new DataReductionInfo(Status.OK_STATUS);

	}
}
