package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        
		Dataset[] profile = getProfile(image, region, rbs, tryUpdate, isDrag, monitor);
		if (profile == null) return;

		final Dataset x_indices   = profile[0];
		final Dataset x_intensity = profile[1];
		final Dataset y_indices   = profile[2];
		final Dataset y_intensity = profile[3];
		
		//if (monitor.isCanceled()) return;
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace("X "+region.getName());
		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace("Y "+region.getName());
		
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
	
	/**
	 * 
	 * @param image
	 * @param region
	 * @param rbs
	 * @param tryUpdate
	 * @param isDrag
	 * @param monitor
	 * @return x_indices, x_intensity, y_indices, y_intensity   OR null
	 */
	protected Dataset[] getProfile(IImageTrace  image, 
					            IRegion      region, 
					            IROI      rbs, 
					            boolean      tryUpdate,
					            boolean      isDrag,
					            IProgressMonitor monitor) {
		
		if (monitor.isCanceled()) return null;
		if (image==null) return null;
		
		if (!isRegionTypeSupported(region.getRegionType())) return null;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null)
			return null;
		if (!region.isVisible())
			return null;

		if (monitor.isCanceled()) return null;
		
		Dataset[] box = ROIProfile.box((Dataset)image.getData(), (Dataset)image.getMask(), bounds, true);
        if (box==null) return null;
        
        Dataset xi = null;
        Dataset yi = null;
        
        double ang = bounds.getAngle();
        //TODO probably better to deal with this in ROIProfile class, but this will do for now.
        if (image.getAxes() !=  null && ang == 0) {
        	List<IDataset> axes = image.getAxes();
        	
        	int[] spt = bounds.getIntPoint();
    		int[] len = bounds.getIntLengths();
        	
        	final int xstart  = Math.max(0,  spt[1]);
			final int xend   = Math.min(spt[1] + len[1],  image.getData().getShape()[0]);
			final int ystart = Math.max(0,  spt[0]);
			final int yend   = Math.min(spt[0] + len[0],  image.getData().getShape()[1]);
			
			try {
				IDataset xFull = axes.get(0);
			    xi = (Dataset)xFull.getSlice(new int[]{ystart}, new int[]{yend},new int[]{1});
			    xi.setName(xFull.getName());
			} catch (Exception ne) {
				//ignore
			}
			
			try {
				IDataset yFull = axes.get(1);
			    yi = (Dataset)yFull.getSlice(new int[]{xstart}, new int[]{xend},new int[]{1});
			    yi.setName(yFull.getName());
			} catch (Exception ne) {
				//ignore
			}
        	
        }
        
		//if (monitor.isCanceled()) return;
				
		final Dataset x_intensity = box[0];
		x_intensity.setName("X "+region.getName());
		if (xi == null || !Arrays.equals(xi.getShape(), x_intensity.getShape())){
			xi = IntegerDataset.createRange(x_intensity.getSize());
			xi.setName("X Pixel");
		}
		final Dataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		
		
		final Dataset y_intensity = box[1];
		y_intensity.setName("Y "+region.getName());
		if (yi == null || !Arrays.equals(yi.getShape(), y_intensity.getShape())) {
			yi = IntegerDataset.createRange(y_intensity.getSize());
			yi.setName("Y Pixel");
		}
		final Dataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
		
        return new Dataset[]{x_indices, x_intensity, y_indices, y_intensity};
	}
	
	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS)||type==RegionType.PERIMETERBOX;
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
		
		// Fix to http://jira.diamond.ac.uk/browse/SCI-1898
		file.setNexusAttribute(dataGroup, Nexus.SUBENTRY);
		
		if (slice.getMonitor()!=null && slice.getMonitor().isCancelled()) return null;

		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;

			RectangularROI bounds = (RectangularROI)region.getROI();
			
			//create roi name group
			String datasetName = region.getName();
			if (datasetName.startsWith(dataGroup)) datasetName = datasetName.substring(dataGroup.length());
			datasetName = datasetName.replace(' ', '_');
			
			String regionGroup = file.group(datasetName, dataGroup);
			file.setNexusAttribute(regionGroup, Nexus.DATA);

			//box profiles
			String profileGroup = file.group("profile", dataGroup);
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
