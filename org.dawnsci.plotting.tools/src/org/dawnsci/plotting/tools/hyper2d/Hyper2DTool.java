/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.hyper2d;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.EventTrackerServiceLoader;
import org.dawnsci.slicing.tools.hyper.HyperComponent;
import org.dawnsci.slicing.tools.hyper.IDatasetROIReducer;
import org.dawnsci.slicing.tools.hyper.IProvideReducerActions;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Hyper2DTool extends AbstractToolPage {

	HyperComponent component;
	ITraceListener traceListener;
	
	public Hyper2DTool() {
		this.traceListener = new ITraceListener.Stub() {
	
			@Override
			public void traceAdded(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) Hyper2DTool.this.update((IImageTrace)evt.getSource());
			}
			
			@Override
			public void traceUpdated(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) Hyper2DTool.this.update((IImageTrace)evt.getSource());
			}
		};
	}
	
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		component = new HyperComponent(getPart());
		component.createControl(parent);
		
		IImageTrace im = getImageTrace();
		
		update(im);

		// track Tool launch with tool name
		EventTracker tracker = EventTrackerServiceLoader.getService();
		try {
			if (tracker != null)
				tracker.track(getTitle());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void update(IImageTrace im) {
		if (im == null) return;
		
		IDataset ds = im.getData();
		List<IDataset> ax = im.getAxes();
		
		component.setData(ds, ax, new Slice[2], new int[]{0,1}, new MainReducer(), new SideReducer());
	}
	
	@Override
	public void activate() {
		
		if (isActive()) return;
		super.activate();
		getPlottingSystem().addTraceListener(traceListener);
		
	}
	
	@Override
	public void deactivate() {
		getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
	}
	
	@Override
	public void dispose() {
		component.dispose();
		super.dispose();
	}

	@Override
	public Control getControl() {
		return component != null ? component.getControl() : null;
	}

	@Override
	public void setFocus() {
		component.setFocus();

	}

	private class MainReducer implements IDatasetROIReducer, IProvideReducerActions {

		IDataset axis;
		
		@Override
		public boolean isOutput1D() {
			return true;
		}

		@Override
		public IDataset reduce(ILazyDataset data, List<IDataset> axes,
				IROI roi, Slice[] slices, int[] order, IMonitor monitor) throws Exception {
			
			axis = axes.get(0);
			
			
			if (monitor.isCancelled()) return null;
			double point = roi.getPoint()[0];
			int pos = ROISliceUtils.findPositionOfClosestValueInAxis(axes.get(1), point);
			if (monitor.isCancelled()) return null;
			
			Slice slice = new Slice(pos, pos+1, 1);
		
			IDataset sl = data.getSlice(monitor, slice, null).squeeze();
			return sl;
		}

		@Override
		public List<RegionType> getSupportedRegionType() {
			return Arrays.asList(new RegionType[]{RegionType.XAXIS_LINE});
		}

		@Override
		public IROI getInitialROI(List<IDataset> axes, int[] order) {
			double min = axes.get(1).getSlice().min().doubleValue();
			double max = axes.get(1).getSlice().max().doubleValue();

			return new XAxisBoxROI(min+((max-min)/10),0,0,0, 0);
		}

		@Override
		public boolean supportsMultipleRegions() {
			return true;
		}

		@Override
		public List<IDataset> getAxes() {
			return Arrays.asList(new IDataset[]{axis});
		}
		
		@Override
		public List<IAction> getActions(final IPlottingSystem system) {
			
			return Arrays.asList(new IAction[]{getAction(system,this,"org.dawnsci.slicing.tools.hyper.hyper2dmain.newRegion")});
			
		}
		
	}
	
	private class SideReducer implements IDatasetROIReducer, IProvideReducerActions {

		IDataset axis;
		
		@Override
		public boolean isOutput1D() {
			return true;
		}

		@Override
		public IDataset reduce(ILazyDataset data, List<IDataset> axes,
				IROI roi, Slice[] slices, int[] order, IMonitor monitor) throws Exception {
			
			axis = axes.get(1);
			
			double point = roi.getPoint()[0];
			
			if (monitor.isCancelled()) return null;
			int pos = ROISliceUtils.findPositionOfClosestValueInAxis(axes.get(0), point);
			if (monitor.isCancelled()) return null;
			
			Slice slice = new Slice(pos, pos+1, 1);
			if (monitor.isCancelled()) return null;
			IDataset sl = data.getSlice(monitor, null, slice).squeeze();
			return sl;
		}

		@Override
		public List<RegionType> getSupportedRegionType() {
			return Arrays.asList(new RegionType[]{RegionType.XAXIS_LINE});
		}

		@Override
		public IROI getInitialROI(List<IDataset> axes, int[] order) {
			double min = axes.get(0).getSlice().min().doubleValue();
			double max = axes.get(0).getSlice().max().doubleValue();

			return new XAxisBoxROI(min+((max-min)/10),0,0,0, 0);
		}

		@Override
		public boolean supportsMultipleRegions() {
			return true;
		}

		@Override
		public List<IDataset> getAxes() {
			return Arrays.asList(new IDataset[]{axis});
		}
		
		@Override
		public List<IAction> getActions(final IPlottingSystem system) {
			
			return Arrays.asList(new IAction[]{getAction(system,this,"org.dawnsci.slicing.tools.hyper.hyper2dside.newRegion")});
			
		}
	}
	
	private IAction getAction(final IPlottingSystem system, final IDatasetROIReducer reducer, String id) {
		final IAction newRegion = new Action("Create new profile", SWT.TOGGLE) {
			@Override
			public void run() {
				if (isChecked()) {
					createNewRegion(system, reducer);
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
		
		newRegion.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-line-profile.png"));
		newRegion.setId(id);
		return newRegion;
	}
	
	private final void createNewRegion(final IPlottingSystem system, IDatasetROIReducer reducer) {
		// Start with a selection of the right type
		try {
			system.createRegion(RegionUtils.getUniqueName("Image Region", system),reducer.getSupportedRegionType().get(0));
		} catch (Exception e) {
			
		}
	}
}
