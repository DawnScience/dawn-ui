package org.dawnsci.plotting.tools.powderlines;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.common.widgets.gda.Activator;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisLineBoxROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class PowderLineTool extends AbstractToolPage {

	private Composite composite; // root Composite of the tool
	private TableViewer lineTableViewer; // TableViewer holding the list of lines
	private ITraceListener tracerListener; // The trace on which the tool listens
	private PowderLineUnit dataCoordinate = PowderLineUnit.Q; // The coordinate of the input data 
	private double energy; // Energy of the scattered radiation
	private Double[] lineLocations; // Locations of the lines in the above units
	
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
		lineLocations = ArrayUtils.toObject(new double []{3.0, 3.4, 4.85, 5.74, 10.18});
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
				return (Double[]) inputElement;
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
	
	private enum PowderLineUnit {
		Q, ANGLE
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
	
	private void drawPowderLines() {
		System.err.println("(Re)drawing powder lines");
		
		// Correct the stored lines for the plot units
		// FIXME: Currently assumed to be the same units
		
		final XAxisLineBoxROI[] lines = makeROILines(this.lineLocations); 
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					for (XAxisLineBoxROI line : lines) {
						final IRegion rLine = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Line", getPlottingSystem()), RegionType.XAXIS);
						rLine.setROI(line);
						getPlottingSystem().addRegion(rLine);
					}
				} catch (Exception e) {
					logger.error("Cannot create line", e);
				}
			}
		});
	}
	
	private XAxisLineBoxROI[] makeROILines(Double locations[]) {
		List<XAxisLineBoxROI> lines = new ArrayList<XAxisLineBoxROI>();
		for(double location : locations)
			lines.add(new XAxisLineBoxROI(location, 0, 0, 1, 0));

		return lines.toArray(new XAxisLineBoxROI[]{});
	}
	
	private void createActions() {
		final Action loadAction = new Action("Load a list of lines from file", Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
			@Override
			public void run() {
				
			}
		};
		getSite().getActionBars().getToolBarManager().add(loadAction);
	}
}
