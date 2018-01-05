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
import org.dawnsci.plotting.tools.powderlines.EoSLineTool.EosDetailsComposite;
import org.dawnsci.plotting.tools.powderlines.PowderLinesModel.PowderLineCoord;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
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
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
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

/**
 * A tool to allow the importation of powder line data from either a 
 * raw data file, or from a JCPDS file. The latter will enable equation
 * of state features in the interface, such as pressure driven volume
 * changes, and corrections between the experimental and theoretical
 * unit cell volume.
 * 
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class PowderLineTool extends AbstractToolPage {

	private Composite composite; // root Composite of the tool
	private ITraceListener tracerListener; // The trace on which the tool listens
	private PowderLinesModel.PowderLineCoord plotCoordinate = PowderLinesModel.PowderLineCoord.Q; // The coordinate of the input data
	private List<IRegion> currentLineRegions;
	
	private TableViewer manyLineTV; // TableViewer holding the list of all lines
	
	private Set<PowderLinesModel> materialModels;
	
	private SashForm sashForm;
	// sub composites, needed to set the relative size for the different domains
	private Composite mTableCompo;
	private ModelsDetailsComposite modelsDetailsCompo;
	private Composite settingsOuterComposite;
	private SettingsComposite settingsComposite;
	
	protected PowderLinesModel model;
	
	static final PowderLinesModel.PowderLineCoord defaultCoords = PowderLineCoord.D_SPACING;

	/**
	 * 
	 * @author Timothy Spain, timothy.spain@diamond.ac.uk
	 *
	 * An enumeration of the possible types of file to be read in:
	 * without and
	 * with equation of state data
	 *
	 */
	public enum PowderDomains {
			POWDER,
			EQUATION_OF_STATE;
	}
	
	/**
	 * Default constructor for the Powder Line Tool
	 */
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
		
		// Settings
		settingsOuterComposite = new Composite(sashForm, SWT.NONE);
		
		// Create the table of all lines of all materials
		mTableCompo = new Composite(sashForm, SWT.NONE);
		manyLineTV = new TableViewer(mTableCompo, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createManyColumns(manyLineTV);
		
		manyLineTV.getTable().setLinesVisible(true);
		manyLineTV.getTable().setHeaderVisible(true);
		
		manyLineTV.setContentProvider(new ManyLineCP());
		manyLineTV.setInput(materialModels);
		
		modelsDetailsCompo = new ModelsDetailsComposite(sashForm, SWT.BORDER);
		modelsDetailsCompo.setTool(this);
		
		activate();
		
		super.createControl(parent);
		
		drawSettings();
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
	
	// Create the table for the data
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
	
	// label provider for the columns of the data table
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
	
	// Sets the coordinates of the main plot, so that the lines can be
	// displayed along the correct scale
	private void setCoords(PowderLinesModel.PowderLineCoord coord) {
		this.plotCoordinate = coord;
	}
	
	/**
	 * Adds a new model of the lines data to the set to be displayed
	 * @param model
	 * 				the data object for the new set of lines
	 */
	protected void addMaterialModel(PowderLinesModel model) {
		materialModels.add(model);
		this.manyLineTV.setInput(this.materialModels);
		modelsDetailsCompo.addModel(model);
		modelsDetailsCompo.redraw();
	}
	
	/**
	 * Clears all sets of line data from the tool.
	 */
	protected void clearModels() {
		this.materialModels.clear();
		this.manyLineTV.setInput(this.materialModels);
		modelsDetailsCompo.clearModels();
		this.clearLines();
	}
	
	/**
	 * Clears the lines on the plot.
	 */
	protected void clearLines() {
		this.drawPowderLines(new HashSet<PowderLinesModel>());
	}
	
	// Deletes a single model from the set to be displayed
	private void deleteModel(PowderLinesModel delModel) {
		if (materialModels.contains(delModel))
			materialModels.remove(delModel);
		this.manyLineTV.setInput(this.materialModels);
		modelsDetailsCompo.deleteModel(delModel);
		drawPowderLines(materialModels);
	}
	
	// Redraws the powder lines for all lines models
	private void redrawPowderLines() {
		drawPowderLines(materialModels);
	}
	
	//Redraws the powder lines for the given lines models
	private void drawPowderLines(Set<PowderLinesModel> drawModels) {
		// Lines from the material models
		
		// Keep track of our region names, since we are not adding them to the
		// PlottingSystem until the syncExec call
		List<String> usedNames = new ArrayList<>();

		final List<IRegion> gamlaRegions = (currentLineRegions != null) ? new ArrayList<>(currentLineRegions) : null;
		final List<IRegion> nyRegions = new ArrayList<>();

		for (PowderLinesModel linesModel : drawModels)
			nyRegions.addAll(makeRegionsFromModel(linesModel, plotCoordinate, usedNames, getPlottingSystem()));

		currentLineRegions = nyRegions;
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
					for (IRegion lineRegion : nyRegions) {
						try {
							getPlottingSystem().addRegion(lineRegion);
							lineRegion.setMobile(false);
						} catch (Exception e) {
							logger.error("PowderLineTool: Cannot create line region", e);
						}
					}
					// Remove the ROIs that constitute the old lines
					if (gamlaRegions != null) {
						for (IRegion lineRegion : gamlaRegions) {
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
	
	// Generate a set of correctly coloured RoIs from a single LinesModel
	private static List<IRegion> makeRegionsFromModel(PowderLinesModel model, PowderLinesModel.PowderLineCoord plotCoordinate, List<String> usedNames, IPlottingSystem<Object> plotSystem) {
		List<Double> plotLineList = new ArrayList<>();
		for (PowderLineModel lineModel: model.getLineModels())
			plotLineList.add(lineModel.get(plotCoordinate));

		if (plotLineList.isEmpty())
			return new ArrayList<>();
		
		DoubleDataset plotLineLocations = (DoubleDataset) DatasetFactory.createFromList(plotLineList);

		List<XAxisLineBoxROI> nylines = new ArrayList<>();
		IndexIterator iter = plotLineLocations.getIterator();
		while(iter.hasNext()) {
			XAxisLineBoxROI nyRoi = new XAxisLineBoxROI(plotLineLocations.getElementDoubleAbs(iter.index), 0, 0, 1, 0);
			nylines.add(nyRoi);
		}

		List<IRegion> nyRegions = new ArrayList<>();
		
		Color regionColour = new Color(Display.getCurrent(), model.getRegionRGB());
		
		for (XAxisLineBoxROI line : nylines) {
			try {
				IRegion rLine = plotSystem.createRegion(RegionUtils.getUniqueName("PowderLine", plotSystem, usedNames.toArray(new String[]{})), RegionType.XAXIS_LINE);
				rLine.setRegionColor(regionColour);
				usedNames.add(rLine.getName());
				rLine.setROI(line);
				nyRegions.add(rLine);
			} catch (Exception e) {
				System.err.println("Failed creating region for new powder line.");
			}
		}

		return nyRegions;

	}
	
	/**
	 * Refreshes the table and line locations
	 */
	protected void refresh(boolean refreshPlot) {
		this.drawGenericTable();
		this.drawPowderLines(this.materialModels);
		this.drawSettings();
	}
	
	// Creates the action (load, clear) button in the button bar 
	private void createActions() {
		final Shell theShell = this.getSite().getShell();
		final PowderLineTool theTool = this;

		getSite().getActionBars().getToolBarManager().add( new LoadAction(theShell, theTool));
		
		final Action clearAction = new Action("Clear the lines", Activator.getImageDescriptor("icons/delete.gif")) {
			@Override
			public void run() {
				theTool.clearModels();
			}
		};
		getSite().getActionBars().getToolBarManager().add(clearAction);
	}

	/**
	 * Sets the input of the generic data table to the lines models already defined
	 */
	public void drawGenericTable() {
		this.manyLineTV.setInput(materialModels);
	}
	
	// Sets the beam energy used to convert between real and momentum
	// space units
	private void setEnergy(double energy) {
		this.model.setEnergy(energy);
		for (PowderLinesModel materialModel : materialModels) {
			materialModel.setEnergy(energy);
		}
	}
	
	/**
	 * Sets the pressure exerted on each lines model.
	 * <p>
	 * Not generic. Depends on 'instanceof' to find EoS models.
	 * @param pressure
	 * 				Pressure exerted in the measurement environment in Pa
	 */
	public void setPressure(double pressure) {
		for (PowderLinesModel linesModel : materialModels) {
			if (linesModel instanceof EoSLinesModel)
				((EoSLinesModel) linesModel).setPressure(pressure);
		}
		refresh(true);
		modelsDetailsCompo.setPressure(pressure);
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
			
			// Now check for metadata. This is another place that the
			// difference between generic and EoS files impinges.
			IMetadata metadata = dataHolder.getMetadata();
			if (metadata != null) { 
				try {
					if (metadata.getMetaNames().contains("K0")) {
						EoSLinesModel eosModel = new EoSLinesModel();
						final double gpaFactor = 1e9; // gigapascals (file) in pascals (data)
						eosModel.setBulkModulus(Double.parseDouble((String) metadata.getMetaValue("K0"))*gpaFactor);
						eosModel.setBulkModulus_p(Double.parseDouble((String) metadata.getMetaValue("K0P")));
						eosModel.setPressure(0.); // 0 pascals
						eosModel.setComment((String) metadata.getMetaValue("COMMENT")); 
						nyModel = eosModel;
					}
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
			nyModel.setTool(theTool);

			theTool.addMaterialModel(nyModel);
			
			theTool.refresh(true);

		}
	}
	
	// Draws the settings composite, and resizes it if enough
	// information is known about the parent composite. 
	private void drawSettings() {
		if (settingsOuterComposite == null)
			return;

		settingsOuterComposite.setLayout(new FillLayout());
		
		if (settingsComposite == null) {
			settingsComposite = new SettingsComposite(settingsOuterComposite, SWT.NONE);
			settingsComposite.setTool(this);
			settingsComposite.redraw();
		}
		settingsOuterComposite.layout();
		
		Point compoSize = settingsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point sashSize = sashForm.getSize();
		int otherWeight = (sashSize.y - compoSize.y)/2;
		if (compoSize.y > 0 && otherWeight > 0)
			sashForm.setWeights(new int[] {(int) (compoSize.y*1.05), otherWeight, otherWeight});
	}
	
	// Content provider for the data table
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
		private PowderLineTool tool;
		private List<PowderLinesModel> models;
		private List<ModelDetailsComposite> modelDetails;
		private List<Composite> modelDetailsData;
		
		/**
		 * Default constructor.
		 * @param parent
		 * 				Parent {@link Composite}
		 * @param style
		 * 				Composite style parameters.
		 */
		public ModelsDetailsComposite(Composite parent, int style) {
			super(parent, style);
			models = new ArrayList<>();
			modelDetails = new ArrayList<>();
			modelDetailsData = new ArrayList<>();
		}
		
		/**
		 * Adds a new model to the model details
		 * @param model
		 * 				Model to be added
		 */
		public void addModel(PowderLinesModel model) {
			if (!models.contains(model)) {
				models.add(model);
				redraw();
			}
		}
		
		/**
		 * Sets the tool which contains the composite, for callback purposes.
		 * @param tool
		 * 			containing tool.
		 */
		public void setTool(PowderLineTool tool) {
			this.tool = tool;
		}
		
		/**
		 * Clears the UI elements for all models from the Composite.
		 */
		public void clearModels() {
			models.clear();
			for (Composite subCompo : modelDetails)
				subCompo.dispose();
			modelDetails.clear();
			for (Composite detailsCompo : modelDetailsData)
				detailsCompo.dispose();
			modelDetailsData.clear();
		}
		
		/**
		 * Deletes the UI elements for a single model from the
		 * Composite.
		 * @param model
		 * 				the model the details of which are to be
		 * 				removed.
		 */
		public void deleteModel(PowderLinesModel model) {
			if (models.contains(model)) {
				models.remove(model);
				redraw();
				this.tool.deleteModel(model);
			}
		}
		
		/**
		 * Instructs the tool to redraw the powder lines.
		 */
		public void redrawLines() {
			this.tool.redrawPowderLines();
		}
		
		@Override
		public void redraw() {
			for (Composite subCompo : modelDetails)
				subCompo.dispose();
			modelDetails.clear();
			for (Composite subCompo : modelDetailsData)
				subCompo.dispose();
			modelDetailsData.clear();
			
			this.setLayout(new GridLayout(1, true));
			
			for (PowderLinesModel drawModel : models) {
				ModelDetailsComposite modelCompo = new ModelDetailsComposite(this, SWT.NONE);
				modelCompo.setModel(drawModel);
				modelCompo.redraw();
				modelCompo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
				modelDetails.add(modelCompo);

				Composite detailsCompo = drawModel.getModelSpecificDetailsComposite(this, SWT.NONE);
				detailsCompo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
				modelDetailsData.add(detailsCompo);
			}
			this.layout();
		}
		
		/**
		 * Sets the pressure value for all relevant UI elements.
		 * @param pressure
		 * 				pressure value to set in Pa.
		 */
		public void setPressure(double pressure) {
			for (Composite detailsCompo: modelDetailsData) {
				if (detailsCompo instanceof EosDetailsComposite)
					((EosDetailsComposite) detailsCompo).setPressure(pressure);
			}
		}
	}
	
	/**
	 * The generic details of a lines model.
	 * @author Timothy Spain, timothy.spain@diamond.ac.uk
	 *
	 */
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
			colourSelector.setColorValue(model.getRegionRGB());
			
			colourSelector.getButton().addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					model.setRegionRGB(colourSelector.getColorValue());
					
					if (getParent() instanceof ModelsDetailsComposite) {
						((ModelsDetailsComposite) getParent()).redrawLines();
					}
				}

				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			deleteButton = new Button(this, SWT.NONE);
			deleteButton.setText("×");
			deleteButton.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
			deleteButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (getParent() instanceof ModelsDetailsComposite) {
						((ModelsDetailsComposite) getParent()).deleteModel(model);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
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
	
	// A Composite to report and change the settings, replacing the former dialog
	static class SettingsComposite extends Composite {
		private PowderLineTool tool;
		
		private Text energyText;
		private Combo coordCombo;

		public SettingsComposite(Composite parent, int style) {
			super(parent, style);
		}
		
		@Override
		public void redraw() {
			
			GridLayout layout = new GridLayout(11, false);
			this.setLayout(layout);
			
			// Plot coordinates
			Label coordLabel = new Label(this, SWT.RIGHT);
			coordLabel.setText("Plot coordinates");
			coordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			coordCombo = new Combo(this, SWT.BORDER);
			String[] coordsItems = new String[]{
					PowderLinesModel.PowderLineCoord.Q.name(),
					PowderLinesModel.PowderLineCoord.ANGLE.name(),
					PowderLinesModel.PowderLineCoord.D_SPACING.name()
			};
			coordCombo.setItems(coordsItems);
			// Select the current coordinates
			int currentIndex = Arrays.asList(coordsItems).indexOf(tool.plotCoordinate.name()); 
			coordCombo.select(currentIndex);
			coordCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			coordCombo.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					coordsChanged();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
			});
			
			Text spacer = new Text(this, SWT.SINGLE);
			spacer.setEditable(false);
			spacer.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
			
			// Beam energy box
			Label energyLabel = new Label(this, SWT.RIGHT);
			energyLabel.setText("Beam energy");
			energyLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			energyText = new Text(this, SWT.BORDER);
			energyText.setText(Double.toString(tool.model.getEnergy()));
			energyText.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			energyText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					energyChanged();
				}
			});
			
			Label unitsLabel = new Label(this, SWT.RIGHT);
			unitsLabel.setText("keV");
			unitsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			super.redraw();
			
			this.layout();
		}
		
		/**
		 * Sets the tool containing this Composite, for callback
		 * purposes.
		 * @param tool
		 * 			Containing tool.
		 */
		public void setTool(PowderLineTool tool) {
			this.tool = tool;
		}
		
		// Update the tool if the energy is changed.
		private void energyChanged() {
			
			double energy;
			try {
				energy = Double.parseDouble(energyText.getText());
			} catch (NumberFormatException nfe) {
				energy = 1.0; // default to 1 keV
			}
			tool.setEnergy(energy);
			tool.refresh(true);
		}
		
		// Update the tool if the plot coordinates type is changed.
		private void coordsChanged() {
			PowderLineCoord coord = PowderLineCoord.valueOf(coordCombo.getItem(coordCombo.getSelectionIndex()));
			tool.setCoords(coord);
			tool.refresh(true);
		}
	}
}
