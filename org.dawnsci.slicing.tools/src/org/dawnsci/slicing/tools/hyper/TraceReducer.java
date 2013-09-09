package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class TraceReducer implements IDatasetROIReducer {

	private final RegionType regionType = RegionType.BOX;
	private List<IDataset> traceAxes;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<AbstractDataset> axes,
			IROI roi, Slice[] slices, int[] order) {
		if (roi instanceof RectangularROI) {
			
			AbstractDataset output = (AbstractDataset)ROISliceUtils.getDataset(data, (RectangularROI)roi, slices, new int[]{order[0],order[1]}, 1);
			
			if (order[0] > order[1]) output = output.mean(order[0]).mean(order[1]);
			else output = output.mean(order[1]).mean(order[0]);

			this.traceAxes = new ArrayList<IDataset>();
			this.traceAxes.add(axes.get(2).getSlice());
			
			return output.squeeze();
		}
		return null;
	}

	@Override
	public boolean isOutput1D() {
		return true;
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
		
		return new RectangularROI(x[0]/10, y[0]/10, x[0]/10, y[0]/10, 0);
	}
	
	@Override
	public boolean supportsMultipleRegions() {
		return true;
	}

	@Override
	public List<IDataset> getAxes() {
		return traceAxes;
	}

}
