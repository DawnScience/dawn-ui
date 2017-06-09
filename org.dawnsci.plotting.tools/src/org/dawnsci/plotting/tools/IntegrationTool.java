package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.jface.viewers.TableViewer;
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
	//private TableViewer integrationResultsTable;
	
	
	
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
		logger.debug("point: {}", Arrays.toString(point));
		logger.debug("endPoint: {}", Arrays.toString(endPoint));
		lowerBound = point[0];
		upperBound = endPoint[0];
		update();
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		logger.debug("Entering roiDragged");
		roiEventHandler(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		logger.debug("Entering roiChanged");
		roiEventHandler(evt);
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		logger.debug("Entering roiSelected");
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
		
		super.createControl(parent);
	}
	
	@Override
	public void activate() {
		if (isActive()) return;
		if (getPlottingSystem() ==  null) return;
		getPlottingSystem().addTraceListener(this);
		try {
			region = getPlottingSystem().createRegion("Integration range", RegionType.XAXIS);
			//region.setROI(new XAxisBoxROI(ptx, pty, width, height, angle));
			region.addROIListener(this);
		} catch (Exception e) {
			e.printStackTrace();
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
	}
	
	@SuppressWarnings("unchecked")
	protected boolean checkEvent(TraceEvent evt) {
		
		//First, if the event source is not a list or ITrace ignore event
		if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
			return false;
		}
		List<ITrace> eventSource = new ArrayList<ITrace>();
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
		Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
	
		if (traces == null || traces.isEmpty()) {
			return;
		}
		update();
	}
			
	@Override
	public void tracesRemoved(TraceEvent evt) {
		Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
		if (traces == null || traces.isEmpty()) {
			return;
		}
		update();
	}

	@Override
	public void traceCreated(TraceEvent evt) {
	}

	@Override
	public void traceWillPlot(TraceWillPlotEvent evt) {
	}
}
