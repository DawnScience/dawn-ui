/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class LineProfileTool extends ProfileTool {

	@Override
	protected void configurePlottingSystem(IPlottingSystem plotter) {
		plotter.getSelectedXAxis().setTitle("Pixel");
		plotter.getSelectedYAxis().setTitle("Intensity");
	}

	@Override
	protected Collection<ITrace> createProfile(	IImageTrace  image, 
						            IRegion      region, 
						            IROI         rbs, 
						            boolean      tryUpdate,
				                    boolean      isDrag,
						            IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return null;
		if (image==null) return null;
		
		if (!isRegionTypeSupported(region.getRegionType())) return null;

		final LinearROI bounds = (LinearROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return null;
		if (!region.isVisible()) return null;

		if (monitor.isCanceled()) return null;
		Dataset[] profileData = ROIProfile.line((Dataset)image.getData(), (Dataset)image.getMask(), bounds, 1d, true);
        if (profileData==null) return null;

		if (monitor.isCanceled()) return null;
		
		final Dataset indices = IntegerDataset.createRange(0, profileData[0].getSize(), 1d);
		indices.setName("Pixel");
		
		List<ITrace> traces = new ArrayList<ITrace>(2);
		for (int i = 0; i < profileData.length; i++) {
			
			final Dataset    intensity = profileData[i];	
			final String     name      = i==0?region.getName():region.getName()+"(Y)";
			intensity.setName(name);
			
			final ILineTrace trace     = (ILineTrace)profilePlottingSystem.getTrace(name);
			if (tryUpdate && trace!=null) {
				traces.add(trace);
				if (trace!=null && !monitor.isCanceled()) getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						trace.setData(indices, intensity);
					}
				});
				
			} else {
				if (monitor.isCanceled()) return null;
				Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[]{intensity}), monitor);
				registerTraces(name, plotted);
				traces.add(plotted.iterator().next());
			}
		}
		return traces;
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.LINE;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.LINE;
	}

	
	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
		
		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;
			
			Dataset[] profileData = ROIProfile.line((Dataset)slice.getData(), (Dataset)image.getMask(), (LinearROI)region.getROI(), 1d, false);
			final Dataset intensity = profileData[0];
			intensity.setName(region.getName().replace(' ', '_'));
			slice.appendData(intensity);
		}
        return new DataReductionInfo(Status.OK_STATUS);
	}
}
