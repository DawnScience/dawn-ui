/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProfileTool extends AbstractToolPage  implements IROIListener, IDataReductionToolPage {

	private final static Logger logger = LoggerFactory.getLogger(ProfileTool.class);
	
	protected IPlottingSystem        profilePlottingSystem;
	private   ITraceListener         traceListener;
	private   IRegionListener        regionListener;
	private   IPaletteListener       paletteListener;
	private   ProfileJob             updateProfiles;
	private   ProfileUIJob           updateUIProfiles;
	private   Map<String,Collection<ITrace>> registeredTraces;
	private   boolean                isUIJob = false;
	protected boolean                alwaysDownsample = false;

	public ProfileTool() {
		
		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
		try {
			profilePlottingSystem = PlottingFactory.createPlottingSystem();
			updateProfiles = new ProfileJob();
			updateUIProfiles = new ProfileUIJob();
			this.paletteListener = new IPaletteListener.Stub() {
				@Override
				public void maskChanged(PaletteEvent evt) {
					update(null, null, false);
				}
				@Override
				public void imageOriginChanged(PaletteEvent evt) {
					update(null, null, false);
				}
			};
			
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
					ProfileTool.this.update(null, null, false);
				}
				@Override
				protected void update(TraceEvent evt) {
					ProfileTool.this.update(null, null, false);
				}
			};
			
			this.regionListener = new IRegionListener.Stub() {			
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().removeROIListener(ProfileTool.this);
						clearTraces(evt.getRegion());
					}
					regionsRemoved(evt);
				}
				
				@Override
				public void regionsRemoved(RegionEvent evt) {
					//clears traces if all regions removed
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions == null || regions.isEmpty()) {
						registeredTraces.clear();
						profilePlottingSystem.clear();
					} else {
						IRegion other = regions.iterator().next();
						ProfileTool.this.update(other, null, false);
					}
				}
				
				@Override
				public void regionAdded(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						ProfileTool.this.update(null, null, false);
					}
				}
				
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().addROIListener(ProfileTool.this);
					}
				}
				
				protected void update(RegionEvent evt) {
					ProfileTool.this.update(null, null, false);
				}
			};
			
			alwaysDownsample = Activator.getLocalPreferenceStore().getBoolean(PlottingConstants.ALWAYS_DOWNSAMPLE_PROFILES);
			
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}

	/**
	 * If set to true, the profile job will run in a UI job
	 * Set to false by default
	 * @param isUIJob
	 */
	public void setIsUIJob(boolean isUIJob) {
		this.isUIJob = isUIJob;
	}

	protected void registerTraces(final IRegion region, final Collection<ITrace> traces) {
		
		final String name = region.getName();
		registerTraces(name, traces);
		
		// Used to set the line on the image to the same color as the plot for line profiles only.
		if (!traces.isEmpty()) {
			final ITrace first = traces.iterator().next();
			if (isRegionTypeSupported(RegionType.LINE) && first instanceof ILineTrace && region.getName().startsWith(getRegionName())) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						region.setRegionColor(((ILineTrace)first).getTraceColor());
					}
				});
			}
		}
	}
	
	protected void registerTraces(String name, Collection<ITrace> traces) {
		
		Collection<ITrace> registered = this.registeredTraces.get(name);
		if (registered==null) {
			registered = new HashSet<ITrace>(7);
			registeredTraces.put(name, registered);
		}
		for (ITrace iTrace : traces) iTrace.setUserObject(ProfileType.PROFILE);
		registered.addAll(traces);
	}

	protected void clearTraces(final IRegion region) {
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
        if (registered!=null) for (ITrace iTrace : registered) {
			profilePlottingSystem.removeTrace(iTrace);
		}
	}

	@Override
	public void createControl(Composite parent) {
		final IPageSite site = getSite();
		createControl(parent, site!=null?site.getActionBars():null);
	}

	public void createControl(Composite parent, IActionBars actionbars) {
		
		final Action reselect = new Action("Create new profile", getImageDescriptor()) {
			public void run() {
				createNewRegion(true);
			}
		};
		if (actionbars != null){
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
			actionbars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));
		}

		profilePlottingSystem.createPlotPart(parent, 
											 getTitle(), 
											 actionbars, 
											 PlotType.XY,
											 this.getViewPart());				
		
		
		configurePlottingSystem(profilePlottingSystem);
		
		// Unused actions removed for tool
		//profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.rescale");
		profilePlottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_INDEX);
		profilePlottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_X_AXIS);

		profilePlottingSystem.setXFirst(true);
		profilePlottingSystem.setRescale(true);
		
		// We get the saved logged axes, if any and log the axes.
		boolean isLog10 = Activator.getLocalPreferenceStore().getBoolean(getToolId()+".xAxisLogged");
		profilePlottingSystem.getSelectedXAxis().setLog10(isLog10);
				
		isLog10 = Activator.getLocalPreferenceStore().getBoolean(getToolId()+".yAxisLogged");
		profilePlottingSystem.getSelectedYAxis().setLog10(isLog10);
		
		final Action clear = new Action("Clear Profiles", Activator.getImageDescriptor("icons/axis.png")) {
			public void run() {
				getPlottingSystem().clearRegions();
			}
		};
		if (actionbars != null){
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.clearRegionsGroup"));
			actionbars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.clearRegionsGroup", clear);
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.clearRegionsGroupAfter"));
		}

		super.createControl(parent);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return profilePlottingSystem;
		} else {
			return super.getAdapter(clazz);
		}
	}

	protected abstract void configurePlottingSystem(IPlottingSystem plotter);
	 
	
	@Override
	public final ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		if (getControl()!=null && !getControl().isDisposed()) {
			getControl().setFocus();
		}
	}
	
	public void activate() {
		super.activate();
		update(null, null, false);
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(regionListener);
		}	
		
		setRegionsActive(true);
		
		// We try to listen to the image mask changing and reprofile if it does.
		if (getPlottingSystem()!=null) {
			if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
		}
		
		if (!isDedicatedView()) createNewRegion(false);
		
	}
	
	protected final void createNewRegion(boolean force) {
		
		// Start with a selection of the right type
		try {
			
			if (!force) {
				// We check to see if the region type preferred is already there
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions) {
					if (iRegion.isUserRegion() && iRegion.isVisible()) {
						// We have one already, do not go into create mode :)
						if (iRegion.getRegionType() == getCreateRegionType()) return;
					}
				}
			}
			
			IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
			region.setUserObject(getMarker());
		} catch (Exception e) {
			logger.error("Cannot create region for profile tool!");
		}
	}
	
	/**
	 * The object used to mark this profile as being part of this tool.
	 * By default just uses package string.
	 * @return
	 */
	protected Object getMarker() {
		return getToolPageRole().getClass().getName().intern();
	}

	/**
	 * 
	 * @return
	 */
	protected abstract boolean isRegionTypeSupported(RegionType type);
	
	/**
	 * 
	 */
    protected abstract RegionType getCreateRegionType();
    
	public void deactivate() {
		super.deactivate();
		saveAxesLogged();
		if (getPlottingSystem()!=null) {

	        getPlottingSystem().removeRegionListener(regionListener);
			getPlottingSystem().removeTraceListener(traceListener);
		}
		setRegionsActive(false);
		if (getPlottingSystem()!=null) {
			if (getImageTrace()!=null) getImageTrace().removePaletteListener(paletteListener);
		}

	}
	
	private void setRegionsActive(boolean active) {
		
		if (getPlottingSystem()!=null) {
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion iRegion : regions) {
				if (active) {
					iRegion.addROIListener(this);
				} else {
					iRegion.removeROIListener(this);
				}
				if (iRegion.getUserObject()==getMarker()) {
					if (active) {
						iRegion.setVisible(active);
					} else {
						// If the plotting system has changed dimensionality
						// to something not compatible with us, remove the region.
						// TODO Change to having getRank() == rank 
						if (getToolPageRole().is2D() && !getPlottingSystem().is2D()) {
						    iRegion.setVisible(active);
						} else if (getPlottingSystem().is2D() && !getToolPageRole().is2D()) {
						    iRegion.setVisible(active);
						}
					}
				}
			}
		}
	}

	@Override
	public Control getControl() {
		if (profilePlottingSystem==null) return null;
		return profilePlottingSystem.getPlotComposite();
	}
	
	public void dispose() {
		deactivate();
		
		registeredTraces.clear();
		if (profilePlottingSystem!=null) profilePlottingSystem.dispose();
		profilePlottingSystem = null;
		super.dispose();
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected abstract Collection<? extends ITrace> createProfile(IImageTrace image, 
			                                IRegion region, 
			                                IROI roi, 
			                                boolean tryUpdate, 
			                                boolean isDrag,
			                                IProgressMonitor monitor);

	@Override
	public void roiDragged(ROIEvent evt) {
		update((IRegion)evt.getSource(), evt.getROI(), true);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		final IRegion region = (IRegion)evt.getSource();
		update(region, region.getROI(), false);
	}
	
	protected synchronized void update(IRegion r, IROI rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.
			if (!r.isUserRegion()) return; // Likewise
		}
		if (!isUIJob)
			updateProfiles.profile(r, rb, isDrag);
		else
			updateUIProfiles.profile(r, rb, isDrag);
	}
	
	private void saveAxesLogged() {
		if (profilePlottingSystem==null || profilePlottingSystem.getPlotComposite()==null || profilePlottingSystem.isDisposed()) return;
		Activator.getLocalPreferenceStore().setValue(getToolId()+".xAxisLogged", profilePlottingSystem.getSelectedXAxis().isLog10());
        Activator.getLocalPreferenceStore().setValue(getToolId()+".yAxisLogged", profilePlottingSystem.getSelectedYAxis().isLog10());
 	}

	protected String getRegionName() {
		return "Profile";
	}
	
	private final class ProfileJob extends Job {
		private   IRegion                currentRegion;
		private   IROI                   currentROI;
		private   boolean                isDrag;

		ProfileJob() {
			super(getRegionName()+" update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, IROI rb, boolean isDrag) {

	        // This in principle is not needed and appears to make no difference wether in or out.
		    // However Irakli has advised that it is needed in some circumstances.
			// This causes the defect reported here however: http://jira.diamond.ac.uk/browse/DAWNSCI-214
			// therefore we are currently not using the extra cancelling.
//	        for (Job job : Job.getJobManager().find(null))
//	            if (job.getClass()==getClass() && job.getState() != Job.RUNNING)
//	        	    job.cancel();
			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			return runProfile(currentRegion, currentROI, isDrag||alwaysDownsample, monitor);
		}
	}

	private final class ProfileUIJob extends UIJob {
		private   IRegion                currentRegion;
		private   IROI                   currentROI;
		private   boolean                isDrag;

		ProfileUIJob() {
			super(getRegionName()+" update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, IROI rb, boolean isDrag) {
			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
			schedule();
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			return runProfile(currentRegion, currentROI, isDrag||alwaysDownsample, monitor);
		}
	}

	private IStatus runProfile(IRegion currentRegion, IROI currentROI, boolean isDrag, IProgressMonitor monitor){
		try {
			if (!isActive()) return Status.CANCEL_STATUS;

			final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);
			IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;

			if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
			if (image==null) {
				profilePlottingSystem.clear();
				return Status.OK_STATUS;
			}

			// if the current region is null try and update quickly (without creating 1D)
			// if the trace is in the registered traces object
			if (currentRegion==null) {
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions!=null) {
					for (IRegion iRegion : regions) {
						if (!iRegion.isUserRegion()) continue;
						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
						if (registeredTraces.containsKey(iRegion.getName())) {
							createProfile(image, iRegion, iRegion.getROI(), true, isDrag, monitor);
						} else {
							createProfile(image, iRegion, iRegion.getROI(), false, isDrag, monitor);
						}
					}
				} else {
					registeredTraces.clear();
					profilePlottingSystem.clear();
				}
			} else {

				if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
				createProfile(image, 
						      currentRegion, 
						      currentROI!=null?currentROI:currentRegion.getROI(), 
							  true, 
							  isDrag,
							  monitor);

			}

			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			profilePlottingSystem.repaint(true);

			return Status.OK_STATUS;
			
		} catch (Throwable ne) {
			logger.error("Internal error processing profile! ", ne);
			return Status.CANCEL_STATUS;
		}
	}

	/**
	 * Used to tell if tool can be used with multiple slice 'Data Reduction' tool.
	 */
	public boolean isProfileTool() {
		return true;
	}
	
	@Override
	public IPlottingSystem getToolPlottingSystem() {
		return profilePlottingSystem;
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

	}
}
