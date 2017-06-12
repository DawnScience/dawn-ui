package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTool extends AbstractToolPage implements IROIListener, ITraceListener {
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTool.class);
	
	private Composite composite;
	private Label lowerBoundLabel;
	private Label upperBoundLabel;
	private double lowerBound;
	private double upperBound;

	private IRegion region;
	private TableViewer integrationResultsTable;
	
	
	
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
		IROI roi = evt.getROI();
		double[] point = roi.getBounds().getPoint();
		double[] endPoint = roi.getBounds().getEndPoint();
		lowerBound = point[0];
		upperBound = endPoint[0];
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
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		Label tempLabel = new Label(composite, SWT.NONE);
		tempLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		tempLabel.setText("Lower bound");
		lowerBoundLabel = new Label(composite, SWT.NONE);
		lowerBoundLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		tempLabel = new Label(composite, SWT.NONE);
		tempLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		tempLabel.setText("Upper bound");
		upperBoundLabel = new Label(composite, SWT.NONE);
		upperBoundLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	
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
		
		
		
		super.createControl(parent);
	}
	
	@Override
	public void activate() {
		if (isActive()) return;
		if (getPlottingSystem() ==  null) return;
		getPlottingSystem().addTraceListener(this);
		try {
			region = getPlottingSystem().createRegion("Integration range", RegionType.XAXIS);
			region.addROIListener(this);
		} catch (Exception e) {
			logger.error("Could not create region", e);
		} 
		update();
		super.activate();
	}

	@Override
	public void deactivate() {
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(this);
			if (region != null) {
				region.removeROIListener(this);
				getPlottingSystem().removeRegion(region);
			}
		}
		super.deactivate();
	}
	
	private synchronized void update() {
		lowerBoundLabel.setText(String.format("%g", lowerBound));
		upperBoundLabel.setText(String.format("%g", upperBound));
		
		Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
		integrationResultsTable.setInput(traces);
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
}
