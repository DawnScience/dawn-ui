package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class BoxProfileTool extends ProfileTool {

	private IAxis xPixelAxis;
	private IAxis yPixelAxis;

	@Override
	protected void configurePlottingSystem(IPlottingSystem plotter) {
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
		if (bounds==null)
			return;
		if (!region.isVisible())
			return;

		if (monitor.isCanceled()) return;
		
		Dataset[] box = ROIProfile.box((Dataset)image.getData(), (Dataset)image.getMask(), bounds, true);
        if (box==null) return;
		//if (monitor.isCanceled()) return;
				
		final Dataset x_intensity = box[0];
		x_intensity.setName("X "+region.getName());
		Dataset xi = IntegerDataset.createRange(x_intensity.getSize());
		final Dataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		x_indices.setName("X Pixel");
		
		final Dataset y_intensity = box[1];
		y_intensity.setName("Y "+region.getName());
		Dataset yi = IntegerDataset.createRange(y_intensity.getSize());
		final Dataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
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
		IHierarchicalDataFile file = slice.getFile();

		String dataGroup = slice.getParent();

		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;

			RectangularROI bounds = (RectangularROI)region.getROI();
			
			//create roi name group
			String regionGroup = file.group(region.getName().replace(' ', '_'), dataGroup);
			file.setNexusAttribute(regionGroup, Nexus.DATA);

			//box profiles
			String profileGroup = file.group("profile", regionGroup);
			file.setNexusAttribute(profileGroup, Nexus.DATA);
			slice.setParent(profileGroup);

			Dataset[] box = ROIProfile.box((Dataset)slice.getData(), (Dataset)image.getMask(), (RectangularROI)region.getROI(), false);

			final Dataset x_intensity = box[0];
			x_intensity.setName("X_Profile");
			slice.appendData(x_intensity);

			final Dataset y_intensity = box[1];
			y_intensity.setName("Y_Profile");
			slice.appendData(y_intensity);

			// Mean, Sum, Std deviation and region
			int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
			int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;

			Dataset dataRegion = ((Dataset)slice.getData()).getSlice(
					new int[] { (int) bounds.getPoint()[1], (int) bounds.getPoint()[0] },
					new int[] { (int) bounds.getEndPoint()[1],(int) bounds.getEndPoint()[0] },
					new int[] {yInc, xInc});
			//mean
			Object mean = dataRegion.mean();
			Dataset meands = DatasetFactory.createFromObject(mean);
			meands.setName("Mean");
			slice.appendData(meands,regionGroup);

			//Sum
			Object sum = dataRegion.sum();
			Dataset sumds = DatasetFactory.createFromObject(sum);
			sumds.setName("Sum");
			slice.appendData(sumds,regionGroup);

			//Standard deviation
			Object std = dataRegion.stdDeviation();
			Dataset stds = DatasetFactory.createFromObject(std);
			stds.setName("Std_Deviation");
			slice.appendData(stds,regionGroup);

			//region
			slice.setParent(regionGroup);
			dataRegion.setName("Region_Slice");
			slice.appendData(dataRegion);
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
}
