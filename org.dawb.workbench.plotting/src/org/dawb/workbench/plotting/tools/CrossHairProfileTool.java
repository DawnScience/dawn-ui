package org.dawb.workbench.plotting.tools;

import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossHairProfileTool extends AbstractToolPage  {

	private final static Logger logger = LoggerFactory.getLogger(CrossHairProfileTool.class);
	
	protected AbstractPlottingSystem plotter;
	private   ITraceListener         traceListener;
	private   IRegion                xHair, yHair;

	
	public CrossHairProfileTool() {
		try {
			plotter = PlottingFactory.getPlottingSystem();
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					updateProfile();
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
								null);		
		createRegions();
		
		activate();
	}
	
	private void createRegions() {
		
		if (getPlottingSystem()==null) return;
		try {
			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("X cross-hair", getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
				xHair.setVisible(false);
				xHair.setTrackMouse(true);
				xHair.setRegionColor(ColorConstants.red);
				xHair.setUserRegion(false); // They cannot see preferences or change it!
				getPlottingSystem().addRegion(xHair);
			}
			
			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Y cross-hair", getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
				yHair.setVisible(false);
				yHair.setTrackMouse(true);
				yHair.setRegionColor(ColorConstants.red);
				yHair.setUserRegion(false); // They cannot see preferences or change it!
				getPlottingSystem().addRegion(yHair);
			}
			
		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

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
		
		createRegions();
		if (xHair!=null) xHair.setVisible(true);
		if (yHair!=null) yHair.setVisible(true);

		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			updateProfile();
		}
	}
	
	public void deactivate() {
		super.deactivate();
		if (xHair!=null) xHair.setVisible(false);
		if (yHair!=null) yHair.setVisible(false);

		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
	}
	
	public void dispose() {
		
	    if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}
	
	@Override
	public Control getControl() {
		if (plotter==null) return null;
		return plotter.getPlotComposite();
	}


	private Job updateProfileJob;
	
	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private synchronized void updateProfile() {

		if (updateProfileJob==null) {
			updateProfileJob = new Job("Cross hair profile update") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					

					return Status.OK_STATUS;
				}	
			};
			updateProfileJob.setSystem(true);
			updateProfileJob.setUser(false);
			updateProfileJob.setPriority(Job.INTERACTIVE);
		}


		updateProfileJob.schedule();
	}


}
