package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class BoxProfileTool extends ProfileTool {

	private IAxis xPixelAxis;
	private IAxis yPixelAxis;

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
		if (xPixelAxis==null) {
			this.xPixelAxis = plotter.getSelectedXAxis();
			xPixelAxis.setTitle("X Pixel");
		}
		
		if (yPixelAxis==null) {
			this.yPixelAxis = plotter.createAxis("Y Pixel", false, SWT.TOP);		
			plotter.getSelectedYAxis().setTitle("Intensity");
		}
	}
	
	@Override
	protected void createProfile(IImageTrace  image, 
			                     IRegion      region, 
			                     IROI      rbs, 
			                     boolean      tryUpdate,
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		
		AbstractDataset[] box = ROIProfile.box((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, true);
        if (box==null) return;
		//if (monitor.isCanceled()) return;
				
		final AbstractDataset x_intensity = box[0];
		x_intensity.setName("X "+region.getName());
		AbstractDataset xi = IntegerDataset.arange(x_intensity.getSize());
		final AbstractDataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		x_indices.setName("X Pixel");
		
		final AbstractDataset y_intensity = box[1];
		y_intensity.setName("Y "+region.getName());
		AbstractDataset yi = IntegerDataset.arange(y_intensity.getSize());
		final AbstractDataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
		y_indices.setName("Y Pixel");

		//if (monitor.isCanceled()) return;
		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace("X "+region.getName());
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace("Y "+region.getName());
		
		if (tryUpdate && x_trace!=null && y_trace!=null) {
			
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					profilePlottingSystem.setSelectedXAxis(xPixelAxis);
					x_trace.setData(x_indices, x_intensity);
					profilePlottingSystem.setSelectedXAxis(yPixelAxis);
					y_trace.setData(y_indices, y_intensity);						
				}
			});

			
		} else {
						
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new IDataset[]{x_intensity}), monitor);
			registerTraces(region, plotted);
			
			profilePlottingSystem.setSelectedXAxis(yPixelAxis);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new IDataset[]{y_intensity}), monitor);
			registerTraces(region, plotted);
			
		}
			
	}
	
	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	/**
	 * Same tool called recursively from the DataReductionWizard
	 */
	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {

		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;
			
			AbstractDataset[] box = ROIProfile.box(slice.getData(), (AbstractDataset)image.getMask(), (RectangularROI)region.getROI(), false);
			
			final AbstractDataset x_intensity = box[0];
			x_intensity.setName("X_"+region.getName().replace(' ', '_'));
			slice.appendData(x_intensity);
			
			final AbstractDataset y_intensity = box[1];
			y_intensity.setName("Y_"+region.getName().replace(' ', '_'));
			slice.appendData(y_intensity);
		}
		 
        return new DataReductionInfo(Status.OK_STATUS);
	}

}
