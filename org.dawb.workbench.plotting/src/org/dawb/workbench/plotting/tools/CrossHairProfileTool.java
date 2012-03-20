package org.dawb.workbench.plotting.tools;

import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.IAxis;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionBoundsListener;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.region.RegionBoundsEvent;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class CrossHairProfileTool extends AbstractToolPage implements IRegionBoundsListener  {

	private final static Logger logger = LoggerFactory.getLogger(CrossHairProfileTool.class);
	
	protected IPlottingSystem        plotter;
	private   ITraceListener         traceListener;
	private   IRegion                xHair, yHair;
	private   IAxis                  x1,x2;
	private   Job                    xUpdateJob, yUpdateJob;
	private   RegionBounds           xBounds, yBounds;
	
	public CrossHairProfileTool() {
		try {
			
			plotter = PlottingFactory.getPlottingSystem();
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					if (xUpdateJob!=null) xUpdateJob.schedule();
					if (yUpdateJob!=null) yUpdateJob.schedule();
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
		
		plotter.getSelectedYAxis().setTitle("Intensity");
		this.x1 = plotter.getSelectedXAxis();
		x1.setTitle("X Slice");
		
		this.x2 = plotter.createAxis("Y Slice", false, SWT.TOP);
		
		activate();
	}
	
	private void createRegions() {
		
		if (getPlottingSystem()==null) return;
		try {
			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
				this.xHair = getPlottingSystem().createRegion("Y Profile", IRegion.RegionType.XAXIS_LINE);
				xHair.setVisible(false);
				xHair.setTrackMouse(true);
				xHair.setRegionColor(ColorConstants.red);
				xHair.setUserRegion(false); // They cannot see preferences or change it!
				getPlottingSystem().addRegion(xHair);
				this.xUpdateJob = createProfileJob(xHair);
			}
			
			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
				this.yHair = getPlottingSystem().createRegion("X Profile", IRegion.RegionType.YAXIS_LINE);
				yHair.setVisible(false);
				yHair.setTrackMouse(true);
				yHair.setRegionColor(ColorConstants.red);
				yHair.setUserRegion(false); // They cannot see preferences or change it!
				getPlottingSystem().addRegion(yHair);
				this.yUpdateJob = createProfileJob(yHair);
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
		if (xHair!=null) {
			xHair.setVisible(true);
			xHair.addRegionBoundsListener(this);
		}
		if (yHair!=null) {
			yHair.setVisible(true);
			yHair.addRegionBoundsListener(this);
		}

		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
	}
	
	public void deactivate() {
		super.deactivate();
		if (xHair!=null) {
			xHair.setVisible(false);
			xHair.removeRegionBoundsListener(this);
		}
		if (yHair!=null) {
			yHair.setVisible(false);
			yHair.removeRegionBoundsListener(this);
		}

		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
	}
	
	public void dispose() {
		
	    deactivate();
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}
	
	@Override
	public Control getControl() {
		if (plotter==null) return null;
		return plotter.getPlotComposite();
	}

	
	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private Job createProfileJob(final IRegion region) {

		Job job = new Job("Cross hair profile update") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				if (x1==null | x2==null) return Status.OK_STATUS;

				RegionBounds bounds = region==xHair ? xBounds : yBounds;
				if (bounds!=null) {

					final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
					IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;

					if (image==null) {
						plotter.clear();
						return Status.OK_STATUS;
					}

					ILineTrace trace = (ILineTrace)plotter.getTrace(region.getName());
					if (trace == null) {
						synchronized (plotter) {  // Only one job at a time can choose axis and create plot.
							if (region==xHair) {
								plotter.setSelectedXAxis(x1);
	
							} else if (region==yHair) {
								plotter.setSelectedXAxis(x2);
							}
							trace = plotter.createLineTrace(region.getName());
							
							if (region==xHair) {
								trace.setTraceColor(ColorConstants.blue);
							} else if (region==yHair) {
								trace.setTraceColor(ColorConstants.red);
							}
						}
					}
					trace.setName(region.getName());

					final AbstractDataset data = image.getData();
					AbstractDataset slice=null, sliceIndex=null;
					if (region==xHair) {
						int index = (int)Math.round(bounds.getX());
						slice = data.getSlice(new int[]{0,index}, new int[]{data.getShape()[0], index+1}, new int[]{1,1});
						slice = slice.flatten();
						sliceIndex = AbstractDataset.arange(slice.getSize(), AbstractDataset.INT);

					} else if (region==yHair) {
						int index = (int)Math.round(bounds.getY());
						slice = data.getSlice(new int[]{index,0}, new int[]{index+1, data.getShape()[1]}, new int[]{1,1});
						slice = slice.flatten();
						sliceIndex = AbstractDataset.arange(slice.getSize(), AbstractDataset.INT);
					}
					slice.setName(region.getName());
					trace.setData(sliceIndex, slice);

					final ILineTrace finalTrace = trace;


					getControl().getDisplay().syncExec(new Runnable() {
						public void run() {

							if (plotter.getTrace(finalTrace.getName())==null) {							
								plotter.addTrace(finalTrace);
							}

							plotter.repaint();
							if (region==xHair) {
								x1.setRange(0, data.getShape()[0]);
							} else if (region==yHair) {
								x2.setRange(0, data.getShape()[1]);
							}
						}
					});
				}

				return Status.OK_STATUS;
			}	
		};
		job.setSystem(true);
		job.setUser(false);
		job.setPriority(Job.INTERACTIVE);

		return job;
	}

	@Override
	public void regionBoundsDragged(RegionBoundsEvent evt) {
		update((IRegion)evt.getSource(), evt.getRegionBounds());
	}

	@Override
	public void regionBoundsChanged(RegionBoundsEvent evt) {
		final IRegion region = (IRegion)evt.getSource();
		update(region, region.getRegionBounds());
	}
	
	private void update(IRegion r, RegionBounds rb) {
		if (r == xHair) {
			this.xBounds = rb;
			xUpdateJob.schedule();
		}
		if (r == yHair) {
			this.yBounds = rb;
			yUpdateJob.schedule();
		}
	}

}
