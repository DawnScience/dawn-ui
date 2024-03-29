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
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;

public class ArpesMainImageReducer implements IDatasetROIReducer {
	
	private final RegionType regionType = RegionType.LINE;
	private List<IDataset> imageAxes;
	
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order) throws Exception {
		
		if (roi instanceof LinearROI) {
			
			final IDataset roiSlice = ROISliceUtils.getDataset(data, (LinearROI)roi, slices,new int[]{order[0],order[1]},1, null);
			final IDataset image    = DatasetUtils.transpose(roiSlice); 
		
			IDataset length = DatasetFactory.createRange(IntegerDataset.class, image.getShape()[1]);
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
