package org.dawnsci.multidimensional.ui.hyper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;

public class ArpesXImageReducer implements IDatasetROIReducer {

	private final RegionType regionType = RegionType.XAXIS;
	private List<IDataset> imageAxes;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order)  throws Exception{
		
		if (roi instanceof RectangularROI) {
			IDataset image = ROISliceUtils.getXAxisDataset2DAverage(data, (RectangularROI)roi, slices, order[0], null);
			
			if (order[0] < order[1]) image = DatasetUtils.transpose(image);
			
			this.imageAxes = new ArrayList<IDataset>();
			this.imageAxes.add(axes.get(1).getSlice());
			this.imageAxes.add(axes.get(2).getSlice());
			
			return image;
		}
		

		return null;
	}
	
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
	public IROI getInitialROI(List<IDataset> axes, int[] order) {
		double len = axes.get(order[0]).getSize();
		
		return new XAxisBoxROI(len/2, 0, len/10, 0,0);
	}

}
