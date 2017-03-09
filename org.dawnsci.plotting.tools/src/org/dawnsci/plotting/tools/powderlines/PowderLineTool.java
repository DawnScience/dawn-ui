package org.dawnsci.plotting.tools.powderlines;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.ServiceLoader;
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
import org.eclipse.january.dataset.Maths;
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
	private PowderLineUnit plotCoordinate = PowderLineUnit.Q; // The coordinate of the input data
	private double energy; // Energy of the scattered radiation
	private DoubleDataset lineLocations; // Locations of the lines in d spacing
	private List<IRegion> currentLineRegions;
	
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
		
		// Example data
		lineLocations = DatasetFactory.createRange(0);
		energy = 76.6;
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
		
		lineTableViewer.setInput(lineLocations);
		
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
		lineLocations = DatasetFactory.createRange(0);
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
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(PowderLineUnit.D_SPACING, energy));

		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("Q (Å⁻¹)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(PowderLineUnit.Q, energy));
		
		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, iCol++);
		colvarTheMagnificent.getColumn().setText("2θ (°)");
		colvarTheMagnificent.getColumn().setWidth(300); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(PowderLineUnit.ANGLE, energy));
		
	}
	
	public enum PowderLineUnit {
		Q, ANGLE, D_SPACING
	}
	
	private class PowderLineLabelProvider extends ColumnLabelProvider {
		private PowderLineUnit /*dataCoordinate = PowderLineUnit.D_SPACING,*/ columnCoordinate;
		private DecimalFormat format = new DecimalFormat("#.###");
		
		public PowderLineLabelProvider(PowderLineUnit columnCoordinate, double energy) {
			this.columnCoordinate = columnCoordinate;
		}
		
		@Override
		public String getText(Object element) {
			double value = (double) element;
			DoubleDataset ddValue = (DoubleDataset) DatasetFactory.createFromObject(new Double[]{value});

			switch (columnCoordinate) {
			case Q:
				return format.format(qFromD(ddValue).getDouble(0));
			case ANGLE:
				return format.format(twoThetaFromD(ddValue, energy).getDouble(0));
			case D_SPACING:
				return format.format(value);
			default:
				return "";
			}
		}
	}
	
	private void setEnergy(double energy) {
		this.energy = energy;
	}
	
//	private double getEnergy( ) {
//		return this.energy;
//	}
	
	private void setUnit(PowderLineUnit unit) {
		this.plotCoordinate = unit;
	}
	
//	private PowderLineUnit getUnit( ) {
//		return this.plotCoordinate;
//	}
	
	private void setLines(DoubleDataset novaLines) {
		this.lineLocations = novaLines;
		this.lineTableViewer.setInput(this.lineLocations);
		this.drawPowderLines();
	}
	
	private void clearLines( ) {
		this.setLines(DatasetFactory.createRange(0));
	}
	
	// Coordinate conversions
	private static DoubleDataset qFromD(DoubleDataset d) {
		return (d.getSize() > 0) ? (DoubleDataset) Maths.divide(Math.PI*2, d) : d;
	}

	private static DoubleDataset twoThetaFromD(DoubleDataset d, double energy) {
		final double hc_keVAA = 12.398_419_739;
		double wavelength = hc_keVAA/energy;
		
		return (d.getSize() > 0) ? (DoubleDataset) Maths.toDegrees(Maths.multiply(2, Maths.arcsin(Maths.divide(wavelength/2, d)))) : d;
	}
	
	private void drawPowderLines() {
		// Correct the stored lines for the plot units
		
		
		
		DoubleDataset plotLineLocations;
		switch (plotCoordinate) {
		case Q:
			plotLineLocations = qFromD(lineLocations);
			break;
		case ANGLE:
			plotLineLocations = twoThetaFromD(lineLocations, energy);
			break;
		case D_SPACING:
		default:
			plotLineLocations = lineLocations;
		}
		
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
		final Action loadAction = new Action("Load a list of lines from file", Activator.getImageDescriptor("icons/import_wiz.png")) {
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
						System.err.println("PowderLineTool: Could not read line data from " + chosenFile + ".");
					return;
				}
				// Only one Dataset, get it, it is the first
				Dataset theDataset= DatasetUtils.convertToDataset(dataHolder.getDataset(0));
//				System.err.println("Dataset name is "+dataHolder.getName(0));
				// Stop reading if there is no valid data
				if (theDataset == null) {
					logger.info("PowderLineTool: No valid data in file " + chosenFile + ".");
					return;
				}
				if (theDataset.getDType() != Dataset.FLOAT) {
					logger.info("PowderLineTool: No valid double data found in file " + chosenFile + ".");
					return;
				}
					
				DoubleDataset lines = (DoubleDataset) DatasetUtils.convertToDataset(dataHolder.getDataset(0));
				theTool.setLines(lines);
			}
		};
		getSite().getActionBars().getToolBarManager().add(loadAction);
		
		final Action coordinateAction = new Action("Set up the coordinates of the plot and lines", Activator.getImageDescriptor("icons/bullet_wrench.png")) {
			@Override
			public void run() {
				PowderLineSettingsDialog dialog = new PowderLineSettingsDialog(theShell);
				dialog.setCurrentValues(energy, plotCoordinate);
				if (dialog.open() == Window.OK) {
					theTool.setEnergy(dialog.getEnergy());
					theTool.setUnit(dialog.getUnit());
					
					theTool.refresh();
				}
			}
		};
		getSite().getActionBars().getToolBarManager().add(coordinateAction);
		
		final Action clearAction = new Action("Clear the lines", Activator.getImageDescriptor("icons/delete.gif")) {
			@Override
			public void run() {
				theTool.clearLines();
			}
		};
		getSite().getActionBars().getToolBarManager().add(clearAction);
	}
	
	public class PowderLineSettingsDialog extends Dialog {

		private double energy;
		private PowderLineUnit unit;
		
		private Text energyText;
		private Combo unitCombo;
		
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
			Label unitLabel = new Label(container, SWT.NONE);
			unitLabel.setText("Plot units");
			
			unitCombo = new Combo(container, SWT.BORDER);
			String[] unitItems = new String[]{
					PowderLineUnit.Q.name(),
					PowderLineUnit.ANGLE.name(),
					PowderLineUnit.D_SPACING.name()
			};
			unitCombo.setItems(unitItems);
			// Select the current unit
			int currentIndex = Arrays.asList(unitItems).indexOf(unit.name()); 
			unitCombo.select(currentIndex);
			
		}
		
		private void createEnergyBox(Composite container) {
			Label energyLabel = new Label(container, SWT.NONE);
			energyLabel.setText("Energy (keV)");
			
			energyText = new Text(container, SWT.BORDER);
			energyText.setText(Double.toString(energy));
			
		}
		
		public void setCurrentValues(double energy, PowderLineUnit unit) {
			this.energy = energy;
			this.unit = unit;
		}
		
		public double getEnergy() {
			return this.energy;
		}
		
		public PowderLineUnit getUnit() {
			return this.unit;
		}
		
		@Override
		protected void okPressed() {
			this.energy = Double.parseDouble(energyText.getText());
			this.unit = PowderLineUnit.valueOf(unitCombo.getItems()[unitCombo.getSelectionIndex()]);
			super.okPressed();
		}
		
	}
}
