package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;

public class ArpesMainImageReducer implements IDatasetROIReducer {
	
	private final RegionType regionType = RegionType.LINE;
	private List<IDataset> imageAxes;
	
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order) {
		if (roi instanceof LinearROI) {
			final IDataset image = DatasetUtils.transpose(ROISliceUtils.getDataset(data, (LinearROI)roi, slices,new int[]{order[0],order[1]},1));
			
			IDataset length = DatasetFactory.createRange(image.getShape()[1], Dataset.INT32);
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
	public IROI getInitialROI(List<IDataset> axes, int[] order) {

		
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
