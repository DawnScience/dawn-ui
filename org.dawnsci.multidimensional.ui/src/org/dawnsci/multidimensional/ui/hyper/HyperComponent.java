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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dawnsci.plotting.actions.ActionBarWrapper;
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
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShortDataset;
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
	
	private Executor leftJobExecutor = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1),
			new ThreadPoolExecutor.DiscardOldestPolicy());
	private Executor rightJobExecutor = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1),
			new ThreadPoolExecutor.DiscardOldestPolicy());
	private IWorkbenchPart part;
	private SashForm sashForm;
	private static final Logger logger = LoggerFactory.getLogger(HyperComponent.class);
	private List<IAction> leftActions;
	private List<IAction> rightActions;
	
	private boolean invertYAxis = true;
	
	private static final String SS1 = "uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.reducerGroup1";
	private static final String SS2 = "uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.reducerGroup2";
	private static final String STARTGROUP = "/org.dawb.common.ui.plot.groupAll";
	private static final String HYPERIMAGE = "HyperImage";
	private static final String HYPERTRACE = "HyperTrace";
	
	public static final String LEFT_REGION_NAME = "Left Region";
	public static final String RIGHT_REGION_NAME = "Right Region";
	
	private HyperRunnableFactory leftFactory;
	private HyperRunnableFactory rightFactory;


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
    	
    	HyperBean b = new HyperBean(lazy, daxes, slices, order);
    	leftFactory.updateData(b);
    	rightFactory.updateData(b);
    	
    	Collection<IRegion> lregions = sideSystem.getRegions();
    	
    	for (IRegion l : lregions) {
    		updateLeft(l, l.getROI());
    	}
    	
    	Collection<IRegion> rregions = mainSystem.getRegions();
    	
    	for (IRegion r : rregions) {
    		updateRight(r, r.getROI());
    	}

    	
    }
	
	public void setData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order) {
		this.setData(lazy, daxes, slices, order, new TraceReducer(), new ImageTrapeziumBaselineReducer());
	}
	
	public void setExternalListeners(IROIListener roiLeft, IROIListener roiRight, IRegionListener regionLeft, IRegionListener regionRight) {
		if (externalRegionListenerLeft != null) mainSystem.removeRegionListener(externalRegionListenerLeft);
		if (externalRegionListenerRight != null) sideSystem.removeRegionListener(externalRegionListenerRight);
		
		externalRegionListenerLeft = regionLeft;
		externalRegionListenerRight = regionRight;
		
		if (regionLeft != null) mainSystem.addRegionListener(regionLeft);
		if (regionRight != null) sideSystem.addRegionListener(regionRight);
		
		externalROIListenerLeft = roiLeft;
		externalROIListenerRight = roiRight;
		
	}
	
	public void setData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order,
			IDatasetROIReducer mainReducer, IDatasetROIReducer sideReducer) {
		
		HyperBean b = new HyperBean(lazy, daxes, slices, order);
		this.leftFactory = new HyperRunnableFactory(sideSystem, b, mainReducer);
		this.rightFactory = new HyperRunnableFactory(mainSystem, b, sideReducer);
		
		cleanUpActions(sideSystem,rightActions);
		cleanUpActions(mainSystem, leftActions);
		
		if (sideReducer instanceof IProvideReducerActions) {
			createActions((IProvideReducerActions)sideReducer, sideSystem, rightActions,roiListenerRight,HYPERTRACE);
		}
		if (mainReducer instanceof IProvideReducerActions) {
			createActions((IProvideReducerActions)mainReducer, mainSystem, leftActions,roiListenerLeft,HYPERIMAGE);
		}
		
		mainSystem.clear();
		mainSystem.getAxes().clear();
		
		int axisCount = 0;
		
		if (mainReducer.isOutput1D()) {
			axisCount++;
		} else {
			List<IDataset> ax2d = new ArrayList<IDataset>();
			ax2d.add(daxes.get(axisCount++));
			ax2d.add(daxes.get(axisCount++));
			mainSystem.createPlot2D(DatasetFactory.zeros(ShortDataset.class, ax2d.get(0).getSize(), ax2d.get(1).getSize()), ax2d, null);
		}
		
		for (IRegion region : mainSystem.getRegions()) {
			mainSystem.removeRegion(region);
		}
		
		sideSystem.clear();
		sideSystem.getAxes().clear();
		
		boolean createSideROI = sideReducer.createROI();
		
		if (createSideROI) {
			for (IRegion region : sideSystem.getRegions()) {
				sideSystem.removeRegion(region);
			}
		}
		
		IROI rroi = myRoi != null ? myRoi : mainReducer.getInitialROI(daxes,order);
		
		try {
			IRegion region = mainSystem.createRegion(LEFT_REGION_NAME, mainReducer.getSupportedRegionType().get(0));
			region.setROI(rroi);
			mainSystem.addRegion(region);
			
			
			region.setUserRegion(false);
			region.addROIListener(this.roiListenerLeft);
			sideSystem.clear();
			
			IRegion windowRegion = null;
			if (createSideROI) {
				windowRegion = sideSystem.createRegion(RIGHT_REGION_NAME, sideReducer.getSupportedRegionType().get(0));
				windowRegion.setRegionColor(ColorConstants.blue);
				IROI broi = sideReducer.getInitialROI(daxes,order);
				windowRegion.setROI(broi);
				sideSystem.addRegion(windowRegion);
				windowRegion.setUserRegion(false);
			} 
		} catch (Exception e) {
			logger.error("Error adding regions to hyperview: " + e.getMessage());
		}
	}

	public void setMyRoi(IROI myRoi) {
		this.myRoi = myRoi;
	}
	
	public void clear() {
		mainSystem.reset();
		sideSystem.reset();
	}

	public void setFocus() {
		if (mainSystem != null) {
			mainSystem.setFocus();
		}
	}
	
	public void dispose() {

		if (mainSystem != null) {
			mainSystem.dispose();
		}
		if (sideSystem != null) {
			sideSystem.dispose();
		}
		
	}
	
	private void createPlottingSystems(SashForm sashForm) {
		try {
			mainSystem = PlottingFactory.createPlottingSystem();
			mainSystem.setColorOption(ColorOption.NONE);
			Composite mainComposite = new Composite(sashForm, SWT.NONE);
			mainComposite.setLayout(new GridLayout(1, false));

			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(mainComposite, null);
			actionBarWrapper.getToolbarControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

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

			ActionBarWrapper actionBarWrapper1 = ActionBarWrapper.createActionBars(sideComp, null);
			actionBarWrapper1.getToolbarControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
	
	public IPlottingSystem<?> getMainSystem() {
		return mainSystem;
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
		if (leftFactory == null) return;
		if (!leftFactory.isSupported(r.getRegionType())) {
			return;
		}
		Runnable run = leftFactory.createRunnable(r, rb, invertYAxis);
		leftJobExecutor.execute(run);
	}
	
	protected void updateLeft(IRegion r, IROI rb) {
		
		if (rightFactory == null) return;
		if (!rightFactory.isSupported(r.getRegionType())) {
			return;
		}
		Runnable run = rightFactory.createRunnable(r, rb, invertYAxis);
		rightJobExecutor.execute(run);
	}
	
	public IDataset getLeftData() {
		return MetadataPlotUtils.getDataFromImageTrace(mainSystem);
	}
	
	public void setInvertYAxis(boolean invertYAxis) {
		this.invertYAxis = invertYAxis;
	}

}
