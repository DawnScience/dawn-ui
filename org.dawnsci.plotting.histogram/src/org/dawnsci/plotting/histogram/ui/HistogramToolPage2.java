package org.dawnsci.plotting.histogram.ui;

import org.dawnsci.plotting.histogram.Activator;
import org.dawnsci.plotting.histogram.ColourMapProvider;
import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.preferences.HistogramPreferencePage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistogramToolPage2 extends AbstractToolPage implements IToolPage {

	private static final Logger logger = LoggerFactory.getLogger(HistogramToolPage2.class);

	private FormToolkit toolkit;
	private ScrolledForm form;
	private ComboViewer colourMapViewer;
	private IAction lockAction;

	private HistogramViewer histogramWidget;

	private ITraceListener traceListener = new TraceListener();

	private IPaletteListener paletteListener = new PaletteListener();

	private SelectionAdapter colourSchemeListener;

	private Button logScaleCheck;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		logger.debug("HistogramToolPage: createControl");
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.reflow(true); // create view with no scrollbars reflowing at this
							// point
		form.getBody().setLayout(GridLayoutFactory.fillDefaults().create());

		createImageSettings(form.getBody());

		createHistogramControl(form.getBody());

		createHistoActions();

		// track tool usage
		super.createControl(parent);
	}

	private void createHistoActions() {
		Action histoPref = new Action("Histogram Preferences...") {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), HistogramPreferencePage.ID,
						null, null);
				if (pref != null) pref.open();
			}
		};
		getSite().getActionBars().getMenuManager().add(histoPref);
		getSite().getActionBars().getMenuManager().add(new Separator());
	}

	/*
	 * Create the image settings, i.e. colour scheme section
	 */
	private void createImageSettings(Composite comp) {
		Section section = toolkit.createSection(comp, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		section.setText("Image Settings");
		section.setDescription("Colour scheme:");

		Composite colourComposite = toolkit.createComposite(section);
		colourComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		GridData layoutData = GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).grab(true, false).create();
		colourComposite.setLayoutData(layoutData);

		colourMapViewer = new ComboViewer(colourComposite, SWT.READ_ONLY);
		colourMapViewer.getControl().setLayoutData(layoutData);

		toolkit.adapt((Composite) colourMapViewer.getControl());
		section.setClient(colourComposite);

		colourMapViewer.setContentProvider(new ColourMapProvider());

		colourSchemeListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				logger.trace("colourSchemeListener");
				setPalette();
				// updateHistogramToolElements(event, true, false);
			}
		};
		((Combo) colourMapViewer.getControl()).addSelectionListener(colourSchemeListener);

		logScaleCheck = toolkit.createButton(colourComposite, "Log Scale", SWT.CHECK);
		logScaleCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IImageTrace image = getImageTrace();
				if (image != null) {
					// TODO: There should be a method on image to
					// setLogColorScale so that
					// it just does the right thing.
					image.getImageServiceBean().setLogColorScale(logScaleCheck.getSelection());
					if (image.isRescaleHistogram()) {
						image.rehistogram();
					} else {
						// XXX: Doing a image.repaint() is not sufficient, force
						// more
						// work to be done by resetting the palette data to what
						// it already has
						image.setPaletteData(image.getPaletteData());
					}
				}
			}

		});

	}

	private void setColourScheme(IPaletteTrace trace) {
		String name = trace != null ? trace.getPaletteName() : null;
		if (name != null) {
			updateColourSchemeCombo(name);
		}
	}

	/*
	 * Create the histogram section
	 */
	private void createHistogramControl(Composite comp) {
		Section section = toolkit.createSection(comp, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());

		section.setText("Histogram Plot");
		section.setDescription("Histogram information for active plot view");

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(GridLayoutFactory.fillDefaults().create());
		sectionClient.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		final IPageSite site = getSite();
		IActionBars actionBars = (site != null) ? site.getActionBars() : null;

		try {
			histogramWidget = new HistogramViewer(sectionClient, getTitle(), null, actionBars);
		} catch (Exception e) {
			logger.error("Cannot locate any plotting systems!", e);
		}

		createSectionToolbar(section);

		GridData create = GridDataFactory.fillDefaults().hint(0, 200)
				.grab(true, true).create();
		histogramWidget.getControl().setLayoutData(create);

		histogramWidget.setContentProvider(new ImageHistogramProvider());

		toolkit.adapt(histogramWidget.getComposite());
		section.setClient(sectionClient);
	}

	/**
	 * Create toolbar
	 *
	 * @param section
	 * @param toolkit
	 */
	private void createSectionToolbar(Section control) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(control);
		final Cursor handCursor = new Cursor(Display.getCurrent(),
				SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handCursor.dispose();
			}
		});

		Action rehistogramAndRescale = new Action(
				"Rehistogram and restore histogram plot to default zoom",
				IAction.AS_PUSH_BUTTON) {
			public void run() {
				IImageTrace imageTrace = getImageTrace();
				if (imageTrace != null) {
					imageTrace.rehistogram();
					histogramWidget.rescaleAxis();
				}
			}
		};
		rehistogramAndRescale.setImageDescriptor(Activator
				.getImageDescriptor("icons/reset.gif"));
		toolBarManager.add(rehistogramAndRescale);


		lockAction = new Action("Lock histogram range", IAction.AS_CHECK_BOX) {
			public void run() {
				IImageTrace image = getImageTrace();
				image.setRescaleHistogram(!isChecked());
			}
		};
		lockAction.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));
		toolBarManager.add(lockAction);


		toolBarManager.update(true);

		control.setTextClient(toolbar);
	}

	@Override
	public void activate() {
		super.activate();

		Assert.isTrue(getPlottingSystem() != null,
				"Plotting system must not be null");

		logger.debug("HistogramToolPage: activate. Plotting System "
				+ getPlottingSystem().hashCode());
		getPlottingSystem().addTraceListener(traceListener);

		IPaletteTrace paletteTrace = getPaletteTrace();
		if (paletteTrace != null) {
			paletteTrace.addPaletteListener(paletteListener);
			logger.debug("HistogramToolPage: activate - palette trace "
					+ paletteTrace.hashCode());
			updateHistogramUIElements(paletteTrace);
		} else {
			logger.debug("HistogramToolPage: activate - palette trace is null.");
		}

	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (getPlottingSystem() != null){
			logger.debug("HistogramToolPage: deactivate. Plotting System "
					+ getPlottingSystem().hashCode());	
			getPlottingSystem().removeTraceListener(traceListener);
		}

		//palette trace is not always set in the activate stage, so could be null
		if (getPaletteTrace() != null){
			getPaletteTrace().removePaletteListener(paletteListener);
		}	
	}

	/**
	 * Returns colour map ComboViewer for testing purposes
	 */
	protected ComboViewer getColourMapViewer() {
		return colourMapViewer;
	}

	/**
	 * Returns histogram viewer for testing purposes
	 */
	protected HistogramViewer getHistogramViewer() {
		return histogramWidget;
	}

	/**
	 * Returns lock action for testing purposes
	 */
	protected IAction getLockAction(){
		return lockAction;
	}

	/**
	 * Update the colour scheme combo on this page
	 *
	 * @param schemeName
	 *            colour scheme name
	 */
	private void updateColourSchemeCombo(String schemeName) {
		if (colourMapViewer == null)
			return;
		if (schemeName == null)
			return;
		colourMapViewer.setSelection(new StructuredSelection(schemeName), true);
	}

	/**
	 * Use the controls from the GUI to set the individual colour elements from
	 * the selected colour scheme
	 */
	private void setPalette() {
		IPaletteTrace paletteTrace = getPaletteTrace();
		if (paletteTrace != null) {
			paletteTrace.setPalette((String) ((StructuredSelection) colourMapViewer
							.getSelection()).getFirstElement());
		}
	}

	@Override
	public boolean isAlwaysSeparateView() {
		return true;
	}

	@Override
	public Control getControl() {
		return form;
	}

	@Override
	public void setFocus() {
		histogramWidget.setFocus();
	}

	/*
	 * Update all the Histogram UI elements, widgets etc. typically done when
	 * there is a new trace or trace has been modified.
	 */
	private void updateHistogramUIElements(IPaletteTrace it) {
		histogramWidget.setInput(it);
		colourMapViewer.setInput(it);
		setColourScheme(it);
	}

	private final class TraceListener implements ITraceListener {
		@Override
		public void traceWillPlot(TraceWillPlotEvent evt) {
			logger.debug("HistogramToolPage: traceWillPlotEvent");

			// use this event to modify the trace before we plot it
			// i.e. set colour palette etc
			// note the trace may include lots of null settings e.g. min/max

			// When we get this notification, the data is not ready in the trace
			// e.g. min gets set but not max/
			// we would have to turn off listeners or something
			IPaletteTrace it = (IPaletteTrace) evt.getSource();
			histogramWidget.setInput(it);
		}

		@Override
		public void tracesAdded(TraceEvent evt) {
			logger.debug("HistogramToolPage: tracesAdded");

		}

		@Override
		public void traceCreated(TraceEvent evt) {
			logger.debug("HistogramToolPage: traceCreated");

		}

		@Override
		public void traceUpdated(TraceEvent evt) {
			logger.debug("HistogramToolPage: traceUpdated");
			IPaletteTrace it = (IPaletteTrace) evt.getSource();
			updateHistogramUIElements(it);
		}

		@Override
		public void traceAdded(TraceEvent evt) {
			logger.debug("HistogramToolPage: traceAdded");
			IPaletteTrace it = (IPaletteTrace) evt.getSource();
			updateHistogramUIElements(it);
			it.addPaletteListener(paletteListener);
		}

		@Override
		public void traceRemoved(TraceEvent evt) {
			logger.debug("HistogramToolPage: traceRemoved");

		}

		@Override
		public void tracesUpdated(TraceEvent evt) {
			logger.debug("HistogramToolPage: tracesUpdated");

		}

		@Override
		public void tracesRemoved(TraceEvent evet) {
			logger.debug("HistogramToolPage: tracesRemoved");

		}

	};

	private final class PaletteListener extends IPaletteListener.Stub{
		@Override
		public void rescaleHistogramChanged(PaletteEvent evt) {
			boolean locked = !((IPaletteTrace)evt.getSource()).isRescaleHistogram();
			lockAction.setChecked(locked);
		}
	}

}
