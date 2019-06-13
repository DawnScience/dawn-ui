package org.dawnsci.plotting.histogram.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.plotting.histogram.Activator;
import org.dawnsci.plotting.histogram.ColourMapProvider;
import org.dawnsci.plotting.histogram.ExtensionPointManager;
import org.dawnsci.plotting.histogram.HistoCategoryProvider;
import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.functions.ColourCategoryContribution;
import org.dawnsci.plotting.histogram.preferences.HistogramPreferencePage;
import org.dawnsci.plotting.histogram.service.PaletteService;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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

	public static final String ID = "org.dawnsci.plotting.histogram.histogram_tool_page_2";

	private static final Logger logger = LoggerFactory.getLogger(HistogramToolPage2.class);

	private FormToolkit toolkit;
	private ScrolledForm form;
	private ComboViewer colourMapViewer;
	private ComboViewer categoryViewer;
	private Button logScaleCheck;
	private Button invertedCheck;
	private SelectionAdapter colourCategoryListener;
	private Job updateJob;

	private IAction lockAction;

	private HistogramViewer histogramWidget;

	private ITraceListener traceListener = new TraceListener();

	private IPaletteListener paletteListener = new PaletteListener();

	private SelectionAdapter colourSchemeListener;

	private IPaletteService pservice;
	
	private AtomicReference<IPaletteTrace> activePaletteTrace = new AtomicReference<IPaletteTrace>(null);

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.reflow(true); // create view with no scrollbars reflowing at this
							// point
		form.getBody().setLayout(GridLayoutFactory.fillDefaults().create());
		
		updateJob =  new Job("Histo update") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPaletteTrace t = activePaletteTrace.get();
				if (t == null) return Status.OK_STATUS;
				histogramWidget.setInput(t);
				return Status.OK_STATUS;
			}
		};
		
		updateJob.setPriority(Job.INTERACTIVE);
		
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
		Section section = toolkit.createSection(comp, Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		section.setText("Image Settings");
		section.setDescription("Colour scheme:");

		Composite colourComposite = toolkit.createComposite(section);
		colourComposite.setLayout(new GridLayout(2, false));
		
		GridData layoutData = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create();
		colourComposite.setLayoutData(layoutData);

		Label categoryLabel = new Label(colourComposite, SWT.RIGHT);
		categoryLabel.setText("Category:");
		categoryViewer = new ComboViewer(colourComposite, SWT.READ_ONLY);
		categoryViewer.getControl().setLayoutData(layoutData);
		toolkit.adapt((Composite) categoryViewer.getControl());
		section.setClient(colourComposite);
		categoryViewer.setContentProvider(new HistoCategoryProvider());
		colourCategoryListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String category = categoryViewer.getElementAt(categoryViewer.getCombo().getSelectionIndex()).toString();
				List<String> colours = getPaletteService().getColorsByCategory(category);
				colourMapViewer.setInput(colours.toArray());
				colourMapViewer.setSelection(new StructuredSelection(colours.get(0)), true);
				for(String desc : getCategoryDescriptions()) {
					if (category.equals(desc)) {
						categoryViewer.getControl().setToolTipText(desc);
					}
				}
				setPalette();
			}
		};
		((Combo) categoryViewer.getControl()).addSelectionListener(colourCategoryListener);
		
		categoryViewer.setInput(getCategories());

		Label colourLabel = new Label(colourComposite, SWT.RIGHT);
		colourLabel.setText("Colormap:");
		colourMapViewer = new ComboViewer(colourComposite, SWT.READ_ONLY);
		colourMapViewer.getControl().setLayoutData(layoutData);
		toolkit.adapt((Composite) colourMapViewer.getControl());
		section.setClient(colourComposite);
		colourMapViewer.setContentProvider(new ColourMapProvider());
		colourMapViewer.getControl().setToolTipText("Type a colormap name and the list will auto-complete with available colormaps, press 'ENTER' to select");
		colourSchemeListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setPalette();
			}
		};
		((Combo) colourMapViewer.getControl()).addSelectionListener(colourSchemeListener);
		List<String> colours = new ArrayList<String>(getPaletteService().getColorSchemes());
		// Add content proposal to combo and a key listener
		String[] proposals = new String[colours.size()];
		for (int i = 0; i < proposals.length; i++) {
			proposals[i] = colours.get(i);
		}
		new AutoCompleteField(colourMapViewer.getCombo(), new ComboContentAdapter(), proposals);
		colourMapViewer.getCombo().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
					setPalette();
			}
		});

		logScaleCheck = toolkit.createButton(colourComposite, "Log Scale", SWT.CHECK);
		logScaleCheck.setSelection(Activator.getPlottingPreferenceStore().getBoolean(PlottingConstants.CM_LOGSCALE));
		logScaleCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				IPaletteTrace p = activePaletteTrace.get();
				
				if (p != null) {
					// TODO: There should be a method on image to
					// setLogColorScale so that
					// it just does the right thing.
					p.getImageServiceBean().setLogColorScale(logScaleCheck.getSelection());
					if (p.isRescaleHistogram() && activePaletteTrace instanceof IImageTrace) {
						((IImageTrace)activePaletteTrace).rehistogram();
					} else {
						// XXX: Doing a image.repaint() is not sufficient, force
						// more
						// work to be done by resetting the palette data to what
						// it already has
						p.setPaletteData(p.getPaletteData());
					}
				}
				Activator.getPlottingPreferenceStore().setValue(PlottingConstants.CM_LOGSCALE, logScaleCheck.getSelection());
			}

		});
		
		invertedCheck = toolkit.createButton(colourComposite, "Inverted", SWT.CHECK);
		// get the value from the preference store
		invertedCheck.setSelection(Activator.getPlottingPreferenceStore().getBoolean(PlottingConstants.CM_INVERTED));
		invertedCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (activePaletteTrace != null) {
					getPaletteService().setInverted(invertedCheck.getSelection());
					setPalette();
				}
				// store value in preferences
				Activator.getPlottingPreferenceStore().setValue(PlottingConstants.CM_INVERTED, invertedCheck.getSelection());
			}
		});
	}

	private String[] getCategories() {
		ExtensionPointManager manager = ExtensionPointManager.getManager();
		List<ColourCategoryContribution> cContrib = manager.getColourCategoryContributions();
		String[] names = new String[cContrib.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = cContrib.get(i).getName();
		}
		return names;
	}

	private String[] getCategoryDescriptions() {
		ExtensionPointManager manager = ExtensionPointManager.getManager();
		List<ColourCategoryContribution> cContrib = manager.getColourCategoryContributions();
		String[] names = new String[cContrib.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = cContrib.get(i).getDescription();
		}
		return names;
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
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

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

		GridData create = GridDataFactory.fillDefaults().hint(0, 200).grab(true, true).create();
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
				
				if (activePaletteTrace != null && activePaletteTrace instanceof IImageTrace) {
					((IImageTrace)activePaletteTrace).rehistogram();
					histogramWidget.rescaleAxis();
				}
			}
		};
		rehistogramAndRescale.setImageDescriptor(Activator
				.getImageDescriptor("icons/reset.gif"));
		toolBarManager.add(rehistogramAndRescale);


		lockAction = new Action("Lock histogram range", IAction.AS_CHECK_BOX) {
			public void run() {
				IPaletteTrace p = activePaletteTrace.get();
				if (p == null) return;
				p.setRescaleHistogram(!isChecked());
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

		Assert.isTrue(getPlottingSystem() != null, "Plotting system must not be null");

		logger.debug("HistogramToolPage: activate. Plotting System "
				+ getPlottingSystem().hashCode());
		getPlottingSystem().addTraceListener(traceListener);

		IPaletteTrace p = getLastPaletteTrace();
		
		activePaletteTrace.set(p);
		if (p != null) {
			p.addPaletteListener(paletteListener);
			logger.debug("HistogramToolPage: activate - palette trace " + p.hashCode());
			updateHistogramUIElements(p);
		} else {
			updateHistogramUIElements(null);
			logger.debug("HistogramToolPage: activate - palette trace is null.");
		}

	}

	private IPaletteTrace getLastPaletteTrace() {
		
		return getLastTrace(IPaletteTrace.class);
		
	}
	
	private <T extends ITrace> T getLastTrace(Class<T> clazz) {
		
		IPlottingSystem<?> system = getPlottingSystem();
		if (system == null) return null;
		
		Collection<T> ts = system.getTracesByClass(clazz);
		if (ts.isEmpty()) return null;
		
		T t = null;
		
		Iterator<T> iterator = ts.iterator();
		
		while (iterator.hasNext()) t = iterator.next();
		
		return t;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (getPlottingSystem() != null){
			logger.debug("HistogramToolPage: deactivate. Plotting System " + getPlottingSystem().hashCode());
			getPlottingSystem().removeTraceListener(traceListener);
			
			Collection<IPaletteTrace> traces = getPlottingSystem().getTracesByClass(IPaletteTrace.class);
			
			if (traces != null) {
				for (IPaletteTrace p : traces) {
					p.removePaletteListener(paletteListener);
				}
			}
			
			if (histogramWidget != null && histogramWidget.getContentProvider() != null) {
				histogramWidget.getContentProvider().dispose();
			}
		}
		

		//palette trace is not always set in the activate stage, so could be null
//		if (getPaletteTrace() != null){
//			getPaletteTrace().removePaletteListener(paletteListener);
//		}	
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
		String category = getPaletteService().getColorCategory(schemeName);
		categoryViewer.setSelection(new StructuredSelection(category), true);

		List<String> colours = getPaletteService().getColorsByCategory(category);
		colourMapViewer.setInput(colours.toArray());
		colourMapViewer.setSelection(new StructuredSelection(schemeName), true);
	}

	/**
	 * Use the controls from the GUI to set the individual colour elements from
	 * the selected colour scheme
	 */
	private void setPalette() {
		IPaletteTrace p = activePaletteTrace.get();
		if (p != null) {
			String selectedColormap = colourMapViewer.getCombo().getText();
			p.setPalette(selectedColormap);
		}
	}

	@Override
	public boolean isAlwaysSeparateView() {
		// TODO fix when set to true: does not work for e4
		return false;
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
	private void updateHistogramUIElements(final IPaletteTrace it) {
		if (it == null && histogramWidget != null) {
			histogramWidget.clear();
		}
		
		if (histogramWidget != null && it != null && updateJob != null) {
			updateJob.schedule();
		}
			
		int categoryIdx  = categoryViewer.getCombo().getSelectionIndex();
		if (categoryIdx < 0)
			categoryIdx = 0;
		String category = categoryViewer.getCombo().getItem(categoryIdx);

		List<String> colours = getPaletteService().getColorsByCategory(category);
		colourMapViewer.setInput(colours.toArray());
		setColourScheme(it);
		if (lockAction != null && it != null) lockAction.setChecked(!it.isRescaleHistogram());
	}

	private final class TraceListener implements ITraceListener {
		@Override
		public void traceWillPlot(TraceWillPlotEvent evt) {
		}

		@Override
		public void tracesAdded(TraceEvent evt) {
		}

		@Override
		public void traceCreated(TraceEvent evt) {
		}

		@Override
		public void traceUpdated(TraceEvent evt) {
			if (!(evt.getSource() instanceof IPaletteTrace)) return;
			IPaletteTrace it = (IPaletteTrace) evt.getSource();
			activePaletteTrace.set(it);
			updateHistogramUIElements(it);
		}

		@Override
		public void traceAdded(TraceEvent evt) {
			IPaletteTrace p = (IPaletteTrace) evt.getSource();
			activePaletteTrace.set(p);
			updateHistogramUIElements(p);
			if (p != null) p.addPaletteListener(paletteListener);
		}

		@Override
		public void traceRemoved(TraceEvent evt) {
		}

		@Override
		public void tracesUpdated(TraceEvent evt) {
		}

		@Override
		public void tracesRemoved(TraceEvent evet) {
		}

	};

	private final class PaletteListener extends IPaletteListener.Stub{
		@Override
		public void rescaleHistogramChanged(PaletteEvent evt) {
			boolean locked = !((IPaletteTrace)evt.getSource()).isRescaleHistogram();
			lockAction.setChecked(locked);
		}

		@Override
		public void paletteChanged(PaletteEvent event) {
			// do not call if All category is selected
			String categorySelected = categoryViewer.getElementAt(categoryViewer.getCombo().getSelectionIndex()).toString();
			if (categorySelected.equals("All"))
				return;
			IPaletteTrace trace = event.getTrace();
			setColourScheme(trace);
		}
	}

	private IPaletteService getPaletteService() {
		if (pservice == null)
			return pservice = (IPaletteService) PaletteService.getPaletteService();
		return pservice;
	}

	@Override
	public String getToolId() {
		return ID;
	}
}
