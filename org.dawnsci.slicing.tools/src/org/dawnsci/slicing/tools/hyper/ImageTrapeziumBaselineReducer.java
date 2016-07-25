/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.slicing.tools.Activator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;

public class ImageTrapeziumBaselineReducer implements IDatasetROIReducer, IProvideReducerActions {

	private final RegionType regionType = RegionType.XAXIS;
	private List<IDataset> imageAxes;
	private boolean subtractBaseline = false;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order, IMonitor monitor) throws Exception {

		if (monitor.isCancelled()) return null;
		if (roi instanceof RectangularROI) {
			IDataset image = ROISliceUtils.getAxisDatasetTrapzSumBaselined(data,axes.get(2).getSlice(),(RectangularROI)roi, slices, order[2],1, subtractBaseline, monitor);
			if (monitor.isCancelled()) return null;

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
	public List<IAction> getActions(IPlottingSystem<?> system) {
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
