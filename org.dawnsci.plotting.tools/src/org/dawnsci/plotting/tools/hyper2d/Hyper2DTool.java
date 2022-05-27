/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.hyper2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawnsci.multidimensional.ui.hyper.HyperComponent;
import org.dawnsci.multidimensional.ui.hyper.IDatasetROIReducer;
import org.dawnsci.multidimensional.ui.hyper.IProvideReducerActions;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hyper2DTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(Hyper2DTool.class);

	HyperComponent component;
	ITraceListener traceListener;
	private boolean updating = false;
	
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

		super.createControl(parent);
	}
	
	private void update(IImageTrace im) {
		if (im == null) return;
		removeAllRegions();
		IDataset ds = im.getData();
		int[] shape = ds.getShape();
		List<IDataset> ax = im.getAxes();
		if (ax == null) {
			ax = new ArrayList<IDataset>();
			ax.add(DatasetFactory.createRange(IntegerDataset.class, shape[1]));
			ax.add(DatasetFactory.createRange(IntegerDataset.class, shape[0]));
		} else {
			if (ax.get(0) == null) {
				ax.set(0, DatasetFactory.createRange(IntegerDataset.class, shape[1]));
			}
			
			if (ax.get(1)== null) {
				ax.set(1, DatasetFactory.createRange(IntegerDataset.class, shape[0]));
			}
		}
		
		component.setExternalListeners(getRightROIListener(), getLeftROIListener(), getRightRegionListener(),getLeftRegionListener());
		component.setData(ds, ax, new Slice[2], new int[]{0,1}, new SideReducer(), new MainReducer());
		
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
		removeAllRegions();
		super.deactivate();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		component.dispose();
		
	}

	@Override
	public Control getControl() {
		return component != null ? component.getControl() : null;
	}

	@Override
	public void setFocus() {
		component.setFocus();

	}
	
	private void removeAllRegions() {
		Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions == null) return;
		for (IRegion r : regions) {
			if (r.getName().startsWith(HyperComponent.LEFT_REGION_NAME) || r.getName().startsWith(HyperComponent.RIGHT_REGION_NAME)) getPlottingSystem().removeRegion(r);
		}
	}
	
	private void removeAllRegions(String name) {
		IPlottingSystem<Object> ps = getPlottingSystem();
		if (ps == null) return;
		Collection<IRegion> regions = ps.getRegions();
		for (IRegion re : regions) {
			if (re.getName().startsWith(name)) ps.removeRegion(re);
		}
	}
	
	private void removeRegion(RegionEvent evt) {
		IRegion r = getRegion(evt);
		if (r == null) return;
		Collection<IRegion> regions = getPlottingSystem().getRegions();
		for (IRegion re : regions) {
			if (re.getName().equals(r.getName())) {
				getPlottingSystem().removeRegion(re);
				return;
			}
		}
	}
	
	private void updateColour(RegionEvent evt) {
		IRegion r = getRegion(evt);
		if (r == null) return;
		Collection<IRegion> regions = getPlottingSystem().getRegions();
		for (IRegion re : regions) {
			if (re.getName().equals(r.getName())) re.setRegionColor(r.getRegionColor());
		}
	}
	
	private void updateROI(IROI roi, double x, boolean right,IRegion region, boolean isMainPlotUpdate, boolean origin) {
		if (updating) return;
		updating = true;
		roi = roi.copy();
		try {
			if (!isMainPlotUpdate) { // if update happens on the tool plotting systems
				IImageTrace next = (IImageTrace)getPlottingSystem().getTraces().iterator().next();
				
				double[] coOrd = new double[]{x,Double.NaN};
				
				if (!right) coOrd = new double[]{Double.NaN,x};
				
				double[] im = ((IImageTrace)next).getPointInImageCoordinates(coOrd);
				
				if (!origin) right = !right;
				
				if (right) {
					roi.setPoint(im[0], 0);
				} else {
					roi.setPoint(0, im[1]);
				}

				region.setROI(roi);
			} else { // if update happens on the main plotting system
				roi.setPoint(x, 0);
				region.setROI(roi);
			}
		} catch (Exception e) {
			logger.error("Could not update roi!",e);
		}
		updating = false;
	}
	
	private void updateRoiEvent(ROIEvent evt, boolean right, boolean isMainPlotUpdate){
		IRegion r = (IRegion) evt.getSource();
		if (r == null) return;
		Collection<IRegion> regions = null;
		if (isMainPlotUpdate) { // if update is on the main plotting system
			if (!right) { 
				regions = component.getSideSystem().getRegions();
			} else {
				regions = ((IRegionSystem) component.getAdapter(IPlottingSystem.class)).getRegions();
			}
		} else { // if update is on the tool plotting systems
			regions = getPlottingSystem().getRegions();
		}
		
		for (IRegion re : regions) {
			if (re.getName().equals(r.getName())) {
				double value = 0;
				IImageTrace next = (IImageTrace)getPlottingSystem().getTraces().iterator().next();
				boolean origin = next.getImageOrigin().isOnLeadingDiagonal();

				try {
					if (isMainPlotUpdate && !right){
						value = evt.getROI().getPointY();
						double[] im = ((IImageTrace) next).getPointInAxisCoordinates(new double[] { Double.NaN, value });
						IROI roi = re.getROI();
						updateROI(roi, im[1], right, re, isMainPlotUpdate, origin);
					} else if (isMainPlotUpdate && right){
						value = evt.getROI().getPointX();
						double[] im = ((IImageTrace) next).getPointInAxisCoordinates(new double[] { value, Double.NaN });
						IROI roi = re.getROI();
						updateROI(roi, im[0], right,re, isMainPlotUpdate, origin);
					} else if (!isMainPlotUpdate) {
						value = evt.getROI().getPointX();
						IROI roi = re.getROI();
						if (!origin) right = !right;
						updateROI(roi, value, right,re, isMainPlotUpdate, origin);
					}
				} catch (Exception e) {
					logger.error("Could not update roi!",e);
				}
			}
		}
	}
	
	private IROIListener getLeftROIListener() {
		return new IROIListener.Stub() {
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateRoiEvent(evt, false, false);
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateRoiEvent(evt, false, false);
			}
		};
	}
	
	private IROIListener getRightROIListener() {
		return new IROIListener.Stub() {
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateRoiEvent(evt, true, false);
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateRoiEvent(evt, true, false);
			}
		};
	}
	
	private IROIListener mainPlotLeftRegionListener = new IROIListener.Stub() {
		@Override
		public void roiDragged(ROIEvent evt) {
			updateRoiEvent(evt, false, true);
		}
	};

	private IRegionListener getLeftRegionListener() {
		return new IRegionListener.Stub() {

			@Override
			public void regionsRemoved(RegionEvent evt) {
				removeAllRegions(HyperComponent.RIGHT_REGION_NAME);
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				IRegion source = getRegion(evt);
				IRegion r = getPlottingSystem().getRegion(source.getName());
				if (r != null)
					r.removeROIListener(mainPlotLeftRegionListener);
				removeRegion(evt);
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				IRegion source = getRegion(evt);
				if (source == null) return;
				if (source.getROI() == null) return;
				
				try {

					boolean origin = getImageTrace().getImageOrigin().isOnLeadingDiagonal();
					IRegion r = createRegion(source, origin);
					r.setRegionColor(ColorConstants.blue);
					r.setName(source.getName());
					r.addROIListener(mainPlotLeftRegionListener);
					getPlottingSystem().addRegion(r);
				} catch (Exception e) {
					logger.error("Could not create region",e);
				}
			}
			
			@Override
			public void regionNameChanged(RegionEvent evt, String oldName) {
				updateColour(evt);
			}
		};
	}

	private IRegion createRegion(IRegion source, boolean isY) throws Exception{
		IRegion r = getPlottingSystem().createRegion(source.getName(), isY ? RegionType.YAXIS_LINE : RegionType.XAXIS_LINE);
		IROI roi = isY ? new YAxisBoxROI() : new XAxisBoxROI();
		double x = source.getROI().getPointX();
		
		updateROI(roi,x,!isY,r, false,getImageTrace().getImageOrigin().isOnLeadingDiagonal());
		return r;
	}
	
	private IROIListener mainPlotRightRegionListener = new IROIListener.Stub() {
		@Override
		public void roiDragged(ROIEvent evt) {
			updateRoiEvent(evt, true, true);
		}
	};

	private IRegionListener getRightRegionListener() {
		return new IRegionListener.Stub() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				removeAllRegions(HyperComponent.LEFT_REGION_NAME);
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				IRegion source = getRegion(evt);
				IRegion r = getPlottingSystem().getRegion(source.getName());
				if (r != null)
					r.removeROIListener(mainPlotRightRegionListener);
				removeRegion(evt);
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				
				IRegion source = getRegion(evt);
				if (source == null) return;
				if (source.getROI() == null) return;
				
				try {

					boolean origin = getImageTrace().getImageOrigin().isOnLeadingDiagonal();
					IRegion r = createRegion(source, !origin);
					r.setRegionColor(ColorConstants.blue);
					r.setName(source.getName());
					r.addROIListener(mainPlotRightRegionListener);
					getPlottingSystem().addRegion(r);
				} catch (Exception e) {
					logger.error("Could not create region",e);
				}

			}
			
			@Override
			public void regionNameChanged(RegionEvent evt, String oldName) {
				updateColour(evt);
			}
		};
	}

	private IRegion getRegion(RegionEvent evt) {
		Object source = evt.getSource();
		if (source instanceof IRegion) return (IRegion)source;
		return null;
	}
	
	private class MainReducer implements IDatasetROIReducer, IProvideReducerActions {

		IDataset axis;
		
		@Override
		public boolean isOutput1D() {
			return true;
		}

		@Override
		public IDataset reduce(ILazyDataset data, List<IDataset> axes,
				IROI roi, Slice[] slices, int[] order) throws Exception {
			
			axis = axes.get(0);
			
			double point = roi.getPoint()[0];
			int pos = ROISliceUtils.findPositionOfClosestValueInAxis(axes.get(1), point);
			
			Slice slice = new Slice(pos, pos+1, 1);
		
			IDataset sl = data.getSlice(slice, null).squeeze();
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
		public List<IAction> getActions(final IPlottingSystem<?> system) {
			
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
				IROI roi, Slice[] slices, int[] order) throws Exception {
			axis = axes.get(1);
			
			double point = roi.getPoint()[0];
			
			int pos = ROISliceUtils.findPositionOfClosestValueInAxis(axes.get(0), point);
			
			Slice slice = new Slice(pos, pos+1, 1);
			IDataset sl = data.getSlice((Slice)null,slice).squeeze();
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
		public List<IAction> getActions(final IPlottingSystem<?> system) {
			
			return Arrays.asList(new IAction[]{getAction(system,this,"org.dawnsci.slicing.tools.hyper.hyper2dside.newRegion")});
			
		}
	}
	
	private IAction getAction(final IPlottingSystem<?> system, final IDatasetROIReducer reducer, String id) {
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
	
	private final void createNewRegion(final IPlottingSystem<?> system, IDatasetROIReducer reducer) {
		// Start with a selection of the right type
		String name = HyperComponent.LEFT_REGION_NAME;
		if (reducer instanceof SideReducer) name = HyperComponent.RIGHT_REGION_NAME;
		try {
			system.createRegion(RegionUtils.getUniqueName(name, system),reducer.getSupportedRegionType().get(0));
		} catch (Exception e) {
			
		}
	}
}
