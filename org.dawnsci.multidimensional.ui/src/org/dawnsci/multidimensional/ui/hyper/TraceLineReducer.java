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
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;

public class TraceLineReducer implements IDatasetROIReducer {

	private final RegionType regionType = RegionType.LINE;
	private List<IDataset> traceAxes;
	
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order, IMonitor monitor) throws Exception {
		
		if (monitor.isCancelled()) return null;
		if (roi instanceof LinearROI) {
			final IDataset image = ROISliceUtils.getDataset(data, (LinearROI)roi, slices,new int[]{order[0],order[1]},1, monitor);
			if (monitor.isCancelled()) return null;
			
			IDataset length = DatasetFactory.createRange(IntegerDataset.class, image.getShape()[1]);
			length.setName("Line Length");
			
			this.traceAxes = new ArrayList<IDataset>();
			this.traceAxes.add(axes.get(2).getSlice());
			
			return image;
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
		return traceAxes;
	}
}
