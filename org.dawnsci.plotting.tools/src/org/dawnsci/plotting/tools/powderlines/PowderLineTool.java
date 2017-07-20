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
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.dawnsci.plotting.tools.powderlines.PowderLineModel.PowderLineCoord;
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
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	private PowderLineModel.PowderLineCoord plotCoordinate = PowderLineModel.PowderLineCoord.Q; // The coordinate of the input data
	private List<IRegion> currentLineRegions;
	
	private PowderLineModel model;
	
	static final PowderLineModel.PowderLineCoord defaultCoords = PowderLineCoord.D_SPACING;
	
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
		
		model = new PowderLineModel();
		// Default data
		model.clearLines();
		model.setEnergy(76.6);
		
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
		
		// Create the table of lines
		lineTableViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(lineTableViewer);
		lineTableViewer.getTable().setLinesVisible(true);
		lineTableViewer.getTable().setHeaderVisible(true);
		// Create the Actions
		createActions();
		
		// define the content and the provider
		lineTableViewer.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return ArrayUtils.toObject(((DoubleDataset) inputElement).getData());
			}
		});
		
		lineTableViewer.setInput(model.getLines(defaultCoords));
		
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
		drawPowderLines();
		
		super.deactivate();
		
		// Remove the traceListener
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(tracerListener);
		}
	}
	
	// Create the table columns
	private void createColumns(final TableViewer viewer) {
		
		// Set the tooltip to not created more than once in the same area
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		

		
		// Create the columns
		TableViewerColumn colvarTheMagnificent;
		int iCol = 0;
		
		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("d spacing (Å)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(PowderLineModel.PowderLineCoord.D_SPACING));

		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("Q (Å⁻¹)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(PowderLineModel.PowderLineCoord.Q));
		
		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("2θ (°)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(PowderLineModel.PowderLineCoord.ANGLE));
		
	}
	
	private class PowderLineLabelProvider extends ColumnLabelProvider {
		private PowderLineModel.PowderLineCoord columnCoordinate;
		private DecimalFormat format = new DecimalFormat("#.###");
		
		public PowderLineLabelProvider(PowderLineModel.PowderLineCoord columnCoordinate/*, double energy*/) {
			this.columnCoordinate = columnCoordinate;
		}
		
		@Override
		public String getText(Object element) {
			double value = (double) element;
			return format.format(model.convertLinePositions(value, defaultCoords, columnCoordinate));
		}
	}
	
	private void setCoords(PowderLineModel.PowderLineCoord coord) {
		this.plotCoordinate = coord;
	}
	
	protected void setLines(DoubleDataset novaLines) {
		model.setLines(novaLines);
		model.setCoords(defaultCoords);
		this.lineTableViewer.setInput(model.getLines());
		this.drawPowderLines();
	}
	
	private void drawPowderLines() {
		// Correct the stored lines for the plot coordinates
		
		
		
		DoubleDataset plotLineLocations = model.getLines(plotCoordinate);
		
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
	private void refresh() {
		lineTableViewer.refresh();
		drawPowderLines();
		
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
					
					theTool.refresh();
				}
			}
		};
		getSite().getActionBars().getToolBarManager().add(coordinateAction);
		
		final Action clearAction = new Action("Clear the lines", Activator.getImageDescriptor("icons/delete.gif")) {
			@Override
			public void run() {
				theTool.model.clearLines();
			}
		};
		getSite().getActionBars().getToolBarManager().add(clearAction);
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
			theTool.setLines(lines);
		}
	}
			
	
	public class PowderLineSettingsDialog extends Dialog {

		private double energy;
		private PowderLineModel.PowderLineCoord coords;
		
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
					PowderLineModel.PowderLineCoord.Q.name(),
					PowderLineModel.PowderLineCoord.ANGLE.name(),
					PowderLineModel.PowderLineCoord.D_SPACING.name()
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
		
		public void setCurrentValues(double energy, PowderLineModel.PowderLineCoord coords) {
			this.energy = energy;
			this.coords = coords;
		}
		
		public double getEnergy() {
			return this.energy;
		}
		
		public PowderLineModel.PowderLineCoord getCoords() {
			return this.coords;
		}
		
		@Override
		protected void okPressed() {
			this.energy = Double.parseDouble(energyText.getText());
			this.coords = PowderLineModel.PowderLineCoord.valueOf(coordCombo.getItems()[coordCombo.getSelectionIndex()]);
			super.okPressed();
		}
		
	}
}
