package org.dawb.workbench.plotting.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public abstract class ProfileTool extends AbstractToolPage  implements IROIListener {

	private final static Logger logger = LoggerFactory.getLogger(ProfileTool.class);
	
	protected AbstractPlottingSystem plotter;
	private   ITraceListener         traceListener;
	private   IRegionListener        regionListener;
	private   Job                    updateProfiles;
	private   IRegion                currentRegion;
	private   ROIBase                currentROI;
	private   Map<String,Collection<ITrace>> registeredTraces;

	public ProfileTool() {
		
		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
		try {
			plotter = PlottingFactory.getPlottingSystem();
			updateProfiles = createProfileJob();
			
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					update(null, null);
				}
			};
			
			this.regionListener = new IRegionListener.Stub() {			
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().removeROIListener(ProfileTool.this);
						clearTraces(evt.getRegion());
					}
				}
				@Override
				public void regionAdded(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						update(null, null);
					}
				}
				
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().addROIListener(ProfileTool.this);
					}
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	protected void registerTraces(final IRegion region, final Collection<ITrace> traces) {
		
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
		if (registered==null) {
			registered = new HashSet<ITrace>(7);
			registeredTraces.put(name, registered);
		}
		registered.addAll(traces);
		
		// Used to set the line on the image to the same color as the plot for line profiles only.
		final ITrace first = traces.iterator().next();
		if (isRegionTypeSupported(RegionType.LINE) && first instanceof ILineTrace && region.getName().startsWith("Profile")) {
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					region.setRegionColor(((ILineTrace)first).getTraceColor());
				}
			});
		}
	}
	
	protected void clearTraces(final IRegion region) {
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
        if (registered!=null) for (ITrace iTrace : registered) {
			plotter.removeTrace(iTrace);
		}
	}
	
	@Override
	public void createControl(Composite parent) {


		final IPageSite site = getSite();
		
		plotter.createPlotPart(parent, 
								getTitle(), 
								site.getActionBars(), 
								PlotType.PT1D,
								this.getViewPart());		

		createAxes(plotter);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plotter;
		} else {
			return super.getAdapter(clazz);
		}
	}

	protected abstract void createAxes(AbstractPlottingSystem plotter);
	 
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		
	}
	
	public void activate() {
		super.activate();
		update(null, null);
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(regionListener);
		}		
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions!=null) for (IRegion iRegion : regions) iRegion.addROIListener(this);
		
		// Start with a selection of the right type
		try {
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Profile", getPlottingSystem()), getCreateRegionType());
		} catch (Exception e) {
			logger.error("Cannot create region for profile tool!");
		}
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
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(regionListener);
		}
		if (getPlottingSystem()!=null) {
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion iRegion : regions) iRegion.removeROIListener(this);
		}
	}
	
	@Override
	public Control getControl() {
		if (plotter==null) return null;
		return plotter.getPlotComposite();
	}
	
	public void dispose() {
		deactivate();
		
		registeredTraces.clear();
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}

	private boolean isUpdateRunning = false;
	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derivative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private Job createProfileJob() {

		Job job = new Job("Profile update") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					isUpdateRunning = true;
					if (!isActive()) return Status.CANCEL_STATUS;
	
					final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
					IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
	
					if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
					if (image==null) {
						plotter.clear();
						return Status.OK_STATUS;
					}
	
					// Get the profiles from the line and box regions.
					if (currentRegion==null) {
						plotter.clear();
						registeredTraces.clear();
						final Collection<IRegion> regions = getPlottingSystem().getRegions();
						if (regions!=null) {
							for (IRegion iRegion : regions) {
								if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
								createProfile(image, iRegion, null, false, monitor);
							}
						}
					} else {
	
						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
						createProfile(image, 
								      currentRegion, 
								      currentROI!=null?currentROI:currentRegion.getROI(), 
								      true, 
								      monitor);
						
					}
	
					if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	                plotter.repaint();
	                
				} finally {
					isUpdateRunning = false;
				}

                                
				return Status.OK_STATUS;

			}	
		};
		job.setSystem(true);
		job.setUser(false);
		job.setPriority(Job.INTERACTIVE);

		return job;
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected abstract void createProfile(IImageTrace image, 
			                              IRegion region, 
			                              ROIBase roi, 
			                              boolean tryUpdate, 
			                              IProgressMonitor monitor);

	@Override
	public void roiDragged(ROIEvent evt) {
		update((IRegion)evt.getSource(), evt.getROI());
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		final IRegion region = (IRegion)evt.getSource();
		update(region, region.getROI());
		
		try {
			updateProfiles.join();
		} catch (InterruptedException e) {
			logger.error("Update profiles job interrupted!", e);
		}
		
        getControl().getDisplay().syncExec(new Runnable() {
        	public void run() {
        		plotter.autoscaleAxes();
        	}
        });

	}
	
	private synchronized void update(IRegion r, ROIBase rb) {
	
		if (r!=null && !isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.

        if (isUpdateRunning)  updateProfiles.cancel();
         
		this.currentRegion = r;
		this.currentROI = rb;
		updateProfiles.schedule();
	}
}
