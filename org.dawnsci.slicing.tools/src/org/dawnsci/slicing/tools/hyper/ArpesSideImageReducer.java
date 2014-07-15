package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.YAxisBoxROI;

public class ArpesSideImageReducer implements IDatasetROIReducer {
	
	private final RegionType regionType = RegionType.YAXIS;
	private List<IDataset> imageAxes;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<AbstractDataset> axes,
			IROI roi, Slice[] slices, int[] order) {
		if (roi instanceof RectangularROI) {
			IDataset image = ROISliceUtils.getYAxisDataset2DAverage(data, (RectangularROI)roi, slices, order[2]);
			
			if (order[0] < order[1]) image = ((AbstractDataset)image).transpose();
			
			this.imageAxes = new ArrayList<IDataset>();
			this.imageAxes.add(axes.get(0).getSlice());
			this.imageAxes.add(axes.get(1).getSlice());
			
			return image;
		}
		

		return null;
	}
	
//	@Override
//	public IDataset reduce(ILazyDataset data, List<ILazyDataset> axes,
//			int dim, IROI roi) {
//		if (roi instanceof RectangularROI) {
//			getAxisDataset(ILazyDataset lz, IDataset axis, RectangularROI roi, Slice[] slices, int dim, int step)
//			final IDataset image = ROISliceUtils.getAxisDataset(data, (RectangularROI)roi, dim);
//			
//			int[] imageAxis = ROISliceUtils.getImageAxis(dim);
//			this.imageAxes = new ArrayList<IDataset>();
//			this.imageAxes.add(axes.get(imageAxis[0]).getSlice());
//			this.imageAxes.add(axes.get(imageAxis[1]).getSlice());
//			
//			return image;
//		}
//		
//		return null;
//	}

	@Override
	public boolean isOutput1D() {
		return false;
	}

	@Override
	public List<RegionType> getSupportedRegionType() {
		
		List<IRegion.RegionType> regionList = new ArrayList<IRegion.RegionType>();
		regionList.add(regionType);
		
		return regionList;
	}
	
	
	@Override
	public boolean supportsMultipleRegions() {
		return false;
	}

	@Override
	public List<IDataset> getAxes() {
		return imageAxes;
	}

	@Override
	public IROI getInitialROI(List<AbstractDataset> axes, int[] order) {
		double len = axes.get(2).count();
		
		return new YAxisBoxROI(0,len/10,0,len/20, 0);
	}
}

