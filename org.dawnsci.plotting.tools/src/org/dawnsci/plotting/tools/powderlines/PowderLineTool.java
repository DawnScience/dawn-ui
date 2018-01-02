/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.powderlines;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.dawnsci.plotting.tools.powderlines.PowderLinesModel.PowderLineCoord;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PowderLineTool extends AbstractToolPage {

	private Composite composite; // root Composite of the tool
	private TableViewer lineTableViewer; // TableViewer holding the list of lines
	private ITraceListener tracerListener; // The trace on which the tool listens
	private PowderLinesModel.PowderLineCoord plotCoordinate = PowderLinesModel.PowderLineCoord.Q; // The coordinate of the input data
	private List<IRegion> currentLineRegions;
	
	private TableViewer manyLineTV; // TableViewer holding the list of all lines
	
	private Set<PowderLinesModel> materialModels;
	
	private SashForm sashForm;
	// sub composites, needed to set the relative size for the different domains
	private Composite mTableCompo;

	private ModelsDetailsComposite modelsDetailsCompo;
	
	protected PowderLinesModel model;
	
	private double pressure;
	
	static final PowderLinesModel.PowderLineCoord defaultCoords = PowderLineCoord.D_SPACING;
	
	public enum PowderDomains {
			POWDER,
			EQUATION_OF_STATE;
	}
	
	public PowderLineTool() {
		try{
			this.tracerListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent event) {
					if (!(event.getSource() instanceof List<?>)) {
						return;
					}
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
		
		model = new PowderLinesModel();
		// Default data
		model.clearLines();
		model.setEnergy(76.6);
		
		materialModels = new HashSet<>();
	}
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void createControl(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		
		// Add a SashForm to show both the table and the domain specific pane
		sashForm = new SashForm(composite, SWT.VERTICAL);
		
		// Create the Actions
		createActions();
		
		// Create the table of all lines of all materials
		mTableCompo = new Composite(sashForm, SWT.NONE);
		manyLineTV = new TableViewer(mTableCompo, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createManyColumns(manyLineTV);
		
		manyLineTV.getTable().setLinesVisible(true);
		manyLineTV.getTable().setHeaderVisible(true);
		
		manyLineTV.setContentProvider(new ManyLineCP());
		manyLineTV.setInput(materialModels);
		
		modelsDetailsCompo = new ModelsDetailsComposite(sashForm, SWT.BORDER);
		
		activate();
		
		super.createControl(parent);
	}
	
	@Override
	public void setFocus() {
		// set the viewer focus
	}
	
	/**
	 * Activate the tool.
	 */
	public void activate() {
		
		// Add the traceListener
		if (getPlottingSystem() != null) {
			getPlottingSystem().addTraceListener(tracerListener);
		}
		
		super.activate();
	}
	
	/**
	 * Deactivate the tool.
	 */
	public void deactivate() {
		// Clear the lines on exit
		model.clearLines();
		drawPowderLines(new HashSet<PowderLinesModel>());
		
		super.deactivate();
		
		// Remove the traceListener
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(tracerListener);
		}
	}
	
	private void createManyColumns(final TableViewer viewer) {
		TableColumnLayout tcl = new TableColumnLayout();
		viewer.getControl().getParent().setLayout(tcl);
		
		
		// Create the columns
		TableViewerColumn colvarTheMagnificent;
		int iCol = 0;
		colvarTheMagnificent = new TableViewerColumn(viewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("d spacing (Å)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new LineModelLabelProvider(PowderLinesModel.PowderLineCoord.D_SPACING));
		tcl.setColumnData(colvarTheMagnificent.getColumn(), new ColumnWeightData(1));
		
		colvarTheMagnificent = new TableViewerColumn(viewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("Q (Å⁻¹)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new LineModelLabelProvider(PowderLinesModel.PowderLineCoord.Q));
		tcl.setColumnData(colvarTheMagnificent.getColumn(), new ColumnWeightData(1));
		
		colvarTheMagnificent = new TableViewerColumn(viewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("2θ (°)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new LineModelLabelProvider(PowderLinesModel.PowderLineCoord.ANGLE));
		tcl.setColumnData(colvarTheMagnificent.getColumn(), new ColumnWeightData(1));

	}
	
	private class LineModelLabelProvider extends ColumnLabelProvider {
		private PowderLinesModel.PowderLineCoord columnCoordinate;
		private DecimalFormat format = new DecimalFormat("#.###");

		public LineModelLabelProvider(PowderLinesModel.PowderLineCoord columnCoordinate) {
			this.columnCoordinate = columnCoordinate;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof PowderLineModel) {
				return format.format(((PowderLineModel) element).get(columnCoordinate));
			} else {
				return element.toString();
			}
		}
	
	}
	
	private void setCoords(PowderLinesModel.PowderLineCoord coord) {
		this.plotCoordinate = coord;
	}
	
	protected void setModel(PowderLinesModel model) {
		this.model = model;
		if (this.lineTableViewer != null)
			lineTableViewer.setInput(model.getLines());
			
	}
	
	protected void addMaterialModel(PowderLinesModel model) {
		materialModels.add(model);
		this.manyLineTV.setInput(this.materialModels);
		modelsDetailsCompo.addModel(model);
		modelsDetailsCompo.redraw();
	}
	
	protected void clearModels() {
		this.materialModels.clear();
		this.manyLineTV.setInput(this.materialModels);
		modelsDetailsCompo.clearModels();
		this.clearLines();
	}
	
	protected void clearLines() {
		this.drawPowderLines(new HashSet<PowderLinesModel>());
	}
	
	private void drawPowderLines(Set<PowderLinesModel> drawModels) {
		// Lines from the material models
		
		List<Double> plotLineList = new ArrayList<>();
		// Iterate over the material models
		for (PowderLinesModel linesModel : drawModels) {
			for (PowderLineModel lineModel : linesModel.getLineModels())
				plotLineList.add(lineModel.get(plotCoordinate));
		}
		DoubleDataset plotLineLocations = (!plotLineList.isEmpty()) ? (DoubleDataset) DatasetFactory.createFromList(plotLineList) : DatasetFactory.zeros(0);
		
		final XAxisLineBoxROI[] novalines = makeROILines(plotLineLocations);
		final List<IRegion> viejoRegions = (currentLineRegions != null) ? new ArrayList<IRegion>(currentLineRegions) : null;
		final List<IRegion> novaRegions = new ArrayList<IRegion>();
		
		// Keep track of our region names, since we are not adding them to the
		// PlottingSystem until the syncExec call
		List<String> usedNames = new ArrayList<String>();
		
		for (XAxisLineBoxROI line : novalines) {
			try {
				IRegion rLine = getPlottingSystem().createRegion(RegionUtils.getUniqueName("PowderLine", getPlottingSystem(), usedNames.toArray(new String[]{})), RegionType.XAXIS_LINE);
				usedNames.add(rLine.getName());
				rLine.setROI(line);
				novaRegions.add(rLine);
			} catch (Exception e) {
				System.err.println("Failed creating region for new powder line.");
			}
		}
		currentLineRegions = novaRegions;
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
					for (IRegion lineRegion : novaRegions) {
						try {
							getPlottingSystem().addRegion(lineRegion);
							lineRegion.setMobile(false);
						} catch (Exception e) {
							logger.error("PowderLineTool: Cannot create line region", e);
						}
					}
					// Remove the ROIs that constitute the old lines
					if (viejoRegions != null) {
						for (IRegion lineRegion : viejoRegions) {
							try {
								getPlottingSystem().removeRegion(lineRegion);
							} catch (Exception e) {
								logger.error("PowderLineTool: Cannot remove line region", e);
							}
						}
					}
			}
		});
	}
	
	private XAxisLineBoxROI[] makeROILines(Dataset locations) {
		
		List<XAxisLineBoxROI> novalines = new ArrayList<XAxisLineBoxROI>();
		
		IndexIterator iter = locations.getIterator();
		while(iter.hasNext())
			novalines.add(new XAxisLineBoxROI(locations.getElementDoubleAbs(iter.index), 0, 0, 1, 0));
		
		return novalines.toArray(new XAxisLineBoxROI[]{});
	}
	
	/**
	 * Refreshes the table and line locations
	 */
	protected void refresh(boolean refreshPlot) {
		this.drawGenericTable();
//		this.drawDomainSpecific(model);
		this.drawPowderLines(this.materialModels);
	}
	
	private void createActions() {
		final Shell theShell = this.getSite().getShell();
		final PowderLineTool theTool = this;

		getSite().getActionBars().getToolBarManager().add( new LoadAction(theShell, theTool));
		
		final Action coordinateAction = new Action("Set up the coordinates of the plot and lines", Activator.getImageDescriptor("icons/bullet_wrench.png")) {
			@Override
			public void run() {
				PowderLineSettingsDialog dialog = new PowderLineSettingsDialog(theShell);
				dialog.setCurrentValues(theTool.model.getEnergy(), plotCoordinate);
				if (dialog.open() == Window.OK) {
					theTool.model.setEnergy(dialog.getEnergy());
					theTool.setCoords(dialog.getCoords());
					
					theTool.refresh(true);
				}
			}
		};
		getSite().getActionBars().getToolBarManager().add(coordinateAction);
		
		final Action clearAction = new Action("Clear the lines", Activator.getImageDescriptor("icons/delete.gif")) {
			@Override
			public void run() {
				theTool.clearModels();
			}
		};
		getSite().getActionBars().getToolBarManager().add(clearAction);
	}

	public void drawGenericTable() {
		this.manyLineTV.setInput(materialModels);
	}
	
	private void setLengthScale(double lengthScale) {
		this.drawGenericTable();
		this.drawPowderLines(this.materialModels);
	}
	
	protected class LoadAction extends Action {
		protected Shell theShell;
		protected PowderLineTool theTool;
		private String[] dSpacingNames;
		public LoadAction() {
			super();
			this.setText("Load a list of lines from file");
			this.setImageDescriptor(Activator.getImageDescriptor("icons/import_wiz.png"));
			// names that a Dataset of d spacings might take
			dSpacingNames = new String[]{"d", "d-spacing"};
		}
		
		public LoadAction(Shell theShell, PowderLineTool theTool) {
			this();
			this.theShell = theShell;
			this.theTool = theTool;
		}
		
		@Override
		public void run() {
			FileDialog chooser = new FileDialog(theShell, SWT.OPEN);
			String chosenFile = chooser.open();
			
			ILoaderService loaderService = ServiceLoader.getLoaderService();
			IDataHolder dataHolder = null;
			// Get the data from the file
			try {
				dataHolder = loaderService.getData(chosenFile, null);
			
			} catch (Exception e) {
				if (chosenFile != null)
					logger.info("PowderLineTool: Could not read line data from " + chosenFile + ".");
				return;
			}
			boolean haveData = false;
			DoubleDataset lines = null;
			// Try to read a named Dataset
			for (String dName : dSpacingNames) {
				Dataset theDataset= DatasetUtils.convertToDataset(dataHolder.getDataset(dName));
				if (theDataset != null && theDataset.getDType() == Dataset.FLOAT) {
					lines = (DoubleDataset) DatasetUtils.convertToDataset(theDataset);
					haveData = true;
				}
			}
			
			if (!haveData) {
				// Only one Dataset, get it, it is the first
				Dataset theDataset= DatasetUtils.convertToDataset(dataHolder.getDataset(0));
				//			System.err.println("Dataset name is "+dataHolder.getName(0));
				// Stop reading if there is no valid data
				if (theDataset == null) {
					logger.info("PowderLineTool: No valid data in file " + chosenFile + ".");
					return;
				}
				if (theDataset.getDType() != Dataset.FLOAT) {
					logger.info("PowderLineTool: No valid double data found in file " + chosenFile + ".");
					return;
				}

				lines = (DoubleDataset) theDataset;
			}
			
			// the model of the data to create
			PowderLinesModel nyModel = null;
			
			// Now check for metadata
			IMetadata metadata = dataHolder.getMetadata();
			if (metadata != null) { 
				System.err.println("PowderLineTool: Metadata found!");
				try {
					if (metadata.getMetaNames().contains("K0"))
						System.err.println("PowderLineTool: Equation of State metadata found!");
					EoSLinesModel eosModel = new EoSLinesModel();
					eosModel.setBulkModulus(Double.parseDouble((String) metadata.getMetaValue("K0")));
					eosModel.setBulkModulus_p(Double.parseDouble((String) metadata.getMetaValue("K0P")));
					eosModel.setPressure(0.);
					eosModel.setComment((String) metadata.getMetaValue("COMMENT")); 
					nyModel = eosModel;
					
				} catch (Exception mE) {
					; // do nothing, the model has not been overwritten
				}
			}
			
			// New, multiple file loading
			if (nyModel == null) {
				nyModel = new PowderLinesModel();
			}
			nyModel.setDescription(chosenFile);
			nyModel.setWavelength(theTool.model.getWavelength());
			nyModel.setCoords(PowderLineCoord.D_SPACING);
			nyModel.setLines(lines);

			theTool.addMaterialModel(nyModel);
			
			theTool.refresh(true);

		}
	}
	
	
	public class PowderLineSettingsDialog extends Dialog {

		private double energy;
		private PowderLinesModel.PowderLineCoord coords;
		
		private Text energyText;
		private Combo coordCombo;
		
		public PowderLineSettingsDialog(Shell parent) {
			super(parent);
		}

		@Override
		public void create() {
			super.create();
			setTitle("Powder Line Tool Settings");
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout  = new GridLayout(2, false);
			container.setLayout(layout);
			
			createCoordinateDropdown(container);
			createEnergyBox(container);
			
			return area;
		}
		
		private void createCoordinateDropdown(Composite container) {
			Label coordLabel = new Label(container, SWT.NONE);
			coordLabel.setText("Plot coordinates");
			
			coordCombo = new Combo(container, SWT.BORDER);
			String[] coordsItems = new String[]{
					PowderLinesModel.PowderLineCoord.Q.name(),
					PowderLinesModel.PowderLineCoord.ANGLE.name(),
					PowderLinesModel.PowderLineCoord.D_SPACING.name()
			};
			coordCombo.setItems(coordsItems);
			// Select the current coordinates
			int currentIndex = Arrays.asList(coordsItems).indexOf(coords.name()); 
			coordCombo.select(currentIndex);
			
		}
		
		private void createEnergyBox(Composite container) {
			Label energyLabel = new Label(container, SWT.NONE);
			energyLabel.setText("Energy (keV)");
			
			energyText = new Text(container, SWT.BORDER);
			energyText.setText(Double.toString(energy));
			
		}
		
		public void setCurrentValues(double energy, PowderLinesModel.PowderLineCoord coords) {
			this.energy = energy;
			this.coords = coords;
		}
		
		public double getEnergy() {
			return this.energy;
		}
		
		public PowderLinesModel.PowderLineCoord getCoords() {
			return this.coords;
		}
		
		@Override
		protected void okPressed() {
			this.energy = Double.parseDouble(energyText.getText());
			this.coords = PowderLinesModel.PowderLineCoord.valueOf(coordCombo.getItems()[coordCombo.getSelectionIndex()]);
			super.okPressed();
		}
		
	}
	
	protected class ManyLineCP implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			Set<?> setOfModels = (Set<?>) inputElement;
			List<PowderLineModel> allLineModels = new ArrayList<>();
			
			for (Object iModel: setOfModels) {
				if (iModel instanceof PowderLinesModel) {
					PowderLinesModel materialModel = (PowderLinesModel) iModel;
					allLineModels.addAll(materialModel.getLineModels());
				}
			}
			
			return allLineModels.toArray();
		}
		
	}
	
	// Composite wrapping the details of each model
	private class ModelsDetailsComposite extends Composite {
		private List<PowderLinesModel> models;
		private List<Composite> modelDetails;
		
		public ModelsDetailsComposite(Composite parent, int style) {
			super(parent, style);
			models = new ArrayList<>();
			modelDetails = new ArrayList<>();
		}
		
		public void addModel(PowderLinesModel model) {
			models.add(model);
			redraw();
		}
		
		public void clearModels() {
			models.clear();
			for (Composite subCompo : modelDetails)
				subCompo.dispose();
			modelDetails.clear();
		}
		
		@Override
		public void redraw() {
			for (Composite subCompo : modelDetails)
				subCompo.dispose();
			modelDetails.clear();
			
			this.setLayout(new GridLayout(1, true));
			
			for (PowderLinesModel drawModel : models) {
				ModelDetailsComposite modelCompo = new ModelDetailsComposite(this, SWT.NONE);
				modelCompo.setModel(drawModel);
				modelCompo.redraw();
				modelCompo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
				modelDetails.add(modelCompo);

				Composite detailsCompo = drawModel.getModelSpecificDetailsComposite(this, SWT.NONE);
				detailsCompo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
				modelDetails.add(detailsCompo);
			}
			this.layout();
		}
	}
	
	protected class ModelDetailsComposite extends Composite {
		PowderLinesModel model;
		Label filenameText;
		ColorSelector colourSelector;
		Button deleteButton;
		Composite modelSpecific;

		public ModelDetailsComposite(Composite parent, int style) {
			super(parent, style);
		}
		
		// Set the model to display
		public void setModel(PowderLinesModel model) {
			this.model = model;
		}
		
		// Redraw the composite, e.g. when the model pressure changes
		@Override
		public void redraw() {
			// Layout
			GridLayout layout = new GridLayout(3, false);
			this.setLayout(layout);
			
			// Generic details
			filenameText = new Label(this, SWT.SINGLE | SWT.LEAD);
			filenameText.setText(model.getDescription());
			filenameText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			
			colourSelector = new ColorSelector(this);
			colourSelector.getButton().setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			// TODO: Add the colour call-back
			colourSelector.setColorValue(new RGB(255, 0, 255));
			
			deleteButton = new Button(this, SWT.NONE);
			deleteButton.setText("×");
			deleteButton.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
			deleteButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.err.println("Don't press this button again!");
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
			});
		}
	}

	// A composite for the details of a generic powder line model
	static class GenericDetailsComposite extends Composite {
		Label genericLabel;
		public GenericDetailsComposite(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new FillLayout());
			
			genericLabel = new Label(this, SWT.NONE);
			genericLabel.setText("No metadata");
		}
	}
	
}
