package org.dawnsci.plotting.histogram.ui;

import org.dawnsci.plotting.histogram.IHistogramProvider;
import org.dawnsci.plotting.histogram.IHistogramProvider.IHistogramDatasets;
import org.eclipse.core.runtime.Assert;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A viewer for a histogram (composite with a histogram plot, region of
 * interest, and plotting system with histogram trace lines and RGB trace
 * lines.)
 * <p>
 * Content providers for histogram viewer must implement the
 * <code>IHistogramProvider</code> interface and set it using
 * <code>setContentProvider</code>
 * </p>
 * <p>
 * Input to the histogram viewer must implement the <code>IPaletteTrace</code>
 * interface and get set using <code>setInput</code> method.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class HistogramViewer extends ContentViewer {

	// Used to stop recursion.
	private boolean updatingROI = false;

	private static final Logger logger = LoggerFactory.getLogger(HistogramViewer.class);

	private Composite composite;
	private IPlottingSystem<Composite> histogramPlottingSystem = null;
	private IRegion region;

	private Text minText;
	private Text maxText;

	private ILineTrace histoTrace;
	private ILineTrace redTrace;
	private ILineTrace greenTrace;
	private ILineTrace blueTrace;
	private boolean firstUpdateTraces = true;

	private IROIListener histogramRegionListener = new IROIListener.Stub() {

		@Override
		public void roiDragged(ROIEvent evt) {
			// Do nothing
		}

		@Override
		public void update(ROIEvent evt) {
			if (updatingROI) return;
			try {
				updatingROI = true;
				IROI roi = evt.getROI();
				if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					getHistogramProvider().setMin(rroi.getPoint()[0]);
					double max = rroi.getEndPoint()[0];
					getHistogramProvider().setMax(max);
					rescaleAxis(false);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				updatingROI = false;
			}
		}
	};

	/**
	 * Create a new Histogram Widget with a newly created plotting system.
	 *
	 * @param composite
	 *            parent composite to add widget to. Must not be
	 *            <code>null</code>
	 * @throws Exception
	 *             throws an exception if there is a failure to create a default
	 *             plotting system or region of interest
	 */
	public HistogramViewer(final Composite parent) throws Exception {
		this(parent, null, null, null);
	}

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
	public HistogramViewer(final Composite parent, String title,
			IPlottingSystem<Composite> plot, IActionBars site) throws Exception {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		createMinMaxSettings(composite);

		if (plot != null) {
			histogramPlottingSystem = plot;
		} else {
			histogramPlottingSystem = PlottingFactory.createPlottingSystem();
		}

		histogramPlottingSystem.createPlotPart(composite, title, site,
				PlotType.XY, null);
		histogramPlottingSystem.setRescale(false);
		histogramPlottingSystem.getPlotComposite().setLayoutData(
				GridDataFactory.fillDefaults().grab(true, true).create());

		createRegion();
		createTraces();
		installMinMaxListeners();

		hookControl(getControl());
	}

	private void createMinMaxSettings(Composite comp) {
		Composite c = new Composite(comp, SWT.NONE);
		c.setLayout(GridLayoutFactory.swtDefaults().numColumns(4)
				.create());
		c.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());

		Label minLabel = new Label(c, SWT.NONE);
		minLabel.setText("Min:");
		minLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		minText = new Text(c, SWT.BORDER);
		minText.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		Label maxLabel = new Label(c, SWT.NONE);
		maxLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		maxLabel.setText("Max:");
		maxText = new Text(c, SWT.BORDER);
		maxText.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));

	}

	/**
	 * Create the region of interest for the histogram
	 */
	private void createRegion() throws Exception {
		region = histogramPlottingSystem.createRegion("Histogram Region", RegionType.XAXIS);
		region.setRegionColor(ColorConstants.blue);
		histogramPlottingSystem.addRegion(region);
		region.addROIListener(histogramRegionListener);
	}

	/**
	 * Check if values are close to each other
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean isCloseTo(double a, double b) {
		if (a == b) {
			return true;
		}
		double max = Math.max(Math.abs(a), Math.abs(b));
		return Math.abs(a-b) < 10*Math.ulp(max);
	}

	/**
	 * Update Region
	 *
	 * @param histoMax
	 * @param histoMin
	 */
	private void updateRegion(double histoMin, double histoMax) {
		IRectangularROI oldRoi = (IRectangularROI) region.getROI();

		if (oldRoi != null) {
			// don't bother updating region if it is the same
			if (isCloseTo(oldRoi.getPoint()[0], histoMin)
					&& isCloseTo(oldRoi.getEndPoint()[0], histoMax)) {
				return;
			}
		}

		RectangularROI rroi = new RectangularROI(histoMin, 0, histoMax - histoMin, 1, 0);
		region.removeROIListener(histogramRegionListener);
		try {
			region.setROI(rroi);
		} finally {
			region.addROIListener(histogramRegionListener);
		}
	}

	public void clear() {
		histoTrace.setVisible(false);
		redTrace.setVisible(false);
		greenTrace.setVisible(false);
		blueTrace.setVisible(false);
		region.setVisible(false);
	}

	/**
	 * Create traces
	 */
	private void createTraces() {
		// Set up the histogram trace
		histoTrace = histogramPlottingSystem.createLineTrace("Histogram");
		histoTrace.setTraceType(TraceType.STEP_HORIZONTALLY);
		histoTrace.setLineWidth(1);
		histoTrace.setTraceColor(new Color(null, 0, 0, 0));

		// Set up the RGB traces
		redTrace = histogramPlottingSystem.createLineTrace("Red");
		greenTrace = histogramPlottingSystem.createLineTrace("Green");
		blueTrace = histogramPlottingSystem.createLineTrace("Blue");

		redTrace.setLineWidth(2);
		greenTrace.setLineWidth(2);
		blueTrace.setLineWidth(2);

		redTrace.setTraceColor(new Color(null, 255, 0, 0));
		greenTrace.setTraceColor(new Color(null, 0, 255, 0));
		blueTrace.setTraceColor(new Color(null, 0, 0, 255));

		histogramPlottingSystem.addTrace(histoTrace);
		histogramPlottingSystem.addTrace(redTrace);
		histogramPlottingSystem.addTrace(greenTrace);
		histogramPlottingSystem.addTrace(blueTrace);

		histogramPlottingSystem.getSelectedXAxis().setTitle("Intensity");
		histogramPlottingSystem.getSelectedYAxis().setTitle("Log(Frequency)");
	}

	/**
	 * Update RGB traces
	 */
	private void updateTraces() {
		IHistogramDatasets data = getHistogramProvider().getDatasets();
		
		histoTrace.setData(data.getX(), data.getY());
		histoTrace.setVisible(true);
		redTrace.setData(data.getRGBX(), data.getR());
		redTrace.setVisible(true);
		greenTrace.setData(data.getRGBX(), data.getG());
		greenTrace.setVisible(true);
		blueTrace.setData(data.getRGBX(), data.getB());
		blueTrace.setVisible(true);
//		blueTrace.repaint();
		IAxis xAxis = histogramPlottingSystem.getSelectedXAxis();
		xAxis.setLog10(getHistogramProvider().isLogColorScale());
		xAxis.setAxisAutoscaleTight(true);

		histogramPlottingSystem.getSelectedYAxis().setAxisAutoscaleTight(true);

	}


	private void installMinMaxListeners() {
		
		minText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				updateMin();
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				//do nothing
				
			}
		});
		
		//force focus loss on enter
		minText.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (SWT.TRAVERSE_RETURN == e.detail) {
					updateMin();
				}
			}
		});
		
		
		maxText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				updateMax();
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				//do nothing
				
			}
		});
		
		//force focus loss on enter
		maxText.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (SWT.TRAVERSE_RETURN == e.detail) {
					updateMax();
				}
			}
		});

	}
	
	private void updateMin() {
		Double d = parseDouble(minText);
		
		if (d == null) {
			minText.setText(Double.toString(getHistogramProvider().getMin()));
			return;
		}
		
		if (validateMin(d)) {
			getHistogramProvider().setMin(d);
			if (!updatingROI) {
				updateRegion(d, getHistogramProvider().getMax());
			}
//			maxText.getParent().layout();
			rescaleAxis(false);
		}
	}
	
	private void updateMax() {
		Double d = parseDouble(maxText);
		
		if (d == null) {
			maxText.setText(Double.toString(getHistogramProvider().getMax()));
			return;
		}
		
		if (validateMax(d)) {
			getHistogramProvider().setMax(d);
			if (!updatingROI) {
				updateRegion(getHistogramProvider().getMin(),d);
			}
//			maxText.getParent().layout();
			rescaleAxis(false);
		}
	}

	/**
	 * Check our minimum values are valid before
	 * applying them
	 */
	private boolean validateMin(double minValue) {
		Double d = parseDouble(maxText);
		if (d == null) {
			return false;
		}
		if (minValue > d) {
			return false;
		}
		return true;
	}

	/**
	 * Check our maximum values are valid before
	 * applying them
	 */
	private boolean validateMax(double mainValue){
		Double d = parseDouble(maxText);
		if (d == null) {
			return false;
		}
		if (mainValue < d) {
			return false;
		}
		return true;
	}

	/**
	 * Updates the region and traces
	 */
	@Override
	protected void inputChanged(Object input, Object oldInput) {
		try {
			refresh();
		} catch (Exception e) {
			logger.debug(e.getMessage());
		}
	};

	/**
	 * Set the input for this viewer, must instantiate IPaletteTrace
	 */
	@Override
	public void setInput(Object input) {
		Assert.isTrue(input instanceof IPaletteTrace);
		super.setInput(input);
	}

	/**
	 * Set the content provider for this viewer, must instantiate
	 * IHistogramProvider
	 */
	@Override
	public void setContentProvider(IContentProvider contentProvider) {
		Assert.isTrue(contentProvider instanceof IHistogramProvider);
		super.setContentProvider(contentProvider);
	}

	@Override
	public void refresh() {
		
		updateTraces();
		updateUIComponents();
		
	}
	
	private void updateUIComponents() {
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					updateUIComponents();
					
				}
			});
			return;
		}
		
		histogramPlottingSystem.repaint();
		
		rescaleAxis(firstUpdateTraces);
		if (firstUpdateTraces) {
			firstUpdateTraces = false;
		}
		
		double min = getHistogramProvider().getMin();
		double max = getHistogramProvider().getMax();
		if (!updatingROI) {
			updateRegion(min, max);
		}
		
		region.setVisible(true);
		updateMinMax(min, max);
	}
	
	private void updateMinMax(double min, double max) {
		minText.setText(Double.toString(min));
		maxText.setText(Double.toString(max));
	}

	/**
	 * Resets the histogram to its original conditions
	 */
	public void reset() {
		// Things to reset
		// 1. histo min
		// 2. histo max
		// 3. ROI
		// 4. scaling
		// 5. ????

		// Things to not reset
		// 1. colour map
		// 2. ???

		// do we need a restore defaults???
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		region.removeROIListener(histogramRegionListener);
		if (!histogramPlottingSystem.isDisposed()) {
			histogramPlottingSystem.dispose();
		}
		super.handleDispose(event);
	}

	/**
	 * Returns the histogram provider used by this widget, or <code>null</code>
	 * if no provider has been set yet.
	 *
	 * @return the histogram provider or <code>null</code> if none
	 */
	@Override
	public IHistogramProvider getContentProvider() {
		return (IHistogramProvider) super.getContentProvider();
	}

	/**
	 * Returns the histogramProvider for this class
	 *
	 * @return the histogram provider or <code>null</code> if none
	 */
	public IHistogramProvider getHistogramProvider() {
		return (IHistogramProvider) super.getContentProvider();
	}

	/**
	 * Get the plotting system associated with this histogram plot
	 *
	 * @return IPlottingSystem<Composite> the plotting system associated with this
	 *         histogram
	 */
	public IPlottingSystem<Composite> getHistogramPlot() {
		return histogramPlottingSystem;
	}

	@Override
	public Control getControl() {
		return composite;
	}

	/**
	 * Returns the parent composite
	 *
	 * @return composite
	 */
	public Composite getComposite() {
		return composite;
	}

	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub
	}

	public void rescaleAxis() {
		rescaleAxis(true);
	}

	private void rescaleAxis(boolean force) {
		if (force || histogramPlottingSystem.isRescale()) {
			IAxis xAxis = histogramPlottingSystem.getSelectedXAxis();
			IHistogramProvider hp = getHistogramProvider();
			double min = hp.getMin();
			double max = hp.getMax();
			histogramPlottingSystem.autoscaleAxes();
			xAxis.setRange(min, max);
		}
	}

	/**
	 * For test purposes only. Return the trace lines
	 */
	protected ILineTrace[] getRGBTraces() {
		return new ILineTrace[] { redTrace, greenTrace, blueTrace };
	}

	public void setFocus() {
		histogramPlottingSystem.setFocus();
	}
	
	private Double parseDouble(Text widget) {
		String text = widget.getText();
		
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			return null;
		}
		
	}
}
