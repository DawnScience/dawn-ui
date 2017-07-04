package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.AxisEvent;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTool extends AbstractToolPage implements IROIListener, ITraceListener {
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTool.class);
	
	private Composite composite;
	private Spinner lowerBoundSpinner;
	private Spinner upperBoundSpinner;
	private double lowerBound;
	private double upperBound;

	private IRegion region;
	private TableViewer integrationResultsTable;
	
	private final ArrayList<ILineTrace> traces = new ArrayList<>();

	private ModifyListener lowerBoundModifyListener;
	private ModifyListener upperBoundModifyListener;

	private double lowerRange;
	private double upperRange;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public Control getControl() {
		if (composite==null) return null;
		return composite;
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

	private void roiEventHandler(ROIEvent evt) {
		lowerBoundSpinner.setEnabled(true);
		upperBoundSpinner.setEnabled(true);
		IROI roi = evt.getROI();
		double[] point = roi.getBounds().getPoint();
		double[] endPoint = roi.getBounds().getEndPoint();
		lowerBound = (int) point[0];
		upperBound = (int) endPoint[0];
		updateSpinnerRanges();
		update();
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		roiEventHandler(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		roiEventHandler(evt);
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		roiEventHandler(evt);
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label tempLabel = new Label(composite, SWT.NONE);
		tempLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		tempLabel.setText("Lower bound");
		lowerBoundSpinner = new Spinner(composite, SWT.NONE);
		lowerBoundSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		lowerBoundModifyListener = e -> {
			String text = lowerBoundSpinner.getText();
			int value = lowerBoundSpinner.getSelection();
			if (text.length() == 0)
				return;
			if (value < lowerBoundSpinner.getMinimum() || value > lowerBoundSpinner.getMaximum())
				return;
			IROI newRoi = new XAxisBoxROI(value, 0.0, upperBound-value, 0.0, 0.0);
			region.setROI(newRoi);
		}; 
	
		lowerBoundSpinner.addModifyListener(lowerBoundModifyListener);
		tempLabel = new Label(composite, SWT.NONE);
		tempLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		tempLabel.setText("Upper bound");
		upperBoundSpinner = new Spinner(composite, SWT.NONE);
		upperBoundSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		upperBoundModifyListener = e -> {
			String text = upperBoundSpinner.getText();
			int value = upperBoundSpinner.getSelection();
			if (text.length() == 0)
				return;
			if (value < upperBoundSpinner.getMinimum() || value > upperBoundSpinner.getMaximum())
				return;
			IROI newRoi = new XAxisBoxROI(lowerBound, 0.0, value-lowerBound, 0.0, 0.0);
			region.setROI(newRoi);
		}; 
		upperBoundSpinner.addModifyListener(upperBoundModifyListener);

		// disable spinners until region is available
		lowerBoundSpinner.setEnabled(false);
		upperBoundSpinner.setEnabled(false);
	
		integrationResultsTable = new TableViewer(composite);
		integrationResultsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		integrationResultsTable.getTable().setHeaderVisible(true);
		integrationResultsTable.getTable().setLinesVisible(true);
		integrationResultsTable.setContentProvider(new ArrayContentProvider());
		
		// add columns
		final TableViewerColumn column1 = new TableViewerColumn(integrationResultsTable, SWT.NONE, 0);
		column1.getColumn().setText("Trace");
		column1.getColumn().setWidth(100);
		column1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILineTrace trace = (ILineTrace) element;
				String name = trace.getDataName();
				name = name.substring(name.lastIndexOf('/')+1);
				return name;
			}
			
			@Override
			public String getToolTipText(Object element) {
				ILineTrace trace = (ILineTrace) element;
				return trace.getDataName();
			}
		});
		
		final TableViewerColumn column2 = new TableViewerColumn(integrationResultsTable, SWT.NONE, 1);
		column2.getColumn().setText("ROI sum");
		column2.getColumn().setWidth(100);
		column2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILineTrace trace = (ILineTrace) element;
				DoubleDataset xData = DatasetUtils.cast(DoubleDataset.class, trace.getXData());
				DoubleDataset yData = DatasetUtils.cast(DoubleDataset.class, trace.getYData());
				int lowerBoundIndex = DatasetUtils.findIndexGreaterThanOrEqualTo(xData, lowerBound);
				int upperBoundIndex = DatasetUtils.findIndexGreaterThanOrEqualTo(xData, upperBound);
				return String.format("%g", yData.getSlice(new int[]{lowerBoundIndex}, new int[]{upperBoundIndex + 1}, null).sum());
			}
		});
		
		integrationResultsTable.setInput(traces);
		
		
		super.createControl(parent);
	}

	private void updateSpinnerRanges() {
		lowerBoundSpinner.removeModifyListener(lowerBoundModifyListener);
		upperBoundSpinner.removeModifyListener(upperBoundModifyListener);
		lowerBoundSpinner.setMinimum((int) Math.min(lowerRange, lowerBound));
		lowerBoundSpinner.setMaximum((int) upperBound);
		upperBoundSpinner.setMinimum((int) lowerBound);
		upperBoundSpinner.setMaximum((int) Math.max(upperRange, upperBound));
		lowerBoundSpinner.addModifyListener(lowerBoundModifyListener);
		upperBoundSpinner.addModifyListener(upperBoundModifyListener);
	}
	
	@Override
	public void activate() {
		if (isActive() && !getTraces().isEmpty())
			return;
		IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem ==  null)
			return;
		plottingSystem.addTraceListener(this);
		List<IAxis> axes = plottingSystem.getAxes();
		IAxis axisX = axes.get(0);

		lowerRange = axisX.getLower();
		upperRange = axisX.getUpper();
		
		updateSpinnerRanges();
		
		axisX.addAxisListener(new IAxisListener() {
			
			@Override
			public void revalidated(AxisEvent evt) {
				// will not be used
			}
			
			@Override
			public void rangeChanged(AxisEvent evt) {
				// whenever the Xrange changes, I need to update the bounds
				lowerRange = (int) evt.getNewLower();
				upperRange = (int) evt.getNewUpper();
				updateSpinnerRanges();
			}
		});
		try {
			region = plottingSystem.createRegion("Integration range", RegionType.XAXIS);
			region.addROIListener(this);
			region.setUserRegion(false);
		} catch (Exception e) {
			logger.error("Could not create region", e);
		} 
		update();
		super.activate();
	}

	@Override
	public void deactivate() {
		logger.debug("IntegrationTool deactivate called");
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(this);
			if (region != null) {
				region.removeROIListener(this);
				getPlottingSystem().removeRegion(region);
				region = null;
			}
		}
		traces.clear();
		super.deactivate();
	}
	
	protected void update() {
		lowerBoundSpinner.removeModifyListener(lowerBoundModifyListener);
		upperBoundSpinner.removeModifyListener(upperBoundModifyListener);
		lowerBoundSpinner.setSelection((int) lowerBound);
		upperBoundSpinner.setSelection((int) upperBound);
		lowerBoundSpinner.addModifyListener(lowerBoundModifyListener);
		upperBoundSpinner.addModifyListener(upperBoundModifyListener);
		
		traces.clear();
		traces.addAll(getPlottingSystem().getTracesByClass(ILineTrace.class));
		integrationResultsTable.refresh();
	}
	
	@SuppressWarnings("unchecked")
	protected boolean checkEvent(TraceEvent evt) {
		
		//First, if the event source is not a list or ITrace ignore event
		if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
			return false;
		}
		List<ITrace> eventSource = new ArrayList<>();
		if (evt.getSource() instanceof List<?>)
			eventSource = (List<ITrace>)evt.getSource();
		if (evt.getSource() instanceof ITrace) {
			eventSource.clear();
			eventSource.add((ITrace)evt.getSource());
		}
		
		for (ITrace t : eventSource) if (t.getUserObject() instanceof ITrace) return false;
		
		return true;
		
	}

	@Override
	public void traceAdded(TraceEvent evt) {
		if (!checkEvent(evt)) return;
		update();
	}
			
	@Override
	public void tracesAdded(TraceEvent evt) {
		if (!checkEvent(evt)) return;
		update();
	}
			
	@Override
	public void traceUpdated(TraceEvent evt) {
		if (!checkEvent(evt)) return;
		update();
	}
			
	@Override
	public void tracesUpdated(TraceEvent evt) {
		if (!checkEvent(evt)) return;
		update();
	}
			
	@Override
	public void traceRemoved(TraceEvent evt) {
		update();
	}
			
	@Override
	public void tracesRemoved(TraceEvent evt) {
		update();
	}

	@Override
	public void traceCreated(TraceEvent evt) {
		// ignore
	}

	@Override
	public void traceWillPlot(TraceWillPlotEvent evt) {
		// ignore
	}

	public List<ILineTrace> getTraces() {
		return traces;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}
}
