package org.dawnsci.plotting.histogram;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchSite;

/**
 * A reusable widget that encapsulates a histogram plot, region of interest, and
 * plotting system with histogram trace lines and  RGB trace lines.
 *
 */
public class HistogramWidget extends Composite {

	private IPlottingSystem histogramPlottingSystem = null;
	private boolean regionDragging = false;
	
	private ILineTrace histoTrace;
	private ILineTrace redTrace;
	private ILineTrace greenTrace;
	private ILineTrace blueTrace;
	

	private double histoMax = 50.0;
	private double histoMin = 25.0;

	private IROIListener histogramRegionListener = new IROIListener.Stub() {
		@Override
		public void roiDragged(ROIEvent evt) {
			IROI roi = evt.getROI();
			updateHistogramToolElements(evt, roi);
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			IROI roi = evt.getROI();
			updateHistogramToolElements(evt, roi);
		}
	};

	/**
	 * Create a new Histogram Widget
	 * 
	 * @param composite
	 *            parent composite to add widget to. Must not be
	 *            <code>null</code>
	 * @param title
	 *            Title string for plot
	 * @param plot
	 *            A plotting system to work with this widget. Can be
	 *            <code>null</code>, in which case a default plotting system
	 *            will be created
	 * @param site
	 *            the workbench site this widget sits in. This is used to set
	 *            commands and handlers. Can be <code>null</code>, in which case
	 *            handlers will not be added and a pop-up menu will not be
	 *            added.
	 * @throws Exception
	 *             throws an exception if there is a failure to create a default
	 *             plotting system or region of interest
	 */
	public HistogramWidget(final Composite composite, String title,
			IPlottingSystem plot, IWorkbenchSite site) throws Exception {
		super(composite, SWT.NONE);

		setLayout(new FillLayout());

		if (plot != null) {
			histogramPlottingSystem = plot;
		} else {
			histogramPlottingSystem = PlottingFactory.createPlottingSystem();
		}

		// IActionBars actionBars = (site != null) ? site.getActionBars() :
		// null;
		histogramPlottingSystem.createPlotPart(this, title, null, PlotType.XY,
				null);
		histogramPlottingSystem.setRescale(false);

		createRegion();
		createTraces();
	}

	protected void updateHistogramToolElements(ROIEvent evt, IROI roi) {
		// TODO Auto-generated method stub

	}

	/**
	 * Create the region of interest for the histogram
	 */
	private void createRegion() throws Exception {
		IRegion region = histogramPlottingSystem.getRegion("Histogram Region");

		// Test if the region is already there and update the currentRegion
		if (region == null || !region.isVisible()) {
			region = histogramPlottingSystem.createRegion("Histogram Region",
					RegionType.XAXIS);
			histogramPlottingSystem.addRegion(region);
			region.addROIListener(histogramRegionListener);
		}

		RectangularROI rroi = new RectangularROI(histoMin, 0, histoMax
				- histoMin, 1, 0);

		// Stop unneeded events firing when roi is set by removing listeners.
		region.removeROIListener(histogramRegionListener);
		region.setROI(rroi);
		region.addROIListener(histogramRegionListener);
	}

	private void createTraces() {
		// Set up the histogram trace
		histoTrace = histogramPlottingSystem
				.createLineTrace("Histogram");
		histoTrace.setTraceType(TraceType.AREA);
		histoTrace.setLineWidth(1);
		histoTrace.setTraceColor(new Color(null, 0, 0, 0));

		// Set up the RGB traces
		redTrace = histogramPlottingSystem.createLineTrace("Red");
		greenTrace = histogramPlottingSystem
				.createLineTrace("Green");
		blueTrace = histogramPlottingSystem.createLineTrace("Blue");

		redTrace.setLineWidth(2);
		greenTrace.setLineWidth(2);
		blueTrace.setLineWidth(2);

		redTrace.setTraceColor(new Color(null, 255, 0, 0));
		greenTrace.setTraceColor(new Color(null, 0, 255, 0));
		blueTrace.setTraceColor(new Color(null, 0, 0, 255));

		// Finally add everything in a threadsafe way.
		this.getParent().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				histogramPlottingSystem.addTrace(histoTrace);
				histogramPlottingSystem.addTrace(redTrace);
				histogramPlottingSystem.addTrace(greenTrace);
				histogramPlottingSystem.addTrace(blueTrace);
				
				//histogramPlot.getSelectedXAxis().setLog10(btnColourMapLog.getSelection());
				histogramPlottingSystem.getSelectedXAxis().setTitle("Intensity");
				//histogramPlot.getSelectedYAxis().setRange(0, finalScale*256);
				histogramPlottingSystem.getSelectedYAxis().setTitle("Log(Frequency)");
			};
		});

	}

	/**
	 * Get the plotting system associated with this histogram plot
	 * 
	 * @return IPlottingSystem the plotting system associated with this
	 *         histogram
	 */
	public IPlottingSystem getHistogramPlot() {
		return histogramPlottingSystem;
	}
}
