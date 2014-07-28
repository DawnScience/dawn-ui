package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.slicing.tools.Activator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.XAxisBoxROI;

public class ImageTrapeziumBaselineReducer implements IDatasetROIReducer, IProvideReducerActions {

	private final RegionType regionType = RegionType.XAXIS;
	private List<IDataset> imageAxes;
	private boolean subtractBaseline = false;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order) {
		if (roi instanceof RectangularROI) {
			IDataset image = ROISliceUtils.getAxisDatasetTrapzSumBaselined(data,axes.get(2).getSlice(),(RectangularROI)roi, slices, order[2],1, subtractBaseline);

			if (order[0] < order[1]) image = DatasetUtils.transpose(image);
			
			this.imageAxes = new ArrayList<IDataset>();
			this.imageAxes.add(axes.get(0).getSlice());
			this.imageAxes.add(axes.get(1).getSlice());
			
			return image;
		}
		
		return null;
	}

	public boolean isSubtractBaseline() {
		return subtractBaseline;
	}

	public void setSubtractBaseline(boolean subtractBaseline) {
		this.subtractBaseline = subtractBaseline;
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
		double min = axes.get(2).getSlice().min().doubleValue();
		double max = axes.get(2).getSlice().max().doubleValue();
		
		return new XAxisBoxROI(min,0,(max-min)/10, 0, 0);
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
	public List<IAction> getActions(IPlottingSystem system) {
		IAction baseline = new Action("Linear baseline", SWT.TOGGLE) {
			@Override
			public void run() {
				setSubtractBaseline(isChecked());
			}
		};
		
		baseline.setImageDescriptor(Activator.getImageDescriptor("icons/LinearBase.png"));
		baseline.setId("org.dawnsci.slicing.tools.hyper.ImageTrapeziumBaselineReducer.baseline");
		
		return Arrays.asList(new IAction[]{baseline});
	}

}
