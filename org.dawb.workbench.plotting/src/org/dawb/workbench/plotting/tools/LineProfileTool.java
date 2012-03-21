package org.dawb.workbench.plotting.tools;

import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.LinearROIData;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class LineProfileTool extends ProfileTool {

	@Override
	protected void createAxes(AbstractPlottingSystem plotter) {
		plotter.getSelectedXAxis().setTitle("Pixel");
		plotter.getSelectedYAxis().setTitle("Intensity");
	}

	@Override
	protected void createProfile(	IImageTrace  image, 
						            IRegion      region, 
						            RegionBounds rbs, 
						            boolean      tryUpdate,
						            IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (region.getRegionType()!=RegionType.LINE) return;

		final RegionBounds bounds = rbs==null ? region.getRegionBounds() : rbs;
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		LinearROI    roi = new LinearROI(bounds.getP1(), bounds.getP2());
		if (monitor.isCanceled()) return;
		LinearROIData ld = new LinearROIData(roi, image.getData(), 1d);

		if (monitor.isCanceled()) return;
		
		final AbstractDataset intensity = ld.getProfileData(0);
		intensity.setName(region.getName());
		final AbstractDataset indices = ld.getXAxes()[0].toDataset();
		indices.setName("Pixel");
		
		if (tryUpdate) {
		
			final ILineTrace trace = (ILineTrace)plotter.getTrace(region.getName());
			
			if (trace!=null && !monitor.isCanceled()) getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					trace.setData(indices, intensity);
				}
			});
			
		} else {
			if (monitor.isCanceled()) return;
			Collection<ITrace> plotted = plotter.createPlot1D(indices, Arrays.asList(new AbstractDataset[]{intensity}), monitor);
			registerTraces(region, plotted);
			
		}
		
	}

	public RegionType getRegionType() {
		return RegionType.LINE;
	}

}
