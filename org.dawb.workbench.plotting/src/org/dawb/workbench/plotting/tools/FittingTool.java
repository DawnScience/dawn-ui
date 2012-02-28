package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;

public class FittingTool extends AbstractToolPage implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(FittingTool.class);
	
	private Composite     composite;
	private TableViewer   table;
	private IRegion       fitRegion;
	private Job           fittingJob;
	private List<IRegion> peakRegions;
	private List<ITrace>  peakTraces;

	public FittingTool() {
		super();
		this.peakRegions = new ArrayList<IRegion>(7);
		this.peakTraces  = new ArrayList<ITrace>(7);
	}

	@Override
	public void createControl(Composite parent) {
		
		// TODO Create a table for the regions.
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Fitting...");
		
		getSite().getActionBars().getToolBarManager().add(new Action("Test") {});
		
		this.fittingJob = createFittingJob();
		
		activate();
	}

	@Override
	public void activate() {
		try {
			for (IRegion region : peakRegions) region.setVisible(true);
			for (ITrace  trace  : peakTraces)  trace.setVisible(true);
			getPlottingSystem().addRegionListener(this);
			this.fitRegion = getPlottingSystem().createRegion("Fit selection", IRegion.RegionType.XAXIS);
			
		} catch (Exception e) {
			logger.error("Cannot put the selection into fitting region mode!", e);
		}		
	}
	@Override
	public void deactivate() {
		try {
			getPlottingSystem().removeRegionListener(this);
			for (IRegion region : peakRegions) region.setVisible(false);
			for (ITrace  trace  : peakTraces)  trace.setVisible(false);
			
		} catch (Exception e) {
			logger.error("Cannot put the selection into fitting region mode!", e);
		}		
	}

	@Override
	public void setFocus() {
        if (table!=null) table.getControl().setFocus();
	}
	
	public void dispose() {
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(this);
		}
        if (table!=null) table.getControl().dispose();
		super.dispose();
	}


	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		
		
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		
		if (evt.getRegion()==fitRegion) fittingJob.schedule();
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		
		
	}


	public Job createFittingJob() {
		
		final Job fit = new Job("Fit peaks") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				if (composite.isDisposed()) return Status.CANCEL_STATUS;
				
				composite.getDisplay().syncExec(new Runnable() {
					public void run() {
						getPlottingSystem().clearRegions();
						for (ITrace  trace  : peakTraces)  getPlottingSystem().removeTrace(trace);
				    }
				});
				
				final RegionBounds bounds = fitRegion.getRegionBounds();
				getPlottingSystem().removeRegionListener(FittingTool.this);
				
				final Collection<ITrace> traces = getPlottingSystem().getTraces();
				if (traces==null || traces.size()<0) return Status.CANCEL_STATUS;

				// We chop x and y by the region bounds. We assume the
				// plot is an XAXIS selection therefore the indices in
				// y = indices chosen in x.
				final double[] p1 = bounds.getP1();
				final double[] p2 = bounds.getP2();
				
				// We peak fit only the first of the data sets plotted for now.
				final ILineTrace   line  = (ILineTrace)traces.iterator().next();
				AbstractDataset x  = line.getXData();
				AbstractDataset y  = line.getYData();
				
				AbstractDataset[] a= FittingUtils.xintersection(x,y,p1[0],p2[0]);
				x = a[0]; y=a[1];
				
				final FittedPeaksBean bean = FittingUtils.getFittedPeaks(x, y, monitor);
				createFittedPeaks(bean);
				
				return Status.OK_STATUS;
			}
		};
		
		fit.setSystem(true);
		fit.setUser(true);
		fit.setPriority(Job.INTERACTIVE);
		return fit;
	}

	/**
	 * Thread safe
	 * @param peaks
	 */
	protected void createFittedPeaks(final FittedPeaksBean bean) {
		
		composite.getDisplay().syncExec(new Runnable() {
			
		    public void run() {
		    	try {
		    		peakRegions.clear();
		    		
					int ipeak = 1;
					// Draw the regions
					for (RegionBounds rb : bean.getPeakBounds()) {
						
						final IRegion area = getPlottingSystem().createRegion("Peak "+ipeak, RegionType.XAXIS);
						area.setRegionColor(ColorConstants.orange);
						area.setRegionBounds(rb);
						area.setMotile(false);
						getPlottingSystem().addRegion(area);
						peakRegions.add(area);
						
						
						final IRegion line = getPlottingSystem().createRegion("Peak Line "+ipeak, RegionType.XAXIS_LINE);
						line.setRegionBounds(new RegionBounds(rb.getCentre(), rb.getCentre()));
						line.setRegionColor(ColorConstants.black);
						line.setMotile(false);
						line.setAlpha(150);
						getPlottingSystem().addRegion(line);
						peakRegions.add(line);
					

					    ++ipeak;
					}
					
		    		peakTraces.clear();

					ipeak = 1;
					// Create some traces for the fitted function
					for (AbstractDataset[] pair : bean.getFunctionData()) {
						
						final ILineTrace trace = getPlottingSystem().createLineTrace("Peak Function "+ipeak);
						trace.setData(pair[0], pair[1]);
						trace.setLineWidth(1);
						trace.setTraceColor(ColorConstants.black);
						getPlottingSystem().addTrace(trace);
						peakTraces.add(trace);
						
					    ++ipeak;
  				    }
					
					
		    	} catch (Exception ne) {
		    		logger.error("Cannot create fitted peaks!", ne);
		    	}
		    } 
		});
	}
}
