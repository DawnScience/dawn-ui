package org.dawnsci.multidimensional.ui.imagecuts;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.IDataset;

public class AdditionalCutDimension {

	private IRegion region;
	private IDataset axis;
	
	public AdditionalCutDimension(IRegion region, IDataset axis) {
		this.region = region;
		this.axis = axis;
	}
	
	public RectangularROI getRoi() {
		return (RectangularROI)region.getROI();
	}
	
	public IDataset getAxis() {
		return axis;
	}
	
	public IRegion getRegion() {
		return region;
	}
}
