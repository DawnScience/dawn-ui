package org.dawnsci.plotting.histogram.ui;

import java.util.Arrays;

import org.dawnsci.plotting.histogram.ExtensionPointManager;
import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.functions.ColourSchemeContribution;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class HistogramToolPage2 extends AbstractToolPage implements IToolPage {

	private FormToolkit toolkit;
	private ScrolledForm form;
	private CCombo colourMapCombo;
	
	private SelectionListener colourSchemeListener;


	private HistogramWidget histogramWidget;
	
	private IPaletteListener paletteListener;
	
	// HELPERS
	private ExtensionPointManager extensionPointManager;

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

		createImageSettings(form.getBody());
		createHistogramControl(form.getBody());

	}

	/*
	 * Create the image settings, i.e. colour scheme section
	 */
	private void createImageSettings(Composite comp) {
		Section section = toolkit.createSection(comp, Section.DESCRIPTION
				| Section.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
				.create());

		section.setText("Image Settings");
		section.setDescription("Colour scheme:");

		Composite colourComposite = toolkit.createComposite(section);
		colourComposite.setLayout(GridLayoutFactory.fillDefaults()
				.numColumns(2).create());
		colourComposite.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
		// Label label = toolkit.createLabel(colourComposite, "Colour Scheme:");
		colourMapCombo = new CCombo(colourComposite, SWT.FLAT | SWT.DROP_DOWN
				| SWT.READ_ONLY | SWT.BORDER);
		colourMapCombo.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
		toolkit.adapt(colourMapCombo);
		toolkit.paintBordersFor(colourComposite);
		section.setClient(colourComposite);
		
		initializeColourMap();
	}
	
	
	/*
	 * Initialize colour map combo with available colour maps
	 */
	private void initializeColourMap() {
		extensionPointManager = ExtensionPointManager.getManager();
		// TODO: 1. populate the colourMapCombo control from extension point as per HistogramToolPage

		for (ColourSchemeContribution contribution : extensionPointManager.getColourSchemeContributions()) {
			colourMapCombo.add(contribution.getName());
		}
		
		// TODO: 2. initialize the default selection to the specified colour map in preferences as per HistogramToolPage
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		final String schemeName = store.getString("org.dawb.plotting.system.colourSchemeName");
		colourMapCombo.select(Arrays.asList(colourMapCombo.getItems()).indexOf(schemeName));
		
		// TODO: 3. connect up a listener to react to changes in the colour map. Initially the colour map
		//			should be set on the image via the PaletteTrace. Don't worry about updating histogram yet. 
		paletteListener = new IPaletteListener.Stub(){
			@Override
			public void paletteChanged(PaletteEvent event) {
				//if (internalEvent > 0) return;
				logger.trace("paletteChanged");
				//paletteData = event.getPaletteData();
				//updateHistogramToolElements(event.getTrace(), null, false, false);
				
				IPaletteTrace trace = event.getTrace();
				String name = trace != null ? trace.getPaletteName() : null;
				if (name != null) {
					updateColourScheme(name);
				}
			}
		};
		
		colourSchemeListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				logger.trace("colourSchemeListener");
				setPaletteName();
				buildPaletteData();
				//updateHistogramToolElements(event, true, false);
			}
		};
		
		colourMapCombo.addSelectionListener(colourSchemeListener);
		
	}

	/*
	 * Create the histogram section
	 */
	private void createHistogramControl(Composite comp) {
		Section section = toolkit.createSection(comp, Section.DESCRIPTION
				| Section.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());

		section.setText("Histogram Plot");
		section.setDescription("Histogram information for active plot view");

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(GridLayoutFactory.fillDefaults().create());
		sectionClient.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());

		try {
			histogramWidget = new HistogramWidget(sectionClient, getTitle(),
					null, null);
		} catch (Exception e) {
			logger.error("Cannot locate any plotting systems!", e);
		}

		GridData create = GridDataFactory.fillDefaults().hint(0, 200)
				.grab(true, true).create();
		histogramWidget.setLayoutData(create);

		toolkit.adapt(histogramWidget);
		section.setClient(sectionClient);
	}

	@Override
	public void activate() {
		super.activate();
		
		IPaletteTrace image = getPaletteTrace();

		if (getPlottingSystem() != null) {
			generateHistogram(image);
		}
		
		if (image!=null) {
			image.addPaletteListener(paletteListener);
		}
		
	}

	private void inputChanged(IPaletteTrace oldImage, IPaletteTrace newImage) {
		// if(oldImage != null) {
		// removeListenerFrom(oldHistogramProvider);
		// }
		// if(newImage != null) {
		// addListenerTo(newHistogramProvider);
		// }
	}

	protected void addListenerTo(ImageHistogramProvider hProvider) {
		// box.addListener(this);
		// for (Iterator iterator = box.getBoxes().iterator();
		// iterator.hasNext();) {
		// MovingBox aBox = (MovingBox) iterator.next();
		// addListenerTo(aBox);
		// }
	}

	private void generateHistogram(IPaletteTrace image) {
		if (image == null)
			return;

		ImageHistogramProvider h = new ImageHistogramProvider(image);
		histogramWidget.setInput(h);
	}
	
	/**
	 * Update the colour scheme combo on this page
	 * @param schemeName colour scheme name
	 */
	private void updateColourScheme(String schemeName) {
		//if (updatingColorSchemeInternally)
			//return;
		if (colourMapCombo == null || colourMapCombo.isDisposed())
			return;
		if (schemeName == null)
			return;
		colourMapCombo.select(Arrays.asList(colourMapCombo.getItems()).indexOf(
				schemeName));
	}
	
	/**
	 * Use the controls from the GUI to set the individual colour elements from the selected colour scheme
	 */

	
	private void setPaletteName() {
		ColourSchemeContribution colourScheme = extensionPointManager.getColourSchemeContribution(colourMapCombo.getText());
		getPaletteTrace().setPaletteName(colourScheme.getName());
		
	}
	
	private void buildPaletteData() {

		// first get the appropriate bits from the extension points
		ColourSchemeContribution colourScheme = extensionPointManager.getColourSchemeContribution(colourMapCombo.getText());
	
		int[] red = extensionPointManager.getTransferFunctionFromID(colourScheme.getRedID()).getFunction().getArray();
		int[] green = extensionPointManager.getTransferFunctionFromID(colourScheme.getGreenID()).getFunction().getArray();
		int[] blue = extensionPointManager.getTransferFunctionFromID(colourScheme.getBlueID()).getFunction().getArray();

		PaletteData data = getPaletteTrace().getPaletteData();
		data.colors = new RGB[256];

		for (int i = 0; i < 256; i++) {
			data.colors[i] = new RGB(red[i], green[i], blue[i]);
		}
		getPaletteTrace().setPaletteData(data);
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
		form.setFocus();
	}

}
