/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.plotting.printing;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.dawb.workbench.plotting.printing.PrintSettings.Orientation;
import org.dawb.workbench.plotting.printing.PrintSettings.Scale;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class based on the preview SWT dialog example found in "Professional Java Interfaces with SWT/JFace" Jackwind Li
 * Guojie John Wiley & Sons 2005
 * 
 * @author Baha El Kassaby
 */
public class PlotPrintPreviewDialog extends Dialog {
	private Shell shell;
	private Display display;
	private Canvas canvas;
	private Printer printer;
	private PrintMargin margin;
	private Combo comboPrinterName;
	private Combo comboScale;
//	private Combo comboOrientation;
	private Button buttonAspectRatio;

	protected String printScaleText = "Scale";
	protected String printButtonText = "Print";
	protected String printToolTipText = "Print the plotting";
	protected String printerSelectText = "Select a printer";
	protected String printPreviewText = "Print preview";
	protected String orientationText = "Orientation";
	protected String resolutionText = "Resolution";
	protected String portraitText = "Portrait";
	protected String landscapeText = "Landscape";
	protected String defaultPrinterText = "Printer";
	
	private PrintSettings settings;
	private PrinterData[] printerNames;
	
	private Image image;
	private XYGraph xyGraph;
	private String fileName = "SDA plot";
	
	private static final Logger logger = LoggerFactory.getLogger(PlotPrintPreviewDialog.class);

	/**
	 * PlotPrintPreviewDialog constructor
	 * 
	 * @param viewerApp
	 *            the viewerApp object used to create the image
	 * @param device
	 *            the display device
	 * @param legendTable
	 *            the legend of the plot
	 * @param settings
	 *            The input PrintSettings. Will construct a default one if null.
	 */
	public PlotPrintPreviewDialog(XYGraph xyGraph, Display device, PrintSettings settings) {
		super(device.getActiveShell());
		this.display = device;		
		if (settings != null) {
			this.settings = settings.clone();
		} else {
			this.settings = new PrintSettings();
		}
		this.printer = new Printer(this.settings.getPrinterData());
		this.xyGraph = xyGraph;

		if(getPreferenceAspectRatio())
			image = xyGraph.getImage(getImageSizeRect(xyGraph));
		else
			image = xyGraph.getImage(getPrinterSizeRect(printer));
	}

	// Resize the image to the printer size
	private Rectangle getImageSizeRect(XYGraph xyGraph){
		int imageWidth = xyGraph.getBounds().width;
		int imageHeight = xyGraph.getBounds().height;
		int printWidth = printer.getBounds().width;
		Point screenDPI = Display.getCurrent().getDPI();
		Point printerDPI = printer.getDPI();
		float scaleFactorX = printerDPI.x / screenDPI.x;

		imageHeight = Math.round((printWidth*imageHeight)/imageWidth);
		imageWidth = printWidth;
		
		if(scaleFactorX==0){
			scaleFactorX = screenDPI.x/printerDPI.x;
			imageWidth = Math.round(imageWidth*scaleFactorX);
			imageHeight = Math.round(imageHeight*scaleFactorX);
		}
		else{
			imageWidth = Math.round(imageWidth/scaleFactorX);
			imageHeight = Math.round(imageHeight/scaleFactorX);
		}
		Rectangle rect = new Rectangle(0, 0, imageWidth, imageHeight);
		return rect;
	}

	// Resize the printer size if too big
	private Rectangle getPrinterSizeRect(Printer printer){
		int printWidth = printer.getBounds().width;
		int printHeight = printer.getBounds().width;
		Point screenDPI = Display.getCurrent().getDPI();
		Point printerDPI = printer.getDPI();
		float scaleFactorX = printerDPI.x / screenDPI.x;
		
		if(scaleFactorX==0){
			scaleFactorX = screenDPI.x/printerDPI.x;
			printWidth = Math.round(printWidth*scaleFactorX);
			printHeight = Math.round(printHeight*scaleFactorX);
		}
		else{
			printWidth = Math.round(printWidth/scaleFactorX);
			printHeight = Math.round(printHeight/scaleFactorX);
		}

		Rectangle rect = new Rectangle(0, 0, printWidth, printHeight);
		return rect;
	}

	/**
	 * Creates and then opens the dialog. Note that setting or getting whether
	 * to use portrait or not must be handled separately.
	 * 
	 * @return The new value of the PrintSettings.
	 */
	public PrintSettings open() {
		setPrinter(printer, settings.getScale().getValue());
		
		shell = new Shell(display.getActiveShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setText(printPreviewText);
		GridLayout previewGridLayout =new GridLayout(4, false);
		shell.setLayout(previewGridLayout);

		final Composite previewComposite = new Composite(shell, SWT.TOP);
		RowLayout previewLayout = new RowLayout();
		previewLayout.wrap=true;
		previewLayout.center=true;
		previewComposite.setLayout(previewLayout);


//		final Button buttonSelectPrinter = new Button(previewComposite, SWT.PUSH);
//		buttonSelectPrinter.setText(printerSelectText);
//		buttonSelectPrinter.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				PrintDialog dialog = new PrintDialog(shell);
//				// Prompts the printer dialog to let the user select a printer.
//				PrinterData printerData = dialog.open();
//				if (printerData == null) // the user cancels the dialog
//					return;
//				settings.setPrinterData(printerData);
//				// Loads the printer.
//				setPrinter(printer, settings.getScale().getValue());
//				// print the plot
//				print(printer, margin, settings);
//				shell.dispose();
//			}
//		});
		
		final Button buttonPrint = new Button(previewComposite, SWT.PUSH);
		buttonPrint.setText(printButtonText);
		buttonPrint.setToolTipText(printToolTipText);
		buttonPrint.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Printer printer = new Printer(settings.getPrinterData());
				print(printer, margin, settings);
				shell.dispose();
			}
		});

		Composite printerNameComposite = new Composite(previewComposite, SWT.BORDER);
		RowLayout printerNameLayout=new RowLayout();
		printerNameLayout.center=true;
		printerNameComposite.setLayout(printerNameLayout);
		new Label(printerNameComposite, SWT.BOTTOM).setText(defaultPrinterText + ":");
		comboPrinterName = new Combo(printerNameComposite, SWT.READ_ONLY);
		PrinterData[] printerList = Printer.getPrinterList();
		for (int i = 0; i < printerList.length; i++) {
			comboPrinterName.add(printerList[i].name);
		}
		comboPrinterName.select(getPreferencePrinterName());
		comboPrinterName.addSelectionListener(printerNameSelection);
		
		Composite scaleComposite = new Composite(previewComposite, SWT.BORDER);
		RowLayout scaleLayout=new RowLayout();
		scaleLayout.center=true;
		scaleComposite.setLayout(scaleLayout);
		new Label(scaleComposite, SWT.BOTTOM).setText(printScaleText + ":");
		comboScale = new Combo(scaleComposite, SWT.READ_ONLY);
		Scale[] scaleList = Scale.values();
		for (int i = 0; i < scaleList.length; i++) {
			comboScale.add(scaleList[i].getName());
		}
		comboScale.select(getPreferencePrintScale());
		comboScale.addSelectionListener(scaleSelection);

		Composite aspectRatioComposite = new Composite(previewComposite, SWT.BORDER);
		RowLayout aspectRatioLayout = new RowLayout();
		aspectRatioLayout.center=true;
		aspectRatioComposite.setLayout(aspectRatioLayout);
		buttonAspectRatio = new Button(aspectRatioComposite, SWT.CHECK);
		buttonAspectRatio.setText("Keep Aspect Ratio");
		buttonAspectRatio.setSelection(getPreferenceAspectRatio());
		buttonAspectRatio.addSelectionListener(aspectRatioListener);

		// TODO orientation button disabled: works for preview not for data sent to printer
//		Composite orientationComposite = new Composite(previewComposite, SWT.BORDER);
//		RowLayout orientationLayout=new RowLayout();
//		orientationLayout.center=true;
//		orientationComposite.setLayout(orientationLayout);
//		new Label(orientationComposite, SWT.NULL).setText(orientationText + ":");
//		comboOrientation = new Combo(orientationComposite, SWT.READ_ONLY);
//		comboOrientation.add(portraitText);
//		comboOrientation.add(landscapeText);
//		comboOrientation.select(getPreferencePrintOrientation());
//		comboOrientation.addSelectionListener(orientationSelection);

		canvas = new Canvas(shell, SWT.BORDER);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 4;
		canvas.setLayoutData(gridData);

		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				@SuppressWarnings("unused")
				Canvas canvas = null;
				switch (e.type) {
				case SWT.Paint:
					canvas = (Canvas) e.widget;
					paint(e, settings.getOrientation());
					break;
				}
			}
		};
		canvas.addListener(SWT.Paint, listener);

		shell.setSize(800, 650);
		shell.open();
		setPrinter(printer, settings.getScale().getValue());

		addPropertyListeners();
		
		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				// If no more entries in event queue
				shell.getDisplay().sleep();
			}
		}
		return settings;
	}
	
	private SelectionAdapter printerNameSelection = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			selectPrinter(comboPrinterName.getSelectionIndex());
			setPrinterNamePreference(comboPrinterName.getSelectionIndex());
		}
	};

	private void selectPrinter(int printerNameNum) {
		PrinterData[] printerList = Printer.getPrinterList();
		settings.setPrinterData(printerList[printerNameNum]);
		setPrinter(printer, settings.getScale().getValue());
		logger.info(printer.getPrinterData().name);
//		Rectangle imageSize = new Rectangle(0, 0, printer.getBounds().width, printer.getBounds().height);
//		image = xyGraph.getImage(imageSize);
	}

	private SelectionAdapter scaleSelection = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			selectScale(comboScale.getSelectionIndex());
			setScalePreference(comboScale.getSelectionIndex());
		}
	};

	private void selectScale(int scaleNum) {
		// ("100%", 0.5), ("75%", 2.0), ("66%", 3.0), ("50%", 4.0), ("33%", 5.0), ("25%", 6.0), ("10%", 7.0)
		switch (scaleNum){
		case 0:
			settings.setScale(Scale.DEFAULT);
			break;
		case 1:
			settings.setScale(Scale.PERCENT75);
			break;
		case 2:
			settings.setScale(Scale.PERCENT66);
			break;
		case 3:
			settings.setScale(Scale.PERCENT50);
			break;
		case 4:
			settings.setScale(Scale.PERCENT33);
			break;
		case 5:
			settings.setScale(Scale.PERCENT25);
			break;
		case 6:
			settings.setScale(Scale.PERCENT10);
			break;
		}	
		setPrinter(printer, settings.getScale().getValue());
	}

	private SelectionAdapter aspectRatioListener = new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {
			settings.setKeepAspectRatio(buttonAspectRatio.getSelection());
			setAspectRatioPreference(buttonAspectRatio.getSelection());
			// set aspect ratio
			if(buttonAspectRatio.getSelection()){
				image = xyGraph.getImage(getImageSizeRect(xyGraph));
			}else {
				image = xyGraph.getImage(getPrinterSizeRect(printer));
			}
			canvas.redraw();
		}
	};

//	private SelectionAdapter orientationSelection = new SelectionAdapter() {
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			selectOrientation(comboOrientation.getSelectionIndex());
//			setOrientationPreference(comboOrientation.getSelectionIndex());
//			
//		}
//	};

//	private void selectOrientation(int orientationNum) {
//		// "Portrait", "Landscape"
//		switch (orientationNum){
//		case 0:
//			settings.setOrientation(Orientation.PORTRAIT);
//			settings.getPrinterData().orientation = Orientation.PORTRAIT.getValue();
//			break;
//		case 1:
//			settings.setOrientation(Orientation.LANDSCAPE);
//			settings.getPrinterData().orientation = Orientation.LANDSCAPE.getValue();
//			break;
//		}
//		canvas.redraw();
//	}
	
	/**
	 * PlotPrintPreviewDialog is listening to eventual property changes done through the Preference Page
	 */
	private void addPropertyListeners() {
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		//final int index = store.getInt("printSettingsPreferencePage");
		
		store.addPropertyChangeListener(new IPropertyChangeListener() {

		//AnalysisRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				//IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
				ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
				if (property.equals(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME)
						|| property.equals(PrintingPrefValues.PRINTSETTINGS_SCALE)
						|| property.equals(PrintingPrefValues.PRINTSETTINGS_ORIENTATION)
						|| property.equals(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)) {

					int printerName;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME)) {
						printerName = preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME);
					} else {
						printerName = preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME);
					}
					printerNames = Printer.getPrinterList();
					for (int i = 0; i < printerNames.length; i++) {
						if(i==printerName){
							settings.setPrinterData(printerNames[i]);
							break;
						}
					}

					int scale;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_SCALE)) {
						scale = preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_SCALE);
					} else {
						scale = preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_SCALE);
					}
					Scale[] scales = Scale.values();
					for (int i = 0; i < scales.length; i++) {
						if(i==scale){
							settings.setScale(scales[i]);
							break;
						}
					}

					boolean keepAspectRatio;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)) {
						keepAspectRatio = preferenceStore.getDefaultBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO);
					} else {
						keepAspectRatio = preferenceStore.getBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO);
					}
					settings.setKeepAspectRatio(keepAspectRatio);

					int orientation;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ORIENTATION)) {
						orientation = preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_ORIENTATION);
					} else {
						orientation = preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_ORIENTATION);
					}
					Orientation[] orientations = Orientation.values();
					for (int i = 0; i < orientations.length; i++) {
						if(i==orientation){
							settings.setOrientation(orientations[i]);
							break;
						}
					}
				}
			}
		});
	}
	
	private int getPreferencePrinterName() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		return preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME)
				? preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME)
				: preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME);
	}
	
	private int getPreferencePrintScale() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		return preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_SCALE)
				? preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_SCALE)
				: preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_SCALE);
	}
	
	private boolean getPreferenceAspectRatio() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		return preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)
				? preferenceStore.getDefaultBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)
				: preferenceStore.getBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO);
	}

//	private int getPreferencePrintOrientation() {
//		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
//		return preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ORIENTATION)
//				? preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_ORIENTATION)
//				: preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_ORIENTATION);
//	}

	private void setPrinterNamePreference(int value) {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		settings.setPrinterData(Printer.getPrinterList()[value]);
		preferenceStore.setValue(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME, value);
	}
	
	private void setScalePreference(int value) {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		settings.setScale(Scale.values()[value]);
		preferenceStore.setValue(PrintingPrefValues.PRINTSETTINGS_SCALE, value);
	}

	private void setAspectRatioPreference(boolean value) {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		settings.setKeepAspectRatio(value);
		preferenceStore.setValue(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO, value);
	}
	
//	private void setOrientationPreference(int value) {
//		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
//		settings.setOrientation(Orientation.values()[value]);
//		preferenceStore.setValue(PrintingPrefValues.PRINTSETTINGS_ORIENTATION, value);
//	}
	
	private void paint(Event e, Orientation orientation) {
	
		int canvasBorder = 20;

		if (printer == null || printer.isDisposed())
			return;
		
		printer.getPrinterData().orientation = orientation.getValue();
			
		Rectangle printerBounds = printer.getBounds();
		Point canvasSize = canvas.getSize();
		
		double viewScaleFactor = (canvasSize.x - canvasBorder * 2) * 1.0 / printerBounds.width;
		viewScaleFactor = Math.min(viewScaleFactor, (canvasSize.y - canvasBorder * 2) * 1.0 / printerBounds.height);

		int offsetX = (canvasSize.x - (int) (viewScaleFactor * printerBounds.width)) / 2;
		int offsetY = (canvasSize.y - (int) (viewScaleFactor * printerBounds.height)) / 2;

		e.gc.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		// draws the page layout
		e.gc.fillRectangle(offsetX, offsetY, (int) (viewScaleFactor * printerBounds.width),
				(int) (viewScaleFactor * printerBounds.height));

		// draws the margin.
		e.gc.setLineStyle(SWT.LINE_DASH);
		e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		int marginOffsetX = offsetX + (int) (viewScaleFactor * margin.left);
		int marginOffsetY = offsetY + (int) (viewScaleFactor * margin.top);
//			 e.gc.drawRectangle(marginOffsetX, marginOffsetY, (int) (viewScaleFactor * (margin.right - margin.left)),
//			 (int) (viewScaleFactor * (margin.bottom - margin.top)));
		
//		Rectangle imageSize = new Rectangle(0, 0, printerBounds.width, printerBounds.height);
//		image = xyGraph.getImage(imageSize);
		
		if (image != null) {
			int imageWidth = image.getBounds().width;
			int imageHeight = image.getBounds().height;
			double dpiScaleFactorX = printer.getDPI().x * 1.0 / shell.getDisplay().getDPI().x;
			double dpiScaleFactorY = printer.getDPI().y * 1.0 / shell.getDisplay().getDPI().y;

			double imageSizeFactor = Math.min(1, (margin.right - margin.left) * 1.0
				/ (dpiScaleFactorX * imageWidth));
			imageSizeFactor = Math.min(imageSizeFactor, (margin.bottom - margin.top) * 1.0
				/ (dpiScaleFactorY * imageHeight));
			e.gc.drawImage(image, 0, 0, imageWidth, imageHeight, marginOffsetX, marginOffsetY,
				(int) (dpiScaleFactorX * imageSizeFactor * imageWidth * viewScaleFactor),
				(int) (dpiScaleFactorY * imageSizeFactor * imageHeight * viewScaleFactor));
		}
	}

	/**
	 * Sets target printer.
	 * 
	 * @param printer
	 */
	private void setPrinter(Printer printer, double marginSize) {
		if (printer == null) {
			printer = new Printer(Printer.getDefaultPrinterData());
		}
		this.printer = printer;
		margin = PrintMargin.getPrintMargin(printer, marginSize);
		if (canvas != null)
			canvas.redraw();
	}

//	/**
//	 * Lets the user to select a printer and prints the image on it.
//	 */
//	private void print() {
//		PrintDialog dialog = new PrintDialog(shell);
//		// Prompts the printer dialog to let the user select a printer.
//		PrinterData printerData = dialog.open();
//
//		if (printerData == null) // the user cancels the dialog
//			return;
//		// Loads the printer.
//		Printer printer = new Printer(printerData);
//		print(printer, null, settings);
//	}

	/**
	 * Prints the image current displayed to the specified printer.
	 * 
	 * @param printer
	 */
	private void print(final Printer printer, PrintMargin printMargin, final PrintSettings settings) {
		if (image == null) // If no image is loaded, do not print.
			return;

		final Point printerDPI = printer.getDPI();
		final Point displayDPI = display.getDPI();
		logger.info(getClass()+":"+ displayDPI + " " + printerDPI);

		final PrintMargin margin = (printMargin == null ? PrintMargin.getPrintMargin(printer, 1.0) : printMargin);

		Thread printThread = new Thread() {
			@Override
			public void run() {
				if (!printer.startJob(fileName)) {
					logger.info(getClass()+":"+ "Failed to start print job!");
					printer.dispose();
					return;
				}

				GC gc = new GC(printer);

				if (!printer.startPage()) {
					logger.info(getClass()+":"+ "Failed to start a new page");
					gc.dispose();
					return;
				} else {
					
					printer.getPrinterData().orientation = settings.getOrientation().getValue();

					int imageWidth = image.getBounds().width;
					int imageHeight = image.getBounds().height;
					// Handles DPI conversion.
					double dpiScaleFactorX = printerDPI.x * 1.0 / displayDPI.x;
					double dpiScaleFactorY = printerDPI.y * 1.0 / displayDPI.y;
					// If the image is too large to draw on a page, reduces its
					// width and height proportionally.
					double imageSizeFactor = Math.min(1, (margin.right - margin.left) * 1.0
							/ (dpiScaleFactorX * imageWidth));
					imageSizeFactor = Math.min(imageSizeFactor, (margin.bottom - margin.top) * 1.0
							/ (dpiScaleFactorY * imageHeight));
					// Draws the image to the printer.
					gc.drawImage(image, 0, 0, imageWidth, imageHeight, margin.left - 20, margin.top - 20,
							(int) (dpiScaleFactorX * imageSizeFactor * imageWidth), (int) (dpiScaleFactorY
									* imageSizeFactor * imageHeight));
					gc.dispose();

				}
				image.dispose();
				printer.endPage();
				printer.endJob();

				printer.dispose();
				logger.info(getClass()+":Printing job done!");
			}
		};
		printThread.start();
	}
}
