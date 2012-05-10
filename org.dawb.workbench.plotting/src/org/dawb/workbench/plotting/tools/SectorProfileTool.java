package org.dawb.workbench.plotting.tools;

import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public abstract class SectorProfileTool extends ProfileTool {


	@Override
	protected void createAxes(AbstractPlottingSystem plotter) {

		// Do nothing for now
	}
	
	protected abstract AbstractDataset getXAxis(final SectorROI sroi, final AbstractDataset integral);
	
	/**
	 * Please name the integral the same as the name you would like to plot.
	 * 
	 * @param data
	 * @param mask
	 * @param sroi
	 * @param region
	 * @param isDrag
	 * @return
	 */
	protected abstract AbstractDataset getIntegral( final AbstractDataset data,
										            final AbstractDataset mask, 
										            final SectorROI       sroi, 
						                            final IRegion         region,
										            final boolean         isDrag);


	@Override
	protected void createProfile(IImageTrace  image, 
			                     IRegion      region, 
			                     ROIBase      rbs, 
			                     boolean      tryUpdate,
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final SectorROI sroi = (SectorROI) (rbs==null ? region.getROI() : rbs);
		if (sroi==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		
		final AbstractDataset data = isDrag ? image.getDownsampled()     : image.getData();
		final AbstractDataset mask = isDrag ? image.getDownsampledMask() : image.getMask();
		if (isDrag) sroi.downsample(image.getDownsampleBin());
		
		
		final AbstractDataset integral = getIntegral(data, mask, sroi, region, isDrag);	
        if (integral==null) return;
				
		final AbstractDataset xi = getXAxis(sroi, integral);
		
		if (tryUpdate) {
			final ILineTrace x_trace = (ILineTrace)plotter.getTrace(integral.getName());
			
			if (x_trace!=null) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						x_trace.setData(xi, integral);
					}
				});
			}		
			
		} else {
						
			Collection<ITrace> plotted = plotter.createPlot1D(xi, Arrays.asList(new AbstractDataset[]{integral}), monitor);
			registerTraces(region, plotted);			
		}
			
	}
	
	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.SECTOR || type==RegionType.RING;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.SECTOR;
	}

}
