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
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display a 3D dataset across two plots with ROI slicing
 */
public class HyperComponent { 
	
	private IPlottingSystem<Composite> mainSystem;
	private IPlottingSystem<Composite> sideSystem;
	private IRegionListener regionListenerLeft;
	private IRegionListener regionListenerRight;
	private IRegionListener externalRegionListenerLeft;
	private IRegionListener externalRegionListenerRight;
	private IROIListener roiListenerLeft;
	private IROIListener roiListenerRight;
	private IROIListener externalROIListenerLeft;
	private IROIListener externalROIListenerRight;
	private HyperDelegateJob leftJob;
	private HyperDelegateJob rightJob;
	private Composite mainComposite;
	private IWorkbenchPart part;
	private SashForm sashForm;
	private final static Logger logger = LoggerFactory.getLogger(HyperComponent.class);
	private List<IAction> leftActions;
	private List<IAction> rightActions;
	
	private static final String SS1 = "uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.reducerGroup1";
	private static final String SS2 = "uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.reducerGroup2";
	private static final String STARTGROUP = "/org.dawb.common.ui.plot.groupAll";
	private static final String HYPERIMAGE = "HyperImage";
	private static final String HYPERTRACE = "HyperTrace";
	
	public static final String LEFT_REGION_NAME = "Left Region";
	public static final String RIGHT_REGION_NAME = "Right Region";

	// use to override the default value of left roi
	private IROI myRoi;

	// default constructor
	public HyperComponent() {
	}

	public HyperComponent(IWorkbenchPart part) {
		this.part = part;
	}

	public void createControl(Composite parent) {
		this.sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		Color bgdColour = parent.getBackground();
		sashForm.setBackground(bgdColour != null ? bgdColour : new Color(parent.getDisplay(), 255, 255, 255));
		
		createPlottingSystems(sashForm);
		
		leftActions = new ArrayList<IAction>();
		rightActions = new ArrayList<IAction>();
	}
	
    public Control getControl() {
    	return sashForm;
    }
    
    public void updateData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order) {
    	leftJob.cancel();
    	rightJob.cancel();
    	leftJob.updateData(lazy, daxes, slices, order);
    	rightJob.updateData(lazy, daxes, slices, order);
    	leftJob.schedule();
    	rightJob.schedule();
    	
    }
	
	public void setData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order) {
		this.setData(lazy, daxes, slices, order, new TraceReducer(), new ImageTrapeziumBaselineReducer());
	}
	
	public void setExternalListeners(IROIListener roiLeft, IROIListener roiRight, IRegionListener regionLeft, IRegionListener regionRight) {
		if (externalRegionListenerLeft != null) sideSystem.removeRegionListener(externalRegionListenerLeft);
		if (externalRegionListenerRight != null) mainSystem.removeRegionListener(externalRegionListenerRight);
		
		externalRegionListenerLeft = regionLeft;
		externalRegionListenerRight = regionRight;
		
		if (regionLeft != null) mainSystem.addRegionListener(regionLeft);
		if (regionRight != null) sideSystem.addRegionListener(regionRight);
		
		externalROIListenerLeft = roiLeft;
		externalROIListenerRight = roiRight;
		
	}
	
	public void setData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order,
			IDatasetROIReducer mainReducer, IDatasetROIReducer sideReducer) {
		//FIXME needs to be made more generic
		this.leftJob = new HyperDelegateJob("Left update",
				sideSystem,
				lazy,
				daxes,
				slices, order, mainReducer);
		
		this.rightJob = new HyperDelegateJob("Right update",
				mainSystem,
				lazy,
				daxes,
				slices,
				order, sideReducer);
		
		cleanUpActions(sideSystem,rightActions);
		cleanUpActions(mainSystem, leftActions);
		
		if (rightJob.getReducer() instanceof IProvideReducerActions) {
			createActions((IProvideReducerActions)rightJob.getReducer(), sideSystem, rightActions,roiListenerRight,HYPERTRACE);
		}
		if (leftJob.getReducer() instanceof IProvideReducerActions) {
			createActions((IProvideReducerActions)leftJob.getReducer(), mainSystem, leftActions,roiListenerLeft,HYPERIMAGE);
		}

		IROI rroi = myRoi != null ? myRoi : mainReducer.getInitialROI(daxes,order);
		IROI broi = sideReducer.getInitialROI(daxes,order);
		
		mainSystem.clear();
		mainSystem.getAxes().clear();
		
		int axisCount = 0;
		
		if (mainReducer.isOutput1D()) {
			axisCount++;
		} else {
			List<IDataset> ax2d = new ArrayList<IDataset>();
			ax2d.add(daxes.get(axisCount++));
			ax2d.add(daxes.get(axisCount++));
			mainSystem.createPlot2D(DatasetFactory.zeros(new int[] {ax2d.get(0).getSize(), ax2d.get(1).getSize()}, Dataset.INT16), ax2d, null);
		}
		
		for (IRegion region : mainSystem.getRegions()) {
			mainSystem.removeRegion(region);
		}
		
		sideSystem.clear();
		sideSystem.getAxes().clear();
		
		if (mainReducer.isOutput1D()) {
			List<IDataset> xd = new ArrayList<IDataset>();
			IDataset axis = daxes.get(axisCount++);
			xd.add(DatasetFactory.zeros(new int[] {(int)axis.getSize()},Dataset.INT16));
			
			sideSystem.createPlot1D(axis,xd, null);
		} else {
			List<IDataset> xd = new ArrayList<IDataset>();
			xd.add(DatasetFactory.createRange(10, Dataset.INT32));
			xd.add(daxes.get(axisCount++));
			sideSystem.createPlot2D(DatasetFactory.ones(new int[] {10,(int)xd.get(1).getSize()}, Dataset.INT32), xd, null);
		}
		
		for (IRegion region : sideSystem.getRegions()) {
			sideSystem.removeRegion(region);
		}
		
		try {
			IRegion region = mainSystem.createRegion(LEFT_REGION_NAME, mainReducer.getSupportedRegionType().get(0));
			region.setROI(rroi);
			mainSystem.addRegion(region);
			
			
			region.setUserRegion(false);
			region.addROIListener(this.roiListenerLeft);
			sideSystem.clear();
			updateRight(region, rroi);
			
			IRegion windowRegion = sideSystem.createRegion(RIGHT_REGION_NAME, sideReducer.getSupportedRegionType().get(0));
			windowRegion.setRegionColor(ColorConstants.blue);
			windowRegion.setROI(broi);
			
			windowRegion.addROIListener(this.roiListenerRight);
			sideSystem.addRegion(windowRegion);
			windowRegion.setUserRegion(false);
			updateLeft(windowRegion,broi);
			
		} catch (Exception e) {
			logger.error("Error adding regions to hyperview: " + e.getMessage());
		}
	}

	public void setMyRoi(IROI myRoi) {
		this.myRoi = myRoi;
	}

	public void setFocus() {
		mainComposite.setFocus();
	}
	
	public void dispose() {
		
		if (mainSystem != null && !mainSystem.isDisposed()) mainSystem.dispose();
		if (sideSystem != null && !sideSystem.isDisposed()) sideSystem.dispose();
		
		if (leftJob != null) leftJob.cancel();
		if (rightJob != null) rightJob.cancel();
		
	}
	
	private void createPlottingSystems(SashForm sashForm) {
		try {
			mainSystem = PlottingFactory.createPlottingSystem();
			mainSystem.setColorOption(ColorOption.NONE);
			mainComposite = new Composite(sashForm, SWT.NONE);
			mainComposite.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(mainComposite);

			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(mainComposite, null);

			Composite displayPlotComp  = new Composite(mainComposite, SWT.BORDER);
			displayPlotComp.setLayout(new FillLayout());
			displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			mainSystem.createPlotPart(displayPlotComp, 
													HYPERIMAGE, 
													 actionBarWrapper, 
													 PlotType.IMAGE, 
													 part);
			
			mainSystem.repaint();
			
			sideSystem = PlottingFactory.createPlottingSystem();
			sideSystem.setColorOption(ColorOption.NONE);
			Composite sideComp = new Composite(sashForm, SWT.NONE);
			sideComp.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(sideComp);
			ActionBarWrapper actionBarWrapper1 = ActionBarWrapper.createActionBars(sideComp, null);
			Composite sidePlotComp  = new Composite(sideComp, SWT.BORDER);
			sidePlotComp.setLayout(new FillLayout());
			sidePlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			sideSystem.createPlotPart(sidePlotComp, 
													HYPERTRACE, 
													 actionBarWrapper1, 
													 PlotType.XY, 
													 null);
			
			regionListenerLeft = getRegionListenerToLeft();
			mainSystem.addRegionListener(regionListenerLeft);
			regionListenerRight = getRegionListenerToRight();
			sideSystem.addRegionListener(regionListenerRight);
			
			roiListenerLeft = getROIListenerToRight();
			roiListenerRight = getROIListenerLeft();
			
			mainSystem.getActionBars().getToolBarManager().remove(ToolPageRole.ROLE_1D.getId());
			mainSystem.getActionBars().getToolBarManager().remove(ToolPageRole.ROLE_2D.getId());
			mainSystem.getActionBars().getToolBarManager().update(true);

			sideSystem.getActionBars().getToolBarManager().remove(ToolPageRole.ROLE_1D.getId());
			sideSystem.getActionBars().getToolBarManager().remove(ToolPageRole.ROLE_2D.getId());
			sideSystem.getActionBars().getToolBarManager().update(true);
		} catch (Exception e) {
			logger.error("Error creating hyperview plotting systems: " + e.getMessage());
		}
	}

	public IPlottingSystem<?> getSideSystem() {
		return sideSystem;
	}

    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		
		if (clazz == Page.class) {
			// TODO Page for helping with part
		} else if (clazz == IToolPageSystem.class || clazz == IPlottingSystem.class) {
			return mainSystem;
		}
		return null;
	}
    
    private void cleanUpActions(IPlottingSystem<Composite> system, List<IAction> cached) {
    	IActionBars actionBars = system.getActionBars();
    	IToolBarManager toolBarManager = actionBars.getToolBarManager();
		IMenuManager menuManager = actionBars.getMenuManager();
		
		
		for (IAction action : cached) {
			toolBarManager.remove(action.getId());
			menuManager.remove(action.getId());
		}

		toolBarManager.remove(SS1);
		toolBarManager.remove(SS2);
		menuManager.remove(SS1);
		menuManager.remove(SS2);
		cached.clear();
		toolBarManager.update(true);
    }
    
    private void createActions(IProvideReducerActions provider, IPlottingSystem<Composite> system, List<IAction> cached, IROIListener listener, String barName) {
    	
    	IActionBars actionBars = system.getActionBars();
    	IToolBarManager toolBarManager = actionBars.getToolBarManager();
		IMenuManager menuManager = actionBars.getMenuManager();
		List<IAction> actions = provider.getActions(system);
		
		IContributionItem s1 = new Separator(SS1);
		
		toolBarManager.insertBefore(barName+STARTGROUP,s1);
		menuManager.add(s1);

		for (IAction action : actions) {
			toolBarManager.insertBefore(barName+STARTGROUP, action);
			menuManager.add(action);
			cached.add(action);
		}
		IContributionItem s2 = new Separator(SS2);
		toolBarManager.insertBefore(barName+STARTGROUP,s2);
		menuManager.add(s2);
		toolBarManager.update(true);
    }
    

	private IRegionListener getRegionListenerToLeft() {
		return new IRegionListener.Stub() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				
				for(ITrace trace : sideSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() instanceof IRegion) {
						if (((IRegion)trace.getUserObject()).isUserRegion()) {
							sideSystem.removeTrace(trace);
						}
					}
				}
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				
				for(ITrace trace : sideSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() == evt.getSource()) {
						sideSystem.removeTrace(trace);
					}
				}
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion() != null) {
					evt.getRegion().setUserRegion(true);
					evt.getRegion().addROIListener(roiListenerLeft);
					if (externalROIListenerLeft != null) evt.getRegion().addROIListener(externalROIListenerLeft);
					updateRight((IRegion)evt.getSource(),((IRegion)evt.getSource()).getROI());
				}
				
			}
		};
	}
	
	private IRegionListener getRegionListenerToRight() {
		return new IRegionListener.Stub() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				
				for(ITrace trace : mainSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() instanceof IRegion) {
						if (((IRegion)trace.getUserObject()).isUserRegion()) {
							mainSystem.removeTrace(trace);
						}
					}
				}
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				
				for(ITrace trace : mainSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() == evt.getSource()) {
						mainSystem.removeTrace(trace);
					}
				}
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion() != null) {
					evt.getRegion().setUserRegion(true);
					evt.getRegion().addROIListener(roiListenerRight);
					if (externalROIListenerRight != null) evt.getRegion().addROIListener(externalROIListenerRight);
					updateLeft((IRegion)evt.getSource(),((IRegion)evt.getSource()).getROI());
				}
				
			}
		};
	}
	
	private IROIListener getROIListenerToRight() {

		return new IROIListener.Stub() {
						
			@Override
			public void roiDragged(ROIEvent evt) {
				updateRight((IRegion)evt.getSource(), evt.getROI());
				updateOnLeftRegionChange(evt.getROI());
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateRight((IRegion)evt.getSource(), evt.getROI());
				updateOnLeftRegionChange(evt.getROI());
			}
		};
	}

	/**
	 * Method to override if something needs to be updated on a left region change
	 */
	public void updateOnLeftRegionChange(IROI roi) {
	}

	private IROIListener getROIListenerLeft() {
		return new IROIListener.Stub() {
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateLeft((IRegion)evt.getSource(),evt.getROI());
				updateOnRightRegionChange(evt.getROI());
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateLeft((IRegion)evt.getSource(),evt.getROI());
				updateOnRightRegionChange(evt.getROI());
			}
		};
	}

	/**
	 * Method to override if something needs to be updated on a right region change
	 */
	public void updateOnRightRegionChange(IROI roi) {
	}

	protected void updateRight(IRegion r, IROI rb) {
		
		leftJob.profile(r, rb);
	}
	
	protected void updateLeft(IRegion r, IROI rb) {

		rightJob.profile(r,rb);
	}
	
	private void updateImage(final IPlottingSystem<Composite> plot, final IDataset image, final List<IDataset> axes) {
		
		plot.updatePlot2D(image, axes, null);
		
	}
	
	private void updateTrace(final IPlottingSystem<Composite> plot, final IDataset axis, final IDataset data, final boolean update, final IRegion region) {

		if (update) {
			plot.updatePlot1D(axis,Arrays.asList(new IDataset[] {data}), null);
			plot.repaint();	
		} else {
			final List<ITrace> traceOut = plot.createPlot1D(axis,Arrays.asList(new IDataset[] {data}), null);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					for (ITrace trace : traceOut) {
						trace.setUserObject(region);
						if (trace instanceof ILineTrace){
							region.setRegionColor(((ILineTrace)trace).getTraceColor());
							//use name listener to update color
							String name = region.getName();
							plot.renameRegion(region, name + " Color");
							plot.renameRegion(region, name);
						}
					}
				}
			});
		}
	}
	
	private void updateTrace(final IPlottingSystem<Composite> plot, final IDataset axis, final IDataset data) {

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				Collection<ITrace> traces = plot.getTraces(ILineTrace.class);

				List<IDataset> datasets = convertFrom2DToListOf1D(axis, data);

				if (traces == null || traces.isEmpty()) {
					plot.createPlot1D(axis, datasets, null);
					return;
				}
				int i = 0;
				for (ITrace trace : traces) {
					if (i < datasets.size()) ((ILineTrace)trace).setData(axis, datasets.get(i));
					else plot.removeTrace(trace);
					i++;
				}

				if (i >= datasets.size()) {
					plot.repaint();
					return;
				}

				List<IDataset> subdatasets = new ArrayList<IDataset>(datasets.size() - i);

				for (; i < datasets.size(); ++i) {
					subdatasets.add(datasets.get(i));
				}

				plot.createPlot1D(axis, subdatasets, null);
				plot.repaint();
			}
		});
	}
	
	private List<IDataset> convertFrom2DToListOf1D(IDataset axis, IDataset data) {
		
		int[] dataShape = data.getShape();
		
		List<IDataset> datasets = new ArrayList<IDataset>(dataShape[0]);
		Slice[] slices = new Slice[2];
		slices[0] = new Slice(0,1,1);
		
		for (int i = 0; i < dataShape[0]; i++) {
			slices[0].setStart(i);
			slices[0].setStop(i+1);
			
			IDataset out = data.getSlice(slices);
			
			out.setName("trace_" + i);
			
			datasets.add(out.squeeze());
		}
		
		return datasets;
		
	}

	private class HyperDelegateJob extends Job {
		
		private IRegion currentRegion;
		private IROI currentROI;
		private IPlottingSystem<Composite> plot;
		private ILazyDataset data;
		private List<IDataset> axes;
		private int[] order;
		private Slice[] slices;
		private IDatasetROIReducer reducer;
		
		
		public HyperDelegateJob(String name,
				IPlottingSystem<Composite> plot,
				ILazyDataset data,
				List<IDataset> axes,
				Slice[] slices,
				int[] order,
				IDatasetROIReducer reducer) {
			
			super(name);
			this.plot = plot;
			this.data = data;
			this.axes = axes;
			this.order = order;
			this.slices = slices;
			this.reducer = reducer;
			setSystem(false);
			setUser(false);
		}
		
		public void profile(IRegion r, IROI rb) {
			
			cancel(); // Needed for large datasets but makes small ones look less responsive.
			this.currentRegion = r;
			this.currentROI    = rb;
	        
          	schedule();		
		}
		
		public void updateData(ILazyDataset data,
				List<IDataset> axes,
				Slice[] slices,
				int[] order){
			this.data = data;
			this.axes = axes;
			this.order = order;
			this.slices = slices;
		}
		
		public IDatasetROIReducer getReducer() {
			return reducer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				
				IDataset output = this.reducer.reduce(data, axes, currentROI, slices, order, new ProgressMonitorWrapper(monitor));
				if (output==null) return Status.CANCEL_STATUS;
				
				List<IDataset> outputAxes = this.reducer.getAxes();

				if (!this.reducer.isOutput1D()) {
					output.setName("Image");
					updateImage(plot,output,outputAxes);
				} else {
					
					IDataset axis = null;
					
					if (outputAxes != null && !outputAxes.isEmpty()) {
						axis = outputAxes.get(0);
					}
					
					if (output.getRank() == 1) {
						Collection<ITrace> traces = plot.getTraces();
						for (ITrace trace : traces) {
							Object uo = trace.getUserObject();
							if (uo == currentRegion) {
								output.setName(trace.getName());
								updateTrace(plot,axis,output,true,currentRegion);
								return Status.OK_STATUS;
							}
						}

						String name = TraceUtils.getUniqueTrace("trace", plot, (String[])null);
						output.setName(name);
						
						updateTrace(plot,axis,output,false,currentRegion);
					} else {
						updateTrace(plot,axis,output);
					}
					
				}

				return Status.OK_STATUS;
			} catch (Throwable ne) {
				return Status.CANCEL_STATUS;
			}
		}
	}
}
