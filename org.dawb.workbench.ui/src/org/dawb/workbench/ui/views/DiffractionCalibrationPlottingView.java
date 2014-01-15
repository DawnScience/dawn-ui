/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.workbench.ui.views;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.diffraction.CalibrantPositioningWidget;
import org.dawb.workbench.ui.diffraction.DiffractionCalibrationConstants;
import org.dawb.workbench.ui.diffraction.DiffractionCalibrationUtils;
import org.dawb.workbench.ui.diffraction.table.DiffCalTableViewer;
import org.dawb.workbench.ui.diffraction.table.DiffractionTableData;
import org.dawb.workbench.ui.diffraction.table.TableChangedEvent;
import org.dawb.workbench.ui.diffraction.table.TableChangedListener;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.utils.RadioUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;
import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;

/**
 * This listens for a selected editor (of a diffraction image) and allows
 * 
 * 1) selection of calibrant
 * 2) movement, scaling and tilting of rings
 * 3) refinement of fit
 * 4) calibration (other images too?)
 *
 * Should display relevant metadata, allow a number of files to contribute to final calibration
 */
public class DiffractionCalibrationPlottingView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(DiffractionCalibrationPlottingView.class);

	private DiffractionTableData currentData;

	public static final String FORMAT_MASK = "##,##0.##########";

	private List<DiffractionTableData> model;
	private ILoaderService service;

	private Composite parent;
	private ScrolledComposite scrollComposite;
	private Composite scrollHolder;
	private Button calibrateImagesButton;
	private Combo calibrantCombo;
	private Group wavelengthComp;
	private Group calibOptionGroup;

	private IPlottingSystem plottingSystem;

	private ISelectionChangedListener selectionChangeListener;
	private IDetectorPropertyListener detectorPropertyListener;
	private CalibrantSelectedListener calibrantChangeListener;
	private TableChangedListener tableChangedListener;

	private List<String> pathsList = new ArrayList<String>();

	private FormattedText wavelengthFormattedText;
	private FormattedText energyFormattedText;

	private IToolPageSystem toolSystem;

	private String calibrantName;

	private CalibrantPositioningWidget calibrantPositioning;
	private DiffCalTableViewer diffractionTableViewer;

	private boolean usedFixedWavelength = true; // these two flags should match the default calibration action
	private boolean postFitWavelength = false;

	public DiffractionCalibrationPlottingView() {
		service = (ILoaderService) PlatformUI.getWorkbench().getService(ILoaderService.class);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		setSite(site);
		setPartName("Diffraction Calibration View");

		if (memento != null) {
			for (String k : memento.getAttributeKeys()) {
				if (k.startsWith(DiffractionCalibrationConstants.DATA_PATH)) {
					int i = Integer.parseInt(k.substring(DiffractionCalibrationConstants.DATA_PATH.length()));
					pathsList.add(i, memento.getString(k));
				}
				if (k.startsWith(DiffractionCalibrationConstants.CALIBRANT)) {
					calibrantName = memento.getString(k);
				}
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			int i = 0;
			for (TableItem t : diffractionTableViewer.getTable().getItems()) {
				DiffractionTableData data = (DiffractionTableData) t.getData();
				memento.putString(DiffractionCalibrationConstants.DATA_PATH + String.valueOf(i++), data.path);
			}
			memento.putString(DiffractionCalibrationConstants.CALIBRANT, calibrantCombo.getItem(calibrantCombo.getSelectionIndex()));
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());

		this.parent = parent;
		final Display display = parent.getDisplay();

		// selection change listener for table viewer
		selectionChangeListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection is = event.getSelection();
				if (is instanceof StructuredSelection) {
					StructuredSelection structSelection = (StructuredSelection) is;
					DiffractionTableData selectedData = (DiffractionTableData) structSelection.getFirstElement();
					if (selectedData == null || selectedData == currentData)
						return;
					drawSelectedData(selectedData);
				}
			}
		};

		detectorPropertyListener = new IDetectorPropertyListener() {
			@Override
			public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						diffractionTableViewer.refresh();
					}
				});
			}
		};

		calibrantChangeListener = new CalibrantSelectedListener() {
			@Override
			public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
				calibrantCombo.select(calibrantCombo.indexOf(evt.getCalibrant()));
			}
		};

		tableChangedListener = new TableChangedListener() {
			@Override
			public void tableChanged(TableChangedEvent event) {
				setWavelength(currentData);
				if (model.size() > 0)
					setXRaysModifiersEnabled(true);
			}
		};

		// main sash form which contains the left sash and the plotting system
		SashForm mainSash = new SashForm(parent, SWT.HORIZONTAL);
		mainSash.setBackground(new Color(display, 192, 192, 192));
		mainSash.setLayout(new FillLayout());

		// left sash form which contains the diffraction calibration controls
		// and the diffraction tool
		SashForm leftSash = new SashForm(mainSash, SWT.VERTICAL);
		leftSash.setBackground(new Color(display, 192, 192, 192));
		leftSash.setLayout(new GridLayout(1, false));
		leftSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Composite controlComp = new Composite(leftSash, SWT.NONE);
		controlComp.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(controlComp);
		createToolbarActions(controlComp);

		Label instructionLabel = new Label(controlComp, SWT.WRAP);
		instructionLabel.setText("Drag/drop a file/data to the table below, " +
				"choose a type of calibrant, " +
				"modify the rings using the positioning controls, " +
				"modify the wavelength/energy with the wanted values, " +
				"match rings to the image, " +
				"and select the calibration type before running the calibration process.");
		instructionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		Point pt = instructionLabel.getSize(); pt.x +=4; pt.y += 4; instructionLabel.setSize(pt);

		// make a scrolled composite
		scrollComposite = new ScrolledComposite(controlComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrollComposite.setLayout(new GridLayout(1, false));
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollHolder = new Composite(scrollComposite, SWT.NONE);

		GridLayout gl = new GridLayout(1, false);
		scrollHolder.setLayout(gl);
		scrollHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

		// table of images and found rings
		diffractionTableViewer = new DiffCalTableViewer(scrollHolder, pathsList, service);
		diffractionTableViewer.addSelectionChangedListener(selectionChangeListener);
		diffractionTableViewer.addTableChangedListener(tableChangedListener);
		model = diffractionTableViewer.getModel();

		Composite calibrantHolder = new Composite(scrollHolder, SWT.NONE);
		calibrantHolder.setLayout(new GridLayout(1, false));
		calibrantHolder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		Composite mainControlComp = new Composite(calibrantHolder, SWT.NONE);
		mainControlComp.setLayout(new GridLayout(2, false));
		mainControlComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		Composite leftCalibComp = new Composite(mainControlComp, SWT.NONE);
		leftCalibComp.setLayout(new GridLayout(1, false));
		leftCalibComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

		// create calibrant combo
		Composite selectCalibComp = new Composite(leftCalibComp, SWT.FILL);
		selectCalibComp.setLayout(new GridLayout(2, false));
		selectCalibComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label l = new Label(selectCalibComp, SWT.NONE);
		l.setText("Calibrant:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		calibrantCombo = new Combo(selectCalibComp, SWT.READ_ONLY);
		final CalibrationStandards standards = CalibrationFactory
				.getCalibrationStandards();
		calibrantCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentData == null)
					return;
				String calibrantName = calibrantCombo.getItem(calibrantCombo
						.getSelectionIndex());
				// update the calibrant in diffraction tool
				standards.setSelectedCalibrant(calibrantName, true);
				DiffractionCalibrationUtils
						.drawCalibrantRings(currentData.augmenter);
			}
		});
		for (String c : standards.getCalibrantList()) {
			calibrantCombo.add(c);
		}
		String s = standards.getSelectedCalibrant();
		if (s != null) {
			calibrantCombo.setText(s);
		}
		calibrantCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		calibrantPositioning = new CalibrantPositioningWidget(leftCalibComp, model);

		Composite rightCalibComp = new Composite(mainControlComp, SWT.NONE);
		rightCalibComp.setLayout(new GridLayout(1, false));
		rightCalibComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Radio group
		calibOptionGroup = new Group(rightCalibComp, SWT.BORDER);
		calibOptionGroup.setLayout(new GridLayout(1, false));
		calibOptionGroup.setText("Calibration options");
		try {
			RadioUtils.createRadioControls(calibOptionGroup, createWavelengthRadioActions());
		} catch (Exception e) {
			logger.error("Could not create controls:" + e);
		}
		calibrateImagesButton = new Button(calibOptionGroup, SWT.PUSH);
		calibrateImagesButton.setText("Run Calibration Process");
		calibrateImagesButton.setToolTipText("Calibrate detector in chosen images");
		calibrateImagesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		calibrateImagesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (model.size() <= 0)
					return;

				Job calibrateJob = DiffractionCalibrationUtils.calibrateImages(display, plottingSystem, model, currentData,
						usedFixedWavelength, postFitWavelength);
				if (calibrateJob == null)
					return;
				calibrateJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						display.asyncExec(new Runnable() {
							public void run() {
								refreshTable();
								double wavelength = currentData.md.getDiffractionCrystalEnvironment().getWavelength();
								int previousPrecision = BigDecimal.valueOf((Double)wavelengthFormattedText.getValue()).precision();
								wavelength = DiffractionCalibrationUtils.setPrecision(wavelength, previousPrecision);
								wavelengthFormattedText.setValue(wavelength);
								energyFormattedText.setValue(DiffractionCalibrationUtils.getWavelengthEnergy(wavelength));
								setCalibrateButtons();
							}
						});
					}
				});
				calibrateJob.schedule();
			}
		});
		setCalibrateOptionsEnabled(false);

		wavelengthComp = new Group(rightCalibComp, SWT.NONE);
		wavelengthComp.setText("X-Rays");
		wavelengthComp.setToolTipText("Set the wavelength / energy");
		wavelengthComp.setLayout(new GridLayout(3, false));
		wavelengthComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label wavelengthLabel = new Label(wavelengthComp, SWT.NONE);
		wavelengthLabel.setText("Wavelength");

		wavelengthFormattedText = new FormattedText(wavelengthComp, SWT.SINGLE | SWT.BORDER);
		wavelengthFormattedText.setFormatter(new NumberFormatter(FORMAT_MASK, FORMAT_MASK, Locale.UK));
		wavelengthFormattedText.getControl().setToolTipText("Set the wavelength in Angstrom");
		wavelengthFormattedText.getControl().addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// update wavelength of each image
				double distance = 0;
				Object obj = wavelengthFormattedText.getValue();
				if (obj instanceof Long)
					distance = ((Long) obj).doubleValue();
				else if (obj instanceof Double)
					distance = (Double) obj;
				for (int i = 0; i < model.size(); i++) {
					model.get(i).md.getDiffractionCrystalEnvironment().setWavelength(distance);
				}
				// update wavelength in keV
				double energy = DiffractionCalibrationUtils.getWavelengthEnergy(distance);
				if (energy != Double.POSITIVE_INFINITY) {
					String newFormat = DiffractionCalibrationUtils.getFormatMask(distance, energy);
					energyFormattedText.setFormatter(new NumberFormatter(FORMAT_MASK, newFormat, Locale.UK));
				}
				energyFormattedText.setValue(energy);
				// update wavelength in diffraction tool tree viewer
				NumericNode<Length> node = DiffractionCalibrationUtils.getDiffractionTreeNode(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, toolSystem);
				if (node.getUnit().equals(NonSI.ANGSTROM)) {
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, distance, toolSystem);
				} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, energy * 1000, toolSystem);
				} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, energy, toolSystem);
				}
			}
		});
		wavelengthFormattedText.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Label unitDistanceLabel = new Label(wavelengthComp, SWT.NONE);
		unitDistanceLabel.setText(NonSI.ANGSTROM.toString());

		Label energyLabel = new Label(wavelengthComp, SWT.NONE);
		energyLabel.setText("Energy");

		energyFormattedText = new FormattedText(wavelengthComp, SWT.SINGLE | SWT.BORDER);
		energyFormattedText.setFormatter(new NumberFormatter(FORMAT_MASK, FORMAT_MASK, Locale.UK));
		energyFormattedText.getControl().setToolTipText("Set the wavelength in keV");
		energyFormattedText.getControl().addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// update wavelength of each image
				double energy = 0;
				Object obj = energyFormattedText.getValue();
				if (obj instanceof Long)
					energy = ((Long) obj).doubleValue();
				else if (obj instanceof Double)
					energy = (Double) obj;
				for (int i = 0; i < model.size(); i++) {
					model.get(i).md.getDiffractionCrystalEnvironment().setWavelength(DiffractionCalibrationUtils.getWavelengthEnergy(energy));
				}
				// update wavelength in Angstrom
				double distance = DiffractionCalibrationUtils.getWavelengthEnergy(energy);
				if (distance != Double.POSITIVE_INFINITY) {
					String newFormat = DiffractionCalibrationUtils.getFormatMask(energy, distance);
					wavelengthFormattedText.setFormatter(new NumberFormatter(FORMAT_MASK, newFormat, Locale.UK));
				}
				wavelengthFormattedText.setValue(distance);
				// update wavelength in Diffraction tool tree viewer
				NumericNode<Length> node = DiffractionCalibrationUtils.getDiffractionTreeNode(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH,toolSystem);
				if (node.getUnit().equals(NonSI.ANGSTROM)) {
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, distance, toolSystem);
				} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, energy * 1000, toolSystem);
				} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, energy, toolSystem);
				}
			}
		});
		energyFormattedText.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Label unitEnergyLabel = new Label(wavelengthComp, SWT.NONE);
		unitEnergyLabel.setText(SI.KILO(NonSI.ELECTRON_VOLT).toString());
		// Enable/disable the modifiers
		setXRaysModifiersEnabled(false);

		scrollHolder.layout();
		scrollComposite.setContent(scrollHolder);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setMinSize(scrollHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrollComposite.layout();
		// end of Diffraction Calibration controls

		// start plotting system
		Composite plotComp = new Composite(mainSash, SWT.NONE);
		plotComp.setLayout(new GridLayout(1, false));
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		try {
			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComp, "", actionBarWrapper, PlotType.IMAGE, this);
			plottingSystem.setTitle("");
			plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e1) {
			logger.error("Could not create plotting system:" + e1);
		}

		// try to load the previous data saved in the memento
		DiffractionTableData good = null;
		for (String p : pathsList) {
			if (!p.endsWith(".nxs")) {
				DiffractionTableData d = diffractionTableViewer.createData(p, null);
				if (good == null && d != null) {
					good = d;
					setWavelength(d);
					setCalibrant();
				}
			}
		}
		diffractionTableViewer.refresh();
		if (good != null) {
			final DiffractionTableData g = good;
			display.asyncExec(new Runnable() { // this is necessary to give the plotting system time to lay out itself
				@Override
				public void run() {
					diffractionTableViewer.setSelection(new StructuredSelection(g));
				}
			});
		}
		if (model.size() > 0)
			setXRaysModifiersEnabled(true);

		// start diffraction tool
		Composite diffractionToolComp = new Composite(leftSash, SWT.BORDER);
		diffractionToolComp.setLayout(new FillLayout());
		try {
			toolSystem = (IToolPageSystem) plottingSystem.getAdapter(IToolPageSystem.class);
			// Show tools here, not on a page.
			toolSystem.setToolComposite(diffractionToolComp);
			toolSystem.setToolVisible(DiffractionCalibrationConstants.DIFFRACTION_ID, ToolPageRole.ROLE_2D, null);
		} catch (Exception e2) {
			logger.error("Could not open diffraction tool:" + e2);
		}
		// Set the various tools/controls for the calibrant positioning
		calibrantPositioning.setToolSystem(toolSystem);
		calibrantPositioning.setControlsToUpdate(calibOptionGroup, calibrateImagesButton);
		calibrantPositioning.setTableViewerToUpdate(diffractionTableViewer);

		CalibrationFactory.addCalibrantSelectionListener(calibrantChangeListener);
		// mainSash.setWeights(new int[] { 1, 2});
	}

	private void setCalibrateOptionsEnabled(boolean b) {
		calibOptionGroup.setEnabled(b);
		calibrateImagesButton.setEnabled(b);
	}

	private void setXRaysModifiersEnabled(boolean b) {
		wavelengthComp.setEnabled(b);
		wavelengthFormattedText.getControl().setEnabled(b);
		energyFormattedText.getControl().setEnabled(b);
	}

	private void createToolbarActions(Composite parent) {
		ToolBar tb = new ToolBar(parent, SWT.NONE);
		tb.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		Image exportImage = new Image(Display.getDefault(), Activator.getImageDescriptor("icons/page_white_excel.png").getImageData());
		Image resetRingsImage = new Image(Display.getDefault(), Activator.getImageDescriptor("icons/reset_rings.png").getImageData());
		Image resetImage = new Image(Display.getDefault(), Activator.getImageDescriptor("icons/table_delete.png").getImageData());
		ToolItem exportItem = new ToolItem(tb, SWT.PUSH);
		ToolItem resetRingsItem = new ToolItem(tb, SWT.PUSH);
		ToolItem resetItem = new ToolItem(tb, SWT.PUSH);

		Button exportButton = new Button(tb, SWT.PUSH);
		exportItem.setToolTipText("Export metadata to XLS");
		exportItem.setControl(exportButton);
		exportItem.setImage(exportImage);

		Button resetRingsButton = new Button(tb, SWT.PUSH);
		resetRingsItem.setToolTipText("Remove found rings");
		resetRingsItem.setControl(resetRingsButton);
		resetRingsItem.setImage(resetRingsImage);

		Button resetButton = new Button(tb, SWT.PUSH);
		resetItem.setToolTipText("Reset metadata");
		resetItem.setControl(resetButton);
		resetItem.setImage(resetImage);

		exportItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
				dialog.setText("Save metadata to Comma Separated Value file");
				dialog.setFilterNames(new String[] { "CSV Files", "All Files (*.*)" });
				dialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
				//dialog.setFilterPath("c:\\"); // Windows path
				dialog.setFileName("metadata.csv");
				dialog.setOverwrite(true);
				String savedFilePath = dialog.open();
				if (savedFilePath != null) {
					DiffractionCalibrationUtils.saveModelToCSVFile(model, savedFilePath);
				}
			}
		});

		resetRingsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				DiffractionCalibrationUtils.hideFoundRings(plottingSystem);
			}
		});

		resetItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// select last item in table
				if (model != null && model.size() > 0) {
					diffractionTableViewer.setSelection(new StructuredSelection(model.get(model.size() - 1)));
					for (int i = 0; i < model.size(); i++) {
						// Restore original metadata
						DetectorProperties originalProps = model.get(i).md.getOriginalDetector2DProperties();
						DiffractionCrystalEnvironment originalEnvironment = model.get(i).md.getOriginalDiffractionCrystalEnvironment();
						model.get(i).md.getDetector2DProperties().restore(originalProps);
						model.get(i).md.getDiffractionCrystalEnvironment().restore(originalEnvironment);
					}
					// update diffraction tool viewer
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.BEAM_CENTRE_XPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[0], toolSystem);
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.BEAM_CENTRE_YPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[1], toolSystem);
					DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getBeamCentreDistance(), toolSystem);

					// update wavelength
					double wavelength = currentData.md.getDiffractionCrystalEnvironment().getWavelength();
					energyFormattedText.setValue(DiffractionCalibrationUtils.getWavelengthEnergy(wavelength));
					wavelengthFormattedText.setValue(wavelength);
					// update wavelength in diffraction tool tree viewer
					NumericNode<Length> node = DiffractionCalibrationUtils.getDiffractionTreeNode(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, toolSystem);
					if (node.getUnit().equals(NonSI.ANGSTROM)) {
						DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, wavelength, toolSystem);
					} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
						DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, DiffractionCalibrationUtils.getWavelengthEnergy(wavelength) * 1000, toolSystem);
					} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
						DiffractionCalibrationUtils.updateDiffTool(DiffractionCalibrationConstants.WAVELENGTH_NODE_PATH, DiffractionCalibrationUtils.getWavelengthEnergy(wavelength), toolSystem);
					}
					diffractionTableViewer.refresh();
				}
			}
		});
	}

	private List<Action> createWavelengthRadioActions() {
		List<Action> radioActions = new ArrayList<Action>();

		Action usedFixedWavelengthAction = new Action() {
			@Override
			public void run() {
				usedFixedWavelength = true;
				postFitWavelength = false;
			}
		};
		usedFixedWavelengthAction.setText("Per Image Fit with fixed wavelength");
		usedFixedWavelengthAction.setToolTipText("Individual fit with fixed wavelength"); // TODO write a more detailed tool tip

		Action simultaneousFitAction = new Action() {
			@Override
			public void run() {
				usedFixedWavelength = false;
			}
		};
		simultaneousFitAction.setText("Fit wavelength and distance");
		simultaneousFitAction.setToolTipText("Fits all the parameters at once"); // TODO write a more detailed tool tip

		Action postWavelengthAction = new Action() {
			@Override
			public void run() {
				usedFixedWavelength = true;
				postFitWavelength = true;
			}
		};
		postWavelengthAction.setText("Per Image Fit then refine wavelength");
		postWavelengthAction.setToolTipText("Per image fit, then refine wavelength"); // TODO write a more detailed tool tip

		radioActions.add(usedFixedWavelengthAction);
		radioActions.add(simultaneousFitAction);
		radioActions.add(postWavelengthAction);

		return radioActions;
	}

	private void setWavelength(DiffractionTableData data) {
		// set the wavelength
		if (data != null) {
			double wavelength = data.md.getOriginalDiffractionCrystalEnvironment().getWavelength();
			wavelengthFormattedText.setValue(wavelength);
			double energy = DiffractionCalibrationUtils.getWavelengthEnergy(wavelength);
			if (energy != Double.POSITIVE_INFINITY) {
				energyFormattedText.setFormatter(new NumberFormatter(FORMAT_MASK, 
						DiffractionCalibrationUtils.getFormatMask(wavelength, energy), Locale.UK));
			}
			energyFormattedText.setValue(energy);
		}
	}

	private void setCalibrant() {
		// set the calibrant
		CalibrationStandards standard = CalibrationFactory.getCalibrationStandards();
		if (calibrantName != null) {
			calibrantCombo.select(calibrantCombo.indexOf(calibrantName));
			standard.setSelectedCalibrant(calibrantName, true);
		} else {
			calibrantCombo.select(calibrantCombo.indexOf(standard.getSelectedCalibrant()));
		}
	}

	private void drawSelectedData(DiffractionTableData data) {
		if (currentData != null) {
			DiffractionImageAugmenter aug = currentData.augmenter;
			if (aug != null)
				aug.deactivate(service.getLockedDiffractionMetaData()!=null);
		}

		if (data.image == null)
			return;

		plottingSystem.clear();
		plottingSystem.updatePlot2D(data.image, null, null);
		plottingSystem.setTitle(data.name);
		plottingSystem.getAxes().get(0).setTitle("");
		plottingSystem.getAxes().get(1).setTitle("");
		plottingSystem.setKeepAspect(true);
		plottingSystem.setShowIntensity(false);

		currentData = data;

		// update the diffraction data on the calibrant positioning widget
		calibrantPositioning.setDiffractionData(currentData);

		//Data has its own augmenters so disable the tool augmenter
		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DiffractionCalibrationConstants.DIFFRACTION_ID);
		DiffractionImageAugmenter toolAug = diffTool.getAugmenter();
		if (toolAug != null) toolAug.deactivate(service.getLockedDiffractionMetaData()!=null);
		
		DiffractionImageAugmenter aug = data.augmenter;
		if (aug == null) {
			aug = new DiffractionImageAugmenter(plottingSystem);
			data.augmenter = aug;
		}
		aug.activate();
		if (data.md != null) {
			aug.setDiffractionMetadata(data.md);
			// Add listeners to monitor metadata changes in diffraction tool
			data.md.getDetector2DProperties().addDetectorPropertyListener(detectorPropertyListener);
//			data.md.getDiffractionCrystalEnvironment().addDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
		}
		DiffractionCalibrationUtils.hideFoundRings(plottingSystem);
		DiffractionCalibrationUtils.drawCalibrantRings(aug);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class key) {
		if (key == IPlottingSystem.class) {
			return plottingSystem;
		} else if (key == IToolPageSystem.class) {
			return plottingSystem.getAdapter(IToolPageSystem.class);
		}
		return super.getAdapter(key);
	}

	private void refreshTable() {
		if (diffractionTableViewer == null)
			return;
		diffractionTableViewer.refresh();
		// reset the scroll composite
		Rectangle r = scrollHolder.getClientArea();
		scrollComposite.setMinSize(scrollHolder.computeSize(r.width, SWT.DEFAULT));
		scrollHolder.layout();
	}

	private void setCalibrateButtons() {
		// enable/disable calibrate button according to use column
		int used = 0;
		for (DiffractionTableData d : model) {
			if (d.use && d.nrois > 0) {
				used++;
			}
		}
		setCalibrateOptionsEnabled(used > 0);
	}

	private void removeListeners() {
		if(diffractionTableViewer != null) {
			diffractionTableViewer.removeSelectionChangedListener(selectionChangeListener);
			diffractionTableViewer.removeTableChangedListener(tableChangedListener);
		}
		CalibrationFactory.removeCalibrantSelectionListener(calibrantChangeListener);
		// deactivate the diffraction tool
		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DiffractionCalibrationConstants.DIFFRACTION_ID);
		if (diffTool != null)
			diffTool.deactivate();
		// deactivate each augmenter in loaded data
		if (model != null) {
			for (DiffractionTableData d : model) {
				if (d.augmenter != null)
					d.augmenter.deactivate(service.getLockedDiffractionMetaData()!=null);
				diffractionTableViewer.removeDetectorPropertyListener(d);
			}
			model.clear();
		}
		logger.debug("model emptied");
	}

	@Override
	public void dispose() {
		super.dispose();
		removeListeners();
		// FIXME Clear
	}

	@Override
	public void setFocus() {
		if (parent != null && !parent.isDisposed())
			parent.setFocus();
	}
}
