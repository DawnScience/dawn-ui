package org.dawb.workbench.plotting.tools;

import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionBoundsListener;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.region.RegionBoundsEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
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

public abstract class ProfileTool extends AbstractToolPage  implements IRegionBoundsListener {

	private final static Logger logger = LoggerFactory.getLogger(ProfileTool.class);
	
	protected AbstractPlottingSystem plotter;
	private   ITraceListener         traceListener;
	private   IRegionListener        regionListener;
	private   Job                    updateProfiles;
	private   IRegion                currentRegion;
	private   RegionBounds           currentBounds;

	public ProfileTool() {
		try {
			plotter = PlottingFactory.getPlottingSystem();
			plotter.setColorOption(ColorOption.NONE);
			plotter.setDatasetChoosingRequired(false);
			
			updateProfiles = createProfileJob();
			
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					updateProfiles.schedule();
				}
			};
			
			this.regionListener = new IRegionListener.Stub() {			
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) evt.getRegion().removeRegionBoundsListener(ProfileTool.this);
				}
				@Override
				public void regionAdded(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						currentRegion = evt.getRegion();
					    updateProfiles.schedule();
					}
				}
				
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion()!=null) evt.getRegion().addRegionBoundsListener(ProfileTool.this);
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	@Override
	public void createControl(Composite parent) {


		final IPageSite site = getSite();
		
		plotter.createPlotPart(parent, 
								getTitle(), 
								site.getActionBars(), 
								PlotType.PT1D,
								this.getPart());		

		createAxes(plotter);
	}

	protected abstract void createAxes(AbstractPlottingSystem plotter);

	/**
	 * Required if you want to make tools work.
	 * Currently we do not want 1D tools on the derivative page
	 * 
	public Object getAdapter(final Class clazz) {

		if (clazz == IToolPageSystem.class) {
			return plotter;
		}

		return null;
	}
	 */
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		
	}
	
	public void activate() {
		super.activate();
		updateProfiles.schedule();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(regionListener);
		}		
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions!=null) for (IRegion iRegion : regions) iRegion.addRegionBoundsListener(this);
	}
	
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(regionListener);
		}
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions!=null) for (IRegion iRegion : regions) iRegion.removeRegionBoundsListener(this);
	}
	
	@Override
	public Control getControl() {
		if (plotter==null) return null;
		return plotter.getPlotComposite();
	}
	
	public void dispose() {
		deactivate();
		
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}

	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private Job createProfileJob() {

		Job job = new Job("Profile update") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				final Collection<ITrace>    traces= getPlottingSystem().getTraces();	
				IImageTrace image = null;
				for (ITrace trace : traces) {
					if (trace instanceof IImageTrace) {
						image = (IImageTrace)trace;
						break;
					}
				}

				if (image==null) {
					plotter.clear();
					return Status.OK_STATUS;
				}

				// Get the profiles from the line and box regions.
				if (currentRegion==null) {
					plotter.clear();
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions!=null) {
						for (IRegion iRegion : regions) createProfile(image, iRegion, null, monitor);
					}
				} else {

					createProfile(image, currentRegion, currentBounds!=null?currentBounds:currentRegion.getRegionBounds(), monitor);
					currentRegion = null;
					currentBounds = null;
				}

				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
                plotter.repaint();
                
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
	 * @param bounds - may be null
	 * @param monitor
	 */
	protected abstract void createProfile(IImageTrace image, IRegion region, final RegionBounds bounds, IProgressMonitor monitor);

	@Override
	public void regionBoundsDragged(RegionBoundsEvent evt) {
		currentRegion = (IRegion)evt.getSource();
		currentBounds = evt.getRegionBounds();
		updateProfiles.schedule();
	}

	@Override
	public void regionBoundsChanged(RegionBoundsEvent evt) {
		updateProfiles.schedule();
	}
	
	@Override
	public Object getAdapter(Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plotter;
		} else {
			return super.getAdapter(clazz);
		}
	}

}
