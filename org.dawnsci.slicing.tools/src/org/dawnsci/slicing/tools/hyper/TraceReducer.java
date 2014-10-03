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
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;

public class TraceReducer implements IDatasetROIReducer, IProvideReducerActions {

	private final RegionType regionType = RegionType.BOX;
	private List<IDataset> traceAxes;
	
	@Override
	public IDataset reduce(ILazyDataset data, List<IDataset> axes,
			IROI roi, Slice[] slices, int[] order) {
		if (roi instanceof RectangularROI) {
			
			Dataset output = (Dataset)ROISliceUtils.getDataset(data, (RectangularROI)roi, slices, new int[]{order[0],order[1]}, 1);
			
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
	public IROI getInitialROI(List<IDataset> axes, int[] order) {
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

	@Override
	public List<IAction> getActions(final IPlottingSystem system) {
		final IAction newRegion = new Action("Create new profile", SWT.TOGGLE) {
			@Override
			public void run() {
				if (isChecked()) {
					createNewRegion(system);
				} else {
					IContributionItem item = system.getActionBars().getToolBarManager().find("org.csstudio.swt.xygraph.undo.ZoomType.NONE");
					if (item != null && item instanceof ActionContributionItem) {
						((ActionContributionItem)item).getAction().run();
					}
				}
			}
		};
		
		system.addRegionListener(new IRegionListener.Stub() {
	
			@Override
			public void regionAdded(RegionEvent evt) {
				newRegion.run();
			}
		});
		
		newRegion.setImageDescriptor(Activator.getImageDescriptor("icons/ProfileBox2.png"));
		newRegion.setId("org.dawnsci.slicing.tools.hyper.TraceReducer.newRegion");
		return Arrays.asList(new IAction[]{newRegion});
	}
	
	protected final void createNewRegion(final IPlottingSystem system) {
		// Start with a selection of the right type
		try {
			system.createRegion(RegionUtils.getUniqueName("Image Region", system),getSupportedRegionType().get(0));
		} catch (Exception e) {
			
		}
	}

}
