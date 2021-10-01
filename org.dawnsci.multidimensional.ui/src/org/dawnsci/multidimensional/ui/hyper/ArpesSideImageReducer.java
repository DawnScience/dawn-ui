/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.multidimensional.ui.hyper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;

public class ArpesSideImageReducer implements IDatasetROIReducer {
	
	private final RegionType regionType = RegionType.YAXIS;
	private List<IDataset> imageAxes;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order, IMonitor monitor)  throws Exception{
		
		if (monitor.isCancelled()) return null;
		if (roi instanceof RectangularROI) {
			IDataset image = ROISliceUtils.getYAxisDataset2DAverage(data, (RectangularROI)roi, slices, order[2], monitor);
			
			if (order[0] < order[1]) image = DatasetUtils.transpose(image);
			
			this.imageAxes = new ArrayList<IDataset>();
			this.imageAxes.add(axes.get(0).getSlice());
			this.imageAxes.add(axes.get(1).getSlice());
			
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
		double len = axes.get(2).getSize();
		
		return new YAxisBoxROI(0,len/10,0,len/20, 0);
	}
	
	@Override
	public boolean createROI() {
		return false;
	}
	
	
}

