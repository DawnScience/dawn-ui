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
	private PowderLineUnit dataCoordinate = PowderLineUnit.Q; // The coordinate of the input data 
	private double energy; // Energy of the scattered radiation
	private DoubleDataset lineLocations; // Locations of the lines in the above units
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
		lineLocations = DatasetFactory.createFromList(DoubleDataset.class, Arrays.asList(ArrayUtils.toObject(new double []{3.0, 3.4, 4.85, 5.74, 10.18})));
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
		
		// Activating the tool is the time to first draw the regions, I think
		drawPowderLines();
		
		super.activate();
	}
	
	/**
	 * Deactivate the tool.
	 */
	public void deactivate() {
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
		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, 0);
		colvarTheMagnificent.getColumn().setText("Q (Å⁻¹)");
		colvarTheMagnificent.getColumn().setWidth(400); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(dataCoordinate, PowderLineUnit.Q, energy));
		
		colvarTheMagnificent = new TableViewerColumn(lineTableViewer, SWT.CENTER, 1);
		colvarTheMagnificent.getColumn().setText("2θ (°)");
		colvarTheMagnificent.getColumn().setWidth(400); // a reasonable width
		colvarTheMagnificent.setLabelProvider(new PowderLineLabelProvider(dataCoordinate, PowderLineUnit.ANGLE, energy));
		
	}
	
	public enum PowderLineUnit {
		Q, ANGLE, D_SPACING
	}
	
	private class PowderLineLabelProvider extends ColumnLabelProvider {
		private PowderLineUnit dataCoordinate, columnCoordinate;
		private double energy;
		private static final double hckeVAA = 12.39841974;//(17)
		private DecimalFormat format = new DecimalFormat("#.###");
		
		public PowderLineLabelProvider(PowderLineUnit dataCoordinate, PowderLineUnit columnCoordinate, double energy) {
			this.dataCoordinate = dataCoordinate;
			this.columnCoordinate = columnCoordinate;
			this.energy = energy;
			this.format = new DecimalFormat("#.###");
		}
		
		@Override
		public String getText(Object element) {
			double value = (double) element;
			
			if (this.dataCoordinate == this.columnCoordinate)
				return format.format(value);
			else if (this.dataCoordinate == PowderLineUnit.Q)
				// Column is 2θ, data is Q
				return format.format(Math.toDegrees(2*Math.asin(value/this.energy * hckeVAA/(4*Math.PI))));
			else if (this.dataCoordinate == PowderLineUnit.ANGLE)
				// Column is Q, data is 2θ
				return format.format(4*Math.PI*this.energy*Math.sin(Math.toRadians(value)/2)/hckeVAA);
			else 
				return "";
		}
	}
	
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	
	public double getEnergy( ) {
		return this.energy;
	}
	
	public void setUnit(PowderLineUnit unit) {
		this.dataCoordinate = unit;
	}
	
	public PowderLineUnit getUnit( ) {
		return this.dataCoordinate;
	}
	
	public void setLines(DoubleDataset novaLines) {
		this.lineLocations = novaLines;
		this.lineTableViewer.setInput(this.lineLocations);
		this.drawPowderLines();
	}
	
	private void drawPowderLines() {
		// Correct the stored lines for the plot units
		// FIXME: Currently assumed to be the same units
		
		final XAxisLineBoxROI[] novalines = makeROILines(this.lineLocations);
		final List<IRegion> viejoRegions = (currentLineRegions != null) ? new ArrayList<IRegion>(currentLineRegions) : null;
		final List<IRegion> novaRegions = new ArrayList<IRegion>();
		
		// Keep track of our region names, since we are not adding them to the
		// PlottingSystem until the async call
		List<String> usedNames = new ArrayList<String>();
		
		for (XAxisLineBoxROI line : novalines) {
			try {
				IRegion rLine = getPlottingSystem().createRegion(RegionUtils.getUniqueName("PowderLine", getPlottingSystem(), usedNames.toArray(new String[]{})), RegionType.XAXIS_LINE);
				usedNames.add(rLine.getName());
				rLine.setROI(line);
				rLine.setMobile(false);
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
	
	private void createActions() {
		final Shell theShell = this.getSite().getShell();
		final PowderLineTool theTool = this;
		final Action loadAction = new Action("Load a list of lines from file", Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
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
					System.err.println("PowderLineTool: Could not read line data from " + chosenFile);
				}
				// Only one Dataset, get it, it is the first
				DoubleDataset lines = (DoubleDataset) DatasetUtils.convertToDataset(dataHolder.getDataset(0));
				System.err.println("Dataset name is "+dataHolder.getName(0));
				theTool.setLines(lines);
			}
		};
		getSite().getActionBars().getToolBarManager().add(loadAction);
		
		final Action coordinateAction = new Action("Set up the coordinates of the plot and lines", Activator.getImageDescriptor("icons/bullet_wrench.png")) {
			@Override
			public void run() {
				PowderLineSettingsDialog dialog = new PowderLineSettingsDialog(theShell);
				dialog.setCurrentValues(energy, dataCoordinate);
				if (dialog.open() == Window.OK) {
					theTool.setEnergy(dialog.getEnergy());
					theTool.setUnit(dialog.getUnit());
				}
			}
		};
		getSite().getActionBars().getToolBarManager().add(coordinateAction);
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
//			this.unit = PowderLineUnit.valueOf(unitText.getText());
			this.unit = PowderLineUnit.valueOf(unitCombo.getItems()[unitCombo.getSelectionIndex()]);
			System.out.println("Energy = "+this.energy);
			System.out.println("units = "+this.unit);
			super.okPressed();
		}
		
	}
}
