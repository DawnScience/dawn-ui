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
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;

public class ArpesMainImageReducer implements IDatasetROIReducer{
	
	private final RegionType regionType = RegionType.LINE;
	private List<IDataset> imageAxes;
	
	
	@Override
	public IDataset reduce(ILazyDataset data, List<AbstractDataset> axes,
			IROI roi, Slice[] slices, int[] order) {
		if (roi instanceof LinearROI) {
			final IDataset image = ((AbstractDataset)ROISliceUtils.getDataset(data, (LinearROI)roi, slices,new int[]{order[0],order[1]},1)).transpose();
			
			IDataset length = AbstractDataset.arange(image.getShape()[1], AbstractDataset.INT32);
			length.setName("Line Length");
			
			this.imageAxes = new ArrayList<IDataset>();
			this.imageAxes.add(length);
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
	public IROI getInitialROI(List<AbstractDataset> axes, int[] order) {

		
		int[] x = axes.get(0).getShape();
		int[] y = axes.get(1).getShape();
		
		double[] start = new double[]{0,0};
		double[] end = new double[]{x[0]/10,y[0]/10};
		
		return new LinearROI(start, end);

	}

	@Override
	public boolean supportsMultipleRegions() {
		return false;
	}

	@Override
	public List<IDataset> getAxes() {
		return imageAxes;
	}
}
