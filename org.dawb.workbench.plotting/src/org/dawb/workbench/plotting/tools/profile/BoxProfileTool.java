package org.dawb.workbench.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

import ncsa.hdf.object.Group;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.gda.extensions.loaders.H5Utils;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
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
			                     ROIBase      rbs, 
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
		
		AbstractDataset[] box = ROIProfile.box(image.getData(), image.getMask(), bounds, true);
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
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[]{x_intensity}), monitor);
			registerTraces(region, plotted);
			
			profilePlottingSystem.setSelectedXAxis(yPixelAxis);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[]{y_intensity}), monitor);
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
	public IStatus export(IHierarchicalDataFile file, Group parent, AbstractDataset data, IProgressMonitor monitor) throws Exception {

		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			
			AbstractDataset[] box = ROIProfile.box(data, image.getMask(), (RectangularROI)region.getROI(), false);
			
			final AbstractDataset x_intensity = box[0];
			x_intensity.setName("X_"+region.getName().replace(' ', '_'));
			H5Utils.appendDataset(file, parent, x_intensity);
			
			final AbstractDataset y_intensity = box[1];
			y_intensity.setName("Y_"+region.getName().replace(' ', '_'));
			H5Utils.appendDataset(file, parent, y_intensity);
		}
		 
        return Status.OK_STATUS;
	}

}
