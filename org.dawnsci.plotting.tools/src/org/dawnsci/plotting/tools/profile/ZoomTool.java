package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

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
	protected void createProfile(final IImageTrace  image, 
			                     IRegion      region,
			                     IROI      rbs, 
			                     boolean      tryUpdate, 
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
		
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if ((region.getRegionType()!=RegionType.BOX)&&(region.getRegionType()!=RegionType.PERIMETERBOX)) return;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;

		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
		
		try {
			AbstractDataset slice = null;
			AbstractDataset im    = (AbstractDataset)image.getData();
			double xStart = bounds.getPoint()[0];
			double yStart = bounds.getPoint()[1];
			double xEnd = bounds.getEndPoint()[0];
			double yEnd = bounds.getEndPoint()[1];
			// shape[0] = y, shape[1] = x
			int[] shape = im.getShape();
			// Check for most of the possible cases where a region might be out of bounds
			if(xStart < 0 && yStart >= 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart < 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, shape[0] },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart < 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { shape[1], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if (xStart >= 0 && yStart < 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
					new int[] { (int) yEnd, (int) xEnd },
					new int[] {yInc, xInc});
	
			slice.setName(region.getName());
			// Calculate axes to have real values not size
			AbstractDataset yLabels = null;
			AbstractDataset xLabels = null;
			if (image.getAxes()!=null && image.getAxes().size() > 0) {
				AbstractDataset xl = (AbstractDataset)image.getAxes().get(0);
				if (xl!=null) xLabels = getLabelsFromLabels(xl, bounds, 0);
				AbstractDataset yl = (AbstractDataset)image.getAxes().get(1);
				if (yl!=null) yLabels = getLabelsFromLabels(yl, bounds, 1);
			}
			
			if (yLabels==null) yLabels = IntegerDataset.arange(bounds.getPoint()[1], bounds.getEndPoint()[1], yInc);
			if (xLabels==null) xLabels = IntegerDataset.arange(bounds.getPoint()[0], bounds.getEndPoint()[0], xInc);
			
			final IImageTrace zoom_trace = (IImageTrace)profilePlottingSystem.updatePlot2D(slice, Arrays.asList(new IDataset[]{xLabels, yLabels}), monitor);
			registerTraces(region, Arrays.asList(new ITrace[]{zoom_trace}));
			Display.getDefault().syncExec(new Runnable()  {
				public void run() {
				     zoom_trace.setPaletteData(image.getPaletteData());
				}
			});
			
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger.trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
		}

	}

	private AbstractDataset getLabelsFromLabels(AbstractDataset xl, RectangularROI bounds, int axisIndex) {
		try {
			int fromIndex = (int)bounds.getPoint()[axisIndex];
			int toIndex   = (int)bounds.getEndPoint()[axisIndex];
			int step      = toIndex>fromIndex ? 1 : -1;
			final AbstractDataset slice = xl.getSlice(new int[]{fromIndex}, new int[]{toIndex}, new int[]{step});
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
			
			final AbstractDataset slice = ((AbstractDataset)drslice.getData()).getSlice(new int[] { (int) bounds.getPoint()[1],   (int) bounds.getPoint()[0]    },
											                       new int[] { (int) bounds.getEndPoint()[1],(int) bounds.getEndPoint()[0] },
											                       new int[] {yInc, xInc});
			slice.setName(region.getName().replace(' ','_'));
			
			drslice.appendData(slice);
		}
        return new DataReductionInfo(Status.OK_STATUS);

	}
}
