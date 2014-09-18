package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

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
	protected void createProfile(	IImageTrace  image, 
						            IRegion      region, 
						            IROI         rbs, 
						            boolean      tryUpdate,
				                    boolean      isDrag,
						            IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final LinearROI bounds = (LinearROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null)
			return;
		if (!region.isVisible())
			return;

		if (monitor.isCanceled()) return;
		Dataset[] profileData = ROIProfile.line((Dataset)image.getData(), (Dataset)image.getMask(), bounds, 1d, true);
        if (profileData==null) return;

		if (monitor.isCanceled()) return;
		
		final Dataset intensity = profileData[0];
		intensity.setName(region.getName());
		final Dataset indices = IntegerDataset.createRange(0, intensity.getSize(), 1d);
		indices.setName("Pixel");
		
		final ILineTrace trace = (ILineTrace)profilePlottingSystem.getTrace(region.getName());
		if (tryUpdate && trace!=null) {
			if (trace!=null && !monitor.isCanceled()) getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					trace.setData(indices, intensity);
				}
			});
			
		} else {
			if (monitor.isCanceled()) return;
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[]{intensity}), monitor);
			registerTraces(region, plotted);
			
		}
		
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
